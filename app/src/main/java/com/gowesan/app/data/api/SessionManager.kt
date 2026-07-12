package com.gowesan.app.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gowesan_session")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val userId: Flow<String> = context.dataStore.data.map { it[KEY_USER_ID] ?: "" }
    val username: Flow<String> = context.dataStore.data.map { it[KEY_USERNAME] ?: "" }

    suspend fun saveLogin(userId: String, username: String, displayName: String?) {
        context.dataStore.edit {
            it[KEY_USER_ID] = userId
            it[KEY_USERNAME] = username
            it[KEY_DISPLAY_NAME] = displayName ?: ""
            it[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }
}
