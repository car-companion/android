package com.dsd.carcompanion.vehicleInterfaces

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
import com.dsd.carcompanion.databinding.FragmentGrantPermissionsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.PermissionResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.service.VehicleService
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior

class UserPermissionsFragment : Fragment() {

    private var _binding: FragmentGrantPermissionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrantPermissionsBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llHomeFragmentBottomSheet)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.imgBackground
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            context,
            R.drawable.homescreend
        )


        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        _bottomSheetBehavior?.isDraggable = false
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llHomeFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Fetch and display vehicles
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if (!accessToken.isNullOrEmpty()) {
                    fetchVehicles(accessToken) // Fetch vehicles using the token
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
            val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
            val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

            val response = vehicleRepository.getOwnedVehicles()

            when (response) {
                is ResultOf.Success -> {
                    val vehicles = response.data
                    val vehicleInfo = vehicles.joinToString("\n") { vehicle ->
                        "Nickname: ${vehicle.nickname ?: "N/A"}, VIN: ${vehicle.vin}"
                    }
//                    binding.tvShowApiResponse.text = vehicleInfo
                    updateVehicleSpinner(vehicles,accessToken)
                }
                is ResultOf.Error -> {
//                    binding.tvShowApiResponse.text = "Error: ${response.message}"
                }

                ResultOf.Idle -> TODO()
                ResultOf.Loading -> TODO()
            }
        } catch (e: Exception) {
            Log.e("FetchVehicles", "Error: ${e.message}", e)
            Toast.makeText(context, "Error fetching vehicles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateVehicleSpinner(vehicles: List<VehicleResponse>, accessToken: String) {
        val defaultOption = "Select a vehicle"
        val spinnerItems: MutableList<String> = mutableListOf(defaultOption)
        val vinMapping: MutableList<String> = mutableListOf("") // Default option has no VIN

        if (vehicles.isNotEmpty()) {
            spinnerItems.addAll(vehicles.map { it.nickname ?: "Unnamed Vehicle (${it.vin})" })
            vinMapping.addAll(vehicles.map { it.vin })
        } else {
            spinnerItems.add("No vehicles available")
            vinMapping.add("") // Keep mapping consistent
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerItems
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicleSelection.adapter = adapter

        binding.spinnerVehicleSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // Default option selected
//                    binding.tvShowApiResponse.text = getString(R.string.no_vehicle_selected)
                } else if (vehicles.isNotEmpty() && position in 1 until vinMapping.size) {
                    val selectedVIN = vinMapping[position]
                    fetchPermissionsForVIN(selectedVIN, accessToken)
                } else {
//                    binding.tvShowApiResponse.text = getString(R.string.no_permissions_selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                binding.tvShowApiResponse.text = getString(R.string.no_vehicle_selected)
            }
        }
    }


    private fun fetchPermissionsForVIN(vin: String, accessToken: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                // Fetch permissions for the selected VIN
                val response = vehicleRepository.getComponentsForVehicle(vin)

                when (response) {
                    is ResultOf.Success -> {
                        val permissions = response.data as? List<PermissionResponse>
                        if (permissions != null) {
                            updatePermissionsTextView(permissions)
                        } else {
//                            binding.tvShowApiResponse.text = getString(R.string.unexpected_response_format)
                        }
                    }
                    is ResultOf.Error -> {
//                        binding.tvShowApiResponse.text = "Error: ${response.message}"
                    }
                    ResultOf.Idle -> {
//                        binding.tvShowApiResponse.text = getString(R.string.fetching_idle_state)
                    }
                    ResultOf.Loading -> {
//                        binding.tvShowApiResponse.text = getString(R.string.fetching_loading_state)
                    }
                }
            } catch (e: Exception) {
//                binding.tvShowApiResponse.text = "Error fetching permissions: ${e.message}"
                Log.e("FetchPermissions", "Error fetching permissions: ${e.message}", e)
                showToast("Error fetching permissions: ${e.message}")
            }
        }
    }


    private fun updatePermissionsTextView(permissions: List<PermissionResponse>) {
        if (permissions.isEmpty()) {
//            binding.tvShowApiResponse.text = getString(R.string.no_permissions_available)
        } else {
            val permissionsText = permissions.joinToString("\n") { permission ->
                "${permission.name} (${permission.type.name}): ${if (permission.status == 0) "Disabled" else "Enabled"}"
            }
//            binding.tvShowApiResponse.text = permissionsText
        }
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
        enableAllCheckBoxes(false)
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
