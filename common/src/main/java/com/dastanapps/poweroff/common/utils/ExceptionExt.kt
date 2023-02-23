package com.dastanapps.poweroff.common.utils


/**
 *
 * Created by Iqbal Ahmed on 23/02/2023 9:08 PM
 *
 */

fun tryCatchIgnore(func: () -> Unit) {
    try {
        func()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun tryCatch(func: () -> Unit, catch: ((Exception) -> Unit)) {
    try {
        func()
    } catch (e: Exception) {
        e.printStackTrace()
        catch.invoke(e)
    }
}