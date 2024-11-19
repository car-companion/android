package com.dsd.carcompanion.vehicleOwnership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.mrudultora.colorpicker.ColorPickerBottomSheetDialog
import com.mrudultora.colorpicker.listeners.OnSelectColorListener
import com.mrudultora.colorpicker.util.ColorItemShape


class VehicleOwnershipFragment : Fragment() {

    private var _binding: FragmentVehicleOwnershipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleOwnershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dayPicker = binding.dpVehicleOwnershipFragmentYear.findViewById<NumberPicker>(resources.getIdentifier("day", "id", "android"))
        dayPicker?.visibility = View.GONE

        binding.btnVehicleOwnershipFragmentColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors()
                .setDefaultSelectedColor(0)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        // handle color or position
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }
    }
}