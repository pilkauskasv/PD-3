package com.example.calculator.engine

import com.example.calculator.model.BinaryOperator
import com.example.calculator.model.CalculatorAction
import com.example.calculator.model.CalculatorError
import com.example.calculator.model.CalculatorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine
    private lateinit var state: CalculatorState

    @Before
    fun setup() {
        engine = CalculatorEngine()
        state = CalculatorState()
    }

    private fun press(vararg actions: CalculatorAction) {
        for (action in actions) {
            state = engine.reduce(state, action)
        }
    }

    private fun enterDigits(text: String) {
        for (ch in text) {
            when (ch) {
                '.' -> press(CalculatorAction.Decimal)
                in '0'..'9' -> press(CalculatorAction.Digit(ch))
            }
        }
    }

    private fun display(symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT): String =
        engine.formatDisplay(state, symbols)

    @Test
    fun `01 basic addition matches reference screenshot`() {
        enterDigits("12")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("8")
        press(CalculatorAction.Equals)

        assertEquals("20", display())
        assertEquals("12 + 8 =", state.expressionText)
        assertEquals(1, state.history.size)
        assertEquals("12 + 8 =", state.history.first().expression)
        assertEquals("20", state.history.first().resultFormatted)
    }

    @Test
    fun `02 decimal addition avoids binary floating point drift`() {
        enterDigits("0.1")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("0.2")
        press(CalculatorAction.Equals)

        assertEquals("0.3", display())
    }

    @Test
    fun `03 subtraction with decimals`() {
        enterDigits("100")
        press(CalculatorAction.Operator(BinaryOperator.SUBTRACT))
        enterDigits("37.5")
        press(CalculatorAction.Equals)

        assertEquals("62.5", display())
    }

    @Test
    fun `04 multiplication strips trailing decimal zeros`() {
        enterDigits("1.25")
        press(CalculatorAction.Operator(BinaryOperator.MULTIPLY))
        enterDigits("8")
        press(CalculatorAction.Equals)

        assertEquals("10", display())
    }

    @Test
    fun `05 division uses 16 significant digits precision`() {
        enterDigits("1")
        press(CalculatorAction.Operator(BinaryOperator.DIVIDE))
        enterDigits("3")
        press(CalculatorAction.Equals)

        assertEquals("0.3333333333333333", display())
    }

    @Test
    fun `06 sequential immediate execution ignores operator precedence`() {
        enterDigits("2")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("3")
        press(CalculatorAction.Operator(BinaryOperator.MULTIPLY))
        assertEquals("5", display())

        enterDigits("4")
        press(CalculatorAction.Equals)
        assertEquals("20", display())
    }

    @Test
    fun `07 changing pending operator replaces without evaluating`() {
        enterDigits("10")
        press(
            CalculatorAction.Operator(BinaryOperator.ADD),
            CalculatorAction.Operator(BinaryOperator.MULTIPLY)
        )
        enterDigits("5")
        press(CalculatorAction.Equals)

        assertEquals("50", display())
        assertEquals("10 × 5 =", state.expressionText)
    }

    @Test
    fun `08 repeated equals replays last operator and operand`() {
        enterDigits("12")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("8")
        press(CalculatorAction.Equals)
        assertEquals("20", display())

        press(CalculatorAction.Equals)
        assertEquals("28", display())

        press(CalculatorAction.Equals)
        assertEquals("36", display())
    }

    @Test
    fun `09 reciprocal matches reference history format`() {
        enterDigits("8")
        press(CalculatorAction.Reciprocal)

        assertEquals("0.125", display())
        assertEquals("1/( 8 )", state.expressionText)
        assertEquals(1, state.history.size)
        assertEquals("1/( 8 )", state.history.first().expression)
        assertEquals("0.125", state.history.first().resultFormatted)
    }

    @Test
    fun `10 squaring and square root work accurately`() {
        enterDigits("9")
        press(CalculatorAction.Square)
        assertEquals("81", display())
        assertEquals("sqr( 9 )", state.expressionText)

        press(CalculatorAction.SquareRoot)
        assertEquals("9", display())
        assertEquals("√( sqr( 9 ) )", state.expressionText)
    }

    @Test
    fun `11 square root of non-perfect square computes 16 significant digits`() {
        enterDigits("2")
        press(CalculatorAction.SquareRoot)

        assertEquals("1.414213562373095", display())
    }

    @Test
    fun `12 percentage with addition is relative to first operand`() {
        enterDigits("200")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("10")
        press(CalculatorAction.Percent)
        assertEquals("20", display())

        press(CalculatorAction.Equals)
        assertEquals("220", display())
    }

    @Test
    fun `13 percentage with subtraction is relative to first operand`() {
        enterDigits("200")
        press(CalculatorAction.Operator(BinaryOperator.SUBTRACT))
        enterDigits("10")
        press(CalculatorAction.Percent)
        press(CalculatorAction.Equals)

        assertEquals("180", display())
    }

    @Test
    fun `14 percentage with multiplication treats operand as ratio`() {
        enterDigits("200")
        press(CalculatorAction.Operator(BinaryOperator.MULTIPLY))
        enterDigits("10")
        press(CalculatorAction.Percent)
        assertEquals("0.1", display())

        press(CalculatorAction.Equals)
        assertEquals("20", display())
    }

    @Test
    fun `15 percentage with no pending operator clears to zero`() {
        enterDigits("50")
        press(CalculatorAction.Percent)

        assertEquals("0", display())
    }

    @Test
    fun `16 backspace and negate edit active entry cleanly`() {
        enterDigits("123")
        press(CalculatorAction.Backspace)
        assertEquals("12", display())

        press(CalculatorAction.Negate)
        assertEquals("-12", display())

        press(CalculatorAction.Backspace, CalculatorAction.Backspace)
        assertEquals("0", display())
    }

    @Test
    fun `17 clear entry keeps pending operation while clear all resets`() {
        enterDigits("12")
        press(CalculatorAction.Operator(BinaryOperator.ADD))
        enterDigits("99")
        press(CalculatorAction.ClearEntry)
        assertEquals("0", display())

        enterDigits("8")
        press(CalculatorAction.Equals)
        assertEquals("20", display())

        press(CalculatorAction.ClearAll)
        assertEquals("0", display())
        assertEquals("", state.expressionText)
    }

    @Test
    fun `18 division by zero triggers error and is excluded from history`() {
        enterDigits("5")
        press(CalculatorAction.Operator(BinaryOperator.DIVIDE))
        enterDigits("0")
        press(CalculatorAction.Equals)

        assertEquals(CalculatorError.DIVIDE_BY_ZERO, state.error)
        assertTrue(state.history.isEmpty())

        enterDigits("7")
        assertNull(state.error)
        assertEquals("7", display())
    }

    @Test
    fun `19 reciprocal of zero, negative sqrt, and overflow trigger respective errors`() {
        enterDigits("0")
        press(CalculatorAction.Reciprocal)
        assertEquals(CalculatorError.DIVIDE_BY_ZERO, state.error)

        press(CalculatorAction.ClearAll)
        enterDigits("4")
        press(CalculatorAction.Negate, CalculatorAction.SquareRoot)
        assertEquals(CalculatorError.INVALID_INPUT, state.error)

        press(CalculatorAction.ClearAll)
        enterDigits("1000000000000000")
        repeat(10) {
            if (state.error == null) {
                press(CalculatorAction.Square)
            }
        }
        assertEquals(CalculatorError.OVERFLOW, state.error)
    }

    @Test
    fun `20 single-slot memory, history recall, clear, and localized decimal formatting work`() {
        enterDigits("25")
        press(CalculatorAction.MemoryStore)
        assertEquals("25", state.memoryValue?.toPlainString())

        press(CalculatorAction.ClearAll)
        enterDigits("5")
        press(CalculatorAction.MemoryAdd)
        enterDigits("3")
        press(CalculatorAction.MemorySubtract)

        press(CalculatorAction.ClearAll, CalculatorAction.MemoryRecall)
        assertEquals("27", display())

        press(CalculatorAction.MemoryClear)
        assertNull(state.memoryValue)

        val ltSymbols = NumberFormatSymbols(decimalSeparator = ",", groupingSeparator = "\u00A0")
        press(CalculatorAction.ClearAll)
        enterDigits("8")
        state = engine.reduce(state, CalculatorAction.Reciprocal, ltSymbols)
        assertEquals("0,125", engine.formatDisplay(state, ltSymbols))
        assertEquals("0,125", state.history.first().resultFormatted)

        val savedEntry = state.history.first()
        press(CalculatorAction.ClearAll)
        press(CalculatorAction.ToggleHistory)
        assertTrue(state.isHistoryVisible)

        press(CalculatorAction.RecallHistory(savedEntry))
        assertFalse(state.isHistoryVisible)
        assertEquals("0.125", display())

        press(CalculatorAction.ClearHistory)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun `21 negating right operand after operator chains accurately`() {
        enterDigits("12")
        press(
            CalculatorAction.Operator(BinaryOperator.ADD),
            CalculatorAction.Negate,
            CalculatorAction.Operator(BinaryOperator.MULTIPLY)
        )
        assertEquals("0", display())

        enterDigits("5")
        press(CalculatorAction.Equals)
        assertEquals("0", display())
    }

    @Test
    fun `22 square root preserves precision for very small numbers below 1e-24`() {
        // 1e-24 squared = 1e-48; sqrt(1e-48) must equal 1e-24 and not round down to 0
        enterDigits("0.000000000001")
        press(CalculatorAction.Square) // 1e-24
        press(CalculatorAction.Square) // 1e-48
        press(CalculatorAction.SquareRoot)

        assertEquals("1e-24", display())
    }
}
