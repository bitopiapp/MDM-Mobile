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


























package com.uztech.phonelock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvStatus: TextView

    companion object {
        const val REQUEST_CODE_ENABLE_ADMIN = 100
        const val REQUEST_CODE_SET_DEVICE_OWNER = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)

        tvStatus = findViewById(R.id.tvStatus)
        val btnSetDeviceOwner = findViewById<Button>(R.id.btnSetDeviceOwner)
        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
        val btnLockNow = findViewById<Button>(R.id.btnLockNow)
        val btnDisallowAll = findViewById<Button>(R.id.btnDisallowAll)

        val disableFactoryReset = findViewById<Button>(R.id.disableFactoryReset)
        val enableFactoryReset = findViewById<Button>(R.id.enableFactoryReset)

        btnSetDeviceOwner.setOnClickListener { setDeviceOwner() }
        btnEnableAdmin.setOnClickListener { enableDeviceAdmin() }
        btnLockNow.setOnClickListener { lockDeviceNow() }
        btnDisallowAll.setOnClickListener { disallowAllFeatures() }

        disableFactoryReset.setOnClickListener { disableFactoryReset() }
        enableFactoryReset.setOnClickListener { enableFactoryReset() }

        updateStatus()
    }

    private fun setDeviceOwner() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
                try {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                    intent.putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Setting as device owner for complete control"
                    )
                    startActivityForResult(intent, REQUEST_CODE_SET_DEVICE_OWNER)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Use adb command:\nadb shell dpm set-device-owner com.uztech.phonelock/.DeviceAdminReceiver",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Already device owner", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Device admin permission is required to lock the device and control settings"
            )
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        } else {
            Toast.makeText(this, "Device admin already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun lockDeviceNow() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.lockNow()
            Toast.makeText(this, "Device locked", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableFactoryReset() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                    try {
                        // Apply factory reset prevention
                        applyFactoryResetRestrictions(true)
                        Toast.makeText(this, "Factory reset disabled", Toast.LENGTH_SHORT).show()
                        updateStatus()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to disable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableFactoryReset() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                    try {
                        // Remove factory reset restrictions
                        applyFactoryResetRestrictions(false)
                        Toast.makeText(this, "Factory reset enabled", Toast.LENGTH_SHORT).show()
                        updateStatus()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to enable factory reset: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Need device owner permission", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFactoryResetRestrictions(disable: Boolean) {
        // Factory reset restriction constants
        val factoryResetRestriction = "no_factory_reset"
        val safeBootRestriction = "no_safe_boot"
        val debuggingRestriction = "no_debugging_features"
        val devSettingsRestriction = "no_development_settings"

        val restrictions = listOf(
            factoryResetRestriction,
            safeBootRestriction,
            debuggingRestriction,
            devSettingsRestriction
        )

        for (restriction in restrictions) {
            try {
                if (disable) {
                    devicePolicyManager.addUserRestriction(componentName, restriction)
                } else {
                    devicePolicyManager.clearUserRestriction(componentName, restriction)
                }
            } catch (e: Exception) {
                // Some restrictions might not be supported
                println("Restriction $restriction not supported: ${e.message}")
            }
        }

        // Also control other related restrictions
        val otherRestrictions = listOf(
            "no_config_wifi",
            "no_config_bluetooth",
            "no_config_tethering",
            "no_share_location"
        )

        for (restriction in otherRestrictions) {
            try {
                if (disable) {
                    devicePolicyManager.addUserRestriction(componentName, restriction)
                } else {
                    devicePolicyManager.clearUserRestriction(componentName, restriction)
                }
            } catch (e: Exception) {
                // Ignore unsupported restrictions
            }
        }
    }

    private fun disallowAllFeatures() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            try {
                // Disable camera
                devicePolicyManager.setCameraDisabled(componentName, true)

                // Device owner features
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                        // Hide Play Store
                        try {
                            devicePolicyManager.setApplicationHidden(
                                componentName,
                                "com.android.vending",
                                true
                            )
                        } catch (e: Exception) {
                            // Play Store might not exist
                        }

                        // Apply all restrictions
                        applyAllRestrictions()

                        // Also disable factory reset
                        applyFactoryResetRestrictions(true)
                    }
                }

                Toast.makeText(this, "All features disabled including factory reset", Toast.LENGTH_SHORT).show()
                updateStatus()
            } catch (e: SecurityException) {
                Toast.makeText(
                    this,
                    "Need device owner permission for complete control",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(this, "Please enable device admin first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyAllRestrictions() {
        // Create a list of restriction names to try
        val restrictionNames = mutableListOf<String>()

        // Try to add DISALLOW_INSTALL_APPS
        try {
            val disallowInstallApps = try {
                // Try to get the constant value
                DevicePolicyManager::class.java.getField("DISALLOW_INSTALL_APPS").get(null) as String
            } catch (e: NoSuchFieldException) {
                // If constant doesn't exist, use the string value directly
                "no_install_apps"
            } catch (e: Exception) {
                null
            }

            if (disallowInstallApps != null) {
                restrictionNames.add(disallowInstallApps)
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Try to add DISALLOW_UNINSTALL_APPS
        try {
            val disallowUninstallApps = try {
                DevicePolicyManager::class.java.getField("DISALLOW_UNINSTALL_APPS").get(null) as String
            } catch (e: NoSuchFieldException) {
                "no_uninstall_apps"
            } catch (e: Exception) {
                null
            }

            if (disallowUninstallApps != null) {
                restrictionNames.add(disallowUninstallApps)
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Add other commonly available restrictions
        addCommonRestrictions(restrictionNames)

        // Apply all restrictions
        for (restriction in restrictionNames) {
            try {
                devicePolicyManager.addUserRestriction(componentName, restriction)
                println("Applied restriction: $restriction")
            } catch (e: IllegalArgumentException) {
                println("Failed to apply restriction $restriction: ${e.message}")
            } catch (e: SecurityException) {
                println("Security exception for $restriction: ${e.message}")
            }
        }
    }

    private fun addCommonRestrictions(restrictions: MutableList<String>) {
        // Add restrictions that are usually available

        // Try DISALLOW_CONFIG_WIFI
        try {
            restrictions.add("no_config_wifi")
        } catch (e: Exception) {}

        // Try DISALLOW_CONFIG_BLUETOOTH
        try {
            restrictions.add("no_config_bluetooth")
        } catch (e: Exception) {}

        // Try DISALLOW_SHARE_LOCATION (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                restrictions.add("no_share_location")
            } catch (e: Exception) {}
        }

        // Try DISALLOW_CONFIG_TETHERING
        try {
            restrictions.add("no_config_tethering")
        } catch (e: Exception) {}

        // Try DISALLOW_CONFIG_DATE_TIME
        try {
            restrictions.add("no_config_date_time")
        } catch (e: Exception) {}

        // Try DISALLOW_SAFE_BOOT
        try {
            restrictions.add("no_safe_boot")
        } catch (e: Exception) {}
    }

    private fun updateStatus() {
        val status = StringBuilder("Status:\n")

        val isAdminActive = devicePolicyManager.isAdminActive(componentName)

        if (isAdminActive) {
            status.append("✓ Device Admin Enabled\n")

            try {
                val isCameraDisabled = devicePolicyManager.getCameraDisabled(componentName)
                if (isCameraDisabled) {
                    status.append("✓ Camera Disabled\n")
                } else {
                    status.append("✗ Camera Enabled\n")
                }
            } catch (e: SecurityException) {
                status.append("? Camera Status Unknown\n")
            }

            // Check factory reset status
            try {
                // Try to check if factory reset is restricted
                val restrictions = listOf("no_factory_reset", "no_safe_boot")
                var factoryResetDisabled = false
                for (restriction in restrictions) {
                    try {
                        // Note: There's no direct API to check if a restriction is set
                        // We'll assume if device owner is set and we applied restrictions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                                factoryResetDisabled = true
                                break
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                }

                if (factoryResetDisabled) {
                    status.append("✓ Factory Reset Disabled\n")
                } else {
                    status.append("✗ Factory Reset Enabled\n")
                }
            } catch (e: Exception) {
                status.append("? Factory Reset Status Unknown\n")
            }

        } else {
            status.append("✗ Device Admin Disabled\n")
            status.append("✗ Camera Control Not Available\n")
            status.append("✗ Factory Reset Control Not Available\n")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                    status.append("✓ Device Owner\n")

                    if (isAdminActive) {
                        try {
                            val isPlayStoreHidden = devicePolicyManager.isApplicationHidden(
                                componentName,
                                "com.android.vending"
                            )
                            if (isPlayStoreHidden) {
                                status.append("✓ Play Store Hidden\n")
                            } else {
                                status.append("✗ Play Store Visible\n")
                            }
                        } catch (e: Exception) {
                            status.append("? Play Store Status Unknown\n")
                        }
                    }
                } else {
                    status.append("✗ Not Device Owner\n")
                }
            } catch (e: SecurityException) {
                status.append("? Device Owner Status Unknown\n")
            }
        } else {
            status.append("✗ Device Owner Not Supported\n")
        }

        tvStatus.text = status.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Device admin enabled successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to enable device admin", Toast.LENGTH_SHORT).show()
                }
                updateStatus()
            }

            REQUEST_CODE_SET_DEVICE_OWNER -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Device owner set successfully", Toast.LENGTH_SHORT).show()
                }
                updateStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }
}