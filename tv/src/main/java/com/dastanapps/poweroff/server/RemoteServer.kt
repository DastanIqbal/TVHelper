package com.dastanapps.poweroff.server

import com.dastanapps.poweroff.utils.NetworkUtils.getSystemIP
import org.json.JSONObject
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:55 PM
 *
 */

fun main(args: Array<String>) {
    val remoteServer = RemoteServer()
    remoteServer.start()
}

class RemoteServer {

    private var isRunning = false
    private var thread: Thread? = null

    var mouseCursor: ((x: Double, y: Double) -> Unit)? = null
    var tapOn: ((x: Double, y: Double) -> Unit)? = null

    fun start() {
        isRunning = true
        server()
        thread?.start()
    }

    fun stop() {
        isRunning = false
        thread?.interrupt()
    }

    private fun server() {
        val port = 8585 // Change this to your desired port number

        val serverSocket = ServerSocketChannel.open()
        serverSocket.socket().bind(InetSocketAddress(port))
        serverSocket.configureBlocking(false)

        println("Server started on IPv4 ${getSystemIP()} Port $port")

        val selector = Selector.open()
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)

        thread = Thread {
            while (isRunning) {
                selector.select()
                val selectedKeys = selector.selectedKeys().iterator()

                while (selectedKeys.hasNext()) {
                    val key = selectedKeys.next()
                    selectedKeys.remove()

                    if (!key.isValid) {
                        continue
                    }

                    if (key.isAcceptable) {
                        acceptConnection(key)
                    }

                    if (key.isReadable) {
                        readData(key)
                    }
                }
            }
            // Clean up resources before exiting
            serverSocket?.close()
            selector?.close()
        }
    }

    private fun acceptConnection(key: SelectionKey) {
        val serverSocket = key.channel() as ServerSocketChannel
        val socketChannel = serverSocket.accept()
        socketChannel.configureBlocking(false)

        println("Accepted connection from ${socketChannel.remoteAddress}")

        socketChannel.register(key.selector(), SelectionKey.OP_READ)
    }

    private fun readData(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val buffer = ByteBuffer.allocate(1024)
        val bytesRead = socketChannel.read(buffer)
        val data = buffer.array().sliceArray(0 until bytesRead)
        try {
            val json = JSONObject(String(data))

            if (bytesRead == -1 || json.get("type") == "stop") {
                println("Connection closed by ${socketChannel.remoteAddress}")
                socketChannel.close()
                key.cancel()
                return
            }

            println("Received data: ${String(data)}")

            process(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun process(json: JSONObject) {
        val type = json.get("type")
        if (type == "mouse") {
            val x = json.getDouble("x")
            val y = json.getDouble("y")
            mouseCursor?.invoke(x, y)
        } else if (type == "single_tap") {
            val x = json.getDouble("x")
            val y = json.getDouble("y")
            tapOn?.invoke(x, y)
        }
    }

}