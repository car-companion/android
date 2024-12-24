package com.dsd.carcompanion.api.instance

import android.util.Log
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
                Log.d("RetrofitClient", "Request with Token: ${modifiedRequest}")
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
