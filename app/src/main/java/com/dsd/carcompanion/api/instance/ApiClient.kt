package com.dsd.carcompanion.api.instance

import com.dsd.carcompanion.api.service.UserService
import com.dsd.carcompanion.api.service.VehicleService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://car-companion-dev.azurewebsites.net/"

    // Default Retrofit instance (unauthenticated)
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Token-authenticated Retrofit instance
    fun createRetrofitWithToken(authToken: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val modifiedRequest = originalRequest.newBuilder()
                    .header("accept", "application/json")
                    .header("Authorization", "JWT $authToken")
                    .build()
                chain.proceed(modifiedRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
object UserClient {
    val apiService: UserService by lazy {
        RetrofitClient.retrofit.create(UserService::class.java)
    }
}

object VehicleClient {
    // Default API service (unauthenticated)
    val apiService: VehicleService by lazy {
        RetrofitClient.retrofit.create(VehicleService::class.java)
    }

    // Token-based API service
    fun getApiServiceWithToken(authToken: String): VehicleService {
        val retrofitWithToken = RetrofitClient.createRetrofitWithToken(authToken)
        return retrofitWithToken.create(VehicleService::class.java)
    }
}