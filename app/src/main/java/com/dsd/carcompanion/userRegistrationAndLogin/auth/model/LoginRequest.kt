package com.dsd.carcompanion.userRegistrationAndLogin.auth.model

data class LoginRequest(
    val email: String,
    val password: String
)