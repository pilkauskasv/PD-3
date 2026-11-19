package com.example.calculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.ui.theme.LocalCalculatorColors

enum class CalculatorKeyStyle {
    DIGIT,
    FUNCTION,
    EQUALS
}

@Composable
fun CalculatorButton(
    style: CalculatorKeyStyle,
    label: String? = null,
    icon: ImageVector? = null,
    accessibilityLabel: String? = null,
    enabled: Boolean = true,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = when (style) {
        CalculatorKeyStyle.DIGIT ->
            if (isPressed && enabled) colors.digitKeyPressed else colors.digitKeyContainer
        CalculatorKeyStyle.FUNCTION ->
            if (isPressed && enabled) colors.functionKeyPressed else colors.functionKeyContainer
        CalculatorKeyStyle.EQUALS ->
            if (isPressed && enabled) colors.equalsKeyPressed else colors.equalsKeyContainer
    }

    val contentColor = when {
        !enabled -> colors.disabledText
        style == CalculatorKeyStyle.EQUALS -> colors.equalsText
        else -> colors.primaryText
    }

    val border = if (style == CalculatorKeyStyle.EQUALS) {
        null
    } else {
        BorderStroke(1.dp, colors.keyBorder)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource,
        modifier = modifier.semantics {
            role = Role.Button
            if (accessibilityLabel != null) {
                contentDescription = accessibilityLabel
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else if (label != null) {
                Text(
                    text = label,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = 1
                )
            }
        }
    }
}
