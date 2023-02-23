package com.dastanapps.poweroff.wifi.net

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.dastanapps.poweroff.wifi.MainApp
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 10:19 AM
 *
 */

private val TAG = "NETWORK-EXT"

data class NetworkInfo(
    val ipAddress: String,
    val ipAddressPrefix: String,
    val subnetAddress: String,
    val networkAddress: String,
    val broadcastAddress: String
)

fun findNetworkSubnet(): NetworkInfo {
    val wifiManager = MainApp.INSTANCE.getSystemService(Context.WIFI_SERVICE) as WifiManager
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

    return NetworkInfo(
        ipAddress = ipAddressString,
        ipAddressPrefix = ipAddressPrefixString,
        subnetAddress = subnetMaskString,
        networkAddress = networkAddressString,
        broadcastAddress = broadcastAddressString
    )
}

fun scanNetwork(subnet: String = "192.168.1.") {
    val subnet = subnet
    for (i in 0..254) {
        val host = subnet + i
        for (port in 1..65535) {
            scanIpPort(host, port)
        }
    }
}

fun scanIpPort(host: String, port: Int): Pair<String, Int>? {
    val timeout = 1000
    return try {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), timeout)
        socket.close()
        Log.d(TAG, "Found Ip: $host Port: $port")
        Pair(host, port)
    } catch (e: IOException) {
        //ignore crash
        null
    }
}

fun scanPort(subnet: String = "192.168.1.", port: Int): List<Pair<String, Int>> {
    val list = ArrayList<Pair<String, Int>>()
    val subnet = subnet
    for (i in 0..25) {
        val host = subnet + i
        Log.d(TAG, "Scanning Ip: $host Port: $port")
        val result = scanIpPort(host, port)
        result?.run { list.add(this) }
    }
    return list
}