package com.dsd.carcompanion.api.models

data class ColorResponse(
    val name: String,
    var hex_code: String,
    val is_metallic: Boolean
)