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
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var adminComponent: ComponentName
    private lateinit var dpm: DevicePolicyManager
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var sharedPref: SharedPreferences  // ✅ এটা ব্যবহৃত হবে
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

        // FCM লগ ট্যাগ
        private const val FCM_LOG_TAG = "FCM_MAIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "📱 MainActivity শুরু হয়েছে")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        // ✅ সব ভ্যারিয়েবল ইনিশিয়ালাইজ করা
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, DeviceAdminReceiver::class.java)
        adminComponent = componentName  // ✅ এইটা যোগ করা হয়েছে
        dpm = devicePolicyManager  // ✅ এইটা যোগ করা হয়েছে

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref = prefs  // ✅ একই SharedPreferences ব্যবহার করছি

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager


        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnGetFcmToken).setOnClickListener {
            Log.d(FCM_LOG_TAG, "ইউজার ক্লিক করেছেন: Get FCM Token")
            getAndDisplayFCMToken()
        }



        // ৪. ফ্যাক্টরি রিসেট কন্ট্রোল
        findViewById<Button>(R.id.disableFactoryReset).setOnClickListener {
            setFactoryReset(false)
        }

        // 5. ফ্যাক্টরি রিসেট কন্ট্রোল
        findViewById<Button>(R.id.enableFactoryReset).setOnClickListener {
            setFactoryReset(true)
        }

        // 5. ফ্যাক্টরি রিসেট কন্ট্রোল
        findViewById<Button>(R.id.lockFactoryReset).setOnClickListener {
            saveLockState(true)
            enableKioskMode()
        }

        // 5. ফ্যাক্টরি রিসেট কন্ট্রোল
        findViewById<Button>(R.id.unlockFactoryReset).setOnClickListener {
            saveLockState(false)
            disableKioskMode()
        }

        // 6. open chrome
        findViewById<Button>(R.id.permissionforChrom).setOnClickListener {
            openChromeOnly()
        }

        // ফোরগ্রাউন্ড সার্ভিস শুরু করা
        startForegroundServiceForFCM()

        // FCM নোটিফিকেশন হ্যান্ডেল করা
        handleFCMNotification()

        // স্বয়ংক্রিয় চেক করা
        checkFCMStatus()

        // রিবুটের পর লক স্টেট চেক করা
        checkAndRestoreLockState()
    }


    // ==============================================
