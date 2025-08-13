package com.mono.music.data.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow


interface IPreferenceDataStoreAPI {
     fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T>
    suspend fun <T> getFirstPreference(key: Preferences.Key<T>,defaultValue: T):T
    suspend fun <T> putPreference(key: Preferences.Key<T>,value:T)
    suspend fun <T> removePreference(key: Preferences.Key<T>)
    suspend fun clearAllPreference()
}

object PreferenceDataStoreConstants {
    val PROMO_SUCCESS_KEY = booleanPreferencesKey("PROMO_USED")
    val LOGGED_IN_KEY = booleanPreferencesKey("LOGGED_IN")
    val ACCESS_TOKEN_KEY = stringPreferencesKey("ACCESS_TOKEN")
    val REFRESH_TOKEN_KEY = stringPreferencesKey("REFRESH_TOKEN")
    val PHONE_KEY = stringPreferencesKey("PHONE")
    val NAME_KEY = stringPreferencesKey("NAME")
    val VALID_UNTIL_KEY = stringPreferencesKey("VALID_UNTIL")
    val FIRST_TIME_KEY = stringPreferencesKey("FIRST_TIME")
    val BIRTDAY_KEY = stringPreferencesKey("BIRTDAY")
    val GENDER_KEY = stringPreferencesKey("GENDER")
    val LANGUAGE_KEY = stringPreferencesKey("LANGUAGE")

    val REGISTER_COMPLETED_KEY = booleanPreferencesKey("REGISTER_COMPLETED_KEY")
    val PLAN_SELECTED_KEY = booleanPreferencesKey("PLAN_SELECTED_KEY")
    val SELECTED_TRACK_KEY = stringPreferencesKey("SELECTED_TRACK_KEY")

}