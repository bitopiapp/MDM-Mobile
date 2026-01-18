
package com.uztech.phonelock


import android.Manifest
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.os.Build
import androidx.core.content.ContextCompat

import java.net.HttpURLConnection
import java.net.URL

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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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
        const val LOCK_DURATION = 30 * 60 * 1000L
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
        Log.d(FCM_LOG_TAG, "Title 112313: $title")
        Log.d(FCM_LOG_TAG, "Body 23213123: $body")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        if (body != null) {
            checkBodyForCommands(body, title)
        }
    }

    private fun checkBodyForCommands(body: String, title: String?) {
        //hfhvghvghc
        Log.d(FCM_LOG_TAG, "📝 Analyzing notification body: $body  $title")

        val lowerBody = body.lowercase(Locale.getDefault())

        when { lowerBody.contains("account status is now active") -> {
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
                Log.d(FCM_LOG_TAG, "✅ Found INACTIVE command - UNLOCKING SCREEN")
                handler.postDelayed({
                    enableFactoryReset()
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
            // Get device ID
            val deviceId = Settings.Secure.getString(contentResolver,
                Settings.Secure.ANDROID_ID) ?: "unknown"





            val url2 = "https://ephonelocker.info/api/register?imei_number=$deviceId&name=${Build.MANUFACTURER} ${Build.MODEL}&phone=01700000009&email=$deviceId@example.com&address=Dhaka, Bangladesh&nominee_name=Nominee Name&nominee_phone=01800000009&total_amount=50000&down_payment=10000&interval_type=1&interval_value=6&payable_amount=40000&per_installment=3333.33&bill_date=2025-01-15&admin_id=2"
            Log.d("RequestURL", "URL: $url2")
            Thread {
                try {
                    val urlObj = URL(url2)
                    val connection = urlObj.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"

                    // Debug: Print all response headers
                    connection.setRequestProperty("User-Agent", "Android-App")
                    connection.setRequestProperty("Accept", "application/json") // Request JSON

                    // Try to follow redirects
                    connection.instanceFollowRedirects = false // Disable to see redirects

                    val responseCode = connection.responseCode
                    Log.d("POST Response", "Response Code: $responseCode")

                    // Check for redirects
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

                        val location = connection.getHeaderField("Location")
                        Log.d("Redirect", "Redirected to: $location")

                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "Server redirected to login page. Endpoint may require auth.",
                                Toast.LENGTH_LONG).show()
                        }
                        return@Thread
                    }

                    // Read response
                    val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream.bufferedReader().use { it.readText() }
                    }

                    // Log only first 500 chars to see what we get
                    val preview = if (response.length > 500) response.substring(0, 500) + "..." else response
                    Log.d("POST Response", "Preview: $preview")

                    // Check if response is HTML (contains <html> tag)
                    val isHtml = response.contains("<html", ignoreCase = true)
                    Log.d("ResponseType", "Is HTML: $isHtml")

                    runOnUiThread {
                        if (isHtml) {
                            Toast.makeText(this@MainActivity,
                                "Server returned HTML page. Check endpoint URL or authentication.",
                                Toast.LENGTH_LONG).show()
                        } else if (responseCode == 200) {
                            Toast.makeText(this@MainActivity, "Success!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity,
                                "Error: $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("POST Error", e.toString(), e)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()


            val url = "https://ephonelocker.info/api/save-firebase-token?token=$token&imei=$deviceId"

            Log.d("RequestURL", "URL: $url")

            Thread {
                try {
                    val urlObj = URL(url)
                    val connection = urlObj.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"

                    // Debug: Print all response headers
                    connection.setRequestProperty("User-Agent", "Android-App")
                    connection.setRequestProperty("Accept", "application/json") // Request JSON

                    // Try to follow redirects
                    connection.instanceFollowRedirects = false // Disable to see redirects

                    val responseCode = connection.responseCode
                    Log.d("POST Response", "Response Code: $responseCode")

                    // Check for redirects
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER) {

                        val location = connection.getHeaderField("Location")
                        Log.d("Redirect", "Redirected to: $location")

                        runOnUiThread {
                            Toast.makeText(this@MainActivity,
                                "Server redirected to login page. Endpoint may require auth.",
                                Toast.LENGTH_LONG).show()
                        }
                        return@Thread
                    }

                    // Read response
                    val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream.bufferedReader().use { it.readText() }
                    }

                    // Log only first 500 chars to see what we get
                    val preview = if (response.length > 500) response.substring(0, 500) + "..." else response
                    Log.d("POST Response", "Preview: $preview")

                    // Check if response is HTML (contains <html> tag)
                    val isHtml = response.contains("<html", ignoreCase = true)
                    Log.d("ResponseType", "Is HTML: $isHtml")

                    runOnUiThread {
                        if (isHtml) {
                            Toast.makeText(this@MainActivity,
                                "Server returned HTML page. Check endpoint URL or authentication.",
                                Toast.LENGTH_LONG).show()
                        } else if (responseCode == 200) {
                            Toast.makeText(this@MainActivity, "Success!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity,
                                "Error: $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("POST Error", e.toString(), e)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity,
                            "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
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
            Toast.makeText(this, "Touch locked for Pending Payment. Please pay us.\nBksah : 0188XXXXXXXXX \n Thanks", Toast.LENGTH_SHORT).show()

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
        status.append(if (isTouchLocked) "🔒 Touch locked for Pending Payment. Please pay us.\nBksah : 0188XXXXXXXXX \n Thanks\n" else "✅ Touch Ready\n")

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