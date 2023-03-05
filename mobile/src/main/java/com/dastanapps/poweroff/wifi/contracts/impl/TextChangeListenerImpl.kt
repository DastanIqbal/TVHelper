package com.dastanapps.poweroff.wifi.contracts.impl

import android.text.TextWatcher
import android.widget.TextView
import androidx.core.widget.doOnTextChanged

/**
 *
 * Created by Iqbal Ahmed on 05/03/2023 11:10 PM
 *
 */

inline fun TextView.onTextChanged(
    crossinline textChanged: (s: CharSequence?) -> Unit
): TextWatcher {
    return doOnTextChanged { text, start, before, count ->
        textChanged.invoke(text)
    }
}