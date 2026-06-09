package com.google.ai.edge.gallery.feature.jarvis.tools

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JarvisToolExecutor"

/**
 * Executor for Jarvis tools.
 */
@Singleton
class ToolExecutor @Inject constructor(
    private val registry: ToolRegistry
) {
    /**
     * Executes a tool by name with the given parameters.
     */
    fun execute(toolName: String, parameters: Map<String, Any>): ToolResult {
        Log.d(TAG, "Executing tool: $toolName with params: $parameters")
        val tool = registry.getTool(toolName) ?: return ToolResult(
            success = false,
            message = "Tool not found: $toolName"
        )

        return try {
            tool.execute(parameters)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing tool $toolName", e)
            ToolResult(
                success = false,
                message = "Error executing tool $toolName: ${e.message}"
            )
        }
    }
}
