package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.engine.NumberFormatter
import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class OtherSubTab(val title: String) {
    AGE("العمر والتواريخ"),
    BMI("مؤشر كتلة الجسم BMI"),
    GPA("معدل الدرجات GPA")
}

@Composable
fun OtherCalculatorsScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableStateOf(OtherSubTab.AGE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = currentSubTab.ordinal,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            OtherSubTab.values().forEach { tab ->
                Tab(
                    selected = currentSubTab == tab,
                    onClick = { currentSubTab = tab },
                    text = { Text(tab.title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (currentSubTab) {
                OtherSubTab.AGE -> AgeCalculatorView(settings)
                OtherSubTab.BMI -> BmiCalculatorView(settings)
                OtherSubTab.GPA -> GpaCalculatorView(settings)
            }
        }
    }
}

// 1. AGE & DATE CALCULATOR
@Composable
private fun AgeCalculatorView(settings: AppSettings) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var birthYearStr by remember { mutableStateOf((currentYear - 25).toString()) }
    var birthMonthStr by remember { mutableStateOf("1") }
    var birthDayStr by remember { mutableStateOf("15") }

    val bYear = birthYearStr.toIntOrNull() ?: 2000
    val bMonth = (birthMonthStr.toIntOrNull() ?: 1).coerceIn(1, 12)
    val bDay = (birthDayStr.toIntOrNull() ?: 1).coerceIn(1, 31)

    // Calculate age
    val today = Calendar.getInstance()
    val birthCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, bYear)
        set(Calendar.MONTH, bMonth - 1)
        set(Calendar.DAY_OF_MONTH, bDay)
    }

    var ageYears = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
    var ageMonths = today.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
    var ageDays = today.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

    if (ageDays < 0) {
        ageMonths--
        val prevMonth = (today.get(Calendar.MONTH) - 1 + 12) % 12
        val tempCal = Calendar.getInstance().apply { set(Calendar.MONTH, prevMonth) }
        ageDays += tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (ageMonths < 0) {
        ageYears--
        ageMonths += 12
    }

    val diffMillis = (today.timeInMillis - birthCal.timeInMillis).coerceAtLeast(0)
    val totalDays = diffMillis / (1000 * 60 * 60 * 24)

    // Next Birthday calculation
    val nextBirthdayCal = Calendar.getInstance().apply {
        set(Calendar.MONTH, bMonth - 1)
        set(Calendar.DAY_OF_MONTH, bDay)
    }
    if (nextBirthdayCal.before(today)) {
        nextBirthdayCal.add(Calendar.YEAR, 1)
    }
    val daysUntilNextBday = ((nextBirthdayCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("تاريخ الميلاد", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = birthDayStr,
                        onValueChange = { birthDayStr = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("اليوم") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthMonthStr,
                        onValueChange = { birthMonthStr = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("الشهر") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = birthYearStr,
                        onValueChange = { birthYearStr = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("السنة") },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("العمر الدقيق", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)

                Text(
                    text = "${ageYears} سنة و ${ageMonths} شهر و ${ageDays} يوم",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الأيام المتبقية لعيد الميلاد القادم:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$daysUntilNextBday يوم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("إجمالي الأيام المعاشة:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalDays يوم", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("إجمالي الساعات التقريبية:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${totalDays * 24} ساعة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 2. BMI CALCULATOR
@Composable
private fun BmiCalculatorView(settings: AppSettings) {
    var heightCmStr by remember { mutableStateOf("175") }
    var weightKgStr by remember { mutableStateOf("70") }

    val heightCm = heightCmStr.toDoubleOrNull() ?: 170.0
    val weightKg = weightKgStr.toDoubleOrNull() ?: 70.0

    val heightM = heightCm / 100.0
    val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0.0

    val (categoryText, categoryColor) = when {
        bmi < 18.5 -> Pair("نقص في الوزن (نحافة)", Color(0xFFFBBF24))
        bmi in 18.5..24.9 -> Pair("وزن طبيعي وصحي ومثالي", Color(0xFF10B981))
        bmi in 25.0..29.9 -> Pair("زيادة في الوزن", Color(0xFFF97316))
        else -> Pair("سمنة مفرطة", Color(0xFFEF4444))
    }

    val minIdealWeight = 18.5 * (heightM * heightM)
    val maxIdealWeight = 24.9 * (heightM * heightM)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = heightCmStr,
                    onValueChange = {
                        val western = NumberFormatter.toWesternDigits(it)
                        if (western.isEmpty() || western.matches(Regex("""^\d*\.?\d*$"""))) heightCmStr = western
                    },
                    label = { Text("الطول (سنتيمتر cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightKgStr,
                    onValueChange = {
                        val western = NumberFormatter.toWesternDigits(it)
                        if (western.isEmpty() || western.matches(Regex("""^\d*\.?\d*$"""))) weightKgStr = western
                    },
                    label = { Text("الوزن (كيلوغرام kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("مؤشر كتلة الجسم (BMI)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text(
                    text = "%.1f".format(bmi),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = categoryText,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الوزن الصحي المثالي لطولك:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.1f".format(minIdealWeight)} - ${"%.1f".format(maxIdealWeight)} كجم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3. GPA CALCULATOR
private data class CourseGradeItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var score: String,
    var hours: String
)

@Composable
private fun GpaCalculatorView(settings: AppSettings) {
    var courses by remember {
        mutableStateOf(
            listOf(
                CourseGradeItem(name = "الرياضيات", score = "92", hours = "3"),
                CourseGradeItem(name = "الفيزياء", score = "85", hours = "3"),
                CourseGradeItem(name = "البرمجة", score = "96", hours = "4"),
                CourseGradeItem(name = "اللغة الإنجليزية", score = "88", hours = "2")
            )
        )
    }

    var totalPoints = 0.0
    var totalHours = 0.0

    courses.forEach { c ->
        val score = c.score.toDoubleOrNull() ?: 0.0
        val hours = c.hours.toDoubleOrNull() ?: 0.0
        totalPoints += (score * hours)
        totalHours += hours
    }

    val percentageAverage = if (totalHours > 0) totalPoints / totalHours else 0.0
    val gpa4 = (percentageAverage / 100.0) * 4.0
    val gpa5 = (percentageAverage / 100.0) * 5.0

    val rating = when {
        percentageAverage >= 90 -> "ممتاز مرتفع (A+)"
        percentageAverage >= 85 -> "ممتاز (A)"
        percentageAverage >= 80 -> "جيد جداً مرتفع (B+)"
        percentageAverage >= 75 -> "جيد جداً (B)"
        percentageAverage >= 70 -> "جيد مرتفع (C+)"
        percentageAverage >= 60 -> "مقبول (D)"
        else -> "راسب (F)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Results Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("المعدل التراكمي", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${"%.2f".format(percentageAverage)}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(rating, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("معدل من 4: ${"%.2f".format(gpa4)}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("معدل من 5: ${"%.2f".format(gpa5)}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("إجمالي الساعات: ${totalHours.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        // Add Course Row
        Button(
            onClick = {
                courses = courses + CourseGradeItem(name = "مادة جديدة", score = "80", hours = "3")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("إضافة مادة جديدة")
        }

        // Course List
        courses.forEachIndexed { index, course ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = course.name,
                        onValueChange = { newName ->
                            courses = courses.toMutableList().also { it[index] = course.copy(name = newName) }
                        },
                        label = { Text("المادة") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = course.score,
                        onValueChange = { newScore ->
                            courses = courses.toMutableList().also { it[index] = course.copy(score = newScore) }
                        },
                        label = { Text("الدرجة (100)") },
                        modifier = Modifier.weight(1.1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = course.hours,
                        onValueChange = { newHours ->
                            courses = courses.toMutableList().also { it[index] = course.copy(hours = newHours) }
                        },
                        label = { Text("الساعات") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true
                    )
                    if (courses.size > 1) {
                        IconButton(
                            onClick = {
                                courses = courses.toMutableList().also { it.removeAt(index) }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
