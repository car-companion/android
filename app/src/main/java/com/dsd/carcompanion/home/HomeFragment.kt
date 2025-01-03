package com.dsd.carcompanion.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.dsd.carcompanion.R
import com.dsd.carcompanion.adapters.VehicleInfoAdapter
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
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
        recyclerView = binding.rvHomeFragmentVehicleInfo
        vehicleInfoAdapter = VehicleInfoAdapter(vehicleInfoList)
        recyclerView.adapter = vehicleInfoAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _bottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        _bottomSheetBehavior?.isDraggable = true
        _bottomSheetBehavior?.isHideable = false
        _bottomSheetBehavior?.peekHeight = 150

        binding.fabHomeFragmentDimension.setOnClickListener {
            val is3DMode = binding.fabHomeFragmentDimension.text == getString(R.string.home_fragment_3d_mode_fab_dimension)
            binding.fabHomeFragmentDimension.text = getString(
                if (is3DMode) R.string.home_fragment_2d_mode_fab_dimension else R.string.home_fragment_3d_mode_fab_dimension
            )
        }

        binding.swHomeFragmentVehicle.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
               binding.tvHomeFragmentVehicleState.text = getString(R.string.bottom_sheet_vehicle_unlocked_tv_state)
            } else {
                binding.tvHomeFragmentVehicleState.text = getString(R.string.bottom_sheet_vehicle_locked_tv_state)
            }
        }

        binding.swHomeFragmentWindows.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.tvHomeFragmentWindowsState.text = getString(R.string.bottom_sheet_windows_opened_tv_state)
            } else {
                binding.tvHomeFragmentWindowsState.text = getString(R.string.bottom_sheet_windows_closed_tv_state)
            }
        }

        binding.swHomeFragmentLights.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.tvHomeFragmentLightsState.text = getString(R.string.bottom_sheet_lights_on_tv_state)
            } else {
                binding.tvHomeFragmentLightsState.text = getString(R.string.bottom_sheet_lights_off_tv_state)
            }
        }

        binding.tvHomeFragmentTemperature.text = buildString {
            append(binding.sliderHomeFragmentTemperature.value.toString())
            append(getString(R.string.bottom_sheet_temperature_degrees_tv_state))
        }
        binding.sliderHomeFragmentTemperature.addOnChangeListener { _, value, _ ->
            binding.tvHomeFragmentTemperature.text = buildString {
                append(value.toString())
                append(getString(R.string.bottom_sheet_temperature_degrees_tv_state))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}