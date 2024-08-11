package com.example.sport_geo_app.data.model

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point


data class Workout(
    @SerializedName("workout_id") val workout_id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("point_coordinates") val pointCoordinates: Point?,
    @SerializedName("linestring_coordinates") val linestringCoordinates: LineString?,
    @SerializedName("duration") val duration: String,
    @SerializedName("sport") val sport: String
)

data class WorkoutCreate(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: String,
    @SerializedName("distanceMeters") val distanceMeters: Float,
    @SerializedName("sport") val sport: String,
    @SerializedName("linestring_coordinates") val linestring_coordinates:  JsonObject?,
)

data class PointPin(
    @SerializedName("place_id") val place_id: String,
    @SerializedName("liikuntapaikkatyyppi") val liikuntapaikkatyyppi: String,
    @SerializedName("name_fi") val name_fi: String,
    @SerializedName("katuosoite") val katuosoite: String,
)

