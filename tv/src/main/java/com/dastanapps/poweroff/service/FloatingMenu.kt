package com.dastanapps.poweroff.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Point
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
import com.dastanapps.poweroff.MainApp.Companion.log
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.common.utils.copyText


/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */
class FloatingMenu(
    private val service: TVHelperService
) {

    private val context get() = service
    private val density by lazy { context.resources.displayMetrics.density }

    private val boundaryXRight by lazy {
        context.resources.displayMetrics.widthPixels - cursorIcon?.width!!
    }

    private val boundaryYBottom by lazy {
        context.resources.displayMetrics.heightPixels - cursorIcon?.height!!
    }

    private var mLayout: FrameLayout? = null


    private var mCursorLayout: FrameLayout? = null
    private var cursorIcon: ImageView? = null
    private val windowManager: WindowManager by lazy {
        context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    }
    private val iconSize = 30

    val handler = Handler(Looper.getMainLooper())


    fun isCursorVisible() = cursorIcon?.visibility == View.VISIBLE
    fun toggleUI(hide: Boolean) {
        handler.post {
            if (hide) {
                mLayout?.visibility = View.INVISIBLE
                cursorIcon?.visibility = View.INVISIBLE
            } else {
                mLayout?.visibility = View.VISIBLE
                cursorIcon?.visibility = View.VISIBLE
            }
        }
    }

    fun onServiceConnected() {
        mLayout = FrameLayout(context)
        cursorIcon = ImageView(context)

        cursorIcon?.layoutParams = FrameLayout.LayoutParams(
            (iconSize * density).toInt(), (iconSize * density).toInt()
        )

        cursorIcon?.setImageResource(R.drawable.baseline_mouse_24)

        mCursorLayout = FrameLayout(context).apply {
            addView(cursorIcon)
        }

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.RIGHT
        }

        LayoutInflater.from(context).inflate(R.layout.layout_floating_menu, mLayout)

        val cursorLayout = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            format = PixelFormat.TRANSLUCENT
        }


        windowManager.addView(mLayout, lp)
        windowManager.addView(mCursorLayout, cursorLayout)

        configurePowerButton()
        configureHomeButton()
        configureRecentButton()
        configureBackButton()
        configureVolumeButton()

        configureScrollButton()
        configureSwipeButton()

        TVHelperService.IS_SERVICE_RUNNING = true
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
            log("gesture cancelled");
        }

        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            log("gesture completed");
        }
    };

    fun tapOn(x: Float, y: Float) {
        cursorIcon?.run {
            val boundaryX = this.x + x + (iconSize * density) / 2.0f
            val boundaryY = this.y + y + (iconSize * density) / 2.0f

            val list = findNode(
                service,
                service.rootInActiveWindow,
                Point(boundaryX.toInt(), boundaryY.toInt())
            )

            var consumed = false
            list?.forEach loop@{
                if (it.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                    it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    consumed = true
                    return@loop
                }
            }

            if (!consumed) {
                val swipePath = Path()
                swipePath.moveTo(boundaryX, boundaryY)
                swipePath.lineTo(boundaryX, boundaryY)
                val clickBuilder = GestureDescription.Builder()
                clickBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 10))
                service.dispatchGesture(clickBuilder.build(), callback, null)
            }
        }
    }

    fun scroll(type: String) {
        when (type) {
            RemoteEvent.SCROLL_UP.name -> {
                val scrollable = findScrollableNode(
                    service.rootInActiveWindow.getChild(0),
                    AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD
                )
                scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
            }

            RemoteEvent.SCROLL_DOWN.name -> {
                val scrollable = findScrollableNode(
                    service.rootInActiveWindow.getChild(0),
                    AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD
                )
                scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id)
            }
        }
    }

    fun typing(text: String, x: Float, y: Float) {
        cursorIcon?.let { imv ->
            val editableNode = findEditable(service.rootInActiveWindow)

            editableNode?.run {
                if (isEditable) {
                    text.copyText(imv.context)
                    performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                    performAction(AccessibilityNodeInfo.ACTION_PASTE)
                }
            }
        }
    }

    private fun configureBackButton() {
        val powerButton = mLayout?.findViewById<View>(R.id.back)
        powerButton?.setOnClickListener { performAction(AccessibilityService.GLOBAL_ACTION_BACK) }
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
        val volumeUpButton = mLayout?.findViewById<View>(R.id.volume)
        volumeUpButton?.setOnClickListener {
            val audioManager =
                context.getSystemService(AccessibilityService.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout?.findViewById<View>(R.id.scroll)
        scrollButton?.setOnClickListener {
            val scrollable = findScrollableNode(
                service.rootInActiveWindow,
                AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD
            )
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