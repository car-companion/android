package com.dsd.carcompanion.api.repository

import android.util.Log
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.PermissionResponse
import com.dsd.carcompanion.api.models.PermissionsResponse
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.models.VehicleResponse
import com.dsd.carcompanion.api.service.VehicleService
import com.dsd.carcompanion.api.utils.JwtTokenManager
import com.dsd.carcompanion.api.utils.ResultOf
import retrofit2.Response

class VehicleRepository(
    private val vehicleService: VehicleService,
    private val jwtTokenManager: JwtTokenManager
) {

    private suspend fun <T, R> fetchVehicleDataFromApi(
        call: suspend () -> Response<T>,
        transform: suspend (T) -> R
    ): ResultOf<R> {
        return try {
            val response = call()
            Log.d("Testing", response.toString())
            if (response.isSuccessful) {
                response.body()?.let {
                    val transformedData = transform(it) // Transform response into required result
                    ResultOf.Success(transformedData)
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

    suspend fun getOwnedVehicles(): ResultOf<List<VehicleResponse>> {
        return fetchVehicleDataFromApi(
            call = { vehicleService.getMyVehicles() },
            transform = { vehicles: List<VehicleResponse> -> vehicles } // No modification; directly return the list
        )
    }

    suspend fun getComponentsForVehicle(vin: String): ResultOf<List<PermissionResponse>> {
        return fetchVehicleDataFromApi(
            call = { vehicleService.getPermissionsForVehicle(vin) },
            transform = { permissions: List<PermissionResponse> ->
                permissions // Directly return the list of permissions
            }
        )
    }
}