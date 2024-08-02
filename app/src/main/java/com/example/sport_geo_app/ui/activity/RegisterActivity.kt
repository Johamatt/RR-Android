package com.example.sport_geo_app.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.auth.AuthViewModel
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import com.example.sport_geo_app.utils.Constants.USER_EMAIL_KEY
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    @Inject lateinit var encryptedSharedPreferences: SharedPreferences
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()

        if (isLoggedIn()) {
            navigateToMainActivity()
            return
        }
        setupListeners()
    }
    private fun initializeViews() {
        emailInput = findViewById(R.id.register_email_input)
        passwordInput = findViewById(R.id.register_password_input)
        registerBtn = findViewById(R.id.register_btn)
    }
    private fun setupListeners() {
        registerBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.registerUser(email, password)
                observeRegisterResult()
            } else {
                showMessage("Please enter email and password")
            }
        }
    }
    // TODO change better verification
    private fun isLoggedIn(): Boolean {
        return encryptedSharedPreferences.contains(USER_ID_KEY)
    }

    private fun observeRegisterResult() {
        authViewModel.registerResult.observe(this) { result ->
            result.onSuccess { responseBody ->
                handleSuccessResponse(responseBody.string())
            }.onFailure { throwable ->
                handleErrorResponse(throwable)
            }
        }
    }
    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String) {
        with(encryptedSharedPreferences.edit()) {
            putInt(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, userEmail)
            putString(JWT_TOKEN_KEY, jwtToken)
            apply()
        }
    }
    private fun handleSuccessResponse(responseBody: String?) {
        responseBody?.let {
            try {
                val jsonObject = JSONObject(responseBody)
                val userJson = jsonObject.getJSONObject("user")
                val jwtToken = jsonObject.getString("jwtToken")
                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")

                saveUserData(userId, jwtToken, userEmail)
                navigateToMainActivity()
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Failed to parse user info")
            }
        }
    }
    private fun handleErrorResponse(error: Throwable?) {
        val errorMessage = error?.message
        val messageToShow = errorMessage?.let { message ->
            try {
                val jsonObject = JSONObject(message)
                jsonObject.getString("message")
            } catch (e: JSONException) {
                e.printStackTrace()
                "Failed to parse error response"
            }
        } ?: "Unknown error occurred: ${error?.javaClass?.simpleName ?: "Unknown"}"

        showMessage(messageToShow)
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
    }
}

