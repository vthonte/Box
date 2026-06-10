package com.google.ai.edge.gallery.feature.jarvis.settings

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JarvisSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "jarvis_settings"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isJarvisEnabled = MutableStateFlow(prefs.getBoolean("jarvis_enabled", false))
    val isJarvisEnabled: StateFlow<Boolean> = _isJarvisEnabled.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(prefs.getBoolean("wake_word_enabled", true))
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _selectedModelName = MutableStateFlow(prefs.getString("selected_model", ""))
    val selectedModelName: StateFlow<String?> = _selectedModelName.asStateFlow()

    fun setJarvisEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("jarvis_enabled", enabled).apply()
        _isJarvisEnabled.value = enabled
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("wake_word_enabled", enabled).apply()
        _isWakeWordEnabled.value = enabled
    }

    fun setSelectedModel(modelName: String) {
        prefs.edit().putString("selected_model", modelName).apply()
        _selectedModelName.value = modelName
    }
}
