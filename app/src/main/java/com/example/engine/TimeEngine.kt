package com.example.engine

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

data class WidgetTelemetry(
    val dayOfYear: Int,
    val totalDaysInYear: Int,
    val yearProgressPercent: Float,
    val epochMillis: Long,
    val epochSeconds: Long,
    val julianDate: Double,
    val martianSol: Double
)

object TimeEngine {

    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

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
        val dayOfYear = localDate.dayOfYear
        val lengthOfYear = localDate.lengthOfYear()
        val progressPercent = (dayOfYear.toFloat() / lengthOfYear.toFloat()) * 100f

        val epochMillis = now.toInstant().toEpochMilli()
        val epochSeconds = epochMillis / 1000

        val julianDate = (epochMillis / 86400000.0) + 2440587.5
        val martianSol = (julianDate - 2405522.0028779) / 1.0274912517

        return WidgetTelemetry(
            dayOfYear = dayOfYear,
            totalDaysInYear = lengthOfYear,
            yearProgressPercent = progressPercent,
            epochMillis = epochMillis,
            epochSeconds = epochSeconds,
            julianDate = julianDate,
            martianSol = martianSol
        )
    }
}
