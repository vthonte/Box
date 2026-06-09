package com.google.ai.edge.gallery.feature.jarvis.voice

/**
 * Abstraction for wake-word detection engines.
 */
interface WakeWordEngine {
    /**
     * Start listening for the wake word.
     */
    fun startListening(onDetected: () -> Unit)

    /**
     * Stop listening for the wake word.
     */
    fun stopListening()

    /**
     * Clean up resources.
     */
    fun release()
}
