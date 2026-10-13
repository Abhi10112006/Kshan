package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Database(
    entities = [ChronoEvent::class, ChronoHistory::class],
    version = 1,
    exportSchema = false
)
abstract class ChronoDatabase : RoomDatabase() {

    abstract fun chronoDao(): ChronoDao

    companion object {
        @Volatile
        private var INSTANCE: ChronoDatabase? = null

        fun getInstance(context: Context): ChronoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronoDatabase::class.java,
                    "chrono_scope.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.chronoDao()?.let { dao ->
                                seedDefaultEvents(dao)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedDefaultEvents(dao: ChronoDao) {
            val now = ZonedDateTime.now()
            val nextYear = now.plusYears(1).withDayOfYear(1).withHour(0).withMinute(0).withSecond(0)
            val summerBreak = now.plusMonths(5).withDayOfMonth(21).withHour(9).withMinute(0).withSecond(0)
            val specialMilestone = now.plusDays(45).withHour(18).withMinute(0).withSecond(0)

            dao.insertEvent(
                ChronoEvent(
                    title = "New Year's Celebration",
                    targetIsoString = nextYear.toString(),
                    category = "Holiday",
                    isPinned = true,
                    createdAtIsoString = now.toString()
                )
            )
            dao.insertEvent(
                ChronoEvent(
                    title = "Summer Solstice",
                    targetIsoString = summerBreak.toString(),
                    category = "Season",
                    isPinned = false,
                    createdAtIsoString = now.toString()
                )
            )
            dao.insertEvent(
                ChronoEvent(
                    title = "Upcoming Project Launch",
                    targetIsoString = specialMilestone.toString(),
                    category = "Goal",
                    isPinned = false,
                    createdAtIsoString = now.toString()
                )
            )
        }
    }
}
