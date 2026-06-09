package com.google.ai.edge.gallery.feature.jarvis.tools

/**
 * Result of a tool execution.
 */
data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

/**
 * Interface for Jarvis tools.
 */
interface Tool {
    val name: String
    val description: String
    
    /**
     * Executes the tool with the given parameters.
     */
    fun execute(parameters: Map<String, Any>): ToolResult
}
