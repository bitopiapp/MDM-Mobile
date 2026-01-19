package com.uztech.phonelock

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class PersistentLockService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var lockManager: LockManager
    private lateinit var vibrator: Vibrator
    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val TAG = "PersistentLockService"
        const val NOTIFICATION_CHANNEL_ID = "persistent_lock_channel"
        const val NOTIFICATION_ID = 102
        const val ACTION_START_LOCK = "action_start_lock"
        const val ACTION_STOP_LOCK = "action_stop_lock"
        const val ACTION_RESTORE_LOCK = "action_restore_lock"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        lockManager = LockManager(applicationContext, windowManager, vibrator)
        prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")

        // Create notification channel for foreground service
        createNotificationChannel()

        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())

        when (intent?.action) {
            ACTION_START_LOCK -> {
                Log.d(TAG, "Starting persistent lock")
                startPersistentLock()
            }

            ACTION_STOP_LOCK -> {
                Log.d(TAG, "Stopping persistent lock")
                stopPersistentLock()
                stopSelf()
            }

            ACTION_RESTORE_LOCK -> {
                Log.d(TAG, "Restoring lock after reboot")
                restoreLockAfterReboot()
            }
        }

        return START_STICKY
    }

    private fun startPersistentLock() {
        try {
            // Save lock state
            prefs.edit().putBoolean("was_locked_before_reboot", true).apply()

            // Apply lock
            handler.post {
                if (lockManager.lockTouchScreen()) {
                    Log.d(TAG, "Persistent lock applied successfully")

                    // Keep service alive
                    handler.postDelayed({
                        // Check if lock is still active
                        if (prefs.getBoolean("was_locked_before_reboot", false)) {
                            Log.d(TAG, "Keeping lock active...")
                            // Re-apply lock periodically to ensure it stays
                            handler.post { lockManager.lockTouchScreen() }
                        }
                    }, 30000) // Check every 30 seconds
                } else {
                    Log.e(TAG, "Failed to apply persistent lock")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in persistent lock: ${e.message}")
        }
    }

    private fun stopPersistentLock() {
        try {
            // Clear lock state
            prefs.edit().putBoolean("was_locked_before_reboot", false).apply()

            handler.post {
                lockManager.unlockTouchScreen()
                Log.d(TAG, "Persistent lock stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping persistent lock: ${e.message}")
        }
    }

    private fun restoreLockAfterReboot() {
        // Wait a bit for system to stabilize
        handler.postDelayed({
            if (prefs.getBoolean("was_locked_before_reboot", false)) {
                Log.d(TAG, "Restoring lock after reboot delay")
                startPersistentLock()
            }
        }, 5000) // Wait 5 seconds after boot
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Persistent Lock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps touch lock active after reboot"
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("PhoneLock Active")
            .setContentText("Touch lock is being maintained")
            .setSmallIcon(R.drawable.ic_n) // Add your own icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSound(null)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        handler.removeCallbacksAndMessages(null)
    }
}