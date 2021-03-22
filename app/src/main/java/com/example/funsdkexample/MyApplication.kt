package com.example.funsdkexample

import android.app.Application
import com.lib.funsdk.support.FunSupport

class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()

        FunSupport.getInstance().init(this)
    }


}