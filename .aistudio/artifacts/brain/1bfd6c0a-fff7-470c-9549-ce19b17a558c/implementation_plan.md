# क्षण (Kshan) • Ballistic Rocket Pyrotechnics, Direct Shift Input & Animated Countdown Badges

A major technical and aesthetic elevation delivering an authentic pyrotechnic simulation engine with additive light blending, procedural audio/haptics, direct numeric input for date shifts, premium animated share button, and dynamic Typewriter countdown badges.

---

### User Review & Latest Revisions Incorporated

> [!IMPORTANT]
> The following revisions and user feedback are fully incorporated:
> 1. **Celebration Fireworks Rendering & Full-Screen Smoothness Fix**:
>    - **Root Cause & Fix**: Compose `Canvas` draw lambdas must explicitly read animation frame state (`tickTrigger`) to register invalidation on every frame. We bind frame state directly inside `Canvas` to guarantee silky-smooth 60/120fps invalidation without missing frames.
>    - **Visibility from Card**: When tapping "Launch Rockets" on the expanded card, rockets spawn from the lower flanks and arc gracefully above and around the card into the open upper sky with high-visibility luminous trails, bursting into clear sight.
>    - **Realistic Motion**: Delta-time integration (`dt`) with calibrated gravity (`0.22f`), air drag (`0.975f`), and multi-point glowing golden comet tails.
> 2. **Premium Animated Share Button**:
>    - Styled with a multi-layered obsidian glass fill, dynamic specular border shimmer, a rotating/pulsing spark icon on click, spring press scale (`0.95f` $\to$ `1.0f`), and rich haptic confirmation.
> 3. **App-Wide Button Haptics**:
>    - Comprehensive haptic feedback across all interactive controls: preset chips in Difference/Shift/Countdown screens, `+` and `−` steppers, Earlier/Later buttons, Skip Weekends toggles, "+ Add Countdown", Pin/Delete buttons, Time chips, and Date Picker dialog actions.
> 4. **Direct Number Input Dialog & Equal Card Heights**:
>    - Tapping the numeric day count (e.g., `7`) opens a clean, dark obsidian Typewriter number-input dialog (with numbers up to 9999).
>    - Base Date and Days to Shift cards matched in height using `Modifier.height(IntrinsicSize.Max)`.
> 5. **Saved Countdown Badges & Selection Animation**:
>    - Each saved countdown card displays a bold Typewriter remaining badge (e.g. `45d left`) in `CourierPrimeFontFamily`.
>    - Selection animation: Pulsing glow with spring scale (`spring(dampingRatio = 0.7f, stiffness = 400f)`) and dynamic category tint when selecting active countdowns.

---

