package com.example.sport_geo_app.data.model

import com.google.gson.annotations.SerializedName

data class LineString(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<Double>>
)

data class PointPin(
    @SerializedName("place_id") val place_id: String,
    @SerializedName("liikuntapaikkatyyppi") val liikuntapaikkatyyppi: String,
    @SerializedName("name_fi") val name_fi: String,
    @SerializedName("katuosoite") val katuosoite: String,
)

