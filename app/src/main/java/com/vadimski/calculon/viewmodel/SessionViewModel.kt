package com.vadimski.calculon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vadimski.calculon.model.FeedbackState
import com.vadimski.calculon.model.Problem
import com.vadimski.calculon.model.SessionState
import com.vadimski.calculon.util.generateDivision
import com.vadimski.calculon.util.generateMultiplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SessionViewModel(val mode: PracticeMode) : ViewModel() {

    enum class PracticeMode { MULTIPLICATION, DIVISION }

    private val recentResults = mutableListOf<Boolean>()

    private val _sessionState = MutableStateFlow(
        SessionState(currentProblem = initialProblem(mode, level = 1))
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun submitAnswer(answer: String, remainder: String = "0") {
        val state = _sessionState.value
        val isCorrect = when (val p = state.currentProblem) {
            is Problem.Multiplication -> answer.toIntOrNull() == p.multiplicand * p.multiplier
            is Problem.Division -> {
                val q = answer.toIntOrNull() ?: return
                val r = remainder.toIntOrNull() ?: 0
                q == p.quotient && r == p.remainder
            }
        }

        recentResults.add(isCorrect)
        if (recentResults.size > 4) recentResults.removeAt(0)

        if (isCorrect) {
            val newStreak = state.streak + 1
            val promoted = mode == PracticeMode.MULTIPLICATION && newStreak >= 3 && state.level < 3
            _sessionState.update {
                it.copy(
                    streak = if (promoted) 0 else newStreak,
                    level = if (promoted) state.level + 1 else state.level,
                    totalCorrect = state.totalCorrect + 1,
                    totalAttempts = state.totalAttempts + 1,
                    feedbackState = FeedbackState.Correct
                )
            }
        } else {
            val wrongInLast4 = recentResults.takeLast(4).count { !it }
            val demoted = mode == PracticeMode.MULTIPLICATION && wrongInLast4 >= 2 && state.level > 1
            val nextFeedback = FeedbackState.Wrong
            _sessionState.update {
                it.copy(
                    streak = 0,
                    level = if (demoted) state.level - 1 else state.level,
                    totalAttempts = state.totalAttempts + 1,
                    feedbackState = nextFeedback
                )
            }
        }
    }

    fun showSolution() {
        _sessionState.update { it.copy(feedbackState = FeedbackState.ShowSolution) }
    }

    fun nextProblem() {
        val level = _sessionState.value.level
        _sessionState.update {
            it.copy(
                currentProblem = initialProblem(mode, level),
                feedbackState = FeedbackState.None
            )
        }
    }

    fun resetSession() {
        recentResults.clear()
        _sessionState.value = SessionState(currentProblem = initialProblem(mode, level = 1))
    }

    class Factory(private val mode: PracticeMode) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(mode) as T
    }
}

private fun initialProblem(mode: SessionViewModel.PracticeMode, level: Int) =
    if (mode == SessionViewModel.PracticeMode.MULTIPLICATION)
        generateMultiplication(level)
    else
        generateDivision()
