package com.dastanapps.poweroff.server

import com.dastanapps.poweroff.MainApp
import com.dastanapps.poweroff.MainApp.Companion.log
import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.common.utils.deviceIP
import com.dastanapps.poweroff.common.utils.toast
import com.dastanapps.poweroff.common.utils.tryCatchIgnore
import com.dastanapps.poweroff.service.SharedChannel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
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

    private var thread: Thread? = null

    var mouseCursor: ((x: Double, y: Double) -> Unit)? = null
    var tapOn: ((x: Double, y: Double) -> Unit)? = null
    var scroll: ((type: String) -> Unit)? = null

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
        MainApp.mainScope.launch {
            SharedChannel.serverRunningState.send(false)
        }
        thread?.interrupt()
    }

    var serverSocketChannel: ServerSocketChannel? = null
    var selector: Selector? = null
    private fun server() {
        val port = 8585 // Change this to your desired port number

        if (serverSocketChannel == null || serverSocketChannel?.isOpen == false)
            serverSocketChannel = ServerSocketChannel.open()

        var serverSocket = serverSocketChannel!!
        if (serverSocket.socket().isBound) {
            serverSocket.socket().close()
            serverSocketChannel = ServerSocketChannel.open()
            serverSocket = serverSocketChannel!!
        }

        serverSocket.socket().bind(InetSocketAddress(port))
        serverSocket.socket().reuseAddress = true
        serverSocket.socket().soTimeout = 0
        serverSocket.configureBlocking(false)

        log("Server started on IPv4 ${deviceIP()} Port $port")

        selector = if (selector == null || selector?.isOpen == false)
            Selector.open()
        else {
            selector?.close()
            Selector.open()
        }
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)

        val selector = selector!!
        thread = Thread {
            while (IS_SERVER_RUNNING && selector.select() > 0) {
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

        log("Accepted connection from ${socketChannel.remoteAddress}")
        MainApp.mainScope.launch {
            MainApp.INSTANCE?.toast("Client connected ${(socketChannel.remoteAddress as InetSocketAddress).address.hostAddress}")
        }

        socketChannel.register(key.selector(), SelectionKey.OP_READ)
    }

    private fun readData(key: SelectionKey) {
        var restart = true
        try {
            val socketChannel = key.channel() as SocketChannel
            val buffer = ByteBuffer.allocate(1024)
            val bytesRead = socketChannel.read(buffer)
            val data = buffer.array().sliceArray(0 until bytesRead)
            if (bytesRead == -1) {
                log("Connection closed by ${socketChannel.remoteAddress}")

                MainApp.mainScope.launch {
                    MainApp.INSTANCE?.toast("Client ${(socketChannel.remoteAddress as InetSocketAddress).address.hostAddress} disconnected")
                }

                socketChannel.close()
                key.cancel()
                return
            }

            val json = JSONObject(String(data))

            if (json.get("type") == RemoteEvent.STOP_SERVER.name) {
                restart = false
                log("Connection closed by ${socketChannel.remoteAddress} Client")
                socketChannel.close()
                key.cancel()
                return
            }

            log("Received data: ${String(data)}")

            process(json)
        } catch (e: Exception) {
            e.printStackTrace()
            tryCatchIgnore { FirebaseCrashlytics.getInstance().recordException(e) }
            if (restart)
                start()
        }
    }

    private fun process(json: JSONObject) {
        val type = json.getString("type")
        when (type) {
            RemoteEvent.MOUSE.name -> {
                val x = json.getDouble("x")
                val y = json.getDouble("y")
                mouseCursor?.invoke(x, y)
            }

            RemoteEvent.SINGLE_TAP.name -> {
                val x = json.getDouble("x")
                val y = json.getDouble("y")
                tapOn?.invoke(x, y)
            }

            RemoteEvent.SCROLL_UP.name -> {
                scroll?.invoke(type)
            }

            RemoteEvent.SCROLL_DOWN.name -> {
                scroll?.invoke(type)
            }
        }
    }

    companion object {
        var IS_SERVER_RUNNING = false
    }

}