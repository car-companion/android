package com.dsd.carcompanion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class NewPasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_password, container, false)

        // Find views
        val passwordEditText = view.findViewById<EditText>(R.id.etNewPassword)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.etConfirmPassword)
        val submitButton = view.findViewById<Button>(R.id.btnSubmitNewPassword)

        // Set onClick listener for the submit button
        submitButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check if passwords match and are at least 6 characters long
            if (password == confirmPassword && password.length >= 6) {
                // Notify the user that the password has been changed
                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()

                // Navigate back to MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()  // Optionally finish the current activity
            } else {
                // Show error if passwords do not match or are too short
                confirmPasswordEditText.error = "Passwords do not match or are too short"
            }
        }

        return view
    }
}
