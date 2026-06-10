package com.google.ai.edge.gallery.feature.jarvis.core

import android.content.Context
import android.util.Log
import com.google.ai.edge.gallery.customtasks.common.CustomTask
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.runtime.runtimeHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JarvisModelManager"

/**
 * Manages the persistent loading of models for Jarvis to ensure zero-latency response.
 */
@Singleton
class JarvisModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customTasks: Set<@JvmSuppressWildcards CustomTask>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var persistentModel: Model? = null
    private var persistentTask: Task? = null

    /**
     * Ensures that the specified model is loaded and ready for inference.
     */
    fun loadModel(task: Task, model: Model, onDone: (String) -> Unit = {}) {
        if (persistentModel?.name == model.name && model.instance != null) {
            Log.d(TAG, "Model ${model.name} already loaded.")
            onDone("")
            return
        }

        persistentModel = model
        persistentTask = task
        val customTask = customTasks.find { it.task.id == task.id }
        
        if (customTask == null) {
            onDone("Custom task for ${task.id} not found")
            return
        }

        scope.launch {
            Log.d(TAG, "Initializing persistent model: ${model.name}")
            customTask.initializeModelFn(context, this, model) { error ->
                if (error.isEmpty()) {
                    Log.d(TAG, "Persistent model ${model.name} loaded successfully")
                } else {
                    Log.e(TAG, "Failed to load persistent model ${model.name}: $error")
                }
                onDone(error)
            }
        }
    }

    /**
     * Unloads the persistent model to free up resources.
     */
    fun unloadModel(task: Task) {
        val model = persistentModel ?: return
        val customTask = customTasks.find { it.task.id == task.id }
        
        scope.launch {
            Log.d(TAG, "Unloading persistent model: ${model.name}")
            customTask?.cleanUpModelFn(context, this, model) {
                persistentModel = null
                Log.d(TAG, "Persistent model unloaded")
            }
        }
    }

    fun getPersistentModel(): Model? = persistentModel

    fun getPersistentTask(): Task? = persistentTask

    /**
     * Runs inference on the persistent model.
     */
    fun runInference(
        input: String,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val model = persistentModel ?: run {
            onError("No persistent model loaded")
            return
        }

        scope.launch {
            try {
                var fullResponse = ""
                model.runtimeHelper.runInference(
                    model = model,
                    input = input,
                    resultListener = { partial, done, _ ->
                        if (partial.isNotEmpty()) {
                            fullResponse += partial
                            onToken(partial)
                        }
                        if (done) {
                            onComplete(fullResponse)
                        }
                    },
                    cleanUpListener = {},
                    onError = { onError(it) },
                    coroutineScope = this
                )
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error during inference")
            }
        }
    }
}
