package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentRegistrationBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationFragment : BottomSheetDialogFragment() {

    fun displayFormError(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+$)(?!.*(.)\\\\1{2}).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        jwtTokenDataStore = JwtTokenDataStore(requireContext())
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        // Initialize BottomSheetBehavior
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llRegistrationFragmentBottomSheet)
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

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llRegistrationFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Registration submit button logic
        binding.btnRegistrationFragmentSubmit.setOnClickListener {
//            val firstName = binding.etRegistrationFragmentFirstName.text.toString()
//            val lastName = binding.etRegistrationFragmentLastName.text.toString()
            val email = binding.etRegistrationFragmentEmail.text.toString()
            val username = binding.etRegistrationFragmentUsername.text.toString()
            val password = binding.etRegistrationFragmentPassword.text.toString()
            val confirmPassword = binding.etRegistrationFragmentConfirmPassword.text.toString()

            if (validateForm(firstName="", lastName="", email, username, password, confirmPassword)) {
                val createUserRequest = CreateUserRequest(
                    Email = email,
                    Username = username,
                    FirstName = "",//firstName,
                    LastName = "",//lastName,
                    Password = password
                )

                Log.d("Tester", createUserRequest.toString())

                val userService = UserClient.apiService
                val authRepository = AuthRepository(userService, jwtTokenDataStore)

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = authRepository.register(createUserRequest)

                        withContext(Dispatchers.Main) {
                            if (response is ResultOf.Success) {
                                Log.d("Register Fragment", "Registration Successful!")
                                loginUser(username, password)
                            } else if (response is ResultOf.Error) {
                                Log.e("Register Fragment", "Registration failed: ${response.message}")
                                displayFormError("Registration failed: ${response.message}")
                            } else {
                                Log.e("Register Fragment", "Unexpected response")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Register Fragment", "Error during registration: ${e.message}")
                        displayFormError("An error occurred during registration.")
                    }
                }
            }
        }

        // Navigate to Login
        binding.textViewToLogin.setOnClickListener {
            dismiss() // Close the bottom sheet
            // Use Navigation to transition to the LoginFragment
        }
    }

    private fun validateForm(
        firstName: String,
        lastName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            firstName.isEmpty() -> {
                displayFormError("First name is required!")
                false
            }
            !Character.isUpperCase(firstName[0]) -> {
                displayFormError("First name should start with a capital letter!")
                false
            }
            lastName.isEmpty() -> {
                displayFormError("Last name is required!")
                false
            }
            !Character.isUpperCase(lastName[0]) -> {
                displayFormError("Last name should start with a capital letter!")
                false
            }
            email.isEmpty() -> {
                displayFormError("Email is required!")
                false
            }
            !isValidEmail(email) -> {
                displayFormError("Invalid email address!")
                false
            }
            username.isEmpty() -> {
                displayFormError("Username is required!")
                false
            }
            password.isEmpty() -> {
                displayFormError("Password is required!")
                false
            }
            !isValidPassword(password) -> {
                displayFormError("Password is not valid!\nPassword should contain:\n- Minimum 8 characters\n- At least 1 uppercase letter\n- At least 1 lowercase letter\n- At least 1 number\n- At least 1 special character: @#\$%^&+=")
                false
            }
            confirmPassword.isEmpty() -> {
                displayFormError("Confirm password is required!")
                false
            }
            password != confirmPassword -> {
                displayFormError("Passwords do not match!")
                false
            }
            else -> true
        }
    }

    private fun loginUser(username: String, password: String) {
        val loginRequest = LoginRequest(username = username, password = password)

        val userService = UserClient.apiService
        val authRepository = AuthRepository(userService, jwtTokenDataStore)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authRepository.login(loginRequest)

                withContext(Dispatchers.Main) {
                    if (response is ResultOf.Success) {
                        Log.d("Register Fragment", "Login Successful! Navigating to MainActivity.")
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else if (response is ResultOf.Error) {
                        Log.e("Register Fragment", "Login failed: ${response.message}")
                        displayFormError("Login failed: ${response.message}")
                    } else {
                        Log.e("Register Fragment", "Unexpected response")
                    }
                }
            } catch (e: Exception) {
                Log.e("Register Fragment", "Error during login: ${e.message}")
                displayFormError("An error occurred during login.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
