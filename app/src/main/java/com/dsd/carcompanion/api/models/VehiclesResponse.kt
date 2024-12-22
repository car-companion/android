package com.dsd.carcompanion.api.models

data class VehicleResponse(
    val vin: String,
    val nickname: String?,
    val model: VehicleModels,
    val year_built: Int,
    val interior_color: ColorResponse,
    val outer_color: ColorResponse
)

data class VehicleModels(
    val name: String,
    val manufacturer: String
)
