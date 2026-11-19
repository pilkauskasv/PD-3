package com.example.calculator.model

sealed interface CalculatorAction {
    data class Digit(val digit: Char) : CalculatorAction
    data object Decimal : CalculatorAction
    data object Backspace : CalculatorAction
    data object ClearEntry : CalculatorAction
    data object ClearAll : CalculatorAction
    data object Negate : CalculatorAction
    data class Operator(val operator: BinaryOperator) : CalculatorAction
    data object Equals : CalculatorAction
    data object Percent : CalculatorAction
    data object Reciprocal : CalculatorAction
    data object Square : CalculatorAction
    data object SquareRoot : CalculatorAction

    data object MemoryClear : CalculatorAction
    data object MemoryRecall : CalculatorAction
    data object MemoryAdd : CalculatorAction
    data object MemorySubtract : CalculatorAction
    data object MemoryStore : CalculatorAction

    data object ToggleHistory : CalculatorAction
    data object CloseHistory : CalculatorAction
    data object ClearHistory : CalculatorAction
    data class RecallHistory(val entry: HistoryEntry) : CalculatorAction
}
