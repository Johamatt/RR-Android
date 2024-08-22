package com.example.sport_geo_app

import androidx.lifecycle.ViewModelProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.sport_geo_app.ui.activity.RegisterActivity
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.ErrorManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.verify

@HiltAndroidTest
class RegisterActivityTest {



    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var activityRule = ActivityScenarioRule(RegisterActivity::class.java)

    // TODO mock
    lateinit var authViewModel: AuthViewModel


    lateinit var errorManager: ErrorManager

    @Before
    fun setup() {
        activityRule.scenario.onActivity { activity ->
            authViewModel = ViewModelProvider(activity)[AuthViewModel::class.java]
        }

        hiltRule.inject()
    }

    @Test
    fun testRegisterUser_withValidInput_shouldTriggerViewModel() {

        val scenario = activityRule.scenario


        onView(withId(R.id.register_email_input)).perform(typeText("test@example.com"))
        onView(withId(R.id.register_password_input)).perform(typeText("password"))
        onView(withId(R.id.register_repeat_password_input)).perform(typeText("password"))

        closeSoftKeyboard()


        onView(withId(R.id.register_btn)).perform(click())

        verify { authViewModel.registerUser("test@example.com", "password") }
    }

}
