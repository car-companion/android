package com.dsd.carcompanion.home

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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class HomeFragment : Fragment() {
    //private lateinit var recyclerView: RecyclerView
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
        //recyclerView = binding.rvHomeFragmentVehicleInfo
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)
        //recyclerView.adapter = vehicleInfoAdapter

        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

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

        binding.fabHomeFragmentDimension.setOnClickListener {
            val is3DMode = binding.fabHomeFragmentDimension.text == getString(R.string.home_fragment_3d_mode_fab_dimension)
            binding.fabHomeFragmentDimension.text = getString(
                if (is3DMode) R.string.home_fragment_2d_mode_fab_dimension else R.string.home_fragment_3d_mode_fab_dimension
            )
        }
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
                val isTemperatureComponent = component.type.name == resources.getString(R.string.bottom_sheet_temperature_tv_label)
                textView.text = if (isTemperatureComponent) (slider?.value?.times(100f)).toString()
                else if ((slider?.value ?: 0f) > 0.5f)
                    getString(R.string.bottom_sheet_vehicle_unlocked_tv_state)
                else getString(R.string.bottom_sheet_vehicle_locked_tv_state)
                
        binding.menuIcon.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_FirstFragment)

        /*binding.swHomeFragmentWindows.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.tvHomeFragmentWindowsState.text = getString(R.string.bottom_sheet_windows_opened_tv_state)
            } else {
                binding.tvHomeFragmentWindowsState.text = getString(R.string.bottom_sheet_windows_closed_tv_state)
            }
        }*/
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
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPeriodicComponentUpdates()
        _binding = null
    }
}