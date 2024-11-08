package com.dsd.carcompanion.userRegistrationAndLogin.auth.api

import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.AuthNetworkResponse
import retrofit2.Response
import retrofit2.http.POST

interface RefreshTokenService {
    @POST("api/auth/jwt/refresh/")
    suspend fun refreshToken(
        //@Body body: String
    ): Response<AuthNetworkResponse>
}