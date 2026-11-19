package com.example.calculator.engine

import com.example.calculator.model.BinaryOperator
import com.example.calculator.model.CalculatorAction
import com.example.calculator.model.CalculatorError
import com.example.calculator.model.CalculatorState
import com.example.calculator.model.HistoryEntry
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorEngine {

    fun reduce(
        state: CalculatorState,
        action: CalculatorAction,
        symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT
    ): CalculatorState {
        return when (action) {
            is CalculatorAction.Digit -> onDigit(state, action.digit)
            CalculatorAction.Decimal -> onDecimal(state)
            CalculatorAction.Backspace -> onBackspace(state)
            CalculatorAction.ClearEntry -> onClearEntry(state)
            CalculatorAction.ClearAll -> onClearAll(state)
            CalculatorAction.Negate -> onNegate(state, symbols)
            is CalculatorAction.Operator -> onOperator(state, action.operator, symbols)
            CalculatorAction.Equals -> onEquals(state, symbols)
            CalculatorAction.Percent -> onPercent(state, symbols)
            CalculatorAction.Reciprocal -> onUnary(state, UnaryOperation.RECIPROCAL, symbols)
            CalculatorAction.Square -> onUnary(state, UnaryOperation.SQUARE, symbols)
            CalculatorAction.SquareRoot -> onUnary(state, UnaryOperation.SQUARE_ROOT, symbols)
            CalculatorAction.MemoryClear -> state.copy(memoryValue = null)
            CalculatorAction.MemoryRecall -> onMemoryRecall(state)
            CalculatorAction.MemoryAdd -> onMemoryDelta(state, isAdd = true)
            CalculatorAction.MemorySubtract -> onMemoryDelta(state, isAdd = false)
            CalculatorAction.MemoryStore -> onMemoryStore(state)
            CalculatorAction.ToggleHistory -> state.copy(isHistoryVisible = !state.isHistoryVisible)
            CalculatorAction.CloseHistory -> state.copy(isHistoryVisible = false)
            CalculatorAction.ClearHistory -> state.copy(history = emptyList())
            is CalculatorAction.RecallHistory -> onRecallHistory(state, action.entry)
        }
    }

    fun formatDisplay(
        state: CalculatorState,
        symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT
    ): String {
        return if (state.isEnteringNumber) {
            NumberFormatter.formatInputBuffer(state.inputBuffer, symbols)
        } else {
            val value = parseBuffer(state.inputBuffer)
            NumberFormatter.formatResult(value, symbols)
        }
    }

    private fun onDigit(state: CalculatorState, digit: Char): CalculatorState {
        require(digit in '0'..'9') { "Invalid digit: $digit" }
        val base = if (state.error != null) clearCalculationKeepMeta(state) else state

        if (base.isResultDisplayed && base.pendingOperator == null) {
            return base.copy(
                inputBuffer = digit.toString(),
                isEnteringNumber = true,
                lastBinaryOperator = null,
                lastRightOperand = null,
                isResultDisplayed = false,
                expressionText = "",
                unaryExpressionOperand = null
            )
        }

        if (!base.isEnteringNumber) {
            return base.copy(
                inputBuffer = digit.toString(),
                isEnteringNumber = true,
                isResultDisplayed = false,
                unaryExpressionOperand = null
            )
        }

        val digitCount = base.inputBuffer.count { it in '0'..'9' }
        if (digitCount >= NumberFormatter.MAX_SIGNIFICANT_DIGITS) {
            return base
        }

        val updated = when (base.inputBuffer) {
            "0" -> digit.toString()
            "-0" -> "-$digit"
            else -> base.inputBuffer + digit
        }
        return base.copy(inputBuffer = updated)
    }

    private fun onDecimal(state: CalculatorState): CalculatorState {
        val base = if (state.error != null) clearCalculationKeepMeta(state) else state

        if (base.isResultDisplayed && base.pendingOperator == null) {
            return base.copy(
                inputBuffer = "0.",
                isEnteringNumber = true,
                lastBinaryOperator = null,
                lastRightOperand = null,
                isResultDisplayed = false,
                expressionText = "",
                unaryExpressionOperand = null
            )
        }

        if (!base.isEnteringNumber) {
            return base.copy(
                inputBuffer = "0.",
                isEnteringNumber = true,
                isResultDisplayed = false,
                unaryExpressionOperand = null
            )
        }

        if (base.inputBuffer.contains('.')) return base
        return base.copy(inputBuffer = "${base.inputBuffer}.")
    }

    private fun onBackspace(state: CalculatorState): CalculatorState {
        if (state.error != null) {
            return clearCalculationKeepMeta(state)
        }

        if (!state.isEnteringNumber) {
            return if (state.isResultDisplayed && state.pendingOperator == null) {
                state.copy(expressionText = "", unaryExpressionOperand = null)
            } else {
                state
            }
        }

        val dropped = state.inputBuffer.dropLast(1)
        val normalized = if (dropped.isEmpty() || dropped == "-" || dropped == "-0") {
            "0"
        } else {
            dropped
        }
        return state.copy(inputBuffer = normalized)
    }

    private fun onClearEntry(state: CalculatorState): CalculatorState {
        if (state.error != null || (state.isResultDisplayed && state.pendingOperator == null)) {
            return clearCalculationKeepMeta(state)
        }
        val restoredExpr = if (state.pendingOperator != null && state.leftExpressionText != null) {
            "${state.leftExpressionText} ${state.pendingOperator.symbol}"
        } else {
            ""
        }
        return state.copy(
            inputBuffer = "0",
            isEnteringNumber = false,
            unaryExpressionOperand = null,
            expressionText = restoredExpr
        )
    }

    private fun onClearAll(state: CalculatorState): CalculatorState {
        return clearCalculationKeepMeta(state)
    }

    private fun onNegate(state: CalculatorState, symbols: NumberFormatSymbols): CalculatorState {
        if (state.error != null) return state

        val current = parseBuffer(state.inputBuffer)
        if (current.compareTo(BigDecimal.ZERO) == 0 && !state.inputBuffer.contains('.')) {
            return state.copy(inputBuffer = "0")
        }

        if (state.isEnteringNumber) {
            val toggled = if (state.inputBuffer.startsWith("-")) {
                state.inputBuffer.substring(1)
            } else {
                "-${state.inputBuffer}"
            }
            return state.copy(inputBuffer = toggled)
        }

        val negated = NumberFormatter.normalize(current.negate())
        val newBuffer = NumberFormatter.toCanonicalString(negated)
        val newUnaryExpr = if (state.unaryExpressionOperand != null || state.isResultDisplayed) {
            val inner = state.unaryExpressionOperand ?: NumberFormatter.formatForExpression(current, symbols)
            "negate( $inner )"
        } else {
            null
        }
        val newExpression = when {
            state.pendingOperator != null && state.leftExpressionText != null && newUnaryExpr != null ->
                "${state.leftExpressionText} ${state.pendingOperator.symbol} $newUnaryExpr"
            state.pendingOperator == null && newUnaryExpr != null -> newUnaryExpr
            else -> state.expressionText
        }

        return state.copy(
            inputBuffer = newBuffer,
            unaryExpressionOperand = newUnaryExpr,
            expressionText = newExpression
        )
    }

    private fun onOperator(
        state: CalculatorState,
        operator: BinaryOperator,
        symbols: NumberFormatSymbols
    ): CalculatorState {
        if (state.error != null) return state

        val hasFreshRightOperand =
            state.isEnteringNumber || state.unaryExpressionOperand != null || state.isResultDisplayed

        if (state.pendingOperator != null && state.accumulator != null) {
            if (!hasFreshRightOperand) {
                val leftExpr = state.leftExpressionText
                    ?: NumberFormatter.formatForExpression(state.accumulator, symbols)
                return state.copy(
                    pendingOperator = operator,
                    leftExpressionText = leftExpr,
                    expressionText = "$leftExpr ${operator.symbol}"
                )
            }

            val right = parseBuffer(state.inputBuffer)
            return when (val eval = evaluateBinary(state.accumulator, state.pendingOperator, right)) {
                is EvalResult.Failure -> state.copy(
                    error = eval.error,
                    accumulator = null,
                    pendingOperator = null,
                    leftExpressionText = null,
                    isEnteringNumber = false,
                    isResultDisplayed = false,
                    unaryExpressionOperand = null
                )

                is EvalResult.Success -> {
                    val leftExpr = NumberFormatter.formatForExpression(eval.value, symbols)
                    state.copy(
                        inputBuffer = NumberFormatter.toCanonicalString(eval.value),
                        isEnteringNumber = false,
                        accumulator = eval.value,
                        leftExpressionText = leftExpr,
                        pendingOperator = operator,
                        lastBinaryOperator = null,
                        lastRightOperand = null,
                        isResultDisplayed = false,
                        expressionText = "$leftExpr ${operator.symbol}",
                        unaryExpressionOperand = null
                    )
                }
            }
        }

        val left = parseBuffer(state.inputBuffer)
        val leftExpr = state.unaryExpressionOperand
            ?: NumberFormatter.formatForExpression(left, symbols)
        return state.copy(
            accumulator = left,
            leftExpressionText = leftExpr,
            pendingOperator = operator,
            lastBinaryOperator = null,
            lastRightOperand = null,
            isEnteringNumber = false,
            isResultDisplayed = false,
            expressionText = "$leftExpr ${operator.symbol}",
            unaryExpressionOperand = null
        )
    }

    private fun onEquals(state: CalculatorState, symbols: NumberFormatSymbols): CalculatorState {
        if (state.error != null) {
            return clearCalculationKeepMeta(state)
        }

        if (state.pendingOperator != null && state.accumulator != null) {
            val left = state.accumulator
            val right = parseBuffer(state.inputBuffer)
            val leftExpr = state.leftExpressionText
                ?: NumberFormatter.formatForExpression(left, symbols)
            val rightExpr = state.unaryExpressionOperand
                ?: NumberFormatter.formatForExpression(right, symbols)
            val fullExpr = "$leftExpr ${state.pendingOperator.symbol} $rightExpr ="

            return when (val eval = evaluateBinary(left, state.pendingOperator, right)) {
                is EvalResult.Failure -> state.copy(
                    error = eval.error,
                    accumulator = null,
                    leftExpressionText = null,
                    pendingOperator = null,
                    lastBinaryOperator = null,
                    lastRightOperand = null,
                    isEnteringNumber = false,
                    isResultDisplayed = false,
                    expressionText = fullExpr,
                    unaryExpressionOperand = null
                )

                is EvalResult.Success -> {
                    val formatted = NumberFormatter.formatResult(eval.value, symbols)
                    val entry = HistoryEntry(
                        id = state.nextHistoryId,
                        expression = fullExpr,
                        resultFormatted = formatted,
                        rawValue = eval.value
                    )
                    state.copy(
                        inputBuffer = NumberFormatter.toCanonicalString(eval.value),
                        isEnteringNumber = false,
                        accumulator = null,
                        leftExpressionText = null,
                        pendingOperator = null,
                        lastBinaryOperator = state.pendingOperator,
                        lastRightOperand = right,
                        isResultDisplayed = true,
                        expressionText = fullExpr,
                        unaryExpressionOperand = null,
                        history = listOf(entry) + state.history,
                        nextHistoryId = state.nextHistoryId + 1L
                    )
                }
            }
        }

        if (state.lastBinaryOperator != null && state.lastRightOperand != null) {
            val left = parseBuffer(state.inputBuffer)
            val right = state.lastRightOperand
            val op = state.lastBinaryOperator
            val fullExpr = "${NumberFormatter.formatForExpression(left, symbols)} ${op.symbol} " +
                "${NumberFormatter.formatForExpression(right, symbols)} ="

            return when (val eval = evaluateBinary(left, op, right)) {
                is EvalResult.Failure -> state.copy(
                    error = eval.error,
                    accumulator = null,
                    leftExpressionText = null,
                    pendingOperator = null,
                    lastBinaryOperator = null,
                    lastRightOperand = null,
                    isEnteringNumber = false,
                    isResultDisplayed = false,
                    expressionText = fullExpr,
                    unaryExpressionOperand = null
                )

                is EvalResult.Success -> {
                    val formatted = NumberFormatter.formatResult(eval.value, symbols)
                    val entry = HistoryEntry(
                        id = state.nextHistoryId,
                        expression = fullExpr,
                        resultFormatted = formatted,
                        rawValue = eval.value
                    )
                    state.copy(
                        inputBuffer = NumberFormatter.toCanonicalString(eval.value),
                        isEnteringNumber = false,
                        isResultDisplayed = true,
                        expressionText = fullExpr,
                        unaryExpressionOperand = null,
                        history = listOf(entry) + state.history,
                        nextHistoryId = state.nextHistoryId + 1L
                    )
                }
            }
        }

        val current = parseBuffer(state.inputBuffer)
        val expr = state.unaryExpressionOperand
            ?: NumberFormatter.formatForExpression(current, symbols)
        val fullExpr = "$expr ="
        val formatted = NumberFormatter.formatResult(current, symbols)
        val entry = HistoryEntry(
            id = state.nextHistoryId,
            expression = fullExpr,
            resultFormatted = formatted,
            rawValue = NumberFormatter.normalize(current)
        )
        return state.copy(
            inputBuffer = NumberFormatter.toCanonicalString(current),
            isEnteringNumber = false,
            isResultDisplayed = true,
            expressionText = fullExpr,
            unaryExpressionOperand = null,
            history = listOf(entry) + state.history,
            nextHistoryId = state.nextHistoryId + 1L
        )
    }

    private fun onPercent(state: CalculatorState, symbols: NumberFormatSymbols): CalculatorState {
        if (state.error != null) return state

        val op = state.pendingOperator
        val acc = state.accumulator
        if (op == null || acc == null) {
            return state.copy(
                inputBuffer = "0",
                isEnteringNumber = false,
                isResultDisplayed = true,
                expressionText = "0",
                unaryExpressionOperand = null
            )
        }

        val current = parseBuffer(state.inputBuffer)
        val hundred = BigDecimal(100)
        val rawPercent = when (op) {
            BinaryOperator.ADD, BinaryOperator.SUBTRACT ->
                acc.multiply(current, NumberFormatter.INTERNAL_MATH_CONTEXT)
                    .divide(hundred, NumberFormatter.INTERNAL_MATH_CONTEXT)

            BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE ->
                current.divide(hundred, NumberFormatter.INTERNAL_MATH_CONTEXT)
        }

        if (NumberFormatter.isOverflow(rawPercent)) {
            return state.copy(
                error = CalculatorError.OVERFLOW,
                accumulator = null,
                leftExpressionText = null,
                pendingOperator = null,
                isEnteringNumber = false,
                isResultDisplayed = false,
                unaryExpressionOperand = null
            )
        }

        val result = NumberFormatter.normalize(rawPercent)
        val leftExpr = state.leftExpressionText ?: NumberFormatter.formatForExpression(acc, symbols)
        val rightExpr = NumberFormatter.formatForExpression(result, symbols)
        return state.copy(
            inputBuffer = NumberFormatter.toCanonicalString(result),
            isEnteringNumber = false,
            isResultDisplayed = true,
            unaryExpressionOperand = rightExpr,
            expressionText = "$leftExpr ${op.symbol} $rightExpr"
        )
    }

    private fun onUnary(
        state: CalculatorState,
        operation: UnaryOperation,
        symbols: NumberFormatSymbols
    ): CalculatorState {
        if (state.error != null) return state

        val operand = parseBuffer(state.inputBuffer)
        val innerExpr = state.unaryExpressionOperand
            ?: NumberFormatter.formatForExpression(operand, symbols)
        val newUnaryExpr = when (operation) {
            UnaryOperation.RECIPROCAL -> "1/( $innerExpr )"
            UnaryOperation.SQUARE -> "sqr( $innerExpr )"
            UnaryOperation.SQUARE_ROOT -> "√( $innerExpr )"
        }
        val composedExpr = if (state.pendingOperator != null && state.leftExpressionText != null) {
            "${state.leftExpressionText} ${state.pendingOperator.symbol} $newUnaryExpr"
        } else {
            newUnaryExpr
        }

        val eval = when (operation) {
            UnaryOperation.RECIPROCAL -> {
                if (operand.compareTo(BigDecimal.ZERO) == 0) {
                    EvalResult.Failure(CalculatorError.DIVIDE_BY_ZERO)
                } else {
                    val res = BigDecimal.ONE.divide(operand, NumberFormatter.INTERNAL_MATH_CONTEXT)
                    checkOverflowAndWrap(res)
                }
            }

            UnaryOperation.SQUARE -> {
                val res = operand.multiply(operand, NumberFormatter.INTERNAL_MATH_CONTEXT)
                checkOverflowAndWrap(res)
            }

            UnaryOperation.SQUARE_ROOT -> {
                if (operand < BigDecimal.ZERO) {
                    EvalResult.Failure(CalculatorError.INVALID_INPUT)
                } else {
                    val res = sqrtBigDecimal(operand)
                    checkOverflowAndWrap(res)
                }
            }
        }

        return when (eval) {
            is EvalResult.Failure -> state.copy(
                error = eval.error,
                accumulator = null,
                leftExpressionText = null,
                pendingOperator = null,
                lastBinaryOperator = null,
                lastRightOperand = null,
                isEnteringNumber = false,
                isResultDisplayed = false,
                expressionText = composedExpr,
                unaryExpressionOperand = null
            )

            is EvalResult.Success -> {
                if (state.pendingOperator != null) {
                    state.copy(
                        inputBuffer = NumberFormatter.toCanonicalString(eval.value),
                        isEnteringNumber = false,
                        isResultDisplayed = true,
                        unaryExpressionOperand = newUnaryExpr,
                        expressionText = composedExpr
                    )
                } else {
                    val formatted = NumberFormatter.formatResult(eval.value, symbols)
                    val entry = HistoryEntry(
                        id = state.nextHistoryId,
                        expression = newUnaryExpr,
                        resultFormatted = formatted,
                        rawValue = eval.value
                    )
                    state.copy(
                        inputBuffer = NumberFormatter.toCanonicalString(eval.value),
                        isEnteringNumber = false,
                        isResultDisplayed = true,
                        unaryExpressionOperand = newUnaryExpr,
                        expressionText = newUnaryExpr,
                        lastBinaryOperator = null,
                        lastRightOperand = null,
                        history = listOf(entry) + state.history,
                        nextHistoryId = state.nextHistoryId + 1L
                    )
                }
            }
        }
    }

    private fun onMemoryRecall(state: CalculatorState): CalculatorState {
        if (state.error != null) return state
        val mem = state.memoryValue ?: return state
        val clearExpr = state.pendingOperator == null
        return state.copy(
            inputBuffer = NumberFormatter.toCanonicalString(mem),
            isEnteringNumber = false,
            isResultDisplayed = true,
            unaryExpressionOperand = null,
            expressionText = if (clearExpr) "" else state.expressionText
        )
    }

    private fun onMemoryStore(state: CalculatorState): CalculatorState {
        if (state.error != null) return state
        val current = NumberFormatter.normalize(parseBuffer(state.inputBuffer))
        return state.copy(
            memoryValue = current,
            isEnteringNumber = false,
            isResultDisplayed = true
        )
    }

    private fun onMemoryDelta(state: CalculatorState, isAdd: Boolean): CalculatorState {
        if (state.error != null) return state
        val current = parseBuffer(state.inputBuffer)
        val existing = state.memoryValue ?: BigDecimal.ZERO
        val updated = if (isAdd) {
            existing.add(current, NumberFormatter.INTERNAL_MATH_CONTEXT)
        } else {
            existing.subtract(current, NumberFormatter.INTERNAL_MATH_CONTEXT)
        }
        if (NumberFormatter.isOverflow(updated)) {
            return state.copy(error = CalculatorError.OVERFLOW)
        }
        return state.copy(
            memoryValue = NumberFormatter.normalize(updated),
            isEnteringNumber = false,
            isResultDisplayed = true
        )
    }

    private fun onRecallHistory(state: CalculatorState, entry: HistoryEntry): CalculatorState {
        return state.copy(
            inputBuffer = NumberFormatter.toCanonicalString(entry.rawValue),
            isEnteringNumber = false,
            accumulator = null,
            leftExpressionText = null,
            pendingOperator = null,
            lastBinaryOperator = null,
            lastRightOperand = null,
            isResultDisplayed = true,
            expressionText = entry.expression,
            unaryExpressionOperand = null,
            error = null,
            isHistoryVisible = false
        )
    }

    private fun evaluateBinary(
        left: BigDecimal,
        operator: BinaryOperator,
        right: BigDecimal
    ): EvalResult {
        val raw = when (operator) {
            BinaryOperator.ADD ->
                left.add(right, NumberFormatter.INTERNAL_MATH_CONTEXT)

            BinaryOperator.SUBTRACT ->
                left.subtract(right, NumberFormatter.INTERNAL_MATH_CONTEXT)

            BinaryOperator.MULTIPLY ->
                left.multiply(right, NumberFormatter.INTERNAL_MATH_CONTEXT)

            BinaryOperator.DIVIDE -> {
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    return EvalResult.Failure(CalculatorError.DIVIDE_BY_ZERO)
                }
                left.divide(right, NumberFormatter.INTERNAL_MATH_CONTEXT)
            }
        }
        return checkOverflowAndWrap(raw)
    }

    private fun checkOverflowAndWrap(value: BigDecimal): EvalResult {
        return if (NumberFormatter.isOverflow(value)) {
            EvalResult.Failure(CalculatorError.OVERFLOW)
        } else {
            EvalResult.Success(NumberFormatter.normalize(value))
        }
    }

    private fun sqrtBigDecimal(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO

        val two = BigDecimal(2)
        val mc = NumberFormatter.INTERNAL_MATH_CONTEXT
        val halfMagnitude = (value.precision() - value.scale()) / 2
        var estimate = BigDecimal.ONE.movePointRight(halfMagnitude)
        if (estimate.compareTo(BigDecimal.ZERO) == 0) {
            estimate = BigDecimal.ONE
        }

        for (i in 0 until 80) {
            val next = estimate
                .add(value.divide(estimate, mc), mc)
                .divide(two, mc)
            if (next.compareTo(estimate) == 0) {
                return next
            }
            val diff = next.subtract(estimate).abs()
            val tolerance = next.abs().movePointLeft(22)
            estimate = next
            if (diff <= tolerance) {
                break
            }
        }
        return estimate.setScale(mc.precision, RoundingMode.HALF_UP).round(mc)
    }

    private fun parseBuffer(buffer: String): BigDecimal {
        val cleaned = if (buffer.endsWith(".")) buffer.dropLast(1) else buffer
        return cleaned.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    private fun clearCalculationKeepMeta(state: CalculatorState): CalculatorState {
        return state.copy(
            inputBuffer = "0",
            isEnteringNumber = false,
            accumulator = null,
            leftExpressionText = null,
            pendingOperator = null,
            lastBinaryOperator = null,
            lastRightOperand = null,
            isResultDisplayed = false,
            expressionText = "",
            unaryExpressionOperand = null,
            error = null
        )
    }

    private enum class UnaryOperation {
        RECIPROCAL,
        SQUARE,
        SQUARE_ROOT
    }

    private sealed interface EvalResult {
        data class Success(val value: BigDecimal) : EvalResult
        data class Failure(val error: CalculatorError) : EvalResult
    }
}
