package com.dsd.carcompanion

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore

// Define the preferences name for the DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jwt_tokens")

// Singleton object to manage DataStore access
object DataStoreManager {
    // This will hold a singleton reference to DataStore
    private lateinit var instance: DataStore<Preferences>

    // Initialize the instance of DataStore only when needed
    fun initialize(context: Context) {
        if (!::instance.isInitialized) {
            instance = context.dataStore
        }
    }

    // Get the instance of DataStore
    fun getInstance(): DataStore<Preferences> {
        if (!::instance.isInitialized) {
            throw IllegalStateException("DataStore has not been initialized")
        }
        return instance
    }
}
