package com.dsd.carcompanion.settings

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var isNightMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Determine the current night mode
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        binding.customSwitchNightMode.isChecked = isNightMode

        // Initialize UI components
        setupCustomSwitchNotifications()
        setupCustomSwitchNightMode()
        setupListeners()
    }

    // Initialize notification switch functionality
    private fun setupCustomSwitchNotifications() {
        val customSwitchNotification = binding.customSwitchNotification
        val switchLabelNotification = binding.tvSwitchLabelNotification

        // Set initial label text
        switchLabelNotification.text = getString(R.string.notifications)

        // Handle switch toggle
        customSwitchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("SettingsFragment", "Notifications enabled")
            } else {
                Log.d("SettingsFragment", "Notifications disabled")
            }
        }
    }

    // Initialize night mode switch functionality
    private fun setupCustomSwitchNightMode() {
        val customSwitch = binding.customSwitchNightMode
        val switchLabel = binding.tvSwitchLabelNightMode

        // Set label text
        switchLabel.text = getString(R.string.night_mode)

        // Handle switch toggle
        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            Toast.makeText(
                context,
                if (isChecked) getString(R.string.night_mode_enabled)
                else getString(R.string.night_mode_disabled),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Setup general listeners for UI components
    private fun setupListeners() {
        // User notifications button
        binding.customSwitchNotification.setOnClickListener {
            Toast.makeText(context, getString(R.string.user_settings_clicked), Toast.LENGTH_SHORT).show()
        }

        binding.customSwitchNightMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.tvSwitchLabelNightMode.text = "Night Mode Enabled"
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.tvSwitchLabelNightMode.text = "Night Mode Disabled"
            }
        }
        
        // Privacy policy button
        binding.btnPrivacyPolicy.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_privacy_policy, null)
            showDialog(dialogView)
        }

        // Terms and conditions button
        binding.btnTermsAndConditions.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_conditions, null)
            showDialog(dialogView)
        }

        // About app button
        binding.btnAboutApp.setOnClickListener {
            Toast.makeText(context, getString(R.string.about_app_message), Toast.LENGTH_SHORT).show()
        }
    }

    // Show a dialog with the provided view
    private fun showDialog(dialogView: View) {
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
