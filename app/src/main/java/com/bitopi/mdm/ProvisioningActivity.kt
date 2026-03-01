package com.bitopi.mdm

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle

/**
 * Handles Android 10+ (API 29+) provisioning intents required for Device Owner setup via QR.
 *
 * The setup wizard calls two intents during provisioning:
 *  1. ACTION_GET_PROVISIONING_MODE  — asks the DPC what mode to use.
 *  2. ACTION_ADMIN_POLICY_COMPLIANCE — signals that policies have been applied; DPC must accept.
 *
 * Without these handlers, QR provisioning silently fails on Android 10+ devices.
 */
class ProvisioningActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            DevicePolicyManager.ACTION_GET_PROVISIONING_MODE -> handleGetProvisioningMode()
            DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE -> handleAdminPolicyCompliance()
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun handleGetProvisioningMode() {
        val result = Intent().apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
            )
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun handleAdminPolicyCompliance() {
        setResult(RESULT_OK)
        finish()
    }
}
