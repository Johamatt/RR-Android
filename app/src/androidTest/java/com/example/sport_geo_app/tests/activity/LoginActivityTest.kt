package com.example.sport_geo_app.tests.activity

import android.widget.Button
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.modules.FakeToaster
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.ErrorManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LoginActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Inject
    lateinit var errorManager: ErrorManager

    @Before
    fun setup() {
        hiltRule.inject()
        clearAllMocks()
        FakeToaster.toasts.clear()
    }

    @Test
    fun test_email_login_withValidInput_shouldTriggerViewModel() {
        val email = "test@example.com"
        val password = "password"
        every { authViewModel.loginWithEmail(email, password) } just Runs

        onView(withId(R.id.email_input)).perform(typeText(email))
        onView(withId(R.id.password_input)).perform(typeText(password))
        closeSoftKeyboard()
        onView(withId(R.id.email_login_btn)).perform(click())

        verify { authViewModel.loginWithEmail(email, password) }
    }

    @Test
    fun testToastOnEmptyFields() {
        activityRule.scenario.onActivity { activity ->
            activity.runOnUiThread {
                activity.findViewById<Button>(R.id.email_login_btn).performClick()
            }
        }
        assert(FakeToaster.toasts.contains("Please enter email and password"))
    }
}