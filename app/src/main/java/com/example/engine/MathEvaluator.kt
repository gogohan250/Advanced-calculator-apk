package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

sealed class EvalResult {
    data class Success(val value: Double, val formattedDisplay: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

class MathEvaluator(
    private var angleInDegrees: Boolean = true
) {

    fun setAngleInDegrees(degrees: Boolean) {
        this.angleInDegrees = degrees
    }

    fun isAngleInDegrees(): Boolean = angleInDegrees

    /**
     * Evaluates a mathematical expression string safely without using eval().
     */
    fun evaluate(rawExpr: String): EvalResult {
        if (rawExpr.isBlank()) {
            return EvalResult.Success(0.0, "0")
        }

        return try {
            val sanitized = sanitize(rawExpr)
            if (sanitized.isEmpty()) {
                return EvalResult.Success(0.0, "0")
            }

            // Check if it's a simple two-operand arithmetic (e.g. 0.1 + 0.2) to use high-precision BigDecimal
            val bigDecimalResult = trySimpleBigDecimal(sanitized)
            if (bigDecimalResult != null) {
                return EvalResult.Success(
                    bigDecimalResult.toDouble(),
                    bigDecimalResult.stripTrailingZeros().toPlainString()
                )
            }

            val parser = ExpressionParser(sanitized, angleInDegrees)
            val result = parser.parse()

            if (result.isNaN()) {
                EvalResult.Error("قيمة غير معرفة")
            } else if (result.isInfinite()) {
                EvalResult.Error(if (result > 0) "ما لا نهاية (+∞)" else "ما لا نهاية (-∞)")
            } else {
                // Round tiny floating errors (e.g., 0.30000000000000004 or 6.123233995736766e-17 for cos(90))
                val cleaned = cleanFloatingPointNoise(result)
                EvalResult.Success(cleaned, cleaned.toString())
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "خطأ رياضي")
        } catch (e: Exception) {
            EvalResult.Error("عملية غير صالحة")
        }
    }

    private fun sanitize(expr: String): String {
        var s = NumberFormatter.toWesternDigits(expr)
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace(" ", "")

        // Handle percentage like "50%" -> "(50/100)"
        val pctRegex = Regex("""(\d+(\.\d+)?)%""")
        s = pctRegex.replace(s) { match -> "(${match.groupValues[1]}/100)" }

        // Insert implicit multiplication: e.g. 2(3) -> 2*(3), 2pi -> 2*pi, (2)(3) -> (2)*(3)
        s = s.replace(Regex("""(\d)(pi|e|sqrt|sin|cos|tan|log|ln|\()"""), "$1*$2")
        s = s.replace(Regex("""\)(\d|\(|pi|e|sqrt|sin|cos|tan|log|ln)"""), ")*$1")
        s = s.replace(Regex("""(pi|e)(\d|\()"""), "$1*$2")

        return s
    }

    private fun trySimpleBigDecimal(expr: String): BigDecimal? {
        val simplePattern = Regex("""^([-+]?\d+(\.\d+)?)\s*([+\-*\/])\s*([-+]?\d+(\.\d+)?)$""")
        val match = simplePattern.matchEntire(expr) ?: return null

        val num1 = BigDecimal(match.groupValues[1])
        val op = match.groupValues[3]
        val num2 = BigDecimal(match.groupValues[4])

        return try {
            when (op) {
                "+" -> num1.add(num2)
                "-" -> num1.subtract(num2)
                "*" -> num1.multiply(num2)
                "/" -> {
                    if (num2.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("القسمة على صفر غير جائزة")
                    num1.divide(num2, MathContext.DECIMAL128)
                }
                else -> null
            }
        } catch (e: ArithmeticException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun cleanFloatingPointNoise(value: Double): Double {
        if (abs(value) < 1e-14) return 0.0
        val rounded = BigDecimal.valueOf(value).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
        return rounded.toDouble()
    }
}

/**
 * Recursive descent parser for mathematical expressions.
 */
private class ExpressionParser(
    private val text: String,
    private val angleInDegrees: Boolean
) {
    private var pos = -1
    private var ch = 0

    private fun nextChar() {
        pos++
        ch = if (pos < text.length) text[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < text.length) throw IllegalArgumentException("رمز غير متوقع: " + ch.toChar())
        return x
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor | term `%` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number | function factor | factor `^` factor | factor `!`

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            when {
                eat('+'.code) -> x += parseTerm()
                eat('-'.code) -> x -= parseTerm()
                else -> return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            when {
                eat('*'.code) -> x *= parseFactor()
                eat('/'.code) -> {
                    val divisor = parseFactor()
                    if (abs(divisor) < 1e-15) throw ArithmeticException("القسمة على صفر غير جائزة")
                    x /= divisor
                }
                eat('%'.code) -> {
                    val mod = parseFactor()
                    if (abs(mod) < 1e-15) throw ArithmeticException("القسمة على صفر غير جائزة")
                    x %= mod
                }
                else -> return x
            }
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return +parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = pos

        if (eat('('.code)) {
            x = parseExpression()
            if (!eat(')'.code)) throw IllegalArgumentException("قوس غير مغلق")
        } else if (eat('|'.code)) {
            x = parseExpression()
            if (!eat('|'.code)) throw IllegalArgumentException("قيمة مطلقة غير مغلقة")
            x = abs(x)
        } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
            while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
            // Support scientific E notation like 1e5 or 2.5E-3
            if (ch == 'e'.code || ch == 'E'.code) {
                nextChar()
                if (ch == '+'.code || ch == '-'.code) nextChar()
                while (ch in '0'.code..'9'.code) nextChar()
            }
            x = text.substring(startPos, pos).toDouble()
        } else if (ch in 'a'.code..'z'.code) {
            while (ch in 'a'.code..'z'.code) nextChar()
            val func = text.substring(startPos, pos)
            when (func) {
                "pi" -> x = Math.PI
                "e" -> x = Math.E
                else -> {
                    val arg = parseFactor()
                    x = when (func) {
                        "sqrt" -> {
                            if (arg < 0) throw ArithmeticException("جذر عدد سالب غير معرف في الأعداد الحقيقية")
                            sqrt(arg)
                        }
                        "cbrt" -> cbrt(arg)
                        "sin" -> {
                            val rad = if (angleInDegrees) Math.toRadians(arg) else arg
                            sin(rad)
                        }
                        "cos" -> {
                            val rad = if (angleInDegrees) Math.toRadians(arg) else arg
                            cos(rad)
                        }
                        "tan" -> {
                            val rad = if (angleInDegrees) Math.toRadians(arg) else arg
                            if (angleInDegrees && abs(arg % 180.0) == 90.0) throw ArithmeticException("ظل الزاوية غير معرف (tan)")
                            tan(rad)
                        }
                        "asin" -> {
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("مجال asin بين -1 و 1")
                            val r = asin(arg)
                            if (angleInDegrees) Math.toDegrees(r) else r
                        }
                        "acos" -> {
                            if (arg < -1.0 || arg > 1.0) throw ArithmeticException("مجال acos بين -1 و 1")
                            val r = acos(arg)
                            if (angleInDegrees) Math.toDegrees(r) else r
                        }
                        "atan" -> {
                            val r = atan(arg)
                            if (angleInDegrees) Math.toDegrees(r) else r
                        }
                        "log" -> {
                            if (arg <= 0) throw ArithmeticException("لوغاريتم عدد سالب أو صفر غير معرف")
                            log10(arg)
                        }
                        "ln" -> {
                            if (arg <= 0) throw ArithmeticException("اللوغاريتم الطبيعي لعدد سالب أو صفر غير معرف")
                            ln(arg)
                        }
                        "abs" -> abs(arg)
                        else -> throw IllegalArgumentException("دالة غير معروفة: $func")
                    }
                }
            }
        } else {
            throw IllegalArgumentException("رمز غير متوقع: " + ch.toChar())
        }

        // Postfix operators: power (^) and factorial (!)
        while (true) {
            when {
                eat('^'.code) -> {
                    val p = parseFactor()
                    x = x.pow(p)
                }
                eat('!'.code) -> {
                    if (x < 0 || floor(x) != x || x > 170) {
                        throw ArithmeticException("العاملي (!) معرف فقط للأعداد الصحيحة غير السالبة حتى 170")
                    }
                    x = factorial(x.toLong())
                }
                else -> return x
            }
        }
    }

    private fun factorial(n: Long): Double {
        if (n <= 1) return 1.0
        var res = 1.0
        for (i in 2..n) {
            res *= i
        }
        return res
    }
}
