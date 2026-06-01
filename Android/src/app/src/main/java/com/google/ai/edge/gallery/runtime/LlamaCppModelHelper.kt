package com.google.ai.edge.gallery.runtime

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.gallery.data.ConfigKeys
import com.google.ai.edge.gallery.data.DEFAULT_TEMPERATURE
import com.google.ai.edge.gallery.data.DEFAULT_TOPK
import com.google.ai.edge.gallery.data.DEFAULT_TOPP
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.engine.LlamaCppEngine
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ToolProvider
import com.google.ai.edge.litertlm.ToolManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jegly.offlineLLM.smollm.SmolLM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "BoxLlamaCppModelHelper"
private const val TRACE_TAG = "AGTrace"

/**
 * Box: LlmModelHelper implementation backed by llama.cpp for GGUF models.
 * Routes through the smollm JNI bridge.
 */
object LlamaCppModelHelper : LlmModelHelper {

    // Indexed by model name
    private val engines: MutableMap<String, LlamaCppEngine> = mutableMapOf()
    private val toolManagers: MutableMap<String, ToolManager> = mutableMapOf()
    private fun trace(label: String, value: String) {
        val singleLine = value.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        val chunkSize = 700
        if (singleLine.length <= chunkSize) {
            Log.i(TRACE_TAG, "$label: $singleLine")
            return
        }
        var idx = 0
        var part = 1
        while (idx < singleLine.length) {
            val end = (idx + chunkSize).coerceAtMost(singleLine.length)
            val piece = singleLine.substring(idx, end)
            Log.i(TRACE_TAG, "$label[$part]: $piece")
            idx = end
            part += 1
        }
    }
    private fun traceSystemPrompt(value: String) {
        Log.i(TRACE_TAG, "SYSTEM_PROMPT_BEGIN")
        Log.i(TRACE_TAG, value)
        Log.i(TRACE_TAG, "SYSTEM_PROMPT_END")
    }

    override fun initialize(
        context: Context,
        model: Model,
        supportImage: Boolean,
        supportAudio: Boolean,
        onDone: (String) -> Unit,
        systemInstruction: Contents?,
        tools: List<ToolProvider>,
        enableConversationConstrainedDecoding: Boolean,
        coroutineScope: CoroutineScope?,
    ) {
        if (tools.isNotEmpty()) {
            toolManagers[model.name] = ToolManager(tools)
        } else {
            toolManagers.remove(model.name)
        }

        val topK = model.getIntConfigValue(key = ConfigKeys.TOPK, defaultValue = DEFAULT_TOPK)
        val topP = model.getFloatConfigValue(key = ConfigKeys.TOPP, defaultValue = DEFAULT_TOPP)
        val temperature = model.getFloatConfigValue(
            key = ConfigKeys.TEMPERATURE, defaultValue = DEFAULT_TEMPERATURE
        )

        val modelPath = model.getPath(context = context)
        Log.d(TAG, "Initializing llama.cpp engine for: $modelPath")
        val systemPrompt = extractSystemPrompt(systemInstruction)
        Log.d(TAG, "llama.cpp system prompt length: ${systemPrompt.length}")
        Log.d(TAG, "llama.cpp applied system prompt (init):\n$systemPrompt")
        traceSystemPrompt(systemPrompt)
        val finalSystemPrompt = systemPrompt

        val engine = LlamaCppEngine()
        engines[model.name] = engine

        val params = SmolLM.InferenceParams(
            temperature = temperature,
            topP = topP,
            topK = topK,
            numThreads =
                (Runtime.getRuntime().availableProcessors() / 2)
                    .coerceAtLeast(1)
                    .coerceAtMost(4),
        )

        engine.loadModel(
            modelPath = modelPath,
            params = params,
            systemPrompt = finalSystemPrompt,
            onSuccess = {
                // Store a marker so the ViewModel knows the model is ready
                model.instance = engine
                onDone("")
            },
            onError = { e ->
                Log.e(TAG, "Failed to load GGUF model", e)
                onDone(e.message ?: "Failed to load GGUF model")
            }
        )
    }

