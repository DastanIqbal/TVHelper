package com.dastanapps.poweroff.ui.noserver

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme
import com.dastanapps.poweroff.wifi.Constants
import com.dastanapps.poweroff.wifi.MainApp
import com.dastanapps.poweroff.wifi.data.SAVED_IP
import com.dastanapps.poweroff.wifi.net.scanPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 3:56 PM
 *
 */

class NoServerScreen : ComponentActivity() {

    private val connectionStatus = mutableStateOf(false)
    private val progressStatus = mutableStateOf(false)
    private val savedServerIp by lazy {
        MainApp.INSTANCE.dataStoreManager.readString(SAVED_IP)
    }

    private val serverState by lazy {
        ServerFoundState(
            servers = mutableStateOf(
                arrayListOf()
            ),
            connectionStatus = connectionStatus,
            connect = { ip ->
                MainApp.INSTANCE.connectionManager.connect(ip) {
                    connectionStatus.value = it
                    if (it) {
                        MainApp.INSTANCE.dataStoreManager.writeString(SAVED_IP, ip)
                        Handler(Looper.getMainLooper())
                            .postDelayed({
                                if (!isFinishing) {
                                    finish()
                                }
                            }, 1500)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTVAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(), color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxSize()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val savedServerIp =
                            savedServerIp.collectAsState(initial = "") as MutableState<String>
                        Header(
                            NoServerState(
                                savedServerIpState = savedServerIp,
                                connectionStatus = connectionStatus,
                                progressStatus = progressStatus,
                                scan = {
                                    this@NoServerScreen.lifecycleScope.launch(Dispatchers.IO) {
                                        progressStatus.value = true
                                        val list = scanPort("192.168.1.", Constants.SERVER_PORT)
                                        serverState.servers.value =
                                            list.map { ServerAddress(it.first, it.second, false) }
                                                .toMutableList() as ArrayList<ServerAddress>
                                        progressStatus.value = false
                                    }
                                })
                        )
                        if (progressStatus.value) {
                            Progress()
                        }
                        addSavedIp(savedServerIp)
                        Servers(serverState)
                    }
                }
            }
        }
    }

    private fun addSavedIp(savedServerIp: MutableState<String>) {
        if (savedServerIp.value.isNotEmpty()) {
            serverState.servers.value = arrayListOf(
                ServerAddress(
                    savedServerIp.value,
                    Constants.SERVER_PORT,
                    true
                )
            )
        }
    }
}

@Composable
fun Progress() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(top = 16.dp), color = Color.Red, strokeWidth = 4.dp
        )
    }
}

@Composable
fun Servers(serverState: ServerFoundState) {
    LazyColumn(content = {
        items(serverState.servers.value) {
            ServerItem(pair = it) {
                serverState.connect(it)
            }
        }
    })
}

@Composable
fun ServerItem(pair: ServerAddress, connect: (ip: String) -> Unit) {
    val buttonState = remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =
            if (pair.isSaved) {
                "Saved ${pair.ip}"
            } else {
                "Found ${pair.ip}"
            }
        )
        Button(enabled = buttonState.value, onClick = {
            buttonState.value = false
            connect(pair.ip)
        }) {
            Text(text = "Connect")
        }
    }
}

@Composable
fun Header(state: NoServerState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.connectionStatus.value) {
            Text(
                text = "Server Connected", color = Color.Green
            )
        } else {
            Text(
                text =
                if (state.savedServerIpState.value.isNotEmpty()) {
                    "Server Address Found"
                } else if (!state.progressStatus.value || state.savedServerIpState.value.isEmpty()) {
                    "No Server address saved"
                } else "Searching Servers ..."
            )
            Button(enabled = !state.progressStatus.value, onClick = {
                state.scan()
            }) {
                Text(text = "Scan")
            }
        }
    }
}

@Preview
@Composable
fun HeaderPreview() {
    Header(state = NoServerState(scan = { }))
}
