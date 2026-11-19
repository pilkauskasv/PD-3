package com.example.calculator.model

import java.math.BigDecimal

data class HistoryEntry(
    val id: Long,
    val expression: String,
    val resultFormatted: String,
    val rawValue: BigDecimal
)
