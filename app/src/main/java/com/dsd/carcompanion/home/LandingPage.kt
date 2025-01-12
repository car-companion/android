package com.dsd.carcompanion.home

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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.R
import com.dsd.carcompanion.api.datastore.JwtTokenDataStore
import com.dsd.carcompanion.api.instance.VehicleClient
import com.dsd.carcompanion.api.models.VehiclePreferencesResponse
import com.dsd.carcompanion.api.repository.VehicleRepository
import com.dsd.carcompanion.api.utils.ResultOf
import com.dsd.carcompanion.databinding.FragmentLandingPageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.qtproject.example.my_car_companionApp.QmlModule
import org.qtproject.qt.android.QtQmlStatus
import org.qtproject.qt.android.QtQmlStatusChangeListener
import org.qtproject.qt.android.QtQuickView

class LandingPage: Fragment(), QtQmlStatusChangeListener {

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var jwtTokenDataStore: JwtTokenDataStore

    private var m_qmlView: QtQuickView? = null
    private var m_mainQmlContent: QmlModule.Main = QmlModule.Main()

    private fun displayToastMessage(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        jwtTokenDataStore = JwtTokenDataStore(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("LandingPage", "Welcomeeeeeeeeeeeeeeee")

        //getCurrentVehicles(requireContext())
    }

    override fun onResume() {
        super.onResume()

        Log.d("LandingPage", "Resuming...")
        getCurrentVehicles(requireContext())
    }

    override fun onPause() {
        super.onPause()
        m_qmlView?.removeAllViews()
        m_qmlView = null
    }

    private fun getCurrentVehicles(context: Context) {
        binding.pbLandingVehicleList.visibility = VISIBLE
        binding.tvLandingFragmentNoVehicles.visibility = GONE

        //Remove old vehicles
        val linearLayout = binding.llLandingFragmentVehicleList
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
                            binding.pbLandingVehicleList.visibility = GONE
                            binding.tvLandingFragmentNoVehicles.visibility = VISIBLE
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
        val linearLayout = binding.llLandingFragmentVehicleList
        val themedContext = ContextThemeWrapper(context, R.style.Base_Theme_CarCompanion)

        for (vehicle in vehicleList){
            Log.d("LandingPage", vehicle.toString())

            var vin = vehicle.vin
            var modelName = vehicle.model.name
            var modelManufacturer = vehicle.model.manufacturer
            var yearBuild = vehicle.year_built

            val parentContainer = LinearLayout(themedContext).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 32)
                }
            }

            val density = themedContext.resources.displayMetrics.density
            val sizeInPx = (100 * density).toInt()

            val vehicle3DVehicleContainer = FrameLayout(themedContext).apply {
                id = R.id.vehicle_3d_container
                layoutParams = FrameLayout.LayoutParams(
                    sizeInPx,
                    sizeInPx
                )
            }

            val params: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            m_qmlView = QtQuickView(requireContext(), "Main.qml", "my_car_companionApp")

            vehicle3DVehicleContainer.addView(m_qmlView, params)
            m_qmlView!!.loadContent(m_mainQmlContent)
            Log.d("Home", "After loaded")

            m_qmlView?.setStatusChangeListener { status ->
                Log.d("HomeFragment", status.toString())
                if (status == QtQmlStatus.READY) {
                    Log.d("HomeFragment", "QtQuickView is ready")
                    m_qmlView?.setProperty("mouseAreaEnabled", true)
                }
            }

            //Parent container
            val vehicleInfoContainer = LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 32)
                    setPadding(12, 0, 0, 0)
                }
                setOnClickListener {
                    m_qmlView?.setProperty("mouseAreaEnabled", false)
                    m_qmlView?.removeAllViews()
                    m_qmlView = null
                    val bundle = Bundle().apply {
                        putString("vin", vin)
                    }
                    findNavController().navigate(R.id.action_nav_LandingPage_to_nav_HomeFragment, bundle)
                }
            }
            //Model name textview
            val vehicleTextViewModelName = TextView(themedContext).apply {
                text = modelName
                textSize = 22f
                setTextColor(Color.WHITE)
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_bold) // Replace with your font
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            //Manufacturer text view
            val vehicleTextViewModelManufacturer= TextView(themedContext).apply {
                text = "Manufacturer: " + modelManufacturer
                textSize = 16f
                setTextColor(Color.WHITE)
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
                setTextColor(Color.WHITE)
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
                setTextColor(Color.WHITE)
                typeface = ResourcesCompat.getFont(themedContext, R.font.roboto_medium)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Add the views to the vehicle container
            vehicleInfoContainer.addView(vehicleTextViewModelName)
            vehicleInfoContainer.addView(vehicleTextViewModelManufacturer)
            vehicleInfoContainer.addView(vehicleTextViewModelVin)
            vehicleInfoContainer.addView(vehicleTextViewModelYearBuild)

            // Add the vehicle container to the main LinearLayout
            parentContainer.addView(vehicle3DVehicleContainer)
            parentContainer.addView(vehicleInfoContainer)

            linearLayout.addView(parentContainer)
        }

        binding.pbLandingVehicleList.visibility = GONE
    }

    override fun onStatusChanged(status: QtQmlStatus?) {
        Log.v("CAR_COMPANION_QT", "Status of QtQuickView: $status")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "View is destroyed")
        m_qmlView?.removeAllViews()
        m_qmlView = null
        _binding = null
    }
}