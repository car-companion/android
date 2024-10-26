package com.dsd.carcompanion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dsd.carcompanion.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity(), NavigationListener {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the Email Fragment as the first screen
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EmailFragment())
            .commit()
    }

    override fun navigateToCodeScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CodeFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun navigateToNewPasswordScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NewPasswordFragment())
            .addToBackStack(null)
            .commit()
    }
}
