package com.vadimski.calculon.viewmodel

import androidx.lifecycle.ViewModel
import com.vadimski.calculon.model.MultiplicationSessionState
import com.vadimski.calculon.model.MultiplicationStepResult
import com.vadimski.calculon.model.StepFeedback
import com.vadimski.calculon.util.computeMultiplicationSteps
import com.vadimski.calculon.util.generateMultiplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MultiplicationViewModel : ViewModel() {

    private val recentResults = mutableListOf<Boolean>()

    private val _state = MutableStateFlow(freshSession(level = 1, streak = 0, totalProblems = 0, totalPerfect = 0))
    val state: StateFlow<MultiplicationSessionState> = _state.asStateFlow()

    fun submitInput(input: String) {
        if (input.isBlank()) return
        val s = _state.value
        if (s.isComplete || s.feedback == StepFeedback.CORRECT || s.feedback == StepFeedback.SHOW_SOLUTION) return
        val step = s.steps[s.currentStepIndex]
        if (input.toIntOrNull() == step.rawProduct) {
            _state.update { it.copy(feedback = StepFeedback.CORRECT) }
        } else {
            _state.update { it.copy(feedback = StepFeedback.WRONG, currentStepHadError = true) }
        }
    }

    fun showSolution() {
        _state.update { it.copy(feedback = StepFeedback.SHOW_SOLUTION, currentStepHadError = true) }
    }

    fun continueAfterFeedback() {
        val s = _state.value
        val stepResult = MultiplicationStepResult(s.steps[s.currentStepIndex], s.currentStepHadError)
        val newCompleted = s.completedResults + stepResult
        val nextIndex = s.currentStepIndex + 1

        if (nextIndex >= s.steps.size) {
            val perfect = newCompleted.none { it.hadError }
            recentResults.add(perfect)
            if (recentResults.size > 4) recentResults.removeAt(0)

            val newStreak = if (perfect) s.streak + 1 else 0
            val promoted = newStreak >= 3 && s.level < 3
            val wrongInLast4 = recentResults.count { !it }
            val demoted = !perfect && wrongInLast4 >= 2 && s.level > 1

            _state.update {
                it.copy(
                    completedResults = newCompleted,
                    feedback = StepFeedback.NONE,
                    isComplete = true,
                    streak = if (promoted) 0 else newStreak,
                    level = when {
                        promoted -> s.level + 1
                        demoted -> s.level - 1
                        else -> s.level
                    },
                    totalProblems = it.totalProblems + 1,
                    totalPerfect = if (perfect) it.totalPerfect + 1 else it.totalPerfect
                )
            }
        } else {
            _state.update {
                it.copy(
                    currentStepIndex = nextIndex,
                    completedResults = newCompleted,
                    feedback = StepFeedback.NONE,
                    currentStepHadError = false
                )
            }
        }
    }

    // Used on the last step's CORRECT path: records the result and immediately starts the next
    // problem, skipping the intermediate completion panel.
    fun finishAndNext() {
        val s = _state.value
        val stepResult = MultiplicationStepResult(s.steps[s.currentStepIndex], s.currentStepHadError)
        val newCompleted = s.completedResults + stepResult
        val perfect = newCompleted.none { it.hadError }
        recentResults.add(perfect)
        if (recentResults.size > 4) recentResults.removeAt(0)
        val newStreak = if (perfect) s.streak + 1 else 0
        val promoted = newStreak >= 3 && s.level < 3
        val wrongInLast4 = recentResults.count { !it }
        val demoted = !perfect && wrongInLast4 >= 2 && s.level > 1
        val newLevel = when {
            promoted -> s.level + 1
            demoted -> s.level - 1
            else -> s.level
        }
        _state.value = freshSession(
            level = newLevel,
            streak = if (promoted) 0 else newStreak,
            totalProblems = s.totalProblems + 1,
            totalPerfect = if (perfect) s.totalPerfect + 1 else s.totalPerfect
        )
    }

    fun nextProblem() {
        val s = _state.value
        _state.value = freshSession(s.level, s.streak, s.totalProblems, s.totalPerfect)
    }

    private fun freshSession(
        level: Int, streak: Int, totalProblems: Int, totalPerfect: Int
    ): MultiplicationSessionState {
        val problem = generateMultiplication(level)
        return MultiplicationSessionState(
            problem = problem,
            steps = computeMultiplicationSteps(problem),
            level = level,
            streak = streak,
            totalProblems = totalProblems,
            totalPerfect = totalPerfect
        )
    }
}
