package com.dsd.carcompanion.api.repository

import android.util.Log
import com.dsd.carcompanion.api.service.VehicleService
import com.dsd.carcompanion.api.utils.ResultOf
import retrofit2.Response

class VehicleRepository(
    private val vehicleService: VehicleService
) {

    private suspend fun <T> fetchDataFromApi(call: suspend () -> Response<T>, transform: suspend (T) -> Unit): ResultOf<Unit> {
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
}