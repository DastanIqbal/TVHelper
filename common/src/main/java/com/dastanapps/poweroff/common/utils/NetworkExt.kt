package com.dastanapps.poweroff.common.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

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

data class NetworkInfo(
    val ipAddress: String,
    val ipAddressPrefix: String,
    val subnetAddress: String,
    val networkAddress: String,
    val broadcastAddress: String
)

@RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
fun findNetworkInfo(context: Context): NetworkInfo {
    return try {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

        // Get the IP address and subnet mask of the Wi-Fi network
        val ipAddress = wifiInfo.ipAddress
        val subnetMask = wifiManager.dhcpInfo.netmask

        // Convert the IP address and subnet mask from integers to dotted decimal notation
        val ipAddressString = String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
        val ipAddressPrefixString = String.format(
            "%d.%d.%d.",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff)
        )
        val subnetMaskString = String.format(
            "%d.%d.%d.%d",
            (subnetMask and 0xff),
            (subnetMask shr 8 and 0xff),
            (subnetMask shr 16 and 0xff),
            (subnetMask shr 24 and 0xff)
        )

        // Calculate the network address and broadcast address
        val networkAddress = (ipAddress and subnetMask) // bitwise AND operation
        val broadcastAddress = networkAddress or (subnetMask.inv())

        // Convert the network address and broadcast address from integers to dotted decimal notation
        val networkAddressString = String.format(
            "%d.%d.%d.%d",
            (networkAddress and 0xff),
            (networkAddress shr 8 and 0xff),
            (networkAddress shr 16 and 0xff),
            (networkAddress shr 24 and 0xff)
        )
        val broadcastAddressString = String.format(
            "%d.%d.%d.%d",
            (broadcastAddress and 0xff),
            (broadcastAddress shr 8 and 0xff),
            (broadcastAddress shr 16 and 0xff),
            (broadcastAddress shr 24 and 0xff)
        )

        // Print the subnet information
        Log.d(TAG, "IP Address: $ipAddressString")
        Log.d(TAG, "Subnet Mask: $subnetMaskString")
        Log.d(TAG, "Network Address: $networkAddressString")
        Log.d(TAG, "Broadcast Address: $broadcastAddressString")

        NetworkInfo(
            ipAddress = ipAddressString,
            ipAddressPrefix = ipAddressPrefixString,
            subnetAddress = subnetMaskString,
            networkAddress = networkAddressString,
            broadcastAddress = broadcastAddressString
        )
    } catch (e: Exception) {
        e.printStackTrace()
        NetworkInfo(
            ipAddress = "",
            ipAddressPrefix = "",
            subnetAddress = "",
            networkAddress = "",
            broadcastAddress = "",
        )
    }
}

fun deviceIP(): String {
    return try {
        var sysIP: String? = null
        val osName = System.getProperty("os.name")
        when (osName) {
            "Windows" -> {
                sysIP = InetAddress.getLocalHost().hostAddress
            }

            "Mac OS X" -> {
                sysIP = getSystemIP4("en0")
            }

            else -> {
                val interfaceNames = arrayListOf("wlan0","eth0", "eth1", "eth2", "usb0")
                 run breaking@ {
                     interfaceNames.forEach {
                         sysIP = getSystemIP4(it)
                         if (sysIP != null)
                             return@breaking
                     }
                 }
            }
        }
        sysIP ?: ""
    } catch (E: Exception) {
        Log.d("NETWORK", "System IP Exp : " + E.message)
        ""
    }
}

//For Linux OS
private fun getSystemIP4(name: String): String? {
    return try {
        var ip: String? = null
        val networkInterface = NetworkInterface.getByName(name)
        val inetAddress = networkInterface.inetAddresses
        var currentAddress: InetAddress?
        while (inetAddress.hasMoreElements()) {
            currentAddress = inetAddress.nextElement()
            if (currentAddress is Inet4Address && !currentAddress.isLoopbackAddress()) {
                ip = currentAddress.toString()
                break
            }
        }
        if (ip != null) {
            if (ip.startsWith("/")) {
                ip = ip.substring(1)
            }
        }
        ip
    } catch (E: Exception) {
        Log.d("NETWORK", "System Linux IP Exp : " + E.message)
        null
    }
}