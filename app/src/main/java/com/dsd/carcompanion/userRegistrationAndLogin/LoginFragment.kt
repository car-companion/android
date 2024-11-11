package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        /*viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.resultTextView.append(jwtTokenDataStore.getAccessJwt())
            } catch (e: Exception) {
                // Handle any exceptions that might occur
                Log.e("LoginFragment", "Error during login: ${e.message}")
            }
        }*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginFragmentSubmit.setOnClickListener {

            val usernameEditText: String = binding.tilLoginFragmentUsername.editText?.text.toString()
            val passwordEditText: String = binding.tilLoginFragmentPassword.editText?.text.toString()

            val loginRequest = LoginRequest(username = usernameEditText, password = passwordEditText)

            val userService = UserClient.apiService
            val authRepository = AuthRepository(userService, jwtTokenDataStore)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = authRepository.login(loginRequest)

                    withContext(Dispatchers.Main) {
                        if (response is ResultOf.Success) {
                            Log.d("Login Fragment", "Bravoo")
                            binding.resultTextView.append("\n")
                            binding.resultTextView.append(jwtTokenDataStore.getAccessJwt())
                        } else if (response is ResultOf.Error) {
                            Log.e("LoginFragment", "Login failed: ${response.message}")
                        } else {
                            Log.e("Login Fragment", "Nekaj drugo")
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