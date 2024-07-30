package com.example.sport_geo_app.data.model

data class PlaceModel(
    val place_id: String,
    val point_coordinates: CoordinatesPoint?,
    val polygon_coordinates: Polygon?,
    val linestring_coordinates: LineString?,
    val name_fi: String,
    val liikuntapaikkatyyppi: String,
    val liikuntapaikkatyypinalaryhmä: String,
    val liikuntapaikkatyypinpääryhmä: String,
    val www: String?,
    val lisätieto: String?,
    val muokattu_viimeksi: String?,
    val puhelinnumero: String?,
    val markkinointinimi: String?,
    val sähköposti: String?,
)


data class Workout(
    val workout_id: Int,
    val createdAt: String,
    val place: PlaceModel
)

data class CoordinatesPoint(
    val type: String,
    val coordinates: List<Double>
)

data class Polygon(
    val type: String = "Polygon",
    val coordinates: List<List<List<Double>>>
)

data class LineString(
    val type: String = "LineString",
    val coordinates: List<List<Double>>
)

data class PlaceMapMarkerModel(
    val place_id: String,
    val liikuntapaikkatyyppi: String,
    val name_fi: String,
    val katuosoite: String,
)