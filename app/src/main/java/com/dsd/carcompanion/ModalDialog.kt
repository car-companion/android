package com.dsd.carcompanion

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class ModalDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Directly call the function to show the Forgot Password modal
        showForgotPasswordDialog()
    }

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
