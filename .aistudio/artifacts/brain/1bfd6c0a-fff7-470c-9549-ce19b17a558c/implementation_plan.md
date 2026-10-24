# Sculptural Time Companion Upgrades for क्षण (Kshan)

Elevate **क्षण (Kshan)** from a utility date calculator into a tactile, sculptural precision-time instrument featuring a mechanical scrubbing ruler with true physical tape-ruler polarity, cosmic life-context telemetry, a floating *Present Moment (वर्तमान क्षण)* header overlay, an animated **✦ Vision** showcase for **Abhinav Yaduvanshi**, smart per-event *Count Down / Days Since* chronometers with holiday auto-rollover, and a 365-Dot Matrix Year Grid with annual milestone projection.

## User Review & Critical Decisions

> [!IMPORTANT]
> All Phase 1 decisions and all 5 logic, gesture, and UX refinements have been locked into this revised specification:

- **Confirmed Decision 1 — `✦ Vision` Header Pill (Abhinav Yaduvanshi Creator & Vision Studio)**: Renamed the top-right header badge from `Pro` to **`✦ Vision`** (with a gold-to-cyan specular border glow) so it never looks like a paywall. Tapping it opens an interactive **Creator & Vision Studio** window crafted for **Abhinav Yaduvanshi**, featuring kinetic orbital particle rings, spring-animated philosophy cards, interactive touch-reactive constellations, and the story behind *क्षण (Kshan)*.
- **Confirmed Decision 2 — Per-Event Smart Default + Manual Override (`Days Since +` vs. `Count Down −`) & Auto-Rollover**: Rather than a sticky global toggle that confuses future events when switching, the Hero Timepiece automatically derives its default mode per selected event:
  - Defaults to **`Days Since (+)`** when `category == "Kshan"` or when a non-recurring event is in the past (`target.isBefore(now)`).
  - Defaults to **`Count Down (−)`** when the event is in the future or is a recurring Holiday/Birthday/Anniversary/Festival (which automatically rolls forward to its next upcoming annual occurrence).
  - Users can still manually tap the **`− Count Down | + Days Since`** toggle pill at any time to override the active event's view (e.g., inspecting total years/days elapsed since an original birthday or milestone).
- **Confirmed Decision 3 — True Physical Tape-Ruler Polarity & Gesture Isolation**: `MechanicalScrubRuler` uses `Modifier.pointerInput(...) { detectHorizontalDragGestures { change, dragAmount -> change.consume(); ... } }` to strictly consume horizontal drags before any parent container sees them. Dragging the thumb **left** (`dragAmount < 0`) pulls higher numbers under the center indicator needle (**`+1 day`**), and dragging **right** (`dragAmount > 0`) pulls lower numbers under the needle (**`−1 day`**), matching a physical Leica dial or tape measure.
- **Confirmed Decision 4 — Floating `PresentKshanDropdownCard` Overlay (`zIndex(10f)`)**: Tapping the top-left `क्षण` squircle renders `PresentKshanDropdownCard` as a floating `Box` overlay (`zIndex(10f)`) with a tap-outside dismiss scrim and spring slide+fade transition anchored below the header—never pushing or squishing the screen layout underneath.
- **Confirmed Decision 5 — Annual Milestone Projection onto the 365-Dot Year Matrix**: `YearDotMatrixGrid` projects recurring categories (`Birthday`, `Holiday`, `Anniversary`, `Festival`, `Personal`) onto `currentYear` using `MonthDay` safe leap-year mapping (`YearMonth.of(currentYear, month).atDay(day.coerceAtMost(maxDay)).dayOfYear`), ensuring birthdays from past years (e.g., 2006) and holidays rolled into next year (e.g., 2027) always shine as interactive gold stars on the current year's 365-dot grid.

---

## 1. Overview & Core Concept

