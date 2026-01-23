package com.uztech.phonelock

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.net.HttpURLConnection
import java.net.URL
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import android.app.AlertDialog
import android.os.UserManager
import android.net.Uri

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

    companion object {
        const val REQUEST_CODE_ENABLE_ADMIN = 100
        const val REQUEST_CODE_ENABLE_DEVICE_OWNER = 101
        const val PREFS_NAME = "PhoneLockPrefs"
        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
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
        findViewById<Button>(R.id.btnGetFcmToken).setOnClickListener {
            Log.d(FCM_LOG_TAG, "User clicked: Get FCM Token")
            getAndDisplayFCMToken()
        }

        // Factory Reset Buttons
        findViewById<Button>(R.id.disableFactoryReset).setOnClickListener {
            disableFactoryReset()
        }

        // Factory Reset Buttons
        findViewById<Button>(R.id.btnOverlayPermanentOn).setOnClickListener {
            enablePermanentOverlayViaFCM()
        }

        // Start foreground service
        startForegroundServiceForFCM()

        // Handle FCM notifications
        handleFCMNotification()

        // Auto checks
        checkFCMStatus()
        updateStatus()
        // Device Owner check
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, DeviceAdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(packageName)) {
            // Start permanent overlay
            startService(Intent(this, PermanentOverlayService::class.java))

            // Enable lock task mode
            enableLockTaskMode()
        }
        // Check permissions
        checkPermissions()

        // Check if lock should be restored from previous session
        checkAndRestoreLockState()
    }

    private fun enableLockTaskMode() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, DeviceAdminReceiver::class.java)

        // White list this app for lock task
        dpm.setLockTaskPackages(admin, arrayOf(packageName))

        // Start lock task
        startLockTask()

        // Optional: Hide system UI
        dpm.setLockTaskFeatures(admin,
            DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
    }

    private fun checkAndRestoreLockState() {
        val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)

        if (wasLocked) {
            handler.postDelayed({
                lockTouchScreen()
            }, 2000)
        }
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
    // ✅ PERMANENT OVERLAY PERMISSION VIA FCM
    // ==============================================

    // FCM দিয়ে Overlay Permission ON করার function
    fun enablePermanentOverlayViaFCM() {
        Log.d(FCM_LOG_TAG, "🔄 Processing FCM Overlay Enable Command")

        // Step 1: Check if Device Owner
        if (!isDeviceOwner()) {
            Toast.makeText(this,
                "❌ Device Owner permission required\n" +
                        "Use ADB: adb shell dpm set-device-owner com.uztech.phonelock/.DeviceAdminReceiver",
                Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Step 2: Ensure OUR app has overlay permission
            ensureOurAppOverlayPermission()

            // Step 3: Apply Device Owner restrictions to make it permanent
            applyPermanentOverlayRestrictions()

            // Step 4: Save state
            prefs.edit().apply {
                putBoolean("overlay_permanent_enabled", true)
                apply()
            }

            // Step 5: Show success message
            Toast.makeText(this,
                "✅ Overlay Permission Permanently Enabled\n" +
                        "• Your app overlay always ON\n" +
                        "• Child cannot disable\n" +
                        "• Lock screen always works",
                Toast.LENGTH_LONG).show()

            Log.d(FCM_LOG_TAG, "✅ Overlay permanently enabled via FCM")
            updateStatus()

        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "❌ Failed to enable overlay: ${e.message}")
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // FCM দিয়ে Overlay Permission OFF করার function
    fun disablePermanentOverlayViaFCM() {
        Log.d(FCM_LOG_TAG, "🔄 Processing FCM Overlay Disable Command")

        if (!isDeviceOwner()) {
            Toast.makeText(this, "Device Owner permission needed", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Remove restrictions
            removeOverlayRestrictions()

            // Save state
            prefs.edit().apply {
                putBoolean("overlay_permanent_enabled", false)
                apply()
            }

            Toast.makeText(this,
                "✅ Overlay Restrictions Removed\n" +
                        "• User can now change overlay settings\n" +
                        "• Your app overlay may be disabled",
                Toast.LENGTH_LONG).show()

            Log.d(FCM_LOG_TAG, "✅ Overlay restrictions removed via FCM")
            updateStatus()

        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "❌ Failed to disable overlay: ${e.message}")
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // আমাদের App এর Overlay Permission ensure করা
    private fun ensureOurAppOverlayPermission() {
        if (!checkOverlayPermission()) {
            // Auto open settings for permission
            autoOpenOverlaySettings()
        }
    }

    // Automatic Settings open
    private fun autoOpenOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Show alert before opening
            AlertDialog.Builder(this)
                .setTitle("Overlay Permission Required")
                .setMessage("Your app needs Overlay Permission to lock screen. Please enable it.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
                }
                .setCancelable(false)
                .show()
        }
    }

    @SuppressLint("NewApi")
    private fun applyPermanentOverlayRestrictions() {
        if (!isDeviceOwner()) return

        try {
            // 1. Block app uninstall
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                devicePolicyManager.setUninstallBlocked(
                    componentName,
                    packageName,
                    true
                )
                Log.d(FCM_LOG_TAG, "App uninstall blocked")
            }

            // 2. Disable Developer Options
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.putInt(
                    contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    0
                )
                Log.d(FCM_LOG_TAG, "Developer Options disabled")
            }

            // 3. Disable ADB Debugging
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.putInt(
                    contentResolver,
                    Settings.Global.ADB_ENABLED,
                    0
                )
                Log.d(FCM_LOG_TAG, "ADB Debugging disabled")
            }

            // 4. Add User Restrictions
            val restrictions = arrayOf(
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_DEBUGGING_FEATURES,
                UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserManager.DISALLOW_CONFIG_BRIGHTNESS,
                UserManager.DISALLOW_CONFIG_DATE_TIME
            )

            for (restriction in restrictions) {
                try {
                    devicePolicyManager.addUserRestriction(componentName, restriction)
                    Log.d(FCM_LOG_TAG, "Restriction added: $restriction")
                } catch (e: Exception) {
                    Log.e(FCM_LOG_TAG, "Failed to add $restriction: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "Failed to apply restrictions: ${e.message}")
            throw e
        }
    }

    @SuppressLint("NewApi")
    private fun removeOverlayRestrictions() {
        if (!isDeviceOwner()) return

        try {
            // 1. Allow app uninstall
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                devicePolicyManager.setUninstallBlocked(
                    componentName,
                    packageName,
                    false
                )
            }

            // 2. Enable Developer Options
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.putInt(
                    contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                    1
                )
            }

            // 3. Enable ADB Debugging
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.putInt(
                    contentResolver,
                    Settings.Global.ADB_ENABLED,
                    1
                )
            }

            // 4. Remove User Restrictions
            val restrictions = arrayOf(
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_DEBUGGING_FEATURES,
                UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserManager.DISALLOW_CONFIG_BRIGHTNESS,
                UserManager.DISALLOW_CONFIG_DATE_TIME
            )

            for (restriction in restrictions) {
                try {
                    devicePolicyManager.clearUserRestriction(componentName, restriction)
                } catch (e: Exception) {
                    // Ignore
                }
            }

        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "Failed to remove restrictions: ${e.message}")
        }
    }

    // ==============================================
    // ✅ UPDATED FCM NOTIFICATION HANDLER
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
        Log.d(FCM_LOG_TAG, "📝 Analyzing notification body: $body  $title")

        val lowerBody = body.lowercase(Locale.getDefault())

        when {
            lowerBody.contains("account status is now active") -> {
                Log.d(FCM_LOG_TAG, "✅ Found ACTIVE command - LOCKING SCREEN")
                handler.postDelayed({
                    if (lockTouchScreen()) {
                        Toast.makeText(this, "🔒 Screen locked: Account is Active", Toast.LENGTH_LONG).show()
                    }
                }, 1000)
            }

            lowerBody.contains("account status is now inactive") -> {
                Log.d(FCM_LOG_TAG, "✅ Found INACTIVE command - UNLOCKING SCREEN")
                handler.postDelayed({
                    if (unlockTouchScreen()) {
                        Toast.makeText(this, "🔓 Screen unlocked: Account is Inactive", Toast.LENGTH_LONG).show()
                    }
                }, 1000)
            }

            lowerBody.contains("account status is now pending") -> {
                Log.d(FCM_LOG_TAG, "✅ Found PENDING command - ENABLING FACTORY RESET")
                handler.postDelayed({
                    enableFactoryReset()
                }, 1000)
            }

            // ✅ NEW: Overlay Permanent Enable via FCM
            lowerBody.contains("overlay permanent on") ||
                    lowerBody.contains("overlay always on") ||
                    lowerBody.contains("enable overlay permanent") -> {
                Log.d(FCM_LOG_TAG, "✅ Found OVERLAY PERMANENT ON command")
                handler.postDelayed({
                    enablePermanentOverlayViaFCM()
                }, 1000)
            }

            // ✅ NEW: Overlay Restrictions Remove via FCM
            lowerBody.contains("overlay permanent off") ||
                    lowerBody.contains("disable overlay permanent") ||
                    lowerBody.contains("remove overlay restrictions") -> {
                Log.d(FCM_LOG_TAG, "✅ Found OVERLAY PERMANENT OFF command")
                handler.postDelayed({
                    disablePermanentOverlayViaFCM()
                }, 1000)
            }

            // ✅ NEW: Check Status via FCM
            lowerBody.contains("status") ||
                    lowerBody.contains("check status") -> {
                Log.d(FCM_LOG_TAG, "✅ Found STATUS CHECK command")
                handler.postDelayed({
                    sendStatusToServer()
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
    // ✅ STATUS MONITORING & REPORTING
    // ==============================================

    private fun sendStatusToServer() {
        val status = getDeviceStatus()
        val deviceId = Settings.Secure.getString(contentResolver,
            Settings.Secure.ANDROID_ID) ?: "unknown"

        val statusUrl = "https://ephonelocker.info/api/update-status?imei=$deviceId&status=${Uri.encode(status)}"

        Thread {
            try {
                val urlObj = URL(statusUrl)
                val connection = urlObj.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("User-Agent", "Android-App")

                val responseCode = connection.responseCode
                Log.d("StatusUpdate", "Response Code: $responseCode")

                if (responseCode == 200) {
                    Log.d(FCM_LOG_TAG, "✅ Status sent to server")
                }
            } catch (e: Exception) {
                Log.e("StatusUpdate", "Error: ${e.message}")
            }
        }.start()
    }

    private fun getDeviceStatus(): String {
        return StringBuilder().apply {
            append("Device Owner: ${if (isDeviceOwner()) "✅" else "❌"}\n")
            append("Device Admin: ${if (devicePolicyManager.isAdminActive(componentName)) "✅" else "❌"}\n")
            append("Overlay Permission: ${if (checkOverlayPermission()) "✅" else "❌"}\n")
            append("Screen Locked: ${if (isTouchLocked) "🔒" else "🔓"}\n")
            append("Overlay Permanent: ${if (prefs.getBoolean("overlay_permanent_enabled", false)) "✅" else "❌"}\n")
            append("Factory Reset: ${if (prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)) "🔒" else "🔓"}")
        }.toString()
    }

    // ==============================================
    // ✅ UPDATED STATUS DISPLAY
    // ==============================================

    private fun updateStatus() {
        val status = StringBuilder("📱 PhoneLock Status\n\n")

        val isAdminActive = devicePolicyManager.isAdminActive(componentName)
        val isDeviceOwner = isDeviceOwner()
        val isFactoryResetDisabled = prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)
        val fcmToken = getStoredToken()
        val isServiceRunning = isForegroundServiceRunning()
        val hasOverlayPermission = checkOverlayPermission()
        val isOverlayPermanent = prefs.getBoolean("overlay_permanent_enabled", false)

        // Device Admin Status
        status.append(if (isAdminActive) "✅ Device Admin Active\n" else "❌ Device Admin Inactive\n")

        // Device Owner Status
        status.append(if (isDeviceOwner) "✅ Device Owner Active\n" else "❌ Device Owner Inactive\n")

        // Factory Reset Status
        if (isDeviceOwner) {
            status.append(if (isFactoryResetDisabled) "🔒 Factory Reset DISABLED\n" else "🔓 Factory Reset ENABLED\n")
        }

        // Overlay Status
        status.append(if (hasOverlayPermission) "✅ Overlay Permission Granted\n" else "❌ Overlay Permission Needed\n")

        if (isOverlayPermanent && isDeviceOwner) {
            status.append("🔒 Overlay Permanent: ENABLED\n")
        }

        // Touch Lock Status
        status.append(if (isTouchLocked) "🔒 Touch LOCKED (Pending Payment)\n" else "✅ Touch Ready\n")

        if (isTouchLocked) {
            status.append("📱 Bkash: 01996914242\n")
            status.append("💳 Nagad: 01996914242\n")
        }

        // FCM Status
        status.append(if (fcmToken != null) "✅ FCM Token Available\n" else "❌ No FCM Token\n")

        // Service Status
        status.append(if (isServiceRunning) "✅ Background Service Running\n" else "⚠ Service Stopped\n")

        tvStatus.text = status.toString()
    }

    // ==============================================
    // ✅ EXISTING FUNCTIONS (UNCHANGED)
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

            // Get device ID
            val deviceId = Settings.Secure.getString(contentResolver,
                Settings.Secure.ANDROID_ID) ?: "unknown"

            // Register device (run in background)
            sendRegistrationData(deviceId, token)

            // Save token locally
            saveToken(token)

            Toast.makeText(
                this,
                "Token saved! Check Logcat for full token",
                Toast.LENGTH_LONG
            ).show()

            updateStatus()
        })
    }

    private fun sendRegistrationData(deviceId: String, token: String) {
        Thread {
            try {
                // Register device
                val registerUrl = "https://ephonelocker.info/api/register?imei_number=$deviceId&name=${Build.MANUFACTURER} ${Build.MODEL}&phone=01700000009&email=$deviceId@example.com&address=Dhaka, Bangladesh&nominee_name=Nominee Name&nominee_phone=01800000009&total_amount=50000&down_payment=10000&interval_type=1&interval_value=6&payable_amount=40000&per_installment=3333.33&bill_date=2025-01-15&admin_id=2"
                Log.d("RequestURL", "Register URL: $registerUrl")

                sendPostRequest(registerUrl)

                // Save FCM token to server
                val tokenUrl = "https://ephonelocker.info/api/save-firebase-token?token=$token&imei=$deviceId"
                Log.d("RequestURL", "Token URL: $tokenUrl")

                sendPostRequest(tokenUrl)

            } catch (e: Exception) {
                Log.e("Registration", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity,
                        "Registration error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendPostRequest(urlString: String) {
        try {
            val urlObj = URL(urlString)
            val connection = urlObj.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("User-Agent", "Android-App")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            Log.d("POST Response", "Response Code: $responseCode")

            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No response"
            }

            Log.d("POST Response", "Response: ${if (response.length > 200) response.substring(0, 200) + "..." else response}")

            runOnUiThread {
                if (responseCode == 200) {
                    Toast.makeText(this, "Server request successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Server returned: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e("POST Error", e.toString())
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    fun lockTouchScreen(): Boolean {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return false
        }

        if (isTouchLocked) {
            Toast.makeText(this, "Screen already locked", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            lockManager.lockTouchScreen()
            vibratePhone(200)
            isTouchLocked = true
            touchLockStartTime = System.currentTimeMillis()

            prefs.edit().apply {
                putBoolean("was_locked_before_reboot", true)
                putLong("lock_start_time", touchLockStartTime)
                apply()
            }

            Toast.makeText(this, "🔒 Screen locked", Toast.LENGTH_SHORT).show()
            updateStatus()
            return true
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Lock failed: ${e.message}", Toast.LENGTH_SHORT).show()
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

            prefs.edit().apply {
                putBoolean("was_locked_before_reboot", false)
                remove("lock_start_time")
                apply()
            }

            Toast.makeText(this, "✅ Screen unlocked", Toast.LENGTH_SHORT).show()
            updateStatus()
            return true
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Unlock failed: ${e.message}", Toast.LENGTH_SHORT).show()
            isTouchLocked = false
            prefs.edit().putBoolean("was_locked_before_reboot", false).apply()
            updateStatus()
            return false
        }
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
                Toast.makeText(this, "Please enable Overlay Permission", Toast.LENGTH_LONG).show()
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
                    if (prefs.getBoolean("was_locked_before_reboot", false)) {
                        handler.postDelayed({ lockTouchScreen() }, 1000)
                    }
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
        val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)
        if (wasLocked && !isTouchLocked) {
            handler.postDelayed({ lockTouchScreen() }, 1500)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(FCM_LOG_TAG, "🔄 onNewIntent called")
        handleFCMNotification()
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}