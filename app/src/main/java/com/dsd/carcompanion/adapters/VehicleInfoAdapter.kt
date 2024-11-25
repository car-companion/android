package com.dsd.carcompanion.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dsd.carcompanion.api.models.VehicleInfo
import com.dsd.carcompanion.databinding.ItemVehicleInfoBinding

class VehicleInfoAdapter(private val vehicleInfoList: List<VehicleInfo>) : RecyclerView.Adapter<VehicleInfoAdapter.VehicleInfoViewHolder>() {

    private lateinit var binding: ItemVehicleInfoBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleInfoViewHolder {
        val binding = ItemVehicleInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleInfoViewHolder, position: Int) {
        val vehicleInfo = vehicleInfoList[position]
        holder.bind(vehicleInfo)
    }

    override fun getItemCount(): Int = vehicleInfoList.size

    class VehicleInfoViewHolder(private val binding: ItemVehicleInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicleInfo: VehicleInfo) {
            binding.vehicleInfo = vehicleInfo
        }
    }
}
