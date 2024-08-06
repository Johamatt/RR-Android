package com.example.sport_geo_app.data.source

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface WorkoutInterface {

    @GET("workouts")
    @Headers("Content-Type: application/json")
    suspend fun getWorkouts(@Query("user_id") userId: Int): Response<ResponseBody>


    @POST("workouts")
    @Headers("Content-Type: application/json")
    suspend fun createWorkout(@Body requestBody: RequestBody):  Response<ResponseBody>


}