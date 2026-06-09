package com.google.ai.edge.gallery.feature.jarvis.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.feature.jarvis.JarvisFeatureFlag
import com.google.ai.edge.gallery.feature.jarvis.memory.JarvisMemoryManager
import com.google.ai.edge.gallery.feature.jarvis.service.JarvisForegroundService
import com.google.ai.edge.gallery.feature.jarvis.tools.ToolExecutor
import com.google.ai.edge.gallery.feature.jarvis.settings.JarvisSettingsManager
import com.google.ai.edge.gallery.feature.jarvis.ui.JarvisOverlayService
import com.google.ai.edge.gallery.feature.jarvis.voice.JarvisVoiceManager
import com.google.ai.edge.gallery.feature.jarvis.voice.WakeWordEngine
import com.google.ai.edge.gallery.whisper.WhisperEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val TAG = "JarvisManager"

/**
 * State machine for the Jarvis Assistant.
 */
enum class JarvisState {
    IDLE,
    LISTENING,
    THINKING,
    EXECUTING,
    SPEAKING
}

/**
 * JarvisManager is the central coordinator for the Jarvis AI assistant.
 * It manages the conversation lifecycle, tool execution, and state transitions.
 */
@Singleton
class JarvisManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: JarvisSessionManager,
    private val modelManager: JarvisModelManager,
    private val memoryManager: JarvisMemoryManager,
    private val toolExecutor: ToolExecutor,
    private val settingsManager: JarvisSettingsManager,
    private val wakeWordEngine: Provider<WakeWordEngine>,
    private val voiceManager: Provider<JarvisVoiceManager>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(JarvisState.IDLE)
    val state: StateFlow<JarvisState> = _state.asStateFlow()

    init {
        if (JarvisFeatureFlag.IS_ENABLED) {
            Log.d(TAG, "JarvisManager initialized")
            observeSettings()
        }
    }

    private fun observeSettings() {
        scope.launch {
            settingsManager.isJarvisEnabled.collect { enabled ->
                if (enabled) {
                    startService()
                    // Auto-load model if selected
                    val modelName = settingsManager.selectedModelName.value
                    if (!modelName.isNullOrEmpty() && getPersistentModel() == null) {
                        // We need a way to get the actual Model object from the name
                        // This usually requires ModelManagerViewModel or a shared Repository
                    }
                } else {
                    stopService()
                }
            }
        }
    }

    /**
     * Start the Jarvis background service and overlay.
     */
    fun startService() {
        if (!JarvisFeatureFlag.IS_ENABLED) return
        
        val intent = Intent(context, JarvisForegroundService::class.java)
        context.startForegroundService(intent)

        val overlayIntent = Intent(context, JarvisOverlayService::class.java)
        context.startService(overlayIntent)
    }

    /**
     * Call this to initialize models needed for Jarvis (LLM and Whisper).
     */
    fun initializeJarvis(
        llmTask: Task, llmModel: Model,
        whisperTask: Task, whisperModel: Model,
        onDone: (String) -> Unit
    ) {
        modelManager.loadModel(llmTask, llmModel) { llmError ->
            if (llmError.isNotEmpty()) {
                onDone("LLM Load Error: $llmError")
                return@loadModel
            }
            
            modelManager.loadModel(whisperTask, whisperModel) { whisperError ->
                if (whisperError.isNotEmpty()) {
                    onDone("Whisper Load Error: $whisperError")
                } else {
                    val whisperEngine = whisperModel.instance as? WhisperEngine
                    if (whisperEngine != null) {
                        voiceManager.get().setWhisperEngine(whisperEngine)
                        onDone("")
                    } else {
                        onDone("Failed to get WhisperEngine instance")
                    }
                }
            }
        }
    }

    /**
     * Stop the Jarvis background service and overlay.
     */
    fun stopService() {
        val intent = Intent(context, JarvisForegroundService::class.java)
        context.stopService(intent)

        val overlayIntent = Intent(context, JarvisOverlayService::class.java)
        context.stopService(overlayIntent)

        stopWakeWordMonitoring()
        voiceManager.get().stopContinuousListening()
    }

    /**
     * Start monitoring for the wake word.
     */
    fun startWakeWordMonitoring() {
        if (!JarvisFeatureFlag.IS_ENABLED) return
        wakeWordEngine.get().startListening {
            Log.d(TAG, "Wake word detected! Starting interaction...")
            startInteraction()
        }
    }

    /**
     * Stop monitoring for the wake word.
     */
    fun stopWakeWordMonitoring() {
        wakeWordEngine.get().stopListening()
    }

    /**
     * Start a new interaction with Jarvis.
     */
    fun startInteraction() {
        if (!JarvisFeatureFlag.IS_ENABLED) return
        _state.value = JarvisState.LISTENING
        voiceManager.get().startContinuousListening()
    }

    /**
     * Stop the current interaction.
     */
    fun stopInteraction() {
        _state.value = JarvisState.IDLE
        voiceManager.get().stopContinuousListening()
        voiceManager.get().stopSpeaking()
    }

    /**
     * Process a user query.
     */
    fun processQuery(query: String, model: Model) {
        if (!JarvisFeatureFlag.IS_ENABLED) return
        _state.value = JarvisState.THINKING
        
        Log.d(TAG, "Processing query: $query")
        
        scope.launch {
            val sessionId = sessionManager.getSessionId() ?: "default"
            
            // 1. Recall relevant memories
            val memories = memoryManager.recall(query)
            val memoryContext = if (memories.isNotEmpty()) {
                "\nRelevant information you previously learned:\n- ${memories.joinToString("\n- ")}"
            } else ""

            // 2. Prepare prompt with memory
            val fullPrompt = query + memoryContext

            // 3. Run inference
            modelManager.runInference(
                input = fullPrompt,
                onToken = { /* Streaming UI update if any */ },
                onComplete = { response ->
                    Log.d(TAG, "Jarvis Response: $response")
                    
                    // 4. Save to history
                    scope.launch {
                        memoryManager.saveHistory(sessionId, query, response)
                        
                        // Heuristic: If response is very short and factual, maybe remember it?
                        // For now, let's just use the manual trackTurn
                        sessionManager.trackTurn(query, response)
                    }

                    // 5. Handle tool calls (Placeholder for Phase 5 tool detection)
                    // if (response.contains("ACTION:")) { ... }

                    _state.value = JarvisState.SPEAKING
                    voiceManager.get().speak(response) {
                        _state.value = JarvisState.IDLE
                        // Continuous mode: start interaction again
                        startInteraction()
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Inference error: $error")
                    _state.value = JarvisState.IDLE
                    startInteraction()
                }
            )
        }
    }

    fun getPersistentModel(): Model? = modelManager.getPersistentModel()

    /**
     * Unloads the persistent LLM model to free memory.
     */
    fun unloadPersistentModel() {
        val model = getPersistentModel() ?: return
        // We need the task to unload properly via JarvisModelManager
        // For now, let's look it up or assume BuiltInTaskId.LLM_CHAT
        val task = modelManager.getPersistentTask() // I'll add this to ModelManager
        if (task != null) {
            modelManager.unloadModel(task)
        }
    }

    fun setState(newState: JarvisState) {
        _state.value = newState
    }
}
