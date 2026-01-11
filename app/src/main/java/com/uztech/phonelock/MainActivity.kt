package com.uztech.phonelock

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
//
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
            val serviceIntent = Intent(this, ScreenMonitorService::class.java)
            startForegroundService(serviceIntent)
        }

        // 🔑 Disable factory reset if this app is device owner
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
        }

        refreshStatus()
    }
}
