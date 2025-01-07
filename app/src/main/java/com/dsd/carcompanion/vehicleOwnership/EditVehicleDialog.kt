package com.dsd.carcompanion.vehicleOwnership

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.ColorResponse
import com.dsd.carcompanion.api.models.PreferencesResponse
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.DialogEditVehicleBinding
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.mrudultora.colorpicker.ColorPickerBottomSheetDialog
import com.mrudultora.colorpicker.listeners.OnSelectColorListener
import com.mrudultora.colorpicker.util.ColorItemShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditVehicleDialog : DialogFragment() {

    private var vin: String? = null

    private var _binding: DialogEditVehicleBinding? = null
    private val binding get() = _binding!!
    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vin = it.getString("VIN")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditVehicleBinding.inflate(inflater, container, false)
        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        binding.pbEditVehicleDialog.visibility = VISIBLE
        binding.llEditVehicleDialog.visibility = GONE

        getVehicleData()

        binding.btnEditVehicleDialogClose.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun getVehicleData(){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accessToken = withContext(Dispatchers.IO) { jwtTokenDataStore.getAccessJwt() }

                if(accessToken.isNullOrEmpty()) {
                    displayToastMessage("Access token not found")
                    return@launch
                }

                val vehicleService = VehicleClient.getApiServiceWithToken(accessToken)
                val vehicleRepository = VehicleRepository(vehicleService, jwtTokenDataStore)

                if(!vin.isNullOrEmpty()) {
                    when (val response = vehicleRepository.getVehiclePreferences(vin!!)) {
                        is ResultOf.Success -> {
                            Log.d("EditVehicleDialog", "Found: " + response.data.toString())
                            addDataToUIElements(response.data)
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
                }
            } catch (e: Exception) {
                Log.e("VehicleOwnership", "Error: ${e.message}", e)
                displayToastMessage("Error processing request: ${e.message}")
            }
        }
    }

    private suspend fun addDataToUIElements(vehicle: VehiclePreferencesResponse) {
        binding.tvEditVehicleDialogModel.text = vehicle.model.name
        binding.tvEditVehicleDialogVin.text = "VIN: " + vehicle.vin
        binding.tvEditVehicleDialogManufacturer.text = vehicle.model.manufacturer
        binding.tvEditVehicleDialogYearBuilt.text = vehicle.year_built.toString()

        val colorResponses: List<ColorResponse> = getAvailableColors()
        val colors: List<String> = colorResponses.map { color -> color.hex_code }
        var selectedInteriorColorIndex: Int = 0
        var selectedExteriorColorIndex: Int = 0

        if(vehicle.user_preferences != null){
            binding.etEditVehicleDialogNickname.setText(vehicle.user_preferences.nickname?: "")
            binding.cbEditVehicleDialogInteriorMetalic.isChecked =
                vehicle.user_preferences.interior_color?.is_metallic ?: false
            binding.cbEditVehicleDialogExteriorMetalic.isChecked =
                vehicle.user_preferences.exterior_color?.is_metallic ?: false

            selectedInteriorColorIndex =
                if(vehicle.user_preferences.interior_color == null){
                    findCurrentColorPosition(colorResponses, vehicle.default_interior_color)
                } else {
                    findCurrentColorPosition(colorResponses, vehicle.user_preferences.interior_color)
                }
            binding.btnEditVehicleDialogShowInteriorColor.setBackgroundColor(Color.parseColor(colors[selectedInteriorColorIndex]))
            selectedExteriorColorIndex =
                if(vehicle.user_preferences.exterior_color == null){
                    findCurrentColorPosition(colorResponses, vehicle.default_exterior_color)
                } else{
                    findCurrentColorPosition(colorResponses, vehicle.user_preferences.exterior_color)
                }
            binding.btnEditVehicleDialogShowExteriorColor.setBackgroundColor(Color.parseColor(colors[selectedExteriorColorIndex]))
        } else {
            binding.cbEditVehicleDialogInteriorMetalic.isChecked =
                vehicle.default_interior_color.is_metallic
            binding.cbEditVehicleDialogExteriorMetalic.isChecked =
                vehicle.default_exterior_color.is_metallic

            selectedInteriorColorIndex = findCurrentColorPosition(colorResponses, vehicle.default_interior_color)
            binding.btnEditVehicleDialogShowInteriorColor.setBackgroundColor(Color.parseColor(colors[selectedInteriorColorIndex]))

            selectedExteriorColorIndex = findCurrentColorPosition(colorResponses, vehicle.default_exterior_color)
            binding.btnEditVehicleDialogShowExteriorColor.setBackgroundColor(Color.parseColor(colors[selectedExteriorColorIndex]))
        }

        binding.pbEditVehicleDialog.visibility = GONE
        binding.llEditVehicleDialog.visibility = VISIBLE

        binding.btnEditVehicleDialogInteriorColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(ArrayList(colors))
                .setDefaultSelectedColor(selectedInteriorColorIndex)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnEditVehicleDialogShowInteriorColor.setBackgroundColor(color)
                        selectedInteriorColorIndex = position
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        binding.btnEditVehicleDialogExteriorColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(ArrayList(colors))
                .setDefaultSelectedColor(selectedExteriorColorIndex)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnEditVehicleDialogShowExteriorColor.setBackgroundColor(color)
                        selectedExteriorColorIndex = position
                    }
                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        binding.btnEditVehicleDialogSave.setOnClickListener {
            if (!binding.etEditVehicleDialogNickname.text.toString().matches("^[a-zA-Z0-9\\s\\-]+$".toRegex())) {
                displayToastMessage("Nickname can only contain letters, numbers, spaces, and hyphens.")
            } else {
                val prefsData = PreferencesResponse(
                    nickname = binding.etEditVehicleDialogNickname.text.toString(),
                    interior_color = colorResponses[selectedInteriorColorIndex],
                    exterior_color = colorResponses[selectedExteriorColorIndex])

                Log.d("EditVehiceDialog", prefsData.toString())
                //updatePreferences(vin, prefsData)
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

    private fun findCurrentColorPosition(colorResponses: List<ColorResponse>, currentColor: ColorResponse): Int {
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

    private fun displayToastMessage(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(vin: String): EditVehicleDialog {
            val args = Bundle()
            args.putString("VIN", vin)
            val fragment = EditVehicleDialog()
            fragment.arguments = args
            return fragment
        }
    }
}