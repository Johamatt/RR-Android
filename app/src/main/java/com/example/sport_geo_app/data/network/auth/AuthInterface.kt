package com.example.sport_geo_app.data.network.auth

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthInterface {
    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun loginWithEmail(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/google")
    suspend fun loginWithGoogle(
        @Field("idToken") idToken: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/register")
    suspend fun registerUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ResponseBody>
}
