package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.dastanapps.poweroff.server.RemoteServer


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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, event.toString())
    }

    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        floatingMenu.onServiceConnected()
        IS_RUNNING = true

        remoteServer.mouseCursor = { x, y ->
            floatingMenu.moveCursor(x, y)
        }

        remoteServer.tapOn = { x, y ->
            floatingMenu.tapOn(x.toFloat(), y.toFloat())
        }

        remoteServer.scroll = { type ->
            floatingMenu.scroll(type)
        }

        remoteServer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_RUNNING = false
        remoteServer.stop()
    }

    companion object {
        var IS_RUNNING = false
    }
}