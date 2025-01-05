package com.dsd.carcompanion.vehicleInterfaces

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
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
import com.dsd.carcompanion.api.models.ComponentResponse
import com.dsd.carcompanion.api.models.GrantPermissionRequest
import com.dsd.carcompanion.api.models.GrantedPermissions
import com.dsd.carcompanion.api.models.PermissionResponse
import com.dsd.carcompanion.api.models.PermissionsResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import java.util.Calendar

class UserPermissionsFragment : Fragment() {

    private var _binding: FragmentGrantPermissionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    private var components: List<ComponentResponse> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrantPermissionsBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llPermissionsAccessFragmentBottomSheet)

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
        binding.llPermissionsAccessFragmentBottomSheet.setOnClickListener {
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
    }

    private fun setupAccessLevelListener(permissions: List<ComponentResponse>) {
        binding.radioGroupAccessLevel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_full_access -> giveFullAccess()
                R.id.radio_custom_access -> giveCustomAccess() // Allow user customization
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
                    binding.tvShowApiResponse.text = vehicleInfo
                    updateVehicleSpinner(vehicles,accessToken)
                }
                is ResultOf.Error -> {
                    binding.tvShowApiResponse.text = "Error: ${response.message}"
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
            spinnerItems.clear()
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
                    binding.tvShowApiResponse.text = getString(R.string.no_vehicle_selected)
                } else if (vehicles.isNotEmpty() && position in 1 until vinMapping.size) {
                    val selectedVIN = vinMapping[position]
                    fetchComponentsForVIN(selectedVIN, accessToken)
                } else {
                    binding.tvShowApiResponse.text = getString(R.string.no_permissions_selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.tvShowApiResponse.text = getString(R.string.no_vehicle_selected)
            }
        }
    }


    private fun fetchComponentsForVIN(vin: String, accessToken: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                // Fetch permissions for the selected VIN
                val response = vehicleRepository.getComponentsForVehicle(vin)

                when (response) {
                    is ResultOf.Success -> {
                        val fetchedComponents = response.data as? List<ComponentResponse>
                        if (fetchedComponents != null) {
                            components = fetchedComponents
                            updatePermissionsList(fetchedComponents)
                        } else {
//                            binding.tvShowApiResponse.text = getString(R.string.unexpected_response_format)
                        }
                    }
                    is ResultOf.Error -> {
                        binding.tvShowApiResponse.text = "Error: ${response.message}"
                    }
                    ResultOf.Idle -> {
//                        binding.tvShowApiResponse.text = getString(R.string.fetching_idle_state)
                    }
                    ResultOf.Loading -> {
//                        binding.tvShowApiResponse.text = getString(R.string.fetching_loading_state)
                    }
                }
            } catch (e: Exception) {
                binding.tvShowApiResponse.text = "Error fetching permissions: ${e.message}"
                Log.e("FetchPermissions", "Error fetching permissions: ${e.message}", e)
                showToast("Error fetching permissions: ${e.message}")
            }
        }
    }


    private fun updatePermissionsList(components: List<ComponentResponse>) {
        val permissionsLayout = binding.permissionsLayout

        permissionsLayout.removeAllViews()

        if (components.isEmpty()) {
            val textView = TextView(requireContext()).apply {
                text = getString(R.string.no_permissions_available)
            }
            permissionsLayout.addView(textView)
        } else {
            components.forEach { component ->
                val permissionLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val permissionName = TextView(requireContext()).apply {
                    "${component.type.name} (${component.name})".also { text = it }
                }
                permissionLayout.addView(permissionName)

                val radioGroup = RadioGroup(requireContext()).apply {
                    orientation = RadioGroup.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    id = generateRadioGroupId(component)
                }

                // Add "Revoke" radio button
                val revokeButton = RadioButton(requireContext()).apply {
                    text = "Revoke"
                    id = generateRadioButtonId(component, "revoke")
                    isChecked = component.status == 0
                }
                radioGroup.addView(revokeButton)

                // Add "Read" radio button
                val readButton = RadioButton(requireContext()).apply {
                    text = "Read"
                    id = generateRadioButtonId(component, "read")
                    isChecked = component.status == 1 // Example: 1 for read
                }
                radioGroup.addView(readButton)

                // Add "Write" radio button
                val writeButton = RadioButton(requireContext()).apply {
                    text = "Write"
                    id = generateRadioButtonId(component, "write")
                    isChecked = component.status == 2 // Example: 2 for write
                }
                radioGroup.addView(writeButton)

                permissionLayout.addView(radioGroup)

                val datePicker = DatePicker(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    id = generateDatePickerId(component)

                    spinnersShown = true

                    val today = Calendar.getInstance()
                    minDate = today.timeInMillis
                }

                permissionLayout.addView(datePicker)

                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                    setBackgroundColor(requireContext().getColor(R.color.black))
                }
                permissionLayout.addView(divider)

                // Add the permission layout to the main layout
                permissionsLayout.addView(permissionLayout)

            }
            setupAccessLevelListener(components)
            binding.btnGrantAccess.setOnClickListener { handleGrantAccessClick() }
        }
    }

    private fun generateRadioButtonId(component: ComponentResponse, action: String): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "rbtn_${action}_grant_access_fragment_${formattedType}_${formattedName}"
        return idString.hashCode()
    }

    private fun generateRadioGroupId(component: ComponentResponse): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "rg_grant_access_fragment_${formattedType}_${formattedName}"

        return idString.hashCode()
    }

    private fun generateDatePickerId(component: ComponentResponse): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "dp_grant_access_fragment_${formattedType}_${formattedName}"

        return idString.hashCode()
    }

    private fun handleGrantAccessClick() {
        val userIdentifier = binding.etUserIdentifier.text.toString()
        val selectedVehicle = binding.spinnerVehicleSelection.selectedItem?.toString()?.trim()
        val selectedAccessLevel = when (binding.radioGroupAccessLevel.checkedRadioButtonId) {
            R.id.radio_full_access -> "Full Access"
            R.id.radio_custom_access -> "Custom Access"
            else -> null
        }
        val vin = Regex("\\((\\d+)\\)").find(selectedVehicle.toString())?.groupValues?.get(1).toString()

        if (userIdentifier.isNotEmpty() && selectedVehicle != null && selectedAccessLevel != null) {
            val grantPermissions = getSelectedPermissions()

            //if (selectedAccessLevel == "Full Access") {
                // TODO: Call grantfullaccess API (but first when full access radio button is selected, the choices of revoke/read/write and the datepicker should reduce to a single one)
                // TODO: Design should be refactored (grant access for the whole vehicle components / specific type / specific name)
            //} else {
                // TODO: check on user exist and is not owner already and is vin correctly retrieved, etc.
                grantOrRevokeAccessToUser(vin, userIdentifier, grantPermissions)
            //}
            showToast(
                "Access granted to $userIdentifier for $selectedVehicle with $selectedAccessLevel access: $grantPermissions"
            )
        } else {
            showToast("Please fill out all fields")
        }
    }

    private fun giveFullAccess() {
        for(component in components) {
            val writeRadioButtonId = generateRadioButtonId(component, "write")
            val writeRadioButton = binding.permissionsLayout.findViewById<RadioButton>(writeRadioButtonId)

            writeRadioButton?.let {
                it.isChecked = true
                it.isEnabled = false
            }

            val readRadioButton = binding.permissionsLayout.findViewById<RadioButton>(generateRadioButtonId(component, "read"))
            val revokeRadioButton = binding.permissionsLayout.findViewById<RadioButton>(generateRadioButtonId(component, "revoke"))
            readRadioButton?.let {
                it.isEnabled = false
            }
            revokeRadioButton?.let {
                it.isEnabled = false
            }
        }
    }

    private fun giveCustomAccess() {
        for(component in components) {
            val writeRadioButton = binding.permissionsLayout.findViewById<RadioButton>(generateRadioButtonId(component, "write"))
            val readRadioButton = binding.permissionsLayout.findViewById<RadioButton>(generateRadioButtonId(component, "read"))
            val revokeRadioButton = binding.permissionsLayout.findViewById<RadioButton>(generateRadioButtonId(component, "revoke"))

            writeRadioButton?.let {
                it.isEnabled = true
            }
            readRadioButton?.let {
                it.isEnabled = true
            }
            revokeRadioButton?.let {
                it.isEnabled = true
            }
        }
    }

    private fun getSelectedPermissions(): List<PermissionResponse> {
        val updatedPermissions = mutableListOf<PermissionResponse>()

        for (component in components) {
            val radioGroupId = generateRadioGroupId(component)
            val radioGroup = binding.permissionsLayout.findViewById<RadioGroup>(radioGroupId)

            val permissionType = when (radioGroup?.checkedRadioButtonId) {
                generateRadioButtonId(component, "revoke") -> "revoke"
                generateRadioButtonId(component, "read") -> "read"
                generateRadioButtonId(component, "write") -> "write"
                else -> "revoke" // Default to revoke if nothing is selected
            }

            val datePickerId = generateDatePickerId(component)
            val datePicker = binding.permissionsLayout.findViewById<DatePicker>(datePickerId)

            val validUntil = if (datePicker != null) {
                val year = datePicker.year
                val month = datePicker.month + 1 // Months are 0-indexed
                val day = datePicker.dayOfMonth
                String.format("%04d-%02d-%02dT00:00:00.000Z", year, month, day)
            } else {
                "9999-12-31T00:00:00.000Z" // Default to a far-future date if no DatePicker is found
            }

            val updatedPermission = PermissionResponse(
                component_type = component.type.name,
                component_name = component.name,
                permission_type = permissionType,
                valid_until = validUntil
            )

            updatedPermissions.add(updatedPermission)
        }
        return updatedPermissions
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

    // TODO: not finished yet
    private fun grantOrRevokeAccessToUser(vin: String, username: String, permissions: List<PermissionResponse>) {
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
//
//                if(accessToken.isNullOrEmpty()) {
//                    showToast("Access token not found")
//                    return@launch
//                }
//
//                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
//                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)
//
//                val grantPermissionRequests: List<GrantPermissionRequest> = permissions.map { response ->
//                    GrantPermissionRequest(
//                        permission_type = response.permission_type,
//                        valid_until = response.valid_until
//                    )
//                }
//
//                for(permission in permissions) {
//                    val response: ResultOf<GrantedPermissions>
//
//                    if(permission.permission_type == "revoke") {
//                        // TODO: Call revokeaccess API
//                    } else {
//                        // TODO: Call grantaccess API
//                    }
//                }
//
//                when (val response = vehicleRepository.grantFullAccessToUserForVehicle(vin, username, permissions)) {
//                    is ResultOf.Success -> {
//                        if(response.code == 200) {
//                        }
//                    }
//                    is ResultOf.Error -> {
//                        val errorMessage = when (response.code) {
//                            400 -> "Invalid request."
//                            403 -> "Unauthorized access."
//                            404 -> "Vehicle not found."
//                            else -> "Unexpected error: ${response.message}"
//                        }
//                        showToast(errorMessage)
//                        Log.e("VehicleOwnership", errorMessage)
//                    }
//                    ResultOf.Idle -> showToast("Idle state")
//                    ResultOf.Loading -> showToast("Processing...")
//                }
//            } catch (e: Exception) {
//                Log.e("VehicleOwnership", "Error: ${e.message}", e)
//                showToast("Error processing request: ${e.message}")
//            }
//        }
    }
}
