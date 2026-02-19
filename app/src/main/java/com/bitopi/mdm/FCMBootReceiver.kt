package com.bitopi.mdm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class FCMBootReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "FCMBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "FCMBootReceiver triggered with action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Device rebooted, starting FCM services...")

                // Start foreground service
                val serviceIntent = Intent(context, ForegroundNotificationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // Check and restore lock if needed
                val prefs = context.getSharedPreferences(
                    MainActivity.PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)

                if (wasLocked) {
                    Log.d(TAG, "Restoring lock after reboot via FCM receiver")
                  //  PersistentLockService.startService(context, PersistentLockService.ACTION_RESTORE_LOCK)
                }
            }
        }
    }
}
