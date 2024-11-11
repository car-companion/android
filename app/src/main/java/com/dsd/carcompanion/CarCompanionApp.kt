package com.dsd.carcompanion

import android.app.Application

class CarCompanionApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize DataStoreManager with application context
        DataStoreManager.initialize(applicationContext)
    }
}