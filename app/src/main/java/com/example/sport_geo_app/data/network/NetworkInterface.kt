package com.example.sport_geo_app.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NetworkInterface {
    @POST("places/check-proximity")
    @Headers("Content-Type: application/json")
    fun checkProximity(@Body requestBody: RequestBody): Call<ResponseBody>

    @POST("visits")
    @Headers("Content-Type: application/json")
    fun claimReward(@Body requestBody: RequestBody): Call<ResponseBody>
}

