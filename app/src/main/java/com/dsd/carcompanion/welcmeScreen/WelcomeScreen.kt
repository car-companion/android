package com.dsd.carcompanion.welcmeScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.ActivityWelcomeBinding
import com.dsd.carcompanion.utility.ImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeScreen : AppCompatActivity() {

    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _binding: ActivityWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout
        _binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize JwtTokenDataStore
        jwtTokenDataStore = JwtTokenDataStore(this)

        // Apply image effects
        val imageView = binding.imgBackground
        ImageHelper.applyBlurToImageView(
            imageView,
            this,
            R.drawable.background_colors
        )

        logoutUser()
        binding.tvErrorText.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(1000) // 3-second delay
            checkJwtToken()
        }
    }

    private fun checkJwtToken() {
        lifecycleScope.launch {
            try {
                // Check JWT token
                val accessToken = withContext(Dispatchers.IO) {
                    jwtTokenDataStore.getAccessJwt()
                }
                loginTestUser()
            } catch (e: Exception) {
                Log.e("WelcomeScreen", "Error while checking JWT token: ${e.message}")
            }
        }
    }

    private fun loginTestUser() {
        val userName = "presentationUser"
        val password = "MojaIzmisljenaSigurnaLozinka12345"

        val loginRequest = LoginRequest(username = userName, password = password)
        val userService = UserClient.apiService
        val authRepository = AuthRepository(userService, jwtTokenDataStore)

        lifecycleScope.launch {
            delay(2000) // Delay first execution by 2 seconds

            var attempt = 0
            var success = false

            while (attempt < 3 && !success) {
                attempt++
                try {
                    val response = withContext(Dispatchers.IO) {
                        authRepository.login(loginRequest)
                    }

                    if (response is ResultOf.Success) {
                        Log.d("WelcomeScreen", "Login successful on attempt $attempt")
                        startActivity(Intent(this@WelcomeScreen, MainActivity::class.java))
                        finish()
                        success = true
                    } else if (response is ResultOf.Error) {
                        Log.e("WelcomeScreen", "Login failed on attempt $attempt: ${response.message}")
                    } else {
                        Log.e("WelcomeScreen", "Unexpected login result on attempt $attempt")
                    }
                } catch (e: Exception) {
                    Log.e("WelcomeScreen", "Error during login on attempt $attempt: ${e.message}")
                }

                if (!success && attempt < 3) {
                    delay(1000) // Optional: wait 1 second before retrying
                }
            }

            if (!success) {
                Log.e("WelcomeScreen", "Login failed after 3 attempts")
                binding.progressBar.visibility = View.GONE
                binding.tvErrorText.visibility = View.VISIBLE
            }
        }
    }

    private fun logoutUser(){
        lifecycleScope.launch {
            try {
                jwtTokenDataStore.clearAllTokens()

            } catch (e: Exception) {
                Log.e("WelcomeScreen", "Error during logout: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up binding reference to avoid memory leaks
        _binding = null
    }
}
