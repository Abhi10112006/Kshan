package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChronoDatabase
import com.example.data.ChronoEvent
import com.example.data.ChronoHistory
import com.example.engine.CountdownStatus
import com.example.engine.CountdownTelemetry
import com.example.engine.DifferenceResult
import com.example.engine.TimeEngine
import com.example.engine.WidgetTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

    // Countdown Engine
    val countdownTarget: ZonedDateTime = ZonedDateTime.now().plusDays(30),
    val activeEventTitle: String = "New Year's Celebration",
    val activeEventCategory: String = "Holiday",
    val countdownTelemetry: CountdownTelemetry = CountdownTelemetry(
        days = 30,
        hours = 0,
        minutes = 0,
        seconds = 0,
        status = CountdownStatus.ACTIVE
    ),

    // Astronomical & Widget Telemetry
    val widgetTelemetry: WidgetTelemetry = TimeEngine.calculateWidgetTelemetry(ZonedDateTime.now())
)

class ChronoViewModel(application: Application) : AndroidViewModel(application) {

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

        viewModelScope.launch {
            pinnedEvent.collect { event ->
                if (event != null) {
                    try {
                        val parsed = ZonedDateTime.parse(event.targetIsoString)
                        _uiState.update {
                            it.copy(
                                countdownTarget = parsed,
                                activeEventTitle = event.title,
                                activeEventCategory = event.category
                            )
                        }
                    } catch (e: Exception) {
                        // Fallback
                    }
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                tickTelemetry()
            }
        }
    }

    private fun getZoneId(): ZoneId {
        return if (_uiState.value.isUtc) ZoneId.of("UTC") else ZoneId.systemDefault()
    }

    private fun tickTelemetry() {
        val now = ZonedDateTime.now(getZoneId())
        val target = _uiState.value.countdownTarget.withZoneSameInstant(getZoneId())
        val telemetry = TimeEngine.calculateCountdown(now, target)
        val widgets = TimeEngine.calculateWidgetTelemetry(now)

        val shouldCelebrate = telemetry.status == CountdownStatus.TARGET_REACHED &&
                _uiState.value.countdownTelemetry.status != CountdownStatus.TARGET_REACHED

        _uiState.update {
            it.copy(
                countdownTelemetry = telemetry,
                widgetTelemetry = widgets,
                triggerCelebration = if (shouldCelebrate) true else it.triggerCelebration
            )
        }
    }

    fun selectTab(tab: ChronoTab) {
        val log = when (tab) {
            ChronoTab.DIFFERENCE -> "Find out exactly how many days and months are between two dates."
            ChronoTab.DATE_MATH -> "Add or subtract days to calculate past or future dates."
            ChronoTab.COUNTDOWN -> "Live ticking countdowns to your important events and goals."
            ChronoTab.WIDGETS -> "Helpful widgets including Year Progress and your Home Screen companion."
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
        _uiState.update { it.copy(mathDays = days.coerceAtLeast(0)) }
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
    fun setCountdownTarget(target: ZonedDateTime, title: String = "Custom Event", category: String = "Celebration") {
        _uiState.update {
            it.copy(
                countdownTarget = target,
                activeEventTitle = title,
                activeEventCategory = category,
                hudLogText = "Tracking countdown for: $title"
            )
        }
        tickTelemetry()
    }

    fun addNewEvent(title: String, target: ZonedDateTime, category: String) {
        viewModelScope.launch {
            val event = ChronoEvent(
                title = title,
                targetIsoString = target.toString(),
                category = category,
                isPinned = false,
                createdAtIsoString = ZonedDateTime.now().toString()
            )
            dao.insertEvent(event)
            _uiState.update {
                it.copy(
                    countdownTarget = target,
                    activeEventTitle = title,
                    activeEventCategory = category,
                    hudLogText = "Added new countdown: '$title'"
                )
            }
            tickTelemetry()
            com.example.widget.ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun pinEvent(event: ChronoEvent) {
        viewModelScope.launch {
            dao.clearPinnedStatus()
            dao.setPinnedEvent(event.id)
            try {
                val target = ZonedDateTime.parse(event.targetIsoString)
                setCountdownTarget(target, event.title, event.category)
                _uiState.update {
                    it.copy(hudLogText = "'${event.title}' pinned to your Home Screen widget!")
                }
            } catch (e: Exception) {}
            com.example.widget.ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun selectActiveEvent(event: ChronoEvent) {
        try {
            val target = ZonedDateTime.parse(event.targetIsoString)
            setCountdownTarget(target, event.title, event.category)
            _uiState.update {
                it.copy(hudLogText = "Focused on '${event.title}' (${event.category})")
            }
        } catch (e: Exception) {}
    }

    fun deleteEvent(event: ChronoEvent) {
        viewModelScope.launch {
            dao.deleteEvent(event)
            // If the deleted event was active, check for the next upcoming event
            val nextEvent = dao.getPinnedEventDirect() ?: dao.getNextUpcomingEventDirect()
            if (nextEvent != null) {
                try {
                    val nextTarget = ZonedDateTime.parse(nextEvent.targetIsoString)
                    _uiState.update {
                        it.copy(
                            countdownTarget = nextTarget,
                            activeEventTitle = nextEvent.title,
                            activeEventCategory = nextEvent.category,
                            hudLogText = "Removed '${event.title}'. Switched to '${nextEvent.title}'."
                        )
                    }
                } catch (e: Exception) {}
            } else {
                _uiState.update {
                    it.copy(
                        activeEventTitle = "No active countdowns",
                        activeEventCategory = "Celebration",
                        hudLogText = "Removed '${event.title}'. No countdowns remaining."
                    )
                }
            }
            tickTelemetry()
            com.example.widget.ChronoWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun setHudLog(text: String) {
        _uiState.update { it.copy(hudLogText = text) }
    }
}
