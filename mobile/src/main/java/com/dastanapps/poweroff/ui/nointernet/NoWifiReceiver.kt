package com.dastanapps.poweroff.ui.nointernet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.util.Log

/**
 *
 * Created by Iqbal Ahmed on 22/02/2023 5:29 PM
 *
 */

class NoWifiReceiver : BroadcastReceiver() {
    private val TAG = NoWifiReceiver::class.java.simpleName

    var callback: ((isConnected: Boolean) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
            callback?.invoke(networkInfo?.isConnectedOrConnecting == true)
            if (networkInfo?.isConnectedOrConnecting == true) {
                // Device is connected to a Wi-Fi network
                val wifiManager =
                    context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ssid = wifiInfo.ssid
                Log.d(TAG, "Device connected to Wi-Fi network: $ssid")
            } else {
                context?.startActivity(
                    Intent(context, NoWifiActivity::class.java).apply {
                        flags = FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }
    }
}