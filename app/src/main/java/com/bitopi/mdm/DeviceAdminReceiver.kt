package com.bitopi.mdm

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
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

        // Read admin extras bundle from QR payload
        val extras: PersistableBundle? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.getParcelableExtra("android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE")
        } else null

        val serverUrl = extras?.getString("server_url")
        val adminId   = extras?.getString("admin_id")

        Log.d(TAG, "QR extras — server_url=$serverUrl  admin_id=$adminId")

        // Persist so MainActivity can use them
        context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .apply {
                serverUrl?.let { putString("server_url", it) }
                adminId?.let   { putString("admin_id", it) }
                apply()
            }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(launchIntent)
    }
}
