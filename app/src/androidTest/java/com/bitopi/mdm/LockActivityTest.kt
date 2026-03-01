package com.bitopi.mdm

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.not
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LockActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LockActivity::class.java)

    @Before
    fun clearLockPrefs() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        ctx.getSharedPreferences("lock_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("locked", true).commit()
    }

    @Test
    fun testPinInputAndUnlockButtonVisible() {
        onView(withId(R.id.pinInput)).check(matches(isDisplayed()))
        onView(withId(R.id.unlockBtn)).check(matches(isDisplayed()))
    }

    @Test
    fun testWrongPinShowsError() {
        onView(withId(R.id.pinInput)).perform(typeText("0000"), closeSoftKeyboard())
        onView(withId(R.id.unlockBtn)).perform(click())
        onView(withId(R.id.errorText)).check(matches(isDisplayed()))
        onView(withId(R.id.errorText)).check(matches(not(withText(""))))
    }

    @Test
    fun testWrongPinClearsPinInput() {
        onView(withId(R.id.pinInput)).perform(typeText("9999"), closeSoftKeyboard())
        onView(withId(R.id.unlockBtn)).perform(click())
        onView(withId(R.id.pinInput)).check(matches(withText("")))
    }

    @Test
    fun testCorrectPinFinishesActivityAndClearsLockedFlag() {
        onView(withId(R.id.pinInput)).perform(typeText("1234"), closeSoftKeyboard())
        onView(withId(R.id.unlockBtn)).perform(click())

        activityRule.scenario.onActivity { activity ->
            assertTrue("Activity should be finishing after correct PIN", activity.isFinishing)
        }

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val locked = ctx.getSharedPreferences("lock_prefs", Context.MODE_PRIVATE)
            .getBoolean("locked", true)
        assertFalse("lock_prefs.locked should be false after correct PIN", locked)
    }
}
