package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.btnForgotPasswordSubmit.setOnClickListener {
            val emailEditText = binding.etForgotPasswordEnterEmail

            val email: String = binding.etForgotPasswordEnterEmail.text.toString()

            if (isValidEmail(email)) {
                findNavController().navigate(R.id.action_ForgotPassword_to_CodeForgotPassword)
            } else {
                emailEditText.error = getString(R.string.forgot_pass_fragment_message_email_error)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        // Basic email validation check
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
