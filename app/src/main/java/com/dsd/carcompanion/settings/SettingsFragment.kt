package com.dsd.carcompanion.settings

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isNightMode =  nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        binding.customSwitchNightMode.isChecked = isNightMode

        // Connect the UI components to functions
        setupListeners()
    }

    private fun setupListeners() {
        // App Configuration Button
        binding.btnPrivacyPolicy.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_privacy_policy, null)
            showDialog(dialogView)
        }

        // App Configuration Button
        binding.btnTermsAndConditions.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_conditions, null)
            showDialog(dialogView)
        }

        // App Configuration Button
        binding.btnAboutApp.setOnClickListener {
            Toast.makeText(context, "This is it :). Enjoy the app", Toast.LENGTH_SHORT).show()
        }

        // Setup switch functionality
        setupCustomSwitchNotifications()
        setupCustomSwitchNightMode()
    }

    // Custom switch handler
    private fun setupCustomSwitchNotifications() {
        val customSwitchNotification = binding.customSwitchNotification
        val switchLabelNotification = binding.tvSwitchLabelNotification

        // Set initial text
        switchLabelNotification.text = "Enable Notifications"

        customSwitchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchLabelNotification.text = "Notifications Enabled"
            } else {
                switchLabelNotification.text = "Notifications Disabled"
            }
        }
    }

    private fun setupCustomSwitchNightMode() {
        val customSwitchNightMode = binding.customSwitchNightMode
        val switchLabelNightMode = binding.tvSwitchLabelNightMode

        // Set initial text
        switchLabelNightMode.text = "Night Mode"

        customSwitchNightMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                switchLabelNightMode.text = "Night Mode Enabled"
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                switchLabelNightMode.text = "Night Mode Disabled"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(dialogView: View) {
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
