package com.bitopi.mdm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_SERVICE"
        private const val CHANNEL_ID = "fcm_channel_id"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e(TAG, "Wake ======= ${remoteMessage.data}")
        // Acquire wake lock to prevent device sleep
        acquireWakeLock()

        // Get notification data
        val title = remoteMessage.data["status"].toString()
        val body = remoteMessage.data["body"].toString()

        // Log to Logcat
        logFCMMessage(title, body, remoteMessage)

        // Check if this is an install command
        if (title.lowercase().contains("install_app")) {
            handleInstallApp(body)
            return
        }

        // Show notification to user
        showNotification(title, body)

        // Start MainActivity with the notification data
        startMainActivity(title, body)

        // Save notification to shared prefs
        saveNotification(title, body)
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "PhoneLock:FCM_WakeLock"
            )
            wakeLock.acquire(10 * 1000L) // Hold for 10 seconds
            wakeLock.release()
        } catch (e: Exception) {
            Log.e(TAG, "Wake lock error")
        }
    }

    private fun logFCMMessage(title: String, body: String, remoteMessage: RemoteMessage) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        Log.wtf(TAG, "══════════════════════════════════════")
        Log.wtf(TAG, "🎯 FCM NOTIFICATION RECEIVED")
        Log.wtf(TAG, "🕐 Time: $timestamp")
        Log.wtf(TAG, "📢 Title: $title")
        Log.wtf(TAG, "📝 Body: $body")
        Log.wtf(TAG, "══════════════════════════════════════")

        // Log data payload if available
        if (remoteMessage.data.isNotEmpty()) {
            Log.wtf(TAG, "📊 Data Payload: ${remoteMessage.data["message"]}")
            remoteMessage.data.forEach { (key, value) ->
                Log.wtf(TAG, "   $key = $value")
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        try {
            val notificationId = System.currentTimeMillis().toInt()

            // Intent to open MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("fcm_notification", true)
                putExtra("title", title)
                putExtra("body", body)
                putExtra("time", System.currentTimeMillis())

                // Determine action based on body
                when {
                    body.contains("Lock device", ignoreCase = true) -> {
                        putExtra("fcm_action", "lock")
                    }
                    body.contains("Active Device", ignoreCase = true) -> {
                        putExtra("fcm_action", "unlock")
                    }
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_n)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel for Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "PhoneLock Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shows when device receives remote commands"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 300, 500)
                }
                manager.createNotificationChannel(channel)
            }

            manager.notify(notificationId, builder.build())
            Log.d(TAG, "✅ Notification shown")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Notification error: ${e.message}")
        }
    }

    private fun startMainActivity(title: String, body: String) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("fcm_notification", true)
                putExtra("title", title)
                putExtra("body", body)
                putExtra("time", System.currentTimeMillis())
                putExtra("from_service", true)

                // Determine action based on body
                when {
                    body.contains("Lock Device", ignoreCase = true) -> {
                        putExtra("fcm_action", "lock")
                    }
                    body.contains("Active Device", ignoreCase = true) -> {
                        putExtra("fcm_action", "unlock")
                    }
                }
            }

            startActivity(intent)
            Log.d(TAG, "✅ Started MainActivity")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start activity: ${e.message}")
        }
    }

    private fun saveNotification(title: String, body: String) {
        try {
            val prefs = getSharedPreferences("FCM_NOTIFICATIONS", Context.MODE_PRIVATE)
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

            val history = prefs.getString("history", "") ?: ""
            val newEntry = "[$timestamp] $title: $body\n"

            // Keep last 10 notifications
            val updatedHistory = (history + newEntry).lines().takeLast(10).joinToString("\n")

            prefs.edit().apply {
                putString("history", updatedHistory)
                putString("last_title", title)
                putString("last_body", body)
                putLong("last_time", System.currentTimeMillis())
                apply()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save notification")
        }
    }

    private fun handleInstallApp(apkUrl: String) {
        Log.d(TAG, "══════════════════════════════════════")
        Log.d(TAG, "📦 INSTALL APP COMMAND RECEIVED")
        Log.d(TAG, "📦 APK URL: $apkUrl")
        Log.d(TAG, "══════════════════════════════════════")

        // Show download notification
        showNotification("Installing App", "Downloading APK...")

        val installer = SilentInstaller(applicationContext)
        installer.downloadAndInstall(apkUrl, object : SilentInstaller.InstallCallback {
            override fun onDownloadStarted() {
                Log.d(TAG, "📥 Download started")
            }

            override fun onDownloadProgress(percent: Int) {
                Log.d(TAG, "📥 Download: $percent%")
            }

            override fun onDownloadComplete() {
                Log.d(TAG, "📥 Download complete")
                showNotification("Installing App", "Download complete, installing...")
            }

            override fun onInstallStarted() {
                Log.d(TAG, "📦 Install started")
            }

            override fun onInstallSuccess(packageName: String?) {
                Log.d(TAG, "✅ Install SUCCESS: $packageName")
                showNotification("App Installed", "Successfully installed: $packageName")
            }

            override fun onInstallFailed(error: String) {
                Log.e(TAG, "❌ Install FAILED: $error")
                showNotification("Install Failed", error)
            }
        })
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.wtf(TAG, "══════════════════════════════════════")
        Log.wtf(TAG, "🆕 NEW FCM TOKEN")
        Log.wtf(TAG, "Token: ${token.take(20)}...")
        Log.wtf(TAG, "══════════════════════════════════════")

        // Save token
        val prefs = getSharedPreferences("FCM_TOKENS", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("latest_token", token)
            putLong("token_time", System.currentTimeMillis())
            apply()
        }

        // Show notification about new token
        showTokenNotification(token)
    }

    private fun showTokenNotification(token: String) {
        try {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_n)
                .setContentTitle("FCM Token Updated")
                .setContentText("Token changed: ${token.take(10)}...")
                .setPriority(NotificationCompat.PRIORITY_LOW)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(999, builder.build())

        } catch (e: Exception) {
            Log.e(TAG, "Token notification error")
        }
    }
}
