package com.dastanapps.poweroff

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dastanapps.poweroff.service.TVHelperService
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */
data class HelperState(
    val runningState: MutableState<Boolean> = mutableStateOf(false),
    val openService: () -> Unit
)

data class ServerState(
    val runningState: MutableState<Boolean> = mutableStateOf(false),
    val startServer: () -> Unit
)

class MainActivity : ComponentActivity() {

    private val state by lazy {
        HelperState(
            openService = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        )
    }

    private val serverState by lazy {
        ServerState(
            startServer = {
                startService(Intent(this, TVHelperService::class.java))
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    HelperScreen(state, serverState)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        state.runningState.value = TVHelperService.IS_RUNNING
    }
}

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
        Divider(
            thickness = 1.dp
        )
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
            textAlign = TextAlign.Center
        )
        if (state.runningState.value) {
            Text(
                text = "Running",
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
            Text(
                text = "Not Running",
                color = Color.Red,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(top = 24.dp),
                onClick = {
                    state.openService()
                }
            ) {
                Text(text = "Open Accessibility")
            }
        }
    }
}

@Composable
fun ServerScreen(state: ServerState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "To control device, Please use this IP Address",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (state.runningState.value) {
            Text(
                text = "Running",
                color = Color.Green,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            Text(
                text = "Not Running",
                color = Color.Red,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(top = 24.dp),
                onClick = {
                    state.startServer()
                }
            ) {
                Text(text = "Start")
            }
        }
    }
}

@Preview
@Composable
fun HelperScreenPreview() {
    HelperScreen(
        HelperState(openService = {}),
        ServerState(startServer = {})
    )
}
