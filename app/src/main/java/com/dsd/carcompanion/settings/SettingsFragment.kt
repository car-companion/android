package com.dsd.carcompanion.settings

import android.content.res.Configuration
import android.os.Bundle
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

        binding.switchNightMode.isChecked = isNightMode;

        // Connect the UI components to functions
        setupListeners()
    }

    private fun setupListeners() {
        // User Notifications Button
        binding.switchNotification.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            //Toast.makeText(context, "User Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // Night Mode Switch
        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            //TODO: Implement Night Mode functionality in the next sprint
            if (isChecked) {
                Toast.makeText(context, "Night Mode Enabled", Toast.LENGTH_SHORT).show()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                Toast.makeText(context, "Night Mode Disabled", Toast.LENGTH_SHORT).show()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(dialogView: View) {
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
