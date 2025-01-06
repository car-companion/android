package com.dsd.carcompanion.welcmeScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.databinding.ActivityWelcomeBinding
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
import com.dsd.carcompanion.utility.ImageHelper
import kotlinx.coroutines.Dispatchers
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
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            this,
            R.drawable.background_colors
        )

        // Delay for the splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                try {
                    // Check JWT token
                    val accessToken = withContext(Dispatchers.IO) {
                        jwtTokenDataStore.getAccessJwt()
                    }
                    if (!accessToken.isNullOrEmpty()) {
                        // Navigate to MainActivity
                        startActivity(Intent(this@WelcomeScreen, MainActivity::class.java))
                        finish()
                    } else {
                        // Navigate to UserStartActivity
                        startActivity(Intent(this@WelcomeScreen, UserStartActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("WelcomeScreen", "Error while checking JWT token: ${e.message}")
                }
            }
        }, 3000) // 3000 ms = 3 seconds
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up binding reference to avoid memory leaks
        _binding = null
    }
}
