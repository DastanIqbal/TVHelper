package com.dastanapps.poweroff.utils

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface


object NetworkUtils {

    fun getSystemIP(): String? {
        return try {
            var sysIP: String?
            val osName = System.getProperty("os.name")
            when (osName) {
                "Windows" -> {
                    sysIP = InetAddress.getLocalHost().hostAddress
                }

                "Mac OS X" -> {
                    sysIP = getSystemIP4("en0")
                }

                else -> {
                    sysIP = getSystemIP4("eth0")
                    if (sysIP == null) {
                        sysIP = getSystemIP4("eth1")
                        if (sysIP == null) {
                            sysIP = getSystemIP4("eth2")
                            if (sysIP == null) {
                                sysIP = getSystemIP4("usb0")
                            }
                        }
                    }
                }
            }
            sysIP
        } catch (E: Exception) {
            System.err.println("System IP Exp : " + E.message)
            null
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
            System.err.println("System Linux IP Exp : " + E.message)
            null
        }
    }
}