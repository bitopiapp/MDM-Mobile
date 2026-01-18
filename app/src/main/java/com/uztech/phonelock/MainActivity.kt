//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initialize DevicePolicyManager
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//
//        // Initialize views
//        tvStatus = findViewById(R.id.tvStatus)
//        val btnSetDeviceOwner = findViewById<Button>(R.id.btnSetDeviceOwner)
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
//        val btnDisallowAll = findViewById<Button>(R.id.btnDisallowAll)
//
//        // Button click listeners
//        btnSetDeviceOwner.setOnClickListener {
//            setDeviceOwner()
//        }
//
//        btnEnableAdmin.setOnClickListener {
//            enableDeviceAdmin()
//        }
//
//        btnLockNow.setOnClickListener {
//            lockDeviceNow()
//        }
//
//        btnDisallowAll.setOnClickListener {
//            disallowAllFeatures()
//        }
//
//        // Update status - call safely
//        updateStatus()
//    }
//
//    private fun setDeviceOwner() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                try {
//                    // This requires the app to be installed via adb with device owner permission
//                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//                    intent.putExtra(
//                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                        "Setting as device owner for complete control"
//                    )
//                    startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_OWNER)
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        this,
//                        "Cannot set device owner directly. Use adb command:\n" +
//                                "adb shell dpm set-device-owner com.uztech.phonelock/.DeviceAdminReceiver",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            } else {
//                Toast.makeText(this, "Already device owner", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun enableDeviceAdmin() {
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disallowAllFeatures() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            try {
//                // Disable camera
//                devicePolicyManager.setCameraDisabled(componentName, true)
//
//                // Disable various features (requires device owner)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                        // Try to hide Play Store (requires device owner)
//                        try {
//                            devicePolicyManager.setApplicationHidden(
//                                componentName,
//                                "com.uztech.phonelock", // Play Store package
//                                true
//                            )
//                        } catch (e: Exception) {
//                            // Play Store might not exist
//                        }
//
////                        // Apply common restrictions safely
////                        applyUserRestrictionsSafely()
//                    }
//                }
//
//                Toast.makeText(this, "Features restricted", Toast.LENGTH_SHORT).show()
//                updateStatus()
//            } catch (e: SecurityException) {
//                Toast.makeText(
//                    this,
//                    "Need device owner permission for complete control",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
////
////    private fun applyUserRestrictionsSafely() {
////        // Apply only basic restrictions that work on most devices
////        val basicRestrictions = listOf(
////            DevicePolicyManager.DISALLOW_INSTALL_APPS,

////            DevicePolicyManager.DISALLOW_CONFIG_WIFI,
////            DevicePolicyManager.DISALLOW_CONFIG_BLUETOOTH
////        )
////
////        for (restriction in basicRestrictions) {
////            try {
////                devicePolicyManager.addUserRestriction(componentName, restriction)
////            } catch (e: Exception) {
////                // Ignore if restriction can't be applied
////            }
////        }
////    }
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        // Check if admin is active first
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//
//            // Only check camera status if admin is active
//            try {
//                val isCameraDisabled = devicePolicyManager.getCameraDisabled(componentName)
//                if (isCameraDisabled) {
//                    status.append("✓ Camera Disabled\n")
//                } else {
//                    status.append("✗ Camera Enabled\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Camera Status Unknown\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//            status.append("✗ Camera Control Not Available\n")
//        }
//
//        // Check device owner status
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//
//                    // Check Play Store status only if device owner
//                    if (isAdminActive) {
//                        try {
//                            val isPlayStoreHidden = devicePolicyManager.isApplicationHidden(
//                                componentName,
//                                "com.uztech.phonelock"
//                            )
//                            if (isPlayStoreHidden) {
//                                status.append("✓ Play Store Hidden\n")
//                            } else {
//                                status.append("✗ Play Store Visible\n")
//                            }
//                        } catch (e: Exception) {
//                            status.append("? Play Store Status Unknown\n")
//                        }
//                    }
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status Unknown\n")
//            }
//        } else {
//            status.append("✗ Device Owner Not Supported\n")
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled successfully", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set successfully", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//    }
//}

 // .........................................Notification ....................//



//
//
//package com.uztech.phonelock
//
//import android.Manifest
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.UserManager
//import android.util.Log
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val REQUEST_CODE_NOTIFICATION_PERMISSION = 102
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val KEY_ALL_FEATURES_DISABLED = "all_features_disabled"
//        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
//        const val KEY_IS_DEVICE_OWNER = "is_device_owner"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//
//        tvStatus = findViewById(R.id.tvStatus)
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)// Add this button to your layout
//        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
//        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)
//
//        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
//
//
//        disableFactoryReset.setOnClickListener {
//            disableFactoryReset()
//            lockDeviceNow()
//        }
//        enableFactoryReset.setOnClickListener { enableFactoryReset() }
//
//        // Start notification on app start if needed
//        initializeProtection()
//
//        updateStatus()
//    }
//
//    private fun initializeProtection() {
//        // Check if device owner and start notification if needed
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                val notificationEnabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
//                if (notificationEnabled) {
//                    startProtectionNotification()
//                }
//            }
//        }
//    }
//
//    private fun setDeviceOwner() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                try {
//                    // Check for Android 13+ notification permission
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        if (ContextCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.POST_NOTIFICATIONS
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            // Request notification permission
//                            ActivityCompat.requestPermissions(
//                                this,
//                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                                REQUEST_CODE_NOTIFICATION_PERMISSION
//                            )
//                            return
//                        }
//                    }
//
//                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//                    intent.putExtra(
//                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                        "Setting as device owner for complete control"
//                    )
//                    startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_OWNER)
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        this,
//                        "Use adb command:\nadb shell dpm set-device-owner com.uztech.phonelock/.DeviceAdminReceiver",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            } else {
//                Toast.makeText(this, "Already device owner", Toast.LENGTH_SHORT).show()
//                // Start notification immediately if already device owner
//                startProtectionNotification()
//            }
//        }
//    }
//
//    private fun enableDeviceAdmin() {
//        setDeviceOwner()
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        // Apply factory reset prevention
//                        applyFactoryResetRestrictions(true)
//
//                        // Save the state
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//
//                        // Start notification service
//                        startProtectionNotification()
//
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed to disable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        // Remove factory reset restrictions
//                        applyFactoryResetRestrictions(false)
//
//                        // Save the state
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//
//
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed to enable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun startProtectionNotification() {
//        // Check if already running
//        if (!isNotificationServiceRunning()) {
//            val serviceIntent = Intent(this, FactoryResetProtectionServices::class.java)
//
//            try {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(serviceIntent)
//                } else {
//                    startService(serviceIntent)
//                }
//
//                // Save notification state
//                prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, true).apply()
//                Toast.makeText(this, "Protection notification started", Toast.LENGTH_SHORT).show()
//
//            } catch (e: Exception) {
//                Toast.makeText(this, "Failed to start notification: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(this, "Notification already running", Toast.LENGTH_SHORT).show()
//        }
//    }
////
////    private fun stopProtectionNotification() {
////        val serviceIntent = Intent(this, FactoryResetProtectionService::class.java)
////        stopService(serviceIntent)
////
////        // Save notification state
////        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, false).apply()
////        Toast.makeText(this, "Protection notification stopped", Toast.LENGTH_SHORT).show()
////    }
//
//    private fun isNotificationServiceRunning(): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
//        return manager.getRunningServices(Integer.MAX_VALUE)
//            .any { it.service.className == FactoryResetProtectionServices::class.java.name }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                try {
//                    // Use proper UserManager constants
//                    if (disable) {
//                        // Add factory reset restriction
//                        devicePolicyManager.addUserRestriction(componentName,
//                            UserManager.DISALLOW_FACTORY_RESET)
//                    } else {
//                        // Remove factory reset restriction
//                        devicePolicyManager.clearUserRestriction(componentName,
//                            UserManager.DISALLOW_FACTORY_RESET)
//                    }
//                } catch (e: Exception) {
//                    Log.e("FactoryReset", "Error applying restrictions: ${e.message}")
//                    throw e
//                }
//            }
//        }
//    }
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//        val isNotificationEnabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false)
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//
//            if (isFactoryResetDisabled) {
//                status.append("✓ Factory Reset Disabled\n")
//            } else {
//                status.append("✗ Factory Reset Enabled\n")
//            }
//
//            if (isNotificationEnabled) {
//                status.append("✓ Protection Notification Active\n")
//            } else {
//                status.append("✗ Notification Inactive\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status Unknown\n")
//            }
//        } else {
//            status.append("✗ Device Owner Not Supported\n")
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled successfully", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set successfully", Toast.LENGTH_SHORT).show()
//                    // Start notification automatically
//                    startProtectionNotification()
//                    // Save device owner state
//                    prefs.edit().putBoolean(KEY_IS_DEVICE_OWNER, true).apply()
//                } else {
//                    Toast.makeText(this, "Failed to set device owner", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        when (requestCode) {
//            REQUEST_CODE_NOTIFICATION_PERMISSION -> {
//                if (grantResults.isNotEmpty() &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted, proceed with device owner setup
//                    setDeviceOwner()
//                } else {
//                    Toast.makeText(
//                        this,
//                        "Cannot show protection status without notification permission",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//    }
//}
























//-----------------------------Final Vertion ----------------------------------



//
//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.os.Build
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val KEY_ALL_FEATURES_DISABLED = "all_features_disabled"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//
//        tvStatus = findViewById(R.id.tvStatus)
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//
//        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
//        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)
//
//        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
//
//        disableFactoryReset.setOnClickListener {
//            disableFactoryReset()
//            // lock function
//            lockDeviceNow()
//        }
//        enableFactoryReset.setOnClickListener { enableFactoryReset() }
//
//        updateStatus()
//    }
//
//
//
//    private fun enableDeviceAdmin() {
//
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        // Apply factory reset prevention
//                        applyFactoryResetRestrictions(true)
//
//                        // Save the state
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed to disable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        // Remove factory reset restrictions
//                        applyFactoryResetRestrictions(false)
//
//                        // Save the state
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed to enable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        // Factory reset restriction constants
//        val factoryResetRestriction = "no_factory_reset"
//        val safeBootRestriction = "no_safe_boot"
//        val debuggingRestriction = "no_debugging_features"
//        val devSettingsRestriction = "no_development_settings"
//
//        val restrictions = listOf(
//            factoryResetRestriction,
//            safeBootRestriction,
//            debuggingRestriction,
//            devSettingsRestriction
//        )
//
//        for (restriction in restrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Some restrictions might not be supported
//                println("Restriction $restriction not supported: ${e.message}")
//            }
//        }
//
//        // Also control other related restrictions
//        val otherRestrictions = listOf(
//            //  "no_config_wifi",
//            "no_config_bluetooth",
//            "no_config_tethering",
//            "no_share_location"
//        )
//
//        for (restriction in otherRestrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Ignore unsupported restrictions
//            }
//        }
//    }
//
//
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//            // Check factory reset status
//            if (isFactoryResetDisabled) {
//                status.append("✓ Factory Reset Disabled\n")
//            } else {
//                status.append("✗ Factory Reset Enabled\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//            status.append("✗ Camera Control Not Available\n")
//            status.append("✗ Factory Reset Control Not Available\n")
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//
//                    if (isAdminActive) {
//                        try {
//                            val isPlayStoreHidden = devicePolicyManager.isApplicationHidden(
//                                componentName,
//                                "com.android.vending"
//                            )
//                            if (isPlayStoreHidden) {
//                                status.append("✓ Play Store Hidden\n")
//                            } else {
//                                status.append("✗ Play Store Visible\n")
//                            }
//                        } catch (e: Exception) {
//                            status.append("? Play Store Status Unknown\n")
//                        }
//                    }
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status Unknown\n")
//            }
//        } else {
//            status.append("✗ Device Owner Not Supported\n")
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled successfully", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set successfully", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//    }
//}









//
//// touch lock without permission  ---- only single screen -------------------------------------------------------------
//
//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Color
//import android.os.*
//import android.view.*
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//    private lateinit var vibrator: Vibrator
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var isTouchLocked = false
//    private var touchLockStartTime: Long = 0
//    private var lockRunnable: Runnable? = null
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val LOCK_DURATION = 5000L // 5 seconds
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//
//        tvStatus = findViewById(R.id.tvStatus)
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//        val btnLockTouch = findViewById<Button>(R.id.btnLockTouch)
//
//        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
//        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)
//
//        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
//        btnLockTouch.setOnClickListener { lockTouchScreen() }
//
//        disableFactoryReset.setOnClickListener {
//            disableFactoryReset()
//            lockDeviceNow()
//        }
//        enableFactoryReset.setOnClickListener { enableFactoryReset() }
//
//        updateStatus()
//    }
//
//    // ==============================================
//    // TOUCH SCREEN LOCK/UNLOCK WITHOUT PERMISSION
//    // ==============================================
//
//    fun lockTouchScreen() {
//        if (isTouchLocked) {
//            Toast.makeText(this, "Touch already locked. Auto-unlock in ${getRemainingTime()}s", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            // 1. Make window NOT touchable - NO PERMISSION NEEDED
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//            )
//
//            // 2. Add semi-transparent overlay
//            val rootView = findViewById<ViewGroup>(android.R.id.content)
//            val overlayView = View(this).apply {
//                setBackgroundColor(Color.argb(100, 0, 0, 0)) // Semi-transparent
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//                tag = "overlay_view" // Tag for identification
//            }
//            rootView.addView(overlayView)
//
//            // 3. Vibrate for feedback
//            vibratePhone(200)
//
//            // 4. Show message
//            Toast.makeText(this, "Touch locked for 5 seconds", Toast.LENGTH_SHORT).show()
//
//            // 5. Update state
//            isTouchLocked = true
//            touchLockStartTime = System.currentTimeMillis()
//
//            // 6. Schedule auto-unlock
//            lockRunnable = Runnable {
//                unlockTouchScreen()
//                Toast.makeText(this@MainActivity, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//            handler.postDelayed(lockRunnable!!, LOCK_DURATION)
//
//            // 7. Update UI
//            updateStatus()
//
//        } catch (e: Exception) {
//            Toast.makeText(this, "Failed to lock: ${e.message}", Toast.LENGTH_SHORT).show()
//            isTouchLocked = false
//        }
//    }
//
//    private fun unlockTouchScreen() {
//        if (!isTouchLocked) return
//
//        try {
//            // 1. Restore touchability
//            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//            // 2. Remove overlay view
//            val rootView = findViewById<ViewGroup>(android.R.id.content)
//            for (i in 0 until rootView.childCount) {
//                val child = rootView.getChildAt(i)
//                if (child.tag == "overlay_view") {
//                    rootView.removeView(child)
//                    break
//                }
//            }
//
//            // 3. Vibrate for feedback
//            vibratePhone(100)
//
//            // 4. Update state
//            isTouchLocked = false
//
//            // 5. Clean up runnable
//            lockRunnable?.let { handler.removeCallbacks(it) }
//            lockRunnable = null
//
//            // 6. Update UI
//            updateStatus()
//
//        } catch (e: Exception) {
//            // Force reset even on error
//            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//            isTouchLocked = false
//            lockRunnable = null
//            updateStatus()
//        }
//    }
//
//    private fun getRemainingTime(): Long {
//        if (!isTouchLocked) return 0
//        val elapsed = System.currentTimeMillis() - touchLockStartTime
//        return maxOf(0, (LOCK_DURATION - elapsed) / 1000)
//    }
//
//    private fun vibratePhone(duration: Long) {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(duration)
//            }
//        } catch (e: Exception) {
//            // Ignore vibration errors
//        }
//    }
//
//    // ==============================================
//    // DEVICE ADMIN FUNCTIONS
//    // ==============================================
//
//    private fun enableDeviceAdmin() {
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(true)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(false)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        val restrictions = listOf(
//            "no_factory_reset",
//            "no_safe_boot",
//            "no_debugging_features",
//            "no_development_settings"
//        )
//
//        for (restriction in restrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Ignore unsupported
//            }
//        }
//    }
//
//    // ==============================================
//    // STATUS & UI FUNCTIONS
//    // ==============================================
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//
//            if (isFactoryResetDisabled) {
//                status.append("✓ Factory Reset Disabled\n")
//            } else {
//                status.append("✗ Factory Reset Enabled\n")
//            }
//
//            // Touch lock status
//            if (isTouchLocked) {
//                val remaining = getRemainingTime()
//                status.append("⏳ Touch Screen LOCKED\n")
//                status.append("  Auto-unlock in: ${remaining}s\n")
//            } else {
//                status.append("✓ Touch Screen Ready\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//            status.append("✗ Touch Lock Not Available\n")
//        }
//
//        // REMOVED: No overlay permission check needed
//        // if (!checkOverlayPermission()) {
//        //     status.append("⚠ Overlay Permission Needed\n")
//        // }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status\n")
//            }
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    // ==============================================
//    // ACTIVITY LIFECYCLE
//    // ==============================================
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//
//        // Auto-unlock if time has passed while app was in background
//        if (isTouchLocked) {
//            val elapsed = System.currentTimeMillis() - touchLockStartTime
//            if (elapsed >= LOCK_DURATION) {
//                unlockTouchScreen()
//                Toast.makeText(this, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Clean up everything
//        unlockTouchScreen()
//        handler.removeCallbacksAndMessages(null)
//    }
//}


























//
////--------------------------touch lock  screen with permission --------------------
//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Color
//import android.os.*
//import android.provider.Settings
//import android.util.Log
//import android.view.*
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.messaging.FirebaseMessaging
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//    private lateinit var vibrator: Vibrator
//    private lateinit var windowManager: WindowManager
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var isTouchLocked = false
//    private var touchLockStartTime: Long = 0
//    private var lockRunnable: Runnable? = null
//    private var touchBlockerView: View? = null
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val LOCK_DURATION = 10000L // 10 seconds
//        const val OVERLAY_PERMISSION_REQUEST = 102
//
//        // FCM Token keys
//        const val FCM_TOKEN_KEY = "fcm_token_key"
//        const val FCM_TOKEN_SAVED_KEY = "fcm_token_saved"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//        tvStatus = findViewById(R.id.tvStatus)
//
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//        val btnLockTouch = findViewById<Button>(R.id.btnLockTouch)
//        val btnGetFcmToken = findViewById<Button>(R.id.btnGetFcmToken)
//        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
//        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)
//
//        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
//        btnLockTouch.setOnClickListener { lockTouchScreen() }
//        btnGetFcmToken.setOnClickListener { getFCMTokenAndSave() }
//
//        disableFactoryReset.setOnClickListener {
//            disableFactoryReset()
//            lockDeviceNow()
//        }
//        enableFactoryReset.setOnClickListener { enableFactoryReset() }
//
//        // Check for FCM token on app start
//        checkAndGetFCMToken()
//        updateStatus()
//    }
//
//    // ==============================================
//    // FCM TOKEN MANAGEMENT FUNCTIONS
//    // ==============================================
//
//    private fun checkAndGetFCMToken() {
//        // If token not saved previously, get new one
//        if (!isTokenSaved()) {
//            Log.d("FCM", "Token not saved, getting new token")
//            // Don't auto-get, let user click button
//        } else {
//            // Show existing token in status
//            val savedToken = getSavedToken()
//            Log.d("FCM", "Token already saved: ${savedToken?.take(10)}...")
//        }
//    }
//
//    private fun getFCMTokenAndSave() {
//        Log.d("FCM", "Getting FCM token...")
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.exception)
//                Toast.makeText(this, "Failed to get FCM token: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                return@OnCompleteListener
//            }
//
//            // Get new FCM registration token
//            val token = task.result
//            Log.d("FCM_TOKEN", "FCM Token: $token")
//
//            // Save token locally
//            saveTokenLocally(token)
//
//            // Send token to your server
//            sendTokenToServer(token)
//
//            Toast.makeText(
//                this,
//                "FCM token received: ${token.take(10)}...",
//                Toast.LENGTH_LONG
//            ).show()
//
//            updateStatus() // Update status to show token
//        })
//    }
//
//    private fun saveTokenLocally(token: String) {
//        val editor = prefs.edit()
//        editor.putString(FCM_TOKEN_KEY, token)
//        editor.putBoolean(FCM_TOKEN_SAVED_KEY, true)
//        editor.apply()
//
//        Log.d("FCM_TOKEN", "Token saved locally: ${token.take(10)}...")
//    }
//
//    private fun getSavedToken(): String? {
//        return prefs.getString(FCM_TOKEN_KEY, null)
//    }
//
//    private fun isTokenSaved(): Boolean {
//        return prefs.getBoolean(FCM_TOKEN_SAVED_KEY, false)
//    }
//
//    private fun sendTokenToServer(token: String) {
//        // TODO: Implement your API call to send token to server
//
//        // For now, just log it
//        Log.d("API_CALL", "Would send token to server: ${token.take(10)}...")
//
//        // You can implement Retrofit/Volley/OkHttp here
//    }
//
//    // ==============================================
//    // TOUCH SCREEN LOCK/UNLOCK FUNCTIONS
//    // ==============================================
//
//    fun lockTouchScreen() {
//        if (!checkOverlayPermission()) {
//            requestOverlayPermission()
//            return
//        }
//
//        if (isTouchLocked) {
//            Toast.makeText(this, "Touch already locked. Auto-unlock in ${getRemainingTime()}s", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            // 1. Create overlay view that blocks all touch
//            createTouchBlockerOverlay()
//
//            // 2. Vibrate for feedback
//            vibratePhone(200)
//
//            // 3. Show message
//            Toast.makeText(this, "Touch locked for 10 seconds", Toast.LENGTH_SHORT).show()
//
//            // 4. Update state
//            isTouchLocked = true
//            touchLockStartTime = System.currentTimeMillis()
//
//            // 5. Schedule auto-unlock
//            lockRunnable = Runnable {
//                unlockTouchScreen()
//                Toast.makeText(applicationContext, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//            handler.postDelayed(lockRunnable!!, LOCK_DURATION)
//
//            // 6. Update UI
//            updateStatus()
//
//        } catch (e: Exception) {
//            Toast.makeText(this, "Failed to lock: ${e.message}", Toast.LENGTH_SHORT).show()
//            isTouchLocked = false
//        }
//    }
//
//    private fun unlockTouchScreen() {
//        if (!isTouchLocked) return
//
//        try {
//            // 1. Remove overlay view
//            removeTouchBlockerOverlay()
//
//            // 2. Vibrate for feedback
//            vibratePhone(100)
//
//            // 3. Update state
//            isTouchLocked = false
//
//            // 4. Clean up runnable
//            lockRunnable?.let { handler.removeCallbacks(it) }
//            lockRunnable = null
//
//            // 5. Update UI
//            updateStatus()
//
//        } catch (e: Exception) {
//            // Force reset even on error
//            isTouchLocked = false
//            lockRunnable = null
//            updateStatus()
//        }
//    }
//
//    private fun getRemainingTime(): Long {
//        if (!isTouchLocked) return 0
//        val elapsed = System.currentTimeMillis() - touchLockStartTime
//        return maxOf(0, (LOCK_DURATION - elapsed) / 1000)
//    }
//
//    private fun createTouchBlockerOverlay() {
//        // Create a full-screen view that blocks all touch events
//        touchBlockerView = View(this).apply {
//            // Semi-transparent dark overlay
//            setBackgroundColor(Color.argb(136, 0, 0, 0)) // ~50% transparent black
//            isClickable = true
//            isFocusable = true
//
//            // Block all touch events
//            setOnTouchListener { _, _ -> true }
//        }
//
//        // Set layout parameters for overlay
//        val params = WindowManager.LayoutParams().apply {
//            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_PHONE
//            }
//
//            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//
//            width = WindowManager.LayoutParams.MATCH_PARENT
//            height = WindowManager.LayoutParams.MATCH_PARENT
//            format = android.graphics.PixelFormat.TRANSLUCENT
//            gravity = Gravity.START or Gravity.TOP
//
//            // Make sure it's above everything
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//            }
//        }
//
//        // Add overlay to window
//        windowManager.addView(touchBlockerView, params)
//    }
//
//    private fun removeTouchBlockerOverlay() {
//        try {
//            touchBlockerView?.let {
//                if (it.parent != null) {
//                    windowManager.removeView(it)
//                }
//            }
//        } catch (e: Exception) {
//            // Ignore - view might already be removed
//        } finally {
//            touchBlockerView = null
//        }
//    }
//
//    private fun checkOverlayPermission(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Settings.canDrawOverlays(this)
//        } else {
//            true // Always true for Android < 6.0
//        }
//    }
//
//    private fun requestOverlayPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                android.net.Uri.parse("package:$packageName")
//            )
//            try {
//                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
//                Toast.makeText(this, "Please enable 'Display over other apps'", Toast.LENGTH_LONG).show()
//            } catch (e: Exception) {
//                // Fallback to app info
//                val intentFallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                    data = android.net.Uri.parse("package:$packageName")
//                }
//                startActivity(intentFallback)
//            }
//        }
//    }
//
//    private fun vibratePhone(duration: Long) {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(duration)
//            }
//        } catch (e: Exception) {
//            // Ignore vibration errors
//        }
//    }
//
//    // ==============================================
//    // DEVICE ADMIN FUNCTIONS
//    // ==============================================
//
//    private fun enableDeviceAdmin() {
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(true)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(false)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        val restrictions = listOf(
//            "no_factory_reset",
//            "no_safe_boot",
//            "no_debugging_features",
//            "no_development_settings"
//        )
//
//        for (restriction in restrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Ignore unsupported
//            }
//        }
//    }
//
//    // ==============================================
//    // STATUS & UI FUNCTIONS
//    // ==============================================
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//
//            if (isFactoryResetDisabled) {
//                status.append("✓ Factory Reset Disabled\n")
//            } else {
//                status.append("✗ Factory Reset Enabled\n")
//            }
//
//            // Touch lock status
//            if (isTouchLocked) {
//                val remaining = getRemainingTime()
//                status.append("⏳ Touch Screen LOCKED\n")
//                status.append("  Auto-unlock in: ${remaining}s\n")
//            } else {
//                status.append("✓ Touch Screen Ready\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//            status.append("✗ Touch Lock Not Available\n")
//        }
//
//        // Overlay permission status
//        if (!checkOverlayPermission()) {
//            status.append("⚠ Overlay Permission Needed\n")
//        }
//
//        // FCM Token status
//        val token = getSavedToken()
//        if (token != null) {
//            status.append("✓ FCM Token: ${token.take(10)}...\n")
//        } else {
//            status.append("✗ FCM Token Not Available\n")
//        }
//
//        // Device owner status
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status\n")
//            }
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    // ==============================================
//    // ACTIVITY LIFECYCLE
//    // ==============================================
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            OVERLAY_PERMISSION_REQUEST -> {
//                if (checkOverlayPermission()) {
//                    Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
//                    // Auto-proceed to lock after permission granted
//                    handler.postDelayed({
//                        lockTouchScreen()
//                    }, 500)
//                } else {
//                    Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//
//        // Auto-unlock if time has passed while app was in background
//        if (isTouchLocked) {
//            val elapsed = System.currentTimeMillis() - touchLockStartTime
//            if (elapsed >= LOCK_DURATION) {
//                unlockTouchScreen()
//                Toast.makeText(this, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // Clean up everything
//        unlockTouchScreen()
//        handler.removeCallbacksAndMessages(null)
//    }
//}




//
//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Color
//import android.os.*
//import android.provider.Settings
//import android.util.Log
//import android.view.*
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.messaging.FirebaseMessaging
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//    private lateinit var vibrator: Vibrator
//    private lateinit var windowManager: WindowManager
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var isTouchLocked = false
//    private var touchLockStartTime: Long = 0
//    private var lockRunnable: Runnable? = null
//    private var touchBlockerView: View? = null
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val LOCK_DURATION = 10000L
//        const val OVERLAY_PERMISSION_REQUEST = 102
//
//        // FCM Keys
//        const val FCM_TOKEN_KEY = "fcm_token_key"
//        const val FCM_TOKEN_SAVED_KEY = "fcm_token_saved"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // INITIAL LOG
//        Log.d("APP_START", "══════════════════════════════════════")
//        Log.d("APP_START", "📱 PhoneLock App Started")
//        Log.d("APP_START", "Package: com.uztech.phonelock")
//        Log.d("APP_START", "Time: ${System.currentTimeMillis()}")
//        Log.d("APP_START", "══════════════════════════════════════")
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//        tvStatus = findViewById(R.id.tvStatus)
//
//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//        val btnLockTouch = findViewById<Button>(R.id.btnLockTouch)
//        val btnGetFcmToken = findViewById<Button>(R.id.btnGetFcmToken)
//        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
//        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)
//
//        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
//        btnLockTouch.setOnClickListener { lockTouchScreen() }
//        btnGetFcmToken.setOnClickListener {
//            Log.d("USER_ACTION", "User clicked: Get FCM Token")
//            getFCMTokenAndSave()
//        }
//
//        disableFactoryReset.setOnClickListener {
//            disableFactoryReset()
//            lockDeviceNow()
//        }
//        enableFactoryReset.setOnClickListener { enableFactoryReset() }
//
//        // AUTO CHECKS
//        checkFCMStatusOnStart()
//        checkForNotifications()
//        updateStatus()
//    }
//
//    // ==============================================
//    // AUTO NOTIFICATION CHECKING
//    // ==============================================
//
//    private fun checkFCMStatusOnStart() {
//        Log.d("FCM_CHECK", "Checking FCM status on app start...")
//
//        // Check if token exists
//        val token = getSavedToken()
//        if (token != null) {
//            Log.d("FCM_CHECK", "✅ Token exists: ${token.take(15)}...")
//        } else {
//            Log.d("FCM_CHECK", "❌ No FCM token found")
//        }
//
//        // Check if service is registered
//        Log.d("FCM_CHECK", "MyFirebaseMessagingService should be listening...")
//    }
//
//    private fun checkForNotifications() {
//        // Check if app was opened from notification
//        if (intent?.hasExtra("notification_received") == true) {
//            val title = intent.getStringExtra("notification_title")
//            val body = intent.getStringExtra("notification_body")
//
//            Log.d("NOTIFICATION_OPEN", "══════════════════════════════════════")
//            Log.d("NOTIFICATION_OPEN", "📱 App opened from FCM notification!")
//            Log.d("NOTIFICATION_OPEN", "Title: $title")
//            Log.d("NOTIFICATION_OPEN", "Body: $body")
//            Log.d("NOTIFICATION_OPEN", "══════════════════════════════════════")
//
//            Toast.makeText(this, "From notification: $title", Toast.LENGTH_LONG).show()
//        }
//
//        // Log current time for reference
//        Log.d("NOTIFICATION_CHECK", "App ready to receive notifications at: ${System.currentTimeMillis()}")
//    }
//
//    // ==============================================
//    // FCM TOKEN FUNCTIONS
//    // ==============================================
//
//    private fun getFCMTokenAndSave() {
//        Log.d("FCM_ACTION", "══════════════════════════════════════")
//        Log.d("FCM_ACTION", "🔄 Requesting FCM token...")
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                val error = task.exception?.message ?: "Unknown error"
//                Log.e("FCM_ERROR", "❌ Token fetch failed: $error")
//
//                // Detailed error logging
//                when {
//                    error.contains("AUTHENTICATION_FAILED") -> {
//                        Log.e("FCM_ERROR", "🔥 Firebase authentication failed!")
//                        Log.e("FCM_ERROR", "Possible causes:")
//                        Log.e("FCM_ERROR", "1. google-services.json missing/wrong")
//                        Log.e("FCM_ERROR", "2. SHA-1 fingerprint not added")
//                        Log.e("FCM_ERROR", "3. Package name mismatch")
//                    }
//                    error.contains("SERVICE_NOT_AVAILABLE") -> {
//                        Log.e("FCM_ERROR", "📡 Google Play Services not available")
//                    }
//                    error.contains("NETWORK") -> {
//                        Log.e("FCM_ERROR", "🌐 Network error - check internet")
//                    }
//                    else -> {
//                        Log.e("FCM_ERROR", "Unknown error: $error")
//                    }
//                }
//
//                Toast.makeText(this, "FCM Error: ${error.take(50)}...", Toast.LENGTH_LONG).show()
//                return@OnCompleteListener
//            }
//
//            val token = task.result
//            Log.d("FCM_SUCCESS", "══════════════════════════════════════")
//            Log.d("FCM_SUCCESS", "✅ FCM TOKEN RECEIVED!")
//            Log.d("FCM_SUCCESS", "Full Token: $token")
//            Log.d("FCM_SUCCESS", "First 20 chars: ${token.take(20)}...")
//            Log.d("FCM_SUCCESS", "══════════════════════════════════════")
//
//            // Print to console for easy copying
//            println("\n" + "=".repeat(60))
//            println("🔥🔥🔥 COPY THIS FCM TOKEN FOR TESTING 🔥🔥🔥")
//            println(token)
//            println("=".repeat(60) + "\n")
//
//            saveTokenLocally(token)
//
//            Toast.makeText(this, "Token: ${token.take(10)}...", Toast.LENGTH_LONG).show()
//            updateStatus()
//        })
//    }
//
//    private fun saveTokenLocally(token: String) {
//        val editor = prefs.edit()
//        editor.putString(FCM_TOKEN_KEY, token)
//        editor.putBoolean(FCM_TOKEN_SAVED_KEY, true)
//        editor.apply()
//
//        Log.d("TOKEN_SAVE", "✅ Token saved to SharedPreferences")
//    }
//
//    private fun getSavedToken(): String? {
//        return prefs.getString(FCM_TOKEN_KEY, null)
//    }
//
//    private fun isTokenSaved(): Boolean {
//        return prefs.getBoolean(FCM_TOKEN_SAVED_KEY, false)
//    }
//
//    // ==============================================
//    // TOUCH LOCK FUNCTIONS (UNCHANGED)
//    // ==============================================
//
//    fun lockTouchScreen() {
//        if (!checkOverlayPermission()) {
//            requestOverlayPermission()
//            return
//        }
//
//        if (isTouchLocked) {
//            Toast.makeText(this, "Touch already locked. Auto-unlock in ${getRemainingTime()}s", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            createTouchBlockerOverlay()
//            vibratePhone(200)
//            Toast.makeText(this, "Touch locked for 10 seconds", Toast.LENGTH_SHORT).show()
//
//            isTouchLocked = true
//            touchLockStartTime = System.currentTimeMillis()
//
//            lockRunnable = Runnable {
//                unlockTouchScreen()
//                Toast.makeText(applicationContext, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//            handler.postDelayed(lockRunnable!!, LOCK_DURATION)
//
//            updateStatus()
//
//        } catch (e: Exception) {
//            Toast.makeText(this, "Failed to lock: ${e.message}", Toast.LENGTH_SHORT).show()
//            isTouchLocked = false
//        }
//    }
//
//    private fun unlockTouchScreen() {
//        if (!isTouchLocked) return
//
//        try {
//            removeTouchBlockerOverlay()
//            vibratePhone(100)
//            isTouchLocked = false
//            lockRunnable?.let { handler.removeCallbacks(it) }
//            lockRunnable = null
//            updateStatus()
//
//        } catch (e: Exception) {
//            isTouchLocked = false
//            lockRunnable = null
//            updateStatus()
//        }
//    }
//
//    private fun getRemainingTime(): Long {
//        if (!isTouchLocked) return 0
//        val elapsed = System.currentTimeMillis() - touchLockStartTime
//        return maxOf(0, (LOCK_DURATION - elapsed) / 1000)
//    }
//
//    private fun createTouchBlockerOverlay() {
//        touchBlockerView = View(this).apply {
//            setBackgroundColor(Color.argb(136, 0, 0, 0))
//            isClickable = true
//            isFocusable = true
//            setOnTouchListener { _, _ -> true }
//        }
//
//        val params = WindowManager.LayoutParams().apply {
//            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_PHONE
//            }
//
//            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//
//            width = WindowManager.LayoutParams.MATCH_PARENT
//            height = WindowManager.LayoutParams.MATCH_PARENT
//            format = android.graphics.PixelFormat.TRANSLUCENT
//            gravity = Gravity.START or Gravity.TOP
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//            }
//        }
//
//        windowManager.addView(touchBlockerView, params)
//    }
//
//    private fun removeTouchBlockerOverlay() {
//        try {
//            touchBlockerView?.let {
//                if (it.parent != null) {
//                    windowManager.removeView(it)
//                }
//            }
//        } catch (e: Exception) {
//            // Ignore
//        } finally {
//            touchBlockerView = null
//        }
//    }
//
//    private fun checkOverlayPermission(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Settings.canDrawOverlays(this)
//        } else {
//            true
//        }
//    }
//
//    private fun requestOverlayPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                android.net.Uri.parse("package:$packageName")
//            )
//            try {
//                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
//                Toast.makeText(this, "Please enable 'Display over other apps'", Toast.LENGTH_LONG).show()
//            } catch (e: Exception) {
//                val intentFallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                    data = android.net.Uri.parse("package:$packageName")
//                }
//                startActivity(intentFallback)
//            }
//        }
//    }
//
//    private fun vibratePhone(duration: Long) {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(duration)
//            }
//        } catch (e: Exception) {
//            // Ignore
//        }
//    }
//
//    // ==============================================
//    // DEVICE ADMIN FUNCTIONS (UNCHANGED)
//    // ==============================================
//
//    private fun enableDeviceAdmin() {
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Device admin permission is required to lock the device and control settings"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(true)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(false)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        val restrictions = listOf(
//            "no_factory_reset",
//            "no_safe_boot",
//            "no_debugging_features",
//            "no_development_settings"
//        )
//
//        for (restriction in restrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Ignore
//            }
//        }
//    }
//
//    // ==============================================
//    // STATUS UPDATE
//    // ==============================================
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin Enabled\n")
//
//            if (isFactoryResetDisabled) {
//                status.append("✓ Factory Reset Disabled\n")
//            } else {
//                status.append("✗ Factory Reset Enabled\n")
//            }
//
//            if (isTouchLocked) {
//                val remaining = getRemainingTime()
//                status.append("⏳ Touch Screen LOCKED\n")
//                status.append("  Auto-unlock in: ${remaining}s\n")
//            } else {
//                status.append("✓ Touch Screen Ready\n")
//            }
//        } else {
//            status.append("✗ Device Admin Disabled\n")
//            status.append("✗ Touch Lock Not Available\n")
//        }
//
//        if (!checkOverlayPermission()) {
//            status.append("⚠ Overlay Permission Needed\n")
//        }
//
//        val token = getSavedToken()
//        if (token != null) {
//            status.append("✓ FCM Token: ${token.take(10)}...\n")
//        } else {
//            status.append("✗ FCM Token Not Available\n")
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    status.append("✓ Device Owner\n")
//                } else {
//                    status.append("✗ Not Device Owner\n")
//                }
//            } catch (e: SecurityException) {
//                status.append("? Device Owner Status\n")
//            }
//        }
//
//        tvStatus.text = status.toString()
//    }
//
//    // ==============================================
//    // ACTIVITY LIFECYCLE
//    // ==============================================
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            REQUEST_CODE_SET_DEVICE_OWNER -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device owner set", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//
//            OVERLAY_PERMISSION_REQUEST -> {
//                if (checkOverlayPermission()) {
//                    Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
//                    handler.postDelayed({
//                        lockTouchScreen()
//                    }, 500)
//                } else {
//                    Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//
//        if (isTouchLocked) {
//            val elapsed = System.currentTimeMillis() - touchLockStartTime
//            if (elapsed >= LOCK_DURATION) {
//                unlockTouchScreen()
//                Toast.makeText(this, "Touch auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unlockTouchScreen()
//        handler.removeCallbacksAndMessages(null)
//    }
//}


//package com.uztech.phonelock
//
//import android.app.Activity
//import android.app.ActivityManager
//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.graphics.Color
//import android.os.*
//import android.provider.Settings
//import android.util.Log
//import android.view.*
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.tasks.OnCompleteListener
//import com.google.firebase.messaging.FirebaseMessaging
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var devicePolicyManager: DevicePolicyManager
//    private lateinit var componentName: ComponentName
//    private lateinit var tvStatus: TextView
//    private lateinit var prefs: SharedPreferences
//    private lateinit var vibrator: Vibrator
//    private lateinit var windowManager: WindowManager
//
//    private val handler = Handler(Looper.getMainLooper())
//    private var isTouchLocked = false
//    private var touchLockStartTime: Long = 0
//    private var lockRunnable: Runnable? = null
//    private var touchBlockerView: View? = null
//
//    companion object {
//        const val REQUEST_CODE_ENABLE_ADMIN = 100
//        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
//        const val PREFS_NAME = "PhoneLockPrefs"
//        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
//        const val LOCK_DURATION = 10000L
//        const val OVERLAY_PERMISSION_REQUEST = 102
//
//        // FCM Log tag
//        private const val FCM_LOG_TAG = "FCM_MAIN"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initial log
//        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//        Log.d(FCM_LOG_TAG, "📱 MainActivity Started")
//        Log.d(FCM_LOG_TAG, "Time: ${System.currentTimeMillis()}")
//        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//
//        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
//        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
//
//        tvStatus = findViewById(R.id.tvStatus)
//
//        // Setup buttons
//        findViewById<Button>(R.id.btnEnableAdmin).setOnClickListener { enableDeviceAdmin() }
//        findViewById<Button>(R.id.btnLockTouch).setOnClickListener { lockTouchScreen() }
//        findViewById<Button>(R.id.btnGetFcmToken).setOnClickListener {
//            Log.d(FCM_LOG_TAG, "User clicked: Get FCM Token")
//            getAndDisplayFCMToken()
//        }
//        findViewById<Button>(R.id.disableFactoryReset).setOnClickListener {
//            disableFactoryReset()
//            lockDeviceNow()
//        }
//        findViewById<Button>(R.id.enableFactoryReset).setOnClickListener { enableFactoryReset() }
//
//        // ✅ ADD FOREGROUND SERVICE START
//        startForegroundServiceForFCM()
//
//        // Auto checks
//        checkFCMStatus()
//        handleNotificationFromIntent()
//        updateStatus()
//    }
//
//    // ==============================================
//    // ✅ FOREGROUND SERVICE FOR BACKGROUND FCM
//    // ==============================================
//
//    private fun startForegroundServiceForFCM() {
//        try {
//            if (!isForegroundServiceRunning()) {
//                ForegroundNotificationService.startService(this)
//                Log.d(FCM_LOG_TAG, "🚀 Foreground service started for background FCM")
//                Toast.makeText(this, "Background service started", Toast.LENGTH_SHORT).show()
//            } else {
//                Log.d(FCM_LOG_TAG, "✅ Foreground service already running")
//            }
//        } catch (e: Exception) {
//            Log.e(FCM_LOG_TAG, "❌ Failed to start foreground service: ${e.message}")
//        }
//    }
//
//    private fun isForegroundServiceRunning(): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        return manager.getRunningServices(Integer.MAX_VALUE)
//            .any { it.service.className == ForegroundNotificationService::class.java.name }
//    }
//
//    // ==============================================
//    // FCM TOKEN MANAGEMENT - GET TOKEN FOR TESTING
//    // ==============================================
//
//    private fun checkFCMStatus() {
//        val token = getStoredToken()
//        if (token != null) {
//            Log.d(FCM_LOG_TAG, "✅ Stored FCM Token: ${token.take(20)}...")
//        } else {
//            Log.d(FCM_LOG_TAG, "❌ No FCM token stored")
//        }
//    }
//
//    private fun getAndDisplayFCMToken() {
//        Log.d(FCM_LOG_TAG, "🔄 Requesting FCM token from Firebase...")
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                val error = task.exception?.message ?: "Unknown error"
//                Log.e(FCM_LOG_TAG, "❌ FCM Token Error: $error")
//
//                // User-friendly messages
//                val userMsg = when {
//                    error.contains("AUTHENTICATION_FAILED") -> "Firebase setup issue. Check google-services.json"
//                    error.contains("SERVICE_NOT_AVAILABLE") -> "Google Play Services needed"
//                    error.contains("NETWORK") -> "Internet connection required"
//                    else -> "Failed: ${error.take(50)}..."
//                }
//
//                Toast.makeText(this, userMsg, Toast.LENGTH_LONG).show()
//                return@OnCompleteListener
//            }
//
//            val token = task.result
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//            Log.d(FCM_LOG_TAG, "✅ FCM TOKEN SUCCESS!")
//            Log.d(FCM_LOG_TAG, "Token Length: ${token.length} chars")
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//
//            // PRINT TOKEN FOR EASY COPYING
//            println("🎯 COPY THIS TOKEN TO FIREBASE CONSOLE 🎯")
//            println(token)
//            println("🎯 END OF TOKEN 🎯")
//
//            // Save token
//            saveToken(token)
//
//            // Show partial token to user
//            Toast.makeText(
//                this,
//                "Token: ${token.take(10)}... (Check Logcat for full token)",
//                Toast.LENGTH_LONG
//            ).show()
//
//            updateStatus()
//        })
//    }
//
//    private fun saveToken(token: String) {
//        prefs.edit().apply {
//            putString("fcm_token", token)
//            putLong("token_time", System.currentTimeMillis())
//            apply()
//        }
//        Log.d(FCM_LOG_TAG, "💾 Token saved: ${token.take(15)}...")
//    }
//
//    private fun getStoredToken(): String? {
//        return prefs.getString("fcm_token", null)
//    }
//
//    // ==============================================
//    // HANDLE NOTIFICATION WHEN APP OPENS
//    // ==============================================
//
//    private fun handleNotificationFromIntent() {
//        if (intent?.hasExtra("fcm_notification_received") == true) {
//            val title = intent.getStringExtra("notification_title")
//            val body = intent.getStringExtra("notification_body")
//            val time = intent.getLongExtra("notification_time", 0)
//
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//            Log.d(FCM_LOG_TAG, "📱 APP OPENED FROM FCM NOTIFICATION")
//            Log.d(FCM_LOG_TAG, "Title: $title")
//            Log.d(FCM_LOG_TAG, "Body: $body")
//            Log.d(FCM_LOG_TAG, "Received at: $time")
//            Log.d(FCM_LOG_TAG, "Current time: ${System.currentTimeMillis()}")
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//
//            Toast.makeText(this, "Notification: $title", Toast.LENGTH_LONG).show()
//        }
//
//        // ✅ Check if started from background FCM
//        if (intent?.hasExtra("fcm_background") == true) {
//            val title = intent.getStringExtra("notification_title")
//            val body = intent.getStringExtra("notification_body")
//
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//            Log.d(FCM_LOG_TAG, "📱 APP STARTED BY BACKGROUND FCM")
//            Log.d(FCM_LOG_TAG, "Title: $title")
//            Log.d(FCM_LOG_TAG, "Body: $body")
//            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
//        }
//    }
//
//    // ==============================================
//    // TOUCH LOCK FUNCTIONS (UNCHANGED)
//    // ==============================================
//
//    fun lockTouchScreen() {
//        if (!checkOverlayPermission()) {
//            requestOverlayPermission()
//            return
//        }
//
//        if (isTouchLocked) {
//            Toast.makeText(this, "Already locked. Unlocks in ${getRemainingTime()}s", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            createTouchBlockerOverlay()
//            vibratePhone(200)
//            Toast.makeText(this, "Touch locked for 10s", Toast.LENGTH_SHORT).show()
//
//            isTouchLocked = true
//            touchLockStartTime = System.currentTimeMillis()
//
//            lockRunnable = Runnable {
//                unlockTouchScreen()
//                Toast.makeText(applicationContext, "Auto-unlocked", Toast.LENGTH_SHORT).show()
//            }
//            handler.postDelayed(lockRunnable!!, LOCK_DURATION)
//
//            updateStatus()
//
//        } catch (e: Exception) {
//            Toast.makeText(this, "Lock failed", Toast.LENGTH_SHORT).show()
//            isTouchLocked = false
//        }
//    }
//
//    private fun unlockTouchScreen() {
//        if (!isTouchLocked) return
//
//        try {
//            removeTouchBlockerOverlay()
//            vibratePhone(100)
//            isTouchLocked = false
//            lockRunnable?.let { handler.removeCallbacks(it) }
//            lockRunnable = null
//            updateStatus()
//
//        } catch (e: Exception) {
//            isTouchLocked = false
//            lockRunnable = null
//            updateStatus()
//        }
//    }
//
//    private fun getRemainingTime(): Long {
//        if (!isTouchLocked) return 0
//        val elapsed = System.currentTimeMillis() - touchLockStartTime
//        return maxOf(0, (LOCK_DURATION - elapsed) / 1000)
//    }
//
//    private fun createTouchBlockerOverlay() {
//        touchBlockerView = View(this).apply {
//            setBackgroundColor(Color.argb(136, 0, 0, 0))
//            isClickable = true
//            isFocusable = true
//            setOnTouchListener { _, _ -> true }
//        }
//
//        val params = WindowManager.LayoutParams().apply {
//            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            } else {
//                WindowManager.LayoutParams.TYPE_PHONE
//            }
//
//            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//
//            width = WindowManager.LayoutParams.MATCH_PARENT
//            height = WindowManager.LayoutParams.MATCH_PARENT
//            format = android.graphics.PixelFormat.TRANSLUCENT
//            gravity = Gravity.START or Gravity.TOP
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//            }
//        }
//
//        windowManager.addView(touchBlockerView, params)
//    }
//
//    private fun removeTouchBlockerOverlay() {
//        try {
//            touchBlockerView?.let {
//                if (it.parent != null) {
//                    windowManager.removeView(it)
//                }
//            }
//        } catch (e: Exception) {
//            // Ignore
//        } finally {
//            touchBlockerView = null
//        }
//    }
//
//    private fun checkOverlayPermission(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Settings.canDrawOverlays(this)
//        } else {
//            true
//        }
//    }
//
//    private fun requestOverlayPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                android.net.Uri.parse("package:$packageName")
//            )
//            try {
//                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
//                Toast.makeText(this, "Enable 'Display over other apps'", Toast.LENGTH_LONG).show()
//            } catch (e: Exception) {
//                val intentFallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                    data = android.net.Uri.parse("package:$packageName")
//                }
//                startActivity(intentFallback)
//            }
//        }
//    }
//
//    private fun vibratePhone(duration: Long) {
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                @Suppress("DEPRECATION")
//                vibrator.vibrate(duration)
//            }
//        } catch (e: Exception) {
//            // Ignore
//        }
//    }
//
//    // ==============================================
//    // DEVICE ADMIN FUNCTIONS
//    // ==============================================
//
//    private fun enableDeviceAdmin() {
//        if (!devicePolicyManager.isAdminActive(componentName)) {
//            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
//            intent.putExtra(
//                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Required for device locking"
//            )
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
//        } else {
//            Toast.makeText(this, "Already enabled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun lockDeviceNow() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.lockNow()
//            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun disableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(true)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
//                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun enableFactoryReset() {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//                    try {
//                        applyFactoryResetRestrictions(false)
//                        prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
//                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
//                        updateStatus()
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Enable device admin first", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun applyFactoryResetRestrictions(disable: Boolean) {
//        val restrictions = listOf(
//            "no_factory_reset",
//            "no_safe_boot",
//            "no_debugging_features",
//            "no_development_settings"
//        )
//
//        for (restriction in restrictions) {
//            try {
//                if (disable) {
//                    devicePolicyManager.addUserRestriction(componentName, restriction)
//                } else {
//                    devicePolicyManager.clearUserRestriction(componentName, restriction)
//                }
//            } catch (e: Exception) {
//                // Ignore
//            }
//        }
//    }
//
//    // ==============================================
//    // STATUS UPDATE
//    // ==============================================
//
//    private fun updateStatus() {
//        val status = StringBuilder("Status:\n")
//
//        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
//        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
//        val fcmToken = getStoredToken()
//        val isServiceRunning = isForegroundServiceRunning()
//
//        if (isAdminActive) {
//            status.append("✓ Device Admin\n")
//            status.append(if (isFactoryResetDisabled) "✓ Factory Reset Disabled\n" else "✗ Factory Reset Enabled\n")
//            status.append(if (isTouchLocked) "⏳ Touch Locked (${getRemainingTime()}s)\n" else "✓ Touch Ready\n")
//        } else {
//            status.append("✗ Device Admin\n")
//        }
//
//        if (!checkOverlayPermission()) {
//            status.append("⚠ Overlay Permission\n")
//        }
//
//        status.append(if (fcmToken != null) "✓ FCM Token Available\n" else "✗ No FCM Token\n")
//        status.append(if (isServiceRunning) "✓ Background Service Running\n" else "✗ Background Service Stopped\n")
//
//        tvStatus.text = status.toString()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        when (requestCode) {
//            REQUEST_CODE_ENABLE_ADMIN -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Device admin enabled", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//            OVERLAY_PERMISSION_REQUEST -> {
//                if (checkOverlayPermission()) {
//                    Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
//                }
//                updateStatus()
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        updateStatus()
//
//        if (isTouchLocked && System.currentTimeMillis() - touchLockStartTime >= LOCK_DURATION) {
//            unlockTouchScreen()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unlockTouchScreen()
//        handler.removeCallbacksAndMessages(null)
//
//        // ❌ DON'T stop service here if you want background FCM to work
//        // ForegroundNotificationService.stopService(this)
//    }
//}








package com.uztech.phonelock

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import android.annotation.SuppressLint
import java.util.*
class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var vibrator: Vibrator
    private lateinit var windowManager: WindowManager
    private lateinit var lockManager: LockManager

    private val handler = Handler(Looper.getMainLooper())
    private var isTouchLocked = false
    private var touchLockStartTime: Long = 0
    private var lockRunnable: Runnable? = null

    companion object {
        const val REQUEST_CODE_ENABLE_ADMIN = 100
        const val REQUEST_CODE_ENABLE_DEVICE_OWNER = 101
        const val PREFS_NAME = "PhoneLockPrefs"
        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
        const val LOCK_DURATION = 10000L
        const val OVERLAY_PERMISSION_REQUEST = 102

        // FCM Log tag
        private const val FCM_LOG_TAG = "FCM_MAIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "📱 MainActivity Started")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lockManager = LockManager(this, windowManager, vibrator)

        tvStatus = findViewById(R.id.tvStatus)

        // Setup buttons
        findViewById<Button>(R.id.btnEnableAdmin).setOnClickListener { enableDeviceAdmin() }
        findViewById<Button>(R.id.btnLockTouch).setOnClickListener { lockTouchScreen() }
        findViewById<Button>(R.id.btnGetFcmToken).setOnClickListener {
            Log.d(FCM_LOG_TAG, "User clicked: Get FCM Token")
            getAndDisplayFCMToken()
        }
        findViewById<Button>(R.id.btnUnlockTouch).setOnClickListener { unlockTouchScreen() }

        // Factory Reset Buttons
        findViewById<Button>(R.id.disableFactoryReset).setOnClickListener {
            disableFactoryReset()
        }
        findViewById<Button>(R.id.enableFactoryReset).setOnClickListener {
            enableFactoryReset()
        }

        // Device Owner Button
        findViewById<Button>(R.id.btnEnableDeviceOwner).setOnClickListener {
            showDeviceOwnerInstructions()
        }

        // Start foreground service
        startForegroundServiceForFCM()

        // Handle FCM notifications
        handleFCMNotification()

        // Auto checks
        checkFCMStatus()
        updateStatus()

        // Check permissions
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!checkOverlayPermission()) {
            Toast.makeText(this, "Overlay permission needed for touch lock", Toast.LENGTH_LONG).show()
        }

        if (!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(this, "Device Admin permission needed", Toast.LENGTH_LONG).show()
        }
    }

    private fun isDeviceOwner(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            devicePolicyManager.isDeviceOwnerApp(packageName)
        } else {
            false
        }
    }

    // ==============================================
    // ✅ HANDLE FCM NOTIFICATIONS
    // ==============================================

    private fun handleFCMNotification() {
        val title = intent?.getStringExtra("title")
        val body = intent?.getStringExtra("body")

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "🔍 Checking for FCM notifications...")
        Log.d(FCM_LOG_TAG, "Title: $title")
        Log.d(FCM_LOG_TAG, "Body: $body")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        if (body != null) {
            checkBodyForCommands(body, title)
        }
    }

    private fun checkBodyForCommands(body: String, title: String?) {
        Log.d(FCM_LOG_TAG, "📝 Analyzing notification body: $body")

        val lowerBody = body.lowercase(Locale.getDefault())

        when {
            lowerBody.contains("active") ||
                    lowerBody.contains("account status is now active") -> {
                Log.d(FCM_LOG_TAG, "✅ Found ACTIVE command - LOCKING SCREEN")
                handler.postDelayed({
                    if (lockTouchScreen()) {
                        Toast.makeText(this, "🔒 Screen locked: Account is Active", Toast.LENGTH_LONG).show()
                    }
                }, 1000)
            }

            lowerBody.contains("inactive") ||
                    lowerBody.contains("account status is now inactive") -> {
                Log.d(FCM_LOG_TAG, "✅ Found INACTIVE command - UNLOCKING SCREEN")
                handler.postDelayed({
                    if (unlockTouchScreen()) {
                        Toast.makeText(this, "🔓 Screen unlocked: Account is Inactive", Toast.LENGTH_LONG).show()
                    }
                }, 1000)
            }

            else -> {
                Log.d(FCM_LOG_TAG, "ℹ️ No lock/unlock command found in body")
                if (title != null) {
                    Toast.makeText(this, "Notification: $title", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ==============================================
    // ✅ FOREGROUND SERVICE
    // ==============================================

    private fun startForegroundServiceForFCM() {
        try {
            if (!isForegroundServiceRunning()) {
                ForegroundNotificationService.startService(this)
                Log.d(FCM_LOG_TAG, "🚀 Foreground service started")
            } else {
                Log.d(FCM_LOG_TAG, "✅ Foreground service already running")
            }
        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "❌ Failed to start foreground service: ${e.message}")
        }
    }

    private fun isForegroundServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == ForegroundNotificationService::class.java.name }
    }

    // ==============================================
    // FCM TOKEN MANAGEMENT
    // ==============================================

    private fun checkFCMStatus() {
        val token = getStoredToken()
        if (token != null) {
            Log.d(FCM_LOG_TAG, "✅ Stored FCM Token: ${token.take(20)}...")
        } else {
            Log.d(FCM_LOG_TAG, "❌ No FCM token stored")
        }
    }

    private fun getAndDisplayFCMToken() {
        Log.d(FCM_LOG_TAG, "🔄 Requesting FCM token from Firebase...")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                val error = task.exception?.message ?: "Unknown error"
                Log.e(FCM_LOG_TAG, "❌ FCM Token Error: $error")

                val userMsg = when {
                    error.contains("AUTHENTICATION_FAILED") -> "Firebase setup issue"
                    error.contains("SERVICE_NOT_AVAILABLE") -> "Google Play Services needed"
                    error.contains("NETWORK") -> "Internet connection required"
                    else -> "Failed to get token"
                }

                Toast.makeText(this, userMsg, Toast.LENGTH_LONG).show()
                return@OnCompleteListener
            }

            val token = task.result
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
            Log.d(FCM_LOG_TAG, "✅ FCM TOKEN SUCCESS!")
            Log.d(FCM_LOG_TAG, "Token Length: ${token.length} chars")
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

            // Print token for easy copying
            println("\n🎯 COPY THIS TOKEN 🎯")
            println(token)
            println("🎯 END OF TOKEN 🎯\n")

            // Save token
            saveToken(token)

            // Show to user
            Toast.makeText(
                this,
                "Token saved! Check Logcat for full token",
                Toast.LENGTH_LONG
            ).show()

            updateStatus()
        })
    }

    private fun saveToken(token: String) {
        prefs.edit().apply {
            putString("fcm_token", token)
            putLong("token_time", System.currentTimeMillis())
            apply()
        }
        Log.d(FCM_LOG_TAG, "💾 Token saved: ${token.take(15)}...")
    }

    private fun getStoredToken(): String? {
        return prefs.getString("fcm_token", null)
    }

    // ==============================================
    // TOUCH LOCK FUNCTIONS
    // ==============================================

    fun lockTouchScreen(): Boolean {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return false
        }

        if (isTouchLocked) {
            Toast.makeText(this, "Already locked. Unlocks in ${getRemainingTime()}s", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            lockManager.lockTouchScreen()
            vibratePhone(200)
            Toast.makeText(this, "Touch locked for 10s", Toast.LENGTH_SHORT).show()

            isTouchLocked = true
            touchLockStartTime = System.currentTimeMillis()

            lockRunnable = Runnable {
                unlockTouchScreen()
                Toast.makeText(applicationContext, "Auto-unlocked", Toast.LENGTH_SHORT).show()
            }
            handler.postDelayed(lockRunnable!!, LOCK_DURATION)

            updateStatus()
            return true

        } catch (e: Exception) {
            Toast.makeText(this, "Lock failed: ${e.message}", Toast.LENGTH_SHORT).show()
            isTouchLocked = false
            return false
        }
    }

    private fun unlockTouchScreen(): Boolean {
        if (!isTouchLocked) {
            Toast.makeText(this, "Screen not locked", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            lockManager.unlockTouchScreen()
            vibratePhone(100)
            isTouchLocked = false
            lockRunnable?.let { handler.removeCallbacks(it) }
            lockRunnable = null
            updateStatus()
            return true

        } catch (e: Exception) {
            Toast.makeText(this, "Unlock failed: ${e.message}", Toast.LENGTH_SHORT).show()
            isTouchLocked = false
            lockRunnable = null
            updateStatus()
            return false
        }
    }

    private fun getRemainingTime(): Long {
        if (!isTouchLocked) return 0
        val elapsed = System.currentTimeMillis() - touchLockStartTime
        return maxOf(0, (LOCK_DURATION - elapsed) / 1000)
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            try {
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
                Toast.makeText(this, "Enable 'Display over other apps'", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                val intentFallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intentFallback)
            }
        }
    }

    private fun vibratePhone(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    // ==============================================
    // DEVICE ADMIN & DEVICE OWNER FUNCTIONS
    // ==============================================

    private fun enableDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Required for device locking and factory reset control"
            )
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        } else {
            Toast.makeText(this, "✅ Device admin already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeviceOwnerInstructions() {
        val message = """
            📱 DEVICE OWNER SETUP:
            
            Device Owner is required for factory reset control.
            
            🔧 Setup via ADB:
            1. Connect device via USB
            2. Enable USB Debugging
            3. Run command:
            
            adb shell dpm set-device-owner com.uztech.phonelock/.DeviceAdminReceiver
            
            ⚠️ Requirements:
            • Device must be unprovisioned (factory reset)
            • No existing accounts
            • May need to remove all users
            
            📝 Note: Test on emulator or dedicated device.
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // ==============================================
    // FACTORY RESET CONTROL FUNCTIONS
    // ==============================================

    private fun disableFactoryReset() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(this, "❌ Enable Device Admin first", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                try {
                    applyFactoryResetRestrictions(true)
                    prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, true).apply()
                    Toast.makeText(this, "✅ Factory reset disabled", Toast.LENGTH_SHORT).show()
                    updateStatus()
                } catch (e: SecurityException) {
                    Toast.makeText(this, "❌ Permission denied: Need device owner", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "❌ Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this,
                    "❌ Need DEVICE OWNER permission for factory reset control\n" +
                            "Use ADB command shown in Device Owner button",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this, "❌ Requires Android 5.0+", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableFactoryReset() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            Toast.makeText(this, "❌ Enable Device Admin first", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                try {
                    applyFactoryResetRestrictions(false)
                    prefs.edit().putBoolean(KEY_FACTORY_RESET_DISABLED, false).apply()
                    Toast.makeText(this, "✅ Factory reset enabled", Toast.LENGTH_SHORT).show()
                    updateStatus()
                } catch (e: SecurityException) {
                    Toast.makeText(this, "❌ Permission denied", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "❌ Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "❌ Need device owner permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun applyFactoryResetRestrictions(disable: Boolean) {
        val restrictions = listOf(
            "no_factory_reset",
            "no_safe_boot",
            "no_debugging_features",
            "no_development_settings"
        )

        for (restriction in restrictions) {
            try {
                if (disable) {
                    devicePolicyManager.addUserRestriction(componentName, restriction)
                } else {
                    devicePolicyManager.clearUserRestriction(componentName, restriction)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ==============================================
    // STATUS UPDATE
    // ==============================================

    private fun updateStatus() {
        val status = StringBuilder("📱 PhoneLock Status\n\n")

        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
        val isDeviceOwner = isDeviceOwner()
        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
        val fcmToken = getStoredToken()
        val isServiceRunning = isForegroundServiceRunning()
        val hasOverlayPermission = checkOverlayPermission()

        // Device Admin Status
        status.append(if (isAdminActive) "✅ Device Admin Active\n" else "❌ Device Admin Inactive\n")

        // Device Owner Status
        status.append(if (isDeviceOwner) "✅ Device Owner Active\n" else "❌ Device Owner Inactive\n")

        // Factory Reset Status
        if (isDeviceOwner) {
            status.append(if (isFactoryResetDisabled) "✅ Factory Reset DISABLED\n" else "⚠ Factory Reset ENABLED\n")
        } else {
            status.append("⚠ Factory Reset: Need Device Owner\n")
        }

        // Touch Lock Status
        status.append(if (isTouchLocked) "🔒 Touch Locked (${getRemainingTime()}s)\n" else "✅ Touch Ready\n")

        // FCM Status
        status.append(if (fcmToken != null) "✅ FCM Token Available\n" else "❌ No FCM Token\n")

        // Service Status
        status.append(if (isServiceRunning) "✅ Background Service Running\n" else "⚠ Service Stopped\n")

        // Permission Status
        if (!hasOverlayPermission) {
            status.append("⚠ Overlay Permission Needed\n")
        }

        tvStatus.text = status.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "✅ Device admin enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Device admin not enabled", Toast.LENGTH_SHORT).show()
                }
                updateStatus()
            }

            OVERLAY_PERMISSION_REQUEST -> {
                if (checkOverlayPermission()) {
                    Toast.makeText(this, "✅ Overlay permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Overlay permission denied", Toast.LENGTH_SHORT).show()
                }
                updateStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()

        if (isTouchLocked && System.currentTimeMillis() - touchLockStartTime >= LOCK_DURATION) {
            unlockTouchScreen()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Set the new intent so we can read extras
        setIntent(intent)

        Log.d(FCM_LOG_TAG, "🔄 onNewIntent called - app was already running")

        // Handle FCM notifications again
        handleFCMNotification()

        // Update status
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        unlockTouchScreen()
        handler.removeCallbacksAndMessages(null)
    }
}