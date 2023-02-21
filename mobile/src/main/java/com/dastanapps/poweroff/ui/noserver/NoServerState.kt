package com.dastanapps.poweroff.ui.noserver

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 12:52 PM
 *
 */

data class NoServerState(
    val connectionStatus: MutableState<Boolean> = mutableStateOf(false),
    val progressStatus: MutableState<Boolean> = mutableStateOf(false),
    val scan: () -> Unit
)

data class ServerFoundState(
    val servers: MutableState<ArrayList<Pair<String, Int>>>,
    val connectionStatus: MutableState<Boolean> = mutableStateOf(false),
    val connect: (ip: String) -> Unit
)