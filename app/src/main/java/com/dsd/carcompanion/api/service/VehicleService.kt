package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.ColorResponse
import com.dsd.carcompanion.api.models.PermissionResponse
import com.dsd.carcompanion.api.models.PreferencesResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import retrofit2.Response
import retrofit2.http.Body

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehicleService {
    @GET("api/car_companion/colors")
    suspend fun getVehicleColors(): Response<List<ColorResponse>>

    @GET("api/car_companion/vehicles/my_vehicles/")
    suspend fun getMyVehicles(): Response<List<VehicleResponse>>

    @GET("/api/car_companion/vehicles/{vin}/components/")
    suspend fun getPermissionsForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<List<PermissionResponse>>

    @POST("/api/car_companion/vehicles/{vin}/take_ownership/")
    suspend fun takeVehicleOwnership(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<VehicleResponse>

    @GET("api/car_companion/vehicles/{vin}/preferences")
    suspend fun getVehiclePreferences(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<VehicleResponse>

    @PUT("api/car_companion/vehicles/{vin}/preferences")
    suspend fun updateVehiclePreferences(
        @Path(value = "vin", encoded = false) vin: String,
        @Body preferences: PreferencesResponse,
    ): Response<PreferencesResponse>
}