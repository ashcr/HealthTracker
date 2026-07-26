package com.ash.healthtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "migraine_episodes")
data class MigraineEpisode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,           // epoch millis
    val endTime: Long? = null,     // null while ongoing
    val triggers: String = "",     // comma-separated tags
    val reliefMethods: String = "",// comma-separated tags (what helped)
    val severity: Int = 5,         // 1-10
    val notes: String = ""
) {
    val durationMinutes: Long?
        get() = endTime?.let { (it - startTime) / 60000 }
}
