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
import com.dsd.carcompanion.databinding.FragmentGrantPermissionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.VehicleResponse

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

        // Fetch and display vehicles
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if (!accessToken.isNullOrEmpty()) {
                    fetchVehicles(accessToken) // Fetch vehicles using the token
                    binding.tvShowApi.text = "Token Found: JWT $accessToken"
                } else {
                    showToast("No Access JWT Token found")
                }
            } catch (e: Exception) {
                Log.e("UserPermissionsFragment", "Error fetching token: ${e.message}", e)
                showToast("Error fetching token: ${e.message}")
            }
        }

        setupAccessLevelListener()

        binding.btnGrantAccess.setOnClickListener { handleGrantAccessClick() }
    }

    private fun setupAccessLevelListener() {
        binding.radioGroupAccessLevel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_full_access -> setPermissions(locks = true, lights = true, temperature = true, extra = true)
                R.id.radio_custom_access -> enableAllCheckBoxes(true) // Allow user customization
            }
        }
    }

    private suspend fun fetchVehicles(accessToken: String) {
        try {
            // Get the API service with the token
            val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)

            // Fetch the list of vehicles
            val vehicles = vehicleService.getMyVehicles()

            if (vehicles.isEmpty()) {
                // No vehicles available
                binding.tvShowVehicles.text = "No vehicles associated with this account."
                updateVehicleSpinner(emptyList())
            } else {
                // Format the vehicle details for display (optional)
                val vehicleInfo = vehicles.joinToString("\n") { vehicle ->
                    "Nickname: ${vehicle.nickname ?: "N/A"}, VIN: ${vehicle.vin}, " +
                            "Model: ${vehicle.model.name} (${vehicle.model.manufacturer}), " +
                            "Year: ${vehicle.year_built}, " +
                            "Interior Color: ${vehicle.interior_color.name}, " +
                            "Outer Color: ${vehicle.outer_color.name}"
                }
                binding.tvShowVehicles.text = vehicleInfo

                // Populate the spinner with VINs
                updateVehicleSpinner(vehicles)
            }
        } catch (e: Exception) {
            binding.tvShowVehicles.text = "Error fetching vehicles: ${e.message}"
            Log.e("FetchVehicles", "Error fetching vehicles: ${e.message}")
            Toast.makeText(context, "Error fetching vehicles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateVehicleSpinner(vehicles: List<VehicleResponse>) {
        val spinnerItems = if (vehicles.isEmpty()) {
            // Show "No vehicles available" if the list is empty
            listOf("No vehicles available")
        } else {
            // Show VINs of the vehicles
            vehicles.map { it.nickname }
        }

        // Create an ArrayAdapter for the spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerItems
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach the adapter to the spinner
        binding.spinnerVehicleSelection.adapter = adapter
    }


    private fun handleGrantAccessClick() {
        val userIdentifier = binding.etUserIdentifier.text.toString()
        val selectedVehicle = binding.spinnerVehicleSelection.selectedItem?.toString()
        val selectedAccessLevel = when (binding.radioGroupAccessLevel.checkedRadioButtonId) {
            R.id.radio_full_access -> "Full Access"
            R.id.radio_custom_access -> "Custom Access"
            else -> null
        }

        if (userIdentifier.isNotEmpty() && selectedVehicle != null && selectedAccessLevel != null) {
            val grantedPermissions = getSelectedPermissions()
            showToast(
                "Access granted to $userIdentifier for $selectedVehicle with $selectedAccessLevel access: $grantedPermissions"
            )
        } else {
            showToast("Please fill out all fields")
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

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