    override fun resetConversation(
        model: Model,
        supportImage: Boolean,
        supportAudio: Boolean,
        systemInstruction: Contents?,
        tools: List<ToolProvider>,
        enableConversationConstrainedDecoding: Boolean,
    ) {
        if (tools.isNotEmpty()) {
            toolManagers[model.name] = ToolManager(tools)
            Log.d(TAG, "Refreshed ToolManager for ${model.name} on reset (tools=${tools.size})")
        } else {
            toolManagers.remove(model.name)
            Log.d(TAG, "Cleared ToolManager for ${model.name} on reset")
        }

        val engine = engines[model.name] ?: return
        val modelPath = engine.lastModelPath ?: return
        val systemPrompt = extractSystemPrompt(systemInstruction).ifBlank { engine.lastSystemPrompt }
        Log.d(TAG, "llama.cpp applied system prompt (reset, length=${systemPrompt.length}):\n$systemPrompt")
        traceSystemPrompt(systemPrompt)

        Log.d(TAG, "Resetting conversation for ${model.name} (keeping model loaded)")

        engine.resetConversation(
            modelPath = modelPath,
            params = engine.lastLoadParams ?: SmolLM.InferenceParams(),
            systemPrompt = systemPrompt,
            onSuccess = {
                // Update model instance reference
                model.instance = engine
                Log.d(TAG, "Conversation reset complete for ${model.name}")
            },
            onError = { e ->
                Log.e(TAG, "Failed to reset conversation for ${model.name}", e)
            },
        )
    }

    private fun extractSystemPrompt(systemInstruction: Contents?): String {
        if (systemInstruction == null) return ""
        return systemInstruction.contents
            .mapNotNull { content: Content ->
                when (content) {
                    is Content.Text -> content.text
                    else -> null
                }
            }
            .joinToString("\n")
            .trim()
    }

    override fun cleanUp(model: Model, onDone: () -> Unit) {
        val engine = engines.remove(model.name)
        toolManagers.remove(model.name)
        engine?.unloadModel()
        model.instance = null
        onDone()
        Log.d(TAG, "Clean up done for ${model.name}")
    }

    override fun stopResponse(model: Model) {
        val engine = engines[model.name]
        engine?.stopGeneration()
    }

    override fun getContextTokensUsed(model: Model, totalChars: Int): Int {
        val engine = engines[model.name]
        val reported = engine?.lastContextLengthUsed ?: 0
        if (reported > 0) return reported
        return super.getContextTokensUsed(model, totalChars)
    }

    override fun runInference(
        model: Model,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        onError: (message: String) -> Unit,
        images: List<Bitmap>,
        audioClips: List<ByteArray>,
        coroutineScope: CoroutineScope?,
        extraContext: Map<String, String>?,
    ) {
        val engine = engines[model.name]
        if (engine == null) {
            onError("llama.cpp engine not initialized for ${model.name}")
            return
        }
        if (!engine.isModelLoaded.get()) {
            if (coroutineScope == null) {
                onError("Model is still loading for ${model.name}")
                return
            }
            coroutineScope.launch(Dispatchers.Default) {
                var waitedMs = 0L
                while (!engine.isModelLoaded.get() && waitedMs < 20000L) {
                    delay(100L)
                    waitedMs += 100L
                }
                if (!engine.isModelLoaded.get()) {
                    onError("Model is still loading for ${model.name}")
                    return@launch
                }
                runInference(
                    model = model,
                    input = input,
                    resultListener = resultListener,
                    cleanUpListener = cleanUpListener,
                    onError = onError,
                    images = images,
                    audioClips = audioClips,
                    coroutineScope = coroutineScope,
                    extraContext = extraContext,
                )
            }
            return
        }

        // Note: llama.cpp text-only — images/audio not supported in this path
        if (images.isNotEmpty()) {
            Log.w(TAG, "Image input not supported with llama.cpp engine, ignoring ${images.size} images")
        }

        val toolManager = toolManagers[model.name]
        if (toolManager == null) {
            engine.generateResponse(
                query = input,
                onToken = { partialResponse ->
                    resultListener(partialResponse, false, null)
                },
                onComplete = {
                    resultListener("", true, null)
                },
                onCancelled = {
                    resultListener("", true, null)
                },
                onError = { e ->
                    Log.e(TAG, "Inference error", e)
                    onError(e.message ?: "Inference error")
                }
            )
            return
        }

        runToolLoop(
            engine = engine,
            toolManager = toolManager,
            userInput = input,
            resultListener = resultListener,
            onError = onError,
            stepsLeft = 4,
        )
    }

