package com.example.engine

import kotlin.math.*

data class PlotPoint(val x: Float, val y: Float, val isValid: Boolean = true)

class FunctionPlotter {

    /**
     * Replaces variable 'x' in the expression with a concrete numeric value and evaluates safely.
     */
    fun evaluateAt(expression: String, xVal: Double, angleInDegrees: Boolean = false): Double {
        return try {
            var expr = expression.lowercase()
                .replace(" ", "")
                .replace("y=", "")
                .replace("f(x)=", "")

            // Insert multiplication between numbers and x, e.g. 2x -> 2*x, x^2 -> (x)^2
            expr = expr.replace(Regex("""(\d)x"""), "$1*x")
            expr = expr.replace(Regex("""x(\d)"""), "x*$1")

            val xStr = if (xVal < 0) "($xVal)" else "$xVal"
            val replaced = expr.replace("x", xStr)

            val evaluator = MathEvaluator(angleInDegrees)
            when (val res = evaluator.evaluate(replaced)) {
                is EvalResult.Success -> res.value
                is EvalResult.Error -> Double.NaN
            }
        } catch (e: Exception) {
            Double.NaN
        }
    }

    /**
     * Generates a list of continuous plot points for a given math viewport.
     */
    fun computePoints(
        expression: String,
        minX: Double,
        maxX: Double,
        stepCount: Int = 300,
        angleInDegrees: Boolean = false
    ): List<PlotPoint> {
        val points = mutableListOf<PlotPoint>()
        if (maxX <= minX || stepCount <= 1) return points

        val step = (maxX - minX) / stepCount
        var currentX = minX

        for (i in 0..stepCount) {
            val yVal = evaluateAt(expression, currentX, angleInDegrees)
            if (yVal.isNaN() || yVal.isInfinite() || abs(yVal) > 1e6) {
                points.add(PlotPoint(currentX.toFloat(), 0f, isValid = false))
            } else {
                points.add(PlotPoint(currentX.toFloat(), yVal.toFloat(), isValid = true))
            }
            currentX += step
        }
        return points
    }
}
