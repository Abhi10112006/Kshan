package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chrono_events")
data class ChronoEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetIsoString: String, // ISO-8601 formatted string e.g. "2027-01-01T00:00:00Z"
    val category: String, // "Cosmic", "Launch", "Milestone", "Personal"
    val isPinned: Boolean = false,
    val createdAtIsoString: String
)
