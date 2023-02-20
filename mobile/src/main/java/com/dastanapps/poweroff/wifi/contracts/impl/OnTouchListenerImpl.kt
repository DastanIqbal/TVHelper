package com.dastanapps.poweroff.wifi.contracts.impl

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.dastanapps.poweroff.wifi.Constants
import com.dastanapps.poweroff.wifi.net.ConnectionDataStream
import org.json.JSONObject
import kotlin.math.sqrt

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 12:55 PM
 *
 */
class OnTouchListenerImpl(
    private val dataStream: ConnectionDataStream,
) : View.OnTouchListener {
    private var lastX = 0f
    private var lastY = 0f

    private var rawLastX = 0f
    private var rawLastY = 0f

    private var disX = 0f
    private var disY = 0f

    private var startTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val _this = this@OnTouchListenerImpl
        return if (dataStream.isStreamConnected) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //save X and Y positions when user touches the TextView
                    lastX = event.x
                    lastY = event.y

                    rawLastX = event.rawX
                    rawLastY = event.rawY

                    startTime = System.currentTimeMillis()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    disX = event.x - lastX //Mouse movement in x direction
                    disY = event.y - lastY //Mouse movement in y direction

                    lastX = event.x
                    lastY = event.y

                    if (disX != 0f || disY != 0f) {
                        dataStream.sendCommands(
                            JSONObject().apply {
                                put("type", "mouse")
                                put("x", disX)
                                put("y", disY)

                            }.toString()
                        )
                    }
                    // Detect Tap
                    val deltaX = event.rawX - rawLastX
                    val deltaY = event.rawY - rawLastY
                    val distance = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                    if (distance > 5) {
                        startTime = 0
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val duration = System.currentTimeMillis() - startTime
                    if (duration < 200) {
                        dataStream.sendCommands(
                            JSONObject().apply {
                                put("type", Constants.SINGLE_TAP)
                                put("x", disX)
                                put("y", disY)

                            }.toString()
                        )
                    }
                    startTime = 0
                    true
                }

                else -> {
                    false
                }
            }
        } else {
            false
        }
    }
}