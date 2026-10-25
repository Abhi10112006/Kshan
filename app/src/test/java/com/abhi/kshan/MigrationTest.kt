package com.abhi.kshan

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.abhi.kshan.data.ChronoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    @Test
    fun migration1To2_preservesExistingEvents_andBackfillsRepeatsYearlyByCategory(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_chrono_migration_1_2.db"
        context.deleteDatabase(dbName)

        // 1. Create a v1 SQLite database with the exact v1 schema from 1.json
        val callbackV1 = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chrono_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `targetIsoString` TEXT NOT NULL, `category` TEXT NOT NULL, `isPinned` INTEGER NOT NULL, `createdAtIsoString` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chrono_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mode` TEXT NOT NULL, `summary` TEXT NOT NULL, `timestampIsoString` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)"
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '27b19712d30edf28af976a4faa6660c1')"
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }

        val helperV1 = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(callbackV1)
                .build()
        )

        helperV1.writableDatabase.use { db ->
            db.execSQL(
                """
                INSERT INTO chrono_events (id, title, targetIsoString, category, isPinned, createdAtIsoString)
                VALUES (1, 'Aarav Birthday', '2000-05-12T00:00:00+05:30[Asia/Kolkata]', 'Birthday', 1, '2026-01-01T00:00:00Z')
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO chrono_events (id, title, targetIsoString, category, isPinned, createdAtIsoString)
                VALUES (2, 'Product Launch', '2027-01-15T09:00:00+05:30[Asia/Kolkata]', 'Milestone', 0, '2026-01-02T00:00:00Z')
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO chrono_events (id, title, targetIsoString, category, isPinned, createdAtIsoString)
                VALUES (3, 'Wedding Anniversary', '2020-11-20T00:00:00+05:30[Asia/Kolkata]', 'Anniversary', 0, '2026-01-03T00:00:00Z')
                """.trimIndent()
            )
        }
        helperV1.close()

        // 2. Open with Room at v2 using ChronoDatabase.MIGRATION_1_2 (without destructive fallback)
        val roomDb = Room.databaseBuilder(context, ChronoDatabase::class.java, dbName)
            .addMigrations(ChronoDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val events = roomDb.chronoDao().getAllEvents().first()
        assertEquals(3, events.size)

        val birthday = events.first { it.id == 1L }
        val milestone = events.first { it.id == 2L }
        val anniversary = events.first { it.id == 3L }

        assertEquals("Aarav Birthday", birthday.title)
        assertTrue("Birthday category should backfill repeatsYearly = true", birthday.repeatsYearly)

        assertEquals("Product Launch", milestone.title)
        assertFalse("Milestone category should keep repeatsYearly = false", milestone.repeatsYearly)

        assertEquals("Wedding Anniversary", anniversary.title)
        assertTrue("Anniversary category should backfill repeatsYearly = true", anniversary.repeatsYearly)

        roomDb.close()
        context.deleteDatabase(dbName)
    }
}
