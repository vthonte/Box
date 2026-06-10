package com.google.ai.edge.gallery.feature.jarvis.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.feature.jarvis.JarvisFeatureFlag
import com.google.ai.edge.gallery.feature.jarvis.core.JarvisManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "JarvisService"
private const val CHANNEL_ID = "jarvis_channel"
private const val NOTIFICATION_ID = 1001

private const val ACTION_STOP = "com.google.ai.edge.gallery.feature.jarvis.ACTION_STOP"
private const val ACTION_UNLOAD = "com.google.ai.edge.gallery.feature.jarvis.ACTION_UNLOAD"

/**
 * Foreground service that keeps Jarvis active in the background.
 * Responsibilities include wake-word monitoring and model persistence.
 */
@AndroidEntryPoint
class JarvisForegroundService : Service() {

    @Inject lateinit var jarvisManager: JarvisManager
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        if (!JarvisFeatureFlag.IS_ENABLED) {
            stopSelf()
            return
        }
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Jarvis::WakeWordLock")
        wakeLock?.acquire()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "JarvisForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(TAG, "Stop action received")
                jarvisManager.stopInteraction()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UNLOAD -> {
                Log.d(TAG, "Unload action received")
                jarvisManager.unloadPersistentModel()
            }
            else -> {
                Log.d(TAG, "JarvisForegroundService started")
                jarvisManager.startWakeWordMonitoring()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        jarvisManager.stopWakeWordMonitoring()
        wakeLock?.release()
        super.onDestroy()
        Log.d(TAG, "JarvisForegroundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Jarvis Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val unloadIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_UNLOAD
        }
        val unloadPendingIntent = PendingIntent.getService(
            this, 1, unloadIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jarvis is active")
            .setContentText("Listening for wake word...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Assistant", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_delete, "Unload AI", unloadPendingIntent)
            .build()
    }
}
