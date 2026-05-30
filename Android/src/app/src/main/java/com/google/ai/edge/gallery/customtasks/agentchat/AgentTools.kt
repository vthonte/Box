/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ai.edge.gallery.customtasks.agentchat

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.ai.edge.gallery.GalleryEvent
import com.google.ai.edge.gallery.common.AgentAction
import com.google.ai.edge.gallery.common.AskInfoAgentAction
import com.google.ai.edge.gallery.common.AskMcpToolCallPermissionAction
import com.google.ai.edge.gallery.common.CallJsAgentAction
import com.google.ai.edge.gallery.common.CallJsSkillResult
import com.google.ai.edge.gallery.common.CallJsSkillResultImage
import com.google.ai.edge.gallery.common.CallJsSkillResultWebview
import com.google.ai.edge.gallery.common.LOCAL_URL_BASE
import com.google.ai.edge.gallery.common.PermissionResult
import com.google.ai.edge.gallery.common.SkillProgressAgentAction
import com.google.ai.edge.gallery.firebaseAnalytics
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private const val TAG = "AGAgentTools"
private const val WEB_SETTINGS_PREFS = "box_settings"
private const val WEB_PAGE_CHAR_LIMIT_KEY = "web_page_char_limit"
private const val DEFAULT_WEB_PAGE_CHAR_LIMIT = 8000

open class AgentTools() : ToolSet {
  lateinit var context: Context
  lateinit var skillManagerViewModel: SkillManagerViewModel
  lateinit var mcpManagerViewModel: McpManagerViewModel
  lateinit var taskId: String

  private val _actionChannel = Channel<AgentAction>(Channel.UNLIMITED)
  val actionChannel: ReceiveChannel<AgentAction> = _actionChannel
  var resultImageToShow: CallJsSkillResultImage? = null
  var resultWebviewToShow: CallJsSkillResultWebview? = null

