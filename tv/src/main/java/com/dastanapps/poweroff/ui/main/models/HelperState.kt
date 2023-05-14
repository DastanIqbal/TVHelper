package com.dastanapps.poweroff.ui.main.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 4:37 PM
 *
 */

data class HelperState(
    val runningState: MutableState<Boolean> = mutableStateOf(false),
    val openService: () -> Unit
)

data class ServerState(
    val runningState: MutableState<Boolean> = mutableStateOf(false),
    val startServer: (start: Boolean) -> Unit
)

data class CursorState(
    val changeClickState: MutableState<Boolean> = mutableStateOf(false),
    val cursorsList: MutableState<List<Int>> = mutableStateOf(emptyList())
)