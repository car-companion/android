package com.dsd.carcompanion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.databinding.FragmentFirstBinding
import com.dsd.carcompanion.userRegistrationAndLogin.LoginFragment
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.textviewToken.text = jwtTokenDataStore.getAccessJwt()
            } catch (e: Exception) {
                // Handle any exceptions that might occur
                Log.e("First Fragmentt", "Error during loading of Token: ${e.message}")
            }
        }

        binding.logoutButton.setOnClickListener{
            logoutUser()
        }

        binding.vehicleOwnershipButton.setOnClickListener{
            findNavController().navigate(R.id.action_FirstFragment_to_VehicleOwnershipFragment)
        }
    }

    fun logoutUser(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                jwtTokenDataStore.clearAllTokens()

                binding.textviewToken.text = "Logged out, token was removed"

                val intent = Intent(requireContext(), UserStartActivity::class.java)
                startActivity(intent)

                requireActivity().finish()

            } catch (e: Exception) {
                Log.e("FirstFragment", "Error during logout: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}