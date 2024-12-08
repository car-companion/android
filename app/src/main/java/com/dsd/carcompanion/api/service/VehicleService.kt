package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.VehicleResponse

import retrofit2.http.GET
import retrofit2.http.Header

interface VehicleService {
    @GET("api/vehicle/vehicles/my_vehicles/")
    suspend fun getMyVehicles(@Header("Authorization") token: String): List<VehicleResponse>
}