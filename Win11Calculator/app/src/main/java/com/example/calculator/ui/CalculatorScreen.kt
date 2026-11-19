package com.example.calculator.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.R
import com.example.calculator.engine.NumberFormatSymbols
import com.example.calculator.model.CalculatorAction
import com.example.calculator.model.CalculatorUiState
import com.example.calculator.ui.components.CalculatorDisplay
import com.example.calculator.ui.components.CalculatorHeader
import com.example.calculator.ui.components.CalculatorKeypad
import com.example.calculator.ui.components.HistorySheet
import com.example.calculator.ui.components.MemoryRow
import com.example.calculator.ui.theme.LocalCalculatorColors
import com.example.calculator.viewmodel.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val decimalSeparator = stringResource(R.string.decimal_separator)
    val groupingSeparator = stringResource(R.string.grouping_separator)

    LaunchedEffect(decimalSeparator, groupingSeparator) {
        viewModel.updateFormatSymbols(
            NumberFormatSymbols(
                decimalSeparator = decimalSeparator,
                groupingSeparator = groupingSeparator
            )
        )
    }

    BackHandler(enabled = uiState.isHistoryVisible) {
        viewModel.onAction(CalculatorAction.CloseHistory)
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val colors = LocalCalculatorColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
    ) {
        if (isLandscape) {
            LandscapeLayout(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        } else {
            PortraitLayout(
                uiState = uiState,
                onAction = viewModel::onAction
            )
        }

        HistorySheet(
            visible = uiState.isHistoryVisible,
            history = uiState.history,
            sheetHeightFraction = if (isLandscape) 0.85f else 0.66f,
            isLandscape = isLandscape,
            onDismiss = { viewModel.onAction(CalculatorAction.CloseHistory) },
            onSelectEntry = { entry -> viewModel.onAction(CalculatorAction.RecallHistory(entry)) },
            onClearHistory = { viewModel.onAction(CalculatorAction.ClearHistory) }
        )
    }
}

@Composable
private fun PortraitLayout(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CalculatorHeader(
            onHistoryClick = { onAction(CalculatorAction.ToggleHistory) }
        )

        CalculatorDisplay(
            displayValue = uiState.displayValue,
            expressionText = uiState.expressionText,
            error = uiState.error,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.24f)
        )

        MemoryRow(
            hasMemory = uiState.hasMemory,
            isInError = uiState.isInError,
            onAction = onAction
        )

        CalculatorKeypad(
            isInError = uiState.isInError,
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.76f)
        )
    }
}

@Composable
private fun LandscapeLayout(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
        ) {
            CalculatorHeader(
                onHistoryClick = { onAction(CalculatorAction.ToggleHistory) }
            )

            Spacer(modifier = Modifier.weight(1f))

            CalculatorDisplay(
                displayValue = uiState.displayValue,
                expressionText = uiState.expressionText,
                error = uiState.error,
                verticalPadding = 8.dp
            )

            MemoryRow(
                hasMemory = uiState.hasMemory,
                isInError = uiState.isInError,
                onAction = onAction
            )
        }

        CalculatorKeypad(
            isInError = uiState.isInError,
            onAction = onAction,
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
        )
    }
}
