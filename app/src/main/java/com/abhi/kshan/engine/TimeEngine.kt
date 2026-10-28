package com.abhi.kshan.engine

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.MonthDay
import java.time.Period
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
    TODAY_CELEBRATION,
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

data class CompactMetricsStrip(
    val weeks: Long,
    val remainingDays: Long,
    val workdays: Long,
    val totalHours: Long,
    val weeksAndDaysText: String,
    val workdaysText: String,
    val hoursText: String,
    val formattedLine: String
)

data class RunwayFocusMetrics(
    val totalCalendarDays: Long,
    val workingDays: Long,
    val weekendDaysOff: Long,
    val weeks: Long,
    val remainingDays: Long,
    val weeksAndDaysText: String,
    val focusHours: Long,
    val hoursPerDay: Int,
    val topRowText: String,
    val focusLineText: String
)

data class PlannerBufferMetrics(
    val totalCalendarDays: Long,
    val workingDays: Long,
    val weekendBufferDays: Long,
    val formattedSummary: String
)

data class TemporalContextCardItem(
    val title: String,
    val primaryMetric: String,
    val secondaryMetric: String,
    val poeticCaption: String
)

data class TemporalConstellationTelemetry(
    val todayProgressAlongArc: Float?,
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

    private val YEARLY_DEFAULT_CATEGORIES = setOf("birthday", "holiday", "anniversary")
    private val STREAK_MILESTONES = longArrayOf(7L, 14L, 21L, 30L, 50L, 75L, 100L, 150L, 200L, 300L, 365L, 500L, 730L, 1000L)

    /**
     * Returns true if a category defaults to `repeatsYearly = true` when creating a new event.
     * Strictly matches Birthday, Holiday, and Anniversary.
     */
    fun defaultRepeatsYearlyForCategory(category: String): Boolean {
        val lower = category.trim().lowercase(Locale.US)
        return lower in YEARLY_DEFAULT_CATEGORIES
    }

    fun isRecurringCategory(category: String): Boolean = defaultRepeatsYearlyForCategory(category)

    /**
     * Returns true ONLY when the event has a past/present origin timestamp or is a captured Kshan.
     * Strictly future events cannot be toggled to "+ Days Since" mode.
     */
    fun canUseCountUpMode(
        now: ZonedDateTime,
        rawTarget: ZonedDateTime,
        category: String = ""
    ): Boolean {
        if (category.equals("Kshan", ignoreCase = true)) return true
        return !rawTarget.toInstant().isAfter(now.toInstant())
    }

    /**
     * Determines whether an event should automatically default to Days Since (+) mode
     * when selected, unless the user explicitly overrides the mode pill.
     */
    fun shouldDefaultToCountUp(
        now: ZonedDateTime,
        rawTarget: ZonedDateTime,
        category: String,
        repeatsYearly: Boolean = defaultRepeatsYearlyForCategory(category)
    ): Boolean {
        if (category.equals("Kshan", ignoreCase = true)) return true
        return rawTarget.toLocalDate().isBefore(now.toLocalDate()) && !repeatsYearly
    }

    /**
     * Resolves the effective target ZonedDateTime and whether an annual recurring event
     * was rolled forward to its current or next upcoming occurrence.
     */
    fun resolveEffectiveTarget(
        now: ZonedDateTime,
        raw: ZonedDateTime,
        repeatsYearly: Boolean,
        isCountUpMode: Boolean
    ): Pair<ZonedDateTime, Boolean> {
        val today = now.toLocalDate()
        if (isCountUpMode || !repeatsYearly || !raw.toLocalDate().isBefore(today)) {
            return raw to false
        }
        val md = MonthDay.from(raw)
        var d = md.atYear(today.year)
        if (d.isBefore(today)) d = md.atYear(today.year + 1)
        return ZonedDateTime.of(d, raw.toLocalTime(), raw.zone) to true
    }

    // Backward-compatible overload delegating category -> defaultRepeatsYearlyForCategory
    fun resolveEffectiveTarget(
        now: ZonedDateTime,
        rawTarget: ZonedDateTime,
        category: String,
        isCountUpMode: Boolean
    ): Pair<ZonedDateTime, Boolean> = resolveEffectiveTarget(
        now = now,
        raw = rawTarget,
        repeatsYearly = defaultRepeatsYearlyForCategory(category),
        isCountUpMode = isCountUpMode
    )

    /**
     * Calculates live elapsed time since a past or captured Kshan milestone (`+ Days Since`).
     * Never uses `abs()` to turn a future date into fake elapsed days.
     */
    fun calculateElapsedSince(now: ZonedDateTime, origin: ZonedDateTime): CountdownTelemetry {
        val nowInstant = now.toInstant()
        val originInstant = origin.toInstant()
        if (originInstant.isAfter(nowInstant)) {
            return CountdownTelemetry(
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                status = CountdownStatus.AWAITING
            )
        }
        val totalSeconds = Duration.between(originInstant, nowInstant).seconds.coerceAtLeast(0L)
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
     * Projects any saved ChronoEvent onto `currentYear`'s 1..365/366 day index.
     */
    fun projectEventDayOfYear(
        eventZdt: ZonedDateTime,
        repeatsYearly: Boolean,
        currentYear: Int
    ): Int? {
        val eventDate = eventZdt.toLocalDate()
        return if (eventDate.year == currentYear) {
            eventDate.dayOfYear
        } else if (repeatsYearly) {
            MonthDay.from(eventDate).atYear(currentYear).dayOfYear
        } else {
            null
        }
    }

    fun projectEventDayOfYear(
        eventZdt: ZonedDateTime,
        category: String,
        currentYear: Int
    ): Int? = projectEventDayOfYear(
        eventZdt = eventZdt,
        repeatsYearly = defaultRepeatsYearlyForCategory(category),
        currentYear = currentYear
    )

    /**
     * Computes the sleek single-line 3-Metric Strip for backward compatibility & tests:
     * - `weeks = totalDays / 7` and `remDays = totalDays % 7`
     * - `workdays = calculateBusinessDaysBetween(minDate, maxDate)`
     * - `hours = totalDays * 24`
     */
    fun calculateCompactMetricsStrip(
        start: LocalDate,
        end: LocalDate,
        totalDays: Long = abs(ChronoUnit.DAYS.between(start, end))
    ): CompactMetricsStrip {
        val absDays = abs(totalDays)
        val weeks = absDays / 7L
        val remDays = absDays % 7L
        val minDate = if (start.isBefore(end)) start else end
        val maxDate = if (start.isBefore(end)) end else start
        val workdays = calculateBusinessDaysBetween(minDate, maxDate)
        val totalHours = absDays * 24L

        val weeksAndDaysText = "${weeks}w ${remDays}d"
        val workdaysText = "$workdays ${if (workdays == 1L) "Workday" else "Workdays"}"
        val hoursText = "$totalHours ${if (totalHours == 1L) "Hour" else "Hours"}"
        val formattedLine = "$weeksAndDaysText  •  $workdaysText  •  $hoursText"

        return CompactMetricsStrip(
            weeks = weeks,
            remainingDays = remDays,
            workdays = workdays,
            totalHours = totalHours,
            weeksAndDaysText = weeksAndDaysText,
            workdaysText = workdaysText,
            hoursText = hoursText,
            formattedLine = formattedLine
        )
    }

    /**
     * Actionable Runway & Focus Budget Calculator for Tab 1 (Runway):
     * Computes exact working days, weekend days off, weeks + days, and deep focus hours
     * at the user's chosen daily focus pace (`4h/day`, `6h/day`, `8h/day`).
     */
    fun calculateRunwayFocusMetrics(
        start: LocalDate,
        end: LocalDate,
        hoursPerDay: Int = 6
    ): RunwayFocusMetrics {
        val minDate = if (start.isBefore(end)) start else end
        val maxDate = if (start.isBefore(end)) end else start
        val totalCalendarDays = ChronoUnit.DAYS.between(minDate, maxDate).coerceAtLeast(0L)
        val workingDays = calculateBusinessDaysBetween(minDate, maxDate)
        val weekendDaysOff = (totalCalendarDays - workingDays).coerceAtLeast(0L)
        val weeks = totalCalendarDays / 7L
        val remainingDays = totalCalendarDays % 7L
        val clampedPace = hoursPerDay.coerceIn(1, 16)
        val focusHours = workingDays * clampedPace.toLong()

        val weeksAndDaysText = "${weeks}w ${remainingDays}d"
        val workingLabel = if (workingDays == 1L) "1 Working Day" else "$workingDays Working Days"
        val weekendLabel = if (weekendDaysOff == 1L) "1 Weekend Day Off" else "$weekendDaysOff Weekends Off"
        val topRowText = "$weeksAndDaysText  •  $workingLabel  •  $weekendLabel"
        val focusLineText = "= $focusHours Deep Focus Hours (at ${clampedPace}h/day)"

        return RunwayFocusMetrics(
            totalCalendarDays = totalCalendarDays,
            workingDays = workingDays,
            weekendDaysOff = weekendDaysOff,
            weeks = weeks,
            remainingDays = remainingDays,
            weeksAndDaysText = weeksAndDaysText,
            focusHours = focusHours,
            hoursPerDay = clampedPace,
            topRowText = topRowText,
            focusLineText = focusLineText
        )
    }

    /**
     * Computes the exact working days + weekend buffer days breakdown for Tab 2 (Planner):
     * e.g., "12 Working Days + 4 Weekend Buffer Days (16 Calendar Days total)"
     */
    fun calculatePlannerBufferMetrics(
        base: LocalDate,
        arrival: LocalDate
    ): PlannerBufferMetrics {
        val minDate = if (base.isBefore(arrival)) base else arrival
        val maxDate = if (base.isBefore(arrival)) arrival else base
        val totalCalendarDays = ChronoUnit.DAYS.between(minDate, maxDate).coerceAtLeast(0L)
        val workingDays = calculateBusinessDaysBetween(minDate, maxDate)
        val weekendBufferDays = (totalCalendarDays - workingDays).coerceAtLeast(0L)

        val workStr = if (workingDays == 1L) "1 Working Day" else "$workingDays Working Days"
        val bufStr = if (weekendBufferDays == 1L) "1 Weekend Buffer Day" else "$weekendBufferDays Weekend Buffer Days"
        val calStr = if (totalCalendarDays == 1L) "1 Calendar Day total" else "$totalCalendarDays Calendar Days total"

        return PlannerBufferMetrics(
            totalCalendarDays = totalCalendarDays,
            workingDays = workingDays,
            weekendBufferDays = weekendBufferDays,
            formattedSummary = "$workStr + $bufStr ($calStr)"
        )
    }

    /**
     * Computes the Real-Life Context Line inside the Hero Card on Tab 3 (Kshans):
     * - In `Count Down (−)` mode: `"32 Working Days left  •  192h at 6h/day"`
     * - In `Days Since (+)` mode: `"Streak Milestone: Next 100d mark in 14 days"`
     */
    fun calculateHeroContextLine(
        now: ZonedDateTime,
        effectiveTarget: ZonedDateTime,
        rawTarget: ZonedDateTime,
        isCountUpMode: Boolean,
        hoursPerDay: Int = 6
    ): String {
        val today = now.toLocalDate()
        val pace = hoursPerDay.coerceIn(1, 16)
        return if (isCountUpMode) {
            val elapsedDays = ChronoUnit.DAYS.between(rawTarget.toLocalDate(), today).coerceAtLeast(0L)
            val nextMilestone = STREAK_MILESTONES.firstOrNull { it > elapsedDays }
                ?: (((elapsedDays / 100L) + 1L) * 100L)
            val daysToMilestone = (nextMilestone - elapsedDays).coerceAtLeast(1L)
            "Streak Milestone: Next ${nextMilestone}d mark in $daysToMilestone ${if (daysToMilestone == 1L) "day" else "days"}"
        } else {
            val targetDate = effectiveTarget.toLocalDate()
            if (!targetDate.isAfter(today)) {
                val remSec = Duration.between(now.toInstant(), effectiveTarget.toInstant()).seconds.coerceAtLeast(0L)
                val remHrs = remSec / 3600L
                val remMins = (remSec % 3600L) / 60L
                "Today's Final Runway  •  ${remHrs}h ${remMins}m remaining"
            } else {
                val workingDays = calculateBusinessDaysBetween(today, targetDate)
                val focusHours = workingDays * pace.toLong()
                val workLabel = if (workingDays == 1L) "1 Working Day left" else "$workingDays Working Days left"
                "$workLabel  •  ${focusHours}h at ${pace}h/day"
            }
        }
    }

    /**
     * Computes Today's Live Waking Runway for when `From == To` on the Runway tab.
     */
    fun formatTodayWakingRunway(secondsRemainingInDay: Int): String {
        val remSecs = secondsRemainingInDay.coerceAtLeast(0)
        val hours = remSecs / 3600
        val mins = (remSecs % 3600) / 60
        return "Today has ${hours}h ${mins}m of waking runway left"
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
     * Builds the Temporal Constellation & Life Context metrics.
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

        val strip = calculateCompactMetricsStrip(start, end, daysCount)
        val absDays = abs(daysCount)
        val totalMinutes = strip.totalHours * 60L

        val humanRhythmPrimary = strip.weeksAndDaysText
        val humanRhythmSecondary = String.format(Locale.US, "%,d Hours • %,d Minutes", strip.totalHours, totalMinutes)

        val lunarCycles = absDays.toDouble() / 29.53059
        val cosmicPrimary = String.format(Locale.US, "%.2f Lunar Cycles", lunarCycles)
        val cosmicSecondary = strip.workdaysText

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
                poeticCaption = "Synodic moon phases (29.53d) & Monday-Friday workdays"
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

    fun calculateBusinessDaysBetween(start: LocalDate, end: LocalDate): Long {
        val minDate = if (start.isBefore(end)) start else end
        val maxDate = if (start.isBefore(end)) end else start
        var count = 0L
        var current = minDate
        while (current.isBefore(maxDate)) {
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

    /**
     * Calculates live countdown telemetry.
     */
    fun calculateCountdown(now: ZonedDateTime, target: ZonedDateTime): CountdownTelemetry {
        val nowInstant = now.toInstant()
        val targetInstant = target.toInstant()

        if (target.toLocalDate() == now.toLocalDate()) {
            val remSeconds = Duration.between(nowInstant, targetInstant).seconds.coerceAtLeast(0L)
            val hours = (remSeconds % 86400L) / 3600L
            val minutes = (remSeconds % 3600L) / 60L
            val seconds = remSeconds % 60L
            return CountdownTelemetry(
                days = 0,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                status = CountdownStatus.TODAY_CELEBRATION
            )
        }

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

        // 6. Quick Date Jumps (2x2 Bento Grid)
        val endOfYear = LocalDate.of(year, 12, 31)
        val jumps = listOf(
            QuickJumpItem("+30d Sprint", localDate.plusDays(30), 30),
            QuickJumpItem("+90d Quarter", localDate.plusDays(90), 90),
            QuickJumpItem("End of Q$quarter", endOfQuarter, ChronoUnit.DAYS.between(localDate, endOfQuarter).toInt().coerceAtLeast(1)),
            QuickJumpItem("End of Year", endOfYear, ChronoUnit.DAYS.between(localDate, endOfYear).toInt().coerceAtLeast(1))
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
