package com.dsd.carcompanion.userRegistrationAndLogin.auth.repository

import com.dsd.carcompanion.utils.JwtTokenManager
import com.dsd.carcompanion.userRegistrationAndLogin.auth.api.AuthService
import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.AuthNetworkResponse
import com.dsd.carcompanion.userRegistrationAndLogin.auth.model.LoginRequest
import com.dsd.carcompanion.utils.ResultOf
import retrofit2.Response
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val jwtTokenManager: JwtTokenManager
) {
    private suspend fun <T> fetchDataFromApi(call: suspend () -> Response<T>, transform: suspend (T) -> Unit): ResultOf<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let {
                    transform(it)
                    ResultOf.Success(Unit)
                } ?: ResultOf.Error("Empty response body")
            } else {
                ResultOf.Error("API call failed: ${response.message()}")
            }
        } catch (e: Exception) {
            ResultOf.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun login(loginRequest: LoginRequest): ResultOf<Unit> {
        return fetchDataFromApi(call = { authService.login(loginRequest) },
            transform = { response: AuthNetworkResponse ->
                jwtTokenManager.saveAccessJwt(response.accessToken)
                jwtTokenManager.saveRefreshJwt(response.refreshToken)
            })
    }
}
