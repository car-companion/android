package com.dsd.carcompanion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_login)

        val usernameEditText = findViewById<EditText>(R.id.login_username_text_input)
        val passwordEditText = findViewById<EditText>(R.id.login_password_text_input)
        val submitButton = findViewById<Button>(R.id.login_submit_button)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        submitButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            resultTextView.text = "Username: $username\nPassword: $password"
        }

        val forgotPasswordTextView = findViewById<TextView>(R.id.login_forgotten_password_link)
        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        val signUpTextView = findViewById<TextView>(R.id.login_sign_up_link)
        signUpTextView.setOnClickListener {
            Toast.makeText(this, "Sign Up clicked", Toast.LENGTH_SHORT).show()
        }
    }
}