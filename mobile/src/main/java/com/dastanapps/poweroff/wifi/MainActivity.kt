package com.dastanapps.poweroff.wifi

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.poweroff.R
import com.dastanapps.poweroff.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket
import kotlin.math.sqrt

/**
 *
 * Created by Iqbal Ahmed on 19/02/2023 11:15 PM
 *
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var isConnected = false
    private var socket: Socket? = null
    private var out: PrintWriter? = null

    private var lastX = 0f
    private var lastY = 0f

    private var rawLastX = 0f
    private var rawLastY = 0f

    private var disX = 0f
    private var disY = 0f

    private var startTime = 0L


    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .permitAll().build()
        )


        binding.btnCenter.setOnClickListener(this)
        binding.btnL.setOnClickListener(this)
        binding.btnR.setOnClickListener(this)
        binding.btnT.setOnClickListener(this)
        binding.btnB.setOnClickListener(this)

        //capture finger taps and movement on the textview
        binding.mousePad.setOnTouchListener { v: View, event: MotionEvent ->
            val _this = this@MainActivity
            if (isConnected && out != null) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //save X and Y positions when user touches the TextView
                        lastX = event.x
                        lastY = event.y

                        rawLastX = event.rawX
                        rawLastY = event.rawY

                        startTime = System.currentTimeMillis()
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        disX = event.x - lastX //Mouse movement in x direction
                        disY = event.y - lastY //Mouse movement in y direction

                        lastX = event.x
                        lastY = event.y

                        if (disX != 0f || disY != 0f) {
                            sendCommands(
                                JSONObject().apply {
                                    put("type", "mouse")
                                    put("x", disX)
                                    put("y", disY)

                                }.toString()
                            )
                        }
                        // Detect Tap
                        val deltaX = event.rawX - rawLastX
                        val deltaY = event.rawY - rawLastY
                        val distance = sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                        if (distance > 2) {
                            startTime = 0
                        }
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val duration = System.currentTimeMillis() - startTime
                        if (duration < 200) {
                            sendCommands(
                                JSONObject().apply {
                                    put("type", Constants.SINGLE_TAP)
                                    put("x", disX)
                                    put("y", disY)

                                }.toString()
                            )
                        }
                        startTime = 0
                        true
                    }

                    else -> {
                        false
                    }
                }
            } else {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_connect) {
            if (isConnected) {
                sendType(Constants.STOP)
                Toast.makeText(this, "Already Connected", Toast.LENGTH_LONG)
                    .show()
                isConnected = false
            } else {
                val connectPhoneTask = ConnectPhoneTask()
                connectPhoneTask.execute(Constants.SERVER_IP) //try to connect to server in another thread
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //OnClick method is called when any of the buttons are pressed
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_l -> sendType(Constants.PLAY)

            R.id.btn_r -> sendType(Constants.NEXT)

            R.id.btn_t -> sendType(Constants.PREVIOUS)

            R.id.btn_b -> sendType(Constants.STOP)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (isConnected && out != null) {
            try {
//                sendCommands("exit"); //tell server to exit
                socket!!.close() //close socket
            } catch (e: IOException) {
                Log.e("remotedroid", "Error in closing socket", e)
            }
        }
    }

    private fun sendCommands(cmd: String) {
        if (isConnected && out != null) {
            out!!.println(cmd)
        }
    }

    private fun sendType(type: String) {
        sendCommands(
            JSONObject().apply {
                put("type", type)
            }.toString()
        )
    }

    inner class ConnectPhoneTask : AsyncTask<String?, Void?, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            var result = true
            try {
                val serverAddr = InetAddress.getByName(params[0])
                socket =
                    Socket(serverAddr, Constants.SERVER_PORT) //Open socket on server IP and port
            } catch (e: IOException) {
                Log.e("remotedroid", "Error while connecting", e)
                result = false
            }
            return result
        }

        override fun onPostExecute(result: Boolean) {
            isConnected = result
            Toast.makeText(
                this@MainActivity,
                if (isConnected) "Connected to server!" else "Error while connecting",
                Toast.LENGTH_LONG
            ).show()
            try {
                if (isConnected) {
                    out = PrintWriter(
                        BufferedWriter(
                            OutputStreamWriter(
                                socket?.getOutputStream()
                            )
                        ), true
                    ) //create output stream to send data to server
                }
            } catch (e: IOException) {
                Log.e("remotedroid", "Error while creating OutWriter", e)
                Toast.makeText(this@MainActivity, "Error while connecting", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}