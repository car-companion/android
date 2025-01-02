package com.dsd.carcompanion.api.models

data class VehicleResponse(
    val vin: String,
    val model: VehicleModels,
    val year_built: Int,
    val default_interior_color: ColorResponse,
    val default_exterior_color: ColorResponse,
)

data class VehiclePreferencesResponse(
    val vin: String,
    val model: VehicleModels,
    val year_built: Int,
    val default_interior_color: ColorResponse,
    val default_exterior_color: ColorResponse,
    val user_preferences: PreferencesResponse,
)

data class VehicleModels(
    val name: String,
    val manufacturer: String
)
