package com.dsd.carcompanion.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
        ImageHelper.applyBlurToImageView(
            imageView,
            context,
            R.drawable.homescreend,
            blurRadius = 50f
        )

        // Bottom sheet settings
        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
        _bottomSheetBehavior?.isDraggable = false
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

    // Custom switch handlers
    private fun setupCustomSwitchNotifications() {
        val switchLabel = binding.switchNotification.tvSwitchLabel
        val customSwitch = binding.switchNotification.customSwitch
        val switchLabelAction = binding.switchNotification.tvSwitchLabelAction

        val titleTextView: TextView = binding.switchNotification.tvSwitchTitle
        val titleText: String? = getString(R.string.user_settings) // Replace this with dynamic title or null if no title is needed

        if (!titleText.isNullOrEmpty()) {
            titleTextView.text = titleText
            titleTextView.visibility = View.VISIBLE
        } else {
            titleTextView.visibility = View.GONE
        }

        // Set initial text
        switchLabel.text = getString(R.string.notifications)
        switchLabelAction.text = "Disabled"  //This will depend on the user profile

        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabelAction.text = "Enabled"
            } else {
                switchLabelAction.text = "Disabled"
            }
        }
    }

    private fun setupCustomSwitchNightMode() {
        val switchLabel = binding.switchNightMode.tvSwitchLabel
        val customSwitch = binding.switchNightMode.customSwitch
        val switchLabelAction = binding.switchNightMode.tvSwitchLabelAction

        // Set initial text
        switchLabel.text = "Night Mode"
        //switchLabelAction.text = "Disabled"  //This will depend on the user profile

//        customSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                switchLabelAction.text = "Enabled"
//            } else {
//                switchLabelAction.text = "Disabled"
//            }
//        }
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
