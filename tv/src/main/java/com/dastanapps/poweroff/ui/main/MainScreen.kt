package com.dastanapps.poweroff.ui.main

import android.accessibilityservice.AccessibilityService
import android.media.AudioManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.common.utils.deviceIP
import com.dastanapps.poweroff.service.TVHelperService
import com.dastanapps.poweroff.ui.main.models.CursorState
import com.dastanapps.poweroff.ui.main.models.HelperState
import com.dastanapps.poweroff.ui.main.models.ServerState
import com.dastanapps.poweroff.ui.theme.Purple80

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 4:38 PM
 *
 */


@Composable
fun HelperScreen(
    state: HelperState,
    serverState: ServerState,
    cursorState: CursorState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(state = ScrollState(0)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        TopMenu()
        AccessibilityScreen(state = state)
        Divider(thickness = 0.5.dp)
        ServerScreen(state = serverState)
        Divider(thickness = 0.5.dp)
        CursorSection(state = cursorState)
    }
}

@Preview
@Composable
fun TopMenu() {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = {
            TVHelperService.INSTANCE?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_power_settings_new_24),
                contentDescription = "Power"
            )
        }

        IconButton(onClick = {
            TVHelperService.INSTANCE?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.outline_home_24),
                contentDescription = "Home"
            )
        }

        IconButton(onClick = {
            TVHelperService.INSTANCE?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.outline_history_24),
                contentDescription = "Recent"
            )
        }

        IconButton(onClick = {
            val audioManager =
                context.getSystemService(AccessibilityService.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }) {
            Icon(
                painter = painterResource(id = R.drawable.outline_volume_up_24),
                contentDescription = "Volume"
            )
        }
    }
}

@Composable
fun AccessibilityScreen(state: HelperState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enable Accessibility Service to access \nTV Helper in Installed Services",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.runningState.value) {
            Text(
                text = "Service is Running",
                color = Color.Green,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "To Stop, Turn off TV Helper in Installed Services",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Service Not Running",
                    color = Color.Red,
                    modifier = Modifier.padding(end = 16.dp)
                )

                ElevatedButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .height(32.dp),
                    onClick = {
                        state.openService()
                    }
                ) {
                    Text(
                        text = "Open",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ServerScreen(state: ServerState) {
    val context = LocalContext.current
    val currentIpAddress by lazy {
        deviceIP().ifEmpty { "-.-.-.-" }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "To control device, Please use this IP Address in Client Device",
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = currentIpAddress,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (state.runningState.value) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Server is Running",
                    color = Color.Green,
                    modifier = Modifier.padding(end = 16.dp)
                )

                ElevatedButton(
                    modifier = Modifier.height(32.dp),
                    onClick = {
                        state.startServer(false)
                    }
                ) {
                    Text(
                        text = "Stop",
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Server Not Running",
                    color = Color.Red,
                    modifier = Modifier.padding(end = 16.dp)
                )

                ElevatedButton(
                    modifier = Modifier.height(32.dp),
                    onClick = {
                        state.startServer(true)
                    }
                ) {
                    Text(
                        text = "Start",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CursorSection(state: CursorState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Selected Cursor",
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.baseline_mouse_24),
            modifier = Modifier.padding(top = 16.dp),
            contentDescription = "Current Cursor"
        )

        Column(
            modifier = Modifier
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .padding(8.dp)
        ) {
            ElevatedButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = {
                    state.changeClickState.value = !state.changeClickState.value
                }
            ) {
                Text(
                    text = "Change Cursor",
                    fontSize = 12.sp
                )
            }

            if (state.changeClickState.value) {
                Popup(
                    offset = IntOffset.Zero
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(40.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .background(Purple80)
                            .wrapContentWidth(align = Alignment.CenterHorizontally),
                    ) {
                        items(state.cursorsList.value) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_mouse_24),
                                contentDescription = "Current Cursor",
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(device = Devices.TABLET)
@Composable
fun HelperScreenPreview() {
    val runningState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }
    HelperScreen(
        HelperState(
            runningState = runningState,
            openService = {}
        ),
        ServerState(
            runningState = runningState,
            startServer = {}
        ),
        CursorState(
            cursorsList = mutableStateOf(
                arrayListOf<Int>().apply {
                    add(R.drawable.baseline_mouse_24)
                    add(R.drawable.baseline_mouse_24)
                    add(R.drawable.baseline_mouse_24)
                    add(R.drawable.baseline_mouse_24)
                }),
            changeClickState = mutableStateOf(true)
        )
    )
}
