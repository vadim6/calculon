# Calculón — CLAUDE.md

## Project Overview

Android app for a 9-year-old studying math in Spain. Two practice modes: **Multiplication** and **Division**, both rendered in the **Spanish visual method** — which differs from US/UK formats and is not supported by any existing math app library. This is the core differentiator.

Target user: single child, ~9 years old, Spanish primary school curriculum (Primaria).

---

## Tech Stack

- **Language**: Kotlin (no Java)
- **UI**: Jetpack Compose (declarative, no XML layouts)
- **Architecture**: MVVM with Android ViewModel + StateFlow
- **Storage**: None — fully local, in-memory session state only
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: Latest stable
- **No backend, no auth, no database, no network calls**

---

## Package Structure

```
com.vadimski.calculon
├── ui
│   ├── screens
│   │   ├── HomeScreen.kt
│   │   ├── MultiplicationScreen.kt
│   │   └── DivisionScreen.kt
│   ├── components
│   │   ├── MultiplicationCanvas.kt   ← custom Canvas renderer
│   │   ├── DivisionCanvas.kt         ← custom Canvas renderer
│   │   ├── NumberPad.kt
│   │   └── FeedbackOverlay.kt
│   └── theme
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── viewmodel
│   └── SessionViewModel.kt
├── model
│   ├── Problem.kt
│   └── SessionState.kt
└── util
    └── ProblemGenerator.kt
```

---

## Data Model

```kotlin
// model/Problem.kt
sealed class Problem {
    data class Multiplication(
        val multiplicand: Int,   // e.g. 234
        val multiplier: Int,     // e.g. 56
        val level: Int           // 1, 2, or 3
    ) : Problem()

    data class Division(
        val dividend: Int,       // e.g. 137
        val divisor: Int,        // e.g. 4
        val quotient: Int,       // 34
        val remainder: Int       // 1 (0 means no remainder)
    ) : Problem()
}

// model/SessionState.kt
data class SessionState(
    val currentProblem: Problem,
    val streak: Int = 0,
    val level: Int = 1,
    val totalCorrect: Int = 0,
    val totalAttempts: Int = 0,
    val feedbackState: FeedbackState = FeedbackState.None
)

enum class FeedbackState { None, Correct, Wrong, ShowSolution }
```

---

## Problem Generation — util/ProblemGenerator.kt

### Multiplication levels:
- **Level 1**: single × single (1–9 × 1–9), times tables practice
- **Level 2**: 3-digit × single (100–999 × 2–9)
- **Level 3**: 3-digit × 2-digit (100–999 × 11–99, avoid ×10 multiples)

### Division:
- Dividend: 10–999
- Divisor: 2–9
- Always compute quotient and remainder: `quotient = dividend / divisor`, `remainder = dividend % divisor`
- No difficulty levels for division — generate across full range

### Generator functions:
```kotlin
fun generateMultiplication(level: Int): Problem.Multiplication
fun generateDivision(): Problem.Division
```

---

## Adaptive Difficulty — viewmodel/SessionViewModel.kt

Streak-based promotion/demotion for Multiplication only:

```
3 correct in a row → promote level (max 3)
2 wrong in last 4 attempts → demote level (min 1)
```

Track a rolling window of last 4 results (Boolean list).

Division has no levels — always generates across full range.

ViewModel exposes:
```kotlin
val sessionState: StateFlow<SessionState>
fun submitAnswer(answer: String)   // validates, updates state, generates next problem
fun nextProblem()
fun resetSession()
```

Answer validation:
- Multiplication: single Int input, compare to `multiplicand × multiplier`
- Division: two inputs — quotient (Int) and remainder (Int, default 0 if empty). Both must match.

---

## Spanish Math Visual Formats

### Division — ui/components/DivisionCanvas.kt

The Spanish *método de división* uses an **L-shaped bracket** on the right side:

```
  137 | 4
      |______
      | 34 R1
```

Layout rules:
- Dividend drawn top-left
- Vertical line separates dividend from divisor area
- Divisor sits top-right of vertical line
- Horizontal line runs under the divisor (forming the L)
- Quotient sits below the horizontal line, right-aligned under divisor
- If remainder > 0, show as "R{remainder}" after quotient

