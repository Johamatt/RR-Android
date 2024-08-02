package com.example.sport_geo_app.data.network.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import okhttp3.ResponseBody

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<ResponseBody>>()
    val loginResult: LiveData<Result<ResponseBody>> = _loginResult

    private val _registerResult = MutableLiveData<Result<ResponseBody>>()
    val registerResult: LiveData<Result<ResponseBody>> = _registerResult

    fun loginWithEmail(email: String, password: String) {
        authRepository.loginWithEmail(email, password) { response, error ->
            _loginResult.value = if (response != null) {
                Result.success(response)
            } else {
                Result.failure(error ?: Throwable("Unknown error occurred"))
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        Log.d("AuthViewModel", "Attempting Google login")
        authRepository.loginWithGoogle(idToken) { response, error ->
            Log.d("AuthViewModel", "Google login response: ${response?.string()}")
            _loginResult.value = if (response != null) {
                Result.success(response)
            } else {
                Result.failure(error ?: Throwable("Unknown error occurred"))
            }
        }
    }

    fun registerUser(email: String, password: String) {
        authRepository.registerUser(email, password) { response, error ->
            _registerResult.value = if (response != null) {
                Result.success(response)
            } else {
                Result.failure(error ?: Throwable("Unknown error occurred"))
            }
        }
    }
}
