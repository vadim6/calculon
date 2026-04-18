package com.vadimski.calculon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadimski.calculon.model.MultiplicationSessionState
import com.vadimski.calculon.model.StepFeedback
import com.vadimski.calculon.ui.theme.SuccessGreen

/**
 * Renders the Spanish long-multiplication layout progressively as the child solves steps.
 *
 * Digits appear right-to-left as each step is confirmed. Carry is NOT shown in the
 * layout — it is shown as a text hint in the screen. The overflow carry digit (the
 * leading digit that comes from the final step's carry) only appears once all steps
 * in a row are done, matching how a child writes it on paper.
 *
 * Level 1 (A × B): 1 row, 1 step.
 * Level 2 (ABC × D): 1 row, 3 steps, digits appear right-to-left.
 * Level 3 (ABC × DE): 2 rows of 3 steps each, then a rule + final sum.
 */
@Composable
fun MultiplicationProblemView(
    state: MultiplicationSessionState,
    modifier: Modifier = Modifier
) {
    val problem = state.problem
    val isLevel3 = problem.level == 3
    val multiplicandStr = problem.multiplicand.toString()
    val multiplierStr = "×${problem.multiplier}"
    val stepsInRow = multiplicandStr.length

    val multiplierDigits = problem.multiplier.toString().map { it.digitToInt() }.reversed()
    val finalSum = problem.multiplicand * problem.multiplier

    // Column width = widest string that will ever appear
    val colWidth = run {
        val candidates = mutableListOf(multiplicandStr.length, multiplierStr.length)
        multiplierDigits.forEachIndexed { i, d ->
            val suffix = if (isLevel3 && i > 0) 1 else 0
            candidates.add((problem.multiplicand * d).toString().length + suffix)
        }
        if (isLevel3) candidates.add(finalSum.toString().length)
        candidates.max()
    }
    val rule = "─".repeat(colWidth)

    val completedByRow = state.completedResults.groupBy { it.step.partialRowIndex }
    val currentStep = if (!state.isComplete) state.steps.getOrNull(state.currentStepIndex) else null
    val revealCurrent = state.feedback == StepFeedback.CORRECT || state.feedback == StepFeedback.SHOW_SOLUTION

    fun effectiveCompleted(rowIndex: Int): Int {
        val base = completedByRow[rowIndex]?.size ?: 0
        val bonus = if (revealCurrent && currentStep?.partialRowIndex == rowIndex) 1 else 0
        return base + bonus
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MultiRow(multiplicandStr.padStart(colWidth))
        MultiRow(multiplierStr.padStart(colWidth))
        MultiRow(rule)

        multiplierDigits.forEachIndexed { rowIndex, mDigit ->
            val done = effectiveCompleted(rowIndex)
            val fullProduct = (problem.multiplicand * mDigit).toString()
            val suffix = if (isLevel3 && rowIndex > 0) "·" else ""

            val display: String? = when {
                done >= stepsInRow -> fullProduct + suffix
                done > 0 -> fullProduct.takeLast(done) + suffix
                else -> null
            }
            if (display != null) {
                MultiRow(display.padStart(colWidth))
            }
        }

        // Second rule + sum for level 3 once both rows are fully shown
        val allRowsDone = state.isComplete ||
            multiplierDigits.indices.all { effectiveCompleted(it) >= stepsInRow }
        if (isLevel3 && allRowsDone) {
            MultiRow(rule)
            MultiRow(finalSum.toString().padStart(colWidth))
        }

        if (state.isComplete) {
            Spacer(Modifier.height(8.dp))
            val anyError = state.completedResults.any { it.hadError }
            Text(
                text = "${problem.multiplicand} × ${problem.multiplier} = $finalSum",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (anyError) MaterialTheme.colorScheme.onSurface else SuccessGreen
            )
        }
    }
}

@Composable
private fun MultiRow(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 32.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}
