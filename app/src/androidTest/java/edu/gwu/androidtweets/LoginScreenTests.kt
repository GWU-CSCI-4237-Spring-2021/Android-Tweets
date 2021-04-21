package edu.gwu.androidtweets

import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun validateLoginScreenButtonStates() {
        val username = onView(withHint("Username"))
        val password = onView(withHint("Password"))
        val login = onView(withId(R.id.login))
        val signUp = onView(withId(R.id.signUp))

        login.check(matches(not(isEnabled())))
        signUp.check(matches(not(isEnabled())))

        username.perform(clearText())
        password.perform(clearText())

        username.perform(typeText("nick@gwu.edu"))
        password.perform(typeText("abcd12345"))

        login.check(matches(isEnabled()))
        signUp.check(matches(isEnabled()))
    }

    @Test
    fun mapsActivityNavigation() {
        val username = onView(withHint("Username"))
        val password = onView(withHint("Password"))
        val login = onView(withId(R.id.login))
        val signUp = onView(withId(R.id.signUp))

        login.check(matches(not(isEnabled())))
        signUp.check(matches(not(isEnabled())))

        username.perform(clearText())
        password.perform(clearText())

        username.perform(typeText("nick@gwu.edu"))
        password.perform(typeText("abcd12345"))

        login.check(matches(isEnabled()))
        signUp.check(matches(isEnabled()))

        login.perform(click())

        sleep(2000)

        val mapsTitle = onView(withText("Welcome, nick@gwu.edu"))
        mapsTitle.check(matches(isDisplayed()))
    }
}