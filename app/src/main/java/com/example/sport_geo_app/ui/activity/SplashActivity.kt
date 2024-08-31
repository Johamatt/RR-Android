package com.example.sport_geo_app.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.sport_geo_app.MainActivity
import com.example.sport_geo_app.R
import com.example.sport_geo_app.di.Toaster
import com.example.sport_geo_app.ui.viewmodel.AuthViewModel
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import com.example.sport_geo_app.utils.ErrorManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var encryptedSharedPreferences: SharedPreferences
    private val authViewModel: AuthViewModel by viewModels()
    @Inject
    lateinit var errorManager: ErrorManager
    @Inject
    lateinit var toaster: Toaster

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val token = encryptedSharedPreferences.getString(JWT_TOKEN_KEY, null)

        if (token.isNullOrEmpty()) {
            navigateToLogin()
        } else {
            authViewModel.validateToken(token)
        }

        authViewModel.tokenResult.observe(this) { result ->
            result.onSuccess { expired ->
                if (!expired) {
                    encryptedSharedPreferences.edit().clear()
                    toaster.showToast("Token expired")
                    navigateToLogin()
                } else {
                    navigateToMain()
                }
            }.onFailure { throwable ->
                val errorMessage = errorManager.handleErrorResponse(throwable)
                toaster.showToast(errorMessage)
                encryptedSharedPreferences.edit().clear()
                toaster.showToast("Failed to login")
                navigateToLogin()
            }
        }


    }
    private fun navigateToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun navigateToMain() {
        val loginIntent = Intent(this, MainActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

}
