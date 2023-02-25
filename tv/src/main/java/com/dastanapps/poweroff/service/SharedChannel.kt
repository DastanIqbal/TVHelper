package com.dastanapps.poweroff.service

import kotlinx.coroutines.channels.Channel

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 4:18 PM
 *
 */

object SharedChannel {
    val serviceRunningState = Channel<Boolean>()

    val serverRunningState = Channel<Boolean>()

    fun cancelAll(){
        serviceRunningState.cancel()
        serverRunningState.cancel()
    }
}