package com.example.stopwake.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_settings")

@Singleton
class AlarmPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val ALARM_VOLUME = floatPreferencesKey("alarm_volume")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val VIBRATION_PATTERN = stringPreferencesKey("vibration_pattern")
        val GRADUAL_VOLUME_ENABLED = booleanPreferencesKey("gradual_volume_enabled")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val CUSTOM_MESSAGE = stringPreferencesKey("custom_message")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
    }

    val alarmVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALARM_VOLUME] ?: 0.8f
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
    }

    val vibrationPattern: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_PATTERN] ?: "Strong"
    }

    val gradualVolumeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GRADUAL_VOLUME_ENABLED] ?: false
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TTS_ENABLED] ?: true
    }

    val customMessage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_MESSAGE] 
            ?: "Wake up! You're approaching your destination."
    }

    val snoozeDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SNOOZE_DURATION] ?: 5
    }

    suspend fun setAlarmVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_VOLUME] = volume
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setVibrationPattern(pattern: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_PATTERN] = pattern
        }
    }

    suspend fun setGradualVolumeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRADUAL_VOLUME_ENABLED] = enabled
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_ENABLED] = enabled
        }
    }

    suspend fun setCustomMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_MESSAGE] = message
        }
    }

    suspend fun setSnoozeDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION] = duration
        }
    }
}
