package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.engine.NumberFormatter

data class UnitItem(val id: String, val nameAr: String, val factorToBase: Double)

enum class UnitCategory(val titleAr: String, val iconText: String) {
    LENGTH("الطول", "📏"),
    WEIGHT("الوزن", "⚖️"),
    TEMPERATURE("الحرارة", "🌡️"),
    AREA("المساحة", "📐"),
    VOLUME("الحجم", "🧪"),
    SPEED("السرعة", "🚀"),
    TIME("الزمن", "⏱️"),
    DATA("البيانات", "💾")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var inputValueStr by remember { mutableStateOf("1") }

    val unitsForCategory = remember(selectedCategory) {
        when (selectedCategory) {
            UnitCategory.LENGTH -> listOf(
                UnitItem("m", "متر (m)", 1.0),
                UnitItem("km", "كيلومتر (km)", 1000.0),
                UnitItem("cm", "سنتيمتر (cm)", 0.01),
                UnitItem("mm", "مليمتر (mm)", 0.001),
                UnitItem("mile", "ميل (mi)", 1609.344),
                UnitItem("yard", "ياردة (yd)", 0.9144),
                UnitItem("foot", "قدم (ft)", 0.3048),
                UnitItem("inch", "بوصة (in)", 0.0254)
            )
            UnitCategory.WEIGHT -> listOf(
                UnitItem("kg", "كيلوغرام (kg)", 1.0),
                UnitItem("g", "غرام (g)", 0.001),
                UnitItem("mg", "مليغرام (mg)", 0.000001),
                UnitItem("ton", "طن متري (t)", 1000.0),
                UnitItem("lb", "رطل / باوند (lb)", 0.45359237),
                UnitItem("oz", "أونصة (oz)", 0.0283495)
            )
            UnitCategory.TEMPERATURE -> listOf(
                UnitItem("C", "درجة مئوية (°C)", 1.0),
                UnitItem("F", "فهرنهايت (°F)", 1.0),
                UnitItem("K", "كلفن (K)", 1.0)
            )
            UnitCategory.AREA -> listOf(
                UnitItem("m2", "متر مربع (m²)", 1.0),
                UnitItem("km2", "كيلومتر مربع (km²)", 1000000.0),
                UnitItem("hectare", "هكتار", 10000.0),
                UnitItem("acre", "فدان / أكر", 4046.86),
                UnitItem("sqft", "قدم مربع (sq ft)", 0.092903)
            )
            UnitCategory.VOLUME -> listOf(
                UnitItem("L", "لتر (L)", 1.0),
                UnitItem("mL", "مليلتر (mL)", 0.001),
                UnitItem("m3", "متر مكعب (m³)", 1000.0),
                UnitItem("gal", "غالون أمريكي (gal)", 3.78541),
                UnitItem("cup", "كوب (cup)", 0.24)
            )
            UnitCategory.SPEED -> listOf(
                UnitItem("kmh", "كم / ساعة (km/h)", 1.0),
                UnitItem("ms", "متر / ثانية (m/s)", 3.6),
                UnitItem("mph", "ميل / ساعة (mph)", 1.60934),
                UnitItem("knot", "عقدة بحرية (knot)", 1.852)
            )
            UnitCategory.TIME -> listOf(
                UnitItem("sec", "ثانية (s)", 1.0),
                UnitItem("min", "دقيقة (min)", 60.0),
                UnitItem("hr", "ساعة (hr)", 3600.0),
                UnitItem("day", "يوم", 86400.0),
                UnitItem("week", "أسبوع", 604800.0),
                UnitItem("month", "شهر (30 يوم)", 2592000.0),
                UnitItem("year", "سنة (365 يوم)", 31536000.0),
                UnitItem("ms", "ملي ثانية (ms)", 0.001)
            )
            UnitCategory.DATA -> listOf(
                UnitItem("B", "بايت (Byte)", 1.0),
                UnitItem("KB", "كيلوبايت (KB)", 1024.0),
                UnitItem("MB", "ميغابايت (MB)", 1048576.0),
                UnitItem("GB", "غيغابايت (GB)", 1073741824.0),
                UnitItem("TB", "تيرابايت (TB)", 1099511627776.0),
                UnitItem("bit", "بت (Bit)", 0.125)
            )
        }
    }

    var fromUnit by remember(selectedCategory) { mutableStateOf(unitsForCategory[0]) }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(if (unitsForCategory.size > 1) unitsForCategory[1] else unitsForCategory[0])
    }

    // Convert function
    val convertedResult: String = remember(inputValueStr, fromUnit, toUnit, selectedCategory, settings.numberFormat) {
        val inputDouble = inputValueStr.toDoubleOrNull() ?: 0.0
        val outDouble: Double = if (selectedCategory == UnitCategory.TEMPERATURE) {
            when {
                fromUnit.id == toUnit.id -> inputDouble
                fromUnit.id == "C" && toUnit.id == "F" -> (inputDouble * 9.0 / 5.0) + 32.0
                fromUnit.id == "C" && toUnit.id == "K" -> inputDouble + 273.15
                fromUnit.id == "F" && toUnit.id == "C" -> (inputDouble - 32.0) * 5.0 / 9.0
                fromUnit.id == "F" && toUnit.id == "K" -> (inputDouble - 32.0) * 5.0 / 9.0 + 273.15
                fromUnit.id == "K" && toUnit.id == "C" -> inputDouble - 273.15
                fromUnit.id == "K" && toUnit.id == "F" -> (inputDouble - 273.15) * 9.0 / 5.0 + 32.0
                else -> inputDouble
            }
        } else {
            val baseVal = inputDouble * fromUnit.factorToBase
            baseVal / toUnit.factorToBase
        }

        NumberFormatter.formatNumber(
            value = outDouble,
            style = settings.numberFormat,
            useGrouping = settings.useGroupingSeparator,
            maxDecimals = settings.decimalPrecision
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Categories Horizontal Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.values().forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                    },
                    label = { Text("${cat.iconText} ${cat.titleAr}") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Input Card (From)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "من (القيمة والوحدة الأصلية)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputValueStr,
                    onValueChange = {
                        val western = NumberFormatter.toWesternDigits(it)
                        if (western.isEmpty() || western.matches(Regex("""^[-+]?\d*\.?\d*$"""))) {
                            inputValueStr = western
                        }
                    },
                    label = { Text("أدخل القيمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                var fromExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = fromUnit.nameAr,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        unitsForCategory.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.nameAr) },
                                onClick = {
                                    fromUnit = unit
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Swap Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledIconButton(
                onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                },
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "تبديل الوحدات")
            }
        }

        // Output Card (To)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "إلى (النتيجة المحولة)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = convertedResult,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                var toExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = toUnit.nameAr,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        unitsForCategory.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.nameAr) },
                                onClick = {
                                    toUnit = unit
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
