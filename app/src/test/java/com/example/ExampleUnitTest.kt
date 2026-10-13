package com.example

import com.example.engine.CountdownStatus
import com.example.engine.TimeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZonedDateTime

class ExampleUnitTest {

    @Test
    fun testSameDayDifference() {
        val today = LocalDate.of(2026, 10, 12)
        val result = TimeEngine.calculateDifference(today, today, excludeWeekends = false)
        assertTrue(result.isSameDay)
        assertEquals(0L, result.daysCount)
        assertEquals("Same day", result.breakdownText)
    }

    @Test
    fun testCalendarDifference() {
        val start = LocalDate.of(2026, 10, 1)
        val end = LocalDate.of(2026, 10, 11)
        val result = TimeEngine.calculateDifference(start, end, excludeWeekends = false)
        assertFalse(result.isSameDay)
        assertFalse(result.isPast)
        assertEquals(10L, result.daysCount)
    }

    @Test
    fun testBusinessDaysExcludingWeekends() {
        // Oct 12, 2026 is Monday. Oct 16 is Friday (5 days span = 4 business days between Monday and Friday)
        val monday = LocalDate.of(2026, 10, 12)
        val nextMonday = LocalDate.of(2026, 10, 19) // 7 calendar days, but exactly 5 business days
        val result = TimeEngine.calculateDifference(monday, nextMonday, excludeWeekends = true)
        assertEquals(5L, result.daysCount)
    }

    @Test
    fun testDateMathForwardAndBackward() {
        val base = LocalDate.of(2026, 10, 12)
        val forward = TimeEngine.calculateDateMath(base, 10, isAdd = true, excludeWeekends = false)
        assertEquals(LocalDate.of(2026, 10, 22), forward)

        val backward = TimeEngine.calculateDateMath(base, 5, isAdd = false, excludeWeekends = false)
        assertEquals(LocalDate.of(2026, 10, 7), backward)
    }

    @Test
    fun testDateMathExcludingWeekends() {
        // Monday Oct 12 + 5 business days should land on next Monday Oct 19
        val monday = LocalDate.of(2026, 10, 12)
        val arrival = TimeEngine.calculateDateMath(monday, 5, isAdd = true, excludeWeekends = true)
        assertEquals(LocalDate.of(2026, 10, 19), arrival)
    }

    @Test
    fun testCountdownCalculation() {
        val now = ZonedDateTime.now()
        val target = now.plusDays(2).plusHours(4).plusMinutes(15)
        val telemetry = TimeEngine.calculateCountdown(now, target)
        assertEquals(CountdownStatus.ACTIVE, telemetry.status)
        assertEquals(2L, telemetry.days)
        assertEquals(4L, telemetry.hours)
        assertEquals(15L, telemetry.minutes)
    }

    @Test
    fun testWidgetTelemetry() {
        val now = ZonedDateTime.now()
        val telemetry = TimeEngine.calculateWidgetTelemetry(now)
        assertTrue(telemetry.dayOfYear in 1..366)
        assertTrue(telemetry.yearProgressPercent in 0.0f..100.0f)
        assertTrue(telemetry.epochMillis > 0)
        assertTrue(telemetry.julianDate > 2400000.0)
    }
}
