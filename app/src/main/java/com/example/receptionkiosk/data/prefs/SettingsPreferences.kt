package com.example.receptionkiosk.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = SettingsKeys.PREFS_NAME)

class SettingsPreferences(private val context: Context) {
    private val seedDoneKey = booleanPreferencesKey(SettingsKeys.SEED_DONE)
    private val slackWebhookKey = stringPreferencesKey(SettingsKeys.SLACK_WEBHOOK_URL)
    private val adminPinKey = stringPreferencesKey(SettingsKeys.ADMIN_PIN)
    private val keepScreenOnKey = booleanPreferencesKey(SettingsKeys.KEEP_SCREEN_ON)
    private val idleTimeoutKey = intPreferencesKey(SettingsKeys.IDLE_TIMEOUT_SECONDS)

    val isSeedDone: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[seedDoneKey] ?: false }
    val slackWebhookUrl: Flow<String> = context.dataStore.data.map { prefs -> prefs[slackWebhookKey].orEmpty() }
    val adminPin: Flow<String> = context.dataStore.data.map { prefs -> prefs[adminPinKey] ?: "1234" }
    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[keepScreenOnKey] ?: true }
    val idleTimeoutSeconds: Flow<Int> = context.dataStore.data.map { prefs -> prefs[idleTimeoutKey] ?: 60 }

    suspend fun getSlackWebhookUrl(): String = slackWebhookUrl.first().trim()
    suspend fun getAdminPin(): String = adminPin.first()

    suspend fun setSeedDone(done: Boolean) {
        context.dataStore.edit { prefs: MutablePreferences -> prefs[seedDoneKey] = done }
    }

    suspend fun setSlackWebhookUrl(url: String) {
        context.dataStore.edit { prefs: MutablePreferences -> prefs[slackWebhookKey] = url.trim() }
    }

    suspend fun setAdminPin(pin: String) {
        context.dataStore.edit { prefs: MutablePreferences -> prefs[adminPinKey] = pin.trim().ifBlank { "1234" } }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs: MutablePreferences -> prefs[keepScreenOnKey] = enabled }
    }

    suspend fun setIdleTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { prefs: MutablePreferences -> prefs[idleTimeoutKey] = seconds.coerceIn(0, 3600) }
    }
}
