package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "AlarmTool"

class AlarmTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "set_alarm"
    override val description: String = "Set a new alarm. Params: 'hour' (int), 'minute' (int), 'label' (string)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val hour = (parameters["hour"] as? Number)?.toInt() ?: return ToolResult(
            success = false,
            message = "Missing parameter 'hour'"
        )
        val minute = (parameters["minute"] as? Number)?.toInt() ?: return ToolResult(
            success = false,
            message = "Missing parameter 'minute'"
        )
        val label = parameters["label"] as? String ?: "Jarvis Alarm"

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(success = true, message = "Alarm set for $hour:$minute with label '$label'")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarm", e)
            ToolResult(success = false, message = "Failed to set alarm: ${e.message}")
        }
    }
}
