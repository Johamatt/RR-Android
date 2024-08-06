package com.example.sport_geo_app.data.model

data class AuthRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String
)