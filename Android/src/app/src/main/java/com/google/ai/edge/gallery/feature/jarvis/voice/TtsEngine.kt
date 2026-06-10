package com.google.ai.edge.gallery.feature.jarvis.voice

/**
 * Abstraction for Text-to-Speech engines.
 */
interface TtsEngine {
    /**
     * Speak the given text.
     */
    fun speak(text: String, onDone: () -> Unit = {})

    /**
     * Stop speaking.
     */
    fun stop()

    /**
     * Release resources.
     */
    fun release()
}
