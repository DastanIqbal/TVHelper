package com.dastanapps.poweroff

import android.app.Application
import com.dastanapps.poweroff.common.crash.BugLog
import com.dastanapps.poweroff.common.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 1:58 PM
 *
 */

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        BugLog.getInstance().start(this)
    }

    companion object {

        var INSTANCE: MainApp? = null
        fun log(msg: String) = Logger.logDebug("TV Remote", msg)
        val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val mainScope = MainScope()
    }
}