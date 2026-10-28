# क्षण (Kshan) — Intentional Time & Runway Companion (Design System & Product Vision Overhaul)

This plan preserves 100% of the strict **4-Color (`#070A12`, `#121620`, `8%` White Border, `#2ABFFF` Cyan + `#E6C87A` Vision Gold)**, **2-Radius (`24.dp` cards / `100%` full pills)**, **JetBrains Mono (`tnum`)**, **swipe-left + long-press deletion**, and **persistent `11.sp` bottom dock labels**, while transforming **क्षण (Kshan)** from a sterile date calculator into a daily **Intentional Time & Runway Companion** across four reimagined pillars: **Runway**, **Planner**, **Kshans**, and **Pulse**.

## User Review & Critical Decisions

> [!IMPORTANT]
> This revised specification integrates your product vision overhaul with all confirmed design system tokens and engineering guarantees:

- **Voice & Navigation Rebrand (4 Pillars of Kshan)**:
  - **Header Subtitle**: Updated from `"Precision Date Companion"` to **`"Intentional Time & Runway"`** (unclipped).
  - **Persistent `11.sp` Bottom Dock Tabs**: **`Runway`** (formerly *Difference*), **`Planner`** (formerly *Shift*), **`Kshans`** (formerly *Countdowns*), and **`Pulse`** (formerly *Widgets*).
- **Tab 1 — `Runway` (Focus & Runway Engine)**:
  - Merges `FROM (ORIGIN)` and `TO (HORIZON)` into a **Unified Single Horizon Card (`24.dp`, `#121620`)** with a `1.dp` `White.copy(alpha = 0.08f)` vertical divider and a centered `⇄` swap pill.
  - Defaults `TO (HORIZON)` to **`+30 Days` (`Next 30d Sprint`)** on first launch and shows a **Today's Live Kshan Card** (`"Today has Xh Ym of waking runway left"`) whenever `From == To`—completely eliminating the dead `"0 TOTAL DAYS"` state.
  - Upgrades the readout to **`AVAILABLE RUNWAY`** / **`NET WORKING RUNWAY`** with an interactive **Focus Hours Translator (`4h/day`, `6h/day`, `8h/day`)** and a 1-tap **`"Save as Milestone →"`** pill that creates a tracked event in **Kshans**.
- **Tab 2 — `Planner` (Sprint & Deadline Projector)**:
  - Merges **`START ANCHOR`** and **`SPRINT DURATION`** into a **Unified Single Input Card (`24.dp`, `#121620`)** with real-world sprint pills (`7d Sprint`, `14d Sprint`, `21d Habit`, `45d Block`, `90d Quarter`), a **`PROJECTED TARGET DATE`** readout with a subtle slow-rotating monochrome orbital ring, weekend buffer breakdown (`"12 Working Days + 4 Weekend Buffer Days"`), and a 1-tap **`"+ Pin to Kshans"`** pill.
- **Tab 3 — `Kshans` (Life Pace & Daily Reality)**:
  - Adds a **Real-Life Context Line** in **JetBrains Mono** inside the Hero Card (`"32 Working Days left • 192h at 6h/day"` in Count Down mode; `"Streak Milestone: Next 100d mark in 14 days"` in Days Since mode).
  - Styles the pinned Saved Kshan card at **100% `#121620` surface with a `1.dp` `#2ABFFF` glow border** and unpinned cards at **60% `#121620` surface opacity with `6%` white border**, colored left category dots (`GOAL = #2ABFFF`, `BIRTHDAY = #F472B6`, `HOLIDAY = #E6C87A`, `KSHAN = #10B981`), Pin-only right button, and **Swipe-Left + Long-Press to delete**.
