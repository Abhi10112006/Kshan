package com.abhi.kshan.engine

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields

data class DifferenceResult(
    val daysCount: Long,
    val breakdownText: String,
    val isSameDay: Boolean,
    val isPast: Boolean
)

enum class CountdownStatus {
    ACTIVE,
    TARGET_REACHED,
    EVENT_PASSED,
    AWAITING
}

data class CountdownTelemetry(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val status: CountdownStatus
)

data class QuickJumpItem(
    val label: String,
    val targetDate: LocalDate,
    val daysAdded: Int
)

data class WidgetTelemetry(
    val dayOfYear: Int,
    val totalDaysInYear: Int,
    val yearProgressPercent: Float,
    val dayOfMonth: Int,
    val totalDaysInMonth: Int,
    val monthProgressPercent: Float,
    val quarterNumber: Int,
    val quarterProgressPercent: Float,
    val nextSeasonName: String,
    val daysUntilNextSeason: Long,
    val epochMillis: Long,
    val epochSeconds: Long,
    val isoFormattedUtc: String,
    val quickJumps: List<QuickJumpItem>
)

object TimeEngine {

    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun calculateDifference(
        start: LocalDate,
        end: LocalDate,
        excludeWeekends: Boolean
    ): DifferenceResult {
        if (start == end) {
            return DifferenceResult(
                daysCount = 0,
                breakdownText = "Same day",
                isSameDay = true,
                isPast = false
            )
        }

        val isPast = start.isAfter(end)
        val minDate = if (isPast) end else start
        val maxDate = if (isPast) start else end

        val totalDays: Long = if (!excludeWeekends) {
            ChronoUnit.DAYS.between(minDate, maxDate)
        } else {
            calculateBusinessDaysBetween(minDate, maxDate)
        }

        val period = Period.between(minDate, maxDate)
        val parts = mutableListOf<String>()
        if (period.years > 0) {
            parts.add(if (period.years == 1) "1 year" else "${period.years} years")
        }
        if (period.months > 0) {
            parts.add(if (period.months == 1) "1 month" else "${period.months} months")
        }
        if (period.days > 0) {
            parts.add(if (period.days == 1) "1 day" else "${period.days} days")
        }
        if (parts.isEmpty()) {
            parts.add(if (totalDays == 1L) "1 day" else "$totalDays days")
        }

        val suffix = if (isPast) "ago" else "apart"
        val breakdown = "${parts.joinToString(", ")} $suffix"

        return DifferenceResult(
            daysCount = totalDays,
            breakdownText = breakdown,
            isSameDay = false,
            isPast = isPast
        )
    }

