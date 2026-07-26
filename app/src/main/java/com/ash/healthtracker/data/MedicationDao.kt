package com.ash.healthtracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(log: MedicationLog): Long

    @Update
    suspend fun update(log: MedicationLog)

    @Query("SELECT * FROM medication_logs ORDER BY scheduledTime DESC LIMIT 30")
    fun getRecent(): LiveData<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE takenTime IS NULL AND scheduledTime <= :now ORDER BY scheduledTime DESC LIMIT 1")
    suspend fun getLatestPending(now: Long): MedicationLog?

    @Query("SELECT * FROM medication_logs WHERE id = :id")
    suspend fun getById(id: Long): MedicationLog?
}
