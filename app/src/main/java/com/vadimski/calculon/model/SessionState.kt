package com.vadimski.calculon.model

data class SessionState(
    val currentProblem: Problem,
    val streak: Int = 0,
    val level: Int = 1,
    val totalCorrect: Int = 0,
    val totalAttempts: Int = 0,
    val feedbackState: FeedbackState = FeedbackState.None
)

enum class FeedbackState { None, Correct, Wrong, ShowSolution }
