package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentEmailBinding
import androidx.navigation.fragment.findNavController


class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitEmail.setOnClickListener {
            val emailEditText = binding.etEmail

            val email: String = binding.etEmail.text.toString()

            if (isValidEmail(email)) {
                findNavController().navigate(R.id.action_ForgotPassword_to_CodeForgotPassword)
            } else {
                emailEditText.error = "Enter a valid email"
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Basic email validation check
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
