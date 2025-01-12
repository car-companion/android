package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.models.UserModel
import com.dsd.carcompanion.api.models.Users
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {

    @POST("api/auth/jwt/create")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<TokenModel>

    @POST("api/auth/users/")
    suspend fun registerUser(@Body createUserRequest: CreateUserRequest): Response<TokenModel>

    @GET("/api/auth/users/")
    suspend fun getAllUsers(): Response<List<UserModel>>
}