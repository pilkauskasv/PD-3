package com.example.calculator.model

data class CalculatorUiState(
    val displayValue: String = "0",
    val expressionText: String = "",
    val error: CalculatorError? = null,
    val hasMemory: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
    val isHistoryVisible: Boolean = false
) {
    val isInError: Boolean get() = error != null
}
