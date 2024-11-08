package com.dsd.carcompanion.userRegistrationAndLogin.auth.model

data class AuthNetworkResponse(
    val accessToken: String,
    val refreshToken: String
)
