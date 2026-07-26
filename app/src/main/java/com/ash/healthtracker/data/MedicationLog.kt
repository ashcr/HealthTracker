package com.ash.healthtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduledTime: Long,   // when the dose was due
    val takenTime: Long? = null, // when actually marked taken; null = missed/pending
    val medName: String = "Cirtel 40"
)
