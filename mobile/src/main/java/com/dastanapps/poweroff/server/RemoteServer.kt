package com.dastanapps.poweroff.server

import com.dastanapps.poweroff.utils.NetworkUtils.getSystemIP
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

    fun start() {
        isRunning = true
        server()
    }

    fun stop() {
        isRunning = false
    }

    private fun server() {
        val port = 8585 // Change this to your desired port number

        val serverSocket = ServerSocketChannel.open()
        serverSocket.socket().bind(InetSocketAddress(port))
        serverSocket.configureBlocking(false)

        println("Server started on IPv4 ${getSystemIP()} Port $port")

        val selector = Selector.open()
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)

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

        if (bytesRead == -1 || String(data) == "stop\n") {
            println("Connection closed by ${socketChannel.remoteAddress}")
            socketChannel.close()
            key.cancel()
            return
        }

        println("Received data: ${String(data)}")
    }

}