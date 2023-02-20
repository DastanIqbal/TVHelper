package com.dastanapps.poweroff.wifi.contracts.impl

import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.wifi.contracts.IDpadListener
import com.dastanapps.poweroff.wifi.net.ConnectionDataStream

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 1:02 PM
 *
 */

class DpadListenerImpl(
    private val dataStream: ConnectionDataStream
) : IDpadListener {
    override fun center(event: RemoteEvent) {
        dataStream.sendType(RemoteEvent.DPAD_CENTER.name)
    }

    override fun left(event: RemoteEvent) {
        dataStream.sendType(RemoteEvent.DPAD_LEFT.name)
    }

    override fun right(event: RemoteEvent) {
        dataStream.sendType(RemoteEvent.DPAD_RIGHT.name)
    }

    override fun top(event: RemoteEvent) {
        dataStream.sendType(RemoteEvent.DPAD_TOP.name)
    }

    override fun bottom(event: RemoteEvent) {
        dataStream.sendType(RemoteEvent.DPAD_BOTTOM.name)
    }
}