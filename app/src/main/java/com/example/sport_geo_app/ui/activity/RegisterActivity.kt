package com.example.sport_geo_app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.data.network.AuthService
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var viewModel: UserViewModel
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button
    private lateinit var authService: AuthService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthService(this)
        setContentView(R.layout.activity_register)
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("user_id")) {
            val userId = sharedPreferences.getInt("user_id", -1)
            val userEmail = sharedPreferences.getString("user_email", "")
            val userPoints = sharedPreferences.getString("user_points", "")
            if (userId != -1 && !userEmail.isNullOrEmpty() && !userPoints.isNullOrEmpty()) {
                navigateToMainActivity(userId, userEmail, userPoints)
                return
            }
        }

        emailInput = findViewById(R.id.register_email_input)
        passwordInput = findViewById(R.id.register_password_input)
        registerBtn = findViewById(R.id.register_btn)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        authService.registerUser(email, password) { response, error ->
            if (error != null) {
                error.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } else if (response != null && response.isSuccessful) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    handleSuccessResponse(responseBody)
                }
            } else {
                runOnUiThread {
                    handleErrorResponse(response)
                }
            }
        }
    }

    private fun handleSuccessResponse(responseBody: String?) {
        if (responseBody != null) {
            try {
                val jsonObject = JSONObject(responseBody)
                val userJson = jsonObject.getJSONObject("user")

                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userPoints = userJson.getString("points")

                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", userId)
                    putString("user_email", userEmail)
                    putString("user_points", userPoints)
                    putString("user_country", null)
                    apply()
                }

                navigateToMainActivity(userId, userEmail, userPoints)
            } catch (e: Exception) {
                e.printStackTrace()
                displayErrorMessage("Failed to parse user info")
            }
        }
    }

    private fun handleErrorResponse(response: Response?) {
        val responseBody = response?.body?.string()

        responseBody?.let { body ->
            val jsonObject = JSONObject(body)
            val errorArray = jsonObject.getJSONArray("message")

            val errorMessage = StringBuilder()
            for (i in 0 until errorArray.length()) {
                errorMessage.append(errorArray.getString(i))
                if (i < errorArray.length() - 1) {
                    errorMessage.append(", ")
                }
            }

            when (response.code) {
                400 -> {
                    displayErrorMessage(errorMessage.toString())
                }
                401 -> {
                    displayErrorMessage(errorMessage.toString())
                }
                else -> {
                    displayErrorMessage("Authentication failed: ${response.message}")
                }
            }
        } ?: run {
            // Handle case where responseBody is null
            displayErrorMessage("Response body is null")
        }
    }

    private fun displayErrorMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(userId: Int, userEmail: String, userPoints: String) {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.putExtra("user_id", userId)
        intent.putExtra("user_email", userEmail)
        intent.putExtra("user_points", userPoints)
        startActivity(intent)
        finish()
    }
}
