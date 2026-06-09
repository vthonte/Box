package com.google.ai.edge.gallery.feature.jarvis.tools

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "FlashlightTool"

class FlashlightTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {
    override val name: String = "flashlight"
    override val description: String = "Turn the device flashlight on or off. Params: 'enabled' (boolean)"

    override fun execute(parameters: Map<String, Any>): ToolResult {
        val enabled = parameters["enabled"] as? Boolean ?: return ToolResult(
            success = false,
            message = "Missing parameter 'enabled'"
        )

        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enabled)
            
            ToolResult(
                success = true,
                message = "Flashlight turned ${if (enabled) "on" else "off"}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle flashlight", e)
            ToolResult(
                success = false,
                message = "Failed to toggle flashlight: ${e.message}"
            )
        }
    }
}
