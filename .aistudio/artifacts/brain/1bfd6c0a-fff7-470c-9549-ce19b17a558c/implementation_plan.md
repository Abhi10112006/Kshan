# क्षण (Kshan) — Minimalist Luxury Visual Reset & Final Engineering Review Blueprint

This revised plan combines the **Minimalist Luxury Visual Reset ("Titanium & Ice" Monochrome Palette & Space Grotesk Typography)** with every requirement from both the **TimeEngine & Room Edge-Case Refinements** and the **5-Point Final Engineering Review**.

## User Review & Critical Decisions

> [!IMPORTANT]
> All items from your final engineering review have been integrated into this specification:

1. **Non-Destructive Room Migration (`MIGRATION_1_2`)**:
   - Removes `.fallbackToDestructiveMigration()` completely so user-saved countdowns are never wiped on upgrade.
   - Registers `MIGRATION_1_2` on `chrono_events` to add `repeatsYearly INTEGER NOT NULL DEFAULT 0` and backfill `repeatsYearly = 1` where `lower(category) IN ('birthday','holiday','anniversary')`, exporting the v2 schema JSON.
2. **Future-Safe Rollover & `repeatsYearly` Across All Call Sites**:
   - `resolveEffectiveTarget(now, raw, repeatsYearly, isCountUpMode)` only rolls over when `repeatsYearly == true` **and** `raw.toLocalDate().isBefore(now.toLocalDate())`—preventing future-year recurring milestones (e.g., Dec 2027) from ever being pulled backward to the current year, and keeping events occurring today (`raw.toLocalDate() == today`) in a dedicated **`"Today ✦"`** celebration state all day.
   - Passes `event.repeatsYearly` into `resolveEffectiveTarget` and `projectEventDayOfYear` across **all call sites**: `ChronoViewModel`, `CountdownScreen`, `ChronoWidgetProvider`, and `YearDotMatrixGrid` in `WidgetsDashboardScreen`.
   - Resets `overrideCountUpMode = null` whenever an event is selected, edited, or moves from past to future (`!canUseCountUpMode`), preventing any future event from getting stuck in `+ Days Since`.
3. **Exact 3-Metric Strip Math (`TimeEngine`)**:
   - Replaces the tall bottom `HorizontalPager` on **Span** and **Shift** with a single-line frosted 3-metric strip computed from the exact span (`totalDays` and actual start/end `LocalDate` pair):
     - `weeks = totalDays / 7` and `remDays = totalDays % 7` (e.g., `2w 6d` for 20 days).
     - `workdays = calculateBusinessDaysBetween(minDate, maxDate)` using exact Monday–Friday calendar iteration (never `totalDays / 5`).
     - `hours = totalDays * 24` (e.g., `2w 6d  •  14 Workdays  •  480 Hours`).
4. **Crown Scrubber Boundary Clamping, Spring Snap-Back & TalkBack Semantics**:
   - **Hard Boundary Clamping (`0..9999` on Shift)**: When `daysToShift == 0` and the user drags to decrease (or `9999` and drags to increase), clamps `dragAccumulatorPx = 0f`, freezes the sub-tick visual offset, and suppresses haptics.
   - **Spring Snap-Back on Release**: In `onDragEnd` and `onDragCancel`, animates the leftover sub-tick visual offset (`dragAccumulatorPx % tickSpacingPx`) smoothly back to `0f` via `animateFloatAsState(spring())` so ticks re-center cleanly under the white laser needle when the finger lifts.
   - **TalkBack Accessibility Semantics**: Adds `Modifier.semantics` (`customActions` / `setProgress`) with explicit increment/decrement accessibility actions on `MechanicalScrubRuler`.
