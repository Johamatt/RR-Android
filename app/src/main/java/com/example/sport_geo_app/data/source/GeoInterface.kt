package com.example.sport_geo_app.data.source

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeoInterface {
    @GET("places/GeoJsonPoints")
    @Headers("Content-Type: application/json")
    suspend fun getGeoJson(): Response<ResponseBody>

    @GET("places/SearchGeoJsonPoints")
    @Headers("Content-Type: application/json")
    suspend fun searchGeoJson(@Query("search") search: String): Response<ResponseBody>

}
