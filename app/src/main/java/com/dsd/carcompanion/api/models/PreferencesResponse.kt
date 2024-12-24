package com.dsd.carcompanion.api.models

data class PreferencesResponse(
    val nickname: String,
    val interior_color: ColorResponse,
    val outer_color: ColorResponse
)