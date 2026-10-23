package com.abhi.kshan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chrono_history")
data class ChronoHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "DIFF", "MATH"
    val summary: String,
    val timestampIsoString: String
)
