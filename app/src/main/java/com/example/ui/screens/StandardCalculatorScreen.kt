package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.HistoryRepository
import com.example.data.NumberFormatStyle
import com.example.engine.EvalResult
import com.example.engine.MathEvaluator
import com.example.engine.NumberFormatter
import com.example.engine.SoundFeedback
import com.example.ui.components.CalcKey
import com.example.ui.components.DisplayScreen
import com.example.ui.components.KeyType

@Composable
fun StandardCalculatorScreen(
    settings: AppSettings,
    historyRepo: HistoryRepository,
    soundFeedback: SoundFeedback,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var expression by remember { mutableStateOf("") }
    var liveResult by remember { mutableStateOf("0") }
    var memoryValue by remember { mutableDoubleStateOf(0.0) }
    var isSecondActive by remember { mutableStateOf(false) }
    var isAngleDegrees by remember { mutableStateOf(settings.angleInDegrees) }
    var showScientificPad by remember { mutableStateOf(false) }

    val evaluator = remember(isAngleDegrees) {
        MathEvaluator(angleInDegrees = isAngleDegrees)
    }

    // Evaluate live whenever expression changes
    LaunchedEffect(expression, isAngleDegrees) {
        if (expression.isBlank()) {
            liveResult = "0"
        } else {
            when (val eval = evaluator.evaluate(expression)) {
                is EvalResult.Success -> {
                    liveResult = NumberFormatter.formatNumber(
                        value = eval.value,
                        style = settings.numberFormat,
                        useGrouping = settings.useGroupingSeparator,
                        maxDecimals = settings.decimalPrecision
                    )
                }
                is EvalResult.Error -> {
                    // Incomplete expressions are normal while typing; only show error if full evaluation was requested
                }
            }
        }
    }

    fun appendToken(token: String) {
        expression += token
    }

    fun clearAll() {
        expression = ""
        liveResult = "0"
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            // If deleting a word like "sin(", "sqrt(", "log(", remove the entire token
            val words = listOf("asin(", "acos(", "atan(", "sqrt(", "cbrt(", "sin(", "cos(", "tan(", "log(", "ln(", "abs(")
            val matchedWord = words.firstOrNull { expression.endsWith(it) }
            expression = if (matchedWord != null) {
                expression.dropLast(matchedWord.length)
            } else {
                expression.dropLast(1)
            }
        }
    }

    fun calculateEquals() {
        if (expression.isBlank()) return
        when (val res = evaluator.evaluate(expression)) {
            is EvalResult.Success -> {
                val formatted = NumberFormatter.formatNumber(
                    value = res.value,
                    style = settings.numberFormat,
                    useGrouping = settings.useGroupingSeparator,
                    maxDecimals = settings.decimalPrecision
                )
                val displayExpr = NumberFormatter.formatExpressionForDisplay(expression, settings.numberFormat)
                historyRepo.addHistory(displayExpr, formatted)
                expression = res.formattedDisplay
                liveResult = formatted
            }
            is EvalResult.Error -> {
                liveResult = res.message
            }
        }
    }

    fun toggleSign() {
        if (expression.isBlank() && liveResult != "0") {
            expression = "-($liveResult)"
        } else if (expression.startsWith("-(") && expression.endsWith(")")) {
            expression = expression.substring(2, expression.length - 1)
        } else {
            expression = "-($expression)"
        }
    }

    fun applyReciprocal() {
        expression = if (expression.isBlank() && liveResult != "0") {
            "1/($liveResult)"
        } else {
            "1/($expression)"
        }
    }

    fun applySquare() {
        expression = if (expression.isBlank() && liveResult != "0") {
            "($liveResult)^2"
        } else {
            "($expression)^2"
        }
    }

    fun applySquareRoot() {
        expression = if (expression.isBlank() && liveResult != "0") {
            "sqrt($liveResult)"
        } else {
            "sqrt($expression)"
        }
    }

    // Number conversion for keypad button labels
    fun numLabel(digit: String): String {
        return if (settings.numberFormat == NumberFormatStyle.ARABIC) {
            NumberFormatter.toArabicDigits(digit)
        } else digit
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = if (isLandscape) 12.dp else 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Unit
        DisplayScreen(
            expression = NumberFormatter.formatExpressionForDisplay(expression, settings.numberFormat),
            liveResult = liveResult,
            hasMemory = memoryValue != 0.0,
            isDegrees = isAngleDegrees,
            onHistoryClick = onOpenHistory,
            modifier = Modifier.weight(if (isLandscape) 0.35f else 0.28f)
        )

        // Memory & Quick Function Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Memory operations
            FilterChip(
                selected = false,
                onClick = { memoryValue = 0.0 },
                label = { Text("MC", fontSize = 12.sp) },
                enabled = memoryValue != 0.0
            )
            FilterChip(
                selected = false,
                onClick = {
                    val memStr = if (memoryValue % 1.0 == 0.0) memoryValue.toLong().toString() else memoryValue.toString()
                    appendToken(memStr)
                },
                label = { Text("MR", fontSize = 12.sp) },
                enabled = memoryValue != 0.0
            )
            FilterChip(
                selected = false,
                onClick = {
                    val curr = when (val res = evaluator.evaluate(if (expression.isNotBlank()) expression else liveResult)) {
                        is EvalResult.Success -> res.value
                        else -> 0.0
                    }
                    memoryValue += curr
                },
                label = { Text("M+", fontSize = 12.sp) }
            )
            FilterChip(
                selected = false,
                onClick = {
                    val curr = when (val res = evaluator.evaluate(if (expression.isNotBlank()) expression else liveResult)) {
                        is EvalResult.Success -> res.value
                        else -> 0.0
                    }
                    memoryValue -= curr
                },
                label = { Text("M-", fontSize = 12.sp) }
            )

            VerticalDivider(modifier = Modifier.height(20.dp))

            // DEG/RAD Toggle
            FilterChip(
                selected = isAngleDegrees,
                onClick = { isAngleDegrees = !isAngleDegrees },
                label = { Text(if (isAngleDegrees) "درجات DEG" else "راديان RAD", fontSize = 12.sp) }
            )

            // Scientific panel toggle for portrait mode
            if (!isLandscape) {
                FilterChip(
                    selected = showScientificPad,
                    onClick = { showScientificPad = !showScientificPad },
                    label = { Text("العلمية", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }

            // Quick function badges
            FilterChip(
                selected = false,
                onClick = { applySquare() },
                label = { Text("x²", fontSize = 12.sp) }
            )
            FilterChip(
                selected = false,
                onClick = { applySquareRoot() },
                label = { Text("√", fontSize = 12.sp) }
            )
            FilterChip(
                selected = false,
                onClick = { applyReciprocal() },
                label = { Text("1/x", fontSize = 12.sp) }
            )
        }

        // Keypad Container (Landscape uses 2-column split, portrait uses standard + expandable sci panel)
        if (isLandscape) {
            LandscapeKeypad(
                settings = settings,
                soundFeedback = soundFeedback,
                isSecondActive = isSecondActive,
                onSecondToggle = { isSecondActive = !isSecondActive },
                onToken = { appendToken(it) },
                onClear = { clearAll() },
                onBackspace = { backspace() },
                onEquals = { calculateEquals() },
                onToggleSign = { toggleSign() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.72f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Expandable Scientific Ribbon / Panel
                AnimatedVisibility(visible = showScientificPad) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sci Row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalcKey(
                                label = if (isSecondActive) "1st" else "2nd",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { isSecondActive = !isSecondActive },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = if (isSecondActive) "sin⁻¹" else "sin",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { appendToken(if (isSecondActive) "asin(" else "sin(") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = if (isSecondActive) "cos⁻¹" else "cos",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { appendToken(if (isSecondActive) "acos(" else "cos(") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = if (isSecondActive) "tan⁻¹" else "tan",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { appendToken(if (isSecondActive) "atan(" else "tan(") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = "π",
                                type = KeyType.FUNCTION,
                                fontSize = 16.sp,
                                onClick = { appendToken("pi") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Sci Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CalcKey(
                                label = if (isSecondActive) "10ˣ" else "log",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { if (isSecondActive) appendToken("10^") else appendToken("log(") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = if (isSecondActive) "eˣ" else "ln",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { if (isSecondActive) appendToken("e^") else appendToken("ln(") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = "xʸ",
                                type = KeyType.FUNCTION,
                                fontSize = 14.sp,
                                onClick = { appendToken("^") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = "x!",
                                type = KeyType.FUNCTION,
                                fontSize = 15.sp,
                                onClick = { appendToken("!") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                label = "e",
                                type = KeyType.FUNCTION,
                                fontSize = 16.sp,
                                onClick = { appendToken("e") },
                                soundFeedback = soundFeedback,
                                hapticEnabled = settings.hapticFeedbackEnabled,
                                soundEnabled = settings.soundEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Standard Keypad Matrix (5 Rows x 4 Columns)
                // Row 1: AC, ( ), %, ÷
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcKey(
                        label = "AC",
                        type = KeyType.CLEAR,
                        fontSize = 20.sp,
                        onClick = { clearAll() },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "( )",
                        type = KeyType.FUNCTION,
                        fontSize = 20.sp,
                        onClick = {
                            val openCount = expression.count { it == '(' }
                            val closeCount = expression.count { it == ')' }
                            if (openCount > closeCount && expression.lastOrNull()?.isDigit() == true) {
                                appendToken(")")
                            } else {
                                appendToken("(")
                            }
                        },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "%",
                        type = KeyType.FUNCTION,
                        fontSize = 22.sp,
                        onClick = { appendToken("%") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "÷",
                        type = KeyType.OPERATOR,
                        fontSize = 26.sp,
                        onClick = { appendToken("÷") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcKey(
                        label = numLabel("7"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("7") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("8"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("8") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("9"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("9") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "×",
                        type = KeyType.OPERATOR,
                        fontSize = 26.sp,
                        onClick = { appendToken("×") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcKey(
                        label = numLabel("4"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("4") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("5"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("5") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("6"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("6") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "−",
                        type = KeyType.OPERATOR,
                        fontSize = 28.sp,
                        onClick = { appendToken("−") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcKey(
                        label = numLabel("1"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("1") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("2"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("2") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("3"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("3") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "+",
                        type = KeyType.OPERATOR,
                        fontSize = 26.sp,
                        onClick = { appendToken("+") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: ±, 0, ., = (with backspace button alongside)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalcKey(
                        label = "±",
                        type = KeyType.FUNCTION,
                        fontSize = 22.sp,
                        onClick = { toggleSign() },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = numLabel("0"),
                        type = KeyType.NUMBER,
                        onClick = { appendToken("0") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = ".",
                        type = KeyType.NUMBER,
                        fontSize = 26.sp,
                        onClick = { appendToken(".") },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "⌫",
                        type = KeyType.FUNCTION,
                        fontSize = 20.sp,
                        onClick = { backspace() },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    CalcKey(
                        label = "=",
                        type = KeyType.EQUALS,
                        fontSize = 28.sp,
                        onClick = { calculateEquals() },
                        soundFeedback = soundFeedback,
                        hapticEnabled = settings.hapticFeedbackEnabled,
                        soundEnabled = settings.soundEnabled,
                        modifier = Modifier.weight(1f),
                        testTag = "equals_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeKeypad(
    settings: AppSettings,
    soundFeedback: SoundFeedback,
    isSecondActive: Boolean,
    onSecondToggle: () -> Unit,
    onToken: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleSign: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun numLabel(digit: String): String {
        return if (settings.numberFormat == NumberFormatStyle.ARABIC) {
            NumberFormatter.toArabicDigits(digit)
        } else digit
    }

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Left Column: Scientific keys (5 cols x 4 rows)
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Sci row 1
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey(if (isSecondActive) "1st" else "2nd", KeyType.FUNCTION, onSecondToggle, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey(if (isSecondActive) "sin⁻¹" else "sin", KeyType.FUNCTION, { onToken(if (isSecondActive) "asin(" else "sin(") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey(if (isSecondActive) "cos⁻¹" else "cos", KeyType.FUNCTION, { onToken(if (isSecondActive) "acos(" else "cos(") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey(if (isSecondActive) "tan⁻¹" else "tan", KeyType.FUNCTION, { onToken(if (isSecondActive) "atan(" else "tan(") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey("π", KeyType.FUNCTION, { onToken("pi") }, soundFeedback = soundFeedback, fontSize = 15.sp, modifier = Modifier.weight(1f))
            }
            // Sci row 2
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey(if (isSecondActive) "10ˣ" else "log", KeyType.FUNCTION, { if (isSecondActive) onToken("10^") else onToken("log(") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey(if (isSecondActive) "eˣ" else "ln", KeyType.FUNCTION, { if (isSecondActive) onToken("e^") else onToken("ln(") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey("xʸ", KeyType.FUNCTION, { onToken("^") }, soundFeedback = soundFeedback, fontSize = 13.sp, modifier = Modifier.weight(1f))
                CalcKey("x!", KeyType.FUNCTION, { onToken("!") }, soundFeedback = soundFeedback, fontSize = 13.sp, modifier = Modifier.weight(1f))
                CalcKey("e", KeyType.FUNCTION, { onToken("e") }, soundFeedback = soundFeedback, fontSize = 15.sp, modifier = Modifier.weight(1f))
            }
            // Sci row 3
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey("√", KeyType.FUNCTION, { onToken("sqrt(") }, soundFeedback = soundFeedback, fontSize = 14.sp, modifier = Modifier.weight(1f))
                CalcKey("x²", KeyType.FUNCTION, { onToken("^2") }, soundFeedback = soundFeedback, fontSize = 13.sp, modifier = Modifier.weight(1f))
                CalcKey("1/x", KeyType.FUNCTION, { onToken("1/") }, soundFeedback = soundFeedback, fontSize = 12.sp, modifier = Modifier.weight(1f))
                CalcKey("|x|", KeyType.FUNCTION, { onToken("abs(") }, soundFeedback = soundFeedback, fontSize = 13.sp, modifier = Modifier.weight(1f))
                CalcKey("E", KeyType.FUNCTION, { onToken("E") }, soundFeedback = soundFeedback, fontSize = 13.sp, modifier = Modifier.weight(1f))
            }
            // Sci row 4
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey("(", KeyType.FUNCTION, { onToken("(") }, soundFeedback = soundFeedback, fontSize = 15.sp, modifier = Modifier.weight(1f))
                CalcKey(")", KeyType.FUNCTION, { onToken(")") }, soundFeedback = soundFeedback, fontSize = 15.sp, modifier = Modifier.weight(1f))
                CalcKey("%", KeyType.FUNCTION, { onToken("%") }, soundFeedback = soundFeedback, fontSize = 15.sp, modifier = Modifier.weight(1f))
                CalcKey("±", KeyType.FUNCTION, onToggleSign, soundFeedback = soundFeedback, fontSize = 16.sp, modifier = Modifier.weight(1f))
                CalcKey("⌫", KeyType.FUNCTION, onBackspace, soundFeedback = soundFeedback, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
        }

        // Right Column: Standard Numeric keypad
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey("AC", KeyType.CLEAR, onClear, soundFeedback = soundFeedback, fontSize = 16.sp, modifier = Modifier.weight(1f))
                CalcKey(numLabel("7"), KeyType.NUMBER, { onToken("7") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("8"), KeyType.NUMBER, { onToken("8") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("9"), KeyType.NUMBER, { onToken("9") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey("÷", KeyType.OPERATOR, { onToken("÷") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey(numLabel("4"), KeyType.NUMBER, { onToken("4") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("5"), KeyType.NUMBER, { onToken("5") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("6"), KeyType.NUMBER, { onToken("6") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey("×", KeyType.OPERATOR, { onToken("×") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey(numLabel("1"), KeyType.NUMBER, { onToken("1") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("2"), KeyType.NUMBER, { onToken("2") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(numLabel("3"), KeyType.NUMBER, { onToken("3") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey("−", KeyType.OPERATOR, { onToken("−") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcKey(numLabel("0"), KeyType.NUMBER, { onToken("0") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey(".", KeyType.NUMBER, { onToken(".") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
                CalcKey("=", KeyType.EQUALS, onEquals, soundFeedback = soundFeedback, modifier = Modifier.weight(2f))
                CalcKey("+", KeyType.OPERATOR, { onToken("+") }, soundFeedback = soundFeedback, modifier = Modifier.weight(1f))
            }
        }
    }
}
