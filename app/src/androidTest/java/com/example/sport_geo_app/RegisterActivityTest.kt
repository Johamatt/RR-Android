package com.example.sport_geo_app

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.sport_geo_app.ui.activity.RegisterActivity
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
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

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        clearAllMocks()
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
}