    private fun runToolLoop(
        engine: LlamaCppEngine,
        toolManager: ToolManager,
        userInput: String,
        resultListener: ResultListener,
        onError: (message: String) -> Unit,
        stepsLeft: Int,
    ) {
        trace("MODEL_INPUT", userInput)
        val passText = StringBuilder()
        engine.generateResponse(
            query = userInput,
            onToken = { partialResponse -> passText.append(partialResponse) },
            onComplete = { result ->
                val fullText = if (result.response.isNotBlank()) result.response else passText.toString()
                trace("MODEL_OUTPUT", fullText)
                Log.d(TAG, "GGUF pass complete. stepsLeft=$stepsLeft textLength=${fullText.length}")
                val toolCall = parseToolCall(fullText)
                if (toolCall == null) {
                    Log.d(TAG, "No tool call parsed from GGUF output (first 400 chars): ${fullText.take(400)}")
                } else {
                    val safeArgs =
                        runCatching { toolCall.second.toString().take(500) }
                            .getOrElse { "<args-unavailable:${it::class.java.simpleName}>" }
                    Log.d(
                        TAG,
                        "Parsed tool call: name='${toolCall.first}', args=$safeArgs"
                    )
                }

                if (toolCall == null || stepsLeft <= 0) {
                    if (fullText.isNotBlank()) {
                        resultListener(fullText, false, null)
                    }
                    resultListener("", true, null)
                    return@generateResponse
                }

                val normalizedInput =
                    try {
                        normalizeToolInput(toolCall.first, toolCall.second)
                    } catch (t: Throwable) {
                        Log.e(TAG, "Failed to normalize tool args for '${toolCall.first}', using empty args", t)
                        JsonObject()
                    }
                val safeNormalizedArgs =
                    runCatching { normalizedInput.toString().take(500) }
                        .getOrElse { "<args-unavailable:${it::class.java.simpleName}>" }
                Log.d(TAG, "Executing tool '${toolCall.first}' with normalized args: $safeNormalizedArgs")
                trace("TOOL_INPUT", """name=${toolCall.first} args=$safeNormalizedArgs""")
                val toolResult: String = executeToolWithAliases(
                    toolManager = toolManager,
                    requestedToolName = toolCall.first,
                    input = normalizedInput,
                )
                Log.d(TAG, "Tool '${toolCall.first}' completed. resultLength=${toolResult.length}")
                trace("TOOL_OUTPUT", """name=${toolCall.first} result=$toolResult""")
                val safeToolResultForModel = sanitizeToolResultForModel(toolResult)

                val followup =
                    buildString {
                        appendLine("Tool result JSON:")
                        appendLine(safeToolResultForModel)
                    }.trimIndent()

                // Schedule next pass asynchronously to avoid deep callback recursion on some devices.
                CoroutineScope(Dispatchers.Default).launch {
                    runToolLoop(
                        engine = engine,
                        toolManager = toolManager,
                        userInput = followup,
                        resultListener = resultListener,
                        onError = onError,
                        stepsLeft = stepsLeft - 1,
                    )
                }
            },
            onCancelled = { resultListener("", true, null) },
            onError = { e ->
                Log.e(TAG, "Inference error", e)
                onError(e.message ?: "Inference error")
            },
        )
    }

