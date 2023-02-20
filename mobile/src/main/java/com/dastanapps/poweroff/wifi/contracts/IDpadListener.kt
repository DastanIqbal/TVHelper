package com.dastanapps.poweroff.wifi.contracts

import com.dastanapps.poweroff.wifi.dpad.DPadEvent

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:01 PM
 *
 */
interface IDpadListener {
    fun center(event: DPadEvent)
    fun left(event: DPadEvent)
    fun right(event: DPadEvent)
    fun top(event: DPadEvent)
    fun bottom(event: DPadEvent)
}