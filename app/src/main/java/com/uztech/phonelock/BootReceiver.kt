package com.uztech.phonelock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "📱 Boot receiver triggered: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "✅ Device booted - Starting PhoneLock")

                // Start foreground service
                ForegroundNotificationService.startService(context)

                // Start MainActivity
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("from_boot", true)
                }
                context.startActivity(launchIntent)
            }

            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "📱 App updated - Restarting services")
                ForegroundNotificationService.startService(context)
            }

            Intent.ACTION_REBOOT -> {
                Log.d(TAG, "🔄 Device rebooting")
            }
        }
    }
}