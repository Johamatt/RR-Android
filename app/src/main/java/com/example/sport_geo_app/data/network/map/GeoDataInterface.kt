package com.example.sport_geo_app.data.network.map

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface GeoDataInterface {
    @GET("places/GeoJsonPoints")
    @Headers("Content-Type: application/json")
    suspend fun getGeoJson(): Response<ResponseBody>

}
