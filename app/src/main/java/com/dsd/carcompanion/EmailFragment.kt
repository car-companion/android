package com.dsd.carcompanion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class EmailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email, container, false)
        val emailEditText = view.findViewById<EditText>(R.id.etEmail)
        val submitButton = view.findViewById<Button>(R.id.btnSubmitEmail)

        submitButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (isValidEmail(email)) {
                (activity as? NavigationListener)?.navigateToCodeScreen()
            } else {
                emailEditText.error = "Enter a valid email"
            }
        }

        return view
    }

    private fun isValidEmail(email: String): Boolean {
        // Basic email validation check
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
