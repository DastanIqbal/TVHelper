package com.dastanapps.poweroff.common.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 *
 * Created by Iqbal Ahmed on 22/02/2023 3:39 PM
 *
 */

private const val TAG = "NetworkUtilsExt"

data class InternetConnection(
    val isWifi: Boolean = false,
    val isMobile: Boolean = false,
)

@RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun internetConnectionStatus(context: Context): InternetConnection {
    var isWifi = false
    var isMobile = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

    if (networkCapabilities != null) {
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            isWifi = true
            Log.d(TAG, "Device is connected to wifi data")
        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            isMobile = true
            Log.d(TAG, "Device is connected to mobile data")
        }
    } else {
        Log.d(TAG, "Device is not connected to a network")
    }

    return InternetConnection(isWifi = isWifi, isMobile = isMobile)
}

@RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun isConnectedToWifi(context: Context): Boolean {
    return internetConnectionStatus(context = context).isWifi
}

@RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
fun isConnectedToMobile(context: Context): Boolean {
    return internetConnectionStatus(context = context).isMobile
}

fun openWifiSettings(context: Context) {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    context.startActivity(intent)
}