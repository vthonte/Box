package com.google.ai.edge.gallery.feature.jarvis.tools

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry for Jarvis tools.
 */
@Singleton
class ToolRegistry @Inject constructor(
    private val tools: Set<@JvmSuppressWildcards Tool>
) {
    private val toolMap = tools.associateBy { it.name }

    /**
     * Returns all registered tools.
     */
    fun getAllTools(): List<Tool> = tools.toList()

    /**
     * Returns a tool by its name.
     */
    fun getTool(name: String): Tool? = toolMap[name]
}
