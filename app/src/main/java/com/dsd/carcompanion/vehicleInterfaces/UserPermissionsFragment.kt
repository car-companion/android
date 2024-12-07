package com.dsd.carcompanion.vehicleInterfaces

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.databinding.FragmentGrantPermissionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserPermissionsFragment : Fragment() {

    private var _binding: FragmentGrantPermissionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrantPermissionsBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch and display the user's vehicles
        lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) {
                    jwtTokenDataStore.getAccessJwt() // Retrieve JWT token
                }

                if (!accessToken.isNullOrEmpty()) {
                    binding.tvShowApi.text = "Token retrieved successfully: $accessToken"
                    Log.d("Token","JWT$accessToken")
                    fetchVehicles(accessToken) // Fetch vehicle data using the token
                } else {
                    binding.tvShowApi.text = "No Access JWT Token found"
                    Toast.makeText(context, "No Access JWT Token found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.tvShowApi.text = "Error fetching token: ${e.message}"
                Log.e("FetchTokenError", "Error: ${e.message}")
                Toast.makeText(context, "Error fetching token: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        setupAccessLevelListener()

        binding.btnGrantAccess.setOnClickListener {
            val userIdentifier = binding.etUserIdentifier.text.toString()
            val selectedVehicle = binding.spinnerVehicleSelection.selectedItem?.toString()
            val selectedAccessLevel = when (binding.radioGroupAccessLevel.checkedRadioButtonId) {
                R.id.radio_full_access -> "Full Access"
                R.id.radio_custom_access -> "Custom Access"
                else -> null
            }

            if (userIdentifier.isNotEmpty() && selectedVehicle != null && selectedAccessLevel != null) {
                val grantedPermissions = getSelectedPermissions()
                Toast.makeText(
                    context,
                    "Access granted to $userIdentifier for $selectedVehicle with $selectedAccessLevel access: $grantedPermissions",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun fetchVehicles(token: String) {
        try {
            // Create a VehicleService instance with the token
            val vehicleService = VehicleClient.apiService(token)

            // Fetch the list of vehicles
            val vehicles = vehicleService.getMyVehicles()

            // Update the Spinner with the retrieved vehicles
            val vehicleNames = vehicles.map { it.nickname }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                vehicleNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerVehicleSelection.adapter = adapter
        } catch (e: Exception) {
            Log.e("UserPermissionsFragment", "Error fetching vehicles: ${e.message}")
            Toast.makeText(context, "Error fetching vehicles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAccessLevelListener() {
        binding.radioGroupAccessLevel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_full_access -> setPermissions(locks = true, lights = true, temperature = true, extra = true)
                R.id.radio_custom_access -> enableAllCheckBoxes(true) // Allow user customization
            }
        }
    }

    private fun setPermissions(temperature: Boolean, locks: Boolean, lights: Boolean, extra: Boolean) {
        binding.checkboxTemp.isChecked = temperature
        binding.checkboxLocks.isChecked = locks
        binding.checkboxLights.isChecked = lights
        binding.checkboxExtra.isChecked = extra

        enableAllCheckBoxes(false) // Disable further interaction
    }

    private fun enableAllCheckBoxes(enable: Boolean) {
        binding.checkboxTemp.isEnabled = enable
        binding.checkboxLocks.isEnabled = enable
        binding.checkboxLights.isEnabled = enable
        binding.checkboxExtra.isEnabled = enable
    }

    private fun getSelectedPermissions(): String {
        val permissions = mutableListOf<String>()
        if (binding.checkboxTemp.isChecked) permissions.add("Temperature")
        if (binding.checkboxLocks.isChecked) permissions.add("Locks")
        if (binding.checkboxLights.isChecked) permissions.add("Lights")
        if (binding.checkboxExtra.isChecked) permissions.add("Extra")
        return permissions.joinToString(", ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
