package com.google.ai.edge.gallery.feature.jarvis.voice

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.google.ai.edge.gallery.feature.jarvis.JarvisFeatureFlag
import com.google.ai.edge.gallery.feature.jarvis.core.JarvisManager
import com.google.ai.edge.gallery.feature.jarvis.core.JarvisState
import com.google.ai.edge.gallery.whisper.WhisperEngine
import com.google.ai.edge.gallery.whisper.toFloat32
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.math.abs

private const val TAG = "JarvisVoiceManager"
private const val SAMPLE_RATE = 16000
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
private const val VAD_THRESHOLD = 1500 // Basic amplitude threshold
private const val SILENCE_TIMEOUT_MS = 1500L

/**
 * Manages the continuous voice interaction pipeline.
 */
@Singleton
class JarvisVoiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jarvisManager: Provider<JarvisManager>,
    private val ttsEngineFactory: TtsEngineFactory
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordingJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    
    private var whisperEngine: WhisperEngine? = null

    private val ttsEngine: TtsEngine by lazy { ttsEngineFactory.getEngine() }

    fun setWhisperEngine(engine: WhisperEngine) {
        this.whisperEngine = engine
    }

    @SuppressLint("MissingPermission")
    fun startContinuousListening() {
        if (!JarvisFeatureFlag.IS_ENABLED || isRunning.get()) return
        isRunning.set(true)
        
        recordingJob = scope.launch {
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            val buffer = ShortArray(bufferSize / 2)
            val audioData = mutableListOf<Short>()
            var lastVoiceTime = System.currentTimeMillis()
            var isVoiceDetected = false

            recorder.startRecording()
            Log.d(TAG, "Started continuous listening")

            try {
                while (isActive && isRunning.get()) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val peak = buffer.take(read).maxOf { abs(it.toInt()) }
                        
                        if (peak > VAD_THRESHOLD) {
                            if (!isVoiceDetected) {
                                Log.d(TAG, "Voice detected")
                                isVoiceDetected = true
                                // Interruption: Stop speaking if we were speaking
                                if (jarvisManager.get().state.value == JarvisState.SPEAKING) {
                                    stopSpeaking()
                                }
                                jarvisManager.get().setState(JarvisState.LISTENING)
                            }
                            lastVoiceTime = System.currentTimeMillis()
                            for (i in 0 until read) audioData.add(buffer[i])
                        } else if (isVoiceDetected) {
                            for (i in 0 until read) audioData.add(buffer[i])
                            if (System.currentTimeMillis() - lastVoiceTime > SILENCE_TIMEOUT_MS) {
                                Log.d(TAG, "Silence detected, processing...")
                                val capturedAudio = audioData.toShortArray()
                                audioData.clear()
                                isVoiceDetected = false
                                processAudio(capturedAudio)
                            }
                        }
                    }
                    delay(10)
                }
            } finally {
                recorder.stop()
                recorder.release()
                Log.d(TAG, "Stopped continuous listening")
            }
        }
    }

    private suspend fun processAudio(samples: ShortArray) {
        val floatSamples = samples.toFloat32()
        val engine = whisperEngine
        if (engine == null) {
            Log.e(TAG, "WhisperEngine not set")
            return
        }

        val text = engine.transcribe(floatSamples)
        if (text.isNotBlank()) {
            Log.d(TAG, "Transcribed: $text")
            withContext(Dispatchers.Main) {
                val manager = jarvisManager.get()
                manager.processQuery(text, manager.getPersistentModel()!!)
            }
        } else {
            jarvisManager.get().setState(JarvisState.IDLE)
        }
    }

    fun stopContinuousListening() {
        isRunning.set(false)
        recordingJob?.cancel()
        recordingJob = null
    }

    fun speak(text: String, onDone: () -> Unit = {}) {
        ttsEngine.speak(text, onDone)
    }

    fun stopSpeaking() {
        ttsEngine.stop()
    }
}
