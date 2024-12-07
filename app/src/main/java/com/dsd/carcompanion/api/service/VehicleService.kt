package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.VehicleResponse
import retrofit2.http.GET

interface VehicleService {
    @GET("api/vehicle/vehicles/my_vehicles/")
    suspend fun getMyVehicles(): List<VehicleResponse>
}
