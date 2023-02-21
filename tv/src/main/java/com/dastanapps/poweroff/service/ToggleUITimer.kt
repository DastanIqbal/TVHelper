package com.dastanapps.poweroff.service

import android.os.CountDownTimer
import android.util.Log

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 9:49 PM
 *
 */

class ToggleUITimer {
    private val TAG = this::class.java.simpleName

    var finishTimerMillis = 10_000L

    private var countDownTimer: CountDownTimer? = null
    internal var onFinish: (() -> Unit)? = null
    internal var onTick: ((millisUntilFinished: Long) -> Unit)? = null

    fun start() {
        countDownTimer = object : CountDownTimer(finishTimerMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000)
                onTick?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                Log.d(TAG, "Finish")
                onFinish?.invoke()
            }
        }
        countDownTimer?.start()
    }

    private fun cancel() {
        countDownTimer?.cancel()
    }

    fun restart() {
        cancel()
        finishTimerMillis = 10_000L
        start()
    }
}

fun ToggleUITimer.doOnFinish(function: () -> Unit) {
    this.onFinish = function
}

fun ToggleUITimer.doOnTick(function: (until: Long) -> Unit) {
    this.onTick = function
}