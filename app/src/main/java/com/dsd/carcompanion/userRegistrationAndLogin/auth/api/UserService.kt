package com.dsd.carcompanion.userRegistrationAndLogin.auth.api

import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.UserNetworkResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserService {
    @GET("api/auth/users/me/")
    suspend fun fetchUser(): Response<UserNetworkResponse>
}