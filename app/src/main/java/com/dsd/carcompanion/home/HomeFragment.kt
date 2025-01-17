package com.dsd.carcompanion.home

import android.content.Intent
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
import com.dsd.carcompanion.MainActivity
import com.dsd.carcompanion.R
import com.dsd.carcompanion.adapters.VehicleInfoAdapter
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ComponentResponse
import com.dsd.carcompanion.api.models.ComponentStatusUpdate
import com.dsd.carcompanion.api.models.ComponentType
import com.dsd.carcompanion.api.models.UIVehicleStats
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentHomeBinding
import com.dsd.carcompanion.userRegistrationAndLogin.UserStartActivity
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

    private var carModel: UIVehicleStats = UIVehicleStats();

    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        Log.d("HomeFragment", carModel.toString())
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llHomeFragmentBottomSheet)
        jwtTokenDataStore = JwtTokenDataStore(requireContext()) // Initialize the JwtTokenDataStore
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)

        carModel.carVin = arguments?.getString("vin").toString()
        carModel.carColor = arguments?.getString("color").toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var flag: Boolean = true

        val imageView = binding.imgBackground
        ImageHelper.applyBlurToImageView(
            imageView,
            context,
            R.drawable.homescreend,
            blurRadius = 50f
        )

        hideAllElements()

        //First setup of user interface
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
                if (!accessToken.isNullOrEmpty()) {
                    var responseData = fetchComponentsData(accessToken)
                    binding.actionErrorMessage.visibility = View.GONE
                    setUpUiInterface(responseData, true)
                } else {
                    val intent = Intent(requireActivity(), UserStartActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error during periodic fetch: ${e.message}", e)
                binding.actionErrorMessage.visibility = View.VISIBLE
                flag = false
            }
        }

        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 450

        binding.llHomeFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        if(flag){
            // Fetch and display components
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                    if (!accessToken.isNullOrEmpty()) {
                        startPeriodicComponentUpdates(5000L) // Fetch every 5 seconds
                    } else {
                        showToast("No Access JWT Token found")
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error fetching token: ${e.message}", e)
                    showToast("Error fetching token: ${e.message}")
                }
            }
        }
    }

    private fun hideAllElements() {
        binding.actionErrorMessage.visibility = View.GONE
        binding.toggleDoorRight.visibility = View.GONE
        binding.toggleDoorLeft.visibility = View.GONE
        binding.toggleLights.visibility = View.GONE
        binding.toggleLocks.visibility = View.GONE
        binding.sliderTemperature.customSliderLayout.visibility = View.GONE
        binding.switchWindowLeft.customSwitchLayout.visibility = View.GONE
        binding.switchWindowRight.customSwitchLayout.visibility = View.GONE
    }

    private fun setUpUiInterface(responseData: List<ComponentResponse>, iFirstTime: Boolean){
        if(iFirstTime){
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
                    m_qmlView?.setProperty("rightDoorOpen", carModel.isRightDoorOpen)
                    m_qmlView?.setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
                    m_qmlView?.setProperty("rightWindowUp", carModel.isRightWindowUp)
                    m_qmlView?.setProperty("leftWindowUp", carModel.isLeftWindowUp)
                    m_qmlView?.setProperty("lightsOff", carModel.areLightsTurnedOff)
                    m_qmlView?.setProperty("areTiresTurning", carModel.isCarDriving)
                    m_qmlView?.setProperty("isItSnowing", carModel.isItSnowing)
                    m_qmlView?.setProperty("mouseAreaEnabled", true)

                    if(carModel.carColor.isNotEmpty()){
                        m_qmlView?.setProperty("customCarColor", carModel.carColor)
                    }
                }
            }
            binding.actionMakeItSnow.setOnClickListener {
                carModel.isItSnowing = !carModel.isItSnowing
                m_qmlView?.setProperty("isItSnowing", carModel.isItSnowing)
            }
        }

        responseData.forEach{ component ->
            run {
                if (component.type.name.equals("Door")) {
                    if (component.name.equals("Right")) {
                        carModel.enabledRightDoor = true
                        carModel.rightDoorValue = component.status.toFloat()
                        if (component.status > 0) {
                            carModel.isRightDoorOpen = true
                        } else {
                            carModel.isRightDoorOpen = false
                        }
                    } else if (component.name.equals("Left")) {
                        carModel.enabledLeftDoor = true
                        carModel.leftDoorValue = component.status.toFloat()
                        if (component.status > 0) {
                            carModel.isLeftDoorOpen = true
                        } else {
                            carModel.isLeftDoorOpen = false
                        }
                    }
                }
                else if(component.type.name.equals("Charging status")){
                    carModel.chargingEnabled = true
                    if (component.status > 0) {
                        carModel.isCarCharging = true
                    } else {
                        carModel.isCarCharging = false
                    }
                }
                else if(component.type.name.equals("Battery")){
                    carModel.chargingEnabled = true
                    carModel.batteryStatus = component.status.toFloat()
                }
                else if(component.type.name.equals("Lights")){
                    carModel.enabledLights = true
                    carModel.lightsValue = component.status.toFloat()
                    if (component.status > 0) {
                        carModel.areLightsTurnedOff = false
                    } else {
                        carModel.areLightsTurnedOff = true
                    }
                }
                else if(component.type.name.equals("Lock")){
                    carModel.enabledLocks = true
                    carModel.locksValue = component.status.toFloat()
                }
                else if(component.type.name.equals("Temperature")){
                    carModel.enabledTemperature = true
                    carModel.temperatureValue = component.status.toFloat()
                }
                else if (component.type.name.equals("Window")) {
                    if (component.name.equals("Right")) {
                        carModel.enabledRightWindow = true
                        carModel.rightWindowValue = component.status.toFloat()
                        if (component.status > 0) {
                            carModel.isRightWindowUp = false
                        } else {
                            carModel.isRightWindowUp = true
                        }
                    } else if (component.name.equals("Left")) {
                        carModel.enabledLeftWindow = true
                        carModel.leftWindowValue = component.status.toFloat()
                        if (component.status > 0) {
                            carModel.isLeftWindowUp = false
                        } else {
                            carModel.isLeftWindowUp = true
                        }
                    }
                }
            }
        }

        m_qmlView?.setProperty("rightDoorOpen", carModel.isRightDoorOpen)
        m_qmlView?.setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
        m_qmlView?.setProperty("rightWindowUp", carModel.isRightWindowUp)
        m_qmlView?.setProperty("leftWindowUp", carModel.isLeftWindowUp)
        m_qmlView?.setProperty("lightsOff", carModel.areLightsTurnedOff)
        m_qmlView?.setProperty("areTiresTurning", carModel.isCarDriving)
        m_qmlView?.setProperty("isItSnowing", carModel.isItSnowing)

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
        setUpCustomBatteryStatus()
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPeriodicComponentUpdates(intervalMillis: Long = 5000L) {
        isPeriodicFetchRunning = true
        viewLifecycleOwner.lifecycleScope.launch {
            while (isPeriodicFetchRunning) {
                try {
                    val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
                    if (!accessToken.isNullOrEmpty()) {
                        val components = fetchComponentsData(accessToken)
                        withContext(Dispatchers.Main) {
                            Log.d("HomeFragment", "HELLOOOOOOOOOOOOOOOOOOOOOO")
                            checkIncomingValues(components)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error during periodic fetch: ${e.message}", e)
                }
                delay(intervalMillis)
            }
        }
    }

    private fun stopPeriodicRetreval() {
        isPeriodicFetchRunning = false
    }

    private fun checkIncomingValues(responseData: List<ComponentResponse>) {
        responseData.forEach{ component ->
            run {
                if (component.type.name.equals("Door")) {
                    if (component.name.equals("Right")) {
                        if(carModel.enabledRightDoor.equals(false)){
                            carModel.enabledRightDoor = true
                            carModel.rightDoorValue = component.status.toFloat()
                            if (component.status > 0) {
                                carModel.isRightDoorOpen = true
                            } else {
                                carModel.isRightDoorOpen = false
                            }
                            m_qmlView?.setProperty("rightDoorOpen", carModel.isRightDoorOpen)
                            setupCustomToggleDoorRight()
                        } else {
                            if(carModel.rightDoorValue != component.status.toFloat()){
                                carModel.rightDoorValue = component.status.toFloat()
                                if (component.status > 0) {
                                    carModel.isRightDoorOpen = true
                                } else {
                                    carModel.isRightDoorOpen = false
                                }
                                m_qmlView?.setProperty("rightDoorOpen", carModel.isRightDoorOpen)
                                binding.toggleDoorRight.isToggled = carModel.isRightDoorOpen
                            }
                        }
                    } else if (component.name.equals("Left")) {
                        if(carModel.enabledLeftDoor.equals(false)){
                            carModel.enabledLeftDoor = true
                            carModel.leftDoorValue = component.status.toFloat()
                            if (component.status > 0) {
                                carModel.isLeftDoorOpen = true
                            } else {
                                carModel.isLeftDoorOpen = false
                            }
                            m_qmlView?.setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
                            setupCustomToggleDoorLeft()
                        } else {
                            if(carModel.leftDoorValue != component.status.toFloat()){
                                carModel.leftDoorValue = component.status.toFloat()
                                if (component.status > 0) {
                                    carModel.isLeftDoorOpen = true
                                } else {
                                    carModel.isLeftDoorOpen = false
                                }
                                m_qmlView?.setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
                                binding.toggleDoorLeft.isToggled = carModel.isLeftDoorOpen
                            }
                        }
                    }
                }
                else if(component.type.name.equals("Charging status")){
                    if(carModel.chargingEnabled.equals(false)){
                        if (component.status > 0) {
                            carModel.isCarCharging = true
                        } else {
                            carModel.isCarCharging = false
                        }
                        carModel.chargingEnabled = true
                        setUpCustomBatteryStatus()
                    } else {
                        if(carModel.batteryStatus != component.status.toFloat()){
                            if (component.status > 0) {
                                carModel.isCarCharging = true
                            } else {
                                carModel.isCarCharging = false
                            }
                            carModel.chargingEnabled = true
                            setUpCustomBatteryStatus()
                        }
                    }
                }
                else if(component.type.name.equals("Battery")){
                    if(carModel.chargingEnabled.equals(false)){
                        carModel.batteryStatus = component.status.toFloat()
                        carModel.chargingEnabled = true
                        setUpCustomBatteryStatus()
                    } else {
                        if(carModel.batteryStatus != component.status.toFloat()){
                            carModel.batteryStatus = component.status.toFloat()
                            carModel.chargingEnabled = true
                            setUpCustomBatteryStatus()
                        }
                    }
                }
                else if(component.type.name.equals("Lights")){
                    if(carModel.enabledLights.equals(false)){
                        carModel.enabledLights = true
                        carModel.lightsValue = component.status.toFloat()
                        if (component.status > 0) {
                            carModel.areLightsTurnedOff = false
                        } else {
                            carModel.areLightsTurnedOff = true
                        }
                        m_qmlView?.setProperty("lightsOff", carModel.areLightsTurnedOff)
                        setupCustomToggleLights()
                    } else {
                        if(carModel.lightsValue != component.status.toFloat()){
                            carModel.lightsValue = component.status.toFloat()
                            if (component.status > 0) {
                                carModel.areLightsTurnedOff = false
                            } else {
                                carModel.areLightsTurnedOff = true
                            }
                            m_qmlView?.setProperty("lightsOff", carModel.areLightsTurnedOff)
                            binding.toggleLights.isToggled = !carModel.areLightsTurnedOff
                            if(binding.toggleLights.isToggled) binding.toggleLights.setToggleIcon(R.drawable.light_on)
                            else binding.toggleLights.setToggleIcon(R.drawable.light)
                        }
                    }
                }
                else if(component.type.name.equals("Lock")){
                    if(carModel.enabledLocks.equals(false)){
                        carModel.enabledLocks = true
                        carModel.locksValue = component.status.toFloat()
                        setupCustomToggleLocks()
                    } else {
                        if(carModel.locksValue != component.status.toFloat()){
                            carModel.locksValue = component.status.toFloat()
                            binding.toggleLocks.isToggled = carModel.locksValue > 0
                            if(carModel.locksValue > 1){
                                triggerUnlock()
                            } else {
                                triggerLock()
                            }
                        }
                    }
                }
                else if(component.type.name.equals("Temperature")){
                    carModel.enabledTemperature = true
                    carModel.temperatureValue = component.status.toFloat()
                    setupCustomSliderTemperature()
                }
                else if (component.type.name.equals("Window")) {
                    if (component.name.equals("Right")) {
                        if(carModel.enabledRightWindow.equals(false)) {
                            carModel.enabledRightWindow = true
                            carModel.rightWindowValue = component.status.toFloat()
                            if (component.status > 0) {
                                carModel.isRightWindowUp = false
                            } else {
                                carModel.isRightWindowUp = true
                            }
                            m_qmlView?.setProperty("rightWindowUp", carModel.isRightWindowUp)
                            setupCustomSwitchRightWindow()
                        } else {
                            if(carModel.rightWindowValue != component.status.toFloat()){
                                carModel.rightWindowValue = component.status.toFloat()
                                if (component.status > 0) {
                                    carModel.isRightWindowUp = false
                                } else {
                                    carModel.isRightWindowUp = true
                                }
                                m_qmlView?.setProperty("rightWindowUp", carModel.isRightWindowUp)
                                binding.switchWindowRight.customSwitch.isChecked = carModel.isRightWindowUp
                            }
                        }
                    } else if (component.name.equals("Left")) {
                        if(carModel.enabledLeftWindow.equals(false)){
                            carModel.enabledLeftWindow = true
                            carModel.leftWindowValue = component.status.toFloat()
                            if (component.status > 0) {
                                carModel.isLeftWindowUp = false
                            } else {
                                carModel.isLeftWindowUp = true
                            }
                            m_qmlView?.setProperty("leftWindowUp", !carModel.isLeftWindowUp)
                            setupCustomSwitchLeftWindow()
                        } else {
                            if(carModel.leftWindowValue != component.status.toFloat()){
                                carModel.leftWindowValue = component.status.toFloat()
                                if (component.status > 0) {
                                    carModel.isLeftWindowUp = false
                                } else {
                                    carModel.isLeftWindowUp = true
                                }
                                m_qmlView?.setProperty("leftWindowUp", carModel.isLeftWindowUp)
                                binding.switchWindowLeft.customSwitch.isChecked = !carModel.isLeftWindowUp
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun fetchComponentsData(accessToken: String): List<ComponentResponse> {
        return try {
            val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
            val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

            Log.d("HomeFragment", carModel.carVin)
            val response = vehicleRepository.getComponentsForVehicle(carModel.carVin)

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

    private suspend fun updateComponentsStatus(component: ComponentResponse, newStatus: Float, isStartingAgain: Boolean) {
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

                val response = vehicleRepository.updateComponentStatusForVehicle(carModel.carVin, component.type.name, component.name, ComponentStatusUpdate(status = newStatusDouble))
                when (response) {
                    is ResultOf.Success -> {
                        if(response.code == 200) {
                            Log.d("HomeFragment","Component Status has been updated: " + response.data.toString())
                            if(isStartingAgain && !isPeriodicFetchRunning) {
                                isPeriodicFetchRunning = true
                                //startPeriodicComponentUpdates(5000L)
                            }
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

    private fun updateComponentStatus(component: ComponentResponse, newStatus: Float, isStartingAgain: Boolean) {
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

                val response = vehicleRepository.updateComponentStatusForVehicle(carModel.carVin, component.type.name, component.name, ComponentStatusUpdate(status = newStatusDouble))
                when (response) {
                    is ResultOf.Success -> {
                        if(response.code == 200) {
                            Log.d("HomeFragment","Component Status has been updated: " + response.data.toString())
                            if(isStartingAgain && !isPeriodicFetchRunning) {
                                isPeriodicFetchRunning = true
                                //startPeriodicComponentUpdates(5000L)
                            }
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
    private fun setUpCustomBatteryStatus(){
        var iconBattery = binding.actionChargingBattery

        if(!carModel.chargingEnabled){
            iconBattery.visibility = View.GONE
        } else {
            iconBattery.visibility = View.VISIBLE

            if(carModel.isCarCharging){
                iconBattery.setImageResource(R.drawable.ic_battery_charging)
            } else {
                if(carModel.batteryStatus < 0.2){
                    iconBattery.setImageResource(R.drawable.ic_battery_empty)
                } else if (carModel.batteryStatus > 0.9){
                    iconBattery.setImageResource(R.drawable.ic_battery_full)
                } else {
                    iconBattery.setImageResource(R.drawable.ic_battery_half_full)
                }
            }
        }
    }

    private fun setupCustomSwitchLeftWindow() {
        val switchLabel = binding.switchWindowLeft.tvSwitchLabel
        val customSwitch = binding.switchWindowLeft.customSwitch
        val lockButton = binding.toggleLocks

        if(!carModel.enabledLeftWindow){
            binding.switchWindowLeft.customSwitchLayout.visibility = View.GONE
        } else {
            binding.switchWindowLeft.customSwitchLayout.visibility = View.VISIBLE
            // Set initial text
            switchLabel.text = "Left window"
            binding.switchWindowLeft.customSwitch.isChecked = !carModel.isLeftWindowUp

            customSwitch.setOnCheckedChangeListener { _, isChecked ->
                if(!lockButton.isToggled) {
                    stopPeriodicRetreval()
                    if (isChecked) {
                        carModel.isLeftWindowUp = false
                    } else {
                        carModel.isLeftWindowUp = true
                    }
                    carModel.leftWindowValue = if(carModel.isLeftWindowUp) 0.0f else 0.7f
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "Left",
                        ComponentType(
                            name = "Window"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, carModel.leftWindowValue, true)
                    m_qmlView?.setProperty("leftWindowUp", carModel.isLeftWindowUp)
                } else {
                    binding.switchWindowLeft.customSwitch.isChecked = false
                }
            }
        }
    }

    private fun setupCustomSwitchRightWindow() {
        val switchLabel = binding.switchWindowRight.tvSwitchLabel
        val customSwitch = binding.switchWindowRight.customSwitch
        val lockButton = binding.toggleLocks

        if(!carModel.enabledRightWindow){
            binding.switchWindowRight.customSwitchLayout.visibility = View.GONE
        } else {
            binding.switchWindowRight.customSwitchLayout.visibility = View.VISIBLE
            // Set initial text
            switchLabel.text = "Right window"
            binding.switchWindowRight.customSwitch.isChecked = !carModel.isRightWindowUp

            customSwitch.setOnCheckedChangeListener { _, isChecked ->
                if(!lockButton.isToggled) {
                    stopPeriodicRetreval()
                    if (isChecked) {
                        carModel.isRightWindowUp = false
                    } else {
                        carModel.isRightWindowUp = true
                    }
                    carModel.rightWindowValue = if(carModel.isRightWindowUp) 0.0f else 0.7f
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "Right",
                        ComponentType(
                            name = "Window"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, carModel.rightWindowValue, true)
                    m_qmlView?.setProperty("rightWindowUp", carModel.isRightWindowUp)
                } else {
                    binding.switchWindowRight.customSwitch.isChecked = false
                }
            }
        }
    }

    private fun setupCustomSwitchDrivingCar() {
        val switchLabel = binding.switchIsCarDriving.tvSwitchLabel
        val customSwitch = binding.switchIsCarDriving.customSwitch
        val lockButton = binding.toggleLocks

        // Set initial text
        switchLabel.text = "Driving"
        binding.switchIsCarDriving.customSwitch.isChecked = carModel.isCarDriving

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(!lockButton.isToggled) {
                stopPeriodicRetreval()
                if (isChecked) {
                    carModel.isCarDriving = true
                } else {
                    carModel.isCarDriving = false
                }
                m_qmlView?.setProperty("areTiresTurning", carModel.isCarDriving)
            } else {
                binding.switchIsCarDriving.customSwitch.isChecked = false
            }
        }

    }

    private fun setupCustomToggleLocks() {
        val toggleButton = binding.toggleLocks

        if(!carModel.enabledLocks){
            binding.toggleLocks.visibility = View.GONE
        } else {
            binding.toggleLocks.visibility = View.VISIBLE
            // Set initial state
            toggleButton.setToggleIcon(R.drawable.lock) // Set your custom icon here
            if(carModel.locksValue > 0) toggleButton.isToggled = true
            else toggleButton.isToggled = false

            // Set a click listener to handle toggle state changes
            toggleButton.setOnClickListener {
                stopPeriodicRetreval()
                toggleButton.isToggled = !toggleButton.isToggled
                if (toggleButton.isToggled) {
                    triggerLock()
                } else {
                    triggerUnlock()
                }
            }
        }
    }

    private fun setupCustomToggleLights() {
        val toggleButton = binding.toggleLights
        var lockButton = binding.toggleLocks

        if(!carModel.enabledLights){
            toggleButton.visibility = View.GONE
        } else {
            toggleButton.visibility = View.VISIBLE
            // Set initial state
            toggleButton.isToggled = !carModel.areLightsTurnedOff
            if(toggleButton.isToggled) toggleButton.setToggleIcon(R.drawable.light_on)
            else toggleButton.setToggleIcon(R.drawable.light)
            // Set a click listener to handle toggle state changes
            toggleButton.setOnClickListener {
                if(!lockButton.isToggled){
                    stopPeriodicRetreval()
                    toggleButton.isToggled = !toggleButton.isToggled
                    if (toggleButton.isToggled) {
                        binding.toggleLights.setToggleIcon(R.drawable.light_on)
                        carModel.areLightsTurnedOff = false
                    } else {
                        binding.toggleLights.setToggleIcon(R.drawable.light)
                        carModel.areLightsTurnedOff = true
                    }
                    carModel.lightsValue = if(carModel.areLightsTurnedOff) 0.0f else 0.7f
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "Interior",
                        ComponentType(
                            name = "Lights"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, carModel.lightsValue, true)
                    m_qmlView?.setProperty("lightsOff", carModel.areLightsTurnedOff)
                }
            }
        }
    }

    private fun setupCustomToggleDoorRight() {
        val toggleButton = binding.toggleDoorRight
        var lockButton = binding.toggleLocks

        if(!carModel.enabledRightDoor){
            toggleButton.visibility = View.GONE
        } else {
            toggleButton.visibility = View.VISIBLE
            // Set initial state
            toggleButton.setToggleIcon(R.drawable.door) // Set your custom icon here
            toggleButton.isToggled = carModel.isRightDoorOpen // Default state
            // Set a click listener to handle toggle state changes
            toggleButton.setOnClickListener {
                if(!lockButton.isToggled){
                    stopPeriodicRetreval()
                    toggleButton.isToggled = !toggleButton.isToggled
                    if (toggleButton.isToggled) {
                        carModel.isRightDoorOpen = true
                    } else {
                        carModel.isRightDoorOpen = false
                    }
                    carModel.rightDoorValue = if(carModel.isRightDoorOpen) 0.7f else 0.0f
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "Right",
                        ComponentType(
                            name = "Door"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, carModel.rightDoorValue, true)
                    m_qmlView?.setProperty("rightDoorOpen", carModel.isRightDoorOpen)
                }
            }
        }
    }

    private fun setupCustomToggleDoorLeft() {
        val toggleButton = binding.toggleDoorLeft
        var lockButton = binding.toggleLocks

        if(!carModel.enabledLeftDoor){
            toggleButton.visibility = View.GONE
        } else {
            toggleButton.visibility = View.VISIBLE
            // Set initial state
            toggleButton.setToggleIcon(R.drawable.doorr) // Set your custom icon here
            toggleButton.isToggled = carModel.isLeftDoorOpen // Default state

            // Set a click listener to handle toggle state changes
            toggleButton.setOnClickListener {
                if(!lockButton.isToggled) {
                    stopPeriodicRetreval()
                    toggleButton.isToggled = !toggleButton.isToggled
                    if (toggleButton.isToggled) {
                        carModel.isLeftDoorOpen = true
                    } else {
                        carModel.isLeftDoorOpen = false
                    }
                    carModel.leftDoorValue = if(carModel.isLeftDoorOpen) 0.7f else 0.0f
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "Left",
                        ComponentType(
                            name = "Door"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, carModel.leftDoorValue, true)
                    m_qmlView?.setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
                }
            }
        }
    }

    private fun setupCustomSliderTemperature() {
        val sliderTitle = binding.sliderTemperature.tvSliderTitle
        val customSlider = binding.sliderTemperature.customSlider
        val sliderInfoLabel = binding.sliderTemperature.tvSliderInfo

        if(!carModel.enabledTemperature){
            binding.sliderTemperature.customSliderLayout.visibility = View.GONE
        } else {
            binding.sliderTemperature.customSliderLayout.visibility = View.VISIBLE
            sliderTitle.text = getString(R.string.home_temperature)
            var initialTemperature = carModel.temperatureValue * 100
            if(initialTemperature < 15f || initialTemperature > 30f){
                initialTemperature = 16f
            }
            sliderInfoLabel.text = getString(R.string.temperature_label_float, initialTemperature)
            customSlider.value = initialTemperature

            customSlider.valueFrom = 15f
            customSlider.valueTo = 30f
            customSlider.stepSize = 0.5f

            customSlider.setLabelFormatter(null)

            customSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Optional: Do something when the user starts interacting with the slider
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    // Update the value only when the user stops interacting
                    stopPeriodicRetreval()
                    val finalValue = slider.value
                    Log.d("Slider", "Final Value: $finalValue")
                    sliderInfoLabel.text = getString(R.string.temperature_label_float, finalValue.toFloat())
                    var componentResponse: ComponentResponse = ComponentResponse(
                        name = "°c",
                        ComponentType(
                            name = "Temperature"
                        ),
                        status = 0.0
                    )
                    updateComponentStatus(componentResponse, finalValue/100, true)
                }
            })

            // Attach a listener to update the info label dynamically
            customSlider.addOnChangeListener { slider, value, fromUser ->
                sliderInfoLabel.text = getString(R.string.temperature_label_float, value.toFloat())
            }
        }
    }

    private fun triggerUnlock() {
        viewLifecycleOwner.lifecycleScope.launch {
            stopPeriodicRetreval()

            m_qmlView?.apply {
                setProperty("rightDoorOpen", carModel.isRightDoorOpen)
                setProperty("leftDoorOpen", carModel.isLeftDoorOpen)
                setProperty("rightWindowUp", carModel.isRightWindowUp)
                setProperty("leftWindowUp", carModel.isLeftWindowUp)
                setProperty("areTiresTurning", carModel.isCarDriving)
                setProperty("lightsOff", carModel.areLightsTurnedOff)
            }

            binding.apply {
                switchWindowRight.customSwitch.isChecked = !carModel.isRightWindowUp
                switchWindowLeft.customSwitch.isChecked = !carModel.isLeftWindowUp
                switchIsCarDriving.customSwitch.isChecked = carModel.isCarDriving
                toggleLights.isToggled = !carModel.areLightsTurnedOff
                toggleLights.setToggleIcon(if (toggleLights.isToggled) R.drawable.light_on else R.drawable.light)
                toggleDoorLeft.isToggled = carModel.isLeftDoorOpen
                toggleDoorRight.isToggled = carModel.isRightDoorOpen
            }

            carModel.locksValue = 0.0f
            val componentsToUpdate = listOf(
                ComponentResponse(
                    "Right",
                    ComponentType("Door"),
                    0.0
                ) to if (carModel.isRightDoorOpen) 0.7f else 0.0f,
                ComponentResponse("All", ComponentType("Lock"), 0.0) to 0.0f,
                ComponentResponse(
                    "Left",
                    ComponentType("Door"),
                    0.0
                ) to if (carModel.isLeftDoorOpen) 0.7f else 0.0f,
                ComponentResponse(
                    "Right",
                    ComponentType("Window"),
                    0.0
                ) to if (carModel.isRightWindowUp) 0.0f else 0.7f,
                ComponentResponse(
                    "Left",
                    ComponentType("Window"),
                    0.0
                ) to if (carModel.isLeftWindowUp) 0.0f else 0.7f,
                ComponentResponse(
                    "Interior",
                    ComponentType("Lights"),
                    0.0
                ) to if (carModel.areLightsTurnedOff) 0.0f else 0.7f
            )

            for ((component, newValue) in componentsToUpdate) {
                val isStartingAgain = component.name == "Interior"
                updateComponentsStatus(component, newValue, isStartingAgain)
            }
        }
    }

    private fun triggerLock() {
        viewLifecycleOwner.lifecycleScope.launch {
            m_qmlView?.apply {
                setProperty("rightDoorOpen", false)
                setProperty("leftDoorOpen", false)
                setProperty("rightWindowUp", true)
                setProperty("leftWindowUp", true)
                setProperty("areTiresTurning", false)
                setProperty("lightsOff", true)
            }

            binding.apply {
                switchWindowRight.customSwitch.isChecked = false
                switchWindowLeft.customSwitch.isChecked = false
                switchIsCarDriving.customSwitch.isChecked = false
                toggleLights.isToggled = false
                toggleLights.setToggleIcon(R.drawable.light)
                toggleDoorLeft.isToggled = false
                toggleDoorRight.isToggled = false
            }

            carModel.locksValue = 1.0f
            val componentsToUpdate = listOf(
                ComponentResponse("Right", ComponentType("Door"), 0.0) to 0.0f,
                ComponentResponse("All", ComponentType("Lock"), 0.0) to 1.0f,
                ComponentResponse("Left", ComponentType("Door"), 0.0) to 0.0f,
                ComponentResponse("Right", ComponentType("Window"), 0.0) to 0.0f,
                ComponentResponse("Left", ComponentType("Window"), 0.0) to 0.0f,
                ComponentResponse("Interior", ComponentType("Lights"), 0.0) to 0.0f
            )

            for ((component, newValue) in componentsToUpdate) {
                val isStartingAgain = component.name == "Interior"
                updateComponentsStatus(component, newValue, isStartingAgain)
            }
        }
    }

    override fun onStatusChanged(status: QtQmlStatus?) {
        Log.v("CAR_COMPANION_QT", "Status of QtQuickView: $status")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "View is destroyed")
        m_qmlView?.setProperty("mouseAreaEnabled", false)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            m_qmlView?.removeAllViews()
            m_qmlView = null
        }
        isPeriodicFetchRunning = false
        _binding = null
    }
}