package com.bitopi.mdm

import android.app.ActivityManager
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.not
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun clearPrefs() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove("fcm_token").remove("was_locked_before_reboot").commit()
    }

    @Test
    fun testAppLaunchShowsCorrectTitle() {
        onView(withText("Welcome to PPC")).check(matches(isDisplayed()))
    }

    @Test
    fun testSubtitleVisible() {
        onView(withText("BITOPI - MDM - ACTIVITED")).check(matches(isDisplayed()))
    }

    @Test
    fun testFcmButtonVisibleWhenNoTokenStored() {
        onView(withId(R.id.btnGetFcmToken)).check(matches(isDisplayed()))
    }

    @Test
    fun testFcmButtonHiddenWhenTokenAlreadyStored() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("fcm_token", "fake_token_for_test_12345").commit()

        activityRule.scenario.recreate()

        onView(withId(R.id.btnGetFcmToken)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testForegroundServiceRunningAfterLaunch() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val running = manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == ForegroundNotificationService::class.java.name }
        assertTrue("ForegroundNotificationService should be running after MainActivity launch", running)
    }

    @Test
    fun testLaunchApplicationButtonVisible() {
        onView(withId(R.id.permissionforPPC)).check(matches(isDisplayed()))
    }
}
