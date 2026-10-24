package com.abhi.kshan

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.abhi.kshan.engine.TimeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("क्षण (Kshan)", appName)
    }

    @Test
    fun `recurring holiday auto rolls forward and projects onto 365 dot matrix`() {
        val now = ZonedDateTime.of(2026, 10, 24, 12, 0, 0, 0, ZoneId.of("UTC"))
        val pastBirthday = ZonedDateTime.of(2002, 5, 15, 9, 0, 0, 0, ZoneId.of("UTC"))

        assertFalse(TimeEngine.shouldDefaultToCountUp(now, pastBirthday, "Birthday"))

        val (effectiveTarget, rolled) = TimeEngine.resolveEffectiveTarget(
            now = now,
            rawTarget = pastBirthday,
            category = "Birthday",
            isCountUpMode = false
        )
        assertTrue(rolled)
        assertEquals(2027, effectiveTarget.year)

        val projectedDay = TimeEngine.projectEventDayOfYear(pastBirthday, "Birthday", 2026)
        assertNotNull(projectedDay)
        assertEquals(LocalDate.of(2026, 5, 15).dayOfYear, projectedDay)
    }

    @Test
    fun `kshan category defaults to count up mode and temporal constellation computes 3 cards`() {
        val now = ZonedDateTime.of(2026, 10, 24, 12, 0, 0, 0, ZoneId.of("UTC"))
        assertTrue(TimeEngine.shouldDefaultToCountUp(now, now, "Kshan"))

        val constellation = TimeEngine.calculateTemporalConstellation(
            start = LocalDate.of(2026, 10, 1),
            end = LocalDate.of(2026, 10, 27),
            daysCount = 26L,
            today = LocalDate.of(2026, 10, 24)
        )
        assertEquals(3, constellation.cards.size)
        assertTrue(constellation.isTodayInsideSpan)
        assertNotNull(constellation.todayProgressAlongArc)
    }
}

