# क्षण (Kshan) — Production Architecture, Realistic Fireworks Simulator & Release Upgrade Plan (Revised)

This revised plan incorporates all technical corrections for **क्षण (Kshan)** (`com.abhi.kshan`): target-relative `Chronometer` remainder math with `"Reached"` state swapping, `setAndAllowWhileIdle()` alarm scheduling with `BOOT_COMPLETED` recovery, Room schema export, removal of unused `.ttf` fonts and Firebase/network dependencies, full `com/abhi/kshan` package directory migration, mid-range GPU particle budgets with Power Saver caps, and safe GitHub Release workflows.

## User Review & Critical Decisions

> [!IMPORTANT]
> All pre-build technical fixes and scope guards have been integrated into this revision:

- **Fix 1 — `Chronometer` Remainder Math, Target-Relative Day Boundaries & "Reached" State**:
  - A native `Chronometer` displays total elapsed/remaining hours (`123:45:10`) and ticks negative past zero if given the raw target time.
  - **Remainder Base Calculation**: For **4×2 Panoramic** and **4×1 Pill** widgets, we compute `days = remainingMillis / 86_400_000L` and `remainderMillis = remainingMillis - (days * 86_400_000L)` (`target - days * 24h`). We set the `Chronometer` base to `SystemClock.elapsedRealtime() + remainderMillis` with `setCountDown(true)` so it strictly ticks the `HH:MM:SS` remainder (`23:59:59` down to `00:00:00`).
  - **Target-Relative Boundary Refresh**: Instead of refreshing at midnight, we schedule the next widget update at the exact **day boundary relative to the target timestamp** (`nextBoundaryEpochMillis = targetEpochMillis - (days * 86_400_000L)` when `days > 0`, and at `targetEpochMillis` when `days == 0`).
  - **"Milestone Reached" View Swap**: When `nowEpochMillis >= targetEpochMillis`, the widget hides the `Chronometer` view (`View.GONE`) and swaps in a dedicated **"MILESTONE REACHED"** badge so the timer never ticks negative.
- **Fix 2 — Zero-Permission Alarm Scheduling (`setAndAllowWhileIdle`) & System Receivers**:
  - **Alarm Scheduling**: Because Android 14+ denies `SCHEDULE_EXACT_ALARM` by default on new installs, we use `alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextBoundaryEpochMillis, pendingIntent)` (or check `canScheduleExactAlarms()` on API 31+ and fall back to `setAndAllowWhileIdle()`), requiring zero special user permissions.
  - **Broadcast Receivers**: Register `android.intent.action.TIME_SET` (the real system time-change broadcast), `android.intent.action.TIMEZONE_CHANGED`, and `android.intent.action.BOOT_COMPLETED` (with `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />`) so scheduled boundary alarms are automatically restored after a reboot. `ACTION_DATE_CHANGED` is omitted.
- **Fix 3 — Package Migration (`com.abhi.kshan`), Room Schema Export & Font Cleanup**:
  - **Full Directory Migration**: Move source and test folders from `com/example` to `com/abhi/kshan`, update `namespace = "com.abhi.kshan"` and `applicationId = "com.abhi.kshan"`, and update all manifest and widget provider class paths.
  - **Room Schema Export**: Enable `exportSchema = true` in `@Database` and configure `ksp { arg("room.schemaLocation", "$projectDir/schemas") }` in the build script alongside the package migration.
  - **Delete Unused Font `.ttf` Files**: Remove unused font resources (`rozha_one.ttf`, `poppins.ttf`, `jetbrains_mono.ttf`, `plus_jakarta_sans.ttf`) from `res/font/`, keeping only `comic_neue.ttf`, `courier_prime.ttf`, and `kalam.ttf`.
- **Fix 4 — Mid-Range GPU Fireworks Budget, Power Saver Cap & Haptic Permission**:
  - **Particle Budget & Power Saver Throttling**: Cap the pool to **1,200 sparks + 45 smoke puffs** in standard mode, and dynamically throttle to **350 sparks + 0 smoke puffs** (with simplified single-pass rendering) when **Power Saver** is enabled so mid-range phones maintain a locked 60fps.
  - **Manifest Permissions**: Confirm `<uses-permission android:name="android.permission.VIBRATE" />` and `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />` are declared in `AndroidManifest.xml`.
