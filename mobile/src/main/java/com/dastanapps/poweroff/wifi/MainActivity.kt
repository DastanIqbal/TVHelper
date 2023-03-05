package com.dastanapps.poweroff.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.common.RemoteEvent
import com.dastanapps.poweroff.common.utils.isConnectedToWifi
import com.dastanapps.poweroff.common.utils.showKeyboard
import com.dastanapps.poweroff.common.utils.toast
import com.dastanapps.poweroff.databinding.ActivityMainBinding
import com.dastanapps.poweroff.ui.nointernet.NoWifiActivity
import com.dastanapps.poweroff.ui.noserver.NoServerScreen
import com.dastanapps.poweroff.wifi.contracts.impl.DpadListenerImpl
import com.dastanapps.poweroff.wifi.contracts.impl.OnTouchListenerImpl
import com.dastanapps.poweroff.wifi.contracts.impl.onTextChanged
import com.dastanapps.poweroff.wifi.data.SAVED_IP
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 11:15 PM
 *
 */
class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private val dataStream get() = MainApp.INSTANCE.connectionManager.dataStream

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_CANCELED) {
                if (!dataStream.isConnected) {
                    finish()
                }
            } else if (it.resultCode == Activity.RESULT_OK) {
                setUI()
            }
        }

    private lateinit var touchListenerImpl: OnTouchListenerImpl

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        if (isConnectedToWifi(this)) {
            if (!dataStream.isConnected) {
                activityLauncher.launch(Intent(this, NoServerScreen::class.java))
            }
        } else {
            startActivity(Intent(this, NoWifiActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_setting -> {
                activityLauncher.launch(Intent(this, NoServerScreen::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        verifyConnection()
    }

    private fun verifyConnection() {
        if (isConnectedToWifi(this) && dataStream.isConnected) {
            lifecycleScope.launch {
                MainApp.INSTANCE.dataStoreManager.readString(SAVED_IP).collectLatest { ip ->
                    if (ip.isNotEmpty()) {
                        MainApp.INSTANCE.connectionManager.dataStream.ping {
                            if (it) {
                                MainApp.applicationScope.launch {
                                    setUI()
                                    this@MainActivity.toast("Connected to $ip")
                                }
                            } else {
                                MainApp.INSTANCE.connectionManager.connect(ip) {
                                    MainApp.applicationScope.launch {
                                        if (it) {
                                            setUI()
                                            this@MainActivity.toast("Connected to $ip")
                                        } else {
                                            this@MainActivity.toast("Connection Error")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var textWatcher: TextWatcher? = null

    private fun setUI() {
        if (dataStream.isConnected) {
            touchListenerImpl = OnTouchListenerImpl(dataStream)

            binding.dummyEdittext.removeTextChangedListener(textWatcher)
            textWatcher = binding.dummyEdittext.onTextChanged { text ->
                text?.let {
                    if(it.isNotEmpty()) {
                        dataStream.sendText(
                            it.toString(),
                            touchListenerImpl.disX,
                            touchListenerImpl.disY
                        )
                        binding.dummyEdittext.setText("")
                    }
                }
            }

            binding.mousePad.setOnTouchListener(touchListenerImpl)
            binding.dpadView.setOnDPadListener(DpadListenerImpl(dataStream))

            binding.poweron.setOnClickListener {
                dataStream.sendType(RemoteEvent.WAKE_UP.name)
            }

            binding.keyboard.setOnClickListener {
                binding.dummyEdittext.requestFocus()
                binding.dummyEdittext.showKeyboard()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataStream.destroy()
    }
}