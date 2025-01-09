package com.dsd.carcompanion.vehicleOwnership

import android.content.Context
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
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VehicleOwnershipFragment : Fragment() {

    private fun displayToastMessage(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}