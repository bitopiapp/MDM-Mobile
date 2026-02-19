package com.uztech.phonelock

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

class LockActivity : ComponentActivity() {

    companion object {
        private const val CORRECT_PIN = "1234"
        private const val PREFS = "lock_prefs"
        private const val KEY_LOCKED = "locked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_lock)

        val pinInput = findViewById<EditText>(R.id.pinInput)
        val unlockBtn = findViewById<Button>(R.id.unlockBtn)
        val errorText = findViewById<TextView>(R.id.errorText)

        unlockBtn.setOnClickListener {
            val entered = pinInput.text?.toString()?.trim() ?: ""
            if (entered == CORRECT_PIN) {
                clearLockedFlag()
                requestDismissKeyguard()
                finish()
            } else {
                errorText.text = getString(R.string.incorrect_pin)
                pinInput.text?.clear()
            }
        }
    }

    private fun clearLockedFlag() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LOCKED, false)
            .apply()
    }

    private fun requestDismissKeyguard() {
        val km = getSystemService(KeyguardManager::class.java)
        km?.requestDismissKeyguard(this, null)
    }
}
