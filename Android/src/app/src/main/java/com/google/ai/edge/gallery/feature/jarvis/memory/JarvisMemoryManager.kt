package com.google.ai.edge.gallery.feature.jarvis.memory

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JarvisMemoryManager"

@Singleton
class JarvisMemoryManager @Inject constructor(
    private val dao: JarvisMemoryDao
) {
    /**
     * Stores a new memory fact.
     */
    suspend fun remember(content: String) {
        Log.d(TAG, "Remembering: $content")
        dao.insertMemory(MemoryEntity(content = content))
    }

    /**
     * Retrieves relevant memories for a given query.
     * Simple keyword-based search for now (RAG-lite).
     */
    suspend fun recall(query: String): List<String> {
        val keywords = query.split(" ").filter { it.length > 3 }
        val results = mutableSetOf<MemoryEntity>()
        
        for (word in keywords) {
            results.addAll(dao.searchMemories(word))
        }

        return results.sortedByDescending { it.timestamp }
            .take(5)
            .map { it.content }
    }

    /**
     * Saves a turn in the conversation history.
     */
    suspend fun saveHistory(sessionId: String, query: String, response: String) {
        dao.insertHistory(
            JarvisHistoryEntity(
                sessionId = sessionId,
                userQuery = query,
                assistantResponse = response
            )
        )
    }

    /**
     * Loads the last few turns for context.
     */
    suspend fun getRecentHistory(sessionId: String, limit: Int = 10): List<Pair<String, String>> {
        return dao.getHistoryForSession(sessionId)
            .takeLast(limit)
            .flatMap { listOf("user" to it.userQuery, "assistant" to it.assistantResponse) }
    }
}
