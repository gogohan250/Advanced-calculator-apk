package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.engine.SoundFeedback
import com.example.ui.components.CalcKey
import com.example.ui.components.KeyType

enum class NumberBase(val radix: Int, val label: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN")
}

enum class WordSize(val bits: Int, val label: String, val mask: ULong) {
    QWORD(64, "QWORD 64-bit", 0xFFFFFFFFFFFFFFFFuL),
    DWORD(32, "DWORD 32-bit", 0xFFFFFFFFuL),
    WORD(16, "WORD 16-bit", 0xFFFFuL),
    BYTE(8, "BYTE 8-bit", 0xFFuL)
}

enum class BitwiseOp(val symbol: String) {
    AND("AND"),
    OR("OR"),
    XOR("XOR"),
    LSH("LSH"),
    RSH("RSH"),
    ADD("+"),
    SUB("-"),
    MUL("×"),
    DIV("÷")
}

@Composable
fun ProgrammerCalculatorScreen(
    settings: AppSettings,
    soundFeedback: SoundFeedback,
    modifier: Modifier = Modifier
) {
    var selectedBase by remember { mutableStateOf(NumberBase.DEC) }
    var wordSize by remember { mutableStateOf(WordSize.QWORD) }

    var currentValue by remember { mutableStateOf(0uL) }
    var storedValue by remember { mutableStateOf<ULong?>(null) }
    var activeOp by remember { mutableStateOf<BitwiseOp?>(null) }
    var isNewInput by remember { mutableStateOf(true) }

    fun maskValue(v: ULong): ULong = v and wordSize.mask

    // Format for different representations
    val hexStr = currentValue.toString(16).uppercase()
    val decStr = currentValue.toString(10)
    val octStr = currentValue.toString(8)
    val binStr = currentValue.toString(2).padStart(wordSize.bits, '0').chunked(4).joinToString(" ")

    fun onDigitInput(char: Char) {
        val digitValue = char.digitToIntOrNull(selectedBase.radix) ?: return
        val current = if (isNewInput) 0uL else currentValue
        val next = (current * selectedBase.radix.toULong()) + digitValue.toULong()
        currentValue = maskValue(next)
        isNewInput = false
    }

    fun onBackspace() {
        val currentStr = currentValue.toString(selectedBase.radix)
        if (currentStr.length <= 1) {
            currentValue = 0uL
        } else {
            val trimmed = currentStr.dropLast(1)
            currentValue = maskValue(trimmed.toULongOrNull(selectedBase.radix) ?: 0uL)
        }
    }

    fun onClear() {
        currentValue = 0uL
        storedValue = null
        activeOp = null
        isNewInput = true
    }

    fun onExecuteOp(op: BitwiseOp) {
        storedValue = currentValue
        activeOp = op
        isNewInput = true
    }

    fun onNot() {
        currentValue = maskValue(currentValue.inv())
        isNewInput = true
    }

    fun onEquals() {
        val prev = storedValue ?: return
        val op = activeOp ?: return
        val curr = currentValue

        val result: ULong = when (op) {
            BitwiseOp.AND -> prev and curr
            BitwiseOp.OR -> prev or curr
            BitwiseOp.XOR -> prev xor curr
            BitwiseOp.LSH -> prev shl (curr.toInt() and 63)
            BitwiseOp.RSH -> prev shr (curr.toInt() and 63)
            BitwiseOp.ADD -> prev + curr
            BitwiseOp.SUB -> prev - curr
            BitwiseOp.MUL -> prev * curr
            BitwiseOp.DIV -> if (curr != 0uL) prev / curr else 0uL
        }

        currentValue = maskValue(result)
        storedValue = null
        activeOp = null
        isNewInput = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Base Display Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Word Size Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الحاسبة البرمجية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WordSize.values().forEach { size ->
                            FilterChip(
                                selected = wordSize == size,
                                onClick = {
                                    wordSize = size
                                    currentValue = maskValue(currentValue)
                                },
                                label = { Text(size.label.split(" ")[0], fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4 Base Rows (HEX, DEC, OCT, BIN)
                BaseDisplayRow("HEX", hexStr, selectedBase == NumberBase.HEX) { selectedBase = NumberBase.HEX }
                BaseDisplayRow("DEC", decStr, selectedBase == NumberBase.DEC) { selectedBase = NumberBase.DEC }
                BaseDisplayRow("OCT", octStr, selectedBase == NumberBase.OCT) { selectedBase = NumberBase.OCT }
                BaseDisplayRow("BIN", binStr, selectedBase == NumberBase.BIN, isMonospace = true) { selectedBase = NumberBase.BIN }
            }
        }

        // Bitwise Operators Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(selected = activeOp == BitwiseOp.AND, onClick = { onExecuteOp(BitwiseOp.AND) }, label = { Text("AND") }, modifier = Modifier.weight(1f))
            FilterChip(selected = activeOp == BitwiseOp.OR, onClick = { onExecuteOp(BitwiseOp.OR) }, label = { Text("OR") }, modifier = Modifier.weight(1f))
            FilterChip(selected = activeOp == BitwiseOp.XOR, onClick = { onExecuteOp(BitwiseOp.XOR) }, label = { Text("XOR") }, modifier = Modifier.weight(1f))
            FilterChip(selected = false, onClick = { onNot() }, label = { Text("NOT") }, modifier = Modifier.weight(1f))
            FilterChip(selected = activeOp == BitwiseOp.LSH, onClick = { onExecuteOp(BitwiseOp.LSH) }, label = { Text("<<") }, modifier = Modifier.weight(1f))
            FilterChip(selected = activeOp == BitwiseOp.RSH, onClick = { onExecuteOp(BitwiseOp.RSH) }, label = { Text(">>") }, modifier = Modifier.weight(1f))
        }

        // Programmer Keypad (HEX keys A-F, digits, ops)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Row 1: A, B, C, AC, ⌫
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("A", KeyType.NUMBER, { onDigitInput('A') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("B", KeyType.NUMBER, { onDigitInput('B') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("C", KeyType.NUMBER, { onDigitInput('C') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("AC", KeyType.CLEAR, { onClear() }, fontSize = 16.sp, modifier = Modifier.weight(1f))
                CalcKey("⌫", KeyType.FUNCTION, { onBackspace() }, fontSize = 16.sp, modifier = Modifier.weight(1f))
            }
            // Row 2: D, E, F, ÷, ×
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("D", KeyType.NUMBER, { onDigitInput('D') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("E", KeyType.NUMBER, { onDigitInput('E') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("F", KeyType.NUMBER, { onDigitInput('F') }, fontSize = 18.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase == NumberBase.HEX)
                CalcKey("÷", KeyType.OPERATOR, { onExecuteOp(BitwiseOp.DIV) }, fontSize = 22.sp, modifier = Modifier.weight(1f))
                CalcKey("×", KeyType.OPERATOR, { onExecuteOp(BitwiseOp.MUL) }, fontSize = 22.sp, modifier = Modifier.weight(1f))
            }
            // Row 3: 7, 8, 9, -, ( )
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("7", KeyType.NUMBER, { onDigitInput('7') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 7)
                CalcKey("8", KeyType.NUMBER, { onDigitInput('8') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 8)
                CalcKey("9", KeyType.NUMBER, { onDigitInput('9') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 9)
                CalcKey("−", KeyType.OPERATOR, { onExecuteOp(BitwiseOp.SUB) }, fontSize = 24.sp, modifier = Modifier.weight(1f))
                CalcKey("+", KeyType.OPERATOR, { onExecuteOp(BitwiseOp.ADD) }, fontSize = 22.sp, modifier = Modifier.weight(1f))
            }
            // Row 4: 4, 5, 6, 1, 2
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("4", KeyType.NUMBER, { onDigitInput('4') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 4)
                CalcKey("5", KeyType.NUMBER, { onDigitInput('5') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 5)
                CalcKey("6", KeyType.NUMBER, { onDigitInput('6') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 6)
                CalcKey("1", KeyType.NUMBER, { onDigitInput('1') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = true)
                CalcKey("2", KeyType.NUMBER, { onDigitInput('2') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 2)
            }
            // Row 5: 3, 0, =
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcKey("3", KeyType.NUMBER, { onDigitInput('3') }, fontSize = 20.sp, modifier = Modifier.weight(1f), enabledKey = selectedBase.radix > 3)
                CalcKey("0", KeyType.NUMBER, { onDigitInput('0') }, fontSize = 20.sp, modifier = Modifier.weight(2f), enabledKey = true)
                CalcKey("=", KeyType.EQUALS, { onEquals() }, fontSize = 24.sp, modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun BaseDisplayRow(
    baseLabel: String,
    value: String,
    isSelected: Boolean,
    isMonospace: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = baseLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (isMonospace) 12.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun CalcKey(
    label: String,
    type: KeyType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    enabledKey: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (!enabledKey) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else when (type) {
            KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant
            KeyType.OPERATOR -> MaterialTheme.colorScheme.primary
            KeyType.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant
            KeyType.EQUALS -> MaterialTheme.colorScheme.primary
            KeyType.CLEAR -> MaterialTheme.colorScheme.errorContainer
        },
        modifier = modifier
            .padding(2.dp)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = enabledKey, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = if (!enabledKey) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                else when (type) {
                    KeyType.OPERATOR, KeyType.EQUALS -> Color.White
                    KeyType.CLEAR -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
