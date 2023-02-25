package com.dastanapps.poweroff.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dastanapps.poweroff.common.utils.deviceIP
import com.dastanapps.poweroff.ui.main.models.HelperState
import com.dastanapps.poweroff.ui.main.models.ServerState

/**
 *
 * Created by Iqbal Ahmed on 25/02/2023 4:38 PM
 *
 */


@Composable
fun HelperScreen(
    state: HelperState,
    serverState: ServerState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AccessibilityScreen(state = state)
        Divider(thickness = 0.5.dp)
        ServerScreen(state = serverState)
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

@Preview
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
        )
    )
}
