package com.dastanapps.poweroff.wifi.net

import android.annotation.SuppressLint
import android.os.AsyncTask
import com.dastanapps.poweroff.wifi.MainApp
import kotlinx.coroutines.launch

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 12:47 PM
 *
 */

@SuppressLint("StaticFieldLeak")
class ConnectPhoneTask(
    private val status: (isConnect: Boolean) -> Unit
) : AsyncTask<String?, Void?, Boolean>() {
    private val TAG = ConnectPhoneTask::class.java.simpleName

    val dataStream by lazy {
        ConnectionDataStream(status)
    }

    override fun doInBackground(vararg params: String?): Boolean {
        return dataStream.init(params[0]!!)
    }

    override fun onPostExecute(isInit: Boolean) {
        MainApp.applicationIoScope.launch {
            dataStream.prepare(isInit)
        }
    }
}