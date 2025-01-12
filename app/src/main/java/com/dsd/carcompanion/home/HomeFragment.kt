package com.dsd.carcompanion.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.adapters.VehicleInfoAdapter
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ComponentResponse
import com.dsd.carcompanion.api.models.ComponentStatusUpdate
import com.dsd.carcompanion.api.models.ComponentType
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentHomeBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

import org.qtproject.qt.android.QtQuickView
import org.qtproject.example.my_car_companionApp.QmlModule
import org.qtproject.qt.android.QtQmlStatus
import org.qtproject.qt.android.QtQmlStatusChangeListener

class HomeFragment : Fragment(), QtQmlStatusChangeListener {
    private lateinit var vehicleInfoAdapter: VehicleInfoAdapter
    private var vehicleInfoList: MutableList<VehicleInfo> = mutableListOf()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var isPeriodicFetchRunning = false

    private var m_qmlView: QtQuickView? = null
    private var m_mainQmlContent: QmlModule.Main = QmlModule.Main()
    private var isRightDoorOpen: Boolean = false
    private var isLeftDoorOpen: Boolean = false
    private var isRightWindowUp: Boolean = true
    private var isLeftWindowUp: Boolean = true
    private var areLightsTurnedOff: Boolean = false
    private var isCarDriving: Boolean = false
    private var isItSnowing: Boolean = false

    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var carVin: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llHomeFragmentBottomSheet)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)

        carVin = arguments?.getString("vin").toString()

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

        hideAllElements()

        val qtContainer = binding.qtContainer
        val params: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        m_qmlView = QtQuickView(requireContext(), "Main.qml", "my_car_companionApp")

        qtContainer.addView(m_qmlView, params)
        m_qmlView!!.loadContent(m_mainQmlContent)
        Log.d("Home", "After loaded")

        m_qmlView?.setStatusChangeListener { status ->
            Log.d("HomeFragment", status.toString())
            if (status == QtQmlStatus.READY) {
                Log.d("HomeFragment", "QtQuickView is ready")
                m_qmlView?.setProperty("rightDoorOpen", isRightDoorOpen)
                m_qmlView?.setProperty("leftDoorOpen", isLeftDoorOpen)
                m_qmlView?.setProperty("rightWindowUp", isRightWindowUp)
                m_qmlView?.setProperty("leftWindowUp", isLeftWindowUp)
                m_qmlView?.setProperty("lightsOff", areLightsTurnedOff)
                m_qmlView?.setProperty("areTiresTurning", isCarDriving)
                m_qmlView?.setProperty("isItSnowing", isItSnowing)
                m_qmlView?.setProperty("mouseAreaEnabled", true)
                //m_qmlView?.getProperty<String>("root.view3D.scene.qt_Car_Baked_low_v2.node.testing")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
                if (!accessToken.isNullOrEmpty()) {
                    var responseData = fetchComponentsData(accessToken)
                    setUpUiInterface(responseData)
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error during periodic fetch: ${e.message}", e)
            }
        }

//        bottomSheet.post {
//            ImageHelper.applyBlurToViewBackground(requireContext(), bottomSheet, blurRadius = 25f)
//        }

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 450

        // Expand bottom sheet when draggable guide is tapped
        binding.llHomeFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Setup switch functionality
        setupCustomSwitchLeftWindow()
        setupCustomSwitchDrivingCar()
        setupCustomSwitchRightWindow()

        // Add toggle button setup
        setupCustomToggleLocks()
        setupCustomToggleLights()
        setupCustomToggleDoorRight()
        setupCustomToggleDoorLeft()

        //Add sliders setup
        setupCustomSliderTemperature()

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

        binding.actionMakeItSnow.setOnClickListener {
            isItSnowing = !isItSnowing
            m_qmlView?.setProperty("isItSnowing", isItSnowing)
        }
    }

    private fun hideAllElements() {
        binding.toggleDoorRight.visibility = View.GONE
        binding.toggleDoorLeft.visibility = View.GONE
        binding.toggleLights.visibility = View.GONE
        binding.toggleLocks.visibility = View.GONE
        binding.sliderTemperature.customSliderLayout.visibility = View.GONE
        binding.switchWindowLeft.customSwitchLayout.visibility = View.GONE
        binding.switchWindowRight.customSwitchLayout.visibility = View.GONE
    }

    private fun setUpUiInterface(responseData: List<ComponentResponse>){
        responseData.forEach{ component ->
            run {
                if (component.type.name.equals("Door")) {
                    if (component.name.equals("Right")) {
                        binding.toggleDoorRight.visibility = View.VISIBLE
                        if (component.status > 0) {
                            isRightDoorOpen = true
                            binding.toggleDoorRight.isToggled = true
                        } else {
                            isRightDoorOpen = false
                            binding.toggleDoorRight.isToggled = false
                        }
                    } else if (component.name.equals("Left")) {
                        binding.toggleDoorLeft.visibility = View.VISIBLE
                        if (component.status > 0) {
                            isLeftDoorOpen = true
                            binding.toggleDoorLeft.isToggled = true
                        } else {
                            isLeftDoorOpen = false
                            binding.toggleDoorLeft.isToggled = false
                        }
                    }
                }
                else if(component.type.name.equals("Lights")){
                    binding.toggleLights.visibility = View.VISIBLE
                    if (component.status > 0) {
                        areLightsTurnedOff = false
                        binding.toggleLights.isToggled = true
                    } else {
                        areLightsTurnedOff = true
                        binding.toggleLights.isToggled = false
                    }
                }
                else if(component.type.name.equals("Lock")){
                    binding.toggleLocks.visibility = View.VISIBLE
                    binding.toggleLocks.isToggled = false
                }
                else if(component.type.name.equals("Temperature")){
                    binding.sliderTemperature.customSliderLayout.visibility = View.VISIBLE
                    binding.sliderTemperature.customSlider.value = component.status.toFloat() * 100
                }
                else if (component.type.name.equals("Window")) {
                    if (component.name.equals("Right")) {
                        binding.switchWindowRight.customSwitchLayout.visibility = View.VISIBLE
                        if (component.status > 0) {
                            isRightWindowUp = false
                            binding.switchWindowRight.customSwitch.isChecked = true
                        } else {
                            isRightWindowUp = true
                            binding.switchWindowRight.customSwitch.isChecked = false
                        }
                    } else if (component.name.equals("Left")) {
                        binding.switchWindowLeft.customSwitchLayout.visibility = View.VISIBLE
                        if (component.status > 0) {
                            isLeftWindowUp = false
                            binding.switchWindowLeft.customSwitch.isChecked = true
                        } else {
                            isLeftWindowUp = true
                            binding.switchWindowLeft.customSwitch.isChecked = false
                        }
                    }
                }
            }
        }

        m_qmlView?.setProperty("rightDoorOpen", isRightDoorOpen)
        m_qmlView?.setProperty("leftDoorOpen", isLeftDoorOpen)
        m_qmlView?.setProperty("rightWindowUp", isRightWindowUp)
        m_qmlView?.setProperty("leftWindowUp", isLeftWindowUp)
        m_qmlView?.setProperty("lightsOff", areLightsTurnedOff)
        m_qmlView?.setProperty("areTiresTurning", isCarDriving)
        m_qmlView?.setProperty("isItSnowing", isItSnowing)
        m_qmlView?.setProperty("mouseAreaEnabled", true)
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

            val response = vehicleRepository.getComponentsForVehicle(carVin)

            when (response) {
                is ResultOf.Success -> {
                    Log.d("HomeFragment", "Gotten list: " + response.data.toString())
                    response.data
                }
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

            val response = vehicleRepository.getComponentsForVehicle(carVin)

            when (response) {
                is ResultOf.Success -> {
                    val components = response.data
                    //updateBottomSheet(components)
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

                val response = vehicleRepository.updateComponentStatusForVehicle(carVin, component.type.name, component.name, ComponentStatusUpdate(status = newStatusDouble))
                when (response) {
                    is ResultOf.Success -> {
                        if(response.code == 200) {
                            Log.d("HomeFragment","Component Status has been updated: " + response.data.toString())
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

    // Custom switch handlers
    private fun setupCustomSwitchLeftWindow() {
        val switchLabel = binding.switchWindowLeft.tvSwitchLabel
        val customSwitch = binding.switchWindowLeft.customSwitch
        val lockButton = binding.toggleLocks

        // Set initial text
        switchLabel.text = "Left window"
        binding.switchWindowLeft.customSwitch.isChecked = !isLeftWindowUp

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(!lockButton.isToggled) {
                if (isChecked) {
                    isLeftWindowUp = false
                } else {
                    isLeftWindowUp = true
                }
                var status = if(isLeftWindowUp) 0.0f else 0.7f
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "Left",
                    ComponentType(
                        name = "Window"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, status)
                m_qmlView?.setProperty("leftWindowUp", isLeftWindowUp)
            } else {
                binding.switchWindowLeft.customSwitch.isChecked = false
            }
        }
    }

    private fun setupCustomSwitchRightWindow() {
        val switchLabel = binding.switchWindowRight.tvSwitchLabel
        val customSwitch = binding.switchWindowRight.customSwitch
        val lockButton = binding.toggleLocks

        // Set initial text
        switchLabel.text = "Right window"
        binding.switchWindowRight.customSwitch.isChecked = !isRightWindowUp

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(!lockButton.isToggled) {
                if (isChecked) {
                    isRightWindowUp = false
                } else {
                    isRightWindowUp = true
                }
                var status = if(isRightWindowUp) 0.0f else 0.7f
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "Right",
                    ComponentType(
                        name = "Window"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, status)
                m_qmlView?.setProperty("rightWindowUp", isRightWindowUp)
            } else {
                binding.switchWindowRight.customSwitch.isChecked = false
            }
        }
    }

    private fun setupCustomSwitchDrivingCar() {
        val switchLabel = binding.switchIsCarDriving.tvSwitchLabel
        val customSwitch = binding.switchIsCarDriving.customSwitch
        val lockButton = binding.toggleLocks

        // Set initial text
        switchLabel.text = "Driving"
        binding.switchIsCarDriving.customSwitch.isChecked = isCarDriving

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(!lockButton.isToggled) {
                if (isChecked) {
                    isCarDriving = true
                } else {
                    isCarDriving = false
                }
                m_qmlView?.setProperty("areTiresTurning", isCarDriving)
            } else {
                binding.switchIsCarDriving.customSwitch.isChecked = false
            }
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
                triggerLock()
            } else {
                triggerUnlock()
            }
        }
    }

    private fun setupCustomToggleLights() {
        val toggleButton = binding.toggleLights
        var lockButton = binding.toggleLocks

        // Set initial state
        toggleButton.isToggled = !areLightsTurnedOff
        if(toggleButton.isToggled) toggleButton.setToggleIcon(R.drawable.light_on)
        else toggleButton.setToggleIcon(R.drawable.light)

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            if(!lockButton.isToggled){
                toggleButton.isToggled = !toggleButton.isToggled
                if (toggleButton.isToggled) {
                    binding.toggleLights.setToggleIcon(R.drawable.light_on)
                    areLightsTurnedOff = false
                } else {
                    binding.toggleLights.setToggleIcon(R.drawable.light)
                    areLightsTurnedOff = true
                }
                var status = if(areLightsTurnedOff) 0.0f else 0.7f
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "Interior",
                    ComponentType(
                        name = "Lights"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, status)
                m_qmlView?.setProperty("lightsOff", areLightsTurnedOff)
            }
        }
    }

    private fun setupCustomToggleDoorRight() {
        val toggleButton = binding.toggleDoorRight
        var lockButton = binding.toggleLocks

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.door) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            if(!lockButton.isToggled){
                toggleButton.isToggled = !toggleButton.isToggled
                if (toggleButton.isToggled) {
                    isRightDoorOpen = true
                } else {
                    isRightDoorOpen = false
                }
                var status = if(isRightDoorOpen) 0.7f else 0.0f
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "Right",
                    ComponentType(
                        name = "Door"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, status)
                m_qmlView?.setProperty("rightDoorOpen", isRightDoorOpen)
            }
        }
    }

    private fun setupCustomToggleDoorLeft() {
        val toggleButton = binding.toggleDoorLeft
        var lockButton = binding.toggleLocks

        // Set initial state
        toggleButton.setToggleIcon(R.drawable.doorr) // Set your custom icon here
        toggleButton.isToggled = false // Default state

        // Set a click listener to handle toggle state changes
        toggleButton.setOnClickListener {
            if(!lockButton.isToggled) {
                toggleButton.isToggled = !toggleButton.isToggled
                if (toggleButton.isToggled) {
                    isLeftDoorOpen = true
                } else {
                    isLeftDoorOpen = false
                }
                var status = if(isLeftDoorOpen) 0.7f else 0.0f
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "Left",
                    ComponentType(
                        name = "Door"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, status)
                m_qmlView?.setProperty("leftDoorOpen", isLeftDoorOpen)
            }
        }
    }

    private fun setupCustomSliderTemperature() {
        // Initialize UI components
        val sliderTitle = binding.sliderTemperature.tvSliderTitle
        val customSlider = binding.sliderTemperature.customSlider
        val sliderInfoLabel = binding.sliderTemperature.tvSliderInfo

        // Set the title text using a string resource
        sliderTitle.text = getString(R.string.home_temperature)

        // Set the initial value for slider info and slider
        val initialTemperature = 25 // Adjust this based on your desired initial value
        sliderInfoLabel.text = getString(R.string.temperature_label, initialTemperature)

        // Set the slider custom range
        customSlider.valueFrom = 0f
        customSlider.valueTo = 100f
        customSlider.stepSize = 1f

        customSlider.value = initialTemperature.toFloat()

        // Disable the value label on the thumb
        customSlider.setLabelFormatter(null)

        customSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Optional: Do something when the user starts interacting with the slider
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Update the value only when the user stops interacting
                val finalValue = slider.value
                Log.d("Slider", "Final Value: $finalValue")
                sliderInfoLabel.text = getString(R.string.temperature_label, finalValue.toInt())
                var componentResponse: ComponentResponse = ComponentResponse(
                    name = "°c",
                    ComponentType(
                        name = "Temperature"
                    ),
                    status = 0.0
                )
                updateComponentStatus(componentResponse, finalValue)
            }
        })

        // Attach a listener to update the info label dynamically
        customSlider.addOnChangeListener { slider, value, fromUser ->
            sliderInfoLabel.text = getString(R.string.temperature_label, value.toInt())
        }
    }

    private fun triggerUnlock() {
        m_qmlView?.setProperty("rightDoorOpen", isRightDoorOpen)
        m_qmlView?.setProperty("leftDoorOpen", isLeftDoorOpen)
        m_qmlView?.setProperty("rightWindowUp", isRightWindowUp)
        m_qmlView?.setProperty("leftWindowUp", isLeftWindowUp)
        m_qmlView?.setProperty("areTiresTurning", isCarDriving)
        m_qmlView?.setProperty("lightsOff", areLightsTurnedOff)

        binding.switchWindowRight.customSwitch.isChecked = !isRightWindowUp
        binding.switchWindowLeft.customSwitch.isChecked = !isLeftWindowUp
        binding.switchIsCarDriving.customSwitch.isChecked = isCarDriving
        binding.toggleLights.isToggled = !areLightsTurnedOff
        if(binding.toggleLights.isToggled) binding.toggleLights.setToggleIcon(R.drawable.light_on)
        else binding.toggleLights.setToggleIcon(R.drawable.light)
        binding.toggleDoorLeft.isToggled = isLeftDoorOpen
        binding.toggleDoorRight.isToggled = isRightDoorOpen

        var status = if(isRightDoorOpen) 0.7f else 0.0f
        var componentResponse: ComponentResponse = ComponentResponse(
            name = "Right",
            ComponentType(
                name = "Door"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, status)

        status = if(isLeftDoorOpen) 0.7f else 0.0f
        componentResponse = ComponentResponse(
            name = "Left",
            ComponentType(
                name = "Door"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, status)

        status = if(isRightWindowUp) 0.0f else 0.7f
        componentResponse = ComponentResponse(
            name = "Right",
            ComponentType(
                name = "Window"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, status)

        status = if(isLeftWindowUp) 0.0f else 0.7f
        componentResponse = ComponentResponse(
            name = "Left",
            ComponentType(
                name = "Window"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, status)

        status = if(areLightsTurnedOff) 0.0f else 0.7f
        componentResponse = ComponentResponse(
            name = "Interior",
            ComponentType(
                name = "Light"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, status)
    }

    private fun triggerLock() {
        m_qmlView?.setProperty("rightDoorOpen", false)
        m_qmlView?.setProperty("leftDoorOpen", false)
        m_qmlView?.setProperty("rightWindowUp", true)
        m_qmlView?.setProperty("leftWindowUp", true)
        m_qmlView?.setProperty("areTiresTurning", false)
        m_qmlView?.setProperty("lightsOff", true)

        binding.switchWindowRight.customSwitch.isChecked = false
        binding.switchWindowLeft.customSwitch.isChecked = false
        binding.switchIsCarDriving.customSwitch.isChecked = false
        binding.toggleLights.isToggled = false
        binding.toggleLights.setToggleIcon(R.drawable.light)
        binding.toggleDoorLeft.isToggled = false
        binding.toggleDoorRight.isToggled = false

        var componentResponse: ComponentResponse = ComponentResponse(
            name = "Right",
            ComponentType(
                name = "Door"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, 0.0f)

        componentResponse = ComponentResponse(
            name = "Left",
            ComponentType(
                name = "Door"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, 0.0f)

        componentResponse = ComponentResponse(
            name = "Right",
            ComponentType(
                name = "Window"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, 0.0f)

        componentResponse = ComponentResponse(
            name = "Left",
            ComponentType(
                name = "Window"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, 0.0f)

        componentResponse = ComponentResponse(
            name = "Interior",
            ComponentType(
                name = "Light"
            ),
            status = 0.0
        )
        updateComponentStatus(componentResponse, 0.0f)
    }

    override fun onStatusChanged(status: QtQmlStatus?) {
        Log.v("CAR_COMPANION_QT", "Status of QtQuickView: $status")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "View is destroyed")
        m_qmlView?.setProperty("mouseAreaEnabled", false)
        m_qmlView?.removeAllViews()
        m_qmlView = null
        stopPeriodicComponentUpdates()
        _binding = null
    }
}