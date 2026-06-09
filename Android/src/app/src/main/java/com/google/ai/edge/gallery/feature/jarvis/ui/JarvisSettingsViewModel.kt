package com.google.ai.edge.gallery.feature.jarvis.ui

import androidx.lifecycle.ViewModel
import com.google.ai.edge.gallery.feature.jarvis.settings.JarvisSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JarvisSettingsViewModel @Inject constructor(
    val settingsManager: JarvisSettingsManager
) : ViewModel()
