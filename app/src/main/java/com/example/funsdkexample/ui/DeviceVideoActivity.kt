package com.example.funsdkexample.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.funsdkexample.adapter.RecordFileAdapter
import com.example.funsdkexample.databinding.ActivityDeviceVideoBinding
import com.example.funsdkexample.dialog.LoadingDialog
import com.lib.SDKCONST
import com.lib.funsdk.support.FunPath
import com.lib.funsdk.support.FunSupport
import com.lib.funsdk.support.OnFunDeviceOptListener
import com.lib.funsdk.support.OnFunDeviceRecordListener
import com.lib.funsdk.support.config.OPCompressPic
import com.lib.funsdk.support.models.FunDevRecordFile
import com.lib.funsdk.support.models.FunDevice
import com.lib.funsdk.support.models.FunFileData
import com.lib.funsdk.support.widget.FunVideoView
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.lib.sdk.struct.H264_DVR_FINDINFO
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

class DeviceVideoActivity : AppCompatActivity(), OnFunDeviceRecordListener, OnFunDeviceOptListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private var funDevice : FunDevice? = null
    private lateinit var binding: ActivityDeviceVideoBinding
    private lateinit var videoView: FunVideoView
    private lateinit var calendar: Calendar
    private lateinit var recordFileAdapter: RecordFileAdapter
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var seekBar: SeekBar
    private val MESSAGE_REFRESH_PROGRESS = 0x100
    private val MESSAGE_SEEK_PROGRESS = 0x101
    private val MESSAGE_SET_IMAGE = 0x102
    private var maxProgress = 0

    private var handler: Handler? = object : Handler(){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                MESSAGE_REFRESH_PROGRESS -> {
                    refreshProgress()
                    resetProgressInterval()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceVideoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDialog = LoadingDialog(this)
        recordFileAdapter = RecordFileAdapter()
        binding.rcvRecordList.adapter = recordFileAdapter

        videoView = binding.funRecVideoView
        videoView.setOnPreparedListener(this)
        videoView.setOnErrorListener(this)

        seekBar = binding.sbVideo
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        calendar = Calendar.getInstance()

        val devId = intent.getIntExtra("FUN_DEVICE_ID", 0)
        var funDevice = FunSupport.getInstance().findDeviceById(devId)

        if(devId ==0){
            funDevice = FunSupport.getInstance().mCurrDevice
        }
        if(funDevice == null){
            finish()
            return
        }
        else{
            this.funDevice = funDevice
        }

        FunSupport.getInstance().registerOnFunDeviceRecordListener(this)
        FunSupport.getInstance().registerOnFunDeviceOptListener(this)

        onSearchFile()

        binding.btnPlay.setOnButtonClick { buttonCheck, isChecked ->
            if(this.videoView.isPlaying){
                this.videoView.pause()
            }else{
                this.videoView.resume()
            }
            true
        }

        FunPath.onCreateSptTempPath(funDevice.serialNo)
        recordFileAdapter.setOnItemFileRecordClickListener { funFileData: FunFileData ->
            playRecordVideoByFile(funFileData)
        }
    }

    private fun refreshPlayInfo(){
        val startTime = videoView.startTime
        val endTime = videoView.endTime

        maxProgress = endTime - startTime
        if(startTime in 1 until endTime){
            seekBar.max = maxProgress
            seekBar.progress = 0

            resetProgressInterval()
        }else{
            cleanProgressInterval()
        }
    }

    private fun resetProgressInterval(){
        if(handler != null){
            handler?.removeMessages(MESSAGE_REFRESH_PROGRESS)
            handler?.sendEmptyMessageDelayed(MESSAGE_REFRESH_PROGRESS, 500)
        }
    }

    private fun cleanProgressInterval(){
        if(handler != null){
            handler?.removeMessages(MESSAGE_REFRESH_PROGRESS)
        }
    }

    private fun refreshProgress(){
        val posTm = videoView.position
        if(posTm > 0){
            seekBar.progress = posTm - videoView.startTime
        }
    }

    private fun playRecordVideoByFile(recordFile: FunFileData){
        loadingDialog.startLoadingDialog()
        videoView.stopPlayback()
        videoView.playRecordByFile(
            funDevice?.getDevSn(),
            recordFile.fileData,
            funDevice?.CurrChannel!!
        )
        videoView.setMediaSound(true)
    }

    private fun onSearchFile(){
        val time = intArrayOf(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE)
        )
        val info = H264_DVR_FINDINFO()
        info.st_1_nFileType = SDKCONST.FileType.SDK_RECORD_ALL
        info.st_2_startTime.st_0_dwYear = time[0]
        info.st_2_startTime.st_1_dwMonth = time[1]
        info.st_2_startTime.st_2_dwDay = time[2]
        info.st_2_startTime.st_3_dwHour = 0
        info.st_2_startTime.st_4_dwMinute = 0
        info.st_2_startTime.st_5_dwSecond = 0
        info.st_3_endTime.st_0_dwYear = time[0]
        info.st_3_endTime.st_1_dwMonth = time[1]
        info.st_3_endTime.st_2_dwDay = time[2]
        info.st_3_endTime.st_3_dwHour = 23
        info.st_3_endTime.st_4_dwMinute = 59
        info.st_3_endTime.st_5_dwSecond = 59
        info.st_0_nChannelN0 = funDevice?.CurrChannel!!

        loadingDialog.startLoadingDialog()
        FunSupport.getInstance().requestDeviceFileList(funDevice, info)
    }

    override fun onDestroy() {
        FunSupport.getInstance().removeOnFunDeviceOptListener(this)
        FunSupport.getInstance().removeOnFunDeviceOptListener(this)

        if(handler != null){
            handler?.removeCallbacksAndMessages(null);
            handler = null
        }

        super.onDestroy()
    }

    override fun onRequestRecordListSuccess(files: MutableList<FunDevRecordFile>?) {
        if(files == null || files.size == 0){
            toast("Device camera list empty")
        }
    }

    override fun onRequestRecordListFailed(errCode: Int?) {
    }

    override fun onDeviceLoginSuccess(funDevice: FunDevice?) {
    }

    override fun onDeviceLoginFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceGetConfigSuccess(funDevice: FunDevice?, configName: String?, nSeq: Int) {
        onSearchFile()
    }

    override fun onDeviceGetConfigFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceSetConfigSuccess(funDevice: FunDevice?, configName: String?) {
    }

    override fun onDeviceSetConfigFailed(
        funDevice: FunDevice?,
        configName: String?,
        errCode: Int?
    ) {
    }

    override fun onDeviceChangeInfoSuccess(funDevice: FunDevice?) {
    }

    override fun onDeviceChangeInfoFailed(funDevice: FunDevice?, errCode: Int?) {
    }

    override fun onDeviceOptionSuccess(funDevice: FunDevice?, option: String?) {
    }

    override fun onDeviceOptionFailed(funDevice: FunDevice?, option: String?, errCode: Int?) {
    }

    override fun onDeviceFileListChanged(funDevice: FunDevice?) {
    }

    override fun onDeviceFileListChanged(
        funDevice: FunDevice?,
        datas: Array<out H264_DVR_FILE_DATA>?
    ) {
        val files = ArrayList<FunFileData>()
        if(funDevice!=null && this.funDevice != null && funDevice.id == this.funDevice?.id){
            for(data in datas!!){
                val funFileData = FunFileData(data, OPCompressPic())
                files.add(funFileData)
            }
        }
        recordFileAdapter.setRecordFileList(files)
        loadingDialog.dismissDialog()
    }

    override fun onDeviceFileListGetFailed(funDevice: FunDevice?) {
    }

    override fun onPrepared(mp: MediaPlayer?) {
        loadingDialog.dismissDialog()
        refreshPlayInfo()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.d("tahn", what.toString())
        return true
    }
}