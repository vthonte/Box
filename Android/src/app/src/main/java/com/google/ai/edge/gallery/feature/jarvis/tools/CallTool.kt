package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "CallTool"

class CallTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "make_call"
    override val description: String = "Open the dialer with a phone number. Params: 'phoneNumber' (string)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val phoneNumber = parameters["phoneNumber"] as? String ?: return ToolResult(
            success = false,
            message = "Missing parameter 'phoneNumber'"
        )

        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(success = true, message = "Dialing $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open dialer", e)
            ToolResult(success = false, message = "Failed to open dialer: ${e.message}")
        }
    }
}