// ✅ শুধু Chrome ওপেন করা
// ==============================================

    private fun openChromeOnly() {
        try {
            // Chrome এর প্যাকেজ নাম
            val chromePackage = "com.example.counter_app"

            // Chrome লঞ্চ করার Intent পাওয়ার চেষ্টা
            val chromeIntent = packageManager.getLaunchIntentForPackage(chromePackage)

            if (chromeIntent != null) {
                // Chrome ওপেন করা
                chromeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(chromeIntent)
            } else {
                // Chrome ইনস্টল নেই
                Toast.makeText(
                    this,
                    "❌ Chrome ব্রাউজার ইনস্টল নেই",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            // কোনো ত্রুটি হলে
            Toast.makeText(
                this,
                "❌ Chrome ওপেন করা যায়নি",
                Toast.LENGTH_SHORT
            ).show()
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
    // ✅ Firebase থেকে নোটিফিকেশন হ্যান্ডেল করা
    // ==============================================

    private fun handleFCMNotification() {
        val title = intent?.getStringExtra("title")
        val body = intent?.getStringExtra("body")

        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
        Log.d(FCM_LOG_TAG, "🔍 FCM নোটিফিকেশন চেক করা হচ্ছে...")
        Log.d(FCM_LOG_TAG, "শিরোনাম: $title")
        Log.d(FCM_LOG_TAG, "বিস্তারিত: $body")
        Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

        if (body != null) {
            checkBodyForCommands(body, title)
        }
    }

    private fun checkBodyForCommands(body: String, title: String?) {
        Log.d(FCM_LOG_TAG, "📝 নোটিফিকেশন বিশ্লেষণ করা হচ্ছে: $body  $title")

        val lowerBody = body.lowercase(Locale.getDefault())

        when {
            lowerBody.contains("account status is now active") -> {
                Log.d(FCM_LOG_TAG, "✅ ACTIVE কমান্ড পাওয়া গেছে - স্ক্রীন লক করা হবে")
                handler.postDelayed({
                //    Toast.makeText(this, "🔒 স্ক্রীন লক করা হয়েছে: অ্যাকাউন্ট একটিভ", Toast.LENGTH_LONG).show()
                    saveLockState(true)
                    enableKioskMode()
                    findViewById<Button>(R.id.disableFactoryReset).hide()
                    findViewById<Button>(R.id.btnGetFcmToken).hide()
                }, 1000)
            }

            lowerBody.contains("account status is now inactive") -> {
                Log.d(FCM_LOG_TAG, "✅ INACTIVE কমান্ড পাওয়া গেছে - স্ক্রীন আনলক করা হবে")
                handler.postDelayed({
                    saveLockState(false)
                    disableKioskMode()
                    findViewById<Button>(R.id.disableFactoryReset).hide()
                    findViewById<Button>(R.id.btnGetFcmToken).hide()
                    Toast.makeText(this, "🔓 স্ক্রীন আনলক করা হয়েছে: অ্যাকাউন্ট ইনএকটিভ", Toast.LENGTH_LONG).show()
                }, 1000)
            }

            lowerBody.contains("account status is now pending") -> {
                Log.d(FCM_LOG_TAG, "✅ PENDING কমান্ড পাওয়া গেছে - ফ্যাক্টরি রিসেট চালু করা হবে")
                handler.postDelayed({
                    setFactoryReset(true)
                    findViewById<Button>(R.id.disableFactoryReset).show()
                    findViewById<Button>(R.id.btnGetFcmToken).show()
                }, 1000)
            }

            lowerBody.contains("account status is now pending") -> {
                Log.d(FCM_LOG_TAG, "✅ PENDING কমান্ড পাওয়া গেছে - ফ্যাক্টরি রিসেট চালু করা হবে")
                handler.postDelayed({
                    setFactoryReset(true)
                }, 1000)
            }

            else -> {
                Log.d(FCM_LOG_TAG, "ℹ️ লক/আনলক কমান্ড পাওয়া যায়নি")
                if (title != null) {
                    Toast.makeText(this, "নোটিফিকেশন: $title", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ==============================================
    // ✅ ডিভাইস স্ট্যাটাস সার্ভারে পাঠানো
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
                Log.d("StatusUpdate", "রেসপন্স কোড: $responseCode")

                if (responseCode == 200) {
                    Log.d(FCM_LOG_TAG, "✅ স্ট্যাটাস সার্ভারে পাঠানো হয়েছে")
                }
            } catch (e: Exception) {
                Log.e("StatusUpdate", "ত্রুটি: ${e.message}")
            }
        }.start()
    }

    private fun getDeviceStatus(): String {
        return StringBuilder().apply {
            append("ডিভাইস ওনার: ${if (isDeviceOwner()) "✅" else "❌"}\n")
            append("ডিভাইস অ্যাডমিন: ${if (devicePolicyManager.isAdminActive(componentName)) "✅" else "❌"}\n")
            append("স্ক্রীন লক: ${if (isTouchLocked) "🔒" else "🔓"}\n")
            append("ওভারলে পারমেনেন্ট: ${if (prefs.getBoolean("overlay_permanent_enabled", false)) "✅" else "❌"}\n")
            append("ফ্যাক্টরি রিসেট: ${if (prefs.getBoolean(KEY_FACTORY_RESET_DISABLED, false)) "🔒" else "🔓"}")
        }.toString()
    }

    // ==============================================
    // ✅ ফোরগ্রাউন্ড সার্ভিস
    // ==============================================

    private fun startForegroundServiceForFCM() {
        try {
            if (!isForegroundServiceRunning()) {
                ForegroundNotificationService.startService(this)
                Log.d(FCM_LOG_TAG, "🚀 ফোরগ্রাউন্ড সার্ভিস শুরু করা হয়েছে")
            } else {
                Log.d(FCM_LOG_TAG, "✅ ফোরগ্রাউন্ড সার্ভিস ইতিমধ্যে চলছে")
            }
        } catch (e: Exception) {
            Log.e(FCM_LOG_TAG, "❌ ফোরগ্রাউন্ড সার্ভিস শুরু করতে ব্যর্থ: ${e.message}")
        }
    }

    private fun isForegroundServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == ForegroundNotificationService::class.java.name }
    }

    // ==============================================
    // FCM টোকেন ম্যানেজমেন্ট
    // ==============================================

    private fun checkFCMStatus() {
        val token = getStoredToken()
        if (token != null) {
            Log.d(FCM_LOG_TAG, "✅ সংরক্ষিত FCM টোকেন: ${token.take(20)}...")
        } else {
            Log.d(FCM_LOG_TAG, "❌ কোন FCM টোকেন সংরক্ষিত নেই")
        }
    }

    private fun getAndDisplayFCMToken() {
        Log.d(FCM_LOG_TAG, "🔄 Firebase থেকে FCM টোকেন রিকোয়েস্ট করা হচ্ছে...")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                val error = task.exception?.message ?: "অজানা ত্রুটি"
                Log.e(FCM_LOG_TAG, "❌ FCM টোকেন ত্রুটি: $error")

                val userMsg = when {
                    error.contains("AUTHENTICATION_FAILED") -> "Firebase সেটআপ সমস্যা"
                    error.contains("SERVICE_NOT_AVAILABLE") -> "Google Play সার্ভিস প্রয়োজন"
                    error.contains("NETWORK") -> "ইন্টারনেট কানেকশন প্রয়োজন"
                    else -> "টোকেন পাওয়া যায়নি"
                }

                Toast.makeText(this, userMsg, Toast.LENGTH_LONG).show()
                return@OnCompleteListener
            }

            val token = task.result
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")
            Log.d(FCM_LOG_TAG, "✅ FCM টোকেন সফলভাবে পাওয়া গেছে!")
            Log.d(FCM_LOG_TAG, "টোকেন দৈর্ঘ্য: ${token.length} অক্ষর")
            Log.d(FCM_LOG_TAG, "══════════════════════════════════════")

            // কপির জন্য টোকেন প্রিন্ট করা
            println("\n🎯 এই টোকেনটি কপি করুন 🎯")
            println(token)
            println("🎯 টোকেন শেষ 🎯\n")

            // ডিভাইস আইডি পাওয়া
            val deviceId = Settings.Secure.getString(contentResolver,
                Settings.Secure.ANDROID_ID) ?: "unknown"

            // ডিভাইস রেজিস্টার করা (ব্যাকগ্রাউন্ডে)
            sendRegistrationData(deviceId, token)

            // টোকেন লোকালি সেভ করা
            saveToken(token)

            Toast.makeText(
                this,
                "টোকেন সেভ করা হয়েছে! সম্পূর্ণ টোকেন Logcat এ দেখুন",
                Toast.LENGTH_LONG
            ).show()

        })
    }

    private fun sendRegistrationData(deviceId: String, token: String) {
        Thread {
            try {
                // ডিভাইস রেজিস্টার করা
                val registerUrl = "https://ephonelocker.info/api/register?imei_number=$deviceId&name=${Build.MANUFACTURER} ${Build.MODEL}&phone=01700000009&email=$deviceId@example.com&address=Dhaka, Bangladesh&nominee_name=Nominee Name&nominee_phone=01800000009&total_amount=50000&down_payment=10000&interval_type=1&interval_value=6&payable_amount=40000&per_installment=3333.33&bill_date=2025-01-15&admin_id=2"
                Log.d("RequestURL", "রেজিস্টার URL: $registerUrl")

                sendPostRequest(registerUrl)

                // FCM টোকেন সার্ভারে সেভ করা
                val tokenUrl = "https://ephonelocker.info/api/save-firebase-token?token=$token&imei=$deviceId"
                Log.d("RequestURL", "টোকেন URL: $tokenUrl")

                sendPostRequest(tokenUrl)

            } catch (e: Exception) {
                Log.e("Registration", "ত্রুটি: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity,
                        "রেজিস্টারেশন ত্রুটি: ${e.message}", Toast.LENGTH_SHORT).show()
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
            Log.d("POST Response", "রেসপন্স কোড: $responseCode")

            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "কোন রেসপন্স নেই"
            }

            Log.d("POST Response", "রেসপন্স: ${if (response.length > 200) response.substring(0, 200) + "..." else response}")

            runOnUiThread {
                if (responseCode == 200) {
                    Toast.makeText(this, "সার্ভার রিকোয়েস্ট সফল", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "সার্ভার রিটার্ন: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e("POST Error", e.toString())
            runOnUiThread {
                Toast.makeText(this, "ত্রুটি: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToken(token: String) {
        prefs.edit().apply {
            putString("fcm_token", token)
            putLong("token_time", System.currentTimeMillis())
            apply()
        }
        Log.d(FCM_LOG_TAG, "💾 টোকেন সেভ করা হয়েছে: ${token.take(15)}...")
    }

    private fun getStoredToken(): String? {
        return prefs.getString("fcm_token", null)
    }

    private fun saveLockState(locked: Boolean) {
        isTouchLocked = locked
        sharedPref.edit().apply {
            putBoolean("isLocked", locked)
            putBoolean("was_locked_before_reboot", locked)  // ✅ রিবুটের জন্য সেভ করা
            apply()
        }
        Toast.makeText(this, if (locked) "🔒 লক স্টেট সেভ করা হয়েছে" else "🔓 আনলক স্টেট সেভ করা হয়েছে",
            Toast.LENGTH_SHORT).show()
    }

    private fun enableKioskMode() {
        if (isDeviceOwner()) {
            try {
                dpm.setLockTaskPackages(adminComponent, arrayOf(packageName))
                startLockTask()
                isTouchLocked = true
                saveLockState(true)
                Toast.makeText(this, "🔒 ফোন লক করা হয়েছে (কিওস্ক মোড)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("KIOSK", "লক ত্রুটি: ${e.message}")
                Toast.makeText(this, "কিওস্ক মোডে ত্রুটি: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "❌ ডিভাইস ওনার প্রয়োজন", Toast.LENGTH_LONG).show()

        }
    }

    private fun disableKioskMode() {
        try {
            stopLockTask()
            isTouchLocked = false
            saveLockState(false)
            Toast.makeText(this, "🔓 ফোন আনলক হয়েছে", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("KIOSK", "আনলক ত্রুটি: ${e.message}")
        }
    }


    private fun setFactoryReset(isEnabled: Boolean) {
        if (isDeviceOwner()) {
            if (isEnabled) {
                dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET)
                Toast.makeText(this, "✅ ফ্যাক্টরি রিসেট চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET)
                Toast.makeText(this, "🚫 ফ্যাক্টরি রিসেট বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "❌ ফ্যাক্টরি রিসেট কন্ট্রোলের জন্য ডিভাইস ওনার প্রয়োজন",
                Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_ENABLE_ADMIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "✅ ডিভাইস অ্যাডমিন সক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ ডিভাইস অ্যাডমিন সক্রিয় করা হয়নি", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "অন্যান্য রেজাল্ট", Toast.LENGTH_SHORT).show()
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
        Log.d(FCM_LOG_TAG, "🔄 onNewIntent কল হয়েছে")
        handleFCMNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}