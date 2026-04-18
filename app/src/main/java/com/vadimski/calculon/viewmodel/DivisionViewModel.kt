package com.vadimski.calculon.viewmodel

import androidx.lifecycle.ViewModel
import com.vadimski.calculon.model.DivisionPhase
import com.vadimski.calculon.model.DivisionSessionState
import com.vadimski.calculon.model.StepFeedback
import com.vadimski.calculon.model.StepResult
import com.vadimski.calculon.util.computeDivisionSteps
import com.vadimski.calculon.util.generateDivision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DivisionViewModel : ViewModel() {

    private val _state = MutableStateFlow(freshSession(0, 0))
    val state: StateFlow<DivisionSessionState> = _state.asStateFlow()

    fun submitInput(input: String) {
        if (input.isBlank()) return
        val s = _state.value
        if (s.isComplete || s.feedback == StepFeedback.CORRECT || s.feedback == StepFeedback.SHOW_SOLUTION) return

        val step = s.steps[s.currentStepIndex]
        val expected = when (s.phase) {
            DivisionPhase.QUOTIENT_DIGIT -> step.quotientDigit
            DivisionPhase.STEP_REMAINDER -> step.stepRemainder
        }

        if (input.toIntOrNull() == expected) {
            _state.update { it.copy(feedback = StepFeedback.CORRECT) }
        } else {
            _state.update {
                it.copy(feedback = StepFeedback.WRONG, currentStepHadError = true)
            }
        }
    }

    fun showSolution() {
        _state.update { it.copy(feedback = StepFeedback.SHOW_SOLUTION, currentStepHadError = true) }
    }

    fun continueAfterFeedback() {
        val s = _state.value
        when (s.phase) {
            DivisionPhase.QUOTIENT_DIGIT ->
                _state.update { it.copy(phase = DivisionPhase.STEP_REMAINDER, feedback = StepFeedback.NONE) }

            DivisionPhase.STEP_REMAINDER -> {
                val result = StepResult(s.steps[s.currentStepIndex], s.currentStepHadError)
                val newCompleted = s.completedResults + result
                val nextIndex = s.currentStepIndex + 1

                if (nextIndex >= s.steps.size) {
                    val perfect = newCompleted.none { it.hadError }
                    _state.update {
                        it.copy(
                            completedResults = newCompleted,
                            feedback = StepFeedback.NONE,
                            isComplete = true,
                            totalProblems = it.totalProblems + 1,
                            totalPerfect = if (perfect) it.totalPerfect + 1 else it.totalPerfect
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            currentStepIndex = nextIndex,
                            phase = DivisionPhase.QUOTIENT_DIGIT,
                            completedResults = newCompleted,
                            feedback = StepFeedback.NONE,
                            currentStepHadError = false
                        )
                    }
                }
            }
        }
    }

    fun nextProblem() {
        val s = _state.value
        _state.value = freshSession(s.totalProblems, s.totalPerfect)
    }

    private fun freshSession(totalProblems: Int, totalPerfect: Int): DivisionSessionState {
        val problem = generateDivision()
        return DivisionSessionState(
            problem = problem,
            steps = computeDivisionSteps(problem.dividend, problem.divisor),
            totalProblems = totalProblems,
            totalPerfect = totalPerfect
        )
    }
}
