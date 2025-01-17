package com.dsd.carcompanion.api.instance

import com.dsd.carcompanion.api.service.UserService

object UserClient {
    val apiService: UserService by lazy {
        RetrofitClient.retrofit.create(UserService::class.java)
    }

    // Token-based API service
    fun getApiServiceWithToken(authToken: String): UserService {
        return RetrofitClient.createRetrofitWithToken(authToken).create(UserService::class.java)
    }
}
