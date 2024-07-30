package com.example.sport_geo_app.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface NetworkInterface {
    @POST("workouts")
    @Headers("Content-Type: application/json")
    fun markWorkout(@Body requestBody: RequestBody): Call<ResponseBody>
    @GET("workouts")
    @Headers("Content-Type: application/json")
    fun getWorkouts(@Query("user_id") userId: Int): Call<ResponseBody>
    @GET("places/GeoJsonPoints")
    @Headers("Content-Type: application/json")
    fun getGeoJson(): Call<ResponseBody>
}

