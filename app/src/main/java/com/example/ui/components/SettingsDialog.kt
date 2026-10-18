package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppSettings
import com.example.data.AppThemeMode
import com.example.data.NumberFormatStyle
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    currentSettings: AppSettings,
    onThemeChange: (AppThemeMode) -> Unit,
    onNumberFormatChange: (NumberFormatStyle) -> Unit,
    onGroupingChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onPrecisionChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإعدادات والتخصيص",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Theme Selection
            Text(
                text = "المظهر والألوان",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionCard(
                    title = "داكن برتقالي",
                    bgColor = DarkBackground,
                    accentColor = OrangePrimary,
                    isSelected = currentSettings.themeMode == AppThemeMode.DARK_ORANGE,
                    onClick = { onThemeChange(AppThemeMode.DARK_ORANGE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "فاتح ناصع",
                    bgColor = LightBackground,
                    accentColor = LightOrangePrimary,
                    isSelected = currentSettings.themeMode == AppThemeMode.LIGHT,
                    onClick = { onThemeChange(AppThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "أسود فاحم",
                    bgColor = AmoledBackground,
                    accentColor = AmoledOrangePrimary,
                    isSelected = currentSettings.themeMode == AppThemeMode.AMOLED_BLACK,
                    onClick = { onThemeChange(AppThemeMode.AMOLED_BLACK) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOptionCard(
                    title = "أزرق ليلي",
                    bgColor = MidnightBackground,
                    accentColor = MidnightPrimary,
                    isSelected = currentSettings.themeMode == AppThemeMode.MIDNIGHT_BLUE,
                    onClick = { onThemeChange(AppThemeMode.MIDNIGHT_BLUE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ThemeOptionCard(
                    title = "زمردي أنيق",
                    bgColor = EmeraldBackground,
                    accentColor = EmeraldPrimary,
                    isSelected = currentSettings.themeMode == AppThemeMode.EMERALD,
                    onClick = { onThemeChange(AppThemeMode.EMERALD) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) // empty placeholder for balanced 3-col grid
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

            // 2. Number Style
            Text(
                text = "شكل الأرقام",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = currentSettings.numberFormat == NumberFormatStyle.ARABIC,
                    onClick = { onNumberFormatChange(NumberFormatStyle.ARABIC) },
                    label = { Text("أرقام مشرقية (٠ ١ ٢ ٣ ٤)", fontWeight = FontWeight.Medium) },
                    leadingIcon = if (currentSettings.numberFormat == NumberFormatStyle.ARABIC) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = currentSettings.numberFormat == NumberFormatStyle.WESTERN,
                    onClick = { onNumberFormatChange(NumberFormatStyle.WESTERN) },
                    label = { Text("أرقام لاتينية (0 1 2 3 4)", fontWeight = FontWeight.Medium) },
                    leadingIcon = if (currentSettings.numberFormat == NumberFormatStyle.WESTERN) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

            // 3. Toggles: Grouping, Haptics, Sound
            Text(
                text = "التفاعل والعرض",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grouping Separator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("فاصل الآلاف (١،٢٣٤.٥٦)", fontWeight = FontWeight.Medium)
                    Text("فصل خانات الآلاف بفاصلة لسهولة القراءة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = currentSettings.useGroupingSeparator,
                    onCheckedChange = onGroupingChange
                )
            }

            // Haptic Feedback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("اهتزاز لمسي خفيف", fontWeight = FontWeight.Medium)
                    Text("اهتزاز لطيف عند النقر على الأزرار", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = currentSettings.hapticFeedbackEnabled,
                    onCheckedChange = onHapticChange
                )
            }

            // Sound feedback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("صوت النقر", fontWeight = FontWeight.Medium)
                    Text("إصدار نغمة نقر خفيفة عند الحساب", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = currentSettings.soundEnabled,
                    onCheckedChange = onSoundChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))

            // 4. Decimal Precision
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("أقصى عدد للكسور العشرية", fontWeight = FontWeight.Medium)
                    Text("الدقة: ${currentSettings.decimalPrecision} خانات", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { onPrecisionChange(currentSettings.decimalPrecision - 1) },
                        enabled = currentSettings.decimalPrecision > 2,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${currentSettings.decimalPrecision}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    FilledTonalButton(
                        onClick = { onPrecisionChange(currentSettings.decimalPrecision + 1) },
                        enabled = currentSettings.decimalPrecision < 12,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    bgColor: Color,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
