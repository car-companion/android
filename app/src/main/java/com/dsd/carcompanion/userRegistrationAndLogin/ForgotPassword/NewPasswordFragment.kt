package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentEmailBinding
import com.dsd.carcompanion.databinding.FragmentNewPasswordBinding

class NewPasswordFragment : Fragment() {

    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val confirmPasswordEditText = binding.etConfirmPassword

        binding.btnSubmitNewPassword.setOnClickListener{
            val password: String = binding.etNewPassword.text.toString()
            val confirmPassword: String = binding.etConfirmPassword.text.toString()

            if (password == confirmPassword && password.length >= 6) {
                // Notify the user that the password has been changed
                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_NewPassword_to_LoginFragment)
            } else {
                // Show error if passwords do not match or are too short
                confirmPasswordEditText.error = "Passwords do not match or are too short"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
