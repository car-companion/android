package com.dsd.carcompanion.api.repository

import android.util.Log
import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.ResetPasswordRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.models.UserModel
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.utils.JwtTokenManager
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.api.service.UserService
import retrofit2.Response

class AuthRepository(
    private val authService: UserService,
    private val jwtTokenManager: JwtTokenManager
) {

    private suspend fun <T, R> fetchDataFromApi(
        call: suspend () -> Response<T>,
        transform: suspend (T) -> R
    ): ResultOf<R> {
        return try {
            val response = call()
            Log.d("Testing", response.toString())
            if (response.isSuccessful) {
                response.body()?.let {
                    val transformedData = transform(it) // Transform response into required result
                    ResultOf.Success(transformedData, response.code())
                } ?: ResultOf.Error("Empty response body")
            } else {
                ResultOf.Error("API call failed: ${response.message()}", response.code())
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

    suspend fun register(createUserRequest: CreateUserRequest): ResultOf<Unit> {
        return fetchDataFromApi(call = { authService.registerUser(createUserRequest) },
            transform = { response: TokenModel ->
                jwtTokenManager.saveAccessJwt(response.Access)
                jwtTokenManager.saveRefreshJwt(response.Refresh)
            })
    }

    suspend fun resetPassword(email: String): Response<Any> {
        val request = ResetPasswordRequest(email)
        return authService.resetPassword(request)
    }

    suspend fun getAllUsers(): ResultOf<List<UserModel>> {
        return fetchDataFromApi(
            call = { authService.getAllUsers() },
            transform = { users: List<UserModel> -> users }
        )
    }
}