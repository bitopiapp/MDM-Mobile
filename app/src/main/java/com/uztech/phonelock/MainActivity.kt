//
//
//
//
//package com.uztech.phonelock
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.ComponentActivity
//import com.google.firebase.FirebaseApp
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
//        Log.d("FIREBASE", "Firebase initialized: ${FirebaseApp.getInstance().name}")
//        setContentView(R.layout.activity_main)
//
//        val statusText = findViewById<TextView>(R.id.statusText)
//        val enableAdminBtn = findViewById<Button>(R.id.enableAdminBtn)
//        val startServiceBtn = findViewById<Button>(R.id.startServiceBtn)
//        val lockNowButton = findViewById<Button>(R.id.disableFactoryResetBtn)
//        val pinChangeNowButton = findViewById<Button>(R.id.pinChange)
//
//        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
//
//        fun refreshStatus() {
//            statusText.text = if (dpm.isAdminActive(admin)) {
//                getString(R.string.admin_enabled)
//            } else {
//                getString(R.string.admin_disabled)
//            }
//        }
//
//        // Request device admin activation
//        enableAdminBtn.setOnClickListener {
//            print("is clicked ==============----")
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
//                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
//                putExtra(
//                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                    getString(R.string.admin_explanation)
//                )
//            }
//            startActivity(intent)
//        }
//
//        // Start foreground service
//        startServiceBtn.setOnClickListener {
//            print("is clicked ==============00000")
//            val serviceIntent = Intent(this, ScreenMonitorService::class.java)
//            startForegroundService(serviceIntent)
//
//        }
//
//        // Lock Now Button click to instantly lock the device
//        lockNowButton.setOnClickListener {
//            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//            val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
//
//            if (dpm.isAdminActive(admin)) {
//                dpm.lockNow()   // 🔒 Locks the phone instantly
//            } else {
//                println("Device admin not active, please enable it in settings")
//            }
//        }
//
//
//        pinChangeNowButton.setOnClickListener {
//            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//            val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
//
//            if (dpm.isAdminActive(admin)) {
//                // Change the device PIN to "12345"
//                val success = dpm.resetPassword("12345", 0)
//                if (success) {
//                    println("PIN changed successfully to 12345")
//                    dpm.lockNow()   // 🔒 Immediately lock with new PIN
//                } else {
//                    println("Failed to change PIN")
//                }
//            } else {
//                println("Device admin not active, please enable it in settings")
//            }
//        }
//
//
//
////        lockNowButton.setOnClickListener {
////            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
////            if (dpm.isDeviceOwnerApp(packageName)) {
////                // 🔒 This locks the entire device immediately
////                dpm.lockNow()
////                println("Device locked instantly")
////            } else {
////                println("App is not device owner, cannot lock phone")
////            }
////        }
//
//
//
//
//        refreshStatus()
//    }
//}
//
//
//
//
//
//
//
//
//
//




















package com.uztech.phonelock

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        Log.d("FIREBASE", "Firebase initialized: ${FirebaseApp.getInstance().name}")
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val enableAdminBtn = findViewById<Button>(R.id.enableAdminBtn)
        val startServiceBtn = findViewById<Button>(R.id.startServiceBtn)
        val lockNowButton = findViewById<Button>(R.id.lockMyPhone_btnID)
        val pinChangeNowButton = findViewById<Button>(R.id.pinChange)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)

        fun refreshStatus() {
            statusText.text = if (dpm.isAdminActive(admin)) {
                getString(R.string.admin_enabled)
            } else {
                getString(R.string.admin_disabled)
            }
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        // Ask to ignore battery optimization (very important for FCM reliability)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        // Check if app was opened from FCM notification tap
        intent?.let { incomingIntent ->
            if (incomingIntent.hasExtra("from_remote")) {
                val command = incomingIntent.getStringExtra("command") ?: "unknown"
                Toast.makeText(this, "Remote command received: $command", Toast.LENGTH_LONG).show()
            }
        }

        // Enable device admin button
        enableAdminBtn.setOnClickListener {
            Log.d("ADMIN", "Enable admin clicked")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_explanation)
                )
            }
            startActivity(intent)
        }

        // Start foreground service button
        startServiceBtn.setOnClickListener {
            Log.d("SERVICE", "Start service clicked")
            val serviceIntent = Intent(this, ScreenMonitorService::class.java)
            startForegroundService(serviceIntent)
        }

        // Lock now button
        lockNowButton.setOnClickListener {
            if (dpm.isAdminActive(admin)) {
                dpm.lockNow()
                Toast.makeText(this, "Device locked!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enable device admin first!", Toast.LENGTH_LONG).show()
            }
        }

        // Change PIN button
//        pinChangeNowButton.setOnClickListener {
//            if (dpm.isAdminActive(admin)) {
//                val success = dpm.resetPassword("12345", 0)
//                if (success) {
//                    dpm.lockNow()
//                    Toast.makeText(this, "PIN changed to 12345 & locked!", Toast.LENGTH_LONG).show()
//                } else {
//                    Toast.makeText(this, "Failed to change PIN", Toast.LENGTH_LONG).show()
//                }
//            } else {
//                Toast.makeText(this, "Enable device admin first!", Toast.LENGTH_LONG).show()
//            }
//        }

        refreshStatus()
    }

    // ── Correct permission result handler ────────────────────────────────────────────────

}

