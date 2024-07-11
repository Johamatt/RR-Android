package com.example.sport_geo_app.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthInterface {
    @FormUrlEncoded
    @POST
    fun loginWithEmail(@Url url: String, @Field("email") email: String, @Field("password") password: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST
    fun loginWithGoogle(@Url url: String, @Field("idToken") idToken: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST
    fun registerUser(@Url url: String, @Field("email") email: String, @Field("password") password: String): Call<ResponseBody>
}