## 1. Architecture & Component Blueprint

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                 CelebrationFireworksOverlay (Full OLED Canvas)              │
│                                                                             │
│  [ [FPS: 60.0] Telemetry ]                       [ 🔊 Audio/Haptics Toggle ]│
│                                                                             │
│  ┌──────────────────────── Twinkling Starfield ──────────────────────────┐  │
│  │ 120 twinkling background stars (sinusoidal alpha/scale over #000000)  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌────────────────────── 2-Stage Ballistic Engine ───────────────────────┐  │
│  │ • Zero-GC Object Pooling: 50 Rockets, 2500 Particles                  │  │
│  │ • Explicit Canvas Frame Recomposition State (Guaranteed 60/120fps)    │  │
│  │ • Stage 1: Ascending rockets with glowing 6-point golden comet tail   │  │
│  │ • Stage 2 Apex Bursts:                                                │  │
│  │    1. Chrysanthemum Sphere (140 radial embers + linger trails)        │  │
│  │    2. Golden & Cyan Willow (high-drag, weeping gold/cyan streamers)   │  │
│  │    3. Saturn Double-Ring (outer cyan/purple ring + inner gold ring)   │  │
│  │    4. Strobe Crackle (white-hot flash + 3 micro-spark splits)         │  │
│  │ • BlendMode.Plus: Additive light blending for blazing white-hot cores │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌────────────────── Minimizable Card / Sky View ────────────────────────┐  │
│  │ Expanded: Top-lit frosted card with "क्षण • COUNTDOWN SKY PREVIEW"     │  │
│  │           or "क्षण • MILESTONE ARRIVED" + Typewriter remaining pill    │  │
│  │           + Premium Animated Share Button                             │  │
│  │ Collapsed: Floating Top Pill [ 🎆 Tap sky to launch • Show Card • Exit]│  │
│  │ Touch Interaction: Tap or drag anywhere on sky to launch rockets      │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Proposed Implementation Steps

### 1. Upgrade `CelebrationFireworksOverlay.kt`
- **Canvas Recomposition & Frame Binding**:
  - Read `tickTrigger` directly inside the Canvas draw scope so every physics update frame immediately renders.
  - Fix rocket launch positioning so rockets arc above and around the center modal into unobstructed view.
- **Additive Canvas Blending (`BlendMode.Plus`)**:
  - Draw particles and comet tails with `BlendMode.Plus` so intersecting sparks glow brilliantly white-hot at explosion centers.
- **4 Advanced Pyrotechnic Burst Shapes**:
  1. **Chrysanthemum Sphere**: 140 radial velocity embers with air friction (0.975f), gravity (0.45f), and subtle trailing embers.
  2. **Golden & Cyan Willow**: High-drag (0.95f), slow weeping gold (`#FFD700`) and electric cyan (`#00F2FE`) streamers with long decay.
  3. **Saturn Double-Ring**: Two concentric circular shockwaves: an outer cyan/purple ring with 80 sparks, and an inner tilted golden ring with 45 sparks.
  4. **Strobe Crackle**: High-velocity sparks that flash white-hot midway through life and split into 3 micro-sparks right before decay.
- **Telemetry HUD**:
  - Top-left frosted Typewriter badge (`Courier Prime`) calculating real-time FPS (`fps = 1000f / frameTimeMs`).
- **Procedural Sound & Haptics Engine**:
  - Low-latency synthesized launch whoosh and detonation pops using an in-memory `AudioTrack` with generated PCM waves.
  - Multi-stage vibration effects on launch and detonation.
  - Top-right frosted toggle button (`🔊` / `🔇`) to mute or enable audio and haptics instantly.
- **Premium Animated Share Button**:
  - Multi-layered frosted container with gradient border shimmer, animated rotating/scaling share spark icon, spring press animation, and haptic feedback.

### 2. Shift Screen Direct Number Input (`DateMathScreen.kt`)
- **Direct Number Input Dialog**:
  - Clicking on the large day number in the "Days to Shift" card opens `TypewriterNumberInputDialog`.
  - Clean obsidian modal with numeric keypad input, quick-step chips (`+10`, `+30`, `+100`, `365`), and instant calculation.
- **Equal Height Geometry**:
  - Apply `Modifier.height(IntrinsicSize.Max)` inside `ConnectedDateCards` to ensure both glass cards have identical heights.
- **Comprehensive Haptics**:
  - Add tactile haptic clicks to the preset chips, step +/- buttons, earlier/later buttons, and weekend switch.

### 3. Saved Countdowns Badge & Selection Animation (`CountdownScreen.kt`)
- **Bold Typewriter Remaining Badge**:
  - Add an engineered `[ 45d left ]` badge rendered in `CourierPrimeFontFamily` with category accent colors on each saved countdown card.
- **Pulsing Glow & Spring Scale Selection**:
  - When an event is selected or active, animate the card with a pulsing ambient border aura (`animateFloat` with `infiniteRepeatable`), a scale spring (`animateFloatAsState(spring(dampingRatio = 0.7f, stiffness = 400f))`), and a vibrant category color wash.
- **App-Wide Button Haptics**:
  - Add haptic clicks to card selection, pin toggle, delete button, "+ Add Countdown", and modal actions.

---

## 3. Verification Plan

- **Compilation Verification**: Run `compile_applet` to confirm clean compilation across all Jetpack Compose UI, Canvas drawing, AudioTrack synthesis, and IntrinsicSize measurements.
- **Unit & Robolectric Testing**: Run `gradle :app:testDebugUnitTest` to ensure all tests pass without regressions.
- **Functional Checks**:
  - Verify tapping "Launch Rockets" immediately launches visible rockets that arc into open sky.
  - Verify smooth 60/120fps animation in both minimized sky mode and expanded card mode.
  - Verify top-left Typewriter FPS badge displays live FPS.
  - Verify top-right 🔊/🔇 toggle silences procedural audio and vibrations.
  - Verify tapping "7" opens number input dialog, and cards match height.
  - Verify saved countdown cards display bold Typewriter remaining day badges with spring scale selection animation.
  - Verify premium animations and haptics on the Share button and all buttons.
