package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "ShareTool"

class ShareTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "share_text"
    override val description: String = "Share text to other apps. Params: 'text' (string)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val text = parameters["text"] as? String ?: return ToolResult(
            success = false,
            message = "Missing parameter 'text'"
        )

        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share with Jarvis")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            ToolResult(success = true, message = "Sharing text: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share text", e)
            ToolResult(success = false, message = "Failed to share text: ${e.message}")
        }
    }
}
