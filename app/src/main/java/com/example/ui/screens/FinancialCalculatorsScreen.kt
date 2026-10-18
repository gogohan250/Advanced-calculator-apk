package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.engine.NumberFormatter
import kotlin.math.pow

enum class FinanceSubTab(val title: String) {
    LOAN("القرض والتقسيط"),
    PERCENT("النسبة المئوية"),
    VAT("ضريبة القيمة المضافة"),
    TIP("تقسيم الفاتورة")
}

@Composable
fun FinancialCalculatorsScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(FinanceSubTab.LOAN) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-Tab Switcher
        ScrollableTabRow(
            selectedTabIndex = currentSubTab.ordinal,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            FinanceSubTab.values().forEach { tab ->
                Tab(
                    selected = currentSubTab == tab,
                    onClick = { currentSubTab = tab },
                    text = { Text(tab.title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        // Selected Sub Calculator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (currentSubTab) {
                FinanceSubTab.LOAN -> LoanCalculatorView(settings)
                FinanceSubTab.PERCENT -> PercentageCalculatorView(settings)
                FinanceSubTab.VAT -> VatCalculatorView(settings)
                FinanceSubTab.TIP -> TipCalculatorView(settings)
            }
        }
    }
}

// 1. LOAN / EMI CALCULATOR
@Composable
private fun LoanCalculatorView(settings: AppSettings) {
    var loanAmountStr by remember { mutableStateOf("100000") }
    var interestRateStr by remember { mutableStateOf("5") }
    var termYearsStr by remember { mutableStateOf("5") }

    val principal = loanAmountStr.toDoubleOrNull() ?: 0.0
    val annualRate = (interestRateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val years = termYearsStr.toDoubleOrNull() ?: 1.0

    val n = (years * 12).toInt().coerceAtLeast(1)
    val monthlyRate = annualRate / 12.0

    val monthlyPayment = if (monthlyRate > 0) {
        val factor = (1 + monthlyRate).pow(n.toDouble())
        (principal * monthlyRate * factor) / (factor - 1)
    } else {
        principal / n
    }

    val totalPayment = monthlyPayment * n
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FinancialInputCard("مبلغ القرض / التمويل", loanAmountStr, "ريال / جنيه...") { loanAmountStr = it }
        FinancialInputCard("نسبة الفائدة السنوية (%)", interestRateStr, "%") { interestRateStr = it }
        FinancialInputCard("مدة التمويل (بالسنوات)", termYearsStr, "سنة") { termYearsStr = it }

        FinancialResultCard(
            title = "تفاصيل التقسيط والسداد",
            items = listOf(
                "القسط الشهري" to NumberFormatter.formatNumber(monthlyPayment, settings.numberFormat, settings.useGroupingSeparator, 2),
                "إجمالي الفوائد" to NumberFormatter.formatNumber(totalInterest, settings.numberFormat, settings.useGroupingSeparator, 2),
                "إجمالي المبلغ المسدد" to NumberFormatter.formatNumber(totalPayment, settings.numberFormat, settings.useGroupingSeparator, 2),
                "عدد الأقساط" to NumberFormatter.formatNumber(n.toDouble(), settings.numberFormat, false, 0)
            )
        )
    }
}

// 2. PERCENTAGE CALCULATOR
@Composable
private fun PercentageCalculatorView(settings: AppSettings) {
    var mode by remember { mutableStateOf(0) } // 0: Discount, 1: Increase, 2: X% of Y
    var val1 by remember { mutableStateOf("500") }
    var val2 by remember { mutableStateOf("20") }

    val num1 = val1.toDoubleOrNull() ?: 0.0
    val num2 = val2.toDoubleOrNull() ?: 0.0

    val (resLabel1, resVal1, resLabel2, resVal2) = when (mode) {
        0 -> { // Discount
            val discountAmount = num1 * (num2 / 100.0)
            val finalPrice = num1 - discountAmount
            Quad("السعر النهائي بعد الخصم", finalPrice, "قيمة التوفير (الخصم)", discountAmount)
        }
        1 -> { // Increase
            val increaseAmount = num1 * (num2 / 100.0)
            val finalPrice = num1 + increaseAmount
            Quad("السعر النهائي بعد الزيادة", finalPrice, "مبلغ الزيادة", increaseAmount)
        }
        else -> { // X% of Y
            val percentVal = (num2 / 100.0) * num1
            Quad("النتيجة", percentVal, "المتبقي", num1 - percentVal)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("خصم %") }, modifier = Modifier.weight(1f))
            FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("زيادة %") }, modifier = Modifier.weight(1f))
            FilterChip(selected = mode == 2, onClick = { mode = 2 }, label = { Text("نسبة من رقم") }, modifier = Modifier.weight(1f))
        }

        FinancialInputCard(if (mode == 2) "المبلغ الكلي" else "السعر الأصلي", val1, "") { val1 = it }
        FinancialInputCard("النسبة المئوية (%)", val2, "%") { val2 = it }

        FinancialResultCard(
            title = "نتيجة الحساب",
            items = listOf(
                resLabel1 to NumberFormatter.formatNumber(resVal1, settings.numberFormat, settings.useGroupingSeparator, 2),
                resLabel2 to NumberFormatter.formatNumber(resVal2, settings.numberFormat, settings.useGroupingSeparator, 2)
            )
        )
    }
}

