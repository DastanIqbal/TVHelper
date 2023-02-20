package com.dastanapps.poweroff.wifi.contracts.impl

import com.dastanapps.poweroff.wifi.net.ConnectionDataStream
import com.dastanapps.poweroff.wifi.dpad.DPadEvent
import com.dastanapps.poweroff.wifi.contracts.IDpadListener

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:02 PM
 *
 */

class DpadListenerImpl(
    private val dataStream: ConnectionDataStream
) : IDpadListener {
    override fun center(event: DPadEvent) {
        dataStream.sendType(DPadEvent.CENTER.name)
    }

    override fun left(event: DPadEvent) {
        dataStream.sendType(DPadEvent.LEFT.name)
    }

    override fun right(event: DPadEvent) {
        dataStream.sendType(DPadEvent.RIGHT.name)
    }

    override fun top(event: DPadEvent) {
        dataStream.sendType(DPadEvent.TOP.name)
    }

    override fun bottom(event: DPadEvent) {
        dataStream.sendType(DPadEvent.BOTTOM.name)
    }
}