package com.example.sport_geo_app.data.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.util.Date

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

data class WorkOutsTotalResponse(
    @SerializedName("totalDistanceKM") val totalDistanceKM: Double,
    @SerializedName("totalTime")  val totalTime: String,
    @SerializedName("latestWorkouts")  val latestWorkouts: List<Workout>,
    @SerializedName("totalWorkouts") val totalWorkouts: Int
)

data class Workout(
    @SerializedName("workout_id") val workout_id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sport") val sport: String,
    @SerializedName("linestring_coordinates") val linestring_coordinates: LineString,
    @SerializedName("distanceMeters") val distanceMeters: Double,
    @SerializedName("time") val time: String,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("staticMapUrl") val staticMapUrl: String
)


