package com.dsd.carcompanion.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Connect the UI components to functions
        setupListeners()
    }

    private fun setupListeners() {
        // User Notifications Button
        binding.switchNotification.setOnClickListener {
            //TODO: Implement User Settings functionality in the next sprint
            Toast.makeText(context, "User Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // Night Mode Switch
        binding.switchNightMode.setOnCheckedChangeListener { _, isChecked ->
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

        // App Configuration Button
        binding.btnTermsAndConditions.setOnClickListener {
            //TODO: Implement App Configuration functionality in the next sprint
            Toast.makeText(context, "App Configurations clicked", Toast.LENGTH_SHORT).show()
        }

        // App Configuration Button
        binding.btnAboutApp.setOnClickListener {
            //TODO: Implement App Configuration functionality in the next sprint
            Toast.makeText(context, "App Configurations clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
