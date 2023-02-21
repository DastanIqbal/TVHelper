package com.dastanapps.poweroff.wifi

import android.app.Application
import com.dastanapps.poweroff.wifi.data.DataStoreManager
import com.dastanapps.poweroff.wifi.net.ConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 12:27 PM
 *
 */

class MainApp : Application() {

    val connectionManager by lazy { ConnectionManager() }
    val dataStoreManager by lazy { DataStoreManager() }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

    }

    companion object {
        lateinit var INSTANCE: MainApp
        val applicationScope = MainScope()
        val applicationIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    }
}