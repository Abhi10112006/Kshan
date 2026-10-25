package com.abhi.kshan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Database(
    entities = [ChronoEvent::class, ChronoHistory::class],
    version = 2,
    exportSchema = true
)
abstract class ChronoDatabase : RoomDatabase() {

    abstract fun chronoDao(): ChronoDao

    companion object {
        @Volatile
        private var INSTANCE: ChronoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chrono_events ADD COLUMN repeatsYearly INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE chrono_events SET repeatsYearly = 1 WHERE lower(category) IN ('birthday','holiday','anniversary')")
            }
        }

        fun getInstance(context: Context): ChronoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronoDatabase::class.java,
                    "chrono_scope.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.chronoDao()?.let { dao ->
                                    seedDefaultEvents(dao)
                                }
                            }
                        }
                    })
                    .build()
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
                    createdAtIsoString = now.toString(),
                    repeatsYearly = true
                )
            )
            dao.insertEvent(
                ChronoEvent(
                    title = "Summer Solstice",
                    targetIsoString = summerBreak.toString(),
                    category = "Season",
                    isPinned = false,
                    createdAtIsoString = now.toString(),
                    repeatsYearly = false
                )
            )
            dao.insertEvent(
                ChronoEvent(
                    title = "Upcoming Project Launch",
                    targetIsoString = specialMilestone.toString(),
                    category = "Goal",
                    isPinned = false,
                    createdAtIsoString = now.toString(),
                    repeatsYearly = false
                )
            )
        }
    }
}
