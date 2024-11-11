package com.dsd.carcompanion.api.repository

import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.utils.JwtTokenManager
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.api.service.UserService
import retrofit2.Response

class AuthRepository(
    private val authService: UserService,
    private val jwtTokenManager: JwtTokenManager
) {

    private suspend fun <T> fetchDataFromApi(call: suspend () -> Response<T>, transform: suspend (T) -> Unit): ResultOf<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let {
                    transform(it) // Transform response into required result
                    ResultOf.Success(Unit)
                } ?: ResultOf.Error("Empty response body")
            } else {
                ResultOf.Error("API call failed: ${response.message()}")
            }
        } catch (e: Exception) {
            ResultOf.Error("Network error: ${e.localizedMessage}")
        }
    }

    // Function to handle login
    suspend fun login(loginRequest: LoginRequest): ResultOf<Unit> {
        return fetchDataFromApi(call = { authService.loginUser(loginRequest) },
            transform = { response: TokenModel ->
                jwtTokenManager.saveAccessJwt(response.Access)
                jwtTokenManager.saveRefreshJwt(response.Refresh)
            })
    }

    suspend fun reqister(createUserRequest: CreateUserRequest): ResultOf<Unit> {
        return fetchDataFromApi(call = { authService.registerUser(createUserRequest) },
            transform = { response: TokenModel ->
                jwtTokenManager.saveAccessJwt(response.Access)
                jwtTokenManager.saveRefreshJwt(response.Refresh)
            })
    }
}