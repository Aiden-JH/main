package com.example.receptionkiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldSchemaDao {
    @Query("SELECT * FROM field_schemas ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<FieldSchemaEntity>>

    @Query("SELECT * FROM field_schemas WHERE enabled = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeEnabled(): Flow<List<FieldSchemaEntity>>

    @Query("SELECT COUNT(*) FROM field_schemas")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FieldSchemaEntity>)

    @Query("UPDATE field_schemas SET enabled = :enabled WHERE id = :fieldId")
    suspend fun setEnabled(fieldId: Long, enabled: Boolean)

    @Query("UPDATE field_schemas SET required = :required WHERE id = :fieldId")
    suspend fun setRequired(fieldId: Long, required: Boolean)
}
