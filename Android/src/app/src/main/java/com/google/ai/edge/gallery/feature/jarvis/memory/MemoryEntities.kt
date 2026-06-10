package com.google.ai.edge.gallery.feature.jarvis.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a "fact" or "memory" stored by Jarvis.
 */
@Entity(tableName = "jarvis_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1,
    val category: String = "general"
)

/**
 * Entity representing a summary of a conversation turn for context retrieval.
 */
@Entity(tableName = "jarvis_history")
data class JarvisHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val userQuery: String,
    val assistantResponse: String,
    val timestamp: Long = System.currentTimeMillis()
)
