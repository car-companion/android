package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName

data class VehicleModelModel(
    @SerializedName("name")
    val Name: String,

    @SerializedName("manufacturer")
    val Manufacturer: String,
)
