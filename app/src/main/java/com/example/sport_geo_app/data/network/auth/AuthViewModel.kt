package com.example.sport_geo_app.data.network.auth
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
}
