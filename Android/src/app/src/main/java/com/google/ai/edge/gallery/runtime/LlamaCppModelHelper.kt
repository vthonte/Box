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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jegly.offlineLLM.smollm.SmolLM
import kotlinx.coroutines.CoroutineScope

private const val TAG = "BoxLlamaCppModelHelper"

/**
 * Box: LlmModelHelper implementation backed by llama.cpp for GGUF models.
 * Routes through the smollm JNI bridge.
 */
object LlamaCppModelHelper : LlmModelHelper {

    // Indexed by model name
    private val engines: MutableMap<String, LlamaCppEngine> = mutableMapOf()
    private val toolManagers: MutableMap<String, ToolManager> = mutableMapOf()

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
        val toolCallContract =
            if (tools.isNotEmpty()) {
                """
                TOOL CALL FORMAT (MANDATORY):
                When you need a tool, output ONLY a single JSON object:
                {"toolName":"<exact_tool_name>","input":{...}}
                Do not wrap JSON in markdown fences.
                Do not include any extra text before or after this JSON.
                """.trimIndent()
            } else {
                ""
            }
        val finalSystemPrompt =
            listOf(systemPrompt, toolCallContract).filter { it.isNotBlank() }.joinToString("\n\n")

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
        val engine = engines[model.name] ?: return
        val modelPath = engine.lastModelPath ?: return
        val systemPrompt = extractSystemPrompt(systemInstruction).ifBlank { engine.lastSystemPrompt }

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
        if (engine == null || !engine.isModelLoaded.get()) {
            onError("llama.cpp engine not initialized for ${model.name}")
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
        val passText = StringBuilder()
        engine.generateResponse(
            query = userInput,
            onToken = { partialResponse -> passText.append(partialResponse) },
            onComplete = { result ->
                val fullText = if (result.response.isNotBlank()) result.response else passText.toString()
                val toolCall = parseToolCall(fullText)
                if (toolCall == null || stepsLeft <= 0) {
                    if (fullText.isNotBlank()) {
                        resultListener(fullText, false, null)
                    }
                    resultListener("", true, null)
                    return@generateResponse
                }

                // Explicit UI signal in GGUF flow so users can see tool activity immediately.
                resultListener("Running tool: ${toolCall.first}\n", false, null)

                val toolResult: String = try {
                    toolManager.execute(toolCall.first, toolCall.second).toString()
                } catch (e: Exception) {
                    Log.e(TAG, "Tool execution failed for '${toolCall.first}'", e)
                    """{"error":"${e.message ?: "tool execution failed"}"}"""
                }

                val followup =
                    """
                    Tool call executed.
                    Tool name: ${toolCall.first}
                    Tool result JSON:
                    $toolResult

                    If more tools are needed, output exactly one tool-call JSON object.
                    Otherwise provide the final user-facing answer.
                    """.trimIndent()

                runToolLoop(
                    engine = engine,
                    toolManager = toolManager,
                    userInput = followup,
                    resultListener = resultListener,
                    onError = onError,
                    stepsLeft = stepsLeft - 1,
                )
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
                    else -> null
                } ?: continue

                val input = when {
                    obj.has("input") && obj.get("input").isJsonObject -> obj.getAsJsonObject("input")
                    obj.has("arguments") && obj.get("arguments").isJsonObject -> obj.getAsJsonObject("arguments")
                    obj.has("args") && obj.get("args").isJsonObject -> obj.getAsJsonObject("args")
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
}
