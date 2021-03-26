package com.example.funsdkexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funsdkexample.databinding.LayoutDeviceItemBinding
import com.lib.funsdk.support.models.FunDevStatus
import com.lib.funsdk.support.models.FunDevice

class DeviceListAdapter : RecyclerView.Adapter<DeviceListAdapter.DeviceItemViewHolder>() {
    private var deviceItemList: ArrayList<FunDevice> = arrayListOf()
    private lateinit var onItemClickListener: (arg: FunDevice) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceItemViewHolder {
        val binding: LayoutDeviceItemBinding = LayoutDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceItemViewHolder(binding)
    }

    override fun getItemCount(): Int = deviceItemList.size

    override fun onBindViewHolder(holder: DeviceItemViewHolder, position: Int) {
        holder.bind(deviceItemList[position])
    }

    fun setDeviceItemList(deviceItemList: List<FunDevice>){
        this.deviceItemList.clear()
        this.deviceItemList.addAll(deviceItemList)
        notifyDataSetChanged()
    }

    fun setOnDeviceItemClickListener(onItemClickListener: (arg: FunDevice) -> Unit){
        this.onItemClickListener = onItemClickListener
    }

    inner class DeviceItemViewHolder(private val layoutDeviceItemBinding: LayoutDeviceItemBinding) :
        RecyclerView.ViewHolder(layoutDeviceItemBinding.root) {
        fun bind(funDevice: FunDevice){
            layoutDeviceItemBinding.device = funDevice

            if(funDevice.devStatus == FunDevStatus.STATUS_ONLINE){
                layoutDeviceItemBinding.tvStatus.text = "online"
                layoutDeviceItemBinding.tvStatus.setTextColor(-0xe88036)
            }else{
                layoutDeviceItemBinding.tvStatus.text = "offline"
                layoutDeviceItemBinding.tvStatus.setTextColor(-0x25dfd2)
            }

            layoutDeviceItemBinding.root.setOnClickListener {
                onItemClickListener.invoke(funDevice)
            }
        }
    }
}