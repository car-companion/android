package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class VehicleModel (
    @SerializedName("vin")
    val Vin: String,

    @SerializedName("year")
    val Year: Number,

    @SerializedName("model")
    val Model: String,

    @SerializedName("color")
    val Color: String
)