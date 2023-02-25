package com.dastanapps.poweroff.common.log

import android.util.Log

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 2:56 PM
 *
 */

object Logger {

    fun logDebug(app: String, msg: String) {
        Log.d(app, msg)
    }
}