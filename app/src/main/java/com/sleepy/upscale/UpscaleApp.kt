package com.sleepy.upscale

import android.app.Application
import com.xuexiang.xui.XUI

class UpscaleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        XUI.init(this)
        XUI.debug(false)
    }
}
