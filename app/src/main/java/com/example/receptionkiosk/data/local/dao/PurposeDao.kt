package com.example.receptionkiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurposeDao {
    @Query("SELECT * FROM purposes ORDER BY sortOrder ASC, label ASC")
    fun observeAll(): Flow<List<PurposeEntity>>

    @Query("SELECT * FROM purposes WHERE enabled = 1 ORDER BY sortOrder ASC, label ASC")
    fun observeEnabled(): Flow<List<PurposeEntity>>

    @Query("SELECT * FROM purposes WHERE id = :purposeId LIMIT 1")
    suspend fun getById(purposeId: Long): PurposeEntity?

    @Query("SELECT COUNT(*) FROM purposes")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PurposeEntity>)

    @Query("UPDATE purposes SET enabled = :enabled WHERE id = :purposeId")
    suspend fun setEnabled(purposeId: Long, enabled: Boolean)
}
