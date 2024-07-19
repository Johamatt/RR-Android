package com.example.sport_geo_app.data.model

data class PlaceModel(
    val placeId: String,
    val pointCoordinates: CoordinatesPoint?,
    val polygonCoordinates: Polygon?,
    val linestringCoordinates: LineString?,
    val country: String,
    val nameFi: String,
    val liikuntapaikkaTyyppi: String,
    val liikuntapaikkatyypinAlaryhmä: String,
    val liikuntapaikkatyypinPääryhmä: String,
    val katuosoite: String,
    val postinumero: String,
    val www: String?,
    val kunta: String?,
    val kuntaosa: String?,
    val liikuntapintaalaM2: Double?,
    val postitoimipaikka: String?,
    val lisätieto: String?,
    val muokattuViimeksi: String?,
    val kentänLeveysM: Double?,
    val kentänPituusM: Double?,
    val puhelinnumero: String?,
    val pintamateriaaliLisätieto: String?,
    val markkinointinimi: String?,
    val omistaja: String?,
    val maakunta: String?,
    val pintamateriaali: String?,
    val sähköposti: String?,
    val peruskorjausvuodet: String?,
    val aviAlue: String?
)


data class Visit(
    val visitId: Int,
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
    val placeId: String,
    val liikuntapaikkaTyyppi: String,
    val nameFi: String,
    val katuosoite: String,
)