package com.dastanapps.poweroff.wifi.net

import android.util.Log
import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.common.utils.tryCatch
import com.dastanapps.poweroff.common.utils.tryCatchIgnore
import com.dastanapps.poweroff.wifi.Constants
import com.dastanapps.poweroff.wifi.MainApp
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
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
    private var reader: BufferedReader? = null
    private var messageReceived = ""

    var isConnected = false

    fun init(ip: String): Boolean {
        var result = true
        tryCatch({
            destroy()
            val serverAddr = InetAddress.getByName(ip)
            socket = Socket(serverAddr, Constants.SERVER_PORT) //Open socket on server IP and port
            out = PrintWriter(
                BufferedWriter(
                    OutputStreamWriter(
                        socket?.getOutputStream()
                    )
                ), true
            )
            reader = BufferedReader(
                InputStreamReader(
                    socket?.getInputStream()
                )
            )
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
                while (reader?.ready() == true) {
                    messageReceived = reader?.readLine() ?: ""
                }
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
                if (socket?.isClosed == true) {
                    isConnected = false
                    out = null
                }
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

    fun sendText(text: String, x: Float, y: Float) {
        sendCommands(
            JSONObject().apply {
                put("type", RemoteEvent.KEYBOARD)
                put("text", text)
                put("x", x)
                put("y", y)
            }.toString()
        )
    }

    fun cursorPosition(x: Float, y: Float) {
        sendCommands(
            JSONObject().apply {
                put("type", RemoteEvent.MOUSE.name)
                put("x", x)
                put("y", y)

            }.toString()
        )
    }

    fun singleTap(x: Float, y: Float) {
        sendCommands(
            JSONObject().apply {
                put("type", RemoteEvent.SINGLE_TAP.name)
                put("x", x)
                put("y", y)

            }.toString()
        )
    }

    fun ping(status: (isSuccess: Boolean) -> Unit) {
        MainApp.applicationIoScope.launch {
            if (isStreamConnected) {
                tryCatch({
                    sendType(RemoteEvent.PING.name)
//                    if (messageReceived.replace("\n", "") == RemoteEvent.PONG.name) {
//                        status.invoke(true)
//                    } else {
//                        status.invoke(false)
//                    }
                    status.invoke(true)
                }) {
                    status.invoke(false)
                }
            } else {
                status.invoke(false)
            }
        }
    }
}