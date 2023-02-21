package com.dastanapps.poweroff.wifi.data

import com.dastanapps.poweroff.wifi.MainApp
import com.dastanapps.poweroff.wifi.data.local.DataStorePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 1:30 PM
 *
 */

class DataStoreManager {

    private val dataStore by lazy {
        DataStorePrefs(MainApp.INSTANCE)
    }

    fun readString(key: String) = dataStore.readString(key = key)

    fun writeString(key: String, value: String) {
        MainApp.applicationIoScope.launch(Dispatchers.IO) {
            dataStore.writeString(key = key, value = value)
        }
    }
}