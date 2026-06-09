package com.google.ai.edge.gallery.feature.jarvis.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

private const val TAG = "SystemTtsEngine"

/**
 * Implementation of TtsEngine using Android's built-in TextToSpeech.
 */
class SystemTtsEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : TtsEngine {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            } else {
                Log.e(TAG, "Failed to initialize TextToSpeech")
            }
        }
    }

    override fun speak(text: String, onDone: () -> Unit) {
        if (!isInitialized) {
            pendingText = text
            return
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onDone()
            }
            override fun onError(utteranceId: String?) {
                onDone()
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utterance")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
