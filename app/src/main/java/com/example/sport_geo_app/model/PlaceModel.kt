package com.example.sport_geo_app.model

data class PlaceModel(
    val points: Int,
    val name: String,
    val updated_at: String,
    val description: String,
    val created_at: String,
    val place_id: String
)

data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: PlaceModel
)