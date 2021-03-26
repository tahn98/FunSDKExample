package com.example.funsdkexample.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.funsdkexample.databinding.ActivityGuideDeviceBinding
import com.lib.FunSDK
import com.lib.funsdk.support.*
import com.lib.funsdk.support.config.ChannelSystemFunction
import com.lib.funsdk.support.config.SystemInfo
import com.lib.funsdk.support.models.FunDevice
import com.lib.funsdk.support.utils.FileUtils
import com.lib.funsdk.support.widget.FunVideoView
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import org.jetbrains.anko.toast
import java.io.File

class GuideDeviceActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener, OnFunDeviceOptListener, MediaPlayer.OnInfoListener {
    private lateinit var binding: ActivityGuideDeviceBinding
    private lateinit var funVideoView: FunVideoView
    private var funDevice: FunDevice? = null
    private var canToPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuideDeviceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val devId = intent.getIntExtra("FUN_DEVICE_ID", 0)
        funDevice = FunSupport.getInstance().findDeviceById(devId)

        funVideoView = binding.videoView
        funVideoView.setOnPreparedListener(this)
        funVideoView.setOnInfoListener(this)

        FunSupport.getInstance().registerOnFunDeviceOptListener(this)

        if(funDevice?.hasLogin() == false || funDevice?.hasConnected() == false){
            loginDevice()
        }else{
            requestSystemInfo()
            requestChannelSystemFunction()
        }

        bindEvents()
    }

    override fun onResume() {
        super.onResume()
        if(canToPlay){
            playRealMedia()
        }
    }

    override fun onDestroy() {
        FunSupport.getInstance().removeOnFunDeviceOptListener(this)
        super.onDestroy()
    }

    private fun loginDevice(){
        FunSupport.getInstance().requestDeviceLogin(funDevice)
    }

    private fun requestSystemInfo(){
        FunSupport.getInstance().requestDeviceConfig(funDevice, SystemInfo.CONFIG_NAME)
    }

    private fun playRealMedia(){
        if(funDevice?.isRemote == true){
            funVideoView.setRealDevice(funDevice?.getDevSn(), funDevice?.CurrChannel!!)
        }else{
            val deviceIp = FunSupport.getInstance().deviceWifiManager.gatewayIp
            funVideoView.setRealDevice(deviceIp, funDevice?.CurrChannel!!)
        }
    }

    private fun requestChannelSystemFunction(){
        val channelSystemFunction = ChannelSystemFunction()
        FunSupport.getInstance().requestDeviceCmdGeneral(funDevice, channelSystemFunction)
    }

    private fun onDeviceSaveNativePws(){
        FunDevicePassword.getInstance().saveDevicePassword(funDevice?.getDevSn(), "123")
        if(FunSupport.getInstance().saveNativePassword){
            FunSDK.DevSetLocalPwd(funDevice?.getDevSn(), "admin", "123")
        }
    }

    private fun bindEvents(){
        binding.btCapture?.setOnClickListener {
            tryToCapture()
        }
        binding.btRecord?.setOnClickListener {
            tryToRecord()
        }
        binding.btSwitchOrientation?.setOnClickListener {
            switchOrientation()
        }
        binding.btVideoRecord?.setOnClickListener {
            startRecordList()
        }
    }

    private fun switchOrientation(){
        if(requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun tryToRecord(){
        if(!funVideoView.isPlaying){
            toast("Record failure, need playing")
            return
        }

        if(funVideoView.bRecord){
            funVideoView.stopRecordVideo()
            binding.imgRecording?.visibility = View.INVISIBLE
            binding.btRecord?.text = "Record"
            toast("stop record/ save video to" + funVideoView.filePath)
        }else{
            funVideoView.startRecordVideo(null)
            binding.imgRecording?.visibility = View.VISIBLE
            binding.btRecord?.text = "Stop Record"
            toast("start record")
        }
    }

    private fun screenShot(path: String){
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inDither = true
        val bitmap = BitmapFactory.decodeFile(path)

        val file = File(path)
        val imgPath = File(FunPath.PATH_PHOTO + File.separator + file.name)
        if(imgPath.exists()){
            toast("Picture exists")
        }else{
            FileUtils.copyFile(path, FunPath.PATH_PHOTO + File.separator + file.name)
            toast("Save in /FunSDKDemo/snapshot/")
        }
    }

    private fun tryToCapture(){
        if(!funVideoView.isPlaying){
            toast("Media capture failure, need playing")
            return
        }
        val path = funVideoView.captureImage(null)
        if(!path.isNullOrEmpty()){
            Handler(Looper.getMainLooper()).postDelayed({
                screenShot(path)
            }, 200)
        }
    }

    fun startPictureList(){
        val intent = Intent()
        intent.putExtra("FUN_DEVICE_ID", funDevice?.id)
        intent.putExtra("FILE_TYPE", "jpg")
        intent.setClass(this, GuideDeviceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startRecordList(){
        val intent = Intent()
        intent.putExtra("FUN_DEVICE_ID", funDevice?.id)
        intent.putExtra("FILE_TYPE", "h264;mp4")
        intent.setClass(this, DeviceVideoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onPrepared(mp: MediaPlayer?) {
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
        if(SystemInfo.CONFIG_NAME == configName){
            if(funDevice?.channel == null){
                FunSupport.getInstance().requestGetDevChnName(funDevice)
                requestSystemInfo()
                requestChannelSystemFunction()
                return
            }

            canToPlay = true
            playRealMedia()
        }
    }

    override fun onDeviceSetConfigSuccess(funDevice: FunDevice?, configName: String?) {
    }

    override fun onDeviceLoginSuccess(funDevice: FunDevice?) {
        if(funDevice != null && this.funDevice != null){
            if(funDevice.id == this.funDevice?.id){
                requestSystemInfo()
                requestChannelSystemFunction()
            }
        }
    }

    override fun onDeviceLoginFailed(funDevice: FunDevice?, errCode: Int?) {
        if(errCode == FunError.EE_DVR_PASSWORD_NOT_VALID){
            toast("Password not valid")
            onDeviceSaveNativePws()
            loginDevice()
        }
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean = true
}