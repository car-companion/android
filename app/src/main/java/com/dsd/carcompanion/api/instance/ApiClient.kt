package com.dsd.carcompanion.api.instance

import com.dsd.carcompanion.api.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://car-companion-dev.azurewebsites.net/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object UserClient {
    val apiService: UserService by lazy {
        RetrofitClient.retrofit.create(UserService::class.java)
    }
}