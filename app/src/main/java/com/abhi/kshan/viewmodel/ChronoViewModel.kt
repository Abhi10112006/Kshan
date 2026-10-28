package com.abhi.kshan.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhi.kshan.data.ChronoDatabase
import com.abhi.kshan.data.ChronoEvent
import com.abhi.kshan.engine.CountdownStatus
import com.abhi.kshan.engine.CountdownTelemetry
import com.abhi.kshan.engine.DifferenceResult
import com.abhi.kshan.engine.PresentKshanTelemetry
import com.abhi.kshan.engine.TimeEngine
import com.abhi.kshan.engine.WidgetTelemetry
import com.abhi.kshan.widget.ChronoWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class ChronoTab {
    DIFFERENCE,
    DATE_MATH,
    COUNTDOWN,
    WIDGETS
}

data class ChronoUiState(
    val currentTab: ChronoTab = ChronoTab.DIFFERENCE,
    val isUtc: Boolean = false,
    val powerSaverEnabled: Boolean = false,
    val triggerCelebration: Boolean = false,
    val hudLogText: String = "Measure actionable working runway, plan sprints, and track meaningful Kshans.",

    // Runway Engine (Tab 1) — defaults diffEnd to +30 Days (Next 30d Sprint) to eliminate dead 0 state
    val diffStart: LocalDate = LocalDate.now(),
    val diffEnd: LocalDate = LocalDate.now().plusDays(30),
    val diffExcludeWeekends: Boolean = false,
    val selectedFocusHoursPerDay: Int = 6,
    val differenceResult: DifferenceResult = TimeEngine.calculateDifference(
        LocalDate.now(),
        LocalDate.now().plusDays(30),
        false
    ),

    // Planner / Sprint Engine (Tab 2)
    val mathBase: LocalDate = LocalDate.now(),
    val mathDays: Long = 14,
    val mathIsAdd: Boolean = true,
    val mathExcludeWeekends: Boolean = false,
    val mathArrivalDate: LocalDate = LocalDate.now().plusDays(14),

    // Kshans / Countdown & Count-Up Engine (Tab 3)
    val countdownTarget: ZonedDateTime = ZonedDateTime.now().plusDays(30),
    val effectiveCountdownTarget: ZonedDateTime = ZonedDateTime.now().plusDays(30),
    val activeEventTitle: String = "Next 30d Sprint Milestone",
    val activeEventCategory: String = "Goal",
    val activeEventRepeatsYearly: Boolean = false,
    val canUseCountUpMode: Boolean = false,
    val overrideCountUpMode: Boolean? = null,
    val isCountUpMode: Boolean = false,
    val isAutoRolled: Boolean = false,
    val heroContextLine: String = "",
    val countdownTelemetry: CountdownTelemetry = CountdownTelemetry(
        days = 30,
        hours = 0,
        minutes = 0,
        seconds = 0,
        status = CountdownStatus.ACTIVE
    ),

    // Present Kshan ("वर्तमान क्षण") & Pulse Telemetry (Tab 4)
    val presentKshanTelemetry: PresentKshanTelemetry = TimeEngine.calculatePresentKshanTelemetry(ZonedDateTime.now()),
    val widgetTelemetry: WidgetTelemetry = TimeEngine.calculateWidgetTelemetry(ZonedDateTime.now())
)

class ChronoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "KshanChronoVM"
    }

    private val db = ChronoDatabase.getInstance(application)
    private val dao = db.chronoDao()

    private val _uiState = MutableStateFlow(ChronoUiState())
    val uiState: StateFlow<ChronoUiState> = _uiState.asStateFlow()

    val savedEvents: StateFlow<List<ChronoEvent>> = dao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedEvent: StateFlow<ChronoEvent?> = dao.getPinnedEvent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        recalcDifference()
        recalcDateMath()
        tickTelemetry()

        viewModelScope.launch {
            pinnedEvent.collect { event ->
                if (event != null) {
                    try {
                        val parsed = ZonedDateTime.parse(event.targetIsoString)
                        _uiState.update {
                            it.copy(
                                countdownTarget = parsed,
                                activeEventTitle = event.title,
                                activeEventCategory = event.category,
                                activeEventRepeatsYearly = event.repeatsYearly,
                                overrideCountUpMode = null
                            )
                        }
                        tickTelemetry()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse pinned event ISO date: ${event.targetIsoString}", e)
                    }
                }
            }
        }
    }

    private fun getZoneId(): ZoneId {
        return if (_uiState.value.isUtc) ZoneId.of("UTC") else ZoneId.systemDefault()
    }

    /**
     * Driven by MainActivity's repeatOnLifecycle(Lifecycle.State.STARTED) loop
     * so the 1-second ticker halts completely when the app is in the background.
     */
    fun tickTelemetry() {
        val now = ZonedDateTime.now(getZoneId())
        val state = _uiState.value
        val rawTarget = state.countdownTarget.withZoneSameInstant(getZoneId())

        val canCountUp = TimeEngine.canUseCountUpMode(
            now = now,
            rawTarget = rawTarget,
            category = state.activeEventCategory
        )

        val sanitizedOverride = if (canCountUp) state.overrideCountUpMode else null

        val effectiveCountUp = if (!canCountUp) {
            false
        } else {
            sanitizedOverride ?: TimeEngine.shouldDefaultToCountUp(
                now = now,
                rawTarget = rawTarget,
                category = state.activeEventCategory,
                repeatsYearly = state.activeEventRepeatsYearly
            )
        }

        val (effectiveTarget, autoRolled) = TimeEngine.resolveEffectiveTarget(
            now = now,
            raw = rawTarget,
            repeatsYearly = state.activeEventRepeatsYearly,
            isCountUpMode = effectiveCountUp
        )

        val telemetry = if (effectiveCountUp) {
            TimeEngine.calculateElapsedSince(now, rawTarget)
        } else {
            TimeEngine.calculateCountdown(now, effectiveTarget)
        }

        val contextLine = TimeEngine.calculateHeroContextLine(
            now = now,
            effectiveTarget = effectiveTarget,
            rawTarget = rawTarget,
            isCountUpMode = effectiveCountUp,
            hoursPerDay = state.selectedFocusHoursPerDay
        )

        val widgets = TimeEngine.calculateWidgetTelemetry(now)
        val presentKshan = TimeEngine.calculatePresentKshanTelemetry(now)

        val shouldCelebrate = !effectiveCountUp &&
                telemetry.status == CountdownStatus.TARGET_REACHED &&
                state.countdownTelemetry.status != CountdownStatus.TARGET_REACHED

        _uiState.update {
            it.copy(
                effectiveCountdownTarget = effectiveTarget,
                canUseCountUpMode = canCountUp,
                overrideCountUpMode = sanitizedOverride,
                isCountUpMode = effectiveCountUp,
                isAutoRolled = autoRolled,
                heroContextLine = contextLine,
                countdownTelemetry = telemetry,
                presentKshanTelemetry = presentKshan,
                widgetTelemetry = widgets,
                triggerCelebration = if (shouldCelebrate) true else it.triggerCelebration
            )
        }
    }

    fun setFocusHoursPerDay(hours: Int) {
        val clamped = hours.coerceIn(1, 16)
        _uiState.update {
            it.copy(
                selectedFocusHoursPerDay = clamped,
                hudLogText = "Focus pace updated to ${clamped}h/day."
            )
        }
        tickTelemetry()
    }

    fun swapDiffDates() {
        val currentStart = _uiState.value.diffStart
        val currentEnd = _uiState.value.diffEnd
        _uiState.update {
            it.copy(
                diffStart = currentEnd,
                diffEnd = currentStart,
                hudLogText = "Swapped Origin and Horizon dates."
            )
        }
        recalcDifference()
    }

    /**
     * 1-Tap Cross-Tab Bridge: Saves a calculated horizon or projected sprint date directly
     * into the Room-backed Kshans list and navigates to the Kshans tab.
     */
    fun saveHorizonOrSprintToKshans(
        title: String,
        targetDate: LocalDate,
        category: String = "Goal"
    ) {
        val targetZdt = targetDate.atTime(LocalTime.of(9, 0)).atZone(getZoneId())
        val repeatsYearly = TimeEngine.defaultRepeatsYearlyForCategory(category)
        viewModelScope.launch {
            val event = ChronoEvent(
                title = title,
                targetIsoString = targetZdt.toString(),
                category = category,
                isPinned = false,
                createdAtIsoString = ZonedDateTime.now(getZoneId()).toString(),
                repeatsYearly = repeatsYearly
            )
            dao.insertEvent(event)
            _uiState.update {
                it.copy(
                    currentTab = ChronoTab.COUNTDOWN,
                    countdownTarget = targetZdt,
                    activeEventTitle = title,
                    activeEventCategory = category,
                    activeEventRepeatsYearly = repeatsYearly,
                    overrideCountUpMode = null,
                    hudLogText = "Saved '$title' to Kshans."
                )
            }
            tickTelemetry()
            ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    /**
     * Opens a Quick Date Jump directly inside Tab 2 (Planner) or Tab 1 (Runway).
     */
    fun openJumpInPlanner(days: Long, label: String) {
        val clamped = days.coerceIn(0L, 9999L)
        _uiState.update {
            it.copy(
                currentTab = ChronoTab.DATE_MATH,
                mathBase = LocalDate.now(),
                mathDays = clamped,
                mathIsAdd = true,
                hudLogText = "Opened $label in Planner."
            )
        }
        recalcDateMath()
    }

    fun openJumpInRunway(targetDate: LocalDate, label: String) {
        _uiState.update {
            it.copy(
                currentTab = ChronoTab.DIFFERENCE,
                diffStart = LocalDate.now(),
                diffEnd = targetDate,
                hudLogText = "Measuring runway to $label."
            )
        }
        recalcDifference()
    }

    fun toggleCountUpMode(enabled: Boolean) {
        val now = ZonedDateTime.now(getZoneId())
        val state = _uiState.value
        val rawTarget = state.countdownTarget.withZoneSameInstant(getZoneId())
        val canCountUp = TimeEngine.canUseCountUpMode(now, rawTarget, state.activeEventCategory)

        if (enabled && !canCountUp) {
            _uiState.update {
                it.copy(
                    overrideCountUpMode = null,
                    hudLogText = "Days Since (+) is only available for past milestones."
                )
            }
            tickTelemetry()
            return
        }

        _uiState.update {
            it.copy(
                overrideCountUpMode = enabled,
                hudLogText = if (enabled) "Switched to Days Since (+) live elapsed chronometer."
                else "Switched to Count Down (−) mode."
            )
        }
        tickTelemetry()
    }

    fun capturePresentKshan(customTitle: String = "") {
        val now = ZonedDateTime.now(getZoneId())
        val finalTitle = customTitle.trim().ifBlank {
            "Kshan • ${now.format(TimeEngine.SHORT_DATE_FORMATTER)} ${now.format(TimeEngine.TIME_FORMATTER)}"
        }
        viewModelScope.launch {
            val event = ChronoEvent(
                title = finalTitle,
                targetIsoString = now.toString(),
                category = "Kshan",
                isPinned = false,
                createdAtIsoString = now.toString(),
                repeatsYearly = false
            )
            dao.insertEvent(event)
            _uiState.update {
                it.copy(
                    currentTab = ChronoTab.COUNTDOWN,
                    countdownTarget = now,
                    activeEventTitle = finalTitle,
                    activeEventCategory = "Kshan",
                    activeEventRepeatsYearly = false,
                    overrideCountUpMode = null,
                    hudLogText = "Captured auspicious Kshan: '$finalTitle'"
                )
            }
            tickTelemetry()
            ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun selectTab(tab: ChronoTab) {
        val log = when (tab) {
            ChronoTab.DIFFERENCE -> "Runway: Measure working days and deep focus hours between any two moments."
            ChronoTab.DATE_MATH -> "Planner: Project target delivery dates and sprint buffers."
            ChronoTab.COUNTDOWN -> "Kshans: Track future milestones and meaningful life streaks."
            ChronoTab.WIDGETS -> "Pulse: Live daily/quarterly/yearly pace & Nothing OS Home Screen Widget Studio."
        }
        _uiState.update { it.copy(currentTab = tab, hudLogText = log) }
    }

    fun setTimezoneUtc(isUtc: Boolean) {
        _uiState.update {
            it.copy(
                isUtc = isUtc,
                hudLogText = if (isUtc) "Switched to UTC (Coordinated Universal Time)."
                else "Switched to your local system time."
            )
        }
        tickTelemetry()
    }

    fun togglePowerSaver(enabled: Boolean) {
        _uiState.update {
            it.copy(
                powerSaverEnabled = enabled,
                hudLogText = if (enabled) "Battery Saver on: background particle animations paused."
                else "Battery Saver off: full animated visual effects active."
            )
        }
    }

    fun clearCelebration() {
        _uiState.update { it.copy(triggerCelebration = false) }
    }

    fun triggerManualCelebration() {
        _uiState.update { it.copy(triggerCelebration = true) }
    }

    // --- Runway (Difference) Engine ---
    fun setDiffStart(date: LocalDate) {
        _uiState.update { it.copy(diffStart = date) }
        recalcDifference()
    }

    fun setDiffEnd(date: LocalDate) {
        _uiState.update { it.copy(diffEnd = date) }
        recalcDifference()
    }

    fun toggleDiffWeekends(exclude: Boolean) {
        _uiState.update {
            it.copy(
                diffExcludeWeekends = exclude,
                hudLogText = if (exclude) "Skipping weekends: counting Monday through Friday only."
                else "Counting all calendar days including weekends."
            )
        }
        recalcDifference()
    }

    private fun recalcDifference() {
        val s = _uiState.value
        val res = TimeEngine.calculateDifference(s.diffStart, s.diffEnd, s.diffExcludeWeekends)
        _uiState.update { it.copy(differenceResult = res) }
    }

    // --- Planner (Date Math) Engine ---
    fun setMathBase(date: LocalDate) {
        _uiState.update { it.copy(mathBase = date) }
        recalcDateMath()
    }

    fun setMathDays(days: Long) {
        _uiState.update { it.copy(mathDays = days.coerceIn(0L, 9999L)) }
        recalcDateMath()
    }

    fun setMathOperation(isAdd: Boolean) {
        _uiState.update {
            it.copy(
                mathIsAdd = isAdd,
                hudLogText = if (isAdd) "Projecting sprint forward into the future."
                else "Projecting backward into the past."
            )
        }
        recalcDateMath()
    }

    fun toggleMathWeekends(exclude: Boolean) {
        _uiState.update {
            it.copy(
                mathExcludeWeekends = exclude,
                hudLogText = if (exclude) "Counting working days only (skipping weekends)."
                else "Counting standard calendar days."
            )
        }
        recalcDateMath()
    }

    private fun recalcDateMath() {
        val s = _uiState.value
        val res = TimeEngine.calculateDateMath(
            base = s.mathBase,
            days = s.mathDays,
            isAdd = s.mathIsAdd,
            excludeWeekends = s.mathExcludeWeekends
        )
        _uiState.update { it.copy(mathArrivalDate = res) }
    }

    // --- Kshans (Countdown) Engine ---
    fun setCountdownTarget(
        target: ZonedDateTime,
        title: String = "Custom Event",
        category: String = "Goal",
        repeatsYearly: Boolean = TimeEngine.defaultRepeatsYearlyForCategory(category)
    ) {
        _uiState.update {
            it.copy(
                countdownTarget = target,
                activeEventTitle = title,
                activeEventCategory = category,
                activeEventRepeatsYearly = repeatsYearly,
                overrideCountUpMode = null,
                hudLogText = "Tracking milestone: $title"
            )
        }
        tickTelemetry()
    }

    fun addNewEvent(
        title: String,
        target: ZonedDateTime,
        category: String,
        repeatsYearly: Boolean = TimeEngine.defaultRepeatsYearlyForCategory(category)
    ) {
        viewModelScope.launch {
            val event = ChronoEvent(
                title = title,
                targetIsoString = target.toString(),
                category = category,
                isPinned = false,
                createdAtIsoString = ZonedDateTime.now().toString(),
                repeatsYearly = repeatsYearly
            )
            dao.insertEvent(event)
            _uiState.update {
                it.copy(
                    countdownTarget = target,
                    activeEventTitle = title,
                    activeEventCategory = category,
                    activeEventRepeatsYearly = repeatsYearly,
                    overrideCountUpMode = null,
                    hudLogText = "Added new Kshan: '$title'"
                )
            }
            tickTelemetry()
            ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun pinEvent(event: ChronoEvent) {
        viewModelScope.launch {
            dao.clearPinnedStatus()
            dao.setPinnedEvent(event.id)
            try {
                val target = ZonedDateTime.parse(event.targetIsoString)
                setCountdownTarget(target, event.title, event.category, event.repeatsYearly)
                _uiState.update {
                    it.copy(hudLogText = "'${event.title}' pinned to your Home Screen widget!")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse targetIsoString when pinning '${event.title}': ${event.targetIsoString}", e)
            }
            ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun selectActiveEvent(event: ChronoEvent) {
        try {
            val target = ZonedDateTime.parse(event.targetIsoString)
            setCountdownTarget(target, event.title, event.category, event.repeatsYearly)
            _uiState.update {
                it.copy(hudLogText = "Focused on '${event.title}' (${event.category})")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse targetIsoString when selecting '${event.title}': ${event.targetIsoString}", e)
        }
    }

    fun deleteEvent(event: ChronoEvent) {
        viewModelScope.launch {
            dao.deleteEvent(event)
            val nextEvent = dao.getPinnedEventDirect() ?: dao.getNextUpcomingEventDirect()
            if (nextEvent != null) {
                try {
                    val nextTarget = ZonedDateTime.parse(nextEvent.targetIsoString)
                    _uiState.update {
                        it.copy(
                            countdownTarget = nextTarget,
                            activeEventTitle = nextEvent.title,
                            activeEventCategory = nextEvent.category,
                            activeEventRepeatsYearly = nextEvent.repeatsYearly,
                            overrideCountUpMode = null,
                            hudLogText = "Removed '${event.title}'. Switched to '${nextEvent.title}'."
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse nextEvent targetIsoString '${nextEvent.title}': ${nextEvent.targetIsoString}", e)
                }
            } else {
                _uiState.update {
                    it.copy(
                        activeEventTitle = "No active Kshans",
                        activeEventCategory = "Goal",
                        activeEventRepeatsYearly = false,
                        overrideCountUpMode = null,
                        hudLogText = "Removed '${event.title}'. No Kshans remaining."
                    )
                }
            }
            tickTelemetry()
            ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun setHudLog(text: String) {
        _uiState.update { it.copy(hudLogText = text) }
    }
}
