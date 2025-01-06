package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentLoginBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        // Initialize BottomSheetBehavior
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llLoginFragmentBottomSheet)
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

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

        val textView = view.findViewById<TextView>(R.id.tv_swipe_up_hint)

        val spannable = SpannableString("Swipe up to explore\nthe world of car management")
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0, 8,  // Indices for "Swipe up"
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannable

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llLoginFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }



        // Handle button click for login
        binding.btnLoginFragmentSubmit.setOnClickListener {
            val username = binding.tilLoginFragmentUsername.editText?.text.toString().trim()
            val password = binding.tilLoginFragmentPassword.editText?.text.toString().trim()

            // Validate input fields
            if (username.isEmpty() || password.isEmpty()) {
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username = username, password = password)
            val userService = UserClient.apiService
            val authRepository = AuthRepository(userService, jwtTokenDataStore)

            binding.btnLoginFragmentSubmit.isEnabled = false
            binding.btnLoginFragmentSubmit.text = getString(R.string.logging_in)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = authRepository.login(loginRequest)

                    withContext(Dispatchers.Main) {
                        if (response is ResultOf.Success) {
                            Log.d("LoginFragment", "Login successful")
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else if (response is ResultOf.Error) {
                            Log.e("LoginFragment", "Login failed: ${response.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Error during login: ${e.message}")
                    withContext(Dispatchers.Main) {
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        binding.btnLoginFragmentSubmit.isEnabled = true
                        binding.btnLoginFragmentSubmit.text = getString(R.string.button_login)
                    }
                }
            }
        }

        binding.linkLoginFragmentForgotPassword.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.link)
        )
        binding.linkLoginFragmentForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_ForgotPasswordFragment)
        }

        binding.linkLoginFragmentDontHaveAccount.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.link)
        )
        binding.linkLoginFragmentDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegistrationFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