Implement as a `@Composable` using `Canvas {}` with `drawLine`, `drawText` (via `nativeCanvas.drawText`), positioned with explicit coordinates calculated from measured text widths. Accept a `Problem.Division` as parameter plus a `showSolution: Boolean`.

### Multiplication — ui/components/MultiplicationCanvas.kt

**Level 1 & 2** (single-digit multiplier):
```
   234
  ×  6
  ────
  1404
```

**Level 3** (two-digit multiplier) — Spanish partial products layout:
```
    234
  ×  56
  ─────
   1404    ← 234 × 6 (units digit)
  1170·    ← 234 × 5 (tens digit), shifted left by one, dot marks the zero placeholder
  ──────
  13104
```

Layout rules:
- Right-align all numbers on a common right edge
- Horizontal rule after multiplier, and after last partial product
- For level 3: each partial product row shifts left by its digit position
- Use a dot `·` or `0` as the shift placeholder (Spanish textbooks use a small dot)
- Final sum at the bottom

Implement as a `@Composable` using `Canvas {}`. Accept a `Problem.Multiplication` and `showSolution: Boolean`.

---

## Input — ui/components/NumberPad.kt

Custom numeric keypad composable. **Do not use the system keyboard** — it's bad UX for kids.

- Digits 0–9 in a 3×4 grid (standard phone layout)
- Backspace button
- Confirm/Check button (✓)
- For Division: two input fields — "Cociente" (quotient) and "Resto" (remainder). Tapping each field focuses it for the numpad input.

---

## UI/UX Guidelines

- **Language**: Spanish throughout. Use Spanish math terminology:
  - División: *dividendo*, *divisor*, *cociente*, *resto*
  - Multiplicación: *multiplicando*, *multiplicador*, *producto*
- **Tone**: Friendly, encouraging, child-appropriate. No harsh failure states.
- **Feedback**:
  - ✓ Correct: green highlight, short encouraging message (e.g. "¡Muy bien!", "¡Correcto!")
  - ✗ Wrong: red highlight, "Inténtalo de nuevo" — allow one retry before offering "Ver solución"
  - "Ver solución" reveals the full worked layout via `showSolution = true` on the Canvas
- **Navigation**: Simple — Home → Mode screen. Back button returns to Home. No deep nav stack.
- **Accessibility**: Large touch targets (min 48dp), high contrast text, no time pressure

---

## Theme — ui/theme/

Use Material3. Child-friendly palette:
- Primary: warm blue `#1976D2`
- Secondary: warm orange `#FF8F00`
- Background: soft white `#FAFAFA`
- Error: `#D32F2F`
- Typography: use `Nunito` from Google Fonts (rounded, friendly, readable for kids)

---

## What NOT to Build (MVP Scope)

- No user accounts or profiles
- No progress persistence across app restarts (v2)
- No sound effects (v2)
- No animations beyond simple fade for feedback (v2)
- No other operations (addition, subtraction) — v2
- No tablets-specific layout — phone only for now
- No localization beyond Spanish

---

## Google Play Store Notes

- App targets kids — ensure compliance with **Designed for Families** policy
- No ads, no in-app purchases, no data collection → simplifies compliance significantly
- Content rating: Everyone / PEGI 3

---

## Implementation Order

When implementing, follow this order:

1. `Problem.kt` + `SessionState.kt` — data models
2. `ProblemGenerator.kt` — pure logic, no UI
3. `SessionViewModel.kt` — state management
4. `Theme.kt` + `Color.kt` + `Type.kt` — visual foundation
5. `NumberPad.kt` — reusable input component
6. `DivisionCanvas.kt` — the hardest UI piece, do this before multiplication
7. `MultiplicationCanvas.kt` — levels 1/2 first, then level 3 partial products
8. `FeedbackOverlay.kt`
9. `DivisionScreen.kt` + `MultiplicationScreen.kt` — wire everything together
10. `HomeScreen.kt` — mode selector, last because it's the simplest
