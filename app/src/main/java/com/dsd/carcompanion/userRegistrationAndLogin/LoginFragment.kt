package com.dsd.carcompanion.userRegistrationAndLogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentLoginBinding
import com.dsd.carcompanion.databinding.FragmentRegistrationBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginSubmitButton.setOnClickListener {

            val usernameEditText: String = binding.loginUsernameTextInputLayout.editText?.text.toString()
            val passwordEditText: String = binding.loginPasswordTextInputLayout.editText?.text.toString()

            binding.resultTextView.text = getString(R.string.login_result_message, usernameEditText, passwordEditText)
        }

        binding.loginSignUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegistrationFragment)
        }

        binding.loginForgottenPasswordLink.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_ForgotPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}