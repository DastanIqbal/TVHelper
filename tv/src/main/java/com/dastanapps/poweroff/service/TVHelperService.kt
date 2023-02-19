package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.dastanapps.poweroff.server.RemoteServer


/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */
class TVHelperService : AccessibilityService() {

    private val floatingMenu by lazy {
        FloatingMenu(this)
    }

    private val remoteServer by lazy {
        RemoteServer()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        floatingMenu.onServiceConnected()
        IS_RUNNING = true
//        remoteServer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_RUNNING = false
    }

    companion object {
        var IS_RUNNING = false
    }
}