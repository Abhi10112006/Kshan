package com.abhi.kshan.engine

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.util.Locale
import kotlin.math.abs

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

data class TemporalContextCardItem(
    val title: String,
    val primaryMetric: String,
    val secondaryMetric: String,
    val poeticCaption: String
)

data class TemporalConstellationTelemetry(
    val todayProgressAlongArc: Float?, // 0f..1f if Today lies between min(start,end) and max(start,end), else clamped indicator
    val isTodayInsideSpan: Boolean,
    val cards: List<TemporalContextCardItem>
)

data class PresentKshanTelemetry(
    val dayElapsedPercent: Float,
    val secondsIntoDay: Int,
    val secondsRemainingInDay: Int,
    val rituNameDevanagari: String,
    val rituNameEnglish: String,
    val cosmicSubtitle: String
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

    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    val FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)

    private val RECURRING_CATEGORIES = setOf("holiday", "birthday", "anniversary", "festival")

    fun isRecurringCategory(category: String): Boolean {
        val lower = category.trim().lowercase(Locale.US)
        return RECURRING_CATEGORIES.any { lower.contains(it) }
    }

    /**
     * Determines whether an event should automatically default to Days Since (+) mode
     * when selected, unless the user explicitly overrides the mode pill.
     */
    fun shouldDefaultToCountUp(
        now: ZonedDateTime,
        rawTarget: ZonedDateTime,
        category: String
    ): Boolean {
        if (category.equals("Kshan", ignoreCase = true)) return true
        return rawTarget.toInstant().isBefore(now.toInstant()) && !isRecurringCategory(category)
    }

    /**
     * Resolves the effective target ZonedDateTime and whether an annual recurring event
     * was automatically rolled forward to its next upcoming occurrence.
     */
    fun resolveEffectiveTarget(
        now: ZonedDateTime,
        rawTarget: ZonedDateTime,
        category: String,
        isCountUpMode: Boolean
    ): Pair<ZonedDateTime, Boolean> {
        if (!isCountUpMode && rawTarget.toInstant().isBefore(now.toInstant()) && isRecurringCategory(category)) {
            var rolled = rawTarget
            while (!rolled.toInstant().isAfter(now.toInstant())) {
                rolled = rolled.plusYears(1)
            }
            return Pair(rolled, true)
        }
        return Pair(rawTarget, false)
    }

    /**
     * Calculates live elapsed time since a past or captured Kshan milestone (`+ Days Since`).
     */
    fun calculateElapsedSince(now: ZonedDateTime, origin: ZonedDateTime): CountdownTelemetry {
        val duration = Duration.between(origin.toInstant(), now.toInstant()).abs()
        val totalSeconds = duration.seconds
        val days = totalSeconds / 86400L
        val hours = (totalSeconds % 86400L) / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return CountdownTelemetry(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            status = CountdownStatus.ACTIVE
        )
    }

    /**
     * Projects any saved ChronoEvent onto the current calendar year's 1..365/366 day index.
     * Recurring categories (Birthday, Holiday, Anniversary, Festival) are projected onto
     * `currentYear` with leap-year safety so past-year birthdays and next-year rolled holidays
     * always glow as gold milestone stars on the 365-dot Year Matrix.
     */
    fun projectEventDayOfYear(
        eventZdt: ZonedDateTime,
        category: String,
        currentYear: Int
    ): Int? {
        val eventDate = eventZdt.toLocalDate()
        return if (eventDate.year == currentYear) {
            eventDate.dayOfYear
        } else if (isRecurringCategory(category)) {
            val ym = YearMonth.of(currentYear, eventDate.month)
            val safeDay = eventDate.dayOfMonth.coerceAtMost(ym.lengthOfMonth())
            ym.atDay(safeDay).dayOfYear
        } else {
            null
        }
    }

    /**
     * Calculates the "Present Moment (वर्तमान क्षण)" telemetry for the expandable header soul card.
     */
    fun calculatePresentKshanTelemetry(now: ZonedDateTime): PresentKshanTelemetry {
        val localTime = now.toLocalTime()
        val secondsIntoDay = localTime.toSecondOfDay()
        val secondsRemaining = (86400 - secondsIntoDay).coerceAtLeast(0)
        val dayPercent = (secondsIntoDay.toFloat() / 86400f) * 100f

        val month = now.monthValue
        val (rituDev, rituEng, subtitle) = when (month) {
            3, 4 -> Triple("वसंत ऋतु", "Vasanta • Spring", "Blossoming Vernal Equinox Rhythm")
            5, 6 -> Triple("ग्रीष्म ऋतु", "Grishma • High Sun", "Radiant Solstice Zenith")
            7, 8 -> Triple("वर्षा ऋतु", "Varsha • Monsoon", "Atmospheric Renewal & Rain")
            9, 10 -> Triple("शरद ऋतु", "Sharad • Autumn", "Luminous Harvest & Clear Skies")
            11, 12 -> Triple("हेमंत ऋतु", "Hemanta • Pre-Winter", "Crisp Starlight & Amber Twilight")
            else -> Triple("शिशिर ऋतु", "Shishira • Deep Winter", "Still Cosmic Solitude & Frost")
        }

        return PresentKshanTelemetry(
            dayElapsedPercent = dayPercent,
            secondsIntoDay = secondsIntoDay,
            secondsRemainingInDay = secondsRemaining,
            rituNameDevanagari = rituDev,
            rituNameEnglish = rituEng,
            cosmicSubtitle = subtitle
        )
    }

    /**
     * Builds the 3-card Temporal Constellation & Visual Timeline Arc telemetry
     * for the Span (Difference) and Shift (Date Math) screens.
     */
    fun calculateTemporalConstellation(
        start: LocalDate,
        end: LocalDate,
        daysCount: Long,
        today: LocalDate = LocalDate.now()
    ): TemporalConstellationTelemetry {
        val minDate = if (start.isBefore(end)) start else end
        val maxDate = if (start.isBefore(end)) end else start
        val rawSpanDays = ChronoUnit.DAYS.between(minDate, maxDate)

        val isInside = !today.isBefore(minDate) && !today.isAfter(maxDate) && rawSpanDays > 0L
        val progress: Float? = when {
            rawSpanDays == 0L -> if (today == minDate) 0.5f else null
            isInside -> {
                val fromMin = ChronoUnit.DAYS.between(minDate, today).toFloat()
                val ratio = (fromMin / rawSpanDays.toFloat()).coerceIn(0f, 1f)
                if (start.isBefore(end)) ratio else 1f - ratio
            }
            else -> null
        }

        val absDays = abs(daysCount)
        val weeks = absDays / 7L
        val remDays = absDays % 7L
        val totalHours = absDays * 24L
        val totalMinutes = totalHours * 60L

        val humanRhythmPrimary = when {
            absDays == 0L -> "0 Weeks • Instantaneous"
            weeks > 0L && remDays > 0L -> "$weeks ${if (weeks == 1L) "Week" else "Weeks"}, $remDays ${if (remDays == 1L) "Day" else "Days"}"
            weeks > 0L -> "$weeks ${if (weeks == 1L) "Week" else "Weeks"} Exact"
            else -> "$remDays ${if (remDays == 1L) "Day" else "Days"}"
        }
        val humanRhythmSecondary = String.format(Locale.US, "%,d Hours • %,d Minutes", totalHours, totalMinutes)

        val lunarCycles = absDays.toDouble() / 29.53059
        val workWeeks = absDays.toDouble() / 5.0
        val cosmicPrimary = String.format(Locale.US, "%.2f Lunar Cycles", lunarCycles)
        val cosmicSecondary = String.format(Locale.US, "%.1f Standard 5-Day Workweeks", workWeeks)

        val solarOrbitsPercent = (absDays.toDouble() / 365.2425) * 100.0
        val heartbeatsMillions = (absDays.toDouble() * 100_800.0) / 1_000_000.0
        val solarPrimary = String.format(Locale.US, "%.1f%% of a Solar Orbit", solarOrbitsPercent)
        val solarSecondary = if (absDays == 0L) {
            "A single present breath in time"
        } else {
            String.format(Locale.US, "~%.2fM Human Heartbeats", heartbeatsMillions)
        }

        val cards = listOf(
            TemporalContextCardItem(
                title = "HUMAN RHYTHM",
                primaryMetric = humanRhythmPrimary,
                secondaryMetric = humanRhythmSecondary,
                poeticCaption = "Calendar cadence broken into weeks, hours & minutes"
            ),
            TemporalContextCardItem(
                title = "COSMIC & WORK RHYTHM",
                primaryMetric = cosmicPrimary,
                secondaryMetric = cosmicSecondary,
                poeticCaption = "Synodic moon phases (29.53d) & professional sprints"
            ),
            TemporalContextCardItem(
                title = "SOLAR & LIFE PERSPECTIVE",
                primaryMetric = solarPrimary,
                secondaryMetric = solarSecondary,
                poeticCaption = "Earth's orbital arc & human biological pulse (~70 bpm)"
            )
        )

        return TemporalConstellationTelemetry(
            todayProgressAlongArc = progress,
            isTodayInsideSpan = isInside || (rawSpanDays == 0L && today == minDate),
            cards = cards
        )
    }

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
