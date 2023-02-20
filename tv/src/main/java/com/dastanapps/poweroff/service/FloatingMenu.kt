package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.ImageView
import com.dastanapps.poweroff.R


/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */
class FloatingMenu(
    private val service: TVHelperService
) {

    private val context get() = service
    private val handler = Handler(Looper.getMainLooper())

    private val density by lazy { context.resources.displayMetrics.density }

    private val boundaryXRight by lazy {
        context.resources.displayMetrics.widthPixels - cursorIcon?.width!!
    }

    private val boundaryYBottom by lazy {
        context.resources.displayMetrics.heightPixels - cursorIcon?.height!!
    }


    private var mLayout: FrameLayout? = null
    private var cursorIcon: ImageView? = null
    private var cursorLayout: WindowManager.LayoutParams? = null
    private val windowManager: WindowManager by lazy {
        context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    }

    private val iconSize = 30


    fun onServiceConnected() {
        mLayout = FrameLayout(context)
        cursorIcon = ImageView(context)

        cursorIcon?.layoutParams = FrameLayout.LayoutParams(
            (iconSize * density).toInt(), (iconSize * density).toInt()
        )

        cursorIcon?.setImageResource(R.drawable.baseline_mouse_24)

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.RIGHT
        }

        LayoutInflater.from(context).inflate(R.layout.activity_main, mLayout)

        cursorLayout = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            format = PixelFormat.TRANSLUCENT
        }


        windowManager.addView(mLayout, lp)

        windowManager.addView(
            FrameLayout(context).apply {
                addView(cursorIcon)
            },
            cursorLayout
        )

        configurePowerButton();
        configureHomeButton();
        configureRecentButton();
        configureVolumeButton();
        configureScrollButton();
        configureSwipeButton();

        TVHelperService.IS_RUNNING = true
    }

    fun moveCursor(x: Double, y: Double) {
        handler.post {
            cursorIcon?.run {
                val boundaryX = this.x + x.toFloat()
                val boundaryY = this.y + y.toFloat()

                if (boundaryX > 0 && boundaryX < boundaryXRight)
                    translationX = boundaryX

                if (boundaryY > 0 && boundaryY < boundaryYBottom)
                    translationY = boundaryY
            }
        }
    }

    fun performAction(code: Int) {
        service.performGlobalAction(code)
    }

    private val callback = object : AccessibilityService.GestureResultCallback() {
        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            println("gesture cancelled");
        }

        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            println("gesture completed");
        }
    };

    fun tapOn(x: Float, y: Float) {
        cursorIcon?.run {
            val boundaryX = this.x + x + (iconSize * density) / 2.0f
            val boundaryY = this.y + y + (iconSize * density) + iconSize

            val swipePath = Path()
            swipePath.moveTo(boundaryX, boundaryY)
            swipePath.lineTo(boundaryX, boundaryY)
            val clickBuilder = GestureDescription.Builder()
            clickBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 10))
            service.dispatchGesture(clickBuilder.build(), callback, null)
        }
    }

    private fun configureRecentButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.recent)
        powerButton?.setOnClickListener { performAction(AccessibilityService.GLOBAL_ACTION_RECENTS) }
    }

    private fun configureHomeButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.home)
        powerButton?.setOnClickListener { performAction(AccessibilityService.GLOBAL_ACTION_HOME) }
    }

    private fun configurePowerButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.power)
        powerButton?.setOnClickListener { performAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) }
    }

    private fun configureVolumeButton() {
        val volumeUpButton = mLayout?.findViewById<View>(R.id.volume_up)
        volumeUpButton?.setOnClickListener {
            val audioManager =
                context.getSystemService(AccessibilityService.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
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
            tapOn(1000F, 1000F)
        }
    }
}