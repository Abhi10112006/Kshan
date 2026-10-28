package com.abhi.kshan

import com.abhi.kshan.engine.CountdownStatus
import com.abhi.kshan.engine.TimeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeEngineTest {

    private val istZone = ZoneId.of("Asia/Kolkata")

    @Test
    fun feb29_leapYearRollover_rollsToFeb28InNonLeapYear2027_andBackToFeb29InLeapYear2028() {
        val feb29Origin = ZonedDateTime.of(2024, 2, 29, 9, 0, 0, 0, istZone)

        val nowIn2026 = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, istZone)
        val (target2027, rolled2027) = TimeEngine.resolveEffectiveTarget(
            now = nowIn2026,
            raw = feb29Origin,
            repeatsYearly = true,
            isCountUpMode = false
        )
        assertTrue("Should roll over for past yearly event", rolled2027)
        assertEquals(LocalDate.of(2027, 2, 28), target2027.toLocalDate())

        val nowIn2027 = ZonedDateTime.of(2027, 3, 1, 12, 0, 0, 0, istZone)
        val (target2028, rolled2028) = TimeEngine.resolveEffectiveTarget(
            now = nowIn2027,
            raw = feb29Origin,
            repeatsYearly = true,
            isCountUpMode = false
        )
        assertTrue("Should roll over to leap year 2028", rolled2028)
        assertEquals(LocalDate.of(2028, 2, 29), target2028.toLocalDate())
    }

    @Test
    fun birthdayOccurringToday_staysInTodayCelebrationAllDay_evenAfterMorningTimePasses() {
        val pastBirthday = ZonedDateTime.of(2000, 10, 25, 0, 0, 0, 0, istZone)
        val nowEveningToday = ZonedDateTime.of(2026, 10, 25, 18, 45, 0, 0, istZone)

        val (effectiveTarget, wasRolledOver) = TimeEngine.resolveEffectiveTarget(
            now = nowEveningToday,
            raw = pastBirthday,
            repeatsYearly = true,
            isCountUpMode = false
        )
        assertTrue(wasRolledOver)
        assertEquals(LocalDate.of(2026, 10, 25), effectiveTarget.toLocalDate())

        val result = TimeEngine.calculateCountdown(nowEveningToday, effectiveTarget)
        assertEquals(CountdownStatus.TODAY_CELEBRATION, result.status)
        assertEquals(0L, result.days)
    }

    @Test
    fun futureYearRecurringEvent_isNotPulledBackwardIntoCurrentYear() {
        val now = ZonedDateTime.of(2026, 10, 25, 10, 0, 0, 0, istZone)
        val futureEvent2027 = ZonedDateTime.of(2027, 12, 15, 9, 0, 0, 0, istZone)

        val (effectiveTarget, wasRolledOver) = TimeEngine.resolveEffectiveTarget(
            now = now,
            raw = futureEvent2027,
            repeatsYearly = true,
            isCountUpMode = false
        )

        assertFalse("Future event must not roll backward", wasRolledOver)
        assertEquals(LocalDate.of(2027, 12, 15), effectiveTarget.toLocalDate())
        assertFalse("Future event must not allow Days Since toggle", TimeEngine.canUseCountUpMode(now, futureEvent2027))
    }

    @Test
    fun dec31_to_jan1_yearBoundaryRollover_advancesToNextCalendarYear() {
        val dec31Origin = ZonedDateTime.of(2023, 12, 31, 23, 59, 0, 0, istZone)

        val onDec31 = ZonedDateTime.of(2026, 12, 31, 14, 0, 0, 0, istZone)
        val (targetOnDec31, _) = TimeEngine.resolveEffectiveTarget(
            now = onDec31,
            raw = dec31Origin,
            repeatsYearly = true,
            isCountUpMode = false
        )
        assertEquals(LocalDate.of(2026, 12, 31), targetOnDec31.toLocalDate())

        val onJan1 = ZonedDateTime.of(2027, 1, 1, 0, 5, 0, 0, istZone)
        val (targetOnJan1, rolledOnJan1) = TimeEngine.resolveEffectiveTarget(
            now = onJan1,
            raw = dec31Origin,
            repeatsYearly = true,
            isCountUpMode = false
        )
        assertTrue(rolledOnJan1)
        assertEquals(LocalDate.of(2027, 12, 31), targetOnJan1.toLocalDate())
    }

    @Test
    fun compactMetricsStrip_computesAccurateWeeksRemainderWorkdaysAndHours() {
        val start = LocalDate.of(2026, 10, 5) // Monday
        val end = LocalDate.of(2026, 10, 25)  // Sunday (20 calendar days later)

        val strip = TimeEngine.calculateCompactMetricsStrip(start, end)
        assertEquals(2L, strip.weeks)
        assertEquals(6L, strip.remainingDays)
        assertEquals(15L, strip.workdays) // 3 full Mon-Fri weeks = 15 workdays
        assertEquals(480L, strip.totalHours)
        assertEquals("2w 6d  •  15 Workdays  •  480 Hours", strip.formattedLine)
    }

    @Test
    fun runwayFocusMetrics_and_plannerBufferMetrics_computeExpectedCapacity() {
        val start = LocalDate.of(2026, 10, 5) // Monday
        val end = LocalDate.of(2026, 10, 19)  // Monday (14 calendar days later)

        val focus = TimeEngine.calculateRunwayFocusMetrics(start, end, hoursPerDay = 6)
        assertEquals(14L, focus.totalCalendarDays)
        assertEquals(10L, focus.workingDays)
        assertEquals(4L, focus.weekendDaysOff)
        assertEquals(60L, focus.focusHours)
        assertTrue(focus.focusLineText.contains("60 Deep Focus Hours"))

        val buffer = TimeEngine.calculatePlannerBufferMetrics(start, end)
        assertEquals(10L, buffer.workingDays)
        assertEquals(4L, buffer.weekendBufferDays)
        assertEquals(14L, buffer.totalCalendarDays)
        assertTrue(buffer.formattedSummary.contains("10 Working Days + 4 Weekend Buffer Days"))
    }
}
