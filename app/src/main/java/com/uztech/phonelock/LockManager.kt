package com.uztech.phonelock

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.view.*
import android.util.Log

class LockManager(
    private val context: Context,
    private val windowManager: WindowManager,
    private val vibrator: Vibrator
) {
    companion object {
        const val TAG = "LockManager"
    }

    private var overlayView: View? = null

    fun lockTouchScreen(): Boolean {
        try {
            // Remove existing view if any
            unlockTouchScreen()

            // Create overlay view
            overlayView = createOverlayView()

            // Add view to window manager
            windowManager.addView(overlayView, createLayoutParams())

            Log.d(TAG, "Touch screen locked successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock touch screen: ${e.message}")
            return false
        }
    }

    fun unlockTouchScreen(): Boolean {
        try {
            overlayView?.let {
                windowManager.removeView(it)
                overlayView = null
            }
            Log.d(TAG, "Touch screen unlocked successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlock touch screen: ${e.message}")
            return false
        }
    }

    fun isLocked(): Boolean = overlayView != null

    private fun createOverlayView(): View {
        return View(context).apply {
            setBackgroundColor(0x00000000) // Transparent
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true

            // Intercept all touch events
            setOnTouchListener { v, event ->
                // Consume all touch events
                true
            }

            // Request focus
            requestFocus()
        }
    }

    @Suppress("DEPRECATION")
    private fun createLayoutParams(): WindowManager.LayoutParams {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
            )
        }
    }
}