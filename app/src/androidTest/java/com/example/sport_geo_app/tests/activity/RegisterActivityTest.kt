package com.example.sport_geo_app.tests.activity

import android.widget.Button
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.modules.FakeToaster
import com.example.sport_geo_app.ui.activity.LoginActivity
import com.example.sport_geo_app.ui.activity.RegisterActivity
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.ErrorManager
import com.google.android.material.textfield.TextInputEditText
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
class RegisterActivityTest {

    @get:Rule var hiltRule = HiltAndroidRule(this)
    @get:Rule var activityRule = ActivityScenarioRule(RegisterActivity::class.java)
    @Inject lateinit var toaster: Toaster
    @Inject lateinit var authViewModel: AuthViewModel
    @Inject lateinit var errorManager: ErrorManager

    @Before
    fun setup() {
        hiltRule.inject()
        clearAllMocks()
        FakeToaster.toasts.clear()
    }

    @Test
    fun testRegisterUser_withValidInput_shouldTriggerViewModel() {
        val email = "test@example.com"
        val password = "password"
        every { authViewModel.registerUser(email, password) } just Runs

        onView(withId(R.id.register_email_input)).perform(typeText(email))
        onView(withId(R.id.register_password_input)).perform(typeText(password))
        onView(withId(R.id.register_repeat_password_input)).perform(typeText(password))
        closeSoftKeyboard()
        onView(withId(R.id.register_btn)).perform(click())

        verify { authViewModel.registerUser(email, password) }
    }

    @Test
    fun testToastOnEmptyFields() {
        activityRule.scenario.onActivity { activity ->
            activity.runOnUiThread {
                activity.findViewById<Button>(R.id.register_btn).performClick()
            }
        }
        assert(FakeToaster.toasts.contains("Please fill in all fields"))
    }

    @Test
    fun testToastOnPasswordMismatch() {
        activityRule.scenario.onActivity { activity ->
            activity.runOnUiThread {
                activity.findViewById<TextInputEditText>(R.id.register_email_input).setText("test@example.com")
                activity.findViewById<TextInputEditText>(R.id.register_password_input).setText("a+")
                activity.findViewById<TextInputEditText>(R.id.register_repeat_password_input).setText("b-")
                activity.findViewById<Button>(R.id.register_btn).performClick()
            }
        }
        assert(FakeToaster.toasts.contains("Passwords do not match"))
    }

    @Test
    fun testNavigationToLoginActivity() {
        Intents.init()
        try {
            onView(withId(R.id.back_btn)).perform(click())
            Intents.intended(IntentMatchers.hasComponent(LoginActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }
}
