package com.example.sport_geo_app.ui.viewmodel
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sport_geo_app.data.model.AuthResponse
import com.example.sport_geo_app.data.repository.AuthRepository
import com.example.sport_geo_app.utils.Constants.JWT_TOKEN_KEY
import com.example.sport_geo_app.utils.Constants.USER_EMAIL_KEY
import com.example.sport_geo_app.utils.Constants.USER_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.ResponseBody
import org.json.JSONObject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private var encryptedSharedPreferences: SharedPreferences

) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            _loginResult.value = result
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(idToken)
            _loginResult.value = result
        }
    }

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password)
            _registerResult.value = result
        }
    }

    fun handleSuccessResponse(authResponse: AuthResponse) {
        try {
            val jwtToken = authResponse.jwtToken
            val user = authResponse.user
            val userId = user.user_id
            val userEmail = user.email

            with(encryptedSharedPreferences.edit()) {
                putInt(USER_ID_KEY, userId)
                putString(USER_EMAIL_KEY, userEmail)
                putString(JWT_TOKEN_KEY, jwtToken)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

