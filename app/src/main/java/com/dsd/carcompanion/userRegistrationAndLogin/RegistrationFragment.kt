package com.dsd.carcompanion.userRegistrationAndLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentRegistrationBinding

class RegistrationFragment : Fragment() {

    fun displayFormError(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+$)(?!.*(.)\\\\1{2}).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val firstName: String = binding.editTextFirstName.text.toString()
            val lastName: String = binding.editTextLastName.text.toString()
            val email: String = binding.editTextEmail.text.toString()
            val country: String = binding.editTextCountry.text.toString()
            val password: String = binding.editTextPassword.text.toString()
            val confirmPassword: String = binding.editTextConfirmPassword.text.toString()

            if(firstName.isEmpty()) {
                displayFormError("First name is required!")
            }
            else if(lastName.isEmpty()) {
                displayFormError("Last name is required!")
            }
            else if(email.isEmpty()) {
                displayFormError("Email is required!")
            }
            else if(!isValidEmail(email)) {
                displayFormError("Email is not valid...")
            }
            else if(country.isEmpty()) {
                displayFormError("Country is required!")
            }
            else if(password.isEmpty()) {
                displayFormError("Password is required!")
            }
            else if(!isValidPassword(password)) {
                displayFormError("Password is not valid...")
            }
            else if(confirmPassword.isEmpty()) {
                displayFormError("Confirm password is required!")
            }
            else if(password != confirmPassword) {
                displayFormError("Passwords must be the same!")
            } else {
                binding.textViewRegistrationInfo.setText("$firstName $lastName\n$email\n$country\n$password\n$confirmPassword")
            }
        }

        binding.textViewToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_RegistrationFragment_to_LoginFragment)       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}