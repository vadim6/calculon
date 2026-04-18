package com.vadimski.calculon.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadimski.calculon.model.DivisionPhase
import com.vadimski.calculon.model.DivisionSessionState
import com.vadimski.calculon.model.DivisionStep
import com.vadimski.calculon.model.StepFeedback
import com.vadimski.calculon.ui.theme.ErrorRed
import com.vadimski.calculon.ui.theme.SuccessGreen

// ── Top-level entry point ─────────────────────────────────────────────────────

@Composable
fun DivisionProblemView(state: DivisionSessionState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DivisionBracket(
            dividend = state.problem.dividend,
            divisor = state.problem.divisor,
            confirmedQuotient = confirmedQuotientStr(state),
            pendingCount = pendingBlanks(state)
        )
        Spacer(Modifier.height(12.dp))
        DivisionWorkArea(state = state)
    }
}

// ── L-bracket header (Canvas) ─────────────────────────────────────────────────

@Composable
private fun DivisionBracket(
    dividend: Int,
    divisor: Int,
    confirmedQuotient: String,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textSizePx = with(density) { 44.sp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }
    val gapPx = with(density) { 10.dp.toPx() }
    val padPx = with(density) { 16.dp.toPx() }

    val paint = remember { Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD } }
    val linePaint = remember { Paint().apply { isAntiAlias = true; strokeWidth = strokePx } }

    androidx.compose.foundation.Canvas(
        modifier = modifier.fillMaxWidth().height(140.dp)
    ) {
        paint.textSize = textSizePx
        linePaint.color = Color.Black.toArgb()

        val fm = paint.fontMetrics
        val lineH = -fm.ascent + fm.descent + gapPx

        val dividendText = dividend.toString()
        val divisorText = divisor.toString()
        val dividendW = paint.measureText(dividendText)

        val vertX = padPx + dividendW + gapPx
        val divisorX = vertX + gapPx
        val topBaseline = padPx - fm.ascent
        val hLineY = topBaseline + fm.descent + gapPx

        // Measure quotient area width for horizontal line
        paint.color = Color.Black.toArgb()
        val confirmedW = paint.measureText(confirmedQuotient)
        paint.color = Color.Gray.toArgb()
        val pendingW = paint.measureText("_".repeat(pendingCount))
        val hLineEnd = maxOf(divisorX + paint.measureText(divisorText), divisorX + confirmedW + pendingW) + gapPx

        val quotientBaseline = hLineY + gapPx - fm.ascent

        // Dividend
        paint.color = Color.Black.toArgb()
        drawContext.canvas.nativeCanvas.drawText(dividendText, padPx, topBaseline, paint)

        // Vertical bar
        linePaint.color = Color.Black.toArgb()
        drawContext.canvas.nativeCanvas.drawLine(vertX, 0f, vertX, size.height, linePaint)

        // Divisor
        paint.color = Color.Black.toArgb()
        drawContext.canvas.nativeCanvas.drawText(divisorText, divisorX, topBaseline, paint)

        // Horizontal bar (bottom of L)
        drawContext.canvas.nativeCanvas.drawLine(vertX, hLineY, hLineEnd, hLineY, linePaint)

        // Confirmed quotient digits (black)
        if (confirmedQuotient.isNotEmpty()) {
            paint.color = Color.Black.toArgb()
            drawContext.canvas.nativeCanvas.drawText(confirmedQuotient, divisorX, quotientBaseline, paint)
        }
        // Pending blanks (gray)
        if (pendingCount > 0) {
            paint.color = Color.Gray.toArgb()
            val offsetX = divisorX + confirmedW
            drawContext.canvas.nativeCanvas.drawText("_".repeat(pendingCount), offsetX, quotientBaseline, paint)
        }
    }
}

// ── Step work area (Compose + monospace text) ─────────────────────────────────

@Composable
private fun DivisionWorkArea(state: DivisionSessionState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp)
    ) {
        // Completed steps
        state.completedResults.forEachIndexed { index, result ->
            val nextPartial = state.steps.getOrNull(index + 1)?.partialDividend
            StepWorkBlock(
                stepNumber = index + 1,
                step = result.step,
                showProduct = true,
                showRemainder = true,
                hadError = result.hadError,
                nextPartial = nextPartial
            )
            Spacer(Modifier.height(4.dp))
        }

        // Current step (if not complete)
        if (!state.isComplete) {
            val step = state.steps[state.currentStepIndex]
            val quotientConfirmed = state.phase == DivisionPhase.STEP_REMAINDER ||
                    state.feedback == StepFeedback.CORRECT ||
                    state.feedback == StepFeedback.SHOW_SOLUTION
            val remainderConfirmed = state.phase == DivisionPhase.STEP_REMAINDER &&
                    (state.feedback == StepFeedback.CORRECT || state.feedback == StepFeedback.SHOW_SOLUTION)

            StepWorkBlock(
                stepNumber = state.currentStepIndex + 1,
                step = step,
                showProduct = quotientConfirmed,
                showRemainder = remainderConfirmed,
                hadError = null, // still in progress
                nextPartial = null
            )
        }

        // Completion result line
        if (state.isComplete) {
            Spacer(Modifier.height(8.dp))
            val remainder = state.steps.last().stepRemainder
            val resultText = buildString {
                append("${state.problem.dividend} ÷ ${state.problem.divisor} = ${state.problem.quotient}")
                if (remainder > 0) append(", R $remainder")
            }
            val anyError = state.completedResults.any { it.hadError }
            Text(
                text = resultText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (anyError) MaterialTheme.colorScheme.onSurface else SuccessGreen
            )
        }
    }
}

// ── Single step block ─────────────────────────────────────────────────────────

@Composable
private fun StepWorkBlock(
    stepNumber: Int,
    step: DivisionStep,
    showProduct: Boolean,
    showRemainder: Boolean,
    hadError: Boolean?,   // null = in progress
    nextPartial: Int?
) {
    val headerColor = when (hadError) {
        true -> ErrorRed
        false -> SuccessGreen
        null -> MaterialTheme.colorScheme.primary
    }
    val headerSuffix = when (hadError) {
        true -> " ✗"
        false -> " ✓"
        null -> ""
    }

    Column {
        Text(
            text = "Paso $stepNumber$headerSuffix",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = headerColor
        )
        // Arithmetic rows — monospace, right-aligned in a 5-char column
        ArithRow(step.partialDividend.toString().padStart(5))
        if (showProduct) {
            ArithRow(("- " + step.product.toString()).padStart(5))
            ArithRow("─────")
            if (showRemainder) {
                ArithRow(step.stepRemainder.toString().padStart(5))
            }
        }
    }
}

@Composable
private fun ArithRow(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun confirmedQuotientStr(state: DivisionSessionState): String {
    val sb = StringBuilder()
    state.completedResults.forEach { sb.append(it.step.quotientDigit) }
    val showCurrent = state.phase == DivisionPhase.STEP_REMAINDER ||
            state.feedback == StepFeedback.CORRECT ||
            state.feedback == StepFeedback.SHOW_SOLUTION
    if (!state.isComplete && showCurrent && state.currentStepIndex < state.steps.size) {
        sb.append(state.steps[state.currentStepIndex].quotientDigit)
    }
    return sb.toString()
}

private fun pendingBlanks(state: DivisionSessionState): Int {
    if (state.isComplete) return 0
    return state.steps.size - confirmedQuotientStr(state).length
}
