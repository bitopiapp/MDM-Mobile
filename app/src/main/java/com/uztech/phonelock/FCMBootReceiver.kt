package com.uztech.phonelock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class FCMBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("FCM_BOOT", "📱 Device rebooted - Starting service")
            ForegroundNotificationService.startService(context)
        }
    }
}