package com.example.sport_geo_app.data.network.auth
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _loginResult = MutableLiveData<Result<ResponseBody>>()
    val loginResult: LiveData<Result<ResponseBody>> = _loginResult

    private val _registerResult = MutableLiveData<Result<ResponseBody>>()
    val registerResult: LiveData<Result<ResponseBody>> = _registerResult

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

    fun saveUserData(userId: Int, jwtToken: String, userEmail: String) {
        with(encryptedSharedPreferences.edit()) {
            putInt(USER_ID_KEY, userId)
            putString(USER_EMAIL_KEY, userEmail)
            putString(JWT_TOKEN_KEY, jwtToken)
            apply()
        }
    }

    fun handleSuccessResponse(responseBody: String?) {
        responseBody?.let {
            try {
                val jsonObject = JSONObject(responseBody)
                val userJson = jsonObject.getJSONObject("user")
                val jwtToken = jsonObject.getString("jwtToken")
                val userId = userJson.getInt("user_id")
                val userEmail = userJson.getString("email")

                saveUserData(userId, jwtToken, userEmail)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception properly
            }
        }
    }
}

