package com.dsd.carcompanion.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentSettingsBinding
import com.dsd.carcompanion.utility.ImageHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var _bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize BottomSheetBehavior
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.llSettingsFragmentBottomSheet)
        _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.imgBackground
        ImageHelper.applyBlurAndColorFilterToImageView(
            imageView,
            context,
            R.drawable.background_colors
        )

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        // Expand bottom sheet when draggable guide is tapped
        binding.llSettingsFragmentBottomSheet.setOnClickListener {
            _bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Setup switch functionality
        setupCustomSwitchNotifications()
        setupCustomSwitchNightMode()

        // Connect the UI components to functions
        setupListeners()
    }

    // Custom switch handler
    private fun setupCustomSwitchNotifications() {
        val customSwitch = binding.switchNotification.customSwitch
        val switchLabel = binding.switchNotification.tvSwitchLabel

        // Set initial text
        switchLabel.text = "Enable Notifications"

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabel.text = "Notifications Enabled"
            } else {
                switchLabel.text = "Notifications Disabled"
            }
        }
    }

    private fun setupCustomSwitchNightMode() {
        val customSwitch = binding.switchNightMode.customSwitch
        val switchLabel = binding.switchNightMode.tvSwitchLabel

        // Set initial text
        switchLabel.text = "Night Mode"

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabel.text = "Night Mode Enabled"
            } else {
                switchLabel.text = "Night Mode Disabled"
            }
        }
    }

    private fun setupListeners() {
        // User Notifications Button
        binding.switchNotification.customSwitch.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            Toast.makeText(context, "User Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // Night Mode Switch
        binding.switchNightMode.customSwitch.setOnCheckedChangeListener { _, isChecked ->
            //TODO: Implement Night Mode functionality in the next sprint
            if (isChecked) {
                Toast.makeText(context, "Night Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Night Mode Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // App Configuration Button
        binding.btnPrivacyPolicy.setOnClickListener {
            //TODO: Implement App Configuration functionality in the next sprint
            Toast.makeText(context, "App Configurations clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
