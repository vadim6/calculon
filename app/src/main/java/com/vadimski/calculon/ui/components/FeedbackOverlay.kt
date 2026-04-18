package com.vadimski.calculon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vadimski.calculon.model.FeedbackState
import com.vadimski.calculon.ui.theme.ErrorRed
import com.vadimski.calculon.ui.theme.SuccessGreen

private val correctMessages = listOf(
    "¡Muy bien!", "¡Correcto!", "¡Excelente!", "¡Genial!", "¡Perfecto!"
)

@Composable
fun FeedbackOverlay(
    feedbackState: FeedbackState,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onShowSolution: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = feedbackState != FeedbackState.None,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when (feedbackState) {
                        FeedbackState.Correct -> SuccessGreen.copy(alpha = 0.1f)
                        FeedbackState.Wrong, FeedbackState.ShowSolution ->
                            ErrorRed.copy(alpha = 0.08f)
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (feedbackState) {
                    FeedbackState.Correct -> {
                        Text(
                            text = correctMessages.random(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onNext,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Siguiente →", fontSize = 18.sp)
                        }
                    }
                    FeedbackState.Wrong -> {
                        Text(
                            text = "Inténtalo de nuevo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reintentar")
                            }
                            OutlinedButton(
                                onClick = onShowSolution,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ErrorRed
                                )
                            ) {
                                Text("Ver solución")
                            }
                        }
                    }
                    FeedbackState.ShowSolution -> {
                        Text(
                            text = "Solución mostrada",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onNext,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Siguiente →", fontSize = 18.sp)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
