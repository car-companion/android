package com.dsd.carcompanion.vehicleInterfaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.R
import com.dsd.carcompanion.databinding.FragmentGrantPermissionsBinding

class UserPermissions : Fragment() {

    private var _binding: FragmentGrantPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrantPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAccessLevelListener()

        binding.btnGrantAccess.setOnClickListener {
            val userIdentifier = binding.etUserIdentifier.text.toString()
            val selectedVehicle = binding.spinnerVehicleSelection.selectedItem?.toString()
            val selectedAccessLevel = when (binding.radioGroupAccessLevel.checkedRadioButtonId) {
                R.id.radioFullAccess -> "Full Access"
                R.id.radioCustomAccess -> "Custom Access"
                else -> null
            }

            if (userIdentifier.isNotEmpty() && selectedVehicle != null && selectedAccessLevel != null) {
                val grantedPermissions = getSelectedPermissions()
                Toast.makeText(
                    context,
                    "Access granted to $userIdentifier for $selectedVehicle with $selectedAccessLevel access: $grantedPermissions",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAccessLevelListener() {
        binding.radioGroupAccessLevel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioFullAccess -> setPermissions(locks = true, lights = true, temperature = true, extra = true)
                R.id.radioCustomAccess -> enableAllCheckBoxes(true) // Allow user customization
            }
        }
    }

    private fun setPermissions(temperature: Boolean, locks: Boolean, lights: Boolean, extra: Boolean) {
        binding.checkboxTemp.isChecked = temperature
        binding.checkboxLocks.isChecked = locks
        binding.checkboxLights.isChecked = lights
        binding.checkboxExtra.isChecked = extra

        enableAllCheckBoxes(false) // Disable further interaction
    }

    private fun enableAllCheckBoxes(enable: Boolean) {
        binding.checkboxTemp.isEnabled = enable
        binding.checkboxLocks.isEnabled = enable
        binding.checkboxLights.isEnabled = enable
        binding.checkboxExtra.isEnabled = enable
    }

    private fun getSelectedPermissions(): String {
        val permissions = mutableListOf<String>()
        if (binding.checkboxTemp.isChecked) permissions.add("Temperature")
        if (binding.checkboxLocks.isChecked) permissions.add("Locks")
        if (binding.checkboxLights.isChecked) permissions.add("Lights")
        if (binding.checkboxExtra.isChecked) permissions.add("Extra")
        return permissions.joinToString(", ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
