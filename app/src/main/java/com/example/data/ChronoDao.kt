package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChronoDao {

    @Query("SELECT * FROM chrono_events ORDER BY isPinned DESC, targetIsoString ASC")
    fun getAllEvents(): Flow<List<ChronoEvent>>

    @Query("SELECT * FROM chrono_events WHERE isPinned = 1 LIMIT 1")
    fun getPinnedEvent(): Flow<ChronoEvent?>

    @Query("SELECT * FROM chrono_events WHERE isPinned = 1 LIMIT 1")
    suspend fun getPinnedEventDirect(): ChronoEvent?

    @Query("SELECT * FROM chrono_events ORDER BY targetIsoString ASC LIMIT 1")
    suspend fun getNextUpcomingEventDirect(): ChronoEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ChronoEvent): Long

    @Update
    suspend fun updateEvent(event: ChronoEvent)

    @Delete
    suspend fun deleteEvent(event: ChronoEvent)

    @Query("UPDATE chrono_events SET isPinned = 0")
    suspend fun clearPinnedStatus()

    @Query("UPDATE chrono_events SET isPinned = 1 WHERE id = :id")
    suspend fun setPinnedEvent(id: Long)

    @Query("SELECT COUNT(*) FROM chrono_events")
    suspend fun getEventsCount(): Int

    // Calculation History
    @Query("SELECT * FROM chrono_history ORDER BY id DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ChronoHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ChronoHistory)

    @Query("DELETE FROM chrono_history")
    suspend fun clearHistory()
}
