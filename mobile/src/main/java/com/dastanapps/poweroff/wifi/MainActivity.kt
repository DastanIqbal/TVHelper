package com.dastanapps.poweroff.wifi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.databinding.ActivityMainBinding
import com.dastanapps.poweroff.wifi.contracts.impl.DpadListenerImpl
import com.dastanapps.poweroff.wifi.contracts.impl.OnTouchListenerImpl
import com.dastanapps.poweroff.wifi.net.ConnectPhoneTask

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 11:15 PM
 *
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val connectPhoneTask by lazy {
        ConnectPhoneTask(this@MainActivity)
    }

    private val dataStream by lazy {
        connectPhoneTask.dataStream
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.mousePad.setOnTouchListener(OnTouchListenerImpl(dataStream))
        binding.dpadView.setOnDPadListener(DpadListenerImpl(dataStream))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_connect) {
            if (dataStream.isStreamConnected) {
                dataStream.sendType(Constants.STOP)
                dataStream.isConnected = false
                connectPhoneTask.execute(Constants.SERVER_IP)
            } else {
                connectPhoneTask.execute(Constants.SERVER_IP)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}