- **Fix 5 — Safe Release APK Workflow**:
  - Never commit keystores or credentials to version control. Document clean local APK signing and GitHub Releases publishing steps in `README.md` alongside GitHub repository topics.

---

## 1. Overview & Core Concept

- **What It Does**: **क्षण (Kshan)** is a precision chronometer, date difference calculator, working-day shifter, live event countdown studio, and physics-driven pyrotechnic celebration app built with Jetpack Compose.
- **Target Audience / Persona**: Users and open-source developers looking for a fast, zero-bloat Android time utility with adaptive home screen widgets, custom bundled typography, and smooth 60fps particle physics.
- **Key Value**: Shrinks APK size by removing unused Firebase AI/App Check and unused `.ttf` fonts, prevents background battery drain, keeps home screen widgets accurate across day boundaries and reboots, and organizes the codebase under `com.abhi.kshan`.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Home Screen Widget Flow**:
     - **2×2 Orbit Widget**: Renders **Days Only** inside the orbital progress ring, updating automatically at each target-relative 24-hour boundary.
     - **4×2 Panoramic & 4×1 Pill Widgets**: Render **Days** on the glass card plus a live ticking **HH:MM:SS** `Chronometer` calibrated to `target - (days * 24h)`. Once the target instant arrives, the `Chronometer` hides and swaps to a glowing **"MILESTONE REACHED"** state.
  2. **Widget Studio In-App Preview**: Switching between `2×2 Orbit`, `4×2 Panoramic`, and `4×1 Minimal Pill` previews the exact layout with live animated ticking seconds on the 4×2 and 4×1 formats.
  3. **Realistic Fireworks Simulator Flow**: Users can tap/drag across the night sky or toggle **Auto Finale Show Mode** to experience 6 shell types (**Chrysanthemum**, **Willow**, **Saturn Double-Ring**, **Strobe Crackle**, **Peony Crossette 4-Way Split**, and **Horsetail Cascade**), atmospheric sky flash lighting, drifting smoke clouds, and speed-of-sound delayed explosion audio + haptics—automatically throttled when Power Saver is active.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Deep OLED Space Glass (`#000000` to `#0B101E`) with electric neon accents (`#00F2FE` Cyan, `#A855F7` Purple, `#FFD700` Metallic Gold, `#10B981` Emerald).
  - *Typography & Hierarchy*: Strictly bundled `.ttf` resources—**Comic Neue** for UI headers and labels, **Courier Prime** for Typewriter numbers, dates, and live timers, and **Kalam** for the `"क्षण"` brand glyph.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Target-Relative `setAndAllowWhileIdle` Alarms Over `updatePeriodMillis`**:
  - *Chosen Approach*: Schedule an inexact wake alarm (`setAndAllowWhileIdle`, or exact if `canScheduleExactAlarms()` is true) for `targetEpochMillis - (days * 86_400_000L)`, and re-register on `TIME_SET`, `TIMEZONE_CHANGED`, and `BOOT_COMPLETED`.
  - *Why*: Countdown days decrement every 24 hours relative to the event's target time of day, not at midnight. Using `setAndAllowWhileIdle` works out-of-the-box on Android 14+ without requiring the user to grant `SCHEDULE_EXACT_ALARM` in system settings.
- **Decision 2 — Removing Unused Dependencies & Unused Font Assets**:
  - *Chosen Approach*: Strip `firebase-bom`, `firebase-ai`, `firebase-appcheck-recaptcha`, `firebase-appcheck-debug`, `retrofit`, `okhttp`, `logging-interceptor`, and `moshi` from Gradle, and delete `rozha_one.ttf`, `poppins.ttf`, `jetbrains_mono.ttf`, and `plus_jakarta_sans.ttf`.
  - *Why*: Eliminates dead code and unused binary assets to minimize APK size.
- **Decision 3 — Lifecycle-Bound Telemetry Ticker & Structured Logging**:
  - *Chosen Approach*: Drive the 1-second UI telemetry update via a lifecycle-aware flow (`repeatOnLifecycle(Lifecycle.State.STARTED)` / `collectAsStateWithLifecycle`) and replace all empty `catch (e: Exception) {}` blocks with `Log.w("KshanChrono", ..., e)`.
  - *Why*: Stops background CPU wakeups when the app is minimized and ensures date-parsing errors are traceable in Logcat.
