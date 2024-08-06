package com.example.sport_geo_app.data.model

data class AuthResponse(
    val jwtToken: String,
    val user: User
)