// 3. VAT CALCULATOR
@Composable
private fun VatCalculatorView(settings: AppSettings) {
    var amountStr by remember { mutableStateOf("1000") }
    var vatRateStr by remember { mutableStateOf("15") }
    var isInclusive by remember { mutableStateOf(false) } // true: inclusive of VAT, false: exclusive

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = (vatRateStr.toDoubleOrNull() ?: 15.0) / 100.0

    val (netAmount, vatAmount, grossAmount) = if (isInclusive) {
        val net = amount / (1.0 + rate)
        val vat = amount - net
        Triple(net, vat, amount)
    } else {
        val vat = amount * rate
        val gross = amount + vat
        Triple(amount, vat, gross)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !isInclusive,
                onClick = { isInclusive = false },
                label = { Text("السعر غير شامل الضريبة") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isInclusive,
                onClick = { isInclusive = true },
                label = { Text("السعر شامل الضريبة") },
                modifier = Modifier.weight(1f)
            )
        }

        FinancialInputCard("المبلغ المدخل", amountStr, "ريال / جنيه...") { amountStr = it }
        FinancialInputCard("نسبة الضريبة (%)", vatRateStr, "% (السعودية 15%)") { vatRateStr = it }

        FinancialResultCard(
            title = "تفاصيل ضريبة القيمة المضافة",
            items = listOf(
                "المبلغ الأساسي (قبل الضريبة)" to NumberFormatter.formatNumber(netAmount, settings.numberFormat, settings.useGroupingSeparator, 2),
                "مبلغ الضريبة" to NumberFormatter.formatNumber(vatAmount, settings.numberFormat, settings.useGroupingSeparator, 2),
                "المبلغ الإجمالي (بعد الضريبة)" to NumberFormatter.formatNumber(grossAmount, settings.numberFormat, settings.useGroupingSeparator, 2)
            )
        )
    }
}

// 4. TIP & BILL SPLITTER
@Composable
private fun TipCalculatorView(settings: AppSettings) {
    var billStr by remember { mutableStateOf("250") }
    var tipPercent by remember { mutableFloatStateOf(10f) }
    var peopleCount by remember { mutableIntStateOf(3) }

    val bill = billStr.toDoubleOrNull() ?: 0.0
    val tipAmount = bill * (tipPercent / 100.0)
    val totalBill = bill + tipAmount
    val perPerson = totalBill / peopleCount.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FinancialInputCard("قيمة الفاتورة", billStr, "ريال...") { billStr = it }

        // Tip Selector
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("نسبة البقشيش / الإكرامية: ${tipPercent.toInt()}%", fontWeight = FontWeight.Medium)
                Slider(
                    value = tipPercent,
                    onValueChange = { tipPercent = it },
                    valueRange = 0f..30f,
                    steps = 5
                )
            }
        }

        // People Stepper
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("عدد الأشخاص", fontWeight = FontWeight.Medium)
                    Text("تقسيم الفاتورة بالتساوي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { if (peopleCount > 1) peopleCount-- },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "$peopleCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    FilledTonalButton(
                        onClick = { peopleCount++ },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        FinancialResultCard(
            title = "ملخص تقسيم الفاتورة",
            items = listOf(
                "نصيب كل شخص" to NumberFormatter.formatNumber(perPerson, settings.numberFormat, settings.useGroupingSeparator, 2),
                "إجمالي البقشيش" to NumberFormatter.formatNumber(tipAmount, settings.numberFormat, settings.useGroupingSeparator, 2),
                "المبلغ الكلي المطلوب" to NumberFormatter.formatNumber(totalBill, settings.numberFormat, settings.useGroupingSeparator, 2)
            )
        )
    }
}

// Helpers
private data class Quad(val l1: String, val v1: Double, val l2: String, val v2: Double)

@Composable
private fun FinancialInputCard(
    label: String,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = value,
                onValueChange = {
                    val western = NumberFormatter.toWesternDigits(it)
                    if (western.isEmpty() || western.matches(Regex("""^[-+]?\d*\.?\d*$"""))) {
                        onValueChange(western)
                    }
                },
                placeholder = { Text(hint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun FinancialResultCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
            HorizontalDivider()
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                }
            }
        }
    }
}
