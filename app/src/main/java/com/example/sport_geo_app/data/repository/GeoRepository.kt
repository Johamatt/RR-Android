package com.example.sport_geo_app.data.repository

import com.example.sport_geo_app.data.source.GeoInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoRepository @Inject constructor(
    private val geoDataInterface: GeoInterface
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

    suspend fun searchGeoJson(search: String): Result<String> {
        return try {
            val response = geoDataInterface.searchGeoJson(search)
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