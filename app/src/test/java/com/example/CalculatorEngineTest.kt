package com.example

import com.example.data.NumberFormatStyle
import com.example.engine.EvalResult
import com.example.engine.FunctionPlotter
import com.example.engine.MathEvaluator
import com.example.engine.NumberFormatter
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {

    private val evaluator = MathEvaluator(angleInDegrees = true)

    @Test
    fun testHighPrecisionAddition() {
        val result = evaluator.evaluate("0.1 + 0.2")
        assertTrue(result is EvalResult.Success)
        val success = result as EvalResult.Success
        assertEquals(0.3, success.value, 1e-9)
        assertEquals("0.3", success.formattedDisplay)
    }

    @Test
    fun testOperatorPrecedenceAndParentheses() {
        val res1 = evaluator.evaluate("2 + 3 * 4")
        assertTrue(res1 is EvalResult.Success)
        assertEquals(14.0, (res1 as EvalResult.Success).value, 1e-9)

        val res2 = evaluator.evaluate("(2 + 3) * 4")
        assertTrue(res2 is EvalResult.Success)
        assertEquals(20.0, (res2 as EvalResult.Success).value, 1e-9)
    }

    @Test
    fun testScientificFunctions() {
        // sin(90) in degrees is 1.0
        val sinRes = evaluator.evaluate("sin(90)")
        assertTrue(sinRes is EvalResult.Success)
        assertEquals(1.0, (sinRes as EvalResult.Success).value, 1e-6)

        // sqrt(25) is 5.0
        val sqrtRes = evaluator.evaluate("sqrt(25)")
        assertTrue(sqrtRes is EvalResult.Success)
        assertEquals(5.0, (sqrtRes as EvalResult.Success).value, 1e-9)

        // 2^4 = 16
        val powRes = evaluator.evaluate("2^4")
        assertTrue(powRes is EvalResult.Success)
        assertEquals(16.0, (powRes as EvalResult.Success).value, 1e-9)

        // 5! = 120
        val factRes = evaluator.evaluate("5!")
        assertTrue(factRes is EvalResult.Success)
        assertEquals(120.0, (factRes as EvalResult.Success).value, 1e-9)
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val divZero = evaluator.evaluate("10 / 0")
        assertTrue(divZero is EvalResult.Error)
        val error = divZero as EvalResult.Error
        assertTrue(error.message.contains("صفر"))
    }

    @Test
    fun testArabicNumberFormatting() {
        val western = "12345"
        val arabic = NumberFormatter.toArabicDigits(western)
        assertEquals("١٢٣٤٥", arabic)

        val convertedBack = NumberFormatter.toWesternDigits(arabic)
        assertEquals("12345", convertedBack)

        val formattedWithGrouping = NumberFormatter.formatNumber(
            value = 1234567.5,
            style = NumberFormatStyle.ARABIC,
            useGrouping = true,
            maxDecimals = 2
        )
        assertTrue(formattedWithGrouping.contains("،") || formattedWithGrouping.contains(","))
    }

    @Test
    fun testFunctionPlotter() {
        val plotter = FunctionPlotter()
        val yAt2 = plotter.evaluateAt("x^2 - 4", 2.0)
        assertEquals(0.0, yAt2, 1e-6)

        val yAt0 = plotter.evaluateAt("x^2 - 4", 0.0)
        assertEquals(-4.0, yAt0, 1e-6)

        val points = plotter.computePoints("x^2", -5.0, 5.0, 50)
        assertTrue(points.isNotEmpty())
        assertTrue(points.all { it.isValid })
    }
}
