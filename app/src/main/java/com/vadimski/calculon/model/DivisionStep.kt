package com.vadimski.calculon.model

data class DivisionStep(
    val partialDividend: Int,
    val quotientDigit: Int,
    val product: Int,       // quotientDigit × divisor
    val stepRemainder: Int  // partialDividend − product
)

data class StepResult(
    val step: DivisionStep,
    val hadError: Boolean
)

enum class DivisionPhase { QUOTIENT_DIGIT, STEP_REMAINDER }
enum class StepFeedback { NONE, CORRECT, WRONG, SHOW_SOLUTION }

data class DivisionSessionState(
    val problem: Problem.Division,
    val steps: List<DivisionStep>,
    val currentStepIndex: Int = 0,
    val phase: DivisionPhase = DivisionPhase.QUOTIENT_DIGIT,
    val completedResults: List<StepResult> = emptyList(),
    val feedback: StepFeedback = StepFeedback.NONE,
    val currentStepHadError: Boolean = false,
    val isComplete: Boolean = false,
    val totalProblems: Int = 0,
    val totalPerfect: Int = 0
)
