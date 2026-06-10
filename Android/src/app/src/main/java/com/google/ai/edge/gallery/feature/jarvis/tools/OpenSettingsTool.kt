package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "OpenSettingsTool"

class OpenSettingsTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "open_settings"
    override val description: String = "Open device settings. Params: 'type' (string: wifi, bluetooth, sound, general)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val type = parameters["type"] as? String ?: "general"
        
        val action = when (type.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound" -> Settings.ACTION_SOUND_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(success = true, message = "Opened $type settings")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
            ToolResult(success = false, message = "Failed to open settings: ${e.message}")
        }
    }
}
