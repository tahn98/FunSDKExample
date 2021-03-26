package com.example.funsdkexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.funsdkexample.databinding.LayoutRecordFileItemBinding
import com.lib.funsdk.support.models.FunFileData

class RecordFileAdapter: RecyclerView.Adapter<RecordFileAdapter.RecordFileViewHolder>() {
    private val deviceCameraRecordFile: ArrayList<FunFileData> = arrayListOf()
    private lateinit var onItemFileRecordClickListener: (arg: FunFileData) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordFileViewHolder {
        val binding = LayoutRecordFileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordFileViewHolder, position: Int) {
        holder.bind(deviceCameraRecordFile[position])
    }

    override fun getItemCount(): Int = deviceCameraRecordFile.size

    inner class RecordFileViewHolder(private val binding: LayoutRecordFileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(funFileData: FunFileData){
            binding.funFile = funFileData
            binding.root.setOnClickListener {
                onItemFileRecordClickListener.invoke(funFileData)
            }
        }
    }

    fun setRecordFileList(recordFileList: List<FunFileData>){
        this.deviceCameraRecordFile.clear()
        this.deviceCameraRecordFile.addAll(recordFileList)
        notifyDataSetChanged()
    }

    fun setOnItemFileRecordClickListener(onItemFileRecordClickListener: (arg: FunFileData) -> Unit){
        this.onItemFileRecordClickListener = onItemFileRecordClickListener
    }
}