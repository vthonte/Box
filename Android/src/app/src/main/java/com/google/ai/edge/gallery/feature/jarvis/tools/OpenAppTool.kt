package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "OpenAppTool"

class OpenAppTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "open_app"
    override val description: String = "Open an application by its package name or common name. Params: 'app' (string)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val appName = parameters["app"] as? String ?: return ToolResult(
            success = false,
            message = "Missing parameter 'app'"
        )

        // Map common names to package names if needed
        val packageName = when (appName.lowercase()) {
            "camera" -> "android.media.action.IMAGE_CAPTURE" // Special case or package?
            "messages" -> "com.google.android.apps.messaging"
            "browser", "chrome" -> "com.android.chrome"
            "maps" -> "com.google.android.apps.maps"
            else -> appName // Assume it's already a package name or handle searching later
        }

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                ToolResult(success = true, message = "Opened $appName")
            } else {
                ToolResult(success = false, message = "Could not find app: $appName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app", e)
            ToolResult(success = false, message = "Failed to open app: ${e.message}")
        }
    }
}
