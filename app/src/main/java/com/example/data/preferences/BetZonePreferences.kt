package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.betZoneDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "betzone_x_prefs"
)

class BetZonePreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_DAILY_CLAIM_DATE = stringPreferencesKey("last_daily_claim_date")
        val PENDING_PHONE = stringPreferencesKey("pending_phone")
        val PENDING_COUNTRY_CODE = stringPreferencesKey("pending_country_code")
    }

    val onboardingCompleted: Flow<Boolean> = context.betZoneDataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val darkMode: Flow<Boolean> = context.betZoneDataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: true
    }

    val language: Flow<String> = context.betZoneDataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE] ?: "English"
    }

    val lastDailyClaimDate: Flow<String> = context.betZoneDataStore.data.map { prefs ->
        prefs[Keys.LAST_DAILY_CLAIM_DATE] ?: ""
    }

    val pendingPhoneInfo: Flow<Pair<String, String>> = context.betZoneDataStore.data.map { prefs ->
        (prefs[Keys.PENDING_COUNTRY_CODE] ?: "+91") to (prefs[Keys.PENDING_PHONE] ?: "")
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.betZoneDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.betZoneDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.betZoneDataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setLastDailyClaimDate(dateKey: String) {
        context.betZoneDataStore.edit { it[Keys.LAST_DAILY_CLAIM_DATE] = dateKey }
    }

    suspend fun savePendingPhone(countryCode: String, phone: String) {
        context.betZoneDataStore.edit {
            it[Keys.PENDING_COUNTRY_CODE] = countryCode
            it[Keys.PENDING_PHONE] = phone
        }
    }
}
