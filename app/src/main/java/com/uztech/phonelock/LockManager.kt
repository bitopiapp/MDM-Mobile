package com.uztech.phonelock

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.os.Vibrator
import android.os.VibrationEffect
import android.widget.FrameLayout
import android.widget.TextView

class LockManager(
    private val context: Context,
    private val windowManager: WindowManager,
    private val vibrator: Vibrator
) {

    companion object {
        private const val TAG = "LockManager"
    }

    private var touchBlockerView: View? = null
    private var isLocked = false

    fun lockTouchScreen(): Boolean {
        if (isLocked) {
            Log.w(TAG, "Screen already locked")
            return false
        }

        try {
            createTouchBlockerOverlay()
            vibrate(200)
            isLocked = true
            Log.d(TAG, "✅ Screen locked successfully")
            return true

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Permission denied for overlay")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to lock screen: ${e.message}")
            throw e
        }
    }

    fun unlockTouchScreen(): Boolean {
        if (!isLocked) {
            Log.w(TAG, "Screen already unlocked")
            return false
        }

        try {
            removeTouchBlockerOverlay()
            vibrate(100)
            isLocked = false
            Log.d(TAG, "✅ Screen unlocked successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to unlock screen: ${e.message}")
            throw e
        }
    }

    fun isScreenLocked(): Boolean = isLocked

    private fun createTouchBlockerOverlay() {
        // Create a FrameLayout as the main overlay view
        touchBlockerView = FrameLayout(context).apply {
            setBackgroundColor(Color.argb(200, 0, 0, 0)) // Semi-transparent black
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
            setOnTouchListener { _, _ -> true } // Block all touches

            // Add a warning message TextView
            val textView = TextView(context).apply {
                text = "🔒 Screen Locked\nTouch locked for Pending Payment. Please pay us.\nBksah : 0188XXXXXXXXX \n Thanks"
                setTextColor(Color.WHITE)
                textSize = 24f
                gravity = Gravity.CENTER
            }

            // Add TextView to FrameLayout
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            addView(textView, layoutParams)
        }

        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.START or Gravity.TOP

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        windowManager.addView(touchBlockerView, params)
    }

    private fun removeTouchBlockerOverlay() {
        try {
            touchBlockerView?.let {
                if (it.parent != null) {
                    windowManager.removeView(it)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay: ${e.message}")
        } finally {
            touchBlockerView = null
        }
    }

    private fun vibrate(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration,
                        VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed")
        }
    }
}