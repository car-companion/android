package com.dsd.carcompanion.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.R
import com.dsd.carcompanion.adapters.VehicleInfoAdapter
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.navigation.fragment.findNavController
import com.dsd.carcompanion.utility.ImageHelper


class HomeFragment : Fragment() {
    //private lateinit var recyclerView: RecyclerView
    private lateinit var vehicleInfoAdapter: VehicleInfoAdapter
    private var vehicleInfoList: MutableList<VehicleInfo> = mutableListOf()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llHomeFragmentBottomSheet)
        //recyclerView = binding.rvHomeFragmentVehicleInfo
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)
        //recyclerView.adapter = vehicleInfoAdapter

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

        //This moves the toggle group with the bottom sheet, doesn't work completely as intended
//        _bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                // Handle any specific state changes if needed
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                // Calculate the new Y position of the toggle button
//                val toggleButtonY = bottomSheet.top - binding.toggleGroup.height / 2
//                binding.toggleGroup.y = toggleButtonY.toFloat()
//            }
//        })

        // Expand bottom sheet when draggable guide is tapped
        binding.llHomeFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

//        // Toggle 2D/3D mode
//        binding.fabHomeFragmentDimension.setOnClickListener {
//            val is3DMode = binding.fabHomeFragmentDimension.text == getString(R.string.home_fragment_3d_mode_fab_dimension)
//            binding.fabHomeFragmentDimension.text = getString(
//                if (is3DMode) R.string.home_fragment_2d_mode_fab_dimension else R.string.home_fragment_3d_mode_fab_dimension
//            )
//        }

        binding.menuIcon.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_FirstFragment)
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
        _binding = null
    }
}