5. **`MY_PACKAGE_REPLACED` Widget Resync, Font/File Cleanup & Unit Tests**:
   - Adds `android.intent.action.MY_PACKAGE_REPLACED` to `ChronoWidgetProvider`'s `<intent-filter>` in `AndroidManifest.xml` and handles `Intent.ACTION_MY_PACKAGE_REPLACED` in `onReceive` so home screen widgets immediately re-sync and reschedule alarms after an app update.
   - Deletes unused `comic_neue` font files from `res/font/`, bundles **Space Grotesk** (`Regular 400` & `SemiBold 600`) with its OFL license notice, and deletes the stray `androidTest/java/com/example/ExampleInstrumentedTest.kt`.
   - Adds comprehensive JUnit/Robolectric tests (`TimeEngineTest` & `MigrationTest`) covering Feb 29 leap-year rollover (to Feb 28 in 2027 and back to Feb 29 in leap year 2028), birthdays occurring today staying in `"Today ✦"` all day, future-year recurring events (Dec 2027) not getting pulled backward to Dec 2026, Dec 31 → Jan 1 year-boundary rollover, and `MIGRATION_1_2` data preservation.

---

## 1. Overview & Core Concept

- **What It Does**: Elevates **क्षण (Kshan)** into a minimalist, Swiss-horology / Leica-inspired precision date companion with a calm **"Titanium & Ice" Monochrome Palette**, **Space Grotesk** architectural typography, a compact **52.dp frameless Crown Scrubber**, and rock-solid calendar/leap-year math.
- **Target Audience / Persona**: Users who appreciate quiet luxury hardware aesthetics, zero visual clutter, and exact calendar calculations.
- **Key Value**: Every screen fits comfortably without vertical squeezing or text collisions, user data upgrades safely via non-destructive SQLite migration, and recurring annual milestones behave accurately across leap years, today boundaries, and future years.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme ("Titanium & Ice")
- **Surfaces & Borders**:
  - **Background**: Deep OLED Obsidian (`#030509` / `#070B12`).
  - **Card Surface**: Unified Obsidian Glass (`#0A0E17` at `85%` alpha).
  - **Default Border**: Uniform `0.8.dp` Titanium Hairline (`Color.White.copy(alpha = 0.10f)`).
  - **Active / Selected Accent**: Subtle **Ice Cyan (`#38BDF8`)** for primary active focus and **Warm Champagne (`#E5C158`)** for solar/milestone accents—eliminating simultaneous magenta, pink, bright yellow, purple, and green borders.
- **Typography & Hierarchy**:
  - **Architectural UI & Labels**: Locally bundled **Space Grotesk** (`Regular 400` & `SemiBold 600`) with OFL attribution replaces `Comic Neue` across all headers, cards, pills, and descriptions (and `comic_neue` font files are removed).
  - **Tabular Numerals & Dates**: **Courier Prime Bold** (`tnum`) for all hero numbers, dates, countdown digits, percentages, and timestamps.
  - **Brand Emblem**: **Kalam Bold** strictly isolated to the Devanagari `क्षण` emblem.

### De-Cluttered Screen Flows
1. **Top Header (`MainActivity`)**:
   - Displays the frosted `क्षण` squircle and clean `Kshan` + `Precision Date Companion` title (removing `"• वर्तमान"` next to `Kshan`).
   - Top-right **`✦ Vision`** pill styled in subtle Titanium & Warm Champagne (`#E5C158` / `#E2E8F0`).
2. **Span (Difference) & Shift (Date Math) Screens**:
   - **Connected Date Cards**: Quiet Obsidian Glass cards (`#0A0E17` @ 85%) with `0.8.dp` Titanium hairlines; only the active scrub target card gets a subtle `0.8.dp` Ice Cyan (`#38BDF8`) border.
   - **Compact 52.dp Frameless Crown Scrubber**: A sleek `52.dp` horizontal strip with `[ Start | End ]` (or `[ Base | Shift ]`) on the left and smooth sub-tick titanium ruler marks (`dragAccumulatorPx % tickSpacingPx`) with a single white center needle on the right. Includes `0..9999` hard boundary clamping, spring snap-back on finger release, 16ms haptic throttling, and TalkBack increment/decrement semantics. All colliding footer instruction text is deleted.
   - **Clean Hero Readout**: Deletes the overlapping curved rainbow arc and `"TODAY AT X% OF SPAN"` row. The Hero Number (`20`) or Result Date (`Sunday, October 25, 2026`) sits in generous negative space over a soft monochrome/ice-cyan radial glow.
   - **Single-Line Frosted 3-Metric Strip**: Replaces the 130.dp `HorizontalPager` with a single-line frosted strip below the hero readout:
     - `2w 6d  •  14 Workdays  •  480 Hours` (computed from exact business-day math).
