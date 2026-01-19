package com.uztech.phonelock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Boot received with action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Device rebooted, starting lock service...")

                // Check if lock was active before reboot
                val prefs = context.getSharedPreferences(
                    MainActivity.PREFS_NAME,
                    Context.MODE_PRIVATE
                )
                val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)

                if (wasLocked) {
                    Log.d(TAG, "Lock was active before reboot, restoring...")
                    // Start persistent service to restore lock
                    val serviceIntent = Intent(context, PersistentLockService::class.java)
                    serviceIntent.action = PersistentLockService.ACTION_RESTORE_LOCK

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}