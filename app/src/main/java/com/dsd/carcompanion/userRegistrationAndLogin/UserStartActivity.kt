package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.ActivityMainBinding
import com.dsd.carcompanion.databinding.ActivityUserStartBinding
import com.google.android.material.snackbar.Snackbar

class UserStartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityUserStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_login)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }
}