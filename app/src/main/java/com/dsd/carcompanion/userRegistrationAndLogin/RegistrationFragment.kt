package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentRegistrationBinding
import com.dsd.carcompanion.utility.CustomBottomSheetBehavior
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationFragment : Fragment() {
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
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        val behavior = BottomSheetBehavior.from(binding.llRegistrationFragmentBottomSheet)
        if (behavior is CustomBottomSheetBehavior) {
            behavior.setDraggableViewId(R.id.ll_registration_fragment_dragable_part) // Setting draggable part
        }
        _bottomSheetBehavior = behavior
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        _bottomSheetBehavior?.peekHeight = 150
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.isDraggable = true // Allow dragging based on the custom behavior

        // Expand bottom sheet when draggable guide is tapped
        binding.llRegistrationFragmentDragablePart.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        setOnChangeRemoveErrors()

        binding.btnRegistrationFragmentSubmit.setOnClickListener {
            val firstName: String = binding.etRegistrationFragmentFirstName.text.toString()
            val lastName: String = binding.etRegistrationFragmentLastName.text.toString()
            val email: String = binding.etRegistrationFragmentEmail.text.toString()
            val username: String = binding.etRegistrationFragmentUsername.text.toString()
            val password: String = binding.etRegistrationFragmentPassword.text.toString()
            val confirmPassword: String = binding.etRegistrationFragmentConfirmPassword.text.toString()

            binding.tvErrorUnexpcted.visibility = View.GONE

            var flag = validateInput(firstName, lastName, email, username, password, confirmPassword)

            if(!flag) {
                val createUserRequest = CreateUserRequest(
                    Email = email,
                    Username = username,
                    FirstName = firstName,
                    LastName = lastName,
                    Password = password)

                val userService = UserClient.apiService
                val authRepository = AuthRepository(userService, jwtTokenDataStore)

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = authRepository.register(createUserRequest)

                        withContext(Dispatchers.Main) {
                            if (response is ResultOf.Success) {
                                Log.d("Register Fragment", "User successfully registered")
                                loginUser(username, password)
                            } else if (response is ResultOf.Error) {
                                binding.tvErrorUnexpcted.text = "Registration has failed. Please check inputted information and try again"
                                binding.tvErrorUnexpcted.visibility = View.VISIBLE
                                Log.e("Register Fragment", "Register failed: ${response.message}")
                            } else {
                                binding.tvErrorUnexpcted.text = "Unexpected error has occured on server side. Please try again"
                                binding.tvErrorUnexpcted.visibility = View.VISIBLE
                                Log.e("Register Fragment", "Something else")
                            }
                        }

                    } catch (e: Exception) {
                        binding.tvErrorUnexpcted.text = "Unexpected error has occured on server side. Please try again"
                        binding.tvErrorUnexpcted.visibility = View.VISIBLE
                        Log.e("Register Fragment", "Error during registration: ${e.message}")
                    }
                }
            }
        }

        binding.textViewToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_RegistrationFragment_to_LoginFragment)       }
    }

    fun loginUser(username: String, password: String){
        val loginRequest = LoginRequest(username = username, password = password)

        val userService = UserClient.apiService
        val authRepository = AuthRepository(userService, jwtTokenDataStore)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authRepository.login(loginRequest)

                withContext(Dispatchers.Main) {
                    if (response is ResultOf.Success) {
                        Log.d("Register Fragment", "Well done, you registred. Going to main activity")
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else if (response is ResultOf.Error) {
                        binding.tvErrorUnexpcted.text = "Unexpected error has occured on server side when logging in. Please try again"
                        binding.tvErrorUnexpcted.visibility = View.VISIBLE
                        Log.e("Register Fragment", "Register failed: ${response.message}")
                    } else {
                        binding.tvErrorUnexpcted.text = "Unexpected error has occured on server side when logging in. Please try again"
                        binding.tvErrorUnexpcted.visibility = View.VISIBLE
                        Log.e("Register Fragment", "Something else")
                    }
                }
            } catch (e: Exception) {
                binding.tvErrorUnexpcted.text = "Unexpected error has occured on server side when logging in. Please try again"
                binding.tvErrorUnexpcted.visibility = View.VISIBLE
                Log.e("Register Fragment", "Error during registration: ${e.message}")
            }
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        username: String,
        password: String,
        confirmPassword: String): Boolean{

        var flag = false
        var flagHelper = false

        flagHelper = validateInputFirstName(firstName)
        flag = flagHelper

        flag = validateInputLastName(lastName)
        if(!flag){ flag = flagHelper }

        flag = validateInputEmail(email)
        if(!flag){ flag = flagHelper }

        flag = validateInputUsername(username)
        if(!flag){ flag = flagHelper }

        flag = validateInputPasswordAndConfirm(password, confirmPassword)
        if(!flag){ flag = flagHelper }

        return flag
    }

    private fun validateInputFirstName(firstName: String): Boolean{
        binding.tvErrorFirstName.visibility = View.GONE
        if(firstName.isEmpty()){
            binding.tvErrorFirstName.text = buildString {
                append("First name ")
                append(binding.root.context.getString(R.string.field_required))
            }
            binding.tvErrorFirstName.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun validateInputLastName(lastName: String): Boolean{
        binding.tvErrorLastName.visibility = View.GONE
        if(lastName.isEmpty()){
            binding.tvErrorLastName.text = buildString {
                append("Last name ")
                append(binding.root.context.getString(R.string.field_required))
            }
            binding.tvErrorLastName.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun validateInputEmail(email: String): Boolean{
        binding.tvErrorEmail.visibility = View.GONE
        if(email.isEmpty()){
            binding.tvErrorEmail.text = buildString {
                append("Email ")
                append(binding.root.context.getString(R.string.field_required))
            }
            binding.tvErrorEmail.visibility = View.VISIBLE
            return true
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.tvErrorEmail.text = "Inputted email address is not in valid format. Please try again."
            binding.tvErrorEmail.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun validateInputUsername(username: String): Boolean{
        binding.tvErrorUsername.visibility = View.GONE
        if(username.isEmpty()){
            binding.tvErrorUsername.text = buildString {
                append("Username ")
                append(binding.root.context.getString(R.string.field_required))
            }
            binding.tvErrorUsername.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun validateInputPasswordAndConfirm(password: String, confirmPassword: String): Boolean {
        binding.tvErrorPassword.visibility = View.GONE
        binding.tvErrorConfirmPassword.visibility = View.GONE

        if (password.isEmpty()) {
            binding.tvErrorPassword.text = buildString {
                append("Password ")
                append(binding.root.context.getString(R.string.field_required))
            }
            binding.tvErrorPassword.visibility = View.VISIBLE
            return true
        }

        var text = ""
        if (password.length < 8) {
            if(!text.isEmpty()) { text += "\n" }
            text +=  "Password must be at least 8 characters long."
        }
        if (!password.contains(Regex("[A-Z]"))) {
            if(!text.isEmpty()) { text += "\n" }
            text += "Password must contain at least one uppercase letter."
        }
        if (!password.contains(Regex("[a-z]"))) {
            if(!text.isEmpty()) { text += "\n" }
            text += "Password must contain at least one lowercase letter."
        }
        if (!password.contains(Regex("\\d"))) {
            if(!text.isEmpty()) { text += "\n" }
            text += "Password must contain at least one number."
        }
        if (!password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]"))) {
            if(!text.isEmpty()) { text += "\n" }
            text += "Password must contain at least one special character."
        }
        if (password.contains(Regex("(.)\\1{3,}"))) {
            if(!text.isEmpty()) { text += "\n" }
            text += "Password must not contain more than three of the same characters in a row."
        }

        if(!text.isEmpty()) {
            binding.tvErrorPassword.text = text
            binding.tvErrorPassword.visibility = View.VISIBLE
            return true
        }

        if(!confirmPassword.equals(password)){
            binding.tvErrorConfirmPassword.text = "Passwords don't match. Please try again."
            binding.tvErrorConfirmPassword.visibility = View.VISIBLE
            return true
        }
        return false
    }

    private fun setOnChangeRemoveErrors() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvErrorFirstName.visibility = View.GONE
                binding.tvErrorLastName.visibility = View.GONE
                binding.tvErrorEmail.visibility = View.GONE
                binding.tvErrorUsername.visibility = View.GONE
                binding.tvErrorPassword.visibility = View.GONE
                binding.tvErrorConfirmPassword.visibility = View.GONE
                binding.tvErrorUnexpcted.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        }

        // Attach the TextWatcher to each EditText
        binding.etRegistrationFragmentFirstName.addTextChangedListener(textWatcher)
        binding.etRegistrationFragmentLastName.addTextChangedListener(textWatcher)
        binding.etRegistrationFragmentEmail.addTextChangedListener(textWatcher)
        binding.etRegistrationFragmentUsername.addTextChangedListener(textWatcher)
        binding.etRegistrationFragmentPassword.addTextChangedListener(textWatcher)
        binding.etRegistrationFragmentConfirmPassword.addTextChangedListener(textWatcher)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}