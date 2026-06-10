package com.google.ai.edge.gallery.feature.jarvis.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "SimpleWakeWordEngine"

/**
 * A basic wake-word engine using Android's built-in SpeechRecognizer.
 * This is a placeholder for more efficient engines like Porcupine.
 */
class SimpleWakeWordEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : WakeWordEngine, RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onDetectedCallback: (() -> Unit)? = null
    private var isListening = false

    override fun startListening(onDetected: () -> Unit) {
        if (isListening) return
        onDetectedCallback = onDetected
        isListening = true

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@SimpleWakeWordEngine)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        Log.d(TAG, "Started listening for wake word")
    }

    override fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Stopped listening for wake word")
    }

    override fun release() {
        stopListening()
    }

    // RecognitionListener implementation
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { 
            if (it.contains("Jarvis", ignoreCase = true)) {
                Log.d(TAG, "Wake word detected: $it")
                onDetectedCallback?.invoke()
            }
        }
        // Restart listening if we are still active
        if (isListening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            speechRecognizer?.startListening(intent)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { 
            if (it.contains("Jarvis", ignoreCase = true)) {
                Log.d(TAG, "Wake word detected (partial): $it")
                onDetectedCallback?.invoke()
            }
        }
    }

    override fun onError(error: Int) {
        Log.e(TAG, "Speech recognition error: $error")
        if (isListening) {
            stopListening()
            startListening(onDetectedCallback ?: {})
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
