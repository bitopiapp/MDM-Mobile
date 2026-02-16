package com.uztech.phonelock

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adminComponent: ComponentName
    private lateinit var dpm: DevicePolicyManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var sharedPref: SharedPreferences
    private lateinit var vibrator: Vibrator
    private lateinit var windowManager: WindowManager


    private val handler = Handler(Looper.getMainLooper())
    private var isTouchLocked = false
    private var touchLockStartTime: Long = 0

    companion object {
        const val REQUEST_CODE_ENABLE_ADMIN = 100
        const val REQUEST_CODE_ENABLE_DEVICE_OWNER = 101
        const val PREFS_NAME = "PhoneLockPrefs"
        const val KEY_FACTORY_RESET_DISABLED = "factory_reset_disabled"
        const val OVERLAY_PERMISSION_REQUEST = 102

        private const val FCM_LOG_TAG = "FCM_MAIN"
        private const val FRP_GOOGLE_ACCOUNT = "uzzal.biswas.cse@gmail.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "MainActivity started")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        adminComponent = componentName
        dpm = devicePolicyManager

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref = prefs

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager


        val btnGetFcmToken = findViewById<Button>(R.id.btnGetFcmToken)
        btnGetFcmToken.setOnClickListener {
            Log.d(FCM_LOG_TAG, "User clicked: Get FCM Token")
            getAndDisplayFCMToken()
        }

        // Hide button if token already exists
        if (getStoredToken() != null) {
            btnGetFcmToken.hide()
        }



//        findViewById<Button>(R.id.disableFactoryReset).setOnClickListener {
//            setFactoryReset(false)
//        }
//
//        findViewById<Button>(R.id.enableFactoryReset).setOnClickListener {
//            setFactoryReset(true)
//        }
//
//        findViewById<Button>(R.id.lockPhone).setOnClickListener {
//            saveLockState(true)
//            enableKioskMode()
//        }

//        findViewById<Button>(R.id.unlockPhone).setOnClickListener {
//            saveLockState(false)
//            disableKioskMode()
//        }

        findViewById<Button>(R.id.permissionforChrom).setOnClickListener {
            openChromeOnly()
        }

        startForegroundServiceForFCM()
        handleFCMNotification()
        checkFCMStatus()
        checkAndRestoreLockState()
    }






    // ==============================================
    // Chrome open
    // ==============================================

    private fun openChromeOnly() {
        try {
            val chromePackage = "com.android.chrome"
            val chromeIntent = packageManager.getLaunchIntentForPackage(chromePackage)

            if (chromeIntent != null) {
                chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(chromeIntent)
            } else {
                Toast.makeText(this, "Chrome not installed", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open Chrome", Toast.LENGTH_SHORT).show()
        }
    }


    fun Button.hide() {
        this.visibility = View.GONE
    }

    fun Button.show() {
        this.visibility = View.VISIBLE
    }

    private fun checkAndRestoreLockState() {
        val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)

        if (wasLocked) {
            handler.postDelayed({
                saveLockState(true)
                enableKioskMode()
            }, 2000)
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
    // Firebase Notification Handler
    // ==============================================

    private fun handleFCMNotification() {
        val title = intent?.getStringExtra("title")
        val body = intent?.getStringExtra("body")

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "Checking FCM notification...")
        Log.d(FCM_LOG_TAG, "Title: $title")
        Log.d(FCM_LOG_TAG, "Body: $body")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        if (body != null) {
            checkBodyForCommands(body, title)
        }
    }

    private fun checkBodyForCommands(body: String, title: String?) {
        Log.d(FCM_LOG_TAG, "Analyzing notification: $body  $title")

        val lowerBody = body.lowercase(Locale.getDefault())

        when {
            lowerBody.contains("active device") -> {
                Log.d(FCM_LOG_TAG, "INACTIVE command found - unlocking screen")
                handler.postDelayed({
                    saveLockState(false)
                    disableKioskMode()
                //    findViewById<Button>(R.id.disableFactoryReset).hide()
//                    findViewById<Button>(R.id.unlockBtn).hide()
                    findViewById<Button>(R.id.btnGetFcmToken).hide()
                    Toast.makeText(this, "Screen unlocked: Account inactive", Toast.LENGTH_LONG).show()
                }, 1000)
            }

            lowerBody.contains("lock device") -> {
                Log.d(FCM_LOG_TAG, "ACTIVE command found - locking screen")
                handler.postDelayed({
                    saveLockState(true)
                    enableKioskMode()
                //    findViewById<Button>(R.id.disableFactoryReset).hide()
//                    findViewById<Button>(R.id.unlockBtn).hide()
                    findViewById<Button>(R.id.btnGetFcmToken).hide()
                }, 1000)
            }



            lowerBody.contains("factory reset disable") -> {
                Log.d(FCM_LOG_TAG, "PENDING command found - enabling factory reset")
                handler.postDelayed({
                    setFactoryReset(false)
//                    findViewById<Button>(R.id.unlockBtn).hide()
                    findViewById<Button>(R.id.btnGetFcmToken).hide()
                }, 1000)
            }

            lowerBody.contains("factory reset enable") -> {
                Log.d(FCM_LOG_TAG, "PENDING command found - enabling factory reset")
                handler.postDelayed({
                    setFactoryReset(true)
                 //   findViewById<Button>(R.id.disableFactoryReset).show()
//                    findViewById<Button>(R.id.unlockBtn).show()
//                    findViewById<Button>(R.id.btnGetFcmToken).show()
                }, 1000)
            }

            else -> {
                Log.d(FCM_LOG_TAG, "No lock/unlock command found")
                if (title != null) {
                    Toast.makeText(this, "Notification: $title", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ==============================================
    // Device Status to Server
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
                Log.d("StatusUpdate", "Response code: $responseCode")

                if (responseCode == 200) {
                    Log.d(FCM_LOG_TAG, "Status sent to server")
                }
            } catch (e: Exception) {
                Log.e("StatusUpdate", "Error: ${e.message}")
            }
        }.start()
    }

    private fun getDeviceStatus(): String {
        return StringBuilder().apply {
            append("Device Owner: ${if (isDeviceOwner()) "YES" else "NO"}\n")
            append("Device Admin: ${if (devicePolicyManager.isAdminActive(componentName)) "YES" else "NO"}\n")
            append("Screen Lock: ${if (isTouchLocked) "LOCKED" else "UNLOCKED"}\n")
            append("Overlay Permanent: ${if (prefs.getBoolean("overlay_permanent_enabled", false)) "YES" else "NO"}\n")
            append("Factory Reset: ${if (prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)) "LOCKED" else "UNLOCKED"}")
        }.toString()
    }

    // ==============================================
    // Foreground Service
    // ==============================================

    private fun startForegroundServiceForFCM() {
        try {
            if (!isForegroundServiceRunning()) {
                ForegroundNotificationService.startService(this)
                Log.d(FCM_LOG_TAG, "Foreground service started")
            } else {
                Log.d(FCM_LOG_TAG, "Foreground service already running")
            }
        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "Failed to start foreground service: ${e.message}")
        }
    }

    private fun isForegroundServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == ForegroundNotificationService::class.java.name }
    }

    // ==============================================
    // FCM Token Management
    // ==============================================

    private fun checkFCMStatus() {
        val token = getStoredToken()
        if (token != null) {
            Log.d(FCM_LOG_TAG, "Stored FCM token: ${token.take(20)}...")
            // Ensure button is hidden if token is already stored
            runOnUiThread {
                findViewById<Button>(R.id.btnGetFcmToken).hide()
            }
        } else {
            Log.d(FCM_LOG_TAG, "No FCM token stored")
        }
    }

    private fun getAndDisplayFCMToken() {
        Log.d(FCM_LOG_TAG, "Requesting FCM token from Firebase...")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                val error = task.exception?.message ?: "Unknown error"
                Log.e(FCM_LOG_TAG, "FCM token error: $error")

                val userMsg = when {
                    error.contains("AUTHENTICATION_FAILED") -> "Firebase setup issue"
                    error.contains("SERVICE_NOT_AVAILABLE") -> "Google Play Services required"
                    error.contains("NETWORK") -> "Internet connection required"
                    else -> "Token not available"
                }

                Toast.makeText(this, userMsg, Toast.LENGTH_LONG).show()
                return@OnCompleteListener
            }

            val token = task.result
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
            Log.d(FCM_LOG_TAG, "FCM token received successfully!")
            Log.d(FCM_LOG_TAG, "Token length: ${token.length} chars")
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

            println("\nCopy this token:")
            println(token)
            println("Token end\n")

            val deviceId = Settings.Secure.getString(contentResolver,
                Settings.Secure.ANDROID_ID) ?: "unknown"

            sendRegistrationData(deviceId, token)
            saveToken(token)

            // Hide the button on success
            findViewById<Button>(R.id.btnGetFcmToken).hide()

            Toast.makeText(
                this,
                "Token saved! See full token in Logcat",
                Toast.LENGTH_LONG
            ).show()

        })
    }

    private fun sendRegistrationData(deviceId: String, token: String) {
        Thread {
            try {
//                val registerUrl = "https://ephonelocker.info/api/register?imei_number=$deviceId&name=${Build.MANUFACTURER} ${Build.MODEL}&phone=01700000009&email=$deviceId@example.com&address=Dhaka, Bangladesh&nominee_name=Nominee Name&nominee_phone=01800000009&total_amount=50000&down_payment=10000&interval_type=1&interval_value=6&payable_amount=40000&per_installment=3333.33&bill_date=2025-01-15&admin_id=2"
//                Log.d("RequestURL", "Register URL: $registerUrl")
//
//                sendPostRequest(registerUrl)

                val registerUrl = "https://uztech.juimart.com/create-device?name=Employee - ${Build.MANUFACTURER}-$deviceId&adminId=0&deviceToken=$token"
                Log.d("RequestURL", "Register URL: $registerUrl")

                sendPostRequest(registerUrl)

//                val tokenUrl = "https://ephonelocker.info/api/save-firebase-token?token=$token&imei=$deviceId"
//                Log.d("RequestURL", "Token URL: $tokenUrl")
//
//                sendPostRequest(tokenUrl)

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
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Android-App")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            Log.d("POST Response", "Response code: $responseCode")

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
        Log.d(FCM_LOG_TAG, "Token saved: ${token.take(15)}...")
    }

    private fun getStoredToken(): String? {
        return prefs.getString("fcm_token", null)
    }

    private fun saveLockState(locked: Boolean) {
        isTouchLocked = locked
        sharedPref.edit().apply {
            putBoolean("isLocked", locked)
            putBoolean("was_locked_before_reboot", locked)
            apply()
        }
        Toast.makeText(this, if (locked) "Lock state saved" else "Unlock state saved",
            Toast.LENGTH_SHORT).show()
    }

    private fun enableKioskMode() {
        if (isDeviceOwner()) {
            try {
             //   dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
                dpm.setLockTaskPackages(adminComponent, arrayOf(packageName, "com.android.chrome"))
                startLockTask()
                isTouchLocked = true
                saveLockState(true)
                Toast.makeText(this, "Phone locked (Kiosk Mode)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("KIOSK", "Lock error: ${e.message}")
                Toast.makeText(this, "Kiosk mode error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Device Owner required", Toast.LENGTH_LONG).show()
        }
    }

    private fun disableKioskMode() {
        try {
            stopLockTask()
            isTouchLocked = false
            saveLockState(false)
            Toast.makeText(this, "Phone unlocked", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("KIOSK", "Unlock error: ${e.message}")
        }
    }


    private fun setFactoryReset(isEnabled: Boolean) {
        if (isDeviceOwner()) {
            val restrictions = arrayOf(
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_USB_FILE_TRANSFER,
                UserManager.DISALLOW_INSTALL_APPS,
                UserManager.DISALLOW_UNINSTALL_APPS,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_DEBUGGING_FEATURES
            )
            if (isEnabled) {
                for (r in restrictions) {
                    dpm.clearUserRestriction(adminComponent, r)
                }
                disableFRP()
                Toast.makeText(this, "All restrictions removed + FRP off", Toast.LENGTH_SHORT).show()
            } else {
                for (r in restrictions) {
                    dpm.addUserRestriction(adminComponent, r)
                }
                enableFRP()
                Toast.makeText(this, "All restrictions applied + FRP on", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Device Owner required",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun enableFRP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val policy = FactoryResetProtectionPolicy.Builder()
                    .setFactoryResetProtectionAccounts(listOf(FRP_GOOGLE_ACCOUNT))
                    .setFactoryResetProtectionEnabled(true)
                    .build()
                dpm.setFactoryResetProtectionPolicy(adminComponent, policy)
                Log.d("FRP", "FRP enabled with account: $FRP_GOOGLE_ACCOUNT")
            } catch (e: Exception) {
                Log.e("FRP", "FRP error: ${e.message}")
            }
        } else {
            Log.d("FRP", "FRP policy requires Android 11+")
        }
    }

    private fun disableFRP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                dpm.setFactoryResetProtectionPolicy(adminComponent, null)
                Log.d("FRP", "FRP disabled")
            } catch (e: Exception) {
                Log.e("FRP", "FRP disable error: ${e.message}")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Device Admin activated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Device Admin not activated", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "Other result", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val wasLocked = prefs.getBoolean("was_locked_before_reboot", false)
        if (wasLocked && !isTouchLocked) {
            handler.postDelayed({
                saveLockState(true)
                enableKioskMode()
            }, 1500)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(FCM_LOG_TAG, "onNewIntent called")
        handleFCMNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}