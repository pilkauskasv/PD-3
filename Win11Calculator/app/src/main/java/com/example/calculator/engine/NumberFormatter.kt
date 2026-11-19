package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class NumberFormatSymbols(
    val decimalSeparator: String = ".",
    val groupingSeparator: String = ","
) {
    companion object {
        val DEFAULT = NumberFormatSymbols()
    }
}

object NumberFormatter {

    const val MAX_SIGNIFICANT_DIGITS = 16
    const val MAX_EXPONENT = 9999
    const val MIN_EXPONENT = -9999
    private const val SCIENTIFIC_LOW_EXPONENT = -10

    val DISPLAY_MATH_CONTEXT: MathContext = MathContext(MAX_SIGNIFICANT_DIGITS, RoundingMode.HALF_UP)
    val INTERNAL_MATH_CONTEXT: MathContext = MathContext(24, RoundingMode.HALF_UP)

    fun isOverflow(value: BigDecimal): Boolean {
        if (value.compareTo(BigDecimal.ZERO) == 0) return false
        val stripped = value.stripTrailingZeros()
        val exp = stripped.precision() - stripped.scale() - 1
        return exp > MAX_EXPONENT || exp < MIN_EXPONENT
    }

    fun normalize(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        val rounded = value.round(DISPLAY_MATH_CONTEXT)
        if (rounded.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        val stripped = rounded.stripTrailingZeros()
        return if (stripped.scale() < 0) stripped.setScale(0) else stripped
    }

    fun toCanonicalString(value: BigDecimal): String {
        val norm = normalize(value)
        if (norm.compareTo(BigDecimal.ZERO) == 0) return "0"
        return norm.toPlainString()
    }

    fun formatInputBuffer(
        inputBuffer: String,
        symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT
    ): String {
        if (inputBuffer.isEmpty()) return "0"
        val isNegative = inputBuffer.startsWith("-")
        val unsigned = if (isNegative) inputBuffer.substring(1) else inputBuffer
        val dotIndex = unsigned.indexOf('.')

        val rawInt = if (dotIndex >= 0) unsigned.substring(0, dotIndex) else unsigned
        val groupedInt = groupIntegerDigits(rawInt.ifEmpty { "0" }, symbols.groupingSeparator)
        val signPrefix = if (isNegative) "-" else ""

        return if (dotIndex >= 0) {
            val fracPart = unsigned.substring(dotIndex + 1)
            "$signPrefix$groupedInt${symbols.decimalSeparator}$fracPart"
        } else {
            "$signPrefix$groupedInt"
        }
    }

    fun formatResult(
        value: BigDecimal,
        symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT
    ): String {
        val norm = normalize(value)
        if (norm.compareTo(BigDecimal.ZERO) == 0) return "0"

        val exp = norm.precision() - norm.scale() - 1
        return if (exp >= MAX_SIGNIFICANT_DIGITS || exp < SCIENTIFIC_LOW_EXPONENT) {
            formatScientific(norm, exp, symbols)
        } else {
            formatPlain(norm, symbols)
        }
    }

    fun formatForExpression(
        value: BigDecimal,
        symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT
    ): String {
        val norm = normalize(value)
        if (norm.compareTo(BigDecimal.ZERO) == 0) return "0"

        val exp = norm.precision() - norm.scale() - 1
        return if (exp >= MAX_SIGNIFICANT_DIGITS || exp < SCIENTIFIC_LOW_EXPONENT) {
            formatScientific(norm, exp, symbols)
        } else {
            norm.toPlainString().replace(".", symbols.decimalSeparator)
        }
    }

    private fun formatPlain(value: BigDecimal, symbols: NumberFormatSymbols): String {
        val plain = value.toPlainString()
        val isNegative = plain.startsWith("-")
        val unsigned = if (isNegative) plain.substring(1) else plain
        val dotIndex = unsigned.indexOf('.')
        val intPart = if (dotIndex >= 0) unsigned.substring(0, dotIndex) else unsigned
        val groupedInt = groupIntegerDigits(intPart, symbols.groupingSeparator)
        val signPrefix = if (isNegative) "-" else ""

        return if (dotIndex >= 0) {
            val fracPart = unsigned.substring(dotIndex + 1)
            "$signPrefix$groupedInt${symbols.decimalSeparator}$fracPart"
        } else {
            "$signPrefix$groupedInt"
        }
    }

    private fun formatScientific(
        value: BigDecimal,
        exponent: Int,
        symbols: NumberFormatSymbols
    ): String {
        val isNegative = value.signum() < 0
        val absDigits = value.abs().unscaledValue().toString().trimEnd('0').ifEmpty { "0" }
        val mantissa = if (absDigits.length == 1) {
            absDigits
        } else {
            "${absDigits[0]}${symbols.decimalSeparator}${absDigits.substring(1)}"
        }
        val signPrefix = if (isNegative) "-" else ""
        val expSign = if (exponent >= 0) "+" else ""
        return "$signPrefix${mantissa}e$expSign$exponent"
    }

    private fun groupIntegerDigits(digits: String, groupingSeparator: String): String {
        if (digits.length <= 3 || groupingSeparator.isEmpty()) return digits
        val sb = StringBuilder(digits.length + digits.length / 3)
        var count = 0
        for (i in digits.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                sb.append(groupingSeparator)
            }
            sb.append(digits[i])
            count++
        }
        return sb.reverse().toString()
    }
}
