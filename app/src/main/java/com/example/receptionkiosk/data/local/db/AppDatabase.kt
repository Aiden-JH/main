package com.example.receptionkiosk.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.receptionkiosk.data.local.dao.FieldSchemaDao
import com.example.receptionkiosk.data.local.dao.PurposeDao
import com.example.receptionkiosk.data.local.dao.VisitDao
import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.local.entity.VisitEntity

@Database(
    entities = [PurposeEntity::class, FieldSchemaEntity::class, VisitEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purposeDao(): PurposeDao
    abstract fun fieldSchemaDao(): FieldSchemaDao
    abstract fun visitDao(): VisitDao
}
