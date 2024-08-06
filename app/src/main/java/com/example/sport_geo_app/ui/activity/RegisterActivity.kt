package com.example.sport_geo_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.ErrorManager
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    @Inject lateinit var errorManager: ErrorManager
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerBtn: Button

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
                errorManager.handleErrorResponse(throwable)
            }
        }
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
            } else {
                showMessage("Please enter email and password")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

