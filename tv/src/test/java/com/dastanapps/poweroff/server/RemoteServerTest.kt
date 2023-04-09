package com.dastanapps.poweroff.server

import com.dastanapps.poweroff.common.RemoteEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


@OptIn(ExperimentalCoroutinesApi::class)
class RemoteServerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val remoteServer = RemoteServer()

    @Before
    fun setup() {
        val serverThread = Thread {
            remoteServer.start()
        }
        serverThread.start();
        while (!RemoteServer.IS_SERVER_RUNNING) {
            Thread.sleep(1000);
        }
    }

    @Test
    fun pingPong() = runTest {
        try {

            if (!RemoteServer.IS_SERVER_RUNNING) throw Exception("Server not Running")

            withContext(Dispatchers.IO) {
                val socket = Socket("127.0.0.1", RemoteServer.PORT)
                println("Connected to server at " + socket.inetAddress.hostAddress + ":" + socket.port)
                val `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
                val out = PrintWriter(socket.getOutputStream(), true)

                // Send a JSON request to the server
                val request = "{\"type\":\"${RemoteEvent.PING.name}\"}"
                out.println(request)
                println("Sent request: $request")

                // Receive a response from the server
                val response = `in`.readLine()
                println("Received response: $response")

                Assert.assertEquals("{\"type\":\"${RemoteEvent.PONG.name}\"}", response)
                // Clean up
                `in`.close()
                out.close()
                socket.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}