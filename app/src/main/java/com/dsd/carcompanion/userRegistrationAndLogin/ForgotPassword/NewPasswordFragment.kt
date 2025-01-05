package com.dsd.carcompanion.userRegistrationAndLogin.ForgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentNewPasswordBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior

class NewPasswordFragment : Fragment() {

    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!

    private var _bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)

        // Initialize BottomSheetBehavior
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llNewPasswordFragmentBottomSheet)
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.imgBackground
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llNewPasswordFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val confirmPasswordEditText = binding.etNewPasswordConfirmPassword

        binding.btnNewPasswordSubmit.setOnClickListener{
            val password: String = binding.etNewPasswordEnterPassword.text.toString()
            val confirmPassword: String = binding.etNewPasswordConfirmPassword.text.toString()

            if (password == confirmPassword && password.length >= 6) {
                Toast.makeText(context, getString(R.string.forgot_pass_fragment_message_pass_change_success),
                    Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_NewPassword_to_LoginFragment)
            } else {
                // Show error if passwords do not match or are too short
                confirmPasswordEditText.error =
                    getString(R.string.forgot_pass_fragment_message_pass_change_fail)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
