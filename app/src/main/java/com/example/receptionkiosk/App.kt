package com.example.receptionkiosk

import android.app.Application
import com.example.receptionkiosk.core.di.ServiceLocator
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val repository = ServiceLocator.provideRepository(this)
        ServiceLocator.appScope().launch {
            repository.seedDefaultsIfNeeded()
        }
    }
}
