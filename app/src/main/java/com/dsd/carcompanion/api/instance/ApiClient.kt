package com.dsd.carcompanion.api.instance

import com.dsd.carcompanion.api.service.UserService
import com.dsd.carcompanion.api.service.VehicleService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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

    // Function to create OkHttpClient with Interceptor for dynamic token
    private fun createOkHttpClientWithToken(token: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val originalRequest = chain.request()
                    val modifiedRequest: Request = originalRequest.newBuilder()
                        .header("accept", "application/json")
                        .header("Authorization", "JWT $token") // Add Authorization header
                        .build()
                    return chain.proceed(modifiedRequest)
                }
            })
            .build()
    }

    // Function to create Retrofit instance with dynamic token
    fun createRetrofitWithToken(token: String): Retrofit {
        val client = createOkHttpClientWithToken(token)
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use the customized OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// Keep the existing UserClient unchanged
object UserClient {
    val apiService: UserService by lazy {
        RetrofitClient.retrofit.create(UserService::class.java)
    }
}

// Update VehicleClient to support token-based configuration
object VehicleClient {
    fun apiService(token: String): VehicleService {
        val formattedToken = "JWT$token"
        return RetrofitClient.createRetrofitWithToken(formattedToken).create(VehicleService::class.java)
    }
}
