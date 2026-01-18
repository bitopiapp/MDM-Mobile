//package com.uztech.phonelock
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // Handle notification payload
//        println("FCM Token:===================>1111111111111 ")
//        remoteMessage.notification?.let {
//            showNotification(it.title, it.body)
//        }
//
//        // Handle data payload if needed
//        if (remoteMessage.data.isNotEmpty()) {
//            val title = remoteMessage.data["title"]
//            val body = remoteMessage.data["body"]
//            showNotification(title, body)
//        }
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        // Send token to your backend for targeted notifications
//        println("FCM Token:===================> $token")
//    }
//
//    private fun showNotification(title: String?, message: String?) {
//        val channelId = "default_channel"
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_n) // ensure you have this icon in res/drawable
//            .setContentTitle(title ?: "Notification")
//            .setContentText(message ?: "")
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create channel for Android 8.0+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Default Channel",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            manager.createNotificationChannel(channel)
//        }
//
//        manager.notify(0, builder.build())
//    }
//
//
//}

//--------------------------------------------------full running-------------------------------------------------

package com.uztech.phonelock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_MESSAGE", "Message received from: ${remoteMessage.from}")

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d("FCM_MESSAGE", "Notification Title: ${notification.title}")
            Log.d("FCM_MESSAGE", "Notification Body: ${notification.body}")
            showNotification(notification.title, notification.body)
        }

        // Handle data payload if needed
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM_MESSAGE", "Data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            if (title != null || body != null) {
                showNotification(title, body)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New FCM Token: $token")

        // Send token to your backend for targeted notifications
        sendTokenToServer(token)

        // Save token locally (optional)
        saveTokenLocally(token)
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel"
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_n) // Make sure you have this icon
            .setContentTitle(title ?: "Notification")
            .setContentText(message ?: "")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default notification channel"
            }
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement your API call to send token to server
        Log.d("FCM_TOKEN", "Sending token to server: ${token.take(10)}...")
    }

    private fun saveTokenLocally(token: String) {
        val prefs = getSharedPreferences("FCM_Prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d("FCM_TOKEN", "Token saved locally")
    }
}