    private fun parseToolCall(text: String): Pair<String, JsonObject>? {
        val candidates = extractJsonObjects(text)
        for (candidate in candidates) {
            try {
                val obj = JsonParser.parseString(candidate).asJsonObject

                val toolName = when {
                    obj.has("toolName") -> obj.get("toolName").asString
                    obj.has("name") -> obj.get("name").asString
                    obj.has("tool") -> obj.get("tool").asString
                    obj.has("function") -> obj.get("function").asString
                    obj.has("tool_call") && obj.get("tool_call").isJsonObject -> {
                        val tc = obj.getAsJsonObject("tool_call")
                        when {
                            tc.has("toolName") -> tc.get("toolName").asString
                            tc.has("name") -> tc.get("name").asString
                            tc.has("tool") -> tc.get("tool").asString
                            tc.has("function") && tc.get("function").isJsonObject -> {
                                val fn = tc.getAsJsonObject("function")
                                if (fn.has("name")) fn.get("name").asString else null
                            }
                            else -> null
                        }
                    }
                    obj.has("function") && obj.get("function").isJsonObject -> {
                        val fn = obj.getAsJsonObject("function")
                        if (fn.has("name")) fn.get("name").asString else null
                    }
                    else -> null
                } ?: continue

                val input = when {
                    obj.has("input") && obj.get("input").isJsonObject -> obj.getAsJsonObject("input")
                    obj.has("input") && obj.get("input").isJsonPrimitive && obj.get("input").asJsonPrimitive.isString -> {
                        parseJsonObjectString(obj.get("input").asString) ?: JsonObject()
                    }
                    obj.has("arguments") && obj.get("arguments").isJsonObject -> obj.getAsJsonObject("arguments")
                    obj.has("arguments") && obj.get("arguments").isJsonPrimitive && obj.get("arguments").asJsonPrimitive.isString -> {
                        parseJsonObjectString(obj.get("arguments").asString) ?: JsonObject()
                    }
                    obj.has("args") && obj.get("args").isJsonObject -> obj.getAsJsonObject("args")
                    obj.has("args") && obj.get("args").isJsonPrimitive && obj.get("args").asJsonPrimitive.isString -> {
                        parseJsonObjectString(obj.get("args").asString) ?: JsonObject()
                    }
                    obj.has("tool_call") && obj.get("tool_call").isJsonObject -> {
                        val tc = obj.getAsJsonObject("tool_call")
                        when {
                            tc.has("input") && tc.get("input").isJsonObject -> tc.getAsJsonObject("input")
                            tc.has("arguments") && tc.get("arguments").isJsonObject -> tc.getAsJsonObject("arguments")
                            tc.has("arguments") && tc.get("arguments").isJsonPrimitive && tc.get("arguments").asJsonPrimitive.isString -> {
                                parseJsonObjectString(tc.get("arguments").asString) ?: JsonObject()
                            }
                            tc.has("function") && tc.get("function").isJsonObject -> {
                                val fn = tc.getAsJsonObject("function")
                                when {
                                    fn.has("arguments") && fn.get("arguments").isJsonObject -> fn.getAsJsonObject("arguments")
                                    fn.has("arguments") && fn.get("arguments").isJsonPrimitive && fn.get("arguments").asJsonPrimitive.isString -> {
                                        parseJsonObjectString(fn.get("arguments").asString) ?: JsonObject()
                                    }
                                    else -> JsonObject()
                                }
                            }
                            else -> JsonObject()
                        }
                    }
                    obj.has("function") && obj.get("function").isJsonObject -> {
                        val fn = obj.getAsJsonObject("function")
                        when {
                            fn.has("arguments") && fn.get("arguments").isJsonObject -> fn.getAsJsonObject("arguments")
                            fn.has("arguments") && fn.get("arguments").isJsonPrimitive && fn.get("arguments").asJsonPrimitive.isString -> {
                                parseJsonObjectString(fn.get("arguments").asString) ?: JsonObject()
                            }
                            else -> JsonObject()
                        }
                    }
                    else -> JsonObject()
                }
                if (input.size() == 0) {
                    // Compatibility path: treat all non-name keys as input args.
                    for ((key, value) in obj.entrySet()) {
                        if (key == "toolName" || key == "name" || key == "tool" || key == "function") {
                            continue
                        }
                        input.add(key, value)
                    }
                }
                return toolName to input
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun extractJsonObjects(text: String): List<String> {
        val out = mutableListOf<String>()
        var depth = 0
        var start = -1
        for (i in text.indices) {
            val ch = text[i]
            if (ch == '{') {
                if (depth == 0) start = i
                depth++
            } else if (ch == '}') {
                if (depth > 0) {
                    depth--
                    if (depth == 0 && start >= 0) {
                        out.add(text.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return out
    }

    private fun parseJsonObjectString(value: String): JsonObject? {
        return try {
            val parsed = JsonParser.parseString(value)
            if (parsed.isJsonObject) parsed.asJsonObject else null
        } catch (_: Exception) {
            null
        }
    }

    private fun executeToolWithAliases(
        toolManager: ToolManager,
        requestedToolName: String,
        input: JsonObject,
    ): String {
        val candidates = when (requestedToolName.lowercase()) {
            "searchweb", "search_web" ->
                listOf("searchWeb", "search_web", "searchweb")
            "parsewebpage", "parse_web_page" ->
                listOf("parseWebPage", "parse_web_page", "parse_webpage", "parsewebpage")
            "loadskill", "load_skill" ->
                listOf("loadSkill", "load_skill", "loadskill")
            "runmcptool", "run_mcp_tool" ->
                listOf("runMcpTool", "run_mcp_tool", "runmcptool")
            else -> listOf(requestedToolName)
        }.distinct()

        var lastResult = """{"error":"tool execution failed"}"""
        var firstNonNotFoundFailure: String? = null
        for (name in candidates) {
            val candidateInput = normalizeInputForAlias(alias = name, base = input)
            val resultAny: Any = try {
                toolManager.execute(name, candidateInput)
            } catch (e: Exception) {
                Log.e(TAG, "Tool execution threw for '$name'", e)
                lastResult = """{"error":"${e.message ?: "tool execution failed"}"}"""
                if (firstNonNotFoundFailure == null) {
                    firstNonNotFoundFailure = lastResult
                }
                continue
            }

            val resultText = Gson().toJson(resultAny)
            lastResult = resultText
            Log.d(TAG, "Tool attempt '$name' returned: $resultText")

            val lowered = resultText.lowercase()
            val looksMissing =
                lowered.contains("tool not found") ||
                    lowered.contains("not found. try to run it as a skill") ||
                    lowered.contains("\"status\":\"failed\"")
            if (!looksMissing) {
                return resultText
            } else if (
                !lowered.contains("tool not found") &&
                !lowered.contains("not found. try to run it as a skill") &&
                firstNonNotFoundFailure == null
            ) {
                firstNonNotFoundFailure = resultText
            }
        }

        return firstNonNotFoundFailure ?: lastResult
    }

    private fun normalizeInputForAlias(alias: String, base: JsonObject): JsonObject {
        val out = JsonObject()
        for ((k, v) in base.entrySet()) out.add(k, v)

        val lower = alias.lowercase()
        if (lower == "search_web" || lower == "searchweb" || lower == "searchweb") {
            if (!out.has("query")) {
                when {
                    out.has("q") -> out.add("query", out.get("q"))
                    out.has("search_query") -> out.add("query", out.get("search_query"))
                }
            }
            val maxResults = when {
                out.has("max_results") -> jsonPrimitiveAsString(out, "max_results", "5")
                out.has("maxResults") -> jsonPrimitiveAsString(out, "maxResults", "5")
                out.has("limit") -> jsonPrimitiveAsString(out, "limit", "5")
                else -> "5"
            }
            out.addProperty("max_results", maxResults)
            out.remove("maxResults")
            out.remove("limit")
        } else if (lower == "parse_web_page" || lower == "parsewebpage") {
            if (!out.has("url")) {
                when {
                    out.has("link") -> out.add("url", out.get("link"))
                    out.has("href") -> out.add("url", out.get("href"))
                }
            }
            val maxChars = when {
                out.has("max_chars") -> jsonPrimitiveAsString(out, "max_chars", "8000")
                out.has("maxChars") -> jsonPrimitiveAsString(out, "maxChars", "8000")
                out.has("limit") -> jsonPrimitiveAsString(out, "limit", "8000")
                else -> "8000"
            }
            out.addProperty("max_chars", maxChars)
            out.remove("maxChars")
            out.remove("limit")
        }

        return out
    }

    private fun normalizeToolInput(toolName: String, raw: JsonObject): JsonObject {
        val normalized = JsonObject()
        for ((k, v) in raw.entrySet()) {
            normalized.add(k, v)
        }

        if (toolName.equals("searchWeb", ignoreCase = true)) {
            if (!normalized.has("query")) {
                when {
                    normalized.has("q") -> normalized.add("query", normalized.get("q"))
                    normalized.has("search_query") -> normalized.add("query", normalized.get("search_query"))
                    normalized.has("prompt") -> normalized.add("query", normalized.get("prompt"))
                }
            }
            val maxResults = when {
                normalized.has("maxResults") -> jsonPrimitiveAsString(normalized, "maxResults", "5")
                normalized.has("max_results") -> jsonPrimitiveAsString(normalized, "max_results", "5")
                normalized.has("limit") -> jsonPrimitiveAsString(normalized, "limit", "5")
                normalized.has("top_k") -> jsonPrimitiveAsString(normalized, "top_k", "5")
                else -> "5"
            }
            normalized.addProperty("maxResults", maxResults)
            normalized.remove("max_results")
            normalized.remove("limit")
            normalized.remove("top_k")
        } else if (toolName.equals("parseWebPage", ignoreCase = true)) {
            if (!normalized.has("url")) {
                when {
                    normalized.has("link") -> normalized.add("url", normalized.get("link"))
                    normalized.has("href") -> normalized.add("url", normalized.get("href"))
                }
            }
            val maxChars = when {
                normalized.has("maxChars") -> jsonPrimitiveAsString(normalized, "maxChars", "8000")
                normalized.has("max_chars") -> jsonPrimitiveAsString(normalized, "max_chars", "8000")
                normalized.has("limit") -> jsonPrimitiveAsString(normalized, "limit", "8000")
                else -> "8000"
            }
            normalized.addProperty("maxChars", maxChars)
            normalized.remove("max_chars")
            normalized.remove("limit")
        }
        return normalized
    }

    private fun jsonPrimitiveAsString(obj: JsonObject, key: String, fallback: String): String {
        return try {
            if (!obj.has(key)) return fallback
            val p = obj.get(key)
            if (!p.isJsonPrimitive) return fallback
            val prim = p.asJsonPrimitive
            when {
                prim.isString -> prim.asString
                prim.isNumber -> prim.asNumber.toString()
                prim.isBoolean -> prim.asBoolean.toString()
                else -> fallback
            }
        } catch (_: Throwable) {
            fallback
        }
    }

    private fun sanitizeToolResultForModel(raw: String): String {
        val lowered = raw.lowercase()
        val isFailure =
            lowered.contains("error occured") ||
                lowered.contains("error occurred") ||
                lowered.contains("\"status\":\"failed\"") ||
                lowered.contains("illegalargumentexception") ||
                lowered.contains("tool not found")
        if (!isFailure) return raw
        return """{"status":"failed","error":"Tool call failed. Try another source or adjust parameters."}"""
    }
}