  /** Loads skill. */
  @Tool(description = "Loads a skill.")
  fun loadSkill(
    @ToolParam(description = "The name of the skill to load.") skillName: String
  ): Map<String, String> {
    return runBlocking(Dispatchers.Default) {
      val skills = skillManagerViewModel.getSelectedSkills()
      val skill = skills.find { it.name == skillName.trim() }
      val skillContent =
        if (skill != null) {
          "---\nname: ${skill.name}\ndescription: ${skill.description}\n---\n\n${skill.instructions}"
        } else {
          "Skill not found"
        }
      Log.d(TAG, "load skill. Skill content:\n$skillContent")
      if (skill != null) {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Loading skill \"$skillName\"",
            inProgress = true,
            addItemTitle = "Load \"${skill.name}\"",
            addItemDescription = "Description: ${skill.description}",
            customData = skill,
          )
        )
      } else {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Failed to load skill \"$skillName\"",
            inProgress = false,
          )
        )
      }

      mapOf("skill_name" to skillName, "skill_instructions" to skillContent)
    }
  }

  @Tool(description = "Run a MCP tool")
  fun runMcpTool(
    @ToolParam(description = "The name of the tool to run.") toolName: String,
    @ToolParam(description = "The parameters passed to tool as input") input: String,
  ): Map<String, String> {
    Log.d(TAG, "Run MCP tool:\n- name: $toolName\n- input: $input")

    return runBlocking(Dispatchers.IO) {
      val serverState =
        mcpManagerViewModel.uiState.value.mcpServers.find { serverState ->
          serverState.mcpServer.toolsList.any { it.name == toolName }
        }

      if (serverState == null) {
        Log.w(TAG, "MCP server or tool not found for: $toolName")
        logMcpExecution(success = false, errorType = "tool_not_found")
        return@runBlocking guardMissingEntityWithSkillFallback(name = toolName, type = "Tool")
      }

      val client = serverState.client
      if (client == null) {
        logMcpExecution(success = false, errorType = "client_not_initialized")
        return@runBlocking mapOf("error" to "Client not initialized", "status" to "failed")
      }

      // Check if the MCP tool requires user permission. If not always allowed,
      // send an action to ask for permission and wait for the result.
      val mcpTool = serverState.mcpServer.toolsList.find { it.name == toolName }
      val isAlwaysAllow = mcpTool?.alwaysAllow ?: false

      if (!isAlwaysAllow) {
        val permissionAction = AskMcpToolCallPermissionAction(toolName = toolName, argument = input)
        _actionChannel.send(permissionAction)
        val permissionResult = permissionAction.result.await()
        if (permissionResult == PermissionResult.DENY) {
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "Permission denied for MCP tool \"$toolName\"",
              inProgress = false,
            )
          )
          logMcpExecution(success = false, errorType = "permission_denied")
          return@runBlocking mapOf("error" to "Permission denied by user", "status" to "failed")
        }
      }

      try {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Calling MCP tool \"$toolName\"",
            inProgress = true,
            addItemTitle = "Call MCP tool: \"$toolName\"",
            addItemDescription = "- Input: $input",
          )
        )
        val result =
        client.callTool(
          request = CallToolRequest(
            CallToolRequestParams(
              name = toolName,
              arguments = kotlinx.serialization.json.Json.parseToJsonElement(input).jsonObject
            )
          )
        )

        if (result == null) {
          Log.d(TAG, "Tool execution returned null result")
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "Failed to call MCP tool \"$toolName\"",
              inProgress = false,
            )
          )
          logMcpExecution(success = false, errorType = "null_result")
          return@runBlocking mapOf("error" to "Null result", "status" to "failed")
        }

        if (result.isError == true) {
          val errorText =
            result.content.filterIsInstance<TextContent>().joinToString("\n") { it.text ?: "" }
          Log.e(TAG, "MCP tool \"$toolName\" failed: $errorText")
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "Failed to call MCP tool \"$toolName\"",
              addItemTitle = "Call MCP tool \"$toolName\" failed",
              addItemDescription = errorText,
              inProgress = false,
            )
          )
          logMcpExecution(success = false, errorType = "tool_error")
          return@runBlocking mapOf("error" to errorText, "status" to "failed")
        } else {
          val successText =
            result.content.filterIsInstance<TextContent>().joinToString("\n") { it.text ?: "" }
          Log.d(TAG, "MCP tool \"$toolName\" succeeded:\n$successText")
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "Succeeded calling MCP tool \"$toolName\"",
              inProgress = true,
              addItemTitle = "Call MCP tool \"$toolName\" succeeded",
              addItemDescription = successText,
            )
          )
          logMcpExecution(success = true, errorType = "")
          return@runBlocking mapOf("result" to successText, "status" to "succeeded")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error calling MCP tool", e)
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Error calling MCP tool \"$toolName\"",
            inProgress = false,
            addItemTitle = "Call MCP tool \"$toolName\" failed",
            addItemDescription = e.message ?: "Unknown error",
          )
        )
        logMcpExecution(success = false, errorType = "exception")
        return@runBlocking mapOf("error" to (e.message ?: "Unknown error"), "status" to "failed")
      }
    }
  }

  /** Call JS skill */
  @Tool(description = "Runs JS script")
  fun runJs(
    @ToolParam(description = "The name of skill") skillName: String,
    @ToolParam(description = "The script name to run. Use 'index.html' if not provided by user")
    scriptName: String,
    @ToolParam(
      description = "The data to pass to the script. Use empty string if not provided by user"
    )
    data: String,
  ): Map<String, Any> {
    return runBlocking(Dispatchers.Default) {
      Log.d(
        TAG,
        "runJS tool called with:" +
          "\n- skillName: ${skillName}\n- scriptName: ${scriptName}\n- data: ${data}\n",
      )

      val skills = skillManagerViewModel.getSelectedSkills()
      val skill = skills.find { it.name == skillName.trim() }

      if (skill == null) {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Failed to call skill \"$scriptName\"",
            inProgress = false,
          )
        )
        return@runBlocking mapOf(
          "error" to "Skill \"${scriptName}\" not found",
          "status" to "failed",
        )
      }

      // Check secret. If a skill requires a secret and the secret is not provided, show error.
      var secret = ""
      if (skill.requireSecret) {
        val savedSecret =
          skillManagerViewModel.dataStoreRepository.readSecret(
            key = getSkillSecretKey(skillName = skillName)
          )
        if (savedSecret == null || savedSecret.isEmpty()) {
          val action =
            AskInfoAgentAction(
              dialogTitle = "Enter secret",
              fieldLabel =
                skill.requireSecretDescription.ifEmpty {
                  "The JS script needs a secret (API key / token) to proceed:"
                },
            )
          _actionChannel.send(action)
          secret = action.result.await()
          if (secret.isNotEmpty()) {
            skillManagerViewModel.dataStoreRepository.saveSecret(
              key = getSkillSecretKey(skillName = skillName),
              value = secret,
            )
            Log.d(TAG, "Got Secret from ask info dialog: ${secret.substring(0, 3)}")
          } else {
            Log.d(TAG, "The ask info dialog got cancelled. No secret.")
          }
        } else {
          secret = savedSecret
        }
      }

      // Get the url for the skill.
      val url =
        skillManagerViewModel.getJsSkillUrl(skillName = skillName, scriptName = scriptName)
          ?: return@runBlocking mapOf(
            "result" to "JS Skill URL not set properly or skill not found"
          )
      Log.d(TAG, "Calling JS script.\n- url: $url\n- data: $data")

      // Update progress.
      _actionChannel.send(
        SkillProgressAgentAction(
          label = "Calling JS script \"${skillName}/${scriptName}\"",
          inProgress = true,
          addItemTitle = "Call JS script: \"${skillName}/${scriptName}\"",
          addItemDescription = "- URL: ${url.replace(LOCAL_URL_BASE, "")}\n- Data: $data",
          customData = skill,
        )
      )

      // Actually run it and wait for the result.
      val action =
        CallJsAgentAction(url = url, data = data.trim().ifEmpty { "{}" }, secret = secret)
      _actionChannel.send(action)
      val result = action.result.await()

      // Try to parse result to CallJsSkillResult.
      val moshi: Moshi = Moshi.Builder().build()
      val jsonAdapter: JsonAdapter<CallJsSkillResult> =
        moshi.adapter(CallJsSkillResult::class.java).failOnUnknown()
      val resultJson = runCatching { jsonAdapter.fromJson(result) }.getOrNull()
      val error = resultJson?.error

      // Failed to parse. Treat its whole as a result string.
      if (
        resultJson == null ||
          (resultJson.result == null && resultJson.webview == null && resultJson.image == null)
      ) {
        mapOf("result" to result, "status" to "succeeded")
      }
      // Error case.
      else if (error != null) {
        mapOf("error" to error, "status" to "failed")
      }
      // Non-error cases.
      else {
        // Handle image and webview in result.
        val image = resultJson.image
        val webview = resultJson.webview
        if (image != null) {
          Log.d(TAG, "Got an image response.")
          resultImageToShow = image
        }
        if (webview != null) {
          Log.d(TAG, "Got an webview response.")
          val webviewUrl =
            skillManagerViewModel.getJsSkillWebviewUrl(
              skillName = skillName,
              url = webview.url ?: "",
            )
          Log.d(TAG, "Webview url: $webviewUrl")
          resultWebviewToShow = webview.copy(url = webviewUrl)
        }
        Log.d(TAG, "Result: ${resultJson.result}")
        mapOf("result" to (resultJson.result ?: ""), "status" to "succeeded")
      }
    }
  }

  @Tool(
    description =
      "Run an Android intent. It is used to interact with the app to perform certain actions."
  )
  fun runIntent(
    @ToolParam(description = "The intent to run.") intent: String,
    @ToolParam(
      description = "A JSON string containing the parameter values required for the intent."
    )
    parameters: String,
  ): Map<String, String> {
    return runBlocking(Dispatchers.Default) {
      Log.d(TAG, "Run intent. Intent: '$intent', parameters: '$parameters'")
      _actionChannel.send(
        SkillProgressAgentAction(
          label = "Executing intent \"$intent\"",
          inProgress = true,
          addItemTitle = "Execute intent \"$intent\"",
          addItemDescription = "Parameters: $parameters",
        )
      )
      val res = IntentHandler.handleAction(context, intent, parameters)
      return@runBlocking mapOf(
        "action" to intent,
        "parameters" to parameters,
        "result" to res.toString(),
      )
    }
  }

  @Tool(
    description =
      "Searches the web using DuckDuckGo and returns a numbered list of results. " +
        "Use this first, then rank and select the most relevant links to parse."
  )
  fun searchWeb(
    @ToolParam(description = "The search query.") query: String,
    @ToolParam(description = "Maximum number of results to return. Default is 5.") maxResults: String,
  ): Map<String, String> {
    return runBlocking(Dispatchers.IO) {
      try {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Searching web",
            inProgress = true,
            addItemTitle = "Web search",
            addItemDescription = "Query: $query",
          )
        )
        val limit = maxResults.toIntOrNull()?.coerceIn(1, 10) ?: 5
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val fetchers =
          listOf<() -> String>(
            { httpGet("https://html.duckduckgo.com/html/?q=$encodedQuery") },
            { httpGet("https://duckduckgo.com/html/?q=$encodedQuery") },
            { httpGet("https://lite.duckduckgo.com/lite/?q=$encodedQuery") },
            { httpPostForm("https://lite.duckduckgo.com/lite/", mapOf("q" to query)) },
          )
        val results =
          fetchers
            .asSequence()
            .mapNotNull { fetch ->
              try {
                parseDuckDuckGoHtmlResults(fetch())
              } catch (_: Exception) {
                null
              }
            }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
            .take(limit)
        if (results.isEmpty()) {
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "No web results found",
              inProgress = false,
              addItemTitle = "Web search completed",
              addItemDescription = "No results found for query: $query",
            )
          )
          return@runBlocking mapOf("status" to "succeeded", "results_json" to "[]")
        }

        val resultsJsonArray =
          JSONArray().apply {
            results.forEach { item ->
              put(
                JSONObject()
                  .put("title", item.title)
                  .put("text", item.text)
                  .put("url", item.url)
              )
            }
          }
        val resultsJsonString = resultsJsonArray.toString().replace("\\/", "/")

        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Web search completed",
            inProgress = false,
            addItemTitle = "Found results",
            addItemDescription = "Returned ${results.size} result(s)",
            customData = resultsJsonString,
          )
        )
        mapOf("status" to "succeeded", "results_json" to resultsJsonString)
      } catch (e: Exception) {
        Log.e(TAG, "Error searching web", e)
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Web search failed",
            inProgress = false,
            addItemTitle = "Web search error",
            addItemDescription = e.message ?: "Unknown error",
          )
        )
        mapOf("status" to "failed", "error" to (e.message ?: "Search failed"))
      }
    }
  }

  @Tool(
    description =
      "Fetches and extracts cleaned main content from a web page URL and returns Markdown. " +
        "Use after selecting the most relevant links from search results."
  )
  fun parseWebPage(
    @ToolParam(description = "The full URL to parse.") url: String,
    @ToolParam(description = "Maximum number of characters to return. Default is 8000.")
    maxChars: String,
  ): Map<String, String> {
    return runBlocking(Dispatchers.IO) {
      try {
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Parsing web page",
            inProgress = true,
            addItemTitle = "Fetch page",
            addItemDescription = url,
          )
        )
        val requestedLimit = maxChars.toIntOrNull()?.coerceIn(1000, 20000) ?: DEFAULT_WEB_PAGE_CHAR_LIMIT
        val settingsLimit =
          context
            .getSharedPreferences(WEB_SETTINGS_PREFS, Context.MODE_PRIVATE)
            .getInt(WEB_PAGE_CHAR_LIMIT_KEY, DEFAULT_WEB_PAGE_CHAR_LIMIT)
            .coerceIn(1000, 20000)
        val limit = minOf(requestedLimit, settingsLimit)
        val html = httpGet(url)
        val doc = Jsoup.parse(html, url)
        val title = doc.title().trim()
        val markdown = extractMainContentMarkdown(doc).take(limit)
        if (markdown.isBlank()) {
          _actionChannel.send(
            SkillProgressAgentAction(
              label = "Page parse failed",
              inProgress = false,
              addItemTitle = "Parse error",
              addItemDescription = "Could not extract readable content.",
            )
          )
          return@runBlocking mapOf(
            "status" to "failed",
            "error" to "Could not extract readable content from page.",
          )
        }
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Page parsed",
            inProgress = false,
            addItemTitle = "Parsed page",
            addItemDescription = url,
            customData = markdown,
          )
        )
        mapOf(
          "status" to "succeeded",
          "title" to (title ?: ""),
          "url" to url,
          "markdown" to markdown,
        )
      } catch (e: Exception) {
        Log.e(TAG, "Error parsing web page", e)
        _actionChannel.send(
          SkillProgressAgentAction(
            label = "Page parse failed",
            inProgress = false,
            addItemTitle = "Parse error",
            addItemDescription = e.message ?: "Unknown error",
          )
        )
        mapOf("status" to "failed", "error" to (e.message ?: "Page parse failed"))
      }
    }
  }

  /**
   * Guards against missing entities (tools or intents) by checking if they exist as skills. Returns
   * a failure response with a specific hint to the model to try running it as a skill if it is
   * found in the allowed skills list. This helps guide the model when it gets confused and tries to
   * call a skill as a tool or intent.
   */
  private fun guardMissingEntityWithSkillFallback(name: String, type: String): Map<String, String> {
    val skills = skillManagerViewModel.getSelectedSkills()
    val isSkill = skills.any { it.name == name.trim() }
    val error = if (isSkill) "$type not found. Try to run it as a skill" else "Tool not found"
    return mapOf("error" to error, "status" to "failed")
  }

  fun sendAgentAction(action: AgentAction) {
    runBlocking(Dispatchers.Default) { _actionChannel.send(action) }
  }

  private fun logMcpExecution(success: Boolean, errorType: String) {
    Log.d(
      TAG,
      "Analytics: mcp_execution, capability_name=$taskId, success=$success, error_type=$errorType",
    )
    firebaseAnalytics?.logEvent(
      GalleryEvent.MCP_EXECUTION.id,
      Bundle().apply {
        putString("capability_name", taskId)
        putBoolean("success", success)
        if (errorType.isNotEmpty()) {
          putString("error_type", errorType)
        }
      },
    )
  }

  private fun httpGet(url: String): String {
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = 15000
      readTimeout = 20000
      setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari",
      )
      setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    }
    return try {
      val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
      stream.bufferedReader().use { it.readText() }
    } finally {
      conn.disconnect()
    }
  }

  private fun httpPostForm(url: String, form: Map<String, String>): String {
    val body =
      form.entries.joinToString("&") { (k, v) ->
        "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
      }
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
      requestMethod = "POST"
      connectTimeout = 15000
      readTimeout = 20000
      doOutput = true
      setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari",
      )
      setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
      setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      setRequestProperty("Content-Length", bytes.size.toString())
    }
    return try {
      conn.outputStream.use { it.write(bytes) }
      val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
      stream.bufferedReader().use { it.readText() }
    } finally {
      conn.disconnect()
    }
  }

  private data class WebSearchResult(val title: String, val text: String, val url: String)

  private fun parseDuckDuckGoHtmlResults(html: String): List<WebSearchResult> {
    val doc = Jsoup.parse(html)
    val out = mutableListOf<WebSearchResult>()
    val seenUrls = mutableSetOf<String>()

    // Pass 1: standard DuckDuckGo HTML layout.
    val containers = doc.select(".result, .result--web, article, .web-result")
    for (container in containers) {
      val a =
        container.selectFirst("a.result__a, a[data-testid=result-title-a], h2 a[href], h3 a[href]")
          ?: continue
      val title = a.text().trim().ifEmpty { a.attr("title").trim() }
      if (title.isBlank()) continue
      val resolved = resolveDuckDuckGoRedirect(a.attr("href").trim())
      if (!isExternalHttpUrl(resolved)) continue
      if (!seenUrls.add(resolved)) continue

      val snippet = extractDuckDuckGoSnippet(container, a)
      out.add(WebSearchResult(title = title, text = snippet, url = resolved))
    }

    // Pass 2: lite layout (table rows with result title row + snippet row).
    val liteAnchors = doc.select("a.result-link, td.result-link a[href]")
    for (a in liteAnchors) {
      val title = a.text().trim().ifEmpty { a.attr("title").trim() }
      if (title.isBlank()) continue
      val resolved = resolveDuckDuckGoRedirect(a.attr("href").trim())
      if (!isExternalHttpUrl(resolved)) continue
      if (!seenUrls.add(resolved)) continue

      val row = a.parents().firstOrNull { it.tagName().equals("tr", ignoreCase = true) }
      val snippetFromRow =
        row?.nextElementSibling()
          ?.selectFirst("td.result-snippet, .result-snippet, .snippet, td")
          ?.text()
          .orEmpty()
      val snippet =
        if (snippetFromRow.isNotBlank()) {
          cleanSnippet(snippetFromRow)
        } else {
          extractDuckDuckGoSnippet(a.parent() ?: a, a)
        }

      out.add(WebSearchResult(title = title, text = snippet, url = resolved))
    }

    return out.map { if (it.text.isBlank()) it.copy(text = "") else it }
  }

  private fun extractDuckDuckGoSnippet(container: Element, titleAnchor: Element): String {
    val titleText = titleAnchor.text().trim()
    val resolvedUrl = resolveDuckDuckGoRedirect(titleAnchor.attr("href").trim())

    // Lite layout: snippet is typically in the next row's td.result-snippet.
    val titleRow = titleAnchor.parents().firstOrNull { it.tagName().equals("tr", ignoreCase = true) }
    val liteSnippet =
      titleRow
        ?.nextElementSibling()
        ?.selectFirst("td.result-snippet")
        ?.text()
        .orEmpty()
    if (liteSnippet.isNotBlank()) {
      return cleanSnippet(stripTitleAndUrlNoise(liteSnippet, titleText, resolvedUrl))
    }

    val direct =
      container
        .selectFirst(
          ".result__snippet, .result-snippet, [data-result='snippet'], .snippet, .result__extras, .result__body"
        )
        ?.text()
        .orEmpty()
    if (direct.isNotBlank()) {
      return cleanSnippet(stripTitleAndUrlNoise(direct, titleText, resolvedUrl))
    }

    // Fallback: inspect nearby blocks and remove title/url noise.
    val candidateScopes =
      listOfNotNull(
        titleAnchor.parent(),
        titleAnchor.parents().firstOrNull { it.className().contains("result", ignoreCase = true) },
        container,
      )

    for (scope in candidateScopes) {
      val textCandidates =
        scope
          .select("span, p, div")
          .map { it.text().trim() }
          .filter { candidate ->
            candidate.isNotBlank() &&
              candidate.length > 20 &&
              !candidate.equals(titleAnchor.text().trim(), ignoreCase = true) &&
              !candidate.startsWith("http", ignoreCase = true)
          }
      if (textCandidates.isNotEmpty()) {
        return cleanSnippet(stripTitleAndUrlNoise(textCandidates.first(), titleText, resolvedUrl))
      }
    }

    return ""
  }

  private fun stripTitleAndUrlNoise(snippet: String, title: String, url: String): String {
    var cleaned = snippet
    if (title.isNotBlank()) {
      cleaned = cleaned.replace(title, "", ignoreCase = true)
    }
    if (url.isNotBlank()) {
      cleaned = cleaned.replace(url, "", ignoreCase = true)
    }
    val host =
      try {
        URL(url).host.removePrefix("www.")
      } catch (_: Exception) {
        ""
      }
    if (host.isNotBlank()) {
      cleaned = cleaned.replace(host, "", ignoreCase = true)
    }
    return cleaned
      .replace(Regex("https?://\\S+"), "")
      .replace(Regex("www\\.\\S+"), "")
      .trim()
  }

  private fun cleanSnippet(text: String): String {
    return text
      .replace(Regex("\\s+"), " ")
      .replace(Regex("^\\s*[-|:•]+\\s*"), "")
      .trim()
      .take(280)
  }

  private fun isExternalHttpUrl(url: String): Boolean {
    if (!(url.startsWith("http://") || url.startsWith("https://"))) return false
    if (url.contains("duckduckgo.com", ignoreCase = true)) return false
    return true
  }

  private fun resolveDuckDuckGoRedirect(url: String): String {
    // Examples:
    // /l/?uddg=https%3A%2F%2Fexample.com
    // //duckduckgo.com/l/?uddg=...
    // https://duckduckgo.com/l/?uddg=...
    val normalized =
      when {
        url.startsWith("//") -> "https:$url"
        else -> url
      }

    val isRedirectLink =
      normalized.startsWith("/l/?") ||
        normalized.contains("duckduckgo.com/l/?", ignoreCase = true)
    if (!isRedirectLink) return normalized

    return try {
      val uddg = Regex("""[?&]uddg=([^&]+)""").find(normalized)?.groupValues?.get(1)
      if (uddg.isNullOrBlank()) return normalized
      java.net.URLDecoder.decode(uddg, "UTF-8")
    } catch (_: Exception) {
      normalized
    }
  }

  private fun extractMainContentMarkdown(doc: Document): String {
    doc.select("script, style, noscript, iframe, nav, footer, header, aside, form, button").remove()

    val root =
      doc.selectFirst("article, main, [role=main], .post-content, .article-content, .entry-content")
        ?: doc.body()
    val blocks = root.select("h1,h2,h3,h4,h5,h6,p,ul,ol,pre,blockquote")
    val out = StringBuilder()

    for (el in blocks) {
      when (el.tagName().lowercase()) {
        "h1" -> appendBlock(out, "# ${inlineToMarkdown(el)}")
        "h2" -> appendBlock(out, "## ${inlineToMarkdown(el)}")
        "h3" -> appendBlock(out, "### ${inlineToMarkdown(el)}")
        "h4" -> appendBlock(out, "#### ${inlineToMarkdown(el)}")
        "h5" -> appendBlock(out, "##### ${inlineToMarkdown(el)}")
        "h6" -> appendBlock(out, "###### ${inlineToMarkdown(el)}")
        "p" -> {
          val text = inlineToMarkdown(el).trim()
          if (text.isNotBlank()) {
            appendBlock(out, text)
          }
        }
        "ul" -> appendBlock(out, renderList(el, ordered = false))
        "ol" -> appendBlock(out, renderList(el, ordered = true))
        "pre" -> appendBlock(out, "```\n${el.text().trim()}\n```")
        "blockquote" -> {
          val quote = inlineToMarkdown(el).lines().joinToString("\n") { "> ${it.trim()}" }
          appendBlock(out, quote)
        }
      }
    }

    if (out.isBlank()) {
      // Fallback when content is deeply nested.
      val fallback = root.select("h1,h2,h3,p,li,pre,blockquote").take(200)
      for (el in fallback) {
        val line =
          when (el.tagName().lowercase()) {
            "h1" -> "# ${inlineToMarkdown(el)}"
            "h2" -> "## ${inlineToMarkdown(el)}"
            "h3" -> "### ${inlineToMarkdown(el)}"
            "li" -> "- ${inlineToMarkdown(el)}"
            "pre" -> "```\n${el.text().trim()}\n```"
            "blockquote" -> "> ${inlineToMarkdown(el)}"
            else -> inlineToMarkdown(el)
          }
        appendBlock(out, line)
      }
    }

    return out.toString().trim()
  }

  private fun renderList(el: Element, ordered: Boolean): String {
    val items = el.select("> li")
    return items.mapIndexed { idx, li ->
      val prefix = if (ordered) "${idx + 1}." else "-"
      "$prefix ${inlineToMarkdown(li).trim()}"
    }.joinToString("\n")
  }

  private fun inlineToMarkdown(el: Element): String {
    val clone = el.clone()
    clone.select("br").forEach { br ->
      br.after("\\n")
      br.remove()
    }
    clone.select("code").forEach { code -> code.text("`${code.text().trim()}`") }
    clone.select("strong, b").forEach { n -> n.text("**${n.text().trim()}**") }
    clone.select("em, i").forEach { n -> n.text("*${n.text().trim()}*") }
    clone.select("a[href]").forEach { a ->
      // Keep anchor text only; drop URLs to preserve model context window.
      val text = a.text().trim().ifEmpty { "" }
      a.text(text)
    }
    return clone
      .text()
      .replace("\\n", "\n")
      .replace(Regex("https?://\\S+"), "")
      .replace(Regex("www\\.\\S+"), "")
      .replace(Regex("[ \\t\\x0B\\f\\r]+"), " ")
      .trim()
  }

  private fun appendBlock(out: StringBuilder, block: String) {
    val trimmed = block.trim()
    if (trimmed.isNotEmpty()) {
      if (out.isNotEmpty()) out.append("\n\n")
      out.append(trimmed)
    }
  }

  private fun htmlDecode(input: String): String {
    return input
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .replace("&#39;", "'")
  }
}

fun getSkillSecretKey(skillName: String): String {
  return "skill___${skillName}"
}
