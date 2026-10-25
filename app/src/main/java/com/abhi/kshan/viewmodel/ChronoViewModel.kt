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
    val hudLogText: String = "Tap any date to calculate differences, shifts, or live countdowns.",

    // Difference Engine
    val diffStart: LocalDate = LocalDate.now(),
    val diffEnd: LocalDate = LocalDate.now(),
    val diffExcludeWeekends: Boolean = false,
    val differenceResult: DifferenceResult = DifferenceResult(0, "Same day", true, false),

    // Date Math Engine
    val mathBase: LocalDate = LocalDate.now(),
    val mathDays: Long = 7,
    val mathIsAdd: Boolean = true,
    val mathExcludeWeekends: Boolean = false,
    val mathArrivalDate: LocalDate = LocalDate.now().plusDays(7),

    // Countdown & Count-Up Engine
    val countdownTarget: ZonedDateTime = ZonedDateTime.now().plusDays(30),
    val effectiveCountdownTarget: ZonedDateTime = ZonedDateTime.now().plusDays(30),
    val activeEventTitle: String = "New Year's Celebration",
    val activeEventCategory: String = "Holiday",
    val activeEventRepeatsYearly: Boolean = true,
    val canUseCountUpMode: Boolean = false,
    val overrideCountUpMode: Boolean? = null,
    val isCountUpMode: Boolean = false,
    val isAutoRolled: Boolean = false,
    val countdownTelemetry: CountdownTelemetry = CountdownTelemetry(
        days = 30,
        hours = 0,
        minutes = 0,
        seconds = 0,
        status = CountdownStatus.ACTIVE
    ),

    // Present Kshan ("वर्तमान क्षण") & Astronomical Telemetry
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

        // If the event is strictly in the future (!canCountUp), reset overrideCountUpMode = null
        // so it can never get stuck in Days Since mode after an edit or target update.
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
                countdownTelemetry = telemetry,
                presentKshanTelemetry = presentKshan,
                widgetTelemetry = widgets,
                triggerCelebration = if (shouldCelebrate) true else it.triggerCelebration
            )
        }
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
            ChronoTab.DIFFERENCE -> "Find out exactly how many days and months are between two dates."
            ChronoTab.DATE_MATH -> "Add or subtract days to calculate past or future dates."
            ChronoTab.COUNTDOWN -> "Live ticking countdowns & Days Since chronometers for your milestones."
            ChronoTab.WIDGETS -> "Helpful widgets including 365-Dot Year Matrix and your Home Screen companion."
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

    // --- Difference Engine ---
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

    // --- Date Math Engine ---
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
                hudLogText = if (isAdd) "Adding days forward into the future."
                else "Subtracting days backward into the past."
            )
        }
        recalcDateMath()
    }

    fun toggleMathWeekends(exclude: Boolean) {
        _uiState.update {
            it.copy(
                mathExcludeWeekends = exclude,
                hudLogText = if (exclude) "Adding/subtracting working days only (skipping weekends)."
                else "Adding/subtracting standard calendar days."
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

    // --- Countdown Engine ---
    fun setCountdownTarget(
        target: ZonedDateTime,
        title: String = "Custom Event",
        category: String = "Celebration",
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
                    hudLogText = "Added new milestone: '$title'"
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
                        activeEventTitle = "No active countdowns",
                        activeEventCategory = "Celebration",
                        activeEventRepeatsYearly = false,
                        overrideCountUpMode = null,
                        hudLogText = "Removed '${event.title}'. No countdowns remaining."
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
