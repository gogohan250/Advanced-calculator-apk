package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.CurrencyInfo
import com.example.data.CurrencyRepository
import com.example.engine.NumberFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    currencyRepo: CurrencyRepository,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val rates by currencyRepo.rates.collectAsState()
    val lastUpdated by currencyRepo.lastUpdated.collectAsState()
    val isLoading by currencyRepo.isLoading.collectAsState()

    var amountStr by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf(currencyRepo.supportedCurrencies.first { it.code == "USD" }) }
    var toCurrency by remember { mutableStateOf(currencyRepo.supportedCurrencies.first { it.code == "SAR" }) }

    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
    val convertedVal = remember(amountDouble, fromCurrency, toCurrency, rates) {
        currencyRepo.convert(amountDouble, fromCurrency.code, toCurrency.code)
    }

    val singleRate = remember(fromCurrency, toCurrency, rates) {
        currencyRepo.convert(1.0, fromCurrency.code, toCurrency.code)
    }

    val formattedResult = remember(convertedVal, settings.numberFormat) {
        NumberFormatter.formatNumber(
            value = convertedVal,
            style = settings.numberFormat,
            useGrouping = settings.useGroupingSeparator,
            maxDecimals = 4
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Refresh Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (lastUpdated.contains("الإنترنت")) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (lastUpdated.contains("الإنترنت")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = lastUpdated,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            val ok = currencyRepo.refreshRates()
                            if (ok) {
                                Toast.makeText(context, "تم تحديث أسعار الصرف بنجاح", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "تعذر التحديث، تم استخدام الأسعار المخزنة بدون إنترنت", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحديث", fontSize = 12.sp)
                    }
                }
            }
        }

        // From Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "المبلغ والعملة المحولة منها",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        val western = NumberFormatter.toWesternDigits(it)
                        if (western.isEmpty() || western.matches(Regex("""^[-+]?\d*\.?\d*$"""))) {
                            amountStr = western
                        }
                    },
                    label = { Text("المبلغ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyDropdown(
                    label = "اختر العملة",
                    selected = fromCurrency,
                    currencies = currencyRepo.supportedCurrencies,
                    onSelect = { fromCurrency = it }
                )
            }
        }

        // Swap Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = {
                    val temp = fromCurrency
                    fromCurrency = toCurrency
                    toCurrency = temp
                },
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "تبديل العملات")
            }
        }

        // To Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "القيمة الناتجة بالعملة المستهدفة",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$formattedResult ${toCurrency.symbol}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سعر التحويل: 1 ${fromCurrency.code} = ${"%.4f".format(singleRate)} ${toCurrency.code}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                CurrencyDropdown(
                    label = "العملة المستهدفة",
                    selected = toCurrency,
                    currencies = currencyRepo.supportedCurrencies,
                    onSelect = { toCurrency = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: CurrencyInfo,
    currencies: List<CurrencyInfo>,
    onSelect: (CurrencyInfo) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "${selected.flag} ${selected.nameAr} (${selected.code})",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { cur ->
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cur.flag, fontSize = 20.sp)
                            Text(cur.nameAr, fontWeight = FontWeight.Medium)
                            Text("(${cur.code})", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onSelect(cur)
                        expanded = false
                    }
                )
            }
        }
    }
}
