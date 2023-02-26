package com.dastanapps.poweroff.ui.noserver

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.dastanapps.poweroff.common.utils.findNetworkInfo
import com.dastanapps.poweroff.common.utils.toast
import com.dastanapps.poweroff.ui.UIState
import com.dastanapps.poweroff.ui.theme.AndroidTVAppsTheme
import com.dastanapps.poweroff.ui.theme.TextFieldIpv4
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
    private val serverList = mutableStateOf(arrayListOf<ServerAddress>())
    private val savedServerIp by lazy {
        MainApp.INSTANCE.dataStoreManager.readString(SAVED_IP)
    }
    private val ipAddressPrefix by lazy { findNetworkInfo(this).ipAddressPrefix }
    private val dataStream get() = MainApp.INSTANCE.connectionManager.dataStream

    private val serverState by lazy {
        ServerFoundState(servers = serverList,
            connectionStatus = connectionStatus,
            connect = { serverAddress, buttonState ->
                if (buttonState.value == UIState.SUCCESS) {
                    MainApp.INSTANCE.connectionManager.dataStream.destroy()
                    buttonState.value = UIState.IDLE
                } else {
                    buttonState.value = UIState.LOADING
                    MainApp.INSTANCE.connectionManager.connect(serverAddress.ip) {
                        connectionStatus.value = it
                        if (it) {
                            buttonState.value = UIState.SUCCESS
                            MainApp.INSTANCE.dataStoreManager.writeString(
                                SAVED_IP, serverAddress.ip
                            )
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (!isFinishing) {
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }, 1000)
                        } else {
                            buttonState.value = UIState.ERROR
                            Toast.makeText(this, "Connection Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionStatus.value = dataStream.isConnected
        setContent {
            AndroidTVAppsTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(), color = Color.White
                ) {
                    val savedServerIp =
                        savedServerIp.collectAsState(initial = "") as MutableState<String>

                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxSize()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ConnectServerManually(serverState)
                        ScanServer(
                            NoServerState(savedServerIpState = savedServerIp,
                                connectionStatus = connectionStatus,
                                progressStatus = progressStatus,
                                scan = {
                                    this@NoServerScreen.lifecycleScope.launch(Dispatchers.IO) {
                                        progressStatus.value = true
                                        val list = scanPort(ipAddressPrefix, Constants.SERVER_PORT)
                                        serverState.servers.value =
                                            list.map { ServerAddress(it.first, it.second, false) }
                                                .toMutableList() as ArrayList<ServerAddress>
                                        progressStatus.value = false
                                    }
                                })
                        )
                        SavedServer(serverState, savedServerIp)
                        if (progressStatus.value) {
                            Progress()
                        }
                        Servers(serverState, savedServerIp)
                    }
                }
            }
        }
    }
}

@Composable
fun ScanServer(
    state: NoServerState
) {
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
                text = if (state.savedServerIpState.value.isNotEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectServerManually(
    serverState: ServerFoundState
) {
    val buttonState = remember {
        mutableStateOf(
            if (serverState.connectionStatus.value) UIState.SUCCESS
            else UIState.IDLE
        )
    }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ipv4 = remember { mutableStateOf("") }
        TextFieldIpv4(
            ipv4 = ipv4,
            modifier = Modifier
                .widthIn(220.dp, Dp.Infinity)
                .height(50.dp),
            textAlign = TextAlign.Start,
            label = {
                Text(text = "Enter TV IP 0.0.0.0", fontSize = 12.sp)
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Button(enabled = buttonState.value !in arrayOf(UIState.LOADING), onClick = {
            if (Patterns.IP_ADDRESS.matcher(ipv4.value).matches()) {
                serverState.connect(
                    ServerAddress(ipv4.value, Constants.SERVER_PORT), buttonState
                )
            } else {
                context.toast("Invalid IP: ${ipv4.value}")
            }
        }) {
            Text(
                text = if (buttonState.value == UIState.SUCCESS) {
                    "Disconnect"
                } else {
                    "Connect"
                }
            )
        }
    }
}

@Composable
fun SavedServer(serverState: ServerFoundState, savedServerIp: MutableState<String>) {
    if (savedServerIp.value.isNotEmpty()) {
        val serverAddress = ServerAddress(
            savedServerIp.value, Constants.SERVER_PORT, true
        )
        ServerItem(pair = serverAddress, serverState) { item, uiState ->
            serverState.connect(item, uiState)
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
fun Servers(serverState: ServerFoundState, savedServerIp: MutableState<String>) {
    LazyColumn(content = {
        itemsIndexed(serverState.servers.value) { index, it ->
            if (savedServerIp.value != it.ip) {
                ServerItem(pair = it, serverState) { item, uiState ->
                    serverState.connect(item, uiState)
                }
            }
        }
    })
}

@Composable
fun ServerItem(
    pair: ServerAddress,
    serverState: ServerFoundState,
    connect: (ip: ServerAddress, buttonState: MutableState<UIState>) -> Unit
) {
    val buttonState = remember {
        mutableStateOf(
            if (serverState.connectionStatus.value) UIState.SUCCESS
            else UIState.IDLE
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (pair.isSaved) {
                "Saved ${pair.ip}"
            } else {
                "Found ${pair.ip}"
            }
        )
        Button(enabled = buttonState.value !in arrayOf(UIState.LOADING), onClick = {
            connect(pair, buttonState)
        }) {
            Text(
                text = if (buttonState.value == UIState.SUCCESS) {
                    "Disconnect"
                } else {
                    "Connect"
                }
            )
        }
    }
}

@Preview
@Composable
fun HeaderPreview() {
    ScanServer(state = NoServerState(scan = { }))
}

@Preview
@Composable
fun ConnectServerManuallyPreview() {
    ConnectServerManually(serverState = ServerFoundState(servers = remember {
        mutableStateOf(
            arrayListOf()
        )
    }) { _, _ -> })
}
