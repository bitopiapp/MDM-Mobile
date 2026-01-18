package com.uztech.phonelock

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class DeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "DeviceAdminReceiver"
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "✅ Device admin enabled")
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show()

        // Start foreground service
        ForegroundNotificationService.startService(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "❌ Device admin disabled")
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.d(TAG, "Lock task mode entered")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.d(TAG, "Lock task mode exited")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.d(TAG, "✅ Device owner provisioning complete")
        Toast.makeText(context, "Device owner enabled!", Toast.LENGTH_LONG).show()
    }
}