package com.dsd.carcompanion.vehicleInterfaces

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.UserClient
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
import com.dsd.carcompanion.api.models.RevokedPermissions
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import com.dsd.carcompanion.api.repository.AuthRepository
import com.dsd.carcompanion.utility.ImageHelper
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserPermissionsFragment : Fragment() {

    private var _binding: FragmentGrantPermissionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null
    val vinMapping: MutableList<String> = mutableListOf("") // Default option has no VIN
    private var components: List<ComponentResponse> = emptyList()

    private var accessToken: String = ""

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
        ImageHelper.applyBlurToImageView(
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

        binding.radioFullAccess.isEnabled = true
        binding.radioCustomAccess.isEnabled = false

        // Fetch and display vehicles
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt().toString() }

                if (accessToken.isNotEmpty()) {
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

    private fun setupAccessLevelListener() {
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
                        "Nickname: ${vehicle.user_preferences ?: "N/A"}, VIN: ${vehicle.vin}"
                    }

                    //binding.tvShowApiResponse.text = vehicleInfo

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

    private fun updateVehicleSpinner(vehicles: List<VehiclePreferencesResponse>, accessToken: String) {
        val defaultOption = "Select a vehicle"
        val spinnerItems: MutableList<String> = mutableListOf(defaultOption)

        if (vehicles.isNotEmpty()) {
            val titles = vehicles.map { if (it.user_preferences.nickname != null) "${it.user_preferences.nickname} (${it.vin})" else "${it.model.manufacturer} ${it.model.name} (${it.vin})" }
            spinnerItems.addAll(titles)
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
                    binding.tvShowApiResponse.visibility = VISIBLE
                    binding.permissionsLayout.removeAllViews()
                    binding.radioGroupAccessLevel.clearCheck()
                    binding.radioFullAccess.isEnabled = false
                    binding.radioCustomAccess.isEnabled = false

                } else if (vehicles.isNotEmpty() && position in 1 until vinMapping.size) {
                    val selectedVIN = vinMapping[position]
                    fetchComponentsForVIN(selectedVIN, accessToken)
                } else {
                    binding.tvShowApiResponse.text = getString(R.string.no_permissions_selected)
                    components = emptyList()
                    updatePermissionsList()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                binding.tvShowApiResponse.text = getString(R.string.no_vehicle_selected)
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
                        binding.tvShowApiResponse.visibility = GONE
                        val fetchedComponents = response.data as? List<ComponentResponse>
                        if (fetchedComponents != null) {
                            components = fetchedComponents
                            updatePermissionsList()
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

    private fun updatePermissionsList() {
        val permissionsLayout = binding.permissionsLayout

        permissionsLayout.removeAllViews()

        if (components.isEmpty()) {
            val textView = TextView(requireContext()).apply {
                text = getString(R.string.no_permissions_available)
            }
            permissionsLayout.addView(textView)
        } else {
            val componentsLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                id = R.id.ll_grant_access_fragment_components
            }

            val datePickerInput = EditText(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                id = R.id.grant_access_date_picker
                hint = "Select a date"
                isFocusable = false // Prevents the keyboard from showing up
                isClickable = true  // Makes it clickable
            }

            // Set up the Calendar instance
            val calendar = Calendar.getInstance()

            // Set up the DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Update the EditText with the selected date
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    datePickerInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set minimum date to tomorrow
            val today = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            datePickerDialog.datePicker.minDate = today.timeInMillis

            // Show the DatePickerDialog when the EditText is clicked
            datePickerInput.setOnClickListener {
                datePickerDialog.show()
            }

            // Add the EditText to your layout
            val dateLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                addView(datePickerInput)
            }

            componentsLayout.addView(dateLayout)

            components.forEach { component ->
                val permissionLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(12, 16, 12, 16)
                }

                Log.d("Testing", component.toString())

                val permissionName = TextView(requireContext()).apply {
                    "${component.type.name} (${component.name})".also { text = it }
                    textSize = 18f
                }

                permissionLayout.addView(permissionName)

                val radioGroup = RadioGroup(requireContext()).apply {
                    orientation = RadioGroup.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    id = generateRadioGroupId(component)
                    setPadding(12, 12, 0, 12)
                }

                // Add "Revoke" radio button
                val revokeButton = RadioButton(requireContext()).apply {
                    text = "Forbidden"
                    id = generateRadioButtonId(component, "revoke")
                    isChecked = true
                    setPadding(12, 12, 0, 12)
                }
                radioGroup.addView(revokeButton)

                // Add "Read" radio button
                val readButton = RadioButton(requireContext()).apply {
                    text = "Only see state of component"
                    id = generateRadioButtonId(component, "read")
                    isChecked = false
                    setPadding(12, 12, 0, 12)
                }
                radioGroup.addView(readButton)

                // Add "Write" radio button
                val writeButton = RadioButton(requireContext()).apply {
                    text = "Give access"
                    id = generateRadioButtonId(component, "write")
                    isChecked = false
                    setPadding(12, 12, 0, 12)
                }
                radioGroup.addView(writeButton)

                permissionLayout.addView(radioGroup)

                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                    setBackgroundColor(requireContext().getColor(R.color.black))
                }
                permissionLayout.addView(divider)

                // Add the permission layout to the main layout
                componentsLayout.addView(permissionLayout)
            }

            val radioGroupFullAccess = RadioGroup(requireContext()).apply {
                orientation = RadioGroup.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                id = R.id.radio_group_full_access
                visibility = GONE
                setPadding(12, 12, 0, 12)
            }

            val revokeButtonFullAccess = RadioButton(requireContext()).apply {
                text = "Forbidden"
                id = R.id.radio_revoke_full_access
                isChecked = true
                setPadding(12, 12, 0, 12)
            }
            radioGroupFullAccess.addView(revokeButtonFullAccess)
            Log.d("Error", "Is error 1?")
            val readButtonFullAccess = RadioButton(requireContext()).apply {
                text = "Only see state of component"
                id = R.id.radio_read_full_access
                isChecked = false
                setPadding(12, 12, 0, 12)
            }
            radioGroupFullAccess.addView(readButtonFullAccess)
            Log.d("Error", "Is error 2?")
            val writeButtonFullAccess = RadioButton(requireContext()).apply {
                text = "Give access"
                id = R.id.radio_write_full_access
                isChecked = false
                setPadding(12, 12, 0, 12)
            }
            radioGroupFullAccess.addView(writeButtonFullAccess)
            Log.d("Error", "Is error 3?")
            permissionsLayout.addView(componentsLayout)
            Log.d("Error", "Is error 4?")
            permissionsLayout.addView(radioGroupFullAccess)
            Log.d("Error", "Is error 5?")

            binding.radioFullAccess.isEnabled = true
            binding.radioCustomAccess.isEnabled = true
            binding.radioCustomAccess.isChecked = true
            setupAccessLevelListener()
            binding.btnGrantAccess.setOnClickListener { handleGrantAccessClick() }
        }
    }

    private fun generateRadioButtonId(component: ComponentResponse, action: String): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "rbtn_${action}_grant_access_fragment_${formattedType}_${formattedName}"
        Log.d("PremissionFragment", idString)
        return idString.hashCode()
    }

    private fun generateRadioGroupId(component: ComponentResponse): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "rg_grant_access_fragment_${formattedType}_${formattedName}"

        Log.d("PremissionFragment", idString)

        return idString.hashCode()
    }

    private fun handleGrantAccessClick() {
        val userIdentifier = binding.etUserIdentifier.text.toString()
        val selectedVehicle = binding.spinnerVehicleSelection.selectedItemPosition
        val selectedAccessLevel = when (binding.radioGroupAccessLevel.checkedRadioButtonId) {
            R.id.radio_full_access -> "Full Access"
            R.id.radio_custom_access -> "Custom Access"
            else -> null
        }
        val vin = vinMapping[selectedVehicle]

        if (userIdentifier.isNotEmpty() && vin != "" && selectedAccessLevel != null) {
            if (selectedAccessLevel == "Full Access") {
                val grantPermission = getSelectedPermission()
                grantOrRevokeFullAccessToUser(vin, userIdentifier, grantPermission)
            } else {
                val grantPermissions = getSelectedPermissions()
                // TODO: check on user exist and is not owner already, etc.
                //checkIfUserExists()
                grantOrRevokeAccessToUser(vin, userIdentifier, grantPermissions)
            }
        } else {
            showToast("Please fill out all fields")
        }
    }

    private fun giveFullAccess() {
        binding.permissionsLayout.findViewById<RadioGroup>(R.id.radio_group_full_access).visibility = VISIBLE
        binding.permissionsLayout.findViewById<LinearLayout>(R.id.ll_grant_access_fragment_components).visibility = GONE
    }

    private fun giveCustomAccess() {
        binding.permissionsLayout.findViewById<RadioGroup>(R.id.radio_group_full_access).visibility = GONE
        binding.permissionsLayout.findViewById<LinearLayout>(R.id.ll_grant_access_fragment_components).visibility = VISIBLE
    }

    private fun checkIfUserExists() {
        val userService = UserClient.getApiServiceWithToken(accessToken)
        val authRepository = AuthRepository(userService, jwtTokenDataStore)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = authRepository.getAllUsers()

                withContext(Dispatchers.Main) {
                    if (response is ResultOf.Success) {
                        Log.d("GotterUsers", response.toString())
                    } else if (response is ResultOf.Error) {
                        showToast("No users could be found")
                    } else {
                        showToast("Something went wrong during login. Please try again.")
                    }
                }
            } catch (e: Exception) {
                showToast("Couldn't connect to the server. Please check your internet connection and try again.")
            }
        }
    }

    private fun getSelectedPermission(): GrantPermissionRequest {
        val radioGroupFullAccess = binding.permissionsLayout.findViewById<RadioGroup>(R.id.radio_group_full_access)
        val permissionType = when (radioGroupFullAccess.checkedRadioButtonId) {
            R.id.radio_revoke_full_access -> "revoke"
            R.id.radio_read_full_access -> "read"
            R.id.radio_write_full_access -> "write"
            else -> {"revoke"}
        }
        val validity = "9999-12-31T00:00:00.000Z"
        return GrantPermissionRequest(permissionType, validity)
    }

    private fun getSelectedPermissions(): List<PermissionResponse> {
        val updatedPermissions = mutableListOf<PermissionResponse>()

        val datePicker = binding.permissionsLayout.findViewById<EditText>(R.id.grant_access_date_picker)
        val validUntil = if (datePicker != null && !datePicker.text.isNullOrBlank()) {
            val date = datePicker.text.split("/")
            val year = date.get(2).toInt()
            val month = date.get(1).toInt()
            val day = date.get(0).toInt()
            String.format("%04d-%02d-%02dT00:00:00.000Z", year, month, day)
        } else {
            "9999-12-31T00:00:00.000Z" // Default to a far-future date if no DatePicker is found
        }

        Log.d("GrantPremission", validUntil)

        for (component in components) {
            val radioGroupId = generateRadioGroupId(component)
            val radioGroup = binding.permissionsLayout.findViewById<RadioGroup>(radioGroupId)

            val permissionType = when (radioGroup?.checkedRadioButtonId) {
                generateRadioButtonId(component, "revoke") -> "revoke"
                generateRadioButtonId(component, "read") -> "read"
                generateRadioButtonId(component, "write") -> "write"
                else -> "revoke" // Default to revoke if nothing is selected
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

    private fun grantOrRevokeFullAccessToUser(vin: String, username: String, grantPermission: GrantPermissionRequest) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    showToast("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                val response = if(grantPermission.permission_type == "revoke") {
                    vehicleRepository.revokeFullAccessToUserForVehicle(vin, username)
                } else {
                    vehicleRepository.grantFullAccessToUserForVehicle(vin, username, grantPermission)
                }
                when (response) {
                    is ResultOf.Success -> {
                        if(response.code == 200) {
                            resetPermissions()
                            showToast("Permissions have been updated")
                        }
                    }
                    is ResultOf.Error -> {
                        Log.d("SomethngSomething", response.code.toString())
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request."
                            403 -> "Unauthorized access."
                            404 -> "User not found. Please try again"
                            else -> "Unexpected error: ${response.message}"
                        }
                        showToast(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> showToast("Idle state")
                    ResultOf.Loading -> showToast("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                showToast("Error processing request: ${e.message}")
            }
        }
    }

    private fun grantOrRevokeAccessToUser(vin: String, username: String, permissions: List<PermissionResponse>) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    showToast("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                val grantPermissionRequests: List<GrantPermissionRequest> = permissions.map { response ->
                    GrantPermissionRequest(
                        permission_type = response.permission_type,
                        valid_until = response.valid_until
                    )
                }

                permissions.forEachIndexed { index, permission ->
                    val response = if(permission.permission_type == "revoke") {
                        vehicleRepository.revokeAccessToUserForComponent(vin, username, permission.component_type, permission.component_name)
                    } else {
                        vehicleRepository.grantAccessToUserForComponent(vin, username, permission.component_type, permission.component_name, grantPermissionRequests[index])
                    }

                    when (response) {
                        is ResultOf.Success -> {
                            if(response.code == 200) {
                                showToast("Permissions have been updated")
                            }
                        }
                        is ResultOf.Error -> {
                            val errorMessage = when (response.code) {
                                400 -> "Invalid request."
                                403 -> "Unauthorized access."
                                else -> "Unexpected error: ${response.message}"
                            }
                            showToast(errorMessage)
                            Log.e("VehicleOwnership", errorMessage)
                        }
                        ResultOf.Idle -> showToast("Idle state")
                        ResultOf.Loading -> showToast("Processing...")
                    }
                }
                resetPermissions()
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                showToast("Error processing request: ${e.message}")
            }
        }
    }

    private fun resetPermissions() {
        binding.etUserIdentifier.text.clear()
        binding.spinnerVehicleSelection.setSelection(0)
        binding.tvShowApiResponse.visibility = VISIBLE
        binding.permissionsLayout.findViewById<RadioGroup>(R.id.radio_group_full_access).check(R.id.radio_revoke_full_access)
        binding.radioGroupAccessLevel.clearCheck()
        binding.radioFullAccess.isEnabled = false
        binding.radioCustomAccess.isEnabled = false
        binding.permissionsLayout.removeAllViews()
    }
}