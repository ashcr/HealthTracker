package com.ash.healthtracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MigraineDao {
    @Insert
    suspend fun insert(episode: MigraineEpisode): Long

    @Update
    suspend fun update(episode: MigraineEpisode)

    @Delete
    suspend fun delete(episode: MigraineEpisode)

    @Query("SELECT * FROM migraine_episodes ORDER BY startTime DESC")
    fun getAll(): LiveData<List<MigraineEpisode>>

    @Query("SELECT * FROM migraine_episodes WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getOngoing(): MigraineEpisode?

    @Query("SELECT COUNT(*) FROM migraine_episodes WHERE startTime >= :monthStart AND startTime < :monthEnd")
    suspend fun countInRange(monthStart: Long, monthEnd: Long): Int

    @Query("SELECT * FROM migraine_episodes WHERE startTime >= :from ORDER BY startTime DESC")
    suspend fun getSince(from: Long): List<MigraineEpisode>
}
