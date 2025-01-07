package com.dsd.carcompanion.vehicleOwnership

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ColorResponse
import com.dsd.carcompanion.api.models.PreferencesResponse
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mrudultora.colorpicker.ColorPickerBottomSheetDialog
import com.mrudultora.colorpicker.listeners.OnSelectColorListener
import com.mrudultora.colorpicker.util.ColorItemShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VehicleOwnershipFragment : Fragment() {

    private fun displayToastMessage(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    private fun findCurrentColorPosition(
        colorResponses: List<ColorResponse>,
        currentColor: ColorResponse
    ): Int {
        Log.d("Colors", "$colorResponses")
        Log.d("Current", "$currentColor")
        var index = colorResponses.indexOfFirst {
            it.name == currentColor.name &&
                    it.hex_code == currentColor.hex_code &&
                    it.is_metallic == currentColor.is_metallic
        }
        index = if (index == -1) 0 else index
        return index
    }

    private var _binding: FragmentVehicleOwnershipBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore
    private var _bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleOwnershipBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llVehicleOwnershipFragmentBottomSheet)
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCurrentVehicles(requireContext())

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
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
                displayToastMessage("VIN cannot be empty")
            }
        }
    }

    private fun getCurrentVehicles(context: Context) {
        binding.pbVehicleOwnershipVehicleList.visibility = VISIBLE
        binding.tvVehicleOwnershipFragmentNoVehicles.visibility = GONE

        //Remove old vehicles
        val linearLayout = binding.llVehicleOwnershipFragmentVehicleList
        for (i in linearLayout.childCount - 1 downTo 0) {
            val view = linearLayout.getChildAt(i)
            if (view is LinearLayout) {
                linearLayout.removeViewAt(i)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.getOwnedVehicles()) {
                    is ResultOf.Success -> {
                        if(response.data.isEmpty()){
                            binding.pbVehicleOwnershipVehicleList.visibility = GONE
                            binding.tvVehicleOwnershipFragmentNoVehicles.visibility = VISIBLE
                        } else {
                            addListOfVehiclesToView(context, response.data)
                        }
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request. Check the VIN."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private fun addListOfVehiclesToView(context: Context, vehicleList: List<VehiclePreferencesResponse>) {
        val linearLayout = binding.llVehicleOwnershipFragmentVehicleList
        val themedContext = ContextThemeWrapper(context, R.style.Base_Theme_CarCompanion)

        for (vehicle in vehicleList){
            Log.d("VehicleOwnership", vehicle.toString())

            var vin = vehicle.vin
            var modelName = vehicle.model.name
            var modelManufacturer = vehicle.model.manufacturer
            var yearBuild = vehicle.year_built

            //Parent container
            val vehicleContainer = LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 32)
                }
            }
            //Item container for model name and close icon
            val itemTitleContainer = LinearLayout(themedContext).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            //Model name textview
            val vehicleTextViewModelName = TextView(themedContext).apply {
                text = modelName
                textSize = 22f
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_bold) // Replace with your font
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = android.view.Gravity.START
            }
            //Close button imageview
            val closeButton = ImageView(themedContext).apply {
                setImageDrawable(ContextCompat.getDrawable(themedContext, R.drawable.ic_close))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END
                }
                setPadding(4, 4, 4, 4) // Padding for the icon
                setOnClickListener {
                    removeVehicle(vin, context)
                }
            }
            itemTitleContainer.addView(vehicleTextViewModelName)
            itemTitleContainer.addView(closeButton)

            //Back to the parent container
            //Manufacturer text view
            val vehicleTextViewModelManufacturer= TextView(themedContext).apply {
                text = "Manufacturer: " + modelManufacturer
                textSize = 16f
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_medium)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            //Vin text view
            val vehicleTextViewModelVin= TextView(themedContext).apply {
                text = "Vin: " + vin
                textSize = 16f
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_medium)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            //Year build text view
            val vehicleTextViewModelYearBuild= TextView(themedContext).apply {
                text = "Year build: " + yearBuild
                textSize = 16f
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_medium)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            //Button to open a dialog to edit owned cars
            val editButton = Button(themedContext).apply {
                text = "Edit Vehicle"
                background = ContextCompat.getDrawable(themedContext, R.drawable.custom_button)
                setPadding(40, 6, 40, 6)
                setTextColor(ContextCompat.getColor(themedContext, R.color.white))
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_bold)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END
                }

                // Set click listener for the button
                setOnClickListener {
                    val editDialog = EditVehicleDialog.newInstance(vin)
                    editDialog.show(parentFragmentManager, "EditVehicleDialog")
                }
            }

            // Add the views to the vehicle container
            vehicleContainer.addView(itemTitleContainer)
            vehicleContainer.addView(vehicleTextViewModelManufacturer)
            vehicleContainer.addView(vehicleTextViewModelVin)
            vehicleContainer.addView(vehicleTextViewModelYearBuild)
            vehicleContainer.addView(editButton)

            // Add the vehicle container to the main LinearLayout
            linearLayout.addView(vehicleContainer)
        }

        binding.pbVehicleOwnershipVehicleList.visibility = GONE
    }

    private fun removeVehicle(vin: String, context: Context) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.removeOwnedVehicleForUser(vin)) {
                    is ResultOf.Success -> {
                        displayToastMessage("Vehicle: $vin successfully removed.")
                        getCurrentVehicles(context)
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request. Check the VIN."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", "Error: " + errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private fun takeVehicleOwnership(vin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }
                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                when (val response = vehicleRepository.takeVehicleOwnership(vin)) {
                    is ResultOf.Success -> {
                        if(response.code == 208) {
                            displayToastMessage("Already owning vehicle for VIN: $vin\n")
                        }
                        if(response.code == 200){
                            displayToastMessage("Ownership successfully claimed for VIN: $vin\n")
                            getCurrentVehicles(requireContext())
                            val editDialog = EditVehicleDialog.newInstance(vin)
                            editDialog.show(parentFragmentManager, "EditVehicleDialog")
                        }
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request. Check the VIN."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private fun takeVehicleOwnership2(vin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
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
                            val vehicleData = VehiclePreferencesResponse(
                                vin = response.data.vin,
                                model = response.data.model,
                                year_built = response.data.year_built,
                                default_interior_color = response.data.default_interior_color,
                                default_exterior_color = response.data.default_exterior_color,
                                user_preferences = PreferencesResponse(null, null, null)
                            )
                            showPreferences(vin, vehicleData)
                        }
                    }
                    is ResultOf.Error -> {
                        val errorMessage = when (response.code) {
                            400 -> "Invalid request. Check the VIN."
                            403 -> "Unauthorized access."
                            404 -> "Vehicle not found."
                            else -> "Unexpected error: ${response.message}"
                        }
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private fun getAndShowPreferences(vin: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
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
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private suspend fun getAvailableColors(): List<ColorResponse> {
        val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

        if(accessToken.isNullOrEmpty()) {
            displayToastMessage("Access token not found")
            return emptyList()
        }

        val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
        val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

        return try {
            when (val response = vehicleRepository.getVehicleColors()) {
                is ResultOf.Success -> response.data
                is ResultOf.Error -> {
                    val errorMessage = "Unexpected error: ${response.message}"
                    displayToastMessage(errorMessage)
                    Log.e("VehicleOwnership", errorMessage)
                    emptyList()
                }
                ResultOf.Idle -> {
                    displayToastMessage("Idle state")
                    emptyList()
                }
                ResultOf.Loading -> {
                    displayToastMessage("Processing...")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("VehicleOwnership", "Error: ${e.message}", e)
            displayToastMessage("Error processing request: ${e.message}")
            emptyList()
        }
    }

    private suspend fun showPreferences(vin: String, vehicleData: VehiclePreferencesResponse) {
        binding.tvVehicleOwnershipFragmentModel.text = vehicleData.model.name
        binding.tvVehicleOwnershipFragmentManufacturer.text = vehicleData.model.manufacturer
        binding.tvVehicleOwnershipFragmentYearBuilt.text = vehicleData.year_built.toString()

        binding.etVehicleOwnershipFragmentNickname.setText(vehicleData.user_preferences.nickname
            ?: "")

        val colorResponses: List<ColorResponse> = getAvailableColors()
        val colors: List<String> = colorResponses.map { color -> color.hex_code }
        var selectedInteriorColorIndex: Int = if(vehicleData.user_preferences.interior_color == null) findCurrentColorPosition(colorResponses, vehicleData.default_interior_color) else findCurrentColorPosition(colorResponses, vehicleData.user_preferences.interior_color)
        binding.btnVehicleOwnershipFragmentShowInteriorColor.setBackgroundColor(Color.parseColor(colors[selectedInteriorColorIndex]))
        var selectedExteriorColorIndex: Int = if(vehicleData.user_preferences.exterior_color == null) findCurrentColorPosition(colorResponses, vehicleData.default_exterior_color) else findCurrentColorPosition(colorResponses, vehicleData.user_preferences.exterior_color)
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
            if (!binding.etVehicleOwnershipFragmentNickname.text.toString().contains("^[a-zA-Z0-9\\s\\-]+\$")) {
                displayToastMessage("Nickname can only contain letters, numbers, spaces, and hyphens.")
            } else {
                val prefsData = PreferencesResponse(
                    nickname = binding.etVehicleOwnershipFragmentNickname.text.toString(),
                    interior_color = colorResponses[selectedInteriorColorIndex],
                    exterior_color = colorResponses[selectedExteriorColorIndex])
                updatePreferences(vin, prefsData)
            }
        }

        binding.viewVehicleOwnershipFragmentDivider.visibility = VISIBLE
        binding.llVehicleOwnershipFragmentPreferences.visibility = VISIBLE
    }

    private fun updatePreferences(vin: String, prefsData: PreferencesResponse) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
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
                        displayToastMessage(errorMessage)
                        Log.e("VehicleOwnership", errorMessage)
                    }
                    ResultOf.Idle -> displayToastMessage("Idle state")
                    ResultOf.Loading -> displayToastMessage("Processing...")
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
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

        displayToastMessage("Preferences saved!")
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