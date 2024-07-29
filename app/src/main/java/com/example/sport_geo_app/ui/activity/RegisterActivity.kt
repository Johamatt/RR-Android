package com.example.sport_geo_app.ui.activity

import com.example.sport_geo_app.data.network.AuthService
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject


class RegisterActivity : AppCompatActivity() {
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button
    private lateinit var authService: AuthService
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        authService = AuthService(this)
        encryptedSharedPreferences = EncryptedPreferencesUtil.getEncryptedSharedPreferences(this)

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
                registerUser(email, password)
            } else {
                showMessage("Please enter email and password")
            }
        }
    }
    private fun isLoggedIn(): Boolean { // TODO change better verification
        return encryptedSharedPreferences.contains("user_id")
    }
    private fun registerUser(email: String, password: String) {
        authService.registerUser(email, password) { response, error ->
            runOnUiThread {
                if (error != null) {
                    handleErrorResponse(error)
                } else if (response != null) {
                    handleSuccessResponse(response.string())
                } else {
                    handleErrorResponse(null)
                }
            }
        }
    }
    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String) {
        with(encryptedSharedPreferences.edit()) {
            putInt("user_id", userId)
            putString("user_email", userEmail)
            putString("jwtToken", jwtToken)
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
                runOnUiThread {
                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showMessage("Failed to parse user info")
                }
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

        runOnUiThread {
            showMessage(messageToShow)
        }
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}

