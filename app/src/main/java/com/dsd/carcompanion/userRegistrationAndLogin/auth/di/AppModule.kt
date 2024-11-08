package com.dsd.carcompanion.userRegistrationAndLogin.auth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.dsd.carcompanion.userRegistrationAndLogin.auth.datastore.JwtTokenDataStore
import com.dsd.carcompanion.utils.JwtTokenManager
import com.dsd.carcompanion.utils.Constants.AUTH_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @[Provides Singleton]
    fun provideDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { appContext.preferencesDataStoreFile(AUTH_PREFERENCES) }
        )
    }

    @[Provides Singleton]
    fun provideJwtTokenManager(dataStore: DataStore<Preferences>): JwtTokenManager {
        return JwtTokenDataStore(dataStore = dataStore)
    }
}