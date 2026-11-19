package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.calculator.R
import com.example.calculator.ui.theme.LocalCalculatorColors

@Composable
fun CalculatorHeader(
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCalculatorColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.mode_standard),
            style = MaterialTheme.typography.titleLarge,
            color = colors.primaryText
        )

        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = stringResource(R.string.cd_history),
                tint = colors.primaryText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
