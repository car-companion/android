package com.dsd.carcompanion.userRegistrationAndLogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentLoginBinding
import com.dsd.carcompanion.utils.ResultOf
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        binding.btnLoginFragmentSubmit.setOnClickListener {

            val usernameEditText: String = binding.tilLoginFragmentUsername.editText?.text.toString()
            val passwordEditText: String = binding.tilLoginFragmentPassword.editText?.text.toString()

            binding.resultTextView.text = getString(R.string.login_fragment_textview_result_message,
                usernameEditText, passwordEditText)

            loginViewModel.login(usernameEditText, passwordEditText)
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginState.collect { result ->
                when (result) {
                    is ResultOf.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ResultOf.Success -> {
                        binding.progressBar.visibility = View.GONE
                        navigateToHomeScreen()
                    }
                    is ResultOf.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    is ResultOf.Idle -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun navigateToHomeScreen() {
        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
        // Navigate to the home screen after successful login
    }
}