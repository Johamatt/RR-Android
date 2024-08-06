package com.example.sport_geo_app.data.source

import retrofit2.Response
import retrofit2.http.POST
import com.example.sport_geo_app.data.model.AuthRequest
import com.example.sport_geo_app.data.model.AuthResponse
import com.example.sport_geo_app.data.model.GoogleAuthRequest

import retrofit2.http.Body

interface AuthInterface {
    @POST("/auth/login")
    suspend fun loginWithEmail(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    @POST("/auth/register")
    suspend fun registerUser(
        @Body request: AuthRequest
    ): Response<AuthResponse>
}
