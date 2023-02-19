package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.MutableState


class TVHelperService : AccessibilityService() {

    private val floatingMenu by lazy {
        FloatingMenu(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        floatingMenu.onServiceConnected()
        IS_RUNNING = true
    }

    override fun onDestroy() {
        super.onDestroy()
        IS_RUNNING = false
    }

    companion object {
        var IS_RUNNING = false
    }
}