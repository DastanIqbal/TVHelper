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