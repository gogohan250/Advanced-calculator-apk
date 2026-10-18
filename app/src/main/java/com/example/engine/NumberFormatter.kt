package com.example.engine

import com.example.data.NumberFormatStyle
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {

    private val westernToArabicMap = mapOf(
        '0' to '٠',
        '1' to '١',
        '2' to '٢',
        '3' to '٣',
        '4' to '٤',
        '5' to '٥',
        '6' to '٦',
        '7' to '٧',
        '8' to '٨',
        '9' to '٩'
    )

    private val arabicToWesternMap = westernToArabicMap.entries.associate { (k, v) -> v to k }

    fun toArabicDigits(input: String): String {
        val sb = java.lang.StringBuilder()
        for (char in input) {
            sb.append(westernToArabicMap[char] ?: char)
        }
        return sb.toString()
    }

    fun toWesternDigits(input: String): String {
        val sb = java.lang.StringBuilder()
        for (char in input) {
            sb.append(arabicToWesternMap[char] ?: char)
        }
        return sb.toString()
    }

    fun formatNumber(
        value: Double,
        style: NumberFormatStyle = NumberFormatStyle.ARABIC,
        useGrouping: Boolean = true,
        maxDecimals: Int = 8
    ): String {
        if (value.isNaN()) return "غير معرف"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        val bd = runCatching {
            BigDecimal.valueOf(value).setScale(maxDecimals, RoundingMode.HALF_UP).stripTrailingZeros()
        }.getOrNull() ?: BigDecimal.ZERO

        val plainStr = bd.toPlainString()

        // If number is too large or too small for plain string, use scientific notation
        val formattedWestern = if (kotlin.math.abs(value) >= 1e12 || (kotlin.math.abs(value) in 1e-7..1e-1 && plainStr.length > 14)) {
            val df = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
            df.format(value)
        } else if (useGrouping) {
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
                decimalSeparator = '.'
            }
            val parts = plainStr.split('.')
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1] else null

            val groupedInt = runCatching {
                val isNegative = intPart.startsWith("-")
                val cleanInt = if (isNegative) intPart.substring(1) else intPart
                val grouped = cleanInt.reversed().chunked(3).joinToString(",").reversed()
                if (isNegative) "-$grouped" else grouped
            }.getOrDefault(intPart)

            if (decPart != null) "$groupedInt.$decPart" else groupedInt
        } else {
            plainStr
        }

        return if (style == NumberFormatStyle.ARABIC) {
            toArabicDigits(formattedWestern)
        } else {
            formattedWestern
        }
    }

    fun formatExpressionForDisplay(
        expression: String,
        style: NumberFormatStyle = NumberFormatStyle.ARABIC
    ): String {
        return if (style == NumberFormatStyle.ARABIC) {
            toArabicDigits(expression)
        } else {
            toWesternDigits(expression)
        }
    }
}
