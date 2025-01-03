package com.dsd.carcompanion.api.service

import com.dsd.carcompanion.api.models.ColorResponse
import com.dsd.carcompanion.api.models.ComponentResponse
import com.dsd.carcompanion.api.models.ComponentStatusUpdate
import com.dsd.carcompanion.api.models.GrantPermissionRequest
import com.dsd.carcompanion.api.models.GrantedPermissions
import com.dsd.carcompanion.api.models.PermissionsResponse
import com.dsd.carcompanion.api.models.PreferencesResponse
import com.dsd.carcompanion.api.models.RevokedPermissions
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE

import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VehicleService {
    @GET("api/car_companion/colors")
    suspend fun getVehicleColors(): Response<List<ColorResponse>>

    @GET("api/car_companion/vehicles/my_vehicles/")
    suspend fun getMyVehicles(): Response<List<VehiclePreferencesResponse>>

    @GET("/api/car_companion/vehicles/{vin}/components/")
    suspend fun getComponentsForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<List<ComponentResponse>>

    @PATCH("/api/car_companion/vehicles/{vin}/components/{type_name}/{name}")
    suspend fun updateComponentStatusForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "type_name", encoded = false) typeName: String,
        @Path(value = "name", encoded = false) name: String,
        @Body status: ComponentStatusUpdate,
    ): Response<ComponentResponse>

    @POST("/api/car_companion/vehicles/{vin}/take_ownership/")
    suspend fun takeVehicleOwnership(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<VehicleResponse>

    @GET("api/car_companion/vehicles/{vin}/preferences/")
    suspend fun getVehiclePreferences(
        @Path(value = "vin", encoded = false) vin: String,
    ): Response<VehiclePreferencesResponse>

    @PUT("api/car_companion/vehicles/{vin}/preferences/")
    suspend fun updateVehiclePreferences(
        @Path(value = "vin", encoded = false) vin: String,
        @Body preferences: PreferencesResponse,
    ): Response<PreferencesResponse>

    @GET("api/car_companion/vehicles/{vin}/permissions/{username}/")
    suspend fun retrievePermissionsOfUserForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "username", encoded = false) username: String,
    ): Response<PermissionsResponse>

    @POST("api/car_companion/vehicles/{vin}/permissions/{username}/")
    suspend fun grantFullAccessToUserForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "username", encoded = false) username: String,
        @Body grantPermissionRequest: GrantPermissionRequest,
    ): Response<GrantedPermissions>

    @DELETE("api/car_companion/vehicles/{vin}/permissions/{username}/")
    suspend fun revokeFullAccessToUserForVehicle(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "username", encoded = false) username: String,
    ): Response<RevokedPermissions>

    @POST("api/car_companion/vehicles/{vin}/permissions/{username}/component/{component_type}/{component_name}/")
    suspend fun grantAccessToUserForComponent(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "username", encoded = false) username: String,
        @Path(value = "component_type", encoded = false) componentType: String,
        @Path(value = "component_name", encoded = false) componentName: String,
        @Body grantPermissionRequest: GrantPermissionRequest,
    ): Response<GrantedPermissions>

    @DELETE("api/car_companion/vehicles/{vin}/permissions/{username}/component/{component_type}/{component_name}/")
    suspend fun revokeAccessToUserForComponent(
        @Path(value = "vin", encoded = false) vin: String,
        @Path(value = "username", encoded = false) username: String,
        @Path(value = "component_type", encoded = false) componentType: String,
        @Path(value = "component_name", encoded = false) componentName: String,
    ): Response<RevokedPermissions>
}