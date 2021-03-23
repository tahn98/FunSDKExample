package com.example.funsdkexample.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.funsdkexample.adapter.DeviceListAdapter
import com.example.funsdkexample.databinding.ActivityDeviceUserListBinding
import com.lib.MsgContent
import com.lib.funsdk.support.FunSupport
import com.lib.funsdk.support.OnAddSubDeviceResultListener
import com.lib.funsdk.support.OnFunDeviceListener
import com.lib.funsdk.support.OnFunDeviceOptListener
import com.lib.funsdk.support.models.FunDevice
import com.lib.funsdk.support.models.FunLoginType
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import org.jetbrains.anko.toast

class DeviceUserListActivity : AppCompatActivity(), OnFunDeviceOptListener, OnFunDeviceListener,
    OnAddSubDeviceResultListener {

    private lateinit var binding: ActivityDeviceUserListBinding
    private lateinit var deviceListAdapter: DeviceListAdapter
    companion object{
        const val MESSAGE_REFRESH_DEVICE_STATUS = 0x100
        const val INTERVAL_REFRESH_DEV_STATUS = 30 * 1000
    }


    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_REFRESH_DEVICE_STATUS -> {
                    // refresh after interval delay
                    removeMessages(MESSAGE_REFRESH_DEVICE_STATUS)
                    sendEmptyMessageDelayed(
                        MESSAGE_REFRESH_DEVICE_STATUS,
                        INTERVAL_REFRESH_DEV_STATUS.toLong()
                    )
                    onRefresh()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceUserListBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        FunSupport.getInstance().loginType = FunLoginType.LOGIN_BY_INTENTT
        FunSupport.getInstance().registerOnFunDeviceOptListener(this)
        FunSupport.getInstance().registerOnAddSubDeviceResultListener(this)

        initDeviceList()

        binding.srfDeviceList.setOnRefreshListener {
            onRefresh()
            binding.srfDeviceList.isRefreshing = false
        }
    }

    override fun onDestroy() {
        FunSupport.getInstance().removeOnFunDeviceOptListener(this)
        super.onDestroy()
    }

    private fun onRefresh(){
        deviceListAdapter.setDeviceItemList(FunSupport.getInstance().deviceList)
        FunSupport.getInstance().requestAllDeviceStatus()
    }

    private fun initDeviceList() {
        deviceListAdapter = DeviceListAdapter()
        deviceListAdapter.setDeviceItemList(FunSupport.getInstance().deviceList)
        deviceListAdapter.setOnDeviceItemClickListener(this::onDeviceItemClick)
        binding.rcvDeviceList.adapter = deviceListAdapter

        mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_DEVICE_STATUS, 100)
    }

    private fun onDeviceItemClick(funDevice: FunDevice){
        toast(funDevice.devName.toString())
        val intent = Intent(this, GuideDeviceActivity::class.java)
        intent.putExtra("FUN_DEVICE_ID", funDevice.id)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onDeviceGetConfigFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceChangeInfoFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceOptionFailed(funDevice: FunDevice?, option: String?, errCode: Int?) {
    }

    override fun onDeviceChangeInfoSuccess(funDevice: FunDevice?) {
    }

    override fun onDeviceOptionSuccess(funDevice: FunDevice?, option: String?) {
    }

    override fun onDeviceSetConfigFailed(
        funDevice: FunDevice?,
        configName: String?,
        errCode: Int?
    ) {
    }

    override fun onDeviceFileListGetFailed(funDevice: FunDevice?) {
    }

    override fun onDeviceFileListChanged(funDevice: FunDevice?) {
    }

    override fun onDeviceFileListChanged(
        funDevice: FunDevice?,
        datas: Array<out H264_DVR_FILE_DATA>?
    ) {
    }

    override fun onDeviceGetConfigSuccess(funDevice: FunDevice?, configName: String?, nSeq: Int) {
    }

    override fun onDeviceSetConfigSuccess(funDevice: FunDevice?, configName: String?) {
    }

    override fun onDeviceLoginSuccess(funDevice: FunDevice?) {
    }

    override fun onDeviceLoginFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceAddedSuccess() {
    }

    override fun onDeviceListChanged() {
    }

    override fun onDeviceStatusChanged(funDevice: FunDevice?) {
    }

    override fun onDeviceRemovedFailed(errCode: Int?) {
    }

    override fun onAPDeviceListChanged() {
    }

    override fun onDeviceAddedFailed(errCode: Int?) {
    }

    override fun onDeviceRemovedSuccess() {
    }

    override fun onLanDeviceListChanged() {
    }

    override fun onAddSubDeviceSuccess(funDevice: FunDevice?, msgContent: MsgContent?) {
    }

    override fun onAddSubDeviceFailed(funDevice: FunDevice?, msgContent: MsgContent?) {
    }
}