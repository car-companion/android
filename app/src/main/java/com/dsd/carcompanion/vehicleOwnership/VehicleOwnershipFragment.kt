package com.dsd.carcompanion.vehicleOwnership

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ColorResponse
import com.dsd.carcompanion.api.models.PreferencesResponse
import com.dsd.carcompanion.api.models.VehicleResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mrudultora.colorpicker.ColorPickerBottomSheetDialog
import com.mrudultora.colorpicker.listeners.OnSelectColorListener
import com.mrudultora.colorpicker.util.ColorItemShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VehicleOwnershipFragment : Fragment() {

    private fun displayFormError(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    fun findCurrentColorPosition(
        colorResponses: List<ColorResponse>,
        currentColor: ColorResponse
    ): Int {
        return colorResponses.indexOfFirst {
            it.name == currentColor.name &&
                    it.hex_code == currentColor.hex_code &&
                    it.is_metallic == currentColor.is_metallic
        }
    }

    private var _binding: FragmentVehicleOwnershipBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleOwnershipBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llVehicleOwnershipFragmentBottomSheet)
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
        binding.llVehicleOwnershipFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.btnVehicleOwnershipFragmentTake.setOnClickListener{
            val vin = binding.etVehicleOwnershipFragmentModelNumber.text.toString().trim()

            if (vin.isNotEmpty()) {
                takeVehicleOwnership(vin)
            } else {
                displayFormError("VIN cannot be empty")
            }
        }
    }

    private fun takeVehicleOwnership(vin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayFormError("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.takeVehicleOwnership(vin)) {
                    is ResultOf.Success -> {
                        binding.tvVehicleOwnershipFragmentResult.text =
                            "Ownership successfully claimed for VIN: $vin\nDetails: ${response.data}"
                        if(response.code == 208) {
                            getAndShowPreferences(vin)
                        }
                        if(response.code == 200) {
                            showPreferences(vin, response.data)
                        }
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request. Check the VIN."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayFormError(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayFormError("Idle state")
                    ResultOf.Loading -> displayFormError("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayFormError("Error processing request: ${e.message}")
            }
        }
    }

    private fun getAndShowPreferences(vin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayFormError("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.getVehiclePreferences(vin)) {
                    is ResultOf.Success -> {
                        binding.tvVehicleOwnershipFragmentResult.text =
                            "Preferences successfully get for VIN: $vin\nDetails: ${response.data}"
                        showPreferences(vin, response.data)
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayFormError(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayFormError("Idle state")
                    ResultOf.Loading -> displayFormError("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayFormError("Error processing request: ${e.message}")
            }
        }
    }

    private suspend fun getAvailableColors(): List<ColorResponse> {
        val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

        if(accessToken.isNullOrEmpty()) {
            displayFormError("Access token not found")
            return emptyList()
        }

        val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
        val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

        return try {
            when (val response = vehicleRepository.getVehicleColors()) {
                is ResultOf.Success -> response.data
                is ResultOf.Error -> {
                    val errorMessage = "Unexpected error: ${response.message}"
                    displayFormError(errorMessage)
                    Log.e("VehicleOwnership", errorMessage)
                    emptyList()
                }
                ResultOf.Idle -> {
                    displayFormError("Idle state")
                    emptyList()
                }
                ResultOf.Loading -> {
                    displayFormError("Processing...")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleOwnership", "Error: ${e.message}", e)
            displayFormError("Error processing request: ${e.message}")
            emptyList()
        }
    }

    private suspend fun showPreferences(vin: String, vehicleData: VehicleResponse) {
        binding.tvVehicleOwnershipFragmentModel.text = vehicleData.model.name
        binding.tvVehicleOwnershipFragmentManufacturer.text = vehicleData.model.manufacturer
        binding.tvVehicleOwnershipFragmentYearBuilt.text = vehicleData.year_built.toString()

        binding.etVehicleOwnershipFragmentNickname.setText(vehicleData.nickname)

        val colorResponses: List<ColorResponse> = getAvailableColors()
        val colors: List<String> = colorResponses.map { color -> color.hex_code }
        var selectedInteriorColorIndex: Int = findCurrentColorPosition(colorResponses, vehicleData.interior_color)
        binding.btnVehicleOwnershipFragmentShowInteriorColor.setBackgroundColor(Color.parseColor(colors[selectedInteriorColorIndex]))
        var selectedExteriorColorIndex: Int = findCurrentColorPosition(colorResponses, vehicleData.outer_color)
        binding.btnVehicleOwnershipFragmentShowExteriorColor.setBackgroundColor(Color.parseColor(colors[selectedExteriorColorIndex]))

        binding.btnVehicleOwnershipFragmentInteriorColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(ArrayList(colors))
                .setDefaultSelectedColor(selectedInteriorColorIndex)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnVehicleOwnershipFragmentShowInteriorColor.setBackgroundColor(color)
                        selectedInteriorColorIndex = position
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        binding.btnVehicleOwnershipFragmentExteriorColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(ArrayList(colors))
                .setDefaultSelectedColor(selectedExteriorColorIndex)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnVehicleOwnershipFragmentShowExteriorColor.setBackgroundColor(color)
                        selectedExteriorColorIndex = position
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        binding.btnVehicleOwnershipFragmentSavePrefs.setOnClickListener {
            val prefsData = PreferencesResponse(
                nickname = binding.etVehicleOwnershipFragmentNickname.text.toString(),
                interior_color = colorResponses[selectedInteriorColorIndex],
                outer_color = colorResponses[selectedExteriorColorIndex])
            updatePreferences(vin, prefsData)
        }

        binding.viewVehicleOwnershipFragmentDivider.visibility = VISIBLE
        binding.llVehicleOwnershipFragmentPreferences.visibility = VISIBLE
    }

    private fun updatePreferences(vin: String, prefsData: PreferencesResponse) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayFormError("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.updateVehiclePreferences(vin, prefsData)) {
                    is ResultOf.Success -> {
                        cleanView()
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid preferences data."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayFormError(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayFormError("Idle state")
                    ResultOf.Loading -> displayFormError("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayFormError("Error processing request: ${e.message}")
            }
        }
    }

    private fun cleanView() {
        binding.tvVehicleOwnershipFragmentResult.text = ""
        binding.tvVehicleOwnershipFragmentModel.text = ""
        binding.tvVehicleOwnershipFragmentManufacturer.text = ""
        binding.tvVehicleOwnershipFragmentYearBuilt.text = ""
        binding.etVehicleOwnershipFragmentNickname.setText("")
        binding.viewVehicleOwnershipFragmentDivider.visibility = GONE
        binding.llVehicleOwnershipFragmentPreferences.visibility = GONE

        displayFormError("Preferences saved!")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*package com.dsd.carcompanion.vehicleOwnership

class VehicleOwnershipFragment : Fragment() {

    private var _binding: FragmentVehicleOwnershipBinding? = null
    private val binding get() = _binding!!

    @OptIn(ExperimentalStdlibApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Populate Spinner with models from backend (see api folder)
        val s: Spinner = binding.sprVehicleOwnershipFragmentModel
        val arraySpinner = arrayOf(
            "CAMERY (Toyota)", "CAMERY (Toyota)"
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this.requireContext(), R.layout.simple_spinner_item, arraySpinner)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        s.adapter = adapter

        // TODO: change depending on first default color selected in color picker
        binding.btnVehicleOwnershipFragmentShowColor.setBackgroundColor(Color.WHITE)
        var selectedColor = Color.WHITE

        // TODO: fetch colors from backend?
        binding.btnVehicleOwnershipFragmentColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(Color.WHITE, Color.BLACK, Color.GRAY, Color.DKGRAY, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)
                .setDefaultSelectedColor(0)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnVehicleOwnershipFragmentShowColor.setBackgroundColor(color)
                        selectedColor = color
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        val dayPicker = binding.dpVehicleOwnershipFragmentYear.findViewById<NumberPicker>(resources.getIdentifier("day", "id", "android"))
        dayPicker?.visibility = View.GONE

        // TODO: when click on add a vehicle button, check required fields and send new vehicle to backend
        binding.btnVehicleOwnershipFragmentSubmit.setOnClickListener{
            val model = binding.sprVehicleOwnershipFragmentModel.selectedItem.toString()
            val vin = binding.etVehicleOwnershipFragmentNumber.text.toString()
            val color = selectedColor.toHexString(HexFormat.UpperCase)
            val month = binding.dpVehicleOwnershipFragmentYear.month.toString()
            val year = binding.dpVehicleOwnershipFragmentYear.year.toString()

            // TODO: Check required fields and correct values before submit

            binding.tvVehicleOwnershipFragmentRes.text = buildString {
                append("Model: ")
                append(model)
                append("\nVIN: ")
                append(vin)
                append("\nColor: ")
                append(color)
                append("\nMonth: ")
                append(month)
                append("\nYear: ")
                append(year)
            }
        }
    }
}*/