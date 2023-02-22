package com.dastanapps.poweroff.ui.nointernet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme

/**
 *
 * Created by Iqbal Ahmed on 22/02/2023 4:30 PM
 *
 */


@Composable
fun NoWifiDialog(
    dismiss: () -> Unit
) {
    val openDialog = remember { mutableStateOf(true) }

    if (!openDialog.value) return

    AndroidTVAppsTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(), color = Color.White
        ) {
            AlertDialog(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                title = {
                    Text(
                        text = "Need Wifi Connection",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "To user TV Helper Mobile, You have to connect to Wifi",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                    )
                },
                onDismissRequest = {
                    openDialog.value = false
                },
                confirmButton = {
                    Button(onClick = {
                        openDialog.value = false
                        dismiss.invoke()
                    }) {
                        Text(text = "Ok")
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color.Black,
                textContentColor = Color.Black,
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    securePolicy = SecureFlagPolicy.Inherit
                )
            )
        }
    }
}

@Preview
@Composable
fun NoWifiDialogPreview() {
    NoWifiDialog {

    }
}