package com.example.sport_geo_app.data.repository

import com.example.sport_geo_app.data.source.GeoDataInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoDataRepository @Inject constructor(
    private val geoDataInterface: GeoDataInterface
) {
    suspend fun getGeoJson(): Result<String> {
        return try {
            val response = geoDataInterface.getGeoJson()
            if (response.isSuccessful) {
                Result.success(response.body()?.string().orEmpty())
            } else {
                Result.failure(Throwable(response.errorBody()?.string() ?: response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}