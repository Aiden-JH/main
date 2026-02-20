package com.example.receptionkiosk.core.di

import android.content.Context
import androidx.room.Room
import com.example.receptionkiosk.data.local.db.AppDatabase
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import com.example.receptionkiosk.data.repository.ReceptionRepository
import com.example.receptionkiosk.data.repository.ReceptionRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ServiceLocator {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var repository: ReceptionRepository? = null

    fun appScope(): CoroutineScope = appScope

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "reception_kiosk.db"
            ).build().also { database = it }
        }
    }

    fun provideRepository(context: Context): ReceptionRepository {
        return repository ?: synchronized(this) {
            repository ?: run {
                val db = provideDatabase(context)
                val prefs = SettingsPreferences(context.applicationContext)
                ReceptionRepositoryImpl(
                    purposeDao = db.purposeDao(),
                    fieldSchemaDao = db.fieldSchemaDao(),
                    visitDao = db.visitDao(),
                    settingsPreferences = prefs
                )
            }.also { repository = it }
        }
    }
}