- **Tab 4 — `Pulse` (Daily Life Pace Dashboard & Nothing OS Home Screen Widget Studio)**:
  - Places **Live Life Pulse** (Today's Waking Progress %, Quarter Sprint %, and 365-Dot Year Matrix / Ring) at the very top of the screen for a daily morning check-in, makes every **Quick Date Jump (`2×2` Bento Grid)** interactive (open in `Planner` or save as a `Kshan`), and styles **every widget card in a signature Nothing OS dot-matrix aesthetic** with a 1-tap **`"+ Pin to Home Screen"`** launcher action.

---

## 1. Overview & Core Concept

- **What It Does**: Empowers students, builders, and intentional planners to measure real working runway and deep focus hours (**Runway**), project sprint delivery dates with weekend buffers (**Planner**), track high-stakes milestones and life streaks with working-capacity context (**Kshans**), and monitor daily/quarterly/yearly pace in-app and on their Android Home Screen via Nothing OS-styled widgets (**Pulse**).
- **Target Audience / Persona**: Makers, students, founders, and mindful achievers who think in sprints, deep-work hours, and meaningful milestones rather than raw calendar subtraction.
- **Key Value**: Bridges calculation and action—every runway or sprint calculation translates directly into deep focus hours and can be pinned as a tracked **Kshan** or placed on the Android Home Screen in a single tap.

---

## 2. User Experience & Visual Design

### Strict 4-Color, 2-Radius Design System & Typography
- **Four Core Color Tokens**:
  - **Background (`#070A12`)**: Deep obsidian canvas with a subtle **2% procedural noise/grain overlay** and **soft radial edge vignette** on the root `Box`.
  - **Surface (`#121620`)**: Unified matte slate-obsidian fill across all cards, sheets, and the floating bottom navigation island.
  - **Border (`1.dp` solid `Color.White.copy(alpha = 0.08f)`)**: Applied to all standard cards (with `1.dp` `#2ABFFF` glow reserved for the active pinned Kshan row and `Color.White.copy(alpha = 0.06f)` on unpinned 60%-opacity Saved Kshan rows).
  - **Accent (`#2ABFFF` Cyan & Exclusive `#E6C87A` Vision Gold)**:
    - **Single Interactive Cyan (`#2ABFFF`)**: Used across active segmented pills, focus-pace toggles, active dock tabs, and primary actions.
    - **Exclusive Vision Gold (`#E6C87A`)**: Used strictly inside the top-right **`✦ Vision`** header button (filled `#121620` full pill with `#E6C87A` text and a delicate `1.dp` inner highlight).
- **Strict Two-Radius Geometry**:
  - **Cards (`RoundedCornerShape(24.dp)`)**: Every card across `Runway`, `Planner`, `Kshans`, and `Pulse` uses an exact `24.dp` corner radius.
  - **Buttons, Chips & Segmented Controls (`RoundedCornerShape(100)`)**: Every button, horizon/sprint preset chip, pace selector, segmented toggle, and the bottom navigation island uses a full pill shape (`RoundedCornerShape(100)`).
- **Typography**:
  - **Header Branding**: Left squircle (`क्षण` in `Kalam Bold`) + `Kshan` + unclipped subtitle **`"Intentional Time & Runway"`**.
  - **Category & Telemetry Labels**: `10.sp` uppercase, `letterSpacing = 1.2.sp`, `Color.White.copy(alpha = 0.50f)`.
  - **Numerical & Date Readouts**: Locally bundled **JetBrains Mono** with `fontFeatureSettings = "tnum"`.
  - **Card Titles**: `18.sp` `FontWeight.Medium` (`Space Grotesk`).

### Pillar-by-Pillar User Flows

1. **Tab 1 — `Runway` (Actionable Focus & Runway Engine)**:
   - **Unified Single Horizon Card (`24.dp`, `#121620`)**: Left side displays `FROM (ORIGIN)` and right side displays `TO (HORIZON)`, separated by a `1.dp` `White.copy(alpha = 0.08f)` vertical divider and a centered `⇄` full-pill swap button.
   - **Smart Horizon Presets & Crown Scrubber**: Full-pill preset chips (**`End of Month`**, **`Next 30d Sprint`**, **`90d Quarter`**, **`End of Year`**) paired with the `52.dp` Crown Scrubber (`[ Origin | Horizon ]` full-pill target switch).
   - **Zero Dead Empty States**:
     - Initializes `TO (HORIZON)` to **`+30 Days` (`Next 30d Sprint`)** on launch.
     - Whenever `FROM == TO`, replaces the `0` readout with **Today's Live Kshan Card**: `"Today has Xh Ym of waking runway left"` with 1-tap full-pill buttons to measure runway to **`Month End`**, **`+30d Sprint`**, or **`Year End`**.
   - **Available Runway & Focus Hours Translator**:
     - Hero label reads **`AVAILABLE RUNWAY`** (or **`NET WORKING RUNWAY`** when `Skip Weekends` is active) above the Pure White `#FFFFFF` **JetBrains Mono** day count.
     - Below the hero number, the `24.dp` `#121620` Focus Translator card shows:
       - **Row 1**: `3w 5d  •  18 Working Days  •  8 Weekends Off`
       - **Row 2 (Live Focus Budget)**: Interactive daily pace pills (**`4h/day`**, **`6h/day`**, **`8h/day`**) dynamically computing `= 108 Deep Focus Hours (at 6h/day)` alongside a 1-tap **`"Save as Milestone →"`** full-pill button that saves the horizon directly to **Kshans**.

2. **Tab 2 — `Planner` (Sprint & Deadline Projector)**:
   - **Unified Single Input Card (`24.dp`, `#121620`)**: Left half = **`START ANCHOR`** (defaults to Today); Right half = **`SPRINT DURATION`** (tappable **JetBrains Mono** day count dialog + `-` / `+` full-pill steppers) separated by a `1.dp` `8%` white vertical hairline.
   - **Real-World Sprint Chips**: Full-pill presets for **`7d Sprint`**, **`14d Sprint`**, **`21d Habit`**, **`45d Block`**, and **`90d Quarter`**, plus the `[ Earlier (−) | Later (+) ]` full-pill direction switch (`#2ABFFF` active tint) and `52.dp` Crown Scrubber.
   - **Projected Target Date & Lock Action**:
     - Displays **`PROJECTED TARGET DATE`** (`10.sp` uppercase, `1.2.sp` tracking) with a subtle slow-rotating monochrome orbital ring behind the `32.sp` Pure White **JetBrains Mono** target date.
     - Telemetry strip displays exact calendar and buffer math: **`"12 Working Days + 4 Weekend Buffer Days (16 Calendar Days total)"`**.
     - Includes a 1-tap **`"+ Pin to Kshans"`** full-pill button right below the projected date to immediately save and track the projected deadline or habit target in **Kshans**.

3. **Tab 3 — `Kshans` (Milestones, Streaks & Daily Reality)**:
   - **Hero Kshan Card (`24.dp`, `#121620`, `1.dp` `8%` White Border)**:
     - Pure White `#FFFFFF` **JetBrains Mono** (`tnum`) digits for `DAYS : HOURS : MINS : SECS`, with a `150ms` vertical flip + subtle opacity pulse on `SECS`.
     - **Real-Life Context Line**: Directly below the ticking digits, a quiet **JetBrains Mono** insight pill translates raw time into human reality:
       - **Count Down (`−`) mode**: `"32 Working Days left  •  192h at 6h/day"`
       - **Days Since (`+`) mode**: `"Streak Milestone: Next 100d mark in 14 days"`
     - Full-pill `[ − Count Down | + Days Since ]` segmented switch at the bottom of the Hero Card.
   - **Saved Kshans List**:
     - **Pinned Card**: `100%` `#121620` surface with a `1.dp` `#2ABFFF` glow border.
     - **Unpinned Cards**: `60%` `#121620` surface opacity with a `1.dp` `Color.White.copy(alpha = 0.06f)` border.
     - **Category Dots**: `GOAL = #2ABFFF`, `BIRTHDAY = #F472B6`, `HOLIDAY = #E6C87A`, `KSHAN = #10B981`.
     - **Clean Row & Deletion**: Single Pin button on the right; **Swipe-Left (`SwipeToDismissBox`)** or **Long-Press (`DropdownMenu`)** to delete.

4. **Tab 4 — `Pulse` (Daily Life Pace Dashboard & Nothing OS Home Screen Widget Studio)**:
   - **Top Section — Live Life Pulse (Morning Check-In Dashboard)**:
     - **Today's Waking Progress %** & **Quarter Sprint %** side-by-side / stacked telemetry in **JetBrains Mono** (`tnum`) with `#2ABFFF` progress bars.
     - **365-Dot Year Matrix / Orbital Ring** (`24.dp` `#121620` card) with a full-pill `[ 365 Dots | Ring ]` toggle, glowing `#2ABFFF` today dot, and live day/percentage telemetry.
   - **Middle Section — Interactive Quick Date Jumps (`2×2` Bento Grid)**:
     - Every `24.dp` Bento card (`+30d Sprint`, `+90d Quarter`, `Next Quarter Start`, `End of Year`) is interactive: tapping a card lets the user **open that horizon directly in `Planner` / `Runway`** or **save it in 1 tap to `Kshans`**.
   - **Bottom Section — Nothing OS Home Screen Widget Studio**:
     - Every widget card (**Kshan Milestone Matrix**, **365-Dot Year Matrix**, **Today & Quarter Pulse**, and **Working Runway Counter**) is styled with a signature **Nothing OS dot-matrix / industrial monochrome-and-cyan aesthetic** (`#070A12` / `#121620` chassis, **JetBrains Mono** tabular readouts, dot-matrix accents, and `8%` white hairlines) and features a 1-tap **`"+ Pin to Home Screen"`** full-pill button so any widget card can be pinned directly to the user's Android Home Screen.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Default `+30d` Runway + "Today's Live Kshan" Card When `From == To`**
  - *Chosen Approach*: Initialize `Runway` to a 30-day sprint (`Today` -> `Today + 30d`) on startup so the user immediately sees meaningful working days and deep focus hours. If the user sets `From == To`, render a live **Today's Waking Runway Card** showing remaining hours/minutes in the current day with 1-tap horizon pills (`Month End`, `+30d Sprint`, `Year End`).
  - *Why*: Eliminates the sterile, uninspiring `"0 TOTAL DAYS"` screen and turns same-day selection into an urgent daily time-awareness tool.
  - *Alternatives Considered*: Keeping `From == To` as `0 Days` on launch (feels like an uninitialized calculator).
- **Decision 2: 1-Tap Cross-Tab Bridge (`"Save as Milestone →"` & `"+ Pin to Kshans"`)**
  - *Chosen Approach*: Add direct full-pill action buttons in both `Runway` and `Planner` (and on `Pulse` Quick Date Jump cards) that pre-populate or instantly save the calculated target date as a tracked event in the Room-backed `Kshans` list and switch/confirm with haptic feedback.
  - *Why*: Connects planning (`Runway` / `Planner`) with ongoing daily tracking (`Kshans` / Home Screen Widgets) so users never have to memorize and re-enter a calculated date.
  - *Alternatives Considered*: Keeping the four tabs isolated (forces manual re-entry of dates).
- **Decision 3: Multi-Card Nothing OS Home Screen Widget Studio**
  - *Chosen Approach*: Style all widget studio cards in a cohesive Nothing OS dot-matrix/industrial aesthetic and allow pinning any of the widget styles (Milestone Countdown, 365-Dot Year Matrix, Today/Quarter Life Pulse, and Working Runway) to the Android Home Screen via `GlanceAppWidgetManager.requestPinAppWidget`.
  - *Why*: Gives users true desktop/home-screen utility for whichever time lens matters most to them while keeping visual parity between the in-app `Pulse` dashboard and the OS launcher.
  - *Alternatives Considered*: Only allowing a single widget type to be pinned to the Home Screen.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Root Box (#070A12 + 2% Grain + Vignette)                 │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ KshanHeader: [क्षण Squircle] "Kshan / Intentional Time & Runway"       │  │
│  │              + Filled #121620 Full-Pill [✦ Vision (#E6C87A)]           │  │
│  └───────────────────────────────────┬────────────────────────────────────┘  │
│                                      │                                       │
│    ┌───────────────────┬─────────────┴─────────────┬───────────────────┐     │
│    ▼                   ▼                           ▼                   ▼     │
│ ┌───────────────┐ ┌────────────────┐ ┌──────────────────┐ ┌────────────────┐ │
│ │ 1. RUNWAY     │ │ 2. PLANNER     │ │ 3. KSHANS        │ │ 4. PULSE       │ │
│ │ • Unified     │ │ • Unified      │ │ • Hero Card      │ │ • Top: Live    │ │
│ │   Origin/     │ │   Start Anchor │ │   (Pure White    │ │   Life Pulse   │ │
│ │   Horizon     │ │   + Sprint     │ │   JetBrains Mono │ │   (Today %,    │ │
│ │   24.dp Card  │ │   Duration     │ │   + 150ms SECS   │ │   Quarter %,   │ │
│ │ • +30d Init & │ │   24.dp Card   │ │   Flip + Context │ │   365-Dot Year │ │
│ │   Today Live  │ │ • 7d/14d/21d/  │ │   Line: Workdays │ │   Matrix/Ring) │ │
│ │   Kshan Card  │ │   45d/90d Pills│ │   & 6h Focus or  │ │ • Mid: 2x2     │ │
│ │ • Focus Hours │ │ • Projected    │ │   Next Streak)   │ │   Interactive  │ │
│ │   Translator  │ │   Target Date  │ │ • Saved Kshans   │ │   Jump Bento   │ │
│ │   (4h/6h/8h)  │ │   + Orbital +  │ │   (Pinned #2ABFFF│ │ • Bot: Nothing │ │
│ │ • "Save as    │ │   Buffer Math  │ │   Glow, Unpinned │ │   OS Widget    │ │
│ │   Milestone→" │ │ • "+ Pin to    │ │   60%, Swipe/Long│ │   Studio (Pin  │ │
│ │   Bridge      │ │   Kshans" Pill │ │   Press Delete)  │ │   Every Card)  │ │
│ └───────┬───────┘ └────────┬───────┘ └─────────┬────────┘ └────────┬───────┘ │
│         └──────────────────┴─────────┬─────────┴───────────────────┘         │
│                                      ▼                                       │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ FloatingGlassIslandDock (Full Pill 100%, #121620, 1.dp 8% White Border)│  │
│  │ • Persistent 11.sp Labels: [ Runway ] [ Planner ] [ Kshans ] [ Pulse ] │  │
│  └────────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **ViewModel Cross-Tab Bridges (`TimeViewModel`)**:
  - Default `diffEndDate` initialized to `LocalDate.now().plusDays(30)` so `Runway` launches with a live 30-day sprint.
  - `selectedFocusHoursPerDay: StateFlow<Int>` (defaults to `6`, toggleable between `4`, `6`, `8`) for real-time deep focus hour translation in `Runway` and `Kshans`.
  - `saveHorizonOrSprintToKshans(title: String, targetDate: LocalDate, category: String = "GOAL")` helper and pre-populated `AddEditCountdownDialog` trigger so `Runway` (`"Save as Milestone →"`), `Planner` (`"+ Pin to Kshans"`), and `Pulse` Quick Date Jumps can save or open a milestone in one tap.
  - `openDateInPlanner(sprintDays: Long)` / `openDateInRunway(horizonDate: LocalDate)` actions wired to the `Pulse` `2×2` Quick Date Jumps Bento cards.
- **TimeEngine Context Calculators (`TimeEngine.kt`)**:
  - `calculateRunwayFocusMetrics(start: LocalDate, end: LocalDate, skipWeekends: Boolean, hoursPerDay: Int)`: Computes exact working days, weekend days off, weeks+days breakdown, and deep focus hours (`workingDays * hoursPerDay`).
  - `calculateHeroContextLine(targetDateTime: LocalDateTime, now: LocalDateTime, isCountUp: Boolean, hoursPerDay: Int)`:
    - For `Count Down (−)`: Returns `"X Working Days left  •  Yh at 6h/day"`.
    - For `Days Since (+)`: Computes the next milestone threshold (`7d`, `14d`, `21d`, `30d`, `50d`, `100d`, `200d`, `365d`, `500d`, `1000d`, or next `+100d` multiple) and returns `"Streak Milestone: Next Xd mark in Y days"`.

### Interactive Component & State Mapping
1. **`Runway` Tab (`DateDifferenceScreen.kt`)**:
   - **Unified Horizon Card**: Single `RoundedCornerShape(24.dp)` `#121620` card with `1.dp` `White.copy(alpha = 0.08f)` border, left/right tap targets for `FROM (ORIGIN)` and `TO (HORIZON)`, `1.dp` vertical divider, and centered `RoundedCornerShape(100)` `⇄` swap pill.
   - **Same-Day Fallback (`From == To`)**: Renders **Today's Live Kshan Card** displaying remaining waking hours/minutes today and quick-jump pills (`Month End`, `Next 30d Sprint`, `Year End`).
   - **Focus Hours Translator**: Interactive `[ 4h/day | 6h/day | 8h/day ]` full-pill selector + `"Save as Milestone →"` pill.
2. **`Planner` Tab (`DateShiftScreen.kt`)**:
   - **Unified Sprint Input Card**: Single `RoundedCornerShape(24.dp)` `#121620` card combining `START ANCHOR` (left) and `SPRINT DURATION` with `-` / `+` pills (right).
   - **Projected Target Readout**: Subtle slow-rotating monochrome orbital ring behind the `32.sp` Pure White **JetBrains Mono** target date, working vs. weekend buffer breakdown, and `"+ Pin to Kshans"` pill.
3. **`Kshans` Tab (`CountdownScreen.kt`)**:
   - **Hero Card**: Pure White `#FFFFFF` **JetBrains Mono** digits, `150ms` vertical flip on `SECS`, **Real-Life Context Line** pill below the digits, and `[ − Count Down | + Days Since ]` full-pill toggle.
   - **Saved Kshans Rows**: Pinned card at `100%` `#121620` + `1.dp` `#2ABFFF` border; unpinned cards at `60%` `#121620` + `1.dp` `6%` white border; colored category dot (`GOAL = #2ABFFF`, `BIRTHDAY = #F472B6`, `HOLIDAY = #E6C87A`, `KSHAN = #10B981`); Pin-only button + Swipe-Left / Long-Press delete.
4. **`Pulse` Tab (`WidgetsDashboardScreen.kt` & `KshanGlanceWidget.kt`)**:
   - Top-level **Live Life Pulse** (Today's Waking Progress %, Quarter Sprint %, 365-Dot Year Matrix / Ring), middle **Interactive `2×2` Quick Date Jumps Bento Grid**, and bottom **Nothing OS Home Screen Widget Studio** where every widget card features a Nothing OS dot-matrix design and a `"+ Pin to Home Screen"` button.
