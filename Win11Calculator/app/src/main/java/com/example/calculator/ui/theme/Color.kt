package com.example.calculator.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CalculatorColors(
    val background: Color,
    val digitKeyContainer: Color,
    val digitKeyPressed: Color,
    val functionKeyContainer: Color,
    val functionKeyPressed: Color,
    val equalsKeyContainer: Color,
    val equalsKeyPressed: Color,
    val keyBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val disabledText: Color,
    val equalsText: Color,
    val historySheetBackground: Color,
    val scrim: Color,
    val memoryRipple: Color
)

val LightCalculatorColors = CalculatorColors(
    background = Color(0xFFF3F3F3),
    digitKeyContainer = Color(0xFFFFFFFF),
    digitKeyPressed = Color(0xFFF6F6F6),
    functionKeyContainer = Color(0xFFF9F9F9),
    functionKeyPressed = Color(0xFFEFEFEF),
    equalsKeyContainer = Color(0xFF0067C0),
    equalsKeyPressed = Color(0xFF1975C5),
    keyBorder = Color(0xFFE5E5E5),
    primaryText = Color(0xFF1A1A1A),
    secondaryText = Color(0xFF5C5C5C),
    disabledText = Color(0xFFA6A6A6),
    equalsText = Color(0xFFFFFFFF),
    historySheetBackground = Color(0xFFF9F9F9),
    scrim = Color(0x47000000),
    memoryRipple = Color(0x14000000)
)

val DarkCalculatorColors = CalculatorColors(
    background = Color(0xFF202020),
    digitKeyContainer = Color(0xFF3B3B3B),
    digitKeyPressed = Color(0xFF323232),
    functionKeyContainer = Color(0xFF323232),
    functionKeyPressed = Color(0xFF2B2B2B),
    equalsKeyContainer = Color(0xFF4CC2FF),
    equalsKeyPressed = Color(0xFF42B0E8),
    keyBorder = Color(0xFF2D2D2D),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFFC5C5C5),
    disabledText = Color(0xFF6B6B6B),
    equalsText = Color(0xFF000000),
    historySheetBackground = Color(0xFF2C2C2C),
    scrim = Color(0x66000000),
    memoryRipple = Color(0x1FFFFFFF)
)
