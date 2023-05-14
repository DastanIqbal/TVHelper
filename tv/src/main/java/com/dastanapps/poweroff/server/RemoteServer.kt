package com.dastanapps.poweroff.server

import com.dastanapps.poweroff.MainApp
import com.dastanapps.poweroff.MainApp.Companion.log
import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.common.utils.wakeDevice
import com.dastanapps.poweroff.service.SharedChannel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:55 PM
 *
 */

class RemoteServer {

    private var thread: Thread? = null
    private val gson by lazy { Gson() }
    var printOut: PrintWriter? = null
    var serverSocket: ServerSocket? = null

    var mouseCursor: ((x: Double, y: Double) -> Unit)? = null
    var tapOn: ((x: Double, y: Double) -> Unit)? = null
    var scroll: ((type: String) -> Unit)? = null
    var typing: ((text: String, x: Double, y: Double) -> Unit)? = null

    fun start() {
        IS_SERVER_RUNNING = true
        MainApp.mainScope.launch {
            SharedChannel.serverRunningState.send(true)
        }
        server()
        thread?.start()
    }

    fun stop() {
        IS_SERVER_RUNNING = false
        serverSocket?.close()
        MainApp.mainScope.launch {
            SharedChannel.serverRunningState.send(false)
        }
        thread?.interrupt()
    }

    private fun server() {
        thread = Thread() {
            try {
                serverSocket = ServerSocket(PORT)
                log("JSON Server started on port $PORT")

                while (IS_SERVER_RUNNING && serverSocket != null) {
                    val clientSocket = serverSocket!!.accept()
                    log("Client connected from " + clientSocket?.inetAddress?.hostAddress)

                    val bufIn = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
                    printOut = PrintWriter(clientSocket.getOutputStream(), true)

                    var request: String
                    while (bufIn.readLine().also { request = it } != null) {
                        log("Request received: $request")

                        // Process the request
                        val jsonElement = JsonParser.parseString(request)
                        val jsonObject = jsonElement.asJsonObject

                        // Handle the JSON request
                        log("Request Json: $jsonObject")
                        process(jsonObject)
                    }

                    // Clean up
                    bufIn.close()
                    printOut?.close()
                    clientSocket.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun process(json: JsonObject) {
        val type = json.get("type").asString
        log("Request Json Type: $type")
        when (type) {
            RemoteEvent.MOUSE.name -> {
                val x = json.get("x").asDouble
                val y = json.get("y").asDouble
                mouseCursor?.invoke(x, y)
            }

            RemoteEvent.SINGLE_TAP.name -> {
                val x = json.get("x").asDouble
                val y = json.get("y").asDouble
                tapOn?.invoke(x, y)
            }

            RemoteEvent.SCROLL_UP.name -> {
                scroll?.invoke(type)
            }

            RemoteEvent.SCROLL_DOWN.name -> {
                scroll?.invoke(type)
            }

            RemoteEvent.WAKE_UP.name -> {
                MainApp.INSTANCE?.applicationContext?.let {
                    wakeDevice(context = it)
                }
            }

            RemoteEvent.KEYBOARD.name -> {
                val text = json.get("text").asString
                val x = json.get("x").asDouble
                val y = json.get("y").asDouble
                typing?.invoke(text, x, y)
            }

            RemoteEvent.PING.name -> {
                val responseJson = JsonObject()
                responseJson.addProperty("type", RemoteEvent.PONG.name)
                log("Response Json Type: $responseJson")
                sendMessage(responseJson)
            }
        }
    }

    fun sendMessage(responseJson: JsonObject) {
        val response = gson.toJson(responseJson)
        printOut?.println(response)
        printOut?.println("\n\r")
    }

    companion object {
        var IS_SERVER_RUNNING = false
        val PORT = 8585
    }

}