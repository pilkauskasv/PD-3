package com.example.calculator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.calculator.R
import com.example.calculator.model.CalculatorAction
import com.example.calculator.ui.theme.LocalCalculatorColors

@Composable
fun MemoryRow(
    hasMemory: Boolean,
    isInError: Boolean,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val canRecallOrClear = hasMemory && !isInError
    val canModifyMemory = !isInError

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        MemoryButton(
            label = stringResource(R.string.mem_mc),
            description = stringResource(R.string.cd_mem_mc),
            enabled = canRecallOrClear,
            onClick = { onAction(CalculatorAction.MemoryClear) },
            modifier = Modifier.weight(1f)
        )
        MemoryButton(
            label = stringResource(R.string.mem_mr),
            description = stringResource(R.string.cd_mem_mr),
            enabled = canRecallOrClear,
            onClick = { onAction(CalculatorAction.MemoryRecall) },
            modifier = Modifier.weight(1f)
        )
        MemoryButton(
            label = stringResource(R.string.mem_m_plus),
            description = stringResource(R.string.cd_mem_m_plus),
            enabled = canModifyMemory,
            onClick = { onAction(CalculatorAction.MemoryAdd) },
            modifier = Modifier.weight(1f)
        )
        MemoryButton(
            label = stringResource(R.string.mem_m_minus),
            description = stringResource(R.string.cd_mem_m_minus),
            enabled = canModifyMemory,
            onClick = { onAction(CalculatorAction.MemorySubtract) },
            modifier = Modifier.weight(1f)
        )
        MemoryButton(
            label = stringResource(R.string.mem_ms),
            description = stringResource(R.string.cd_mem_ms),
            enabled = canModifyMemory,
            onClick = { onAction(CalculatorAction.MemoryStore) },
            modifier = Modifier.weight(1f)
        )
        MemoryButton(
            label = stringResource(R.string.mem_m_dropdown),
            description = stringResource(R.string.cd_mem_m_dropdown),
            enabled = false,
            onClick = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MemoryButton(
    label: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(color = colors.memoryRipple),
                onClick = onClick
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) colors.primaryText else colors.disabledText
        )
    }
}
