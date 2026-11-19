package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.R
import com.example.calculator.model.BinaryOperator
import com.example.calculator.model.CalculatorAction

@Composable
fun CalculatorKeypad(
    isInError: Boolean,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val gap = 4.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_percent),
                accessibilityLabel = stringResource(R.string.cd_percent),
                enabled = !isInError,
                fontSize = 18.sp,
                onClick = { onAction(CalculatorAction.Percent) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_ce),
                accessibilityLabel = stringResource(R.string.cd_ce),
                fontSize = 16.sp,
                onClick = { onAction(CalculatorAction.ClearEntry) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_c),
                accessibilityLabel = stringResource(R.string.cd_c),
                fontSize = 16.sp,
                onClick = { onAction(CalculatorAction.ClearAll) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                icon = Icons.AutoMirrored.Outlined.Backspace,
                accessibilityLabel = stringResource(R.string.cd_backspace),
                onClick = { onAction(CalculatorAction.Backspace) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_reciprocal),
                accessibilityLabel = stringResource(R.string.cd_reciprocal),
                enabled = !isInError,
                fontSize = 17.sp,
                onClick = { onAction(CalculatorAction.Reciprocal) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_square),
                accessibilityLabel = stringResource(R.string.cd_square),
                enabled = !isInError,
                fontSize = 17.sp,
                onClick = { onAction(CalculatorAction.Square) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_sqrt),
                accessibilityLabel = stringResource(R.string.cd_sqrt),
                enabled = !isInError,
                fontSize = 17.sp,
                onClick = { onAction(CalculatorAction.SquareRoot) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_divide),
                accessibilityLabel = stringResource(R.string.cd_divide),
                enabled = !isInError,
                fontSize = 22.sp,
                onClick = { onAction(CalculatorAction.Operator(BinaryOperator.DIVIDE)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            DigitKey(stringResource(R.string.key_7), '7', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_8), '8', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_9), '9', onAction, Modifier.weight(1f).fillMaxHeight())
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_multiply),
                accessibilityLabel = stringResource(R.string.cd_multiply),
                enabled = !isInError,
                fontSize = 22.sp,
                onClick = { onAction(CalculatorAction.Operator(BinaryOperator.MULTIPLY)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            DigitKey(stringResource(R.string.key_4), '4', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_5), '5', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_6), '6', onAction, Modifier.weight(1f).fillMaxHeight())
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_subtract),
                accessibilityLabel = stringResource(R.string.cd_subtract),
                enabled = !isInError,
                fontSize = 22.sp,
                onClick = { onAction(CalculatorAction.Operator(BinaryOperator.SUBTRACT)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            DigitKey(stringResource(R.string.key_1), '1', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_2), '2', onAction, Modifier.weight(1f).fillMaxHeight())
            DigitKey(stringResource(R.string.key_3), '3', onAction, Modifier.weight(1f).fillMaxHeight())
            CalculatorButton(
                style = CalculatorKeyStyle.FUNCTION,
                label = stringResource(R.string.key_add),
                accessibilityLabel = stringResource(R.string.cd_add),
                enabled = !isInError,
                fontSize = 22.sp,
                onClick = { onAction(CalculatorAction.Operator(BinaryOperator.ADD)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            CalculatorButton(
                style = CalculatorKeyStyle.DIGIT,
                label = stringResource(R.string.key_negate),
                accessibilityLabel = stringResource(R.string.cd_negate),
                enabled = !isInError,
                fontSize = 18.sp,
                onClick = { onAction(CalculatorAction.Negate) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DigitKey(stringResource(R.string.key_0), '0', onAction, Modifier.weight(1f).fillMaxHeight())
            CalculatorButton(
                style = CalculatorKeyStyle.DIGIT,
                label = stringResource(R.string.decimal_separator),
                accessibilityLabel = stringResource(R.string.cd_decimal),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                onClick = { onAction(CalculatorAction.Decimal) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CalculatorButton(
                style = CalculatorKeyStyle.EQUALS,
                label = stringResource(R.string.key_equals),
                accessibilityLabel = stringResource(R.string.cd_equals),
                fontSize = 24.sp,
                onClick = { onAction(CalculatorAction.Equals) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun DigitKey(
    label: String,
    digit: Char,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    CalculatorButton(
        style = CalculatorKeyStyle.DIGIT,
        label = label,
        fontSize = 21.sp,
        fontWeight = FontWeight.Medium,
        onClick = { onAction(CalculatorAction.Digit(digit)) },
        modifier = modifier
    )
}
