package com.dsd.carcompanion.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.adapters.VehicleInfoAdapter
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ComponentResponse
import com.dsd.carcompanion.api.models.ComponentStatusUpdate
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentHomeBinding
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.log


class HomeFragment : Fragment() {
    private lateinit var vehicleInfoAdapter: VehicleInfoAdapter
    private var vehicleInfoList: MutableList<VehicleInfo> = mutableListOf()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private val vin: String = "JH4KA3140LC003233"
    private var isPeriodicFetchRunning = false

    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llHomeFragmentBottomSheet)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.imgBackground
        ImageHelper.applyBlurToImageView(
            imageView,
            context,
            R.drawable.homescreend,
            blurRadius = 50f
        )

//        bottomSheet.post {
//            ImageHelper.applyBlurToViewBackground(requireContext(), bottomSheet, blurRadius = 25f)
//        }

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llHomeFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Setup switch functionality
        setupCustomSwitchWindows()
        setupCustomSwitchLocks()
        setupCustomSwitchLights()

        // Add toggle button setup
        setupCustomToggleLocks()
        setupCustomToggleLights()
        setupCustomToggleDoorRight()
        setupCustomToggleDoorLeft()

        // Connect the UI components to functions
        setupListeners()

        // Fetch and display components
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if (!accessToken.isNullOrEmpty()) {
                    fetchComponents(accessToken) // Fetch components using the token
                    startPeriodicComponentUpdates(5000L) // Fetch every 5 seconds
                } else {
                    showToast("No Access JWT Token found")
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching token: ${e.message}", e)
                showToast("Error fetching token: ${e.message}")
            }
        }

        binding.menuIcon.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.menuIcon)
            popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_SettingsFragment)
                        true
                    }
                    R.id.action_access -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_UserPermissionsFragment)
                        true
                    }
                    R.id.action_add_vehicle -> {
                        findNavController().navigate(R.id.action_HomeFragment_to_VehicleOwnershipFragment)
                        true
                    }
                    R.id.action_logout -> {
                        logoutUser();
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }


    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPeriodicComponentUpdates(intervalMillis: Long = 5000L) {
        isPeriodicFetchRunning = false
        viewLifecycleOwner.lifecycleScope.launch {
            while (isPeriodicFetchRunning) {
                try {
                    val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
                    if (!accessToken.isNullOrEmpty()) {
                        val components = fetchComponentsData(accessToken)
                        withContext(Dispatchers.Main) {
                            updateComponentsUI(components)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error during periodic fetch: ${e.message}", e)
                }
                delay(intervalMillis)
            }
        }
    }

    private fun stopPeriodicComponentUpdates() {
        isPeriodicFetchRunning = false
    }

    private suspend fun fetchComponentsData(accessToken: String): List<ComponentResponse> {
        return try {
            val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
            val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

            val response = vehicleRepository.getComponentsForVehicle(vin)

            when (response) {
                is ResultOf.Success -> response.data
                is ResultOf.Error -> {
                    Log.e("HomeFragment", "Error fetching components: ${response.message}")
                    emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error: ${e.message}", e)
            emptyList()
        }
    }

    private fun updateComponentsUI(components: List<ComponentResponse>) {
        for (component in components) {
            val sliderId = generateSliderId(component)
            val textViewId = generateTextViewId(component)

            val slider = view?.findViewById<Slider>(sliderId)
            val textView = view?.findViewById<TextView>(textViewId)

            if (slider != null) {
                slider.value = component.status.toFloat()
            }

            if (textView != null) {
                val isTemperatureComponent =
                    component.type.name == resources.getString(R.string.bottom_sheet_temperature_tv_label)
                textView.text = if (isTemperatureComponent) (slider?.value?.times(100f)).toString()
                else if ((slider?.value ?: 0f) > 0.5f)
                    getString(R.string.bottom_sheet_vehicle_unlocked_tv_state)
                else getString(R.string.bottom_sheet_vehicle_locked_tv_state)
            }
        }
    }

    private suspend fun fetchComponents(accessToken: String) {
        try {
            val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
            val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

            val response = vehicleRepository.getComponentsForVehicle(vin)

            when (response) {
                is ResultOf.Success -> {
                    val components = response.data
                    updateBottomSheet(components)
                }
                is ResultOf.Error -> {
                    //binding.tvShowApiResponse.text = "Error: ${response.message}"
                }

                ResultOf.Idle -> TODO()
                ResultOf.Loading -> TODO()
            }
        } catch (e: Exception) {
            Log.e("FetchComponents", "Error: ${e.message}", e)
            Toast.makeText(context, "Error fetching vehicles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBottomSheet(components: List<ComponentResponse>) {
        val bottomSheetLayout = binding.llHomeFragmentBottomSheet

        val tvTitleParams = TextView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT)
            text = getString(R.string.bottom_sheet_title_parameters)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        bottomSheetLayout.addView(tvTitleParams)

        val vDivider = View(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.divider_height)
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.divider_margin)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.divider_margin)
            }
            setBackgroundColor(Color.parseColor("#D3D3D3"))
        }
        bottomSheetLayout.addView(vDivider)

        for (component in components) {
            val flexboxLayout = FlexboxLayout(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                justifyContent = JustifyContent.CENTER
                alignItems = AlignItems.CENTER
            }

            val tvTitleComponent = TextView(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                "${component.type.name} ${component.name}".also { text = it }
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            flexboxLayout.addView(tvTitleComponent)

            val llComponent = LinearLayout(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = CENTER
            }

            val isTemperatureComponent = component.type.name == resources.getString(R.string.bottom_sheet_temperature_tv_label)
            val sliderComponent = Slider(requireContext()).apply {
                id = generateSliderId(component)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0f
                )
                valueFrom = 0f
                valueTo = 1f
                stepSize = 0.1f
                value = component.status.toFloat()
            }
            llComponent.addView(sliderComponent)
            flexboxLayout.addView(llComponent)

            val tvStatusComponent = TextView(requireContext()).apply {
                id = generateTextViewId(component)
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.MATCH_PARENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = if (isTemperatureComponent) (sliderComponent.value * 100f).toString() else if (sliderComponent.value > 0.5f) getString(R.string.bottom_sheet_vehicle_unlocked_tv_state) else getString(R.string.bottom_sheet_vehicle_locked_tv_state)
            }
            flexboxLayout.addView(tvStatusComponent)

            sliderComponent.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    val value = slider.value
                    tvStatusComponent.text = if (isTemperatureComponent) (value * 100f).toString() else if (value > 0.5f) getString(R.string.bottom_sheet_vehicle_unlocked_tv_state) else getString(R.string.bottom_sheet_vehicle_locked_tv_state)
                    updateComponentStatus(component, value)
                }
            })
            bottomSheetLayout.addView(flexboxLayout)
        }
    }

    private fun generateSliderId(component: ComponentResponse): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "slider_home_fragment_${formattedType}_${formattedName}"

        return idString.hashCode()
    }

    private fun generateTextViewId(component: ComponentResponse): Int {
        val type = component.type.name
        val name = component.name
        val formattedType = type.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()
        val formattedName = name.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

        val idString = "tv_home_fragment_${formattedType}_${formattedName}"

        return idString.hashCode()
    }

    private fun updateComponentStatus(component: ComponentResponse, newStatus: Float) {
        val newStatusDouble = BigDecimal(newStatus.toDouble())
            .setScale(2, RoundingMode.HALF_UP) // Round to 2 decimal places to keep the slider value intact
            .toDouble()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    showToast("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                val response = vehicleRepository.updateComponentStatusForVehicle(vin, component.type.name, component.name, ComponentStatusUpdate(status = newStatusDouble))
                when (response) {
                    is ResultOf.Success -> {
                        if(response.code == 200) {
                            showToast("Component Status has been updated")
                        }
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request."
                            403 -> "Insufficient write permissions."
                            404 -> "Vehicle or component not found."
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

    fun logoutUser(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                jwtTokenDataStore.clearAllTokens()

                val intent = Intent(requireContext(), UserStartActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                Log.e("FirstFragment", "Error during logout: ${e.message}")
            }
        }
    }

    // Custom switch handlers
    private fun setupCustomSwitchWindows() {
        val switchLabel = binding.switchWindows.tvSwitchLabel
        val customSwitch = binding.switchWindows.customSwitch
        val switchLabelAction = binding.switchWindows.tvSwitchLabelAction

        // Set initial text
        switchLabel.text = "Windows"
        switchLabelAction.text = "Closed"  //This will depend on the user profile

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabelAction.text = "Open"
            } else {
                switchLabelAction.text = "Closed"
            }
        }
    }

    private fun setupCustomSwitchLights() {
        val switchLabel = binding.switchLights.tvSwitchLabel
        val customSwitch = binding.switchLights.customSwitch
        val switchLabelAction = binding.switchLights.tvSwitchLabelAction

        // Set initial text
        switchLabel.text = "Lights"
        switchLabelAction.text = "Off"  //This will depend on the user profile

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabelAction.text = "On"
            } else {
                switchLabelAction.text = "Off"
            }
        }
    }

    private fun setupCustomSwitchLocks() {
        val switchLabel = binding.switchVehicle.tvSwitchLabel
        val customSwitch = binding.switchVehicle.customSwitch
        val switchLabelAction = binding.switchVehicle.tvSwitchLabelAction

        // Set initial text
        switchLabel.text = "Vehicle"
        switchLabelAction.text = "Unlocked"   //This will depend on the user profile

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabelAction.text = "Unlocked"
            } else {
                switchLabelAction.text = "Locked"
            }
        }
    }

    private fun setupListeners() {
        // User Notifications Button
        binding.switchWindows.customSwitch.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            Toast.makeText(context, "Windows state", Toast.LENGTH_SHORT).show()
        }

        binding.switchVehicle.customSwitch.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            Toast.makeText(context, "Vehicle state", Toast.LENGTH_SHORT).show()
        }

        binding.switchLights.customSwitch.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            Toast.makeText(context, "Lights state", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCustomToggleLocks() {
        val toggleButton = binding.toggleLocks

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.lock) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            toggleButton.isToggled = !toggleButton.isToggled
            if (toggleButton.isToggled) {
                Toast.makeText(context, "Mode toggled ON", Toast.LENGTH_SHORT).show()
                // Perform actions for ON state
            } else {
                Toast.makeText(context, "Mode toggled OFF", Toast.LENGTH_SHORT).show()
                // Perform actions for OFF state
            }
        }
    }

    private fun setupCustomToggleLights() {
        val toggleButton = binding.toggleLights

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.light) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            toggleButton.isToggled = !toggleButton.isToggled
            if (toggleButton.isToggled) {
                Toast.makeText(context, "Mode toggled ON", Toast.LENGTH_SHORT).show()
                // Perform actions for ON state
            } else {
                Toast.makeText(context, "Mode toggled OFF", Toast.LENGTH_SHORT).show()
                // Perform actions for OFF state
            }
        }
    }

    private fun setupCustomToggleDoorRight() {
        val toggleButton = binding.toggleDoorRight

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.door) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            toggleButton.isToggled = !toggleButton.isToggled
            if (toggleButton.isToggled) {
                Toast.makeText(context, "Mode toggled ON", Toast.LENGTH_SHORT).show()
                // Perform actions for ON state
            } else {
                Toast.makeText(context, "Mode toggled OFF", Toast.LENGTH_SHORT).show()
                // Perform actions for OFF state
            }
        }
    }

    private fun setupCustomToggleDoorLeft() {
        val toggleButton = binding.toggleDoorLeft

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.doorr) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            toggleButton.isToggled = !toggleButton.isToggled
            if (toggleButton.isToggled) {
                Toast.makeText(context, "Mode toggled ON", Toast.LENGTH_SHORT).show()
                // Perform actions for ON state
            } else {
                Toast.makeText(context, "Mode toggled OFF", Toast.LENGTH_SHORT).show()
                // Perform actions for OFF state
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        stopPeriodicComponentUpdates()
        _binding = null
    }
}