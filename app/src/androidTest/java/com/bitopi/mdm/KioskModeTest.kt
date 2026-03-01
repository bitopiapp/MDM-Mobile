package com.bitopi.mdm

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KioskModeTest {

    private lateinit var ctx: Context
    private lateinit var dpm: DevicePolicyManager

    @Before
    fun setup() {
        ctx = InstrumentationRegistry.getInstrumentation().targetContext
        dpm = ctx.getSystemService(DevicePolicyManager::class.java)

        Assume.assumeTrue(
            "Device Owner required for kiosk tests — run: adb shell dpm set-device-owner com.bitopi.mdm/.DeviceAdminReceiver",
            dpm.isDeviceOwnerApp("com.bitopi.mdm")
        )

        // Clear lock state before each test
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("was_locked_before_reboot", false)
            .putBoolean("isLocked", false)
            .commit()
    }

    @Test
    fun testLockCommandEnablesKioskMode() {
        val lockIntent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("body", "lock device")
        }

        val scenario = ActivityScenario.launch<MainActivity>(lockIntent)

        // Wait for the 1-second handler delay in checkBodyForCommands
        Thread.sleep(2500)

        val lockState = dpm.getLockTaskModeState()
        assertNotEquals(
            "Kiosk mode should be active after 'lock device' command",
            DevicePolicyManager.LOCK_TASK_MODE_NONE,
            lockState
        )

        // Stop lock task to clean up
        scenario.onActivity { it.stopLockTask() }
        Thread.sleep(500)
        scenario.close()
    }

    @Test
    fun testUnlockCommandExitsKioskMode() {
        // Step 1: Lock the device
        val lockIntent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("body", "lock device")
        }
        val scenario = ActivityScenario.launch<MainActivity>(lockIntent)
        Thread.sleep(2500)

        // Verify locked
        assertNotEquals(
            "Should be in kiosk mode before unlock test",
            DevicePolicyManager.LOCK_TASK_MODE_NONE,
            dpm.getLockTaskModeState()
        )

        // Step 2: Send unlock via onNewIntent
        val unlockIntent = Intent().apply { putExtra("body", "active device") }
        scenario.onActivity { activity -> activity.onNewIntent(unlockIntent) }
        Thread.sleep(2500)

        assertEquals(
            "Kiosk mode should be off after 'active device' command",
            DevicePolicyManager.LOCK_TASK_MODE_NONE,
            dpm.getLockTaskModeState()
        )

        scenario.close()
    }

    @Test
    fun testFactoryResetRestrictionsApplied() {
        val restrictIntent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("body", "factory reset disable")
        }
        val scenario = ActivityScenario.launch<MainActivity>(restrictIntent)
        Thread.sleep(2500)

        val adminComponent = android.content.ComponentName(ctx, DeviceAdminReceiver::class.java)
        val restrictions = dpm.getUserRestrictions(adminComponent)

        assert(restrictions.getBoolean(android.os.UserManager.DISALLOW_FACTORY_RESET, false)) {
            "DISALLOW_FACTORY_RESET restriction should be active"
        }

        scenario.close()

        // Cleanup — remove restrictions
        val cleanupIntent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("body", "factory reset enable")
        }
        val cleanupScenario = ActivityScenario.launch<MainActivity>(cleanupIntent)
        Thread.sleep(2500)
        cleanupScenario.close()
    }
}
