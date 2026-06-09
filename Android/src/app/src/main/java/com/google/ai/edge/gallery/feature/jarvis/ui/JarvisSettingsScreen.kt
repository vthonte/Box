package com.google.ai.edge.gallery.feature.jarvis.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.feature.jarvis.settings.JarvisSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSettingsScreen(
    settingsManager: JarvisSettingsManager,
    modelManagerViewModel: com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel,
    onBack: () -> Unit
) {
    val isJarvisEnabled by settingsManager.isJarvisEnabled.collectAsState()
    val isWakeWordEnabled by settingsManager.isWakeWordEnabled.collectAsState()
    val selectedModelName by settingsManager.selectedModelName.collectAsState()
    
    val downloadedModels = modelManagerViewModel.getAllDownloadedModels()
    var showModelPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jarvis Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable Jarvis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Jarvis Mode", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Always-on assistant with background capabilities.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isJarvisEnabled,
                    onCheckedChange = { settingsManager.setJarvisEnabled(it) }
                )
            }

            HorizontalDivider()

            // Wake Word
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Wake Word Activation", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Respond to 'Hey Jarvis'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isWakeWordEnabled,
                    enabled = isJarvisEnabled,
                    onCheckedChange = { settingsManager.setWakeWordEnabled(it) }
                )
            }
            
            // Model selection (simplified for now)
            Text("AI Model", style = MaterialTheme.typography.titleSmall)
            Text(
                "Jarvis uses the persistently loaded model for seamless interaction.",
                style = MaterialTheme.typography.bodySmall
            )
            
            OutlinedButton(
                onClick = { showModelPicker = true },
                enabled = isJarvisEnabled
            ) {
                Text(if (selectedModelName.isNullOrEmpty()) "Select Model" else "Model: $selectedModelName")
            }

            if (showModelPicker) {
                AlertDialog(
                    onDismissRequest = { showModelPicker = false },
                    title = { Text("Select LLM Model") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            downloadedModels.forEach { model ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            settingsManager.setSelectedModel(model.name)
                                            showModelPicker = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedModelName == model.name,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(model.displayName.ifEmpty { model.name })
                                }
                            }
                            if (downloadedModels.isEmpty()) {
                                Text("No models downloaded. Please download an LLM model first.")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showModelPicker = false }) { Text("Close") }
                    }
                )
            }
            
            // Memory selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Long-term Memory", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Remember context across sessions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = true, // Force enabled for now
                    enabled = false,
                    onCheckedChange = {}
                )
            }
        }
    }
}
