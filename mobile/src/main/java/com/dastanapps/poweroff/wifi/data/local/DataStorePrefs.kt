package com.dastanapps.poweroff.wifi.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 *
 * Created by Iqbal Ahmed on 21/02/2023 12:19 PM
 *
 */

class DataStorePrefs(
    val context: Context
) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun readString(key: String): Flow<String> {
        val keyStr = stringPreferencesKey(key)
        val flow: Flow<String> = context.dataStore.data.map { preferences ->
            preferences[keyStr] ?: ""
        }
        return flow
    }

    suspend fun writeString(key: String, value: String) {
        val keyStr = stringPreferencesKey(key)
        context.dataStore.edit { settings ->
            settings[keyStr] = value
        }
    }
}