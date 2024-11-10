package com.dsd.carcompanion.userRegistrationAndLogin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.api.models.LoginRequest
import com.dsd.carcompanion.api.models.TokenModel
import com.dsd.carcompanion.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jwt_tokens")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginFragmentSubmit.setOnClickListener {

            val usernameEditText: String = binding.tilLoginFragmentUsername.editText?.text.toString()
            val passwordEditText: String = binding.tilLoginFragmentPassword.editText?.text.toString()

            val loginRequest = LoginRequest(username = usernameEditText, password = passwordEditText)

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Make the suspend API call here
                    val response: Response<TokenModel> =
                        UserClient.apiService.loginUser(loginRequest)

                    if (response.isSuccessful) {
                        val resp = response.body()
                        // Handle the retrieved users data
                        Log.d("LoginFragment", "Login successfully: $resp")
                    } else {
                        // Handle error (e.g., response not successful)
                        Log.e("LoginFragment", "Error: ${response.code()}")
                    }
                } catch (e: Exception) {
                    // Handle network or API call failure
                    Log.e("LoginFragment", "Failure: ${e.message}")
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