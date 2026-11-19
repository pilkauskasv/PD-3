package com.example.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalCalculatorColors = staticCompositionLocalOf { LightCalculatorColors }

@Composable
fun Win11CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val calcColors = if (darkTheme) DarkCalculatorColors else LightCalculatorColors
    val materialScheme = if (darkTheme) {
        darkColorScheme(
            background = calcColors.background,
            surface = calcColors.historySheetBackground,
            primary = calcColors.equalsKeyContainer,
            onPrimary = calcColors.equalsText,
            onBackground = calcColors.primaryText,
            onSurface = calcColors.primaryText
        )
    } else {
        lightColorScheme(
            background = calcColors.background,
            surface = calcColors.historySheetBackground,
            primary = calcColors.equalsKeyContainer,
            onPrimary = calcColors.equalsText,
            onBackground = calcColors.primaryText,
            onSurface = calcColors.primaryText
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = CalculatorTypography,
            content = content
        )
    }
}
