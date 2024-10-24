package com.dsd.carcompanion

import android.app.AlertDialog
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.dsd.carcompanion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate and set the layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup FAB action
        binding.fab.setOnClickListener {
//        view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab)
//                .show()

            // Show the Forgot Password modal dialog when FAB is clicked
            showForgotPasswordDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Function to show the Forgot Password dialog
    private fun showForgotPasswordDialog() {
        // Inflate the custom layout for the dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)

        // Create an AlertDialog and set the custom layout
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)

        // Create the dialog instance
        val alertDialog = dialogBuilder.create()

        // Find the views from the custom layout
        val emailEditText = dialogView.findViewById<EditText>(R.id.etEmail)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitEmail)

        // Handle the submit button click
        submitButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                // Perform forgot password logic (e.g., send a reset email)
                alertDialog.dismiss()  // Dismiss the dialog
            } else {
                // Show an error if the email field is empty
                emailEditText.error = "Please enter your email"
            }
        }

        // Show the dialog
        alertDialog.show()
    }
}
