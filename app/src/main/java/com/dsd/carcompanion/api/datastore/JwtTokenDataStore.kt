package com.dsd.carcompanion.api.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dsd.carcompanion.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.dsd.carcompanion.api.utils.JwtTokenManager

class JwtTokenDataStore(context: Context) : JwtTokenManager {

    private val dataStore = DataStoreManager.getInstance()

    companion object {
        val ACCESS_JWT_KEY = stringPreferencesKey("access_jwt")
        val REFRESH_JWT_KEY = stringPreferencesKey("refresh_jwt")
    }

    override suspend fun saveAccessJwt(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_JWT_KEY] = token
        }
    }

    override suspend fun saveRefreshJwt(token: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_JWT_KEY] = token
        }
    }

    override suspend fun getAccessJwt(): String? {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_JWT_KEY]
        }.first()
    }

    override suspend fun getRefreshJwt(): String? {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_JWT_KEY]
        }.first()
    }

    override suspend fun clearAllTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_JWT_KEY)
            preferences.remove(REFRESH_JWT_KEY)
        }
    }
}