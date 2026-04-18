package com.vadimski.calculon.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vadimski.calculon.model.DivisionPhase
import com.vadimski.calculon.model.StepFeedback
import com.vadimski.calculon.ui.components.DivisionProblemView
import com.vadimski.calculon.ui.components.NumberPad
import com.vadimski.calculon.ui.theme.ErrorRed
import com.vadimski.calculon.ui.theme.SuccessGreen
import com.vadimski.calculon.viewmodel.DivisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivisionScreen(onBack: () -> Unit) {
    val vm: DivisionViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Reset input whenever step or phase changes
    var input by remember(state.currentStepIndex, state.phase, state.isComplete, state.totalProblems, state.feedback) {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("División") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Problemas: ${state.totalProblems}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Perfectos: ${state.totalPerfect}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SuccessGreen
                )
            }

            Spacer(Modifier.height(12.dp))

            // Problem view (bracket + step work)
            DivisionProblemView(
                state = state,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Input area + feedback (hidden when complete)
            if (!state.isComplete) {
                val currentStep = state.steps[state.currentStepIndex]

                // Phase prompt
                val prompt = when (state.phase) {
                    DivisionPhase.QUOTIENT_DIGIT ->
                        "¿Cuántas veces cabe el ${state.problem.divisor} en ${currentStep.partialDividend}?"
                    DivisionPhase.STEP_REMAINDER ->
                        "${currentStep.partialDividend} − ${currentStep.product} = ?"
                }
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                // Input display
                Text(
                    text = input.ifEmpty { "—" },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = when (state.feedback) {
                                StepFeedback.CORRECT -> SuccessGreen
                                StepFeedback.WRONG, StepFeedback.SHOW_SOLUTION -> ErrorRed
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Feedback banner
                when (state.feedback) {
                    StepFeedback.WRONG -> {
                        Text(
                            "Inténtalo de nuevo",
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { vm.showSolution() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) { Text("Ver solución") }
                    }
                    StepFeedback.CORRECT -> {
                        Text(
                            "¡Correcto!",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { vm.continueAfterFeedback() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) { Text("Continuar →") }
                    }
                    StepFeedback.SHOW_SOLUTION -> {
                        val correct = when (state.phase) {
                            DivisionPhase.QUOTIENT_DIGIT -> currentStep.quotientDigit
                            DivisionPhase.STEP_REMAINDER -> currentStep.stepRemainder
                        }
                        Text(
                            "La respuesta es $correct",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { vm.continueAfterFeedback() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Continuar →") }
                    }
                    StepFeedback.NONE -> {}
                }

                Spacer(Modifier.height(12.dp))

                // NumberPad — shown when input is expected
                if (state.feedback == StepFeedback.NONE || state.feedback == StepFeedback.WRONG) {
                    val maxLen = if (state.phase == DivisionPhase.QUOTIENT_DIGIT) 1 else 3
                    NumberPad(
                        onDigit = { d -> if (input.length < maxLen) input += d },
                        onBackspace = { if (input.isNotEmpty()) input = input.dropLast(1) },
                        onConfirm = { vm.submitInput(input) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Completion panel
            if (state.isComplete) {
                val anyError = state.completedResults.any { it.hadError }
                val errorSteps = state.completedResults
                    .mapIndexedNotNull { i, r -> if (r.hadError) i + 1 else null }

                if (anyError) {
                    Text(
                        "Errores en: Paso ${errorSteps.joinToString(", Paso ")}",
                        color = ErrorRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        "¡Perfecto! Sin errores",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { vm.nextProblem() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Siguiente problema →", fontSize = 18.sp) }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
