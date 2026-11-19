package com.example.calculator.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.R
import com.example.calculator.model.CalculatorError
import com.example.calculator.ui.theme.LocalCalculatorColors

@Composable
fun CalculatorDisplay(
    displayValue: String,
    expressionText: String,
    error: CalculatorError?,
    modifier: Modifier = Modifier,
    verticalPadding: Dp = 10.dp
) {
    val colors = LocalCalculatorColors.current
    val exprScroll = rememberScrollState()

    val primaryText = when (error) {
        CalculatorError.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
        CalculatorError.INVALID_INPUT -> stringResource(R.string.error_invalid_input)
        CalculatorError.OVERFLOW -> stringResource(R.string.error_overflow)
        null -> displayValue
    }

    LaunchedEffect(expressionText) {
        exprScroll.scrollTo(exprScroll.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = verticalPadding),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expressionText.ifEmpty { " " },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(exprScroll)
        )

        AutoSizingDisplayText(
            text = primaryText,
            maxFontSize = 48.sp,
            minFontSize = if (error != null) 16.sp else 20.sp,
            allowWrap = error != null,
            color = colors.primaryText
        )
    }
}

@Composable
private fun AutoSizingDisplayText(
    text: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    allowWrap: Boolean,
    color: androidx.compose.ui.graphics.Color
) {
    val textMeasurer = rememberTextMeasurer()
    val baseStyle = MaterialTheme.typography.displayLarge.copy(
        fontWeight = FontWeight.SemiBold
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }.toInt().coerceAtLeast(1)
        val chosenSize = androidx.compose.runtime.remember(text, maxWidthPx, maxFontSize, minFontSize) {
            var resolved = minFontSize
            var candidateSp = maxFontSize.value
            while (candidateSp >= minFontSize.value) {
                val result = textMeasurer.measure(
                    text = text,
                    style = baseStyle.copy(fontSize = candidateSp.sp),
                    maxLines = 1,
                    softWrap = false
                )
                if (result.size.width <= maxWidthPx) {
                    resolved = candidateSp.sp
                    break
                }
                candidateSp -= 2f
            }
            resolved
        }

        Text(
            text = text,
            style = baseStyle.copy(
                fontSize = chosenSize,
                lineHeight = (chosenSize.value * 1.15f).sp
            ),
            color = color,
            maxLines = if (allowWrap) 2 else 1,
            softWrap = allowWrap,
            textAlign = TextAlign.End
        )
    }
}
