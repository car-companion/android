package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentCodeBinding
import com.dsd.carcompanion.databinding.FragmentEmailBinding
import com.dsd.carcompanion.databinding.FragmentLoginBinding

class CodeFragment : Fragment() {

    private var _binding: FragmentCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitCode.setOnClickListener {
            val codeEditText = binding.etCode

            val codeText: String = binding.etCode.text.toString()

            if (isValidCode(codeText)) {
                findNavController().navigate(R.id.action_CodeForgotPassword_to_NewPassword)
            } else {
                codeEditText.error = "Enter a valid 6-digit code"
            }
        }
    }

    private fun isValidCode(code: String): Boolean {
        // Simple check for 6-digit code
        return code.length == 6 && code.all { it.isDigit() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