- **What It Does**: Transforms every static surface of *क्षण (Kshan)* into a tactile horological instrument:
  1. **Mechanical Time Wheel / Scrubbing Ruler**: Thumb-driven horizontal chronometer tape between the date cards and the hero result with isolated horizontal drag consumption, physical tape polarity (drag left = `+1d`, drag right = `−1d`), and per-tick `TextHandleMove` haptics.
  2. **Temporal Constellation & Visual Timeline Arc**: Replaces empty space around the hero number with a glowing curved timeline arc (plotting `Start`, `End`, and a live pulsing `Today` marker) plus a swipeable 3-card **Human Rhythm** micro-carousel (*Weeks + Hours*, *Lunar Cycles + Workweeks*, *Heartbeats + Year Share*).
  3. **Soul of क्षण (Floating `वर्तमान क्षण` Overlay & `✦ Vision` Studio)**: Tapping the `क्षण` squircle floats the *Present Moment* glass overlay over the current tab (live Today % progress, Indian *Ritu / ऋतु* season, and 1-tap *"Capture This Kshan"* memory logger); tapping the **`✦ Vision`** badge launches the animated **Abhinav Yaduvanshi Vision Showcase**.
  4. **Bi-Directional Chronometer (`Count Down −` / `Days Since +`) & Auto-Rollover**: Automatically selects the right mode per event (`Days Since +` for captured Kshans and past non-recurring milestones; `Count Down −` with automatic next-year rollover for recurring holidays/birthdays) while offering a 1-tap manual override pill.
  5. **Interactive 365-Dot Matrix Year Grid**: Adds a Nothing OS / teenage engineering-inspired `Orbital Ring ⇄ 365-Dot Matrix` toggle on the Year & Season Progress card where elapsed days glow cyan, Today pulses white-hot, and both current-year and recurring annual milestones shine as interactive gold stars.
- **Target Audience / Persona**: Designers, engineers, planners, and horology enthusiasts who appreciate tactile hardware-inspired interfaces (Leica camera dials, luxury watch crowns, dot-matrix telemetry).
- **Key Value**: Eliminates dead space and static headers while doubling daily utility with *Days Since* tracking and instant thumb-scrubbing.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Thumb Scrubbing on Span & Shift**: User selects `Start Date` or `End Date` on the ruler target pill (or scrubs `Days to Shift` on the Shift screen) -> drags thumb left or right across luminous millimeter ticks -> `change.consume()` isolates the drag, every 14dp tick crossed fires `HapticFeedbackType.TextHandleMove`, dragging left advances `+1 day` (dragging right steps `−1 day`), and spins the hero counter and timeline arc live.
  2. **Exploring Temporal Context**: Below the hero number, a curved neon arc visually bridges Start and End dates with a glowing `Today` orb. Swiping the 3-card micro-carousel underneath reveals:
     - *Card 1 — Human Rhythm*: `3 Weeks, 5 Days • 624 Hours • 37,440 Mins`
     - *Card 2 — Cosmic & Natural Rhythm*: `0.88 Lunar Cycles • 5.2 Workweeks`
     - *Card 3 — Life & Solar Perspective*: `7.1% of a Solar Year • ~2.69M Heartbeats`
  3. **Capturing a Present Kshan (`वर्तमान क्षण`)**: User taps the top-left `क्षण` squircle -> a floating glass card (`zIndex(10f)`) glides down over the top of the screen with a dim backdrop scrim, showing live daylight/day progress (e.g., `Today Elapsed: 64.28%`), current Indian Ritu (*Sharad Ritu • Autumn*), an optional note field, and a **"Capture This Kshan"** button that saves the current timestamp with `category = "Kshan"` and opens it in Countdowns where it naturally defaults to `Days Since (+)` mode.
  4. **Abhinav Yaduvanshi Vision Showcase (`✦ Vision` Badge)**: User taps the gold-and-cyan **`✦ Vision`** pill -> opens a full-bleed glass showcase modal with an interactive orbital chronometer emblem, animated signature badge for **Abhinav Yaduvanshi**, his vision statement for *क्षण*, interactive kinetic telemetry badges, and spring-physics card reveals.
  5. **Switching `Count Down (−)` / `Days Since (+)` & Year Grid Inspection**: In Countdowns, each selected event automatically resolves its default mode (`Days Since +` for `Kshan` or passed one-off events; `Count Down −` with next-year rollover for recurring holidays/birthdays), and users can tap `− Count Down | + Days Since` to override it. In Widgets & Insights, toggling `365 Dots` renders all 365/366 days of the year with gold star markers for both current-year and projected annual milestones; tapping any dot displays that day's date and milestone details.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Horological Cyber-Sculpture (Leica camera knurling + luxury watch complication + teenage engineering dot-matrix telemetry).
  - *Color Palette & Mood*:
    - `--background`: `#050811` (Deep Obsidian Void)
    - `--surface-glass`: `#0B101E` at 85% opacity with top-lit specular borders
    - `--primary`: `#00F5D4` (Electric Cyan for active ticks, elapsed year dots, and primary arcs)
    - `--secondary`: `#A855F7` (Cosmic Purple for secondary date anchors and Ritu badges)
    - `--accent-gold`: `#FFD700` (Solar Gold for milestone stars on the 365-dot matrix, `✦ Vision` badge highlights, and Present Kshan capture accents)
  - *Typography & Hierarchy*:
    - **Kalam Bold**: Exclusively for the `क्षण` Devanagari glyph and `वर्तमान क्षण` emblem.
    - **Comic Neue (Regular & Bold)**: Expressive titles, philosophy cards, carousel labels, and interactive pills.
    - **Courier Prime (Regular & Bold)**: All ruler tick numbers, live spinning hero counters, `HH:MM:SS` chronometers, percentages, and 365-dot telemetry coordinates.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Isolated Pointer Input & Inverted Tape-Measure Delta on `MechanicalScrubRuler`**:
  - *Chosen Approach*: Call `change.consume()` inside `detectHorizontalDragGestures` and subtract `dragAmount` from `dragAccumulatorPx` (`dragAccumulatorPx -= dragAmount`). Every time `abs(dragAccumulatorPx) >= tickSpacingPx`, emit `steps = (dragAccumulatorPx / tickSpacingPx).toInt()` and subtract `steps * tickSpacingPx` from the accumulator.
  - *Why*: Pulling the tape left (`dragAmount < 0`) yields a positive `dragAccumulatorPx`, incrementing the date (`+1 day`) as higher numbers scroll under the fixed center needle, while consuming all horizontal events prevents any conflict with the 3-card `HorizontalPager` below.
