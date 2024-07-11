package com.example.sport_geo_app.ui.activity

import AuthService
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.viewmodel.UserViewModel
import com.example.sport_geo_app.utils.EncryptedPreferencesUtil
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import org.json.JSONException
import retrofit2.HttpException


class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: UserViewModel
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button
    private lateinit var authService: AuthService
    private lateinit var encryptedSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        authService = AuthService(this)
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
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
                showToast("Please enter email and password")
            }
        }
    }

    private fun isLoggedIn(): Boolean {
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

    private fun handleSuccessResponse(responseBody: String?) {
        responseBody?.let {
            try {
                val jsonObject = JSONObject(responseBody)
                Log.d("RegisterActivity", jsonObject.toString())
                val userJson = jsonObject.getJSONObject("user")
                val jwtToken = jsonObject.getString("jwtToken")
                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")
                val userPoints = userJson.getString("points")

                saveUserData(userId, jwtToken, userEmail, userPoints)
                runOnUiThread {
                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    displayErrorMessage("Failed to parse user info")
                }
            }
        }
    }


    private fun handleErrorResponse(error: Throwable?) {
        error?.let { throwable ->
            val errorMessage = throwable.message

            if (errorMessage != null && errorMessage.isNotEmpty()) {
                try {
                    val jsonObject = JSONObject(errorMessage)
                    val message = jsonObject.getString("message")

                    runOnUiThread {
                        displayErrorMessage(message)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    runOnUiThread {
                        displayErrorMessage("Failed to parse error response")
                    }
                }
            } else {
                runOnUiThread {
                    displayErrorMessage("Unknown error occurred: ${throwable.javaClass.simpleName}")
                }
            }
        } ?: runOnUiThread {
            displayErrorMessage("Response is null")
        }
    }




    private fun saveUserData(userId: Int, jwtToken: String, userEmail: String, userPoints: String) {
        with(encryptedSharedPreferences.edit()) {
            putInt("user_id", userId)
            putString("jwtToken", jwtToken)
            putString("user_email", userEmail)
            putString("user_points", userPoints)
            putString("user_country", null) // If needed
            apply()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayErrorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}

