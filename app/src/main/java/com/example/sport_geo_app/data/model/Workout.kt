package com.example.sport_geo_app.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class WorkOutsTotalResponse(
    @SerializedName("totalDistanceKM") val totalDistanceKM: Double,
    @SerializedName("totalTime")  val totalTime: String
)

data class WorkoutCreateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: String,
    @SerializedName("distanceMeters") val distanceMeters: Float,
    @SerializedName("sport") val sport: String,
    @SerializedName("linestring_coordinates") val linestring_coordinates:  JsonObject?,
)

data class WorkoutsGetResponse(
    @SerializedName("workout_id") val workout_id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: String,
    @SerializedName("distanceMeters") val distanceMeters: Float,
    @SerializedName("sport") val sport: String,
    @SerializedName("linestring_coordinates") val linestringCoordinates: LineString?
)