- **Decision 2 — Per-Event Derived Chronometer Mode (`overrideCountUpMode: Boolean? = null`)**:
  - *Chosen Approach*: Store `overrideCountUpMode: Boolean? = null` in `ChronoUiState` (resetting to `null` whenever `selectActiveEvent`, `pinEvent`, or `addNewEvent` is called). Compute effective `isCountUpMode = overrideCountUpMode ?: shouldDefaultToCountUp(eventCategory, target, now)` where `shouldDefaultToCountUp` is `true` for `category.equals("Kshan", ignoreCase = true)` or when `target.isBefore(now)` for non-recurring categories.
  - *Why*: Selecting a future event (like *New Year 2027*) after capturing a *Kshan* immediately displays a proper `Count Down (−)`, while selecting a captured *Kshan* or past launch date automatically displays `Days Since (+)`—and tapping the toggle pill sets `overrideCountUpMode` for manual inspection.
- **Decision 3 — Leap-Safe Annual Milestone Projection on the 365-Dot Matrix**:
  - *Chosen Approach*: When building the `dayOfYear -> List<ChronoEvent>` lookup for `YearDotMatrixGrid`, check if `event.category` is recurring (`Holiday`, `Birthday`, `Anniversary`, `Festival`, `Personal`). If so, project its month and day onto `currentYear` using `YearMonth.of(currentYear, zdt.month).let { ym -> ym.atDay(zdt.dayOfMonth.coerceAtMost(ym.lengthOfMonth())).dayOfYear }`; otherwise, include it if `zdt.year == currentYear`.
  - *Why*: Ensures annual birthdays (even with a 2006 birth year or Feb 29 leap day) and holidays rolled into 2027 always illuminate their corresponding day dot in the current year's 365-dot matrix.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```text
┌────────────────────────────────────────────────────────────────────────────┐
│                    MainActivity (Root Box Layered Stack)                   │
│  ┌───────────────────────────────────┐  ┌───────────────────────────────┐  │
│  │  [क्षण Squircle Tap]              │  │  [✦ Vision Pill Tap]          │  │
│  │  ▼ Floating PresentKshanOverlay   │  │  ▼ DeveloperVisionModal       │  │
│  │    (zIndex = 10f + Dismiss Scrim) │  │  • Abhinav Yaduvanshi Bio     │  │
│  │  • Today Elapsed % & Sun Arc      │  │  • Interactive Orbital Canvas │  │
│  │  • Indian Ritu (ऋतु) Telemetry    │  │  • Kinetic Vision Cards       │  │
│  │  • "Capture This Kshan" -> Room   │  │                               │  │
│  └───────────────────────────────────┘  └───────────────────────────────┘  │
└───────────────────┬────────────────────────────────────┬───────────────────┘
                    │                                    │
       ┌────────────┴────────────┐          ┌────────────┴────────────┐
       ▼                         ▼          ▼                         ▼
