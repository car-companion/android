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
import com.dsd.carcompanion.utility.CustomBottomSheetBehavior
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

        val behavior = BottomSheetBehavior.from(binding.llLoginFragmentBottomSheet)
        if (behavior is CustomBottomSheetBehavior) {
            behavior.setDraggableViewId(R.id.ll_login_fragment_draggable_part_upper)
        }
        _bottomSheetBehavior = behavior
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        _bottomSheetBehavior?.peekHeight = 800
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.isDraggable = true // Allow dragging based on the custom behavior

        val textView = view.findViewById<TextView>(R.id.tv_swipe_up_hint)

        val spannable = SpannableString("Swipe up to explore\nthe world of car management")
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0, 8,  // Indices for "Swipe up"
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannable

        // Adding a listener to handle state changes
        _bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.llLoginFragmentDraggablePartUpper.visibility = View.GONE
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.llLoginFragmentDraggablePartUpper.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        binding.btnLoginFragmentSubmit.setOnClickListener {
            binding.tvLoginFragmentErrorLogin.visibility = View.GONE

            val usernameEditText: String = binding.tilLoginFragmentUsername.editText?.text.toString()
            val passwordEditText: String = binding.tilLoginFragmentPassword.editText?.text.toString()

            val flag = checkUserNamePasswordEmpty(usernameEditText, passwordEditText)

            if(flag){
                binding.tvLoginFragmentErrorLogin.visibility = View.VISIBLE
            }
            else {
                val loginRequest = LoginRequest(username = usernameEditText, password = passwordEditText)

                val userService = UserClient.apiService
                val authRepository = AuthRepository(userService, jwtTokenDataStore)

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = authRepository.login(loginRequest)

                        withContext(Dispatchers.Main) {
                            if (response is ResultOf.Success) {
                                Log.d("Login Fragment", "Well done, you registred")
                                val intent = Intent(requireActivity(), MainActivity::class.java)
                                startActivity(intent)

                                requireActivity().finish()
                            } else if (response is ResultOf.Error) {
                                binding.tvLoginFragmentErrorLogin.text = "Wrong username or password. Please try again."
                                binding.tvLoginFragmentErrorLogin.visibility = View.VISIBLE
                                Log.e("LoginFragment", "Login failed: ${response.message}")
                            } else {
                                binding.tvLoginFragmentErrorLogin.text = "Something went wrong during login. Please try again."
                                binding.tvLoginFragmentErrorLogin.visibility = View.VISIBLE
                                Log.e("Login Fragment", "Something else")
                            }
                        }
                    } catch (e: Exception) {
                        binding.tvLoginFragmentErrorLogin.text = "Couldn't connect to the server. Please check your internet connection and try again."
                        binding.tvLoginFragmentErrorLogin.visibility = View.VISIBLE
                        Log.e("LoginFragment", "Error during login: ${e.message}")
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

        binding.linkLoginFragmentDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegistrationFragment)
        }
    }

    private fun checkUserNamePasswordEmpty(username: String , password: String): Boolean {
        binding.tvLoginFragmentErrorLogin.text = ""
        var text = ""
        var flag = false
        if(username == "" || username == null){
            text += "Username field can not be empty."
            flag = true;
        }
        if(password == "" || password == null){
            if(flag){
                text += "\n"
            }
            text += "Password field can not be empty."
            flag = true
        }
        if(flag){
            binding.tvLoginFragmentErrorLogin.text = text
        }
        return flag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
