package com.dsd.carcompanion.api.instance

import com.dsd.carcompanion.api.service.VehicleService

object VehicleClient {
    // Default API service (unauthenticated)
    val apiService: VehicleService by lazy {
        RetrofitClient.retrofit.create(VehicleService::class.java)
    }

    // Token-based API service
    fun getApiServiceWithToken(authToken: String): VehicleService {
        return RetrofitClient.createRetrofitWithToken(authToken).create(VehicleService::class.java)
    }
}