- **Decision 4 — Modularizing Large UI & Physics Files**:
  - *Chosen Approach*: Split `WidgetsDashboardScreen` and `CelebrationFireworksOverlay` into focused sub-modules (physics engine, sound/haptics engine, overlay UI, widget mockup previews, and telemetry cards).
  - *Why*: Keeps files under ~450 lines for clean navigation and fast incremental builds.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

- **Architecture & Component Diagram**:

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        क्षण (Kshan) — com.abhi.kshan                       │
├────────────────────────────────────────────────────────────────────────────┤
│  Data & Lifecycle State Layer                                              │
│  ┌──────────────────────────────┐     ┌─────────────────────────────────┐  │
│  │ ChronoDatabase (Room)        │────▶│ ChronoViewModel                 │  │
│  │ • exportSchema = true        │     │ • Lifecycle-bound 1Hz ticker    │  │
│  │ • KSP schemaLocation         │     │ • Structured Log.w error logs   │  │
│  └──────────────────────────────┘     └─────────────────────────────────┘  │
├────────────────────────────────────────────────────────────────────────────┤
│  Modularized UI & Fireworks Simulator Layer                                │
│  ┌──────────────────────────────────┐ ┌─────────────────────────────────┐  │
│  │ Fireworks Simulator Modules      │ │ Widget Studio Modules           │  │
│  │ • PyroPhysicsEngine (1200 sparks │ │ • WidgetsDashboardScreen        │  │
│  │   / 350 in Power Saver, 45 smoke,│ │ • WidgetMockupPreviews (2x2     │  │
│  │   6 shells, sky flash, finale)   │ │   Days-Only, 4x2 & 4x1 Days +   │  │
│  │ • PyroSoundEngine (Distance-     │ │   Live Animated HH:MM:SS)       │  │
│  │   delayed boom &VIBRATE haptics) │ │ • WidgetTelemetryCards          │  │
│  │ • CelebrationFireworksOverlay    │ └─────────────────────────────────┘  │
│  └──────────────────────────────────┘                                      │
├────────────────────────────────────────────────────────────────────────────┤
│  OS Home Screen Widget & Target-Boundary Scheduler                         │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ ChronoWidgetProvider + WidgetAlarmScheduler                          │  │
│  │ • Receivers: TIME_SET, TIMEZONE_CHANGED, BOOT_COMPLETED              │  │
│  │ • Alarm: setAndAllowWhileIdle at target - (days * 24h)               │  │
│  │ • 2x2 Layout: Days-only orbital bitmap                               │  │
│  │ • 4x2 & 4x1 Layout: Days + Chronometer(base = elapsed + remainder)   │  │
│  │ • At target <= now: Hides Chronometer, shows "MILESTONE REACHED"     │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - **Widget `Chronometer` & Alarm Math**:
    - Given `remainingMillis = targetEpochMillis - System.currentTimeMillis()`:
      - If `remainingMillis <= 0L`: Display `0 DAYS` and `"MILESTONE REACHED"` status pill; hide `Chronometer` (`View.GONE`). Cancel pending boundary alarms.
      - If `remainingMillis > 0L`:
        - `days = remainingMillis / 86_400_000L`
        - `remainderMillis = remainingMillis % 86_400_000L` (or `86_400_000L` if exact multiple)
        - On `4×2` and `4×1` widget sizes (detected via `AppWidgetManager.getAppWidgetOptions(widgetId)` `OPTION_APPWIDGET_MIN_WIDTH` / `MIN_HEIGHT`), configure `views.setChronometer(R.id.widget_live_chronometer, SystemClock.elapsedRealtime() + remainderMillis, "%s", true)` and `views.setChronometerCountDown(R.id.widget_live_chronometer, true)`.
        - Schedule next boundary alarm at `triggerAtMillis = System.currentTimeMillis() + remainderMillis` via `setAndAllowWhileIdle`.
  - **Fireworks Simulator Particle & Acoustic Model**:
    - Normal mode: `MAX_SPARKS = 1200`, `MAX_SMOKE = 45`, `MAX_ROCKETS = 30`.
    - Power Saver mode (`powerSaverEnabled == true`): `MAX_SPARKS = 350`, `MAX_SMOKE = 0`, single-layer spark rendering.
    - Acoustic speed-of-sound delay: `delayMs = ((1f - burstY / canvasHeight) * 210L).toLong().coerceIn(70L, 240L)`.
