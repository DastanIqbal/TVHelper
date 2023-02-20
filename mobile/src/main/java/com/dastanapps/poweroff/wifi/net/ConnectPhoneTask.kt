package com.dastanapps.poweroff.wifi.net

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope

/**
 *
 * Created by Iqbal Ahmed on 20/02/2023 12:47 PM
 *
 */

@SuppressLint("StaticFieldLeak")
class ConnectPhoneTask(
    private val activity: FragmentActivity
) : AsyncTask<String?, Void?, Boolean>() {
    private val TAG = ConnectPhoneTask::class.java.simpleName

    val dataStream by lazy {
        ConnectionDataStream(context = activity, scope = activity.lifecycleScope)
    }

    override fun doInBackground(vararg params: String?): Boolean {
        return dataStream.init(params[0]!!)
    }

    override fun onPostExecute(isInit: Boolean) {
        dataStream.prepare(isInit)
    }
}