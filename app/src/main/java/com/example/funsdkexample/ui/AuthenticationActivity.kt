package com.example.funsdkexample.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.funsdkexample.databinding.ActivityMainBinding
import com.lib.funsdk.support.FunError
import com.lib.funsdk.support.FunSupport
import com.lib.funsdk.support.OnFunLoginListener
import com.lib.funsdk.support.models.FunLoginType
import org.jetbrains.anko.toast

class AuthenticationActivity : AppCompatActivity(), OnFunLoginListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FunSupport.getInstance().loginType = FunLoginType.LOGIN_BY_INTENTT
        FunSupport.getInstance().registerOnFunLoginListener(this)

        bindEvents()
    }

    private fun bindEvents(){
        binding.btLogin.setOnClickListener {
            tryToLogin()
        }
    }

    override fun onDestroy() {
        FunSupport.getInstance().removeOnFunLoginListener(this)
        super.onDestroy()
    }

    override fun onLoginSuccess() {
        toast("Login Successfully")
        showUserInfo()
    }

    private fun showUserInfo(){
        val intent = Intent(this,DeviceUserListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onLogout() {
    }

    override fun onLoginFailed(errCode: Int?) {
        toast(FunError.getErrorStr(errCode))
    }

    private fun tryToLogin() {
        if (!FunSupport.getInstance().login("tahn98", "123456789")) {
            Toast.makeText(this, "Can not login", Toast.LENGTH_SHORT).show()
        }
    }
}