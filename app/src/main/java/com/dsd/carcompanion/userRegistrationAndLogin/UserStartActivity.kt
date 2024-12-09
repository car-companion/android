package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.navigation.findNavController
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.ActivityUserStartBinding

class UserStartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_login)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
    }
}