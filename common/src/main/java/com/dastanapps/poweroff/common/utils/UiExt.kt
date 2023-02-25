package com.dastanapps.poweroff.common.utils

import android.content.Context
import android.widget.Toast

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 9:59 PM
 *
 */

fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}