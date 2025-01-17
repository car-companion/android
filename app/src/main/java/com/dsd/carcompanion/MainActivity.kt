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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ComponentStatusUpdate
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.ActivityMainBinding
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the views
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        var navigationButton: ImageView = findViewById(R.id.activity_main_menu_icon)

        binding.root.post {
            try {
                navController = findNavController(R.id.nav_host_fragment_content_main)

                // Open the drawer when the button is clicked
                navigationButton.setOnClickListener {
                    drawerLayout.openDrawer(navigationView) // Opens the drawer
                }

                // Handle menu item clicks
                navigationView.setNavigationItemSelectedListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.nav_landing_page -> {
                            navController.navigate(R.id.nav_LandingPage)
                        }
                        R.id.nav_user_access -> {
                            navController.navigate(R.id.nav_UserPermissionsFragment)
                        }
                        R.id.nav_get_ownership -> {
                            navController.navigate(R.id.nav_VehicleOwnershipFragment)
                        }
                        R.id.nav_settings -> {
                            navController.navigate(R.id.nav_SettingsFragment)
                        }
                        R.id.nav_logout -> {
                            logoutUser()
                        }
                    }
                    drawerLayout.closeDrawers()
                    true
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing NavController: ${e.message}")
            }
        }

        /*binding.menuIcon.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.menuIcon)
            popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_SettingsFragment)
                        true
                    }
                    R.id.action_access -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_UserPermissionsFragment)
                        true
                    }
                    R.id.action_add_vehicle -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_VehicleOwnershipFragment)
                        true
                    }
                    R.id.action_logout -> {
                        logoutUser();
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }*/


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
                    val intent = Intent(this@MainActivity, UserStartActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error while checking JWT token: ${e.message}")
            }
        }
        return super.onCreateView(name, context, attrs)
    }

    fun logoutUser(){
        lifecycleScope.launch {
            try {
                jwtTokenDataStore.clearAllTokens()

                val intent = Intent(this@MainActivity, UserStartActivity::class.java)
                startActivity(intent)
                this@MainActivity.finish()
            } catch (e: Exception) {
                Log.e("FirstFragment", "Error during logout: ${e.message}")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
