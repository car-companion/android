package com.dsd.carcompanion.vehicleOwnership

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dsd.carcompanion.databinding.FragmentVehicleOwnershipBinding
import com.mrudultora.colorpicker.ColorPickerBottomSheetDialog
import com.mrudultora.colorpicker.listeners.OnSelectColorListener
import com.mrudultora.colorpicker.util.ColorItemShape


class VehicleOwnershipFragment : Fragment() {

    fun displayFormError(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    private var _binding: FragmentVehicleOwnershipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleOwnershipBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Populate Spinner with models from backend (see api folder)
        val s: Spinner = binding.sprVehicleOwnershipFragmentModel
        val arraySpinner = arrayOf(
            "CAMERY (Toyota)", "CAMERY (Toyota)"
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this.requireContext(), R.layout.simple_spinner_item, arraySpinner)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        s.adapter = adapter

        // TODO: change depending on first default color selected in color picker
        binding.btnVehicleOwnershipFragmentShowColor.setBackgroundColor(Color.WHITE)
        var selectedColor = Color.WHITE

        // TODO: fetch colors from backend?
        binding.btnVehicleOwnershipFragmentColor.setOnClickListener{
            val bottomSheetDialog = ColorPickerBottomSheetDialog(context)
            bottomSheetDialog.setColumns(6)
                .setColors(Color.WHITE, Color.BLACK, Color.GRAY, Color.DKGRAY, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)
                .setDefaultSelectedColor(0)
                .setColorItemShape(ColorItemShape.CIRCLE)
                .setOnSelectColorListener(object : OnSelectColorListener {
                    override fun onColorSelected(color: Int, position: Int) {
                        binding.btnVehicleOwnershipFragmentShowColor.setBackgroundColor(color)
                        selectedColor = color
                    }

                    override fun cancel() {
                        bottomSheetDialog.dismissDialog()
                    }
                })
                .show()
        }

        val dayPicker = binding.dpVehicleOwnershipFragmentYear.findViewById<NumberPicker>(resources.getIdentifier("day", "id", "android"))
        dayPicker?.visibility = View.GONE

        // TODO: when click on add a vehicle button, check required fields and send new vehicle to backend
        binding.btnVehicleOwnershipFragmentSubmit.setOnClickListener{
            val model = binding.sprVehicleOwnershipFragmentModel.selectedItem.toString()
            val vin = binding.etVehicleOwnershipFragmentNumber.text.toString()
            val color = selectedColor.toHexString(HexFormat.UpperCase)
            val month = binding.dpVehicleOwnershipFragmentYear.month.toString()
            val year = binding.dpVehicleOwnershipFragmentYear.year.toString()

            // TODO: Check required fields and correct values before submit

            binding.tvVehicleOwnershipFragmentRes.text = buildString {
                append("Model: ")
                append(model)
                append("\nVIN: ")
                append(vin)
                append("\nColor: ")
                append(color)
                append("\nMonth: ")
                append(month)
                append("\nYear: ")
                append(year)
            }
        }
    }
}