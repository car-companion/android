package com.dsd.carcompanion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.databinding.ActivityMainBinding
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /*setSupportActionBar(binding.toolbar)

        //Will be needed in the next sprint
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)*/
//        binding.fab.setOnClickListener {
//        view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab)
//                .show()
//
//        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        jwtTokenDataStore = JwtTokenDataStore(context)

        lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) {
                    jwtTokenDataStore.getAccessJwt()
                }
                if (accessToken.isNullOrEmpty()) {
                    Log.d("MainActivity", "No Access JWT Token, navigating to UserStartActivity")
//                    val intent = Intent(this@MainActivity, UserStartActivity::class.java)
//                    startActivity(intent)
//                    finish()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error while checking JWT token: ${e.message}")
            }
        }
        return super.onCreateView(name, context, attrs)
    }

    //Will be needed in the next sprint
    /*override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }*/
}
