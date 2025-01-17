package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentEmailBinding
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EmailFragment : Fragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.imgBackground
        ImageHelper.applyBlurToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        binding.btnForgotPasswordSubmit.setOnClickListener {
            val emailEditText = binding.etForgotPasswordEnterEmail

            val email: String = binding.etForgotPasswordEnterEmail.text.toString()

            if (isValidEmail(email)) {
                sendEmailConfirmation(email)
            } else {
                emailEditText.error = getString(R.string.forgot_pass_fragment_message_email_error)
            }
        }
    }

    private fun sendEmailConfirmation(email: String) {
        val userService = UserClient.apiService
        val authRepository = AuthRepository(userService, jwtTokenDataStore)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authRepository.resetPassword(email)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showSuccessDialog()
                    } else {
                        // Extract error details from the response body
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginFragment", "Error during reset password: $errorBody")
                        showToast("Failed to send email confirmation: $errorBody")
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Unexpected error during reset password: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showToast("An unexpected error occurred.")
                }
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Reset password")
            .setMessage("Please check your inbox for reset password email and follow the steps." +
                    "After you successfully change the password, please login again")
            .setPositiveButton("Go to login") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.action_ForgotPasswordFragment_to_LoginFragment)
            }
            .create()

        dialog.show()
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
