package com.uztech.phonelock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
    // for ck device
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle notification payload
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }

        // Handle data payload if needed
        if (remoteMessage.data.isNotEmpty()) {
            if (dpm.isAdminActive(admin)) {
                dpm.lockNow()
                Toast.makeText(this, "Device locked!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enable device admin first!", Toast.LENGTH_LONG).show()
            }
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            showNotification(title, body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your backend for targeted notifications
        println("FCM Token:===================> $token")
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel"
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_n) // ensure you have this icon in res/drawable
            .setContentTitle(title ?: "Notification")
            .setContentText(message ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }
}
