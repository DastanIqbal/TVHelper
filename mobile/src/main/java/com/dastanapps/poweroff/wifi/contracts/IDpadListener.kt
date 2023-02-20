package com.dastanapps.poweroff.wifi.contracts

import com.dastanapps.poweroff.common.RemoteEvent

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:01 PM
 *
 */
interface IDpadListener {
    fun center(event: RemoteEvent)
    fun left(event: RemoteEvent)
    fun right(event: RemoteEvent)
    fun top(event: RemoteEvent)
    fun bottom(event: RemoteEvent)
}