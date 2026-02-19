package com.uztech.phonelock

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

class PersistentLockService : Service() {
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
        prefs = getSharedPreferences("PhoneLockPrefs", Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        when (intent?.action) {
            ACTION_START_LOCK, ACTION_RESTORE_LOCK -> {
                startPersistentLock()
            }
            ACTION_STOP_LOCK -> {
                stopPersistentLock()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startPersistentLock() {
        prefs.edit().putBoolean("was_locked_before_reboot", true).apply()

        // সরাসরি MainActivity ওপেন করার লুপ
        val lockRunnable = object : Runnable {
            override fun run() {
                if (prefs.getBoolean("was_locked_before_reboot", false)) {
                    val lockIntent = Intent(this@PersistentLockService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(lockIntent)
                    // প্রতি ২০ সেকেন্ড পর পর চেক করবে অ্যাপ সামনে আছে কি না
                    handler.postDelayed(this, 20000)
                }
            }
        }
        handler.post(lockRunnable)
    }

    private fun stopPersistentLock() {
        prefs.edit().putBoolean("was_locked_before_reboot", false).apply()
        handler.removeCallbacksAndMessages(null)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Lock Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Phone Protected")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}