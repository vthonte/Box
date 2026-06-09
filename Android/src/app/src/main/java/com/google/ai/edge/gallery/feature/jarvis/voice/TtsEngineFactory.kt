package com.google.ai.edge.gallery.feature.jarvis.voice

import com.google.ai.edge.gallery.feature.jarvis.settings.JarvisSettingsManager
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TtsEngineFactory @Inject constructor(
    private val systemTtsProvider: Provider<SystemTtsEngine>,
    private val settingsManager: JarvisSettingsManager
) {
    fun getEngine(): TtsEngine {
        // TODO: Support other engines (Piper, Kokoro, Supertonic)
        // val type = settingsManager.selectedTtsType.value
        return systemTtsProvider.get()
    }
}
