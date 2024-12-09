package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.PermissionResponse
import com.dsd.carcompanion.api.models.PermissionsResponse
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.models.VehicleResponse
import retrofit2.Call
import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VehicleService {
    @GET("api/vehicle/vehicles/my_vehicles/")
    suspend fun getMyVehicles(): Response<List<VehicleResponse>>

    @GET("/api/vehicle/vehicles/{vin}/components/")
    suspend fun getPermissionsForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<List<PermissionResponse>>

}