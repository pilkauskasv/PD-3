package com.example.calculator.model

import java.math.BigDecimal

data class CalculatorState(
    val inputBuffer: String = "0",
    val isEnteringNumber: Boolean = false,
    val accumulator: BigDecimal? = null,
    val leftExpressionText: String? = null,
    val pendingOperator: BinaryOperator? = null,
    val lastBinaryOperator: BinaryOperator? = null,
    val lastRightOperand: BigDecimal? = null,
    val isResultDisplayed: Boolean = false,
    val expressionText: String = "",
    val unaryExpressionOperand: String? = null,
    val memoryValue: BigDecimal? = null,
    val error: CalculatorError? = null,
    val history: List<HistoryEntry> = emptyList(),
    val nextHistoryId: Long = 1L,
    val isHistoryVisible: Boolean = false
)
