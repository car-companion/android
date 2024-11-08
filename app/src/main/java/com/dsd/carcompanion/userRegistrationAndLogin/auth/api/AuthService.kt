package com.dsd.carcompanion.userRegistrationAndLogin.auth.api

import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.AuthNetworkResponse
import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/jwt/create/")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthNetworkResponse>
}