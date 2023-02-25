package com.dastanapps.poweroff

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dastanapps.poweroff.service.SharedChannel
import com.dastanapps.poweroff.service.TVHelperService
import com.dastanapps.poweroff.ui.main.HelperScreen
import com.dastanapps.poweroff.ui.main.models.HelperState
import com.dastanapps.poweroff.ui.main.models.ServerState
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */

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
                TVHelperService.INSTANCE?.startServer(it)
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

        MainApp.mainScope.launch {
            SharedChannel.serverRunningState.receiveAsFlow().collect {
                this@MainActivity.serverState.runningState.value = it
            }
        }
        MainApp.mainScope.launch {
            SharedChannel.serviceRunningState.receiveAsFlow().collect {
                this@MainActivity.state.runningState.value = it
            }
        }
    }
}