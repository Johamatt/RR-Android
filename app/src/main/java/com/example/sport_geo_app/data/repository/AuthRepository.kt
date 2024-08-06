package com.example.sport_geo_app.data.repository

import com.example.sport_geo_app.data.source.AuthInterface
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

// AuthRepository.kt
import com.example.sport_geo_app.data.model.AuthRequest
import com.example.sport_geo_app.data.model.AuthResponse
import com.example.sport_geo_app.data.model.GoogleAuthRequest


@Singleton
class AuthRepository @Inject constructor(
    private val authInterface: AuthInterface
) {
    suspend fun loginWithEmail(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authInterface.loginWithEmail(AuthRequest(email, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Throwable(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        return try {
            val response = authInterface.loginWithGoogle(GoogleAuthRequest(idToken))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Throwable(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = authInterface.registerUser(AuthRequest(email, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Throwable(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