┌──────────────────────────┐  ┌──────────────────────┐  ┌────────────────────┐
│ Difference & DateMath    │  │ CountdownScreen      │  │ WidgetsDashboard   │
│ • MechanicalScrubRuler   │  │ • Per-Event Derived  │  │ • YearProgressCard │
│   (change.consume() +    │  │   CountDown (−) vs   │  │   [Ring ⇄ 365 Dots]│
│   left drag = +1 day)    │  │   DaysSince (+) Mode │  │ • Annual Projected │
│ • TemporalConstellation  │  │ • Holiday/Birthday   │  │   Gold Milestone   │
│   Hero (Timeline Arc +   │  │   Auto-Rollover      │  │   Stars on Grid    │
│   Today Dot + 3-Card     │  │                      │  │                    │
│   Human Rhythm Pager)    │  │                      │  │                    │
└──────────────────────────┘  └──────────────────────┘  └────────────────────┘
```

### Data Model & State
- **`TimeEngine` Extensions**:
  - `isRecurringCategory(category: String, title: String): Boolean`: Identifies `Holiday`, `Birthday`, `Anniversary`, `Festival`, or `Personal` annual milestones.
  - `resolveEffectiveTarget(target: ZonedDateTime, now: ZonedDateTime, category: String, title: String, isCountUpMode: Boolean): Pair<ZonedDateTime, Boolean>`: When `!isCountUpMode && target.isBefore(now) && isRecurringCategory(category, title)`, advances `target` year-by-year until `!rolled.isBefore(now)` and returns `Pair(rolled, true)`.
  - `calculateElapsedSince(now: ZonedDateTime, past: ZonedDateTime): CountdownTelemetry`: Computes live `days`, `hours`, `minutes`, `seconds` elapsed (`abs(Duration.between(past, now))`) for `Days Since (+)` mode.
  - `calculatePresentKshanTelemetry(now: ZonedDateTime): PresentKshanTelemetry`: Computes exact percentage of today elapsed (`0.00%–100.00%`), elapsed `HH:MM:SS` today, current Indian **Ritu (ऋतु)** (*Vasanta, Grishma, Varsha, Sharad, Hemanta, Shishira*), and solar phase label.
  - `calculateTemporalConstellation(start: LocalDate, end: LocalDate, totalDays: Long, today: LocalDate)`: Computes the normalized `todayProgress` (`0f..1f`) along the Start→End span plus the 3 Human/Cosmic Rhythm telemetry cards.

### Interactive Component & State Mapping
- **Mechanical Scrubbing Ruler (`MechanicalScrubRuler`)**:
  - Consumes horizontal drag gestures via `change.consume()` and maps leftward thumb movement (`dragAmount < 0`) to positive steps (`+1 day`).
  - *Span Screen*: Includes `ScrubTarget` toggle (`Start Date` vs `End Date`, defaulting to `End Date`).
  - *Shift Screen*: Scrubs `Days to Shift` (`0..9999`) or `Base Date` via a quick selector pill.
- **Floating Present Moment Overlay (`PresentKshanDropdownCard`)**:
  - Placed in the root `Box` of `KshanApp` with `Modifier.zIndex(10f)` and a semi-transparent backdrop scrim (`Color.Black.copy(alpha = 0.55f)`) so opening it never shifts the underlying screen layout.
  - Tapping **"Capture This Kshan"** saves a `ChronoEvent` with `category = "Kshan"`, resets `overrideCountUpMode = null` (so the new event automatically opens in `Days Since (+)` mode), and navigates to the `COUNTDOWN` tab.
- **Creator & Vision Studio (`DeveloperVisionModal`)**:
  - Triggered by tapping the **`✦ Vision`** header pill. Displays an interactive orbital particle gyroscope, **Abhinav Yaduvanshi** creator profile, interactive design-philosophy cards, and a live pyrotechnic spark burst trigger.
- **365-Dot Matrix Year Grid (`YearDotMatrixGrid`)**:
  - Pass `savedEvents` into `WidgetsDashboardScreen` so `YearDotMatrixGrid` projects recurring annual milestones and current-year milestones onto their exact `dayOfYear` dots (`1..365/366`). Tapping any dot selects that day and displays its date and any matching milestone badge.
