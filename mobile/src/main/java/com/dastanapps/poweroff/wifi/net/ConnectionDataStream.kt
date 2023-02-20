package com.dastanapps.poweroff.wifi.net

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dastanapps.poweroff.wifi.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
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
    val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = ConnectPhoneTask::class.java.simpleName

    private var socket: Socket? = null
    private var out: PrintWriter? = null

    var isConnected = false

    fun init(ip: String): Boolean {
        var result = true
        try {
            val serverAddr = InetAddress.getByName(ip)
            socket =
                Socket(serverAddr, Constants.SERVER_PORT) //Open socket on server IP and port
        } catch (e: IOException) {
            Log.e(TAG, "Error while connecting", e)
            result = false
        }
        return result
    }

    fun prepare(isInit: Boolean) {
        isConnected = isInit
        Toast.makeText(
            context,
            if (isConnected) "Connected to server!" else "Error while connecting",
            Toast.LENGTH_LONG
        ).show()
        try {
            if (isConnected) {
                out = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(
                            socket?.getOutputStream()
                        )
                    ), true
                ) //create output stream to send data to server
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error while creating OutWriter", e)
            Toast.makeText(context, "Error while connecting", Toast.LENGTH_LONG)
                .show()
        }
    }

    val isStreamConnected get() = isConnected && out != null

    fun destroy() {
        if (isStreamConnected) {
            try {
                socket?.close() //close socket
            } catch (e: IOException) {
                Log.e(TAG, "Error in closing socket", e)
            }
        }
    }

    fun sendCommands(cmd: String) {
        if (isStreamConnected) {
            scope.launch(Dispatchers.IO) {
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