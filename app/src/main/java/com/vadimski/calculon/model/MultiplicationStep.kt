package com.vadimski.calculon.model

data class MultiplicationStep(
    val multiplicandDigit: Int,
    val multiplierDigit: Int,
    val rawProduct: Int,       // what child enters: multiplicandDigit × multiplierDigit
    val carryIn: Int,          // carry into this step (shown as context hint)
    val partialRowIndex: Int,  // 0 = units multiplier row, 1 = tens row (level 3 only)
    val digitPosition: Int     // 0 = rightmost position in partial product
)

data class MultiplicationStepResult(
    val step: MultiplicationStep,
    val hadError: Boolean
)

data class MultiplicationSessionState(
    val problem: Problem.Multiplication,
    val steps: List<MultiplicationStep>,
    val currentStepIndex: Int = 0,
    val completedResults: List<MultiplicationStepResult> = emptyList(),
    val feedback: StepFeedback = StepFeedback.NONE,
    val currentStepHadError: Boolean = false,
    val isComplete: Boolean = false,
    val streak: Int = 0,
    val level: Int = 1,
    val totalProblems: Int = 0,
    val totalPerfect: Int = 0
)
