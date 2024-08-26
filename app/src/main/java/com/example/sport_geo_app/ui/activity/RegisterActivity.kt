package com.example.sport_geo_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.ErrorManager
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"
    @Inject lateinit var errorManager: ErrorManager
    @Inject lateinit var toaster: Toaster

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var repeatPasswordInput: TextInputEditText
    private lateinit var registerBtn: Button
    private lateinit var loginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initializeViews()
        setupListeners()

        authViewModel.registerResult.observe(this) { result ->
            result.onSuccess { authResponse ->
                authViewModel.handleSuccessResponse(authResponse)
                navigateToMainActivity()
            }.onFailure { throwable ->
                val errorMessage = errorManager.handleErrorResponse(throwable)

                toaster.showToast(errorMessage)
            }
        }
    }
    private fun initializeViews() {
        emailInput = findViewById(R.id.register_email_input)
        passwordInput = findViewById(R.id.register_password_input)
        repeatPasswordInput = findViewById(R.id.register_repeat_password_input)
        registerBtn = findViewById(R.id.register_btn)
        loginTextView = findViewById(R.id.back_btn)
    }
    private fun setupListeners() {
        registerBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val repeatPassword = repeatPasswordInput.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                if (password == repeatPassword) {
                    authViewModel.registerUser(email, password)
                } else {
                    toaster.showToast("Passwords do not match")
                }
            } else {
                toaster.showToast("Please fill in all fields")
            }
        }

        loginTextView.setOnClickListener {
            navigateToLoginActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
    }
}