3. **`वर्तमान क्षण` Floating Overlay & `✦ Vision` Creator Studio**:
   - **`PresentKshanDropdownCard`**: Uses `BackHandler` + `imePadding()`, a refined Warm Champagne (`#E5C158`) solar progress bar, and quiet titanium glass cards.
   - **`DeveloperVisionModal`**: Constrains the rotating dashed orbital rings strictly inside the top `120.dp` `क्षण` emblem box so they never overlap or cut through the `"Abhinav Yaduvanshi"` title text. Replaces multi-colored card icons with unified Champagne (`#E5C158`) and brushed Silver (`#CBD5E1`) accents, running the animation loop strictly while `visible == true`.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Non-Destructive `MIGRATION_1_2` on `chrono_events`**
  - *Chosen Approach*: Upgrade `ChronoDatabase` to `version = 2` using `.addMigrations(MIGRATION_1_2)` without `.fallbackToDestructiveMigration()`:
    ```kotlin
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE chrono_events ADD COLUMN repeatsYearly INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE chrono_events SET repeatsYearly = 1 WHERE lower(category) IN ('birthday','holiday','anniversary')")
        }
    }
    ```
  - *Why*: Preserves all existing user countdowns while automatically enabling yearly repeat for existing Birthdays, Holidays, and Anniversaries.
- **Decision 2: Future-Safe `MonthDay.from(raw).atYear(...)` & Dedicated `"Today ✦"` State**
  - *Chosen Approach*:
    ```kotlin
    val today = now.toLocalDate()
    if (isCountUpMode || !repeatsYearly || !raw.toLocalDate().isBefore(today)) {
        return raw to false
    }
    val md = MonthDay.from(raw)
    var d = md.atYear(today.year)
    if (d.isBefore(today)) d = md.atYear(today.year + 1)
    return ZonedDateTime.of(d, raw.toLocalTime(), raw.zone) to true
    ```
    And in `calculateCountdown(now, target)`: if `target.toLocalDate() == now.toLocalDate()` and `!now.toInstant().isBefore(target.toInstant())`, return `CountdownStatus.TODAY_CELEBRATION` (`"Today ✦"`) for the entire calendar day.
  - *Why*: Prevents future recurring dates (e.g., Dec 2027) from being pulled backward to Dec 2026, preserves Feb 29 on leap years (`2028`) after clamping to Feb 28 on non-leap years (`2027`), and celebrates today's milestone all day long.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (KshanApp)                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ KshanHeader: [क्षण Squircle]  "Kshan"              [✦ Vision Pill]   │  │
│  └───────────────────┬───────────────────────────────────────┬──────────┘  │
│                      │ (zIndex 10f, BackHandler, imePadding) │ (zIndex 20f)│
│                      ▼                                       ▼             │
│        ┌───────────────────────────┐           ┌─────────────────────────┐ │
│        │ PresentKshanDropdownCard  │           │  DeveloperVisionModal   │ │
│        │ • Champagne Solar Bar     │           │  • 120.dp Emblem Orbit  │ │
│        │ • Titanium Glass Cards    │           │  • Champagne & Silver   │ │
│        └───────────────────────────┘           └─────────────────────────┘ │
└──────────────────────────────────────┬─────────────────────────────────────┘
                                       │
         ┌─────────────────────────────┼─────────────────────────────┐
         ▼                             ▼                             ▼
