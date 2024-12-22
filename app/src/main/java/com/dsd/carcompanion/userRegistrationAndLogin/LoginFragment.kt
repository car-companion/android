package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Handle button click for login
        binding.btnLoginFragmentSubmit.setOnClickListener {
            val usernameEditText = binding.tilLoginFragmentUsername.editText?.text.toString()
            val passwordEditText = binding.tilLoginFragmentPassword.editText?.text.toString()

            val loginRequest = LoginRequest(username = usernameEditText, password = passwordEditText)
            val userService = UserClient.apiService
            val authRepository = AuthRepository(userService, jwtTokenDataStore)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = authRepository.login(loginRequest)

                    withContext(Dispatchers.Main) {
                        if (response is ResultOf.Success) {
                            Log.d("Login Fragment", "Well done, you logged in")
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else if (response is ResultOf.Error) {
                            Log.e("LoginFragment", "Login failed: ${response.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Error during login: ${e.message}")
                }
            }
        }

        binding.linkLoginFragmentDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegistrationFragment)
        }

        binding.linkLoginFragmentForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_ForgotPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
