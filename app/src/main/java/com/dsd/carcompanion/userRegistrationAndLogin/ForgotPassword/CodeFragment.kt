package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentCodeBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior

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

        val imageView = binding.imgBackground
        ImageHelper.applyBlurToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        binding.btnForgotPasswordVerify.setOnClickListener {
            val codeEditText = binding.etForgotPasswordEnterCode

            val codeText: String = binding.etForgotPasswordEnterCode.text.toString()

            if (isValidCode(codeText)) {
                findNavController().navigate(R.id.action_CodeForgotPassword_to_NewPassword)
            } else {
                codeEditText.error = getString(R.string.forgot_pass_fragment_message_code_error)
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
