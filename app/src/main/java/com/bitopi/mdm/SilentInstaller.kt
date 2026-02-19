package com.bitopi.mdm

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class SilentInstaller(private val context: Context) {

    companion object {
        private const val TAG = "SilentInstaller"
        private const val ACTION_INSTALL_COMPLETE = "com.bitopi.mdm.INSTALL_COMPLETE"
        private const val APK_DIR = "apk_downloads"
        private const val DOWNLOAD_TIMEOUT_SECONDS = 120L
    }

    interface InstallCallback {
        fun onDownloadStarted()
        fun onDownloadProgress(percent: Int)
        fun onDownloadComplete()
        fun onInstallStarted()
        fun onInstallSuccess(packageName: String?)
        fun onInstallFailed(error: String)
    }

    fun downloadAndInstall(apkUrl: String, callback: InstallCallback?) {
        Thread {
            try {
                callback?.onDownloadStarted()
                Log.d(TAG, "Downloading APK from: $apkUrl")

                // Step 1: Download APK
                val apkFile = downloadApk(apkUrl, callback)
                if (apkFile == null || !apkFile.exists()) {
                    callback?.onInstallFailed("Download failed")
                    return@Thread
                }

                callback?.onDownloadComplete()
                Log.d(TAG, "APK downloaded: ${apkFile.absolutePath} (${apkFile.length()} bytes)")

                // Step 2: Install silently
                callback?.onInstallStarted()
                silentInstall(apkFile, callback)

            } catch (e: Exception) {
                Log.e(TAG, "Download/Install error: ${e.message}")
                callback?.onInstallFailed(e.message ?: "Unknown error")
            }
        }.start()
    }

    private fun downloadApk(url: String, callback: InstallCallback?): File? {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Download failed: HTTP ${response.code}")
                return null
            }

            val body = response.body ?: return null
            val contentLength = body.contentLength()

            // Create download directory
            val downloadDir = File(context.cacheDir, APK_DIR)
            if (!downloadDir.exists()) downloadDir.mkdirs()

            // Clean old APKs
            downloadDir.listFiles()?.forEach { it.delete() }

            val apkFile = File(downloadDir, "install_${System.currentTimeMillis()}.apk")

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var totalRead = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        if (contentLength > 0) {
                            val percent = ((totalRead * 100) / contentLength).toInt()
                            callback?.onDownloadProgress(percent)
                        }
                    }
                    output.flush()
                }
            }

            return apkFile

        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}")
            return null
        }
    }

    private fun silentInstall(apkFile: File, callback: InstallCallback?) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val isDeviceOwner = dpm.isDeviceOwnerApp(context.packageName)

            if (!isDeviceOwner) {
                Log.e(TAG, "Not device owner — cannot silent install")
                callback?.onInstallFailed("Device Owner required for silent install")
                return
            }

            val packageInstaller = context.packageManager.packageInstaller

            // Create install session
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            ).apply {
                setInstallReason(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            }

            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            // Write APK to session
            apkFile.inputStream().use { apkInput ->
                session.openWrite("app_install", 0, apkFile.length()).use { sessionOutput ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (apkInput.read(buffer).also { bytesRead = it } != -1) {
                        sessionOutput.write(buffer, 0, bytesRead)
                    }
                    session.fsync(sessionOutput)
                }
            }

            // Register result receiver
            registerInstallReceiver(callback)

            // Commit session — triggers install
            val intent = Intent(ACTION_INSTALL_COMPLETE).apply {
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            session.commit(pendingIntent.intentSender)
            Log.d(TAG, "Install session committed (sessionId=$sessionId)")

        } catch (e: Exception) {
            Log.e(TAG, "Silent install error: ${e.message}")
            callback?.onInstallFailed(e.message ?: "Install failed")
        }
    }

    private fun registerInstallReceiver(callback: InstallCallback?) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val status = intent.getIntExtra(
                    PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE
                )
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

                when (status) {
                    PackageInstaller.STATUS_SUCCESS -> {
                        Log.d(TAG, "Install SUCCESS: $packageName")
                        callback?.onInstallSuccess(packageName)
                    }
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        Log.d(TAG, "Install needs user action (not device owner?)")
                        callback?.onInstallFailed("User action required — device owner not active")
                    }
                    else -> {
                        Log.e(TAG, "Install FAILED: status=$status msg=$message")
                        callback?.onInstallFailed("Install failed: $message (code=$status)")
                    }
                }

                try {
                    context.unregisterReceiver(this)
                } catch (_: Exception) {}
            }
        }

        val filter = IntentFilter(ACTION_INSTALL_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }
}
