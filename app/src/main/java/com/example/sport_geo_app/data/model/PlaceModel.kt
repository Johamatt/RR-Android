package com.example.sport_geo_app.data.model

data class PlaceModel(
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

data class Visit(
    val visitId: Int,
    val createdAt: String,
    val place: Place
)

data class Place(
    val placeId: String,
    val name: String,
    val country: String,
    val description: String,
    val coordinates: Coordinates,
    val createdAt: String,
    val updatedAt: String
)

data class Coordinates(
    val type: String,
    val coordinates: List<Double>
)
