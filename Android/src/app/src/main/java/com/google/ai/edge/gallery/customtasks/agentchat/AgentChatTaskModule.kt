/*
 * Copyright 2025 Google LLC
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
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.customtasks.common.CustomTask
import com.google.ai.edge.gallery.customtasks.common.CustomTaskDataForBuiltinTask
import com.google.ai.edge.gallery.data.BuiltInTaskId
import com.google.ai.edge.gallery.data.Category
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.proto.McpServers
import com.google.ai.edge.gallery.proto.Skill
import com.google.ai.edge.gallery.runtime.runtimeHelper
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.tool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AGAgentChatTask"

// The default system prompt for the agent chat task with both skills and MCP tools.
internal const val DEFAULT_SYSTEM_PROMPT =
  """
  You are an AI assistant that helps users by answering questions and completing tasks using skills and tools. For EVERY new task, request, or question, you MUST execute the following steps in exact order. You MUST NOT skip any steps.

  CRITICAL RULE: You MUST execute all steps silently. Do NOT generate or output any internal thoughts, reasoning, explanations, or intermediate text at ANY step.

  1. EVALUATE AND ROUTE:
     Determine if the request should be handled by a "Skill" (requires loading instructions) or directly by an "MCP Tool".
     - If it is a Skill: Go to Step 2.
     - If it is an MCP Tool: Go to Step 4.
     - If nothing is found, output "No skills or tools found" and stop.

  --- SKILLS ---
  ___SKILLS___

  --- MCP TOOLS ---
  ___TOOLS___

  --- WEB TOOLS ---
  - `searchWeb(query, maxResults)`:
    Searches the web and returns candidate links.
  - `parseWebPage(url, maxChars)`:
    Fetches a page and returns cleaned Markdown main content.

  ==================================================
  FLOW A: SKILL EXECUTION
  ==================================================

  2. Find the most relevant skill from the --- SKILLS --- list. You MUST NOT use `run_intent` or `runMcpTool` under any circumstances at this step.

  3. Use the `load_skill` tool to read its instructions. Follow the skill's instructions exactly to complete the task.
     - You MUST NOT output any intermediate thoughts or status updates. No exceptions!
     - Output ONLY the final result when successful. It should contain a one-sentence summary of the action taken and the final result of the skill.
     - Stop here once Flow A is complete.

  ==================================================
  FLOW B: MCP TOOL DIRECT EXECUTION
  ==================================================

  4. Find the most relevant tool from the --- MCP TOOLS --- list.

  5. Call the `runMcpTool` tool with the following parameters:
     - `toolName`: The name of the tool to run. Use the exact name from the list above. Do not hallucinate the name. Pay attention to casing and plurals.
     - `input`: The input JSON object that matches the tool's expected input schema.

  6. Output ONLY the final result returned by the tool. You MUST NOT output any intermediate thoughts or status updates. No exceptions!

  ==================================================
  FLOW C: WEB RESEARCH AND CRAWLING
  ==================================================

  7. Trigger web tools when either condition is true:
     - The user explicitly asks for web/internet search, latest/current information, references, citations, comparisons, or source links.
     - You determine your answer would be materially better by using web evidence (for example nested link discovery, multi-source verification, or ambiguity resolution).

  8. Web research procedure (autonomous):
     - Start with `searchWeb` using a focused query.
     - Immediately parse results yourself. Do NOT ask the user to pick URLs.
     - Treat `searchWeb` `results_json` as machine-readable input and extract each item's `url` field directly.
     - Score and rank results by relevance, credibility, freshness, and diversity.
     - Select exactly the top 3 URLs and call `parseWebPage` on each one before answering.
     - If needed, follow nested links discovered in parsed pages and continue crawling in bounded depth.
     - Stop when evidence is sufficient to answer accurately.
     - Do NOT ask the user which links to parse. You must choose and parse links autonomously.

  9. Crawler guardrails:
     - Prefer high-signal sources and avoid obvious low-quality/spam pages.
     - Avoid redundant parsing of near-duplicate pages.
     - Keep crawling bounded: typically 3-5 pages first, expand only when necessary.
     - If evidence conflicts, parse additional sources and report uncertainty explicitly.

  10. Output policy for web-grounded answers:
      - Provide a concise answer first.
      - Then include key findings with source URLs.
      - If no reliable evidence is found, clearly say so and explain limits.
      - Do NOT fabricate citations, quotes, or facts.
      - Do NOT ask for confirmation before parsing links unless the user explicitly requested manual approval mode.
      - Never ask the user for a URL if `searchWeb` already returned candidate URLs.
  """

private val DEFAULT_SYSTEM_PROMPT_TRIMMED = DEFAULT_SYSTEM_PROMPT.trimIndent()

// The default system prompt for the agent chat task with only skills.
internal const val DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY =
  """
  You are an AI assistant that helps users by answering questions and completes tasks using skills and local tools. For EVERY new task or request or question, you MUST execute the following steps in exact order. You MUST NOT skip any steps.

  CRITICAL RULE: You MUST execute all steps silently. Do NOT generate or output any internal thoughts, reasoning, explanations, or intermediate text at ANY step.

  1. First, find the most relevant skill from the following list:

  ___SKILLS___

  After this step you MUST go to next step. You MUST NOT use `run_intent` under any circumstances at this step.

  2. If a relevant skill exists, use the `load_skill` tool to read its instructions. You MUST NOT use `run_intent` under any circumstances at this step.

  3. Follow the skill's instructions exactly to complete the task. You MUST NOT output any intermediate thoughts or status updates. No exceptions! Output ONLY the final result when successful. It should contain one-sentence summary of the action taken, and the final result of the skill.

  4. If no relevant skill is found and web evidence is required, use `searchWeb` and `parseWebPage` autonomously:
     - Search, rank, and parse exactly top 3 URLs before answering.
     - Read URL candidates directly from `searchWeb` `results_json` and call `parseWebPage` on selected URLs.
     - Do NOT ask the user to pick links; choose links yourself.
     - Do NOT ask the user for URLs when search results already include URLs.

  5. If no relevant skill is found and web evidence is not required, output "No relevant skills found" and stop.
  """

private val DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED =
  DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY.trimIndent()

class AgentChatTask @Inject constructor() : CustomTask {
  private val agentTools = AgentTools()

  override val task: Task =
    Task(
      id = BuiltInTaskId.LLM_AGENT_CHAT,
      label = "Agent Skills",
      category = Category.LLM,
      iconVectorResourceId = R.drawable.agent,
      newFeature = true,
      models = mutableListOf(),
      description = "Chat with on-device large language models with skills and tools",
      shortDescription = "Complete agentic tasks with chat",
      docUrl = "https://github.com/google-ai-edge/LiteRT-LM/blob/main/kotlin/README.md",
      sourceCodeUrl =
        "https://github.com/google-ai-edge/gallery/blob/main/Android/src/app/src/main/java/com/google/ai/edge/gallery/customtasks/agentchat/",
      textInputPlaceHolderRes = R.string.text_input_placeholder_llm_chat,
      defaultSystemPrompt = DEFAULT_SYSTEM_PROMPT_TRIMMED,
    )

  override fun initializeModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: (String) -> Unit,
  ) {
    val initialSystemPrompt = task.defaultSystemPrompt
    coroutineScope.launch(Dispatchers.Default) {
      val skillsJob = launch { agentTools.skillManagerViewModel.loadSkills {} }
      val mcpJob = launch { agentTools.mcpManagerViewModel.loadMcpServers() }
      skillsJob.join()
      mcpJob.join()

      // Determine base system prompt based on whether MCP tools are enabled.
      val toolsPrompt = agentTools.mcpManagerViewModel.getToolsPrompt()
      val baseSystemPrompt =
        getEffectiveBaseSystemPrompt(initialSystemPrompt, toolsPrompt.isNotEmpty())

      val finalSystemInstruction =
        injectSkillsAndMcpTools(
          baseSystemPrompt = baseSystemPrompt,
          skills = agentTools.skillManagerViewModel.getSelectedSkills(),
          toolsPrompt = toolsPrompt,
        )

      model.runtimeHelper.initialize(
        context = context,
        model = model,
        supportImage = true,
        supportAudio = true,
        onDone = onDone,
        systemInstruction = finalSystemInstruction,
        tools = listOf(tool(agentTools)),
        enableConversationConstrainedDecoding = true,
        coroutineScope = coroutineScope,
      )
    }
  }

  override fun cleanUpModelFn(
    context: Context,
    coroutineScope: CoroutineScope,
    model: Model,
    onDone: () -> Unit,
  ) {
    model.runtimeHelper.cleanUp(model = model, onDone = onDone)
  }

  @Composable
  override fun MainScreen(data: Any) {
    val myData = data as CustomTaskDataForBuiltinTask
    AgentChatScreen(
      task = task,
      modelManagerViewModel = myData.modelManagerViewModel,
      navigateUp = myData.onNavUp,
      agentTools = agentTools,
    )
  }
}

@Module
@InstallIn(SingletonComponent::class)
internal object AgentChatTaskModule {
  @Provides
  @IntoSet
  fun provideTask(): CustomTask {
    return AgentChatTask()
  }

  @Provides
  @Singleton
  fun provideMcpServersDataStore(@ApplicationContext context: Context): DataStore<McpServers> {
    return DataStoreFactory.create(
      serializer = McpServersSerializer,
      produceFile = { context.dataStoreFile("mcp_servers.pb") },
    )
  }
}

fun injectSkillsAndMcpTools(
  baseSystemPrompt: String,
  skills: List<Skill>,
  toolsPrompt: String,
): Contents {
  val selectedSkillsNamesAndDescriptions =
    skills
      .filter { it.selected }
      .joinToString("\n\n") { skill ->
        "- Skill name: \"${skill.name}\"\n- Description: ${skill.description}"
      }

  val systemPrompt =
    baseSystemPrompt
      .replace("___SKILLS___", selectedSkillsNamesAndDescriptions)
      .replace("___TOOLS___", toolsPrompt)
  Log.d(TAG, "System prompt:\n$systemPrompt")
  return Contents.of(systemPrompt)
}

// Check whether the system prompt is the default one.
internal fun isDefaultSystemPrompt(prompt: String): Boolean {
  return prompt == DEFAULT_SYSTEM_PROMPT_TRIMMED ||
    prompt == DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED
}

// Returns the effective default system prompt depending on whether MCP tools are enabled.
internal fun getEffectiveBaseSystemPrompt(currentPrompt: String, hasMcpTools: Boolean): String {
  return if (isDefaultSystemPrompt(currentPrompt)) {
    if (hasMcpTools) DEFAULT_SYSTEM_PROMPT_TRIMMED else DEFAULT_SYSTEM_PROMPT_SKILLS_ONLY_TRIMMED
  } else {
    currentPrompt
  }
}
