package com.dastanapps.poweroff.wifi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.poweroff.databinding.ActivityMainBinding
import com.dastanapps.poweroff.ui.noserver.NoServerScreen
import com.dastanapps.poweroff.wifi.contracts.impl.DpadListenerImpl
import com.dastanapps.poweroff.wifi.contracts.impl.OnTouchListenerImpl

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 11:15 PM
 *
 */
class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    private val dataStream by lazy {
        MainApp.INSTANCE.connectionManager.dataStream
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MainApp.INSTANCE.connectionManager.dataStream.isConnected) {
            startActivity(Intent(this, NoServerScreen::class.java))
        }
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.mousePad.setOnTouchListener(OnTouchListenerImpl(dataStream))
        binding.dpadView.setOnDPadListener(DpadListenerImpl(dataStream))
    }
}