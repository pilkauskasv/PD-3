package com.example.calculator.viewmodel

import androidx.lifecycle.ViewModel
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.NumberFormatSymbols
import com.example.calculator.model.CalculatorAction
import com.example.calculator.model.CalculatorState
import com.example.calculator.model.CalculatorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel(
    private val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

    private var currentState = CalculatorState()
    private var formatSymbols = NumberFormatSymbols.DEFAULT

    private val _uiState = MutableStateFlow(mapToUiState(currentState))
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun updateFormatSymbols(symbols: NumberFormatSymbols) {
        if (formatSymbols == symbols) return
        formatSymbols = symbols
        _uiState.update { mapToUiState(currentState) }
    }

    fun onAction(action: CalculatorAction) {
        currentState = engine.reduce(currentState, action, formatSymbols)
        _uiState.value = mapToUiState(currentState)
    }

    private fun mapToUiState(state: CalculatorState): CalculatorUiState {
        return CalculatorUiState(
            displayValue = engine.formatDisplay(state, formatSymbols),
            expressionText = state.expressionText,
            error = state.error,
            hasMemory = state.memoryValue != null,
            history = state.history,
            isHistoryVisible = state.isHistoryVisible
        )
    }
}