    private fun calculateBusinessDaysBetween(start: LocalDate, end: LocalDate): Long {
        var count = 0L
        var current = start
        while (current.isBefore(end)) {
            val dow = current.dayOfWeek
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++
            }
            current = current.plusDays(1)
        }
        return count
    }

    fun calculateDateMath(
        base: LocalDate,
        days: Long,
        isAdd: Boolean,
        excludeWeekends: Boolean
    ): LocalDate {
        if (days == 0L) return base

        if (!excludeWeekends) {
            return if (isAdd) base.plusDays(days) else base.minusDays(days)
        }

        var remaining = days
        var current = base
        val step = if (isAdd) 1L else -1L

        while (remaining > 0) {
            current = current.plusDays(step)
            val dow = current.dayOfWeek
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                remaining--
            }
        }
        return current
    }

    fun calculateCountdown(now: ZonedDateTime, target: ZonedDateTime): CountdownTelemetry {
        val nowInstant = now.toInstant()
        val targetInstant = target.toInstant()

        if (nowInstant.isAfter(targetInstant)) {
            val secondsDiff = Duration.between(targetInstant, nowInstant).seconds
            return if (secondsDiff < 60) {
                CountdownTelemetry(
                    days = 0,
                    hours = 0,
                    minutes = 0,
                    seconds = 0,
                    status = CountdownStatus.TARGET_REACHED
                )
            } else {
                CountdownTelemetry(
                    days = 0,
                    hours = 0,
                    minutes = 0,
                    seconds = 0,
                    status = CountdownStatus.EVENT_PASSED
                )
            }
        }

        val duration = Duration.between(nowInstant, targetInstant)
        val totalSeconds = duration.seconds

        if (totalSeconds <= 0) {
            return CountdownTelemetry(
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                status = CountdownStatus.TARGET_REACHED
            )
        }

        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return CountdownTelemetry(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            status = CountdownStatus.ACTIVE
        )
    }

    fun calculateWidgetTelemetry(now: ZonedDateTime): WidgetTelemetry {
        val localDate = now.toLocalDate()
        val year = localDate.year

        // 1. Year Progress
        val dayOfYear = localDate.dayOfYear
        val lengthOfYear = localDate.lengthOfYear()
        val yearProgressPercent = (dayOfYear.toFloat() / lengthOfYear.toFloat()) * 100f

        // 2. Month Progress
        val dayOfMonth = localDate.dayOfMonth
        val lengthOfMonth = localDate.lengthOfMonth()
        val monthProgressPercent = (dayOfMonth.toFloat() / lengthOfMonth.toFloat()) * 100f

        // 3. Quarter Progress
        val quarter = localDate.get(IsoFields.QUARTER_OF_YEAR)
        val startOfQuarter = when (quarter) {
            1 -> LocalDate.of(year, 1, 1)
            2 -> LocalDate.of(year, 4, 1)
            3 -> LocalDate.of(year, 7, 1)
            else -> LocalDate.of(year, 10, 1)
        }
        val endOfQuarter = when (quarter) {
            1 -> LocalDate.of(year, 3, 31)
            2 -> LocalDate.of(year, 6, 30)
            3 -> LocalDate.of(year, 9, 30)
            else -> LocalDate.of(year, 12, 31)
        }
        val daysInQuarter = ChronoUnit.DAYS.between(startOfQuarter, endOfQuarter) + 1
        val daysElapsedInQuarter = ChronoUnit.DAYS.between(startOfQuarter, localDate) + 1
        val quarterProgressPercent = (daysElapsedInQuarter.toFloat() / daysInQuarter.toFloat()) * 100f

        // 4. Season Countdown
        val seasons = listOf(
            "Spring" to LocalDate.of(year, 3, 20),
            "Summer" to LocalDate.of(year, 6, 21),
            "Autumn" to LocalDate.of(year, 9, 22),
            "Winter" to LocalDate.of(year, 12, 21),
            "Spring" to LocalDate.of(year + 1, 3, 20)
        )
        val nextSeason = seasons.first { it.second.isAfter(localDate) }
        val daysUntilSeason = ChronoUnit.DAYS.between(localDate, nextSeason.second)

        // 5. Epoch & ISO
        val epochMillis = now.toInstant().toEpochMilli()
        val epochSeconds = epochMillis / 1000
        val isoFormatted = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now)

        // 6. Quick Date Jumps
        val jumps = listOf(
            QuickJumpItem("+1 Week", localDate.plusWeeks(1), 7),
            QuickJumpItem("+30 Days", localDate.plusDays(30), 30),
            QuickJumpItem("+100 Days", localDate.plusDays(100), 100),
            QuickJumpItem("End of Q$quarter", endOfQuarter, ChronoUnit.DAYS.between(localDate, endOfQuarter).toInt())
        )

        return WidgetTelemetry(
            dayOfYear = dayOfYear,
            totalDaysInYear = lengthOfYear,
            yearProgressPercent = yearProgressPercent,
            dayOfMonth = dayOfMonth,
            totalDaysInMonth = lengthOfMonth,
            monthProgressPercent = monthProgressPercent,
            quarterNumber = quarter,
            quarterProgressPercent = quarterProgressPercent,
            nextSeasonName = nextSeason.first,
            daysUntilNextSeason = daysUntilSeason,
            epochMillis = epochMillis,
            epochSeconds = epochSeconds,
            isoFormattedUtc = isoFormatted,
            quickJumps = jumps
        )
    }
}
