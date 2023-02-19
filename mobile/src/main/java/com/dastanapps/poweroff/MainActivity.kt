package com.dastanapps.poweroff

import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    HelperScreen()
                }
            }
        }
    }
}

@Composable
fun HelperScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AccessibilityScreen()
        Divider(
            thickness = 1.dp
        )
    }
}

@Composable
fun AccessibilityScreen() {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.padding(top = 24.dp),
                onClick = {
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
    HelperScreen()
}
