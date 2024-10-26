package com.dsd.carcompanion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class CodeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_code, container, false)
        val codeEditText = view.findViewById<EditText>(R.id.etCode)
        val submitButton = view.findViewById<Button>(R.id.btnSubmitCode)

        submitButton.setOnClickListener {
            val code = codeEditText.text.toString()
            if (isValidCode(code)) {
                (activity as? NavigationListener)?.navigateToNewPasswordScreen()
            } else {
                codeEditText.error = "Enter a valid 6-digit code"
            }
        }

        return view
    }

    private fun isValidCode(code: String): Boolean {
        // Simple check for 6-digit code
        return code.length == 6 && code.all { it.isDigit() }
    }
}
