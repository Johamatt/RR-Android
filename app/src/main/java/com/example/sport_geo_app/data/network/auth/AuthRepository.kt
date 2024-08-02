package com.example.sport_geo_app.data.network.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authInterface: AuthInterface,
    @ApplicationContext private val context: Context,
) {
    suspend fun loginWithEmail(email: String, password: String): Result<ResponseBody> {
        return try {
            val response = authInterface.loginWithEmail(email, password)
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

    suspend fun loginWithGoogle(idToken: String): Result<ResponseBody> {
        return try {
            val response = authInterface.loginWithGoogle(idToken)
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

    suspend fun registerUser(email: String, password: String): Result<ResponseBody> {
        return try {
            val response = authInterface.registerUser(email, password)
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
