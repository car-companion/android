package com.dsd.carcompanion.api.repository

import android.util.Log
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.service.VehicleService
import com.dsd.carcompanion.api.utils.JwtTokenManager
import com.dsd.carcompanion.api.utils.ResultOf
import retrofit2.Response

class VehicleRepository(
    private val vehicleService: VehicleService,
    private val jwtTokenManager: JwtTokenManager
) {

    private suspend fun <T> fetchVehicleDataFromApi(call: suspend () -> Response<T>, transform: suspend (T) -> Unit): ResultOf<Unit> {
        return try {
            val response = call()
            Log.d("Testing", response.toString())
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

    // TODO
    suspend fun createVehicle() {}

    // TODO
    suspend fun getVehicleModel() {}

    suspend fun getComponentsForVehicle(vin: String): ResultOf<Unit> {
        return fetchVehicleDataFromApi(call = { vehicleService.getPermissionsForVehicle(vin) },
            transform = { response: TokenModel ->
                jwtTokenManager.saveAccessJwt(response.Access)
                jwtTokenManager.saveRefreshJwt(response.Refresh)
            })
    }
}