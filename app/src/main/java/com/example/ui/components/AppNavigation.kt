package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CalculatorMode(val titleAr: String, val icon: ImageVector) {
    STANDARD("الآلة الحاسبة", Icons.Default.Calculate),
    PROGRAMMER("البرمجية", Icons.Default.Terminal),
    GRAPHING("الرسم البياني", Icons.Default.ShowChart),
    UNITS("محوّل الوحدات", Icons.Default.Straighten),
    CURRENCY("محوّل العملات", Icons.Default.CurrencyExchange),
    FINANCIAL("حاسبات مالية", Icons.Default.AccountBalance),
    OTHER("أدوات أخرى", Icons.Default.Widgets)
}

@Composable
fun AppDrawerContent(
    currentMode: CalculatorMode,
    onSelectMode: (CalculatorMode) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.widthIn(max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "الحاسبة المتكاملة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "جميع العمليات والأدوات الحسابية",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            CalculatorMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                NavigationDrawerItem(
                    icon = { Icon(mode.icon, contentDescription = null) },
                    label = { Text(mode.titleAr, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    selected = isSelected,
                    onClick = { onSelectMode(mode) },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun AppBottomNavBar(
    currentMode: CalculatorMode,
    onSelectMode: (CalculatorMode) -> Unit,
    onOpenAllModes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomModes = listOf(
        CalculatorMode.STANDARD,
        CalculatorMode.UNITS,
        CalculatorMode.CURRENCY,
        CalculatorMode.FINANCIAL
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        bottomModes.forEach { mode ->
            val isSelected = currentMode == mode
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectMode(mode) },
                icon = { Icon(mode.icon, contentDescription = mode.titleAr) },
                label = { Text(mode.titleAr, fontSize = 11.sp, maxLines = 1) }
            )
        }

        // More modes button (opens drawer)
        NavigationBarItem(
            selected = currentMode !in bottomModes,
            onClick = onOpenAllModes,
            icon = { Icon(Icons.Default.Menu, contentDescription = "المزيد") },
            label = { Text("الأوضاع", fontSize = 11.sp) }
        )
    }
}
