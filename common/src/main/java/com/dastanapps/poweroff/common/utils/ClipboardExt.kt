package com.dastanapps.poweroff.common.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 *
 * Created by Iqbal Ahmed on 05/03/2023 10:51 PM
 *
 */


fun String.copyText(context: Context) {
    try {
        val text = this
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copy text", text)
        clipboard.setPrimaryClip(clipData)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}