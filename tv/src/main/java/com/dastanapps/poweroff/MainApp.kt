package com.dastanapps.poweroff

import android.app.Application
import com.dastanapps.poweroff.common.log.Logger

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 1:58 PM
 *
 */

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun log(msg: String) = Logger.logDebug("TV Remote", msg)
    }
}