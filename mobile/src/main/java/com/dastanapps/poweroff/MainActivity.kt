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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme


data class HelperState(
    val openService: () -> Unit
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    HelperScreen(state)
                }
            }
        }
    }
}

@Composable
fun HelperScreen(state: HelperState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Enable Accessibility Service to access \nTV Helper in Installed Services",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        if (TVHelperService.IS_RUNNING) {
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
                    state.openService()
                }
            ) {
                Text(text = "Open Accessibility")
            }
        }
    }
}

@Preview
@Composable
fun HelperScreenPreview() {
    HelperScreen(HelperState(openService = {}))
}
