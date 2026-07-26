package com.ash.healthtracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(log: WaterLog): Long

    @Query("SELECT * FROM water_logs WHERE dayKey = :dayKey ORDER BY timestamp DESC")
    fun getForDay(dayKey: String): LiveData<List<WaterLog>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE dayKey = :dayKey")
    fun getTotalForDay(dayKey: String): LiveData<Int>

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
