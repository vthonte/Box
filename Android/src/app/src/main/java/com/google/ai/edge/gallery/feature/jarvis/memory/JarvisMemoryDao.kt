package com.google.ai.edge.gallery.feature.jarvis.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JarvisMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Query("SELECT * FROM jarvis_memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM jarvis_memories WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMemories(query: String): List<MemoryEntity>

    @Query("DELETE FROM jarvis_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: JarvisHistoryEntity)

    @Query("SELECT * FROM jarvis_history WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getHistoryForSession(sessionId: String): List<JarvisHistoryEntity>
}
