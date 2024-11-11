package com.dsd.carcompanion.userRegistrationAndLogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.CreateUserRequest
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentRegistrationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationFragment : Fragment() {

    fun displayFormError(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+$)(?!.*(.)\\\\1{2}).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegistrationFragmentSubmit.setOnClickListener {
            val firstName: String = binding.etRegistrationFragmentFirstName.text.toString()
            val lastName: String = binding.etRegistrationFragmentLastName.text.toString()
            val email: String = binding.etRegistrationFragmentEmail.text.toString()
            val username: String = binding.etRegistrationFragmentUsername.text.toString()
            val password: String = binding.etRegistrationFragmentPassword.text.toString()
            val confirmPassword: String = binding.etRegistrationFragmentConfirmPassword.text.toString()

            //Need change in the next sprint!!!
            if(firstName.isEmpty()) {
                displayFormError("First name is required!")
            }
            else if(!Character.isUpperCase(firstName.get(0))){
                displayFormError("First name should start with a capital letter!")
            }
            else if(lastName.isEmpty()) {
                displayFormError("Last name is required!")
            }
            else if(!Character.isUpperCase(lastName.get(0))){
                displayFormError("Last name should start with a capital letter!")
            }
            else if(email.isEmpty()) {
                displayFormError("Email is required!")
            }
            else if(!isValidEmail(email)) {
                displayFormError("Email is not valid...")
            }
            else if(username.isEmpty()) {
                displayFormError("Username is required!")
            }
            else if(password.isEmpty()) {
                displayFormError("Password is required!")
            }
            else if(!isValidPassword(password)) {
                displayFormError("Password is not valid...\n" +
                        "Password should contain minimum of 8 characters\n" +
                        "Atleast 1 uppercase letter\n" +
                        "Atleast 1 lowercase letter\n" +
                        "Atleast 1 number\n" +
                        "Atleast 1 special character: @#\$%^&+=")
            }
            else if(confirmPassword.isEmpty()) {
                displayFormError("Confirm password is required!")
            }
            else if(password != confirmPassword) {
                displayFormError("Passwords must be the same!")
            } else {
                binding.textViewRegistrationInfo.setText("$firstName $lastName\n$email\n$username\n$password\n$confirmPassword")
                val createUserRequest = CreateUserRequest(
                    Email = email,
                    Username = username,
                    FirstName = firstName,
                    LastName = lastName,
                    Password = password)

                Log.d("Tester", createUserRequest.Password)

                Log.d("Tester", createUserRequest.toString())

                val userService = UserClient.apiService
                val authRepository = AuthRepository(userService, jwtTokenDataStore)

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = authRepository.register(createUserRequest)

                        withContext(Dispatchers.Main) {
                            if (response is ResultOf.Success) {
                                Log.d("Register Fragment", "Bravoo")
                            } else if (response is ResultOf.Error) {
                                Log.e("Register Fragment", "Register failed: ${response.message}")
                            } else {
                                Log.e("Register Fragment", "Something else")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("Register Fragment", "Error during login: ${e.message}")
                    }
                }
            }
        }

        binding.textViewToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_RegistrationFragment_to_LoginFragment)       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}