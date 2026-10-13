# क्षण (Kshan) • Precision Date Companion

A major visual, branding, and navigation update rebranding the app to **क्षण (Kshan)** — Sanskrit/Hindi for *"A Moment in Time"*. Featuring a striking Devanagari typographic logo with English pronunciation guidance, a floating glass island navigation dock with tactile haptics, and automatic real-time Home Screen Widget synchronization with intelligent fallback.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following revisions have been confirmed and integrated:

- **Rebrand to क्षण (Kshan)**:
  - App launcher name and metadata updated to **क्षण (Kshan)**.
  - Header displays the prominent, elegant Devanagari script **क्षण** bundled with premium typography.
  - Includes an English pronunciation subtitle and meaning: **KSHAN • /k-sh-uh-n/ • A Moment in Time** so all users instantly understand and appreciate the brand.
- **Automatic Real-Time Widget Sync & Intelligent Fallback**:
  - Whenever an event is deleted, added, or pinned, `ChronoWidgetProvider.updateAllWidgets(context)` executes immediately, pushing instant RemoteViews updates without waiting for the 30-minute poll cycle.
  - If a pinned countdown is deleted, the widget automatically detects and tracks the next closest upcoming countdown from the database.
  - If no countdowns remain in the database, the widget renders an elegant frosted prompt (*"+ Tap to create a countdown"*) that launches directly into the creation flow.
- **Floating Glass Island Navigation Dock**:
  - Ergonomic floating frosted dock positioned above the bottom navigation insets.
  - Gliding indicator capsule driven by fluid spring physics (`spring(dampingRatio = 0.72f, stiffness = 450f)`).
  - Tactile haptic feedback on tab changes for an ultra-premium feel.

---

## 1. Overview & Core Concept

**क्षण (Kshan)** brings deep cultural elegance to precision date math and time tracking:
- **क्षण (Devanagari)** represents an indivisible unit of time, a fleeting yet meaningful instant.
- **English Pronunciation Guide**: Clear phonetic guide (`/k-sh-uh-n/`) alongside the English name **Kshan** ensures effortless global readability.
- **Fluid & Tactile Navigation**: A dock-style floating capsule keeps navigation within comfortable thumb reach while keeping the UI clean and spacious.
- **Connected Widget Architecture**: The home screen widget is an always-in-sync companion reflecting database changes in real time.

---

## 2. User Experience & Visual Design

### Header & Brand Identity
- **Logo Treatment**:
  - Primary Logo: **क्षण** in oversized, elegant display typography with electric cyan / subtle lavender gradient glow.
  - Pronunciation & Tagline: **Kshan • /k-sh-uh-n/ • Precision Date Companion** in clean, modern typography.
  - Pro Badge: Translucent pill with cyan accent border.

### Floating Glass Island Navigation Dock
- **Form**: 32.dp curved translucent capsule (`#0F172A` with 88% frosted opacity and 1.dp glowing perimeter border) floating above navigation bar insets.
- **Spring Pill**: Active tab indicator smoothly glides behind the selected tab with natural spring physics.
- **Haptic Feedback**: Tactile click pulse on every tab tap.
- **Tabs**:
  1. 📏 **Difference** (`Icons.Default.CompareArrows`)
  2. ➕ **Add / Sub** (`Icons.Default.Calculate`)
  3. ⏱️ **Countdowns** (`Icons.Default.HourglassTop`)
  4. 🪐 **Widgets** (`Icons.Default.Widgets`)

### Reactive Widget States
1. **Active Pinned Event**: Shows event name, days counter, category pill, and remaining status.
2. **Auto-Promoted Next Event**: When a pinned event is removed, instantly falls back to the nearest upcoming event.
3. **Empty Frosted Prompt**: If zero events exist, renders a clean frosted card with `+` icon and *"Tap to create a countdown"*.

---

## 3. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                   क्षण (KSHAN) ARCHITECTURE & DATA FLOW                │
└────────────────────────────────────────────────────────────────────────┘

                        MainActivity (Edge-to-Edge)
                                     │
           ┌─────────────────────────┴────────────────────────┐
           ▼                                                  ▼
     क्षण (Kshan) Header                              LifecycleParticleCanvas
 (Devanagari + Pronunciation)                          (STARTED-bound vfx)
           │                                                  │
           ▼                                                  ▼
 AnimatedContent (Screens)                           ChronoViewModel (MVI)
  - DifferenceScreen                                          │
  - DateMathScreen                                            ├─────────────────────┐
  - CountdownScreen                                           ▼                     ▼
  - WidgetsDashboardScreen                             ChronoDatabase         ChronoWidgetProvider
           │                                           (Room + DAO)           (Instant RemoteViews)
           ▼                                                  │                     │
FloatingGlassIslandDock                                       ▼                     ▼
 (Gliding Spring Pill + Haptics)                       Next Event Fallback    Empty Prompt Card
```

### Execution Checklist
1. **Name & Platform Sync**:
   - Update `app/src/main/res/values/strings.xml`: `app_name = "क्षण (Kshan)"`.
   - Update `metadata.json`: `name = "क्षण (Kshan)"` and description.
2. **Typography & Font**:
   - Install local Devanagari font via `font-util install "Yatra One"` or `"Rozha One"` into `app/src/main/res/font/`.
   - Update `Type.kt` and `MainActivity.kt` header with **क्षण** and pronunciation guide.
3. **DAO & Fallback Query**:
   - In `ChronoDao.kt`, add `getNextUpcomingEventDirect()` query.
4. **Widget Real-Time Sync**:
   - In `ChronoWidgetProvider.kt`, add `updateAllWidgets(context)` companion method and support the empty prompt state in `widget_chrono_scope.xml`.
   - In `ChronoViewModel.kt`, trigger `ChronoWidgetProvider.updateAllWidgets(application)` on `deleteEvent`, `addNewEvent`, and `pinEvent`.
5. **Floating Glass Island Navigation Dock**:
   - Create `FloatingGlassIslandDock.kt` with spring animation and `HapticFeedbackConstants.KEYBOARD_TAP`.
   - Integrate into `MainActivity.kt`.
6. **Compile & Verify**: Run `compile_applet` and verify unit tests pass.
