package com.dastanapps.poweroff.common.utils

import android.content.Context
import android.os.PowerManager

/**
 *
 * Created by Iqbal Ahmed on 04/03/2023 9:01 PM
 *
 */


fun wakeDevice(context: Context, tag: String = "powerOff:WakeLock") {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock = powerManager.newWakeLock(
        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
        tag
    )
    wakeLock.acquire(5 * 1000L)
}