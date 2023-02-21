package com.dastanapps.poweroff.ui.noserver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 12:52 PM
 *
 */

data class NoServerState(
    val savedServerIpState: MutableState<String> = mutableStateOf(""),
    val connectionStatus: MutableState<Boolean> = mutableStateOf(false),
    val progressStatus: MutableState<Boolean> = mutableStateOf(false),
    val scan: () -> Unit
)

data class ServerAddress(val ip: String, val port: Int, val isSaved: Boolean = false)

data class ServerFoundState(
    val servers: MutableState<ArrayList<ServerAddress>>,
    val connectionStatus: MutableState<Boolean> = mutableStateOf(false),
    val connect: (ip: String) -> Unit
)