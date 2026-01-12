//package com.uztech.phonelock
//
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.ComponentActivity
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val status = findViewById<TextView>(R.id.statusText)
//        val enableAdmin = findViewById<Button>(R.id.enableAdminBtn)
//        val startService = findViewById<Button>(R.id.startServiceBtn)
//
//        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
//
//        fun refreshStatus() {
//            status.text = if (dpm.isAdminActive(admin)) getString(R.string.admin_enabled)
//            else getString(R.string.admin_disabled)
//        }
//
//        enableAdmin.setOnClickListener {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
//                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
//                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation))
//            }
//            startActivity(intent)
//        }
//
//        startService.setOnClickListener {
//            startForegroundService(Intent(this, ScreenMonitorService::class.java))
//        }
//
//        refreshStatus()
//    }
//}




//package com.uztech.phonelock
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.os.UserManager
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.ComponentActivity
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val status = findViewById<TextView>(R.id.statusText)
//        val enableAdmin = findViewById<Button>(R.id.enableAdminBtn)
//        val startService = findViewById<Button>(R.id.startServiceBtn)
//
//        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)
//
//        fun refreshStatus() {
//            status.text = if (dpm.isAdminActive(admin)) getString(R.string.admin_enabled)
//            else getString(R.string.admin_disabled)
//        }
//
//        enableAdmin.setOnClickListener {
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
//        startService.setOnClickListener {
//            startForegroundService(Intent(this, ScreenMonitorService::class.java))
//        }
//
//        // 🔑 Disable factory reset if this app is device owner
//        if (dpm.isDeviceOwnerApp(packageName)) {
//            dpm.addUserRestriction(UserManager.DISALLOW_FACTORY_RESET)
//        }
//
//        refreshStatus()
//    }
//}



package com.uztech.phonelock
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val enableAdminBtn = findViewById<Button>(R.id.enableAdminBtn)
        val startServiceBtn = findViewById<Button>(R.id.startServiceBtn)
        val lockNowButton = findViewById<Button>(R.id.disableFactoryResetBtn)
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

        // Request device admin activation
        enableAdminBtn.setOnClickListener {
            print("is clicked ==============----")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_explanation)
                )
            }
            startActivity(intent)
        }

        // Start foreground service
        startServiceBtn.setOnClickListener {
            print("is clicked ==============00000")
            val serviceIntent = Intent(this, ScreenMonitorService::class.java)
            startForegroundService(serviceIntent)

        }

        // Lock Now Button click to instantly lock the device
        lockNowButton.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)

            if (dpm.isAdminActive(admin)) {
                dpm.lockNow()   // 🔒 Locks the phone instantly
            } else {
                println("Device admin not active, please enable it in settings")
            }
        }


        pinChangeNowButton.setOnClickListener {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(this, LockDeviceAdminReceiver::class.java)

            if (dpm.isAdminActive(admin)) {
                // Change the device PIN to "12345"
                val success = dpm.resetPassword("12345", 0)
                if (success) {
                    println("PIN changed successfully to 12345")
                    dpm.lockNow()   // 🔒 Immediately lock with new PIN
                } else {
                    println("Failed to change PIN")
                }
            } else {
                println("Device admin not active, please enable it in settings")
            }
        }



//        lockNowButton.setOnClickListener {
//            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//            if (dpm.isDeviceOwnerApp(packageName)) {
//                // 🔒 This locks the entire device immediately
//                dpm.lockNow()
//                println("Device locked instantly")
//            } else {
//                println("App is not device owner, cannot lock phone")
//            }
//        }




        refreshStatus()
    }
}