┌─────────────────────┐     ┌─────────────────────┐     ┌─────────────────────────┐
│ Difference / Shift  │     │  CountdownScreen    │     │ WidgetsDashboardScreen  │
│ • Titanium Cards    │     │ • Hero Timepiece    │     │ • 365-Dot Year Matrix   │
│ • 52.dp Frameless   │     │   (Today ✦ State &  │     │   (Uses event.          │
│   Crown Scrubber    │     │    Past-Only +Mode) │     │    repeatsYearly)       │
│   (Clamp + Snap +   │     │ • Add Sheet with    │     │ • MY_PACKAGE_REPLACED   │
│    TalkBack)        │     │   repeatsYearly     │     │   Widget Resync         │
│ • Clean Hero Number │     │   Toggle            │     │                         │
│ • 1-Line 3-Metric   │     │                     │     │                         │
│   Strip (Exact      │     │                     │     │                         │
│   Workdays)         │     │                     │     │                         │
└─────────┬───────────┘     └──────────┬──────────┘     └────────────┬────────────┘
          └────────────────────────────┼─────────────────────────────┘
                                       ▼
┌────────────────────────────────────────────────────────────────────────────┐
│            ChronoViewModel & TimeEngine + Room DB (v2 MIGRATION_1_2)       │
│  • ChronoEvent(..., val repeatsYearly: Boolean = false)                    │
│  • resolveEffectiveTarget(now, raw, repeatsYearly, isCountUpMode)          │
│  • calculateCompactMetricsStrip(start, end, totalDays)                     │
│  • JUnit & Robolectric Suite: TimeEngineTest + MigrationTest               │
└────────────────────────────────────────────────────────────────────────────┘
```

### Interactive Component & State Mapping
1. **Call-Site Propagation of `repeatsYearly`**:
   - `ChronoUiState` stores `activeEventRepeatsYearly: Boolean = true`.
   - `ChronoViewModel` updates `activeEventRepeatsYearly` from `pinnedEvent`, `selectActiveEvent`, `addNewEvent`, and `setCountdownTarget`, and resets `overrideCountUpMode = null` whenever an event changes or `!TimeEngine.canUseCountUpMode(now, rawTarget, category)` becomes true.
   - `CountdownScreen` passes `event.repeatsYearly` into `TimeEngine.resolveEffectiveTarget` for every `SavedCountdownCard` and provides a `"Repeats Yearly"` toggle in `SurpriseAddCountdownSheet` (auto-checked when selecting `Birthday`, `Holiday`, or `Anniversary`).
   - `ChronoWidgetProvider` passes `event.repeatsYearly` into `TimeEngine.resolveEffectiveTarget` when computing the home screen widget's target instant and listens for `Intent.ACTION_MY_PACKAGE_REPLACED` in both `AndroidManifest.xml` and `onReceive`.
   - `YearDotMatrixGrid` passes `event.repeatsYearly` into `TimeEngine.projectEventDayOfYear(zdt, event.repeatsYearly, currentYear)`.
2. **Crown Scrubber Physics, Clamping, Snap-Back & Accessibility**:
   - Accepts `minValue: Long? = null`, `maxValue: Long? = null`, and `currentNumericValue: Long? = null` so Shift mode (`0..9999`) clamps `dragAccumulatorPx = 0f` at the boundaries without shifting ticks or firing haptics.
   - Uses `var isDragging by remember { mutableStateOf(false) }` and `animateFloatAsState` to smoothly spring-snap the sub-tick offset `(dragAccumulatorPx % stepPx)` back to `0f` when `onDragEnd` or `onDragCancel` fires.
   - Adds `Modifier.semantics` with `customActions` (`Increment (+1)` and `Decrement (-1)`) and `setProgress` so TalkBack users can scrub dates and day shifts effortlessly.
