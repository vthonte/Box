package com.google.ai.edge.gallery.feature.jarvis.core

import android.util.Log
import com.google.ai.edge.gallery.feature.jarvis.JarvisFeatureFlag
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JarvisSessionManager"

/**
 * Manages assistant sessions, context tracking, and session persistence.
 */
@Singleton
class JarvisSessionManager @Inject constructor() {
    
    private var currentSessionId: String? = null
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    init {
        if (JarvisFeatureFlag.IS_ENABLED) {
            startNewSession()
        }
    }

    /**
     * Starts a new Jarvis session.
     */
    fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
        conversationHistory.clear()
        Log.d(TAG, "New Jarvis session started: $currentSessionId")
    }

    /**
     * Tracks a conversation turn (user query and assistant response).
     */
    fun trackTurn(userQuery: String, assistantResponse: String) {
        conversationHistory.add("user" to userQuery)
        conversationHistory.add("assistant" to assistantResponse)
        
        // TODO: Persist to database if memory is enabled
    }

    /**
     * Returns the current conversation context for the LLM.
     */
    fun getContext(): List<Pair<String, String>> {
        return conversationHistory.toList()
    }

    fun getSessionId(): String? = currentSessionId
}
