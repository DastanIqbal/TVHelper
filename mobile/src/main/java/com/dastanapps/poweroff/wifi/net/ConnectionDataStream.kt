package com.dastanapps.poweroff.wifi.net

import android.util.Log
import com.dastanapps.poweroff.common.utils.tryCatch
import com.dastanapps.poweroff.common.utils.tryCatchIgnore
import com.dastanapps.poweroff.wifi.Constants
import com.dastanapps.poweroff.wifi.MainApp
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:04 PM
 *
 */
class ConnectionDataStream(
    private val status: (isConnect: Boolean) -> Unit
) {
    private val TAG = ConnectPhoneTask::class.java.simpleName

    var socket: Socket? = null
    private var out: PrintWriter? = null

    var isConnected = false

    fun init(ip: String): Boolean {
        var result = true
        tryCatch({
            destroy()
            val serverAddr = InetAddress.getByName(ip)
            socket = Socket(serverAddr, Constants.SERVER_PORT) //Open socket on server IP and port
        }) {
            Log.e(TAG, "Error while connecting", it)
            result = false
        }
        return result
    }

    fun prepare(isInit: Boolean) {
        isConnected = isInit
        tryCatch({
            if (isConnected) {
                out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(
                            socket?.getOutputStream()
                        )
                    ), true
                )
                status.invoke(true)
            } else {
                status.invoke(false)
            }
        }) {
            Log.e(TAG, "Error while creating OutWriter", it)
            status.invoke(false)
        }
    }

    val connectedHost = socket?.inetAddress?.hostAddress ?: ""

    val isStreamConnected get() = isConnected && out != null

    fun destroy() {
        if (isStreamConnected) {
            tryCatchIgnore {
                socket?.close() //close socket
            }
        }
    }

    fun sendCommands(cmd: String) {
        MainApp.applicationIoScope.launch {
            if (isStreamConnected) {
                out?.println(cmd)
            }
        }
    }

    fun sendType(type: String) {
        sendCommands(
            JSONObject().apply {
                put("type", type)
            }.toString()
        )
    }
}