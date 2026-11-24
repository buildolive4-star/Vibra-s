package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppMode
import com.example.data.model.safeEnumValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vibraDataStore: DataStore<Preferences> by preferencesDataStore(name = "vibra_prefs")

class VibraPreferences(private val context: Context) {
    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val PREFERRED_MODE = stringPreferencesKey("preferred_mode")
        val PARTICLES_ENABLED = booleanPreferencesKey("particles_enabled")
    }

    val onboardingCompleted: Flow<Boolean> = context.vibraDataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_DONE] ?: false
    }

    val preferredMode: Flow<AppMode> = context.vibraDataStore.data.map { prefs ->
        safeEnumValue(prefs[Keys.PREFERRED_MODE], AppMode.CALLER)
    }

    val particlesEnabled: Flow<Boolean> = context.vibraDataStore.data.map { prefs ->
        prefs[Keys.PARTICLES_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        context.vibraDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = done
        }
    }

    suspend fun setPreferredMode(mode: AppMode) {
        context.vibraDataStore.edit { prefs ->
            prefs[Keys.PREFERRED_MODE] = mode.name
        }
    }

    suspend fun setParticlesEnabled(enabled: Boolean) {
        context.vibraDataStore.edit { prefs ->
            prefs[Keys.PARTICLES_ENABLED] = enabled
        }
    }
}
