package com.dsd.carcompanion.api.models

import com.google.gson.annotations.SerializedName


data class ComponentResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: ComponentType,
    @SerializedName("status")
    val status: Double
)

data class ComponentType(
    @SerializedName("name")
    val name: String
)

data class ComponentStatusUpdate(
    @SerializedName("status")
    val status: Double
)
