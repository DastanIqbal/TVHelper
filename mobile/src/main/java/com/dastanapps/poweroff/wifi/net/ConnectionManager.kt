package com.dastanapps.poweroff.wifi.net

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 1:22 PM
 *
 */


class ConnectionManager {

    private var connectPhoneTask = ConnectPhoneTask() {
        connectionStatus?.invoke(it)
    }

    val dataStream get() = connectPhoneTask.dataStream

    var connectionStatus: ((isConnect: Boolean) -> Unit)? = null

    fun connect(ip: String, connectionStatus: ((isConnect: Boolean) -> Unit)? = null) {
        this.connectionStatus = connectionStatus
        connectPhoneTask = ConnectPhoneTask() {
            connectionStatus?.invoke(it)
        }
        connectPhoneTask.execute(ip)
    }

}