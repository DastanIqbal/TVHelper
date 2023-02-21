package com.dastanapps.poweroff.wifi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.poweroff.R
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

    private val dataStream get() = MainApp.INSTANCE.connectionManager.dataStream

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_CANCELED) {
                if (!dataStream.isConnected) {
                    finish()
                }
            } else if (it.resultCode == Activity.RESULT_OK) {
                if (dataStream.isConnected) {
                    binding.mousePad.setOnTouchListener(
                        OnTouchListenerImpl(dataStream)
                    )
                    binding.dpadView.setOnDPadListener(DpadListenerImpl(dataStream))
                }
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!dataStream.isConnected) {
            activityLauncher.launch(Intent(this, NoServerScreen::class.java))
        }
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
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
}