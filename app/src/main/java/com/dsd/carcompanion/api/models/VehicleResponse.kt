package com.dsd.carcompanion.api.models

data class VehicleResponse(
    val vin: String,
    val nickname: String,
    val model: Model,
    val year_built: Int,
    val interior_color: Color,
    val outer_color: Color
)

data class Model(
    val name: String,
    val manufacturer: String
)

data class Color(
    val name: String
)
