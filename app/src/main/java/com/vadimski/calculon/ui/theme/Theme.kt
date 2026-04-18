package com.vadimski.calculon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue700,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue200,
    secondary = Orange800,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Orange200,
    background = BackgroundWhite,
    surface = SurfaceGray,
    error = ErrorRed
)

@Composable
fun CalculonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
