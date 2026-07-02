package com.promohub.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.promohub.app.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Хранит пользовательские настройки приложения (адрес сервера). */
class SettingsManager(private val context: Context) {

    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
    }

    /** Сохранённый адрес сервера, либо адрес по умолчанию. */
    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BASE_URL_KEY] ?: RetrofitClient.DEFAULT_BASE_URL
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[BASE_URL_KEY] = url
        }
    }
}
