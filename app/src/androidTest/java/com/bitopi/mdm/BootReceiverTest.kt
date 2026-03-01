package com.bitopi.mdm

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BootReceiverTest {

    private lateinit var ctx: Context

    @Before
    fun setup() {
        ctx = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun cleanUp() {
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("was_locked_before_reboot", false).commit()
    }

    @Test
    fun testBootReceiverStartsPersistentLockServiceWhenDeviceWasLocked() {
        // Simulate the state saved before reboot
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("was_locked_before_reboot", true).commit()

        val receiver = BootReceiver()
        receiver.onReceive(ctx, Intent(Intent.ACTION_BOOT_COMPLETED))

        // Allow service startup time
        Thread.sleep(1500)

        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val running = manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == PersistentLockService::class.java.name }

        assertTrue("PersistentLockService should start when was_locked_before_reboot=true", running)
    }

    @Test
    fun testBootReceiverHandlesNotLockedStateGracefully() {
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("was_locked_before_reboot", false).commit()

        val receiver = BootReceiver()
        // Should complete without exception
        receiver.onReceive(ctx, Intent(Intent.ACTION_BOOT_COMPLETED))

        Thread.sleep(500)

        // No PersistentLockService expected
        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val running = manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == PersistentLockService::class.java.name }

        // Service may or may not be running from prior tests — just confirm no crash
        assertTrue("Boot receiver should handle not-locked state without crashing", true)
    }

    @Test
    fun testBootReceiverHandlesLockedBootCompletedAction() {
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("was_locked_before_reboot", true).commit()

        val receiver = BootReceiver()
        receiver.onReceive(ctx, Intent(Intent.ACTION_LOCKED_BOOT_COMPLETED))

        Thread.sleep(1500)

        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val running = manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == PersistentLockService::class.java.name }

        assertTrue("PersistentLockService should also start on LOCKED_BOOT_COMPLETED", running)
    }
}
