package com.example.sport_geo_app.data.model

data class AuthResponse(
    val jwtToken: String,
    val user: User
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String
)

data class User(
    val user_id: Int,
    val googleId: String?,
    val email: String,
)