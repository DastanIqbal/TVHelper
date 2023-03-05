package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.dastanapps.poweroff.MainApp
import com.dastanapps.poweroff.MainApp.Companion.log
import com.dastanapps.poweroff.server.RemoteServer
import kotlinx.coroutines.launch


/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */
class TVHelperService : AccessibilityService() {
    private val TAG = TVHelperService::class.java.simpleName

    private val floatingMenu by lazy {
        FloatingMenu(this)
    }

    private val remoteServer by lazy {
        RemoteServer()
    }

    private val timer by lazy { ToggleUITimer() }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        log(event.toString())
    }

    override fun onInterrupt() {
        log("onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        INSTANCE = this
        IS_SERVICE_RUNNING = true
        MainApp.mainScope.launch {
            SharedChannel.serviceRunningState.send(true)
        }

        floatingMenu.onServiceConnected()
        floatingMenu.toggleUI(false)

        remoteServer.mouseCursor = { x, y ->
            floatingMenu.moveCursor(x, y)
            floatingMenu.handler.post {
                floatingMenu.toggleUI(false)
                timer.restart()
            }
        }

        remoteServer.tapOn = { x, y ->
            floatingMenu.tapOn(x.toFloat(), y.toFloat())
        }

        remoteServer.scroll = { type ->
            floatingMenu.scroll(type)
        }

        remoteServer.typing = { text, x, y ->
            floatingMenu.typing(text, x.toFloat(), y.toFloat())
        }

        remoteServer.start()

        floatingMenu.handler.post {
            timer.start()
            timer.doOnFinish {
                floatingMenu.toggleUI(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
        IS_SERVICE_RUNNING = false
        MainApp.mainScope.launch {
            SharedChannel.serviceRunningState.send(false)
        }
        remoteServer.stop()
        log("onDestroy")
    }

    fun startServer(start: Boolean) {
        if (start) {
            remoteServer.start()
        } else {
            remoteServer.stop()
        }
    }

    companion object {
        var IS_SERVICE_RUNNING = false
        var INSTANCE: TVHelperService? = null
    }
}