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

//package com.uztech.phonelock
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage

//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d("FCM_MESSAGE", "Message received from: ${remoteMessage.from}")
//
//        // Handle notification payload
//        remoteMessage.notification?.let { notification ->
//            Log.d("FCM_MESSAGE", "Notification Title: ${notification.title}")
//            Log.d("FCM_MESSAGE", "Notification Body: ${notification.body}")
//            showNotification(notification.title, notification.body)
//        }
//
//        // Handle data payload if needed
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.d("FCM_MESSAGE", "Data payload: ${remoteMessage.data}")
//            val title = remoteMessage.data["title"]
//            val body = remoteMessage.data["body"]
//            if (title != null || body != null) {
//                showNotification(title, body)
//            }
//        }
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d("FCM_TOKEN", "New FCM Token: $token")
//
//        // Send token to your backend for targeted notifications
//        sendTokenToServer(token)
//
//        // Save token locally (optional)
//        saveTokenLocally(token)
//    }
//
//    private fun showNotification(title: String?, message: String?) {
//        val channelId = "default_channel"
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_n) // Make sure you have this icon
//            .setContentTitle(title ?: "Notification")
//            .setContentText(message ?: "")
//            .setAutoCancel(true)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(pendingIntent)
//
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create notification channel for Android 8.0+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Default Channel",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Default notification channel"
//            }
//            manager.createNotificationChannel(channel)
//        }
//
//        manager.notify(0, builder.build())
//    }
//
//    private fun sendTokenToServer(token: String) {
//        // TODO: Implement your API call to send token to server
//        Log.d("FCM_TOKEN", "Sending token to server: ${token.take(10)}...")
//    }
//
//    private fun saveTokenLocally(token: String) {
//        val prefs = getSharedPreferences("FCM_Prefs", Context.MODE_PRIVATE)
//        prefs.edit().putString("fcm_token", token).apply()
//        Log.d("FCM_TOKEN", "Token saved locally")
//    }
//}
//






//
//package com.uztech.phonelock
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    companion object {
//        private const val TAG = "FCM_PRINT"
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        // ALWAYS PRINT - EVEN IF NOTIFICATION IS NULL
//        printNotificationToLogcat(remoteMessage)
//
//        // Show notification to user
//        showNotificationToUser(remoteMessage)
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//
//        // PRINT TOKEN
//        Log.d("FCM_TOKEN_LOG", "══════════════════════════════════════")
//        Log.d("FCM_TOKEN_LOG", "🆕 NEW FCM TOKEN")
//        Log.d("FCM_TOKEN_LOG", "Token: $token")
//        Log.d("FCM_TOKEN_LOG", "══════════════════════════════════════")
//
//        // Save locally
//        saveTokenLocally(token)
//    }
//
//    // ==============================================
//    // ALWAYS PRINT NOTIFICATION TO LOGCAT
//    // ==============================================
//
//    private fun printNotificationToLogcat(remoteMessage: RemoteMessage) {
//        // PRINT IMMEDIATELY WHEN NOTIFICATION COMES
//        Log.d("FCM_RECEIVED", "══════════════════════════════════════")
//        Log.d("FCM_RECEIVED", "📨 FCM MESSAGE ARRIVED 📨")
//        Log.d("FCM_RECEIVED", "══════════════════════════════════════")
//
//        // Always print basic info
//        Log.d("FCM_RECEIVED", "⏰ Time: ${System.currentTimeMillis()}")
//        Log.d("FCM_RECEIVED", "📱 From: ${remoteMessage.from ?: "Unknown"}")
//        Log.d("FCM_RECEIVED", "🆔 Message ID: ${remoteMessage.messageId ?: "N/A"}")
//
//        // Print notification payload if exists
//        remoteMessage.notification?.let { notification ->
//            Log.d("FCM_RECEIVED", "📢 Notification Payload:")
//            Log.d("FCM_RECEIVED", "   ├── Title: ${notification.title ?: "No Title"}")
//            Log.d("FCM_RECEIVED", "   ├── Body: ${notification.body ?: "No Body"}")
//            Log.d("FCM_RECEIVED", "   ├── Icon: ${notification.icon ?: "Default"}")
//            Log.d("FCM_RECEIVED", "   └── Sound: ${notification.sound ?: "Default"}")
//        } ?: run {
//            Log.d("FCM_RECEIVED", "📢 Notification Payload: NULL")
//        }
//
//        // Print data payload if exists
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.d("FCM_RECEIVED", "📊 Data Payload:")
//            remoteMessage.data.forEach { (key, value) ->
//                Log.d("FCM_RECEIVED", "   ├── $key = $value")
//            }
//        } else {
//            Log.d("FCM_RECEIVED", "📊 Data Payload: EMPTY")
//        }
//
//        Log.d("FCM_RECEIVED", "══════════════════════════════════════")
//
//        // Also print simplified version for quick viewing
//        println("\n" + "=".repeat(50))
//        println("🔥 NOTIFICATION RECEIVED AT: ${System.currentTimeMillis()}")
//        println("Title: ${remoteMessage.notification?.title ?: "No Title"}")
//        println("Body: ${remoteMessage.notification?.body ?: "No Body"}")
//        println("=".repeat(50) + "\n")
//    }
//
//    // ==============================================
//    // SHOW NOTIFICATION TO USER
//    // ==============================================
//
//    private fun showNotificationToUser(remoteMessage: RemoteMessage) {
//        // Get title and body from either notification or data payload
//        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
//        val body = remoteMessage.notification?.body ?:
//        remoteMessage.data["body"] ?:
//        remoteMessage.data["message"] ?:
//        "New notification received"
//
//        // Create notification
//        createNotification(title ?: "Notification", body, remoteMessage.data)
//    }
//
//    private fun createNotification(title: String, body: String, data: Map<String, String>) {
//        try {
//            val channelId = "fcm_channel"
//            val notificationId = System.currentTimeMillis().toInt()
//
//            // Intent to open app
//            val intent = Intent(this, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                putExtra("notification_received", true)
//                putExtra("notification_title", title)
//                putExtra("notification_body", body)
//                // Add all data
//                data.forEach { (key, value) ->
//                    putExtra("data_$key", value)
//                }
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                this, notificationId, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            // Build notification
//            val builder = NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.ic_n) // Make sure you have this
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
//
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            // Create channel for Android 8.0+
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    channelId,
//                    "FCM Notifications",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = "Firebase Cloud Messaging notifications"
//                    enableVibration(true)
//                    vibrationPattern = longArrayOf(0, 500, 300, 500)
//                }
//                manager.createNotificationChannel(channel)
//            }
//
//            manager.notify(notificationId, builder.build())
//
//            Log.d("NOTIFICATION_SHOWN", "✅ Notification shown: $title")
//
//        } catch (e: Exception) {
//            Log.e("NOTIFICATION_ERROR", "❌ Failed to show notification: ${e.message}")
//        }
//    }
//
//    private fun saveTokenLocally(token: String) {
//        try {
//            val prefs = getSharedPreferences("FCM_Prefs", Context.MODE_PRIVATE)
//            prefs.edit().apply {
//                putString("fcm_token", token)
//                putLong("last_token_time", System.currentTimeMillis())
//                apply()
//            }
//            Log.d("TOKEN_SAVED", "✅ Token saved locally")
//        } catch (e: Exception) {
//            Log.e("TOKEN_ERROR", "❌ Failed to save token: ${e.message}")
//        }
//    }
//}




//package com.uztech.phonelock
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import java.io.File
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    companion object {
//        private const val TAG = "FCM_LOGCAT"
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        // CRITICAL: ALWAYS PRINT - EVEN WHEN APP IS CLOSED
//        printNotificationImmediately(remoteMessage)
//
//        // Process notification
//        processIncomingNotification(remoteMessage)
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//
//        // PRINT TOKEN EVEN WHEN APP CLOSED
//        Log.d(TAG, "════════════════════════════════════════════════")
//        Log.d(TAG, "🔥 FCM TOKEN (APP MAY BE CLOSED)")
//        Log.d(TAG, "Token: $token")
//        Log.d(TAG, "════════════════════════════════════════════════")
//
//        saveTokenForLater(token)
//    }
//
//    // ==============================================
//    // FORCE PRINT EVEN WHEN APP CLOSED
//    // ==============================================
//
//    private fun printNotificationImmediately(remoteMessage: RemoteMessage) {
//        // USE Log.wtf FOR HIGHEST PRIORITY - WORKS EVEN WHEN APP CLOSED
//        Log.wtf(TAG, "🎯🎯🎯 FCM RECEIVED WHILE APP CLOSED 🎯🎯🎯")
//        Log.wtf(TAG, "Time: ${System.currentTimeMillis()}")
//
//        // Always print basic info
//        Log.d(TAG, "════════════════════════════════════════════════")
//        Log.d(TAG, "📨 BACKGROUND FCM NOTIFICATION")
//        Log.d(TAG, "════════════════════════════════════════════════")
//
//        // Notification payload
//        remoteMessage.notification?.let { notification ->
//            Log.d(TAG, "📢 TITLE: ${notification.title ?: "NO_TITLE"}")
//            Log.d(TAG, "📢 BODY: ${notification.body ?: "NO_BODY"}")
//        }
//
//        // Data payload
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.d(TAG, "📊 DATA: ${remoteMessage.data}")
//        }
//
//        Log.d(TAG, "📱 FROM: ${remoteMessage.from ?: "UNKNOWN"}")
//        Log.d(TAG, "════════════════════════════════════════════════")
//
//        // Also write to file for extra safety
//        writeToFile("FCM received at ${System.currentTimeMillis()}: ${remoteMessage.notification?.title}")
//    }
//
//    private fun processIncomingNotification(remoteMessage: RemoteMessage) {
//        val title = remoteMessage.notification?.title ?: "Notification"
//        val body = remoteMessage.notification?.body ?: "New message"
//
//        // Show notification to user
//        showSystemNotification(title.toString(), body.toString())
//
//        // Start app if needed
//        startAppIfClosed()
//    }
//
//    private fun showSystemNotification(title: String, body: String) {
//        try {
//            val channelId = "fcm_background_channel"
//            val notificationId = System.currentTimeMillis().toInt()
//
//            // Intent to open app
//            val intent = Intent(this, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                putExtra("fcm_background", true)
//                putExtra("notification_title", title)
//                putExtra("notification_body", body)
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                this,
//                notificationId,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            val builder = NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(android.R.drawable.ic_dialog_info)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)
//                .setVibrate(longArrayOf(0, 500, 300, 500))
//
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    channelId,
//                    "FCM Background",
//                    NotificationManager.IMPORTANCE_HIGH
//                )
//                manager.createNotificationChannel(channel)
//            }
//
//            manager.notify(notificationId, builder.build())
//
//            Log.d(TAG, "✅ Background notification shown")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Failed to show background notification")
//        }
//    }
//
//    private fun startAppIfClosed() {
//        try {
//            // Start MainActivity to ensure logs are captured
//            val intent = Intent(this, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("started_by_fcm", true)
//            }
//            startActivity(intent)
//
//            Log.d(TAG, "🚀 App started from background FCM")
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Could not start app from background")
//        }
//    }
//
//    private fun saveTokenForLater(token: String) {
//        try {
//            val prefs = getSharedPreferences("FCM_BACKGROUND", Context.MODE_PRIVATE)
//            prefs.edit().apply {
//                putString("token", token)
//                putLong("timestamp", System.currentTimeMillis())
//                apply()
//            }
//
//            // Also write to file
//            writeToFile("Token updated: ${token.take(10)}...")
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Failed to save token in background")
//        }
//    }
//
//    private fun writeToFile(text: String) {
//        try {
//            val file = File(filesDir, "fcm_logs.txt")
//            file.appendText("${System.currentTimeMillis()}: $text\n")
//        } catch (e: Exception) {
//            // Ignore file errors
//        }
//    }
//}


//
//package com.uztech.phonelock
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    companion object {
//        private const val TAG = "FCM_LOGCAT"
//        private const val FCM_LOG_FILE = "fcm_debug_logs.txt"
//    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//
//        // ==============================================
//        // 1. ALWAYS PRINT TO LOGCAT - NO MATTER WHAT!
//        // ==============================================
//        printFCMNotificationToLogcat(remoteMessage)
//
//        // ==============================================
//        // 2. PROCESS NOTIFICATION
//        // ==============================================
//        processIncomingNotification(remoteMessage)
//    }
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//
//        // PRINT TOKEN TO LOGCAT
//        printTokenToLogcat(token)
//
//        // Save token for later use
//        saveTokenForLater(token)
//    }
//
//    // ==============================================
//    // CORE FUNCTION: PRINT FCM TO LOGCAT
//    // ==============================================
//
//    private fun printFCMNotificationToLogcat(remoteMessage: RemoteMessage) {
//        try {
//            // Get current timestamp
//            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
//                .format(Date(System.currentTimeMillis()))
//
//            // ============ SEPARATOR ============
//            Log.wtf(TAG, "🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥")
//
//            // ============ MAIN HEADER ============
//            Log.wtf(TAG, "🎯 FCM NOTIFICATION RECEIVED")
//            Log.wtf(TAG, "🕐 Time: $timestamp (${System.currentTimeMillis()})")
//            Log.wtf(TAG, "🔔 App State: ${getAppState()}")
//
//            // ============ FROM ============
//            Log.wtf(TAG, "📤 From: ${remoteMessage.from ?: "UNKNOWN_SENDER"}")
//
//            // ============ NOTIFICATION PAYLOAD ============
//            remoteMessage.notification?.let { notification ->
//                Log.wtf(TAG, "════════════════ NOTIFICATION ════════════════")
//                Log.wtf(TAG, "📢 Title: ${notification.title ?: "[NO_TITLE]"}")
//                Log.wtf(TAG, "📝 Body: ${notification.body ?: "[NO_BODY]"}")
//                Log.wtf(TAG, "🏷️ Tag: ${notification.tag ?: "[NO_TAG]"}")
//                Log.wtf(TAG, "🎨 Color: ${notification.color ?: "[NO_COLOR]"}")
//                Log.wtf(TAG, "🔊 Sound: ${notification.sound ?: "default"}")
//            } ?: run {
//                Log.wtf(TAG, "⚠️ No notification payload")
//            }
//
//            // ============ DATA PAYLOAD ============
//            if (remoteMessage.data.isNotEmpty()) {
//                Log.wtf(TAG, "══════════════════ DATA ══════════════════")
//                for ((key, value) in remoteMessage.data) {
//                    Log.wtf(TAG, "📊 $key = $value")
//                }
//            } else {
//                Log.wtf(TAG, "⚠️ No data payload")
//            }
//
//            // ============ MESSAGE ID ============
//            Log.wtf(TAG, "════════════════ MESSAGE INFO ════════════════")
//            Log.wtf(TAG, "🆔 Message ID: ${remoteMessage.messageId ?: "[NO_ID]"}")
//            Log.wtf(TAG, "📅 Sent Time: ${remoteMessage.sentTime ?: "[NO_TIME]"}")
//            Log.wtf(TAG, "⏳ TTL: ${remoteMessage.ttl ?: "[NO_TTL]"}")
//
//            // ============ COLLAPSE KEY ============
//            Log.wtf(TAG, "🗝️ Collapse Key: ${remoteMessage.collapseKey ?: "[NO_COLLAPSE_KEY]"}")
//
//            // ============ FINAL SEPARATOR ============
//            Log.wtf(TAG, "🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥\n")
//
//            // ==============================================
//            // ALSO WRITE TO FILE FOR PERMANENT STORAGE
//            // ==============================================
//            writeToFile(remoteMessage)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ ERROR printing FCM to logcat: ${e.message}")
//        }
//    }
//
//    // ==============================================
//    // PRINT TOKEN TO LOGCAT
//    // ==============================================
//
//    private fun printTokenToLogcat(token: String) {
//        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
//            .format(Date(System.currentTimeMillis()))
//
//        Log.wtf(TAG, "════════════════════════════════════════════════")
//        Log.wtf(TAG, "🎯🎯🎯 NEW FCM TOKEN GENERATED 🎯🎯🎯")
//        Log.wtf(TAG, "🕐 Time: $timestamp")
//        Log.wtf(TAG, "════════════════════════════════════════════════")
//        Log.wtf(TAG, "Token: $token")
//        Log.wtf(TAG, "Length: ${token.length} characters")
//        Log.wtf(TAG, "════════════════════════════════════════════════")
//
//        // Also print in easy-to-copy format
//        println("\n" + "=".repeat(80))
//        println("📱 COPY THIS FCM TOKEN:")
//        println(token)
//        println("=".repeat(80) + "\n")
//    }
//
//    // ==============================================
//    // HELPER FUNCTIONS
//    // ==============================================
//
//    private fun getAppState(): String {
//        return try {
//            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
//            val runningAppProcesses = activityManager.runningAppProcesses
//
//            val isForeground = runningAppProcesses?.any {
//                it.processName == packageName && it.importance ==
//                        android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
//            } ?: false
//
//            if (isForeground) "APP IN FOREGROUND" else "APP IN BACKGROUND/CLOSED"
//        } catch (e: Exception) {
//            "UNKNOWN STATE"
//        }
//    }
//
//    private fun writeToFile(remoteMessage: RemoteMessage) {
//        try {
//            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
//                .format(Date(System.currentTimeMillis()))
//
//            val file = File(filesDir, FCM_LOG_FILE)
//
//            val logEntry = """
//                |========================================
//                |Time: $timestamp
//                |From: ${remoteMessage.from ?: "unknown"}
//                |Title: ${remoteMessage.notification?.title ?: "no title"}
//                |Body: ${remoteMessage.notification?.body ?: "no body"}
//                |Data: ${remoteMessage.data}
//                |========================================
//
//                """.trimMargin()
//
//            file.appendText(logEntry)
//
//            Log.d(TAG, "💾 Log saved to file: ${file.absolutePath}")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Failed to write to file: ${e.message}")
//        }
//    }
//
//    // ==============================================
//    // NOTIFICATION PROCESSING
//    // ==============================================
//
//    private fun processIncomingNotification(remoteMessage: RemoteMessage) {
//
//        val title = remoteMessage.notification?.title ?: "PhoneLock Notification"
//        val body = remoteMessage.notification?.body ?: "New message received"
//
//        // 1. Show system notification
//        showSystemNotification(title.toString(), body.toString())
//
//        // 2. Extract and handle data payload
//        handleDataPayload(remoteMessage.data)
//
//        // 3. Log action
//        Log.d(TAG, "✅ Notification processed: 66654757437 3476 t783468643865 836 45834854836 $body")
//
//        if(body==""){
//
//        }
//        else if(body==""){
//
//        }
//    }
//
//    private fun showSystemNotification(title: String, body: String) {
//        try {
//            val channelId = "fcm_background_channel"
//            val notificationId = System.currentTimeMillis().toInt()
//
//            // Intent to open MainActivity with notification data
//            val intent = Intent(this, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                putExtra("fcm_notification_received", true)
//                putExtra("notification_title", title)
//                putExtra("notification_body", body)
//                putExtra("notification_time", System.currentTimeMillis())
//            }
//
//            val pendingIntent = PendingIntent.getActivity(
//                this,
//                notificationId,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            // Build notification
//            val builder = NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.ic_n) // Make sure this exists
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_MAX) // Highest priority
//                .setCategory(NotificationCompat.CATEGORY_ALARM)
//                .setContentIntent(pendingIntent)
//                .setVibrate(longArrayOf(0, 500, 300, 500, 300, 500)) // Long vibration
//                .setLights(0xFF5722, 1000, 1000) // Orange LED
//                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
//
//            // Create notification channel for Android O+
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    channelId,
//                    "FCM Background Notifications",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description = "Shows when FCM messages are received"
//                    enableVibration(true)
//                    vibrationPattern = longArrayOf(0, 500, 300, 500)
//                    setShowBadge(true)
//                }
//                manager.createNotificationChannel(channel)
//            }
//
//            // Show notification
//            manager.notify(notificationId, builder.build())
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Failed to show system notification: ${e.message}")
//        }
//    }
//
//    private fun handleDataPayload(data: Map<String, String>) {
//        if (data.isNotEmpty()) {
//            Log.d(TAG, "📦 Processing data payload:")
//
//            // Check for specific commands
//            when (data["action"]) {
//                "lock_device" -> {
//                    Log.d(TAG, "🔒 Lock device command received")
//                    // Implement device locking here
//                }
//                "lock_touch" -> {
//                    Log.d(TAG, "🖐️ Lock touch command received")
//                    // Implement touch locking here
//                }
//                "get_status" -> {
//                    Log.d(TAG, "📊 Status request received")
//                    // Implement status reporting here
//                }
//            }
//
//            // Log all data
//            data.forEach { (key, value) ->
//                Log.d(TAG, "   $key = $value")
//            }
//        }
//    }
//
//    private fun saveTokenForLater(token: String) {
//        try {
//            val prefs = getSharedPreferences("FCM_TOKENS", Context.MODE_PRIVATE)
//            prefs.edit().apply {
//                putString("latest_token", token)
//                putLong("token_timestamp", System.currentTimeMillis())
//                putInt("token_count", prefs.getInt("token_count", 0) + 1)
//                apply()
//            }
//
//            Log.d(TAG, "✅ Token saved to SharedPreferences")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Failed to save token: ${e.message}")
//        }
//    }
//}





package com.uztech.phonelock

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

        // Acquire wake lock to prevent device sleep
        acquireWakeLock()

        // Get notification data
        val title = remoteMessage.notification?.title ?: "PhoneLock"
        val body = remoteMessage.notification?.body ?: ""

        // Log to Logcat
        logFCMMessage(title, body, remoteMessage)

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
            Log.wtf(TAG, "📊 Data Payload:")
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
                    body.contains("active", ignoreCase = true) -> {
                        putExtra("fcm_action", "lock")
                    }
                    body.contains("inactive", ignoreCase = true) -> {
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
                    body.contains("active", ignoreCase = true) -> {
                        putExtra("fcm_action", "lock")
                    }
                    body.contains("inactive", ignoreCase = true) -> {
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