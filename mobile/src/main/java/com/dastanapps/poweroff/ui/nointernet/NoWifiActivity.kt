package com.dastanapps.poweroff.ui.nointernet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.dastanapps.poweroff.common.utils.isConnectedToWifi
import com.dastanapps.poweroff.wifi.MainApp

data class NoWifiUIState(val wifiState: MutableState<Boolean>)

class NoWifiActivity : ComponentActivity() {
    private val wifiUIState by lazy { NoWifiUIState(mutableStateOf(false)) }

    private val isConnected get() = isConnectedToWifi(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            wifiUIState.wifiState.value = !isConnected
            NoWifiDialog(wifiUIState) { finish() }
        }

        MainApp.INSTANCE.noWifiReceiver.callback = {
            if (it) finish()
        }
    }

    override fun onResume() {
        super.onResume()
        wifiUIState.wifiState.value = !isConnected
        if (isConnected) {
            finish()
        }
    }
}