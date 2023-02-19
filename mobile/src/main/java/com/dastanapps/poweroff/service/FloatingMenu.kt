package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.AudioManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import com.dastanapps.poweroff.R

class FloatingMenu(
    private val service: TVHelperService
) {

    private val context get() = service

    private var mLayout: FrameLayout? = null

    fun onServiceConnected() {
        // Create an overlay and display the action bar
        val wm = context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager

        mLayout = FrameLayout(context)

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.RIGHT
        }

        LayoutInflater.from(context)
            .inflate(R.layout.activity_main, mLayout)

        wm.addView(mLayout, lp)

        configurePowerButton();
        configureHomeButton();
        configureRecentButton();
        configureVolumeButton();
        configureScrollButton();
        configureSwipeButton();

        TVHelperService.IS_RUNNING = true
    }

    private fun configureRecentButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.recent)
        powerButton?.setOnClickListener { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) }
    }

    private fun configureHomeButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.home)
        powerButton?.setOnClickListener { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
    }

    private fun configurePowerButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.power)
        powerButton?.setOnClickListener { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) }
    }

    private fun configureVolumeButton() {
        val volumeUpButton = mLayout?.findViewById<View>(R.id.volume_up)
        volumeUpButton?.setOnClickListener {
            val audioManager =
                context.getSystemService(AccessibilityService.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout?.findViewById<View>(R.id.scroll)
        scrollButton?.setOnClickListener {
            val scrollable = findScrollableNode(service.rootInActiveWindow)
            scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun configureSwipeButton() {
        val swipeButton = mLayout?.findViewById<View>(R.id.swipe)
        swipeButton?.setOnClickListener {
            val swipePath = Path()
            swipePath.moveTo(1000F, 1000F)
            swipePath.lineTo(100F, 1000F)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 500))
            service.dispatchGesture(gestureBuilder.build(), null, null)
        }
    }
}