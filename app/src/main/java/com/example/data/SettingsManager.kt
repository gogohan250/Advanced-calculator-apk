package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    DARK_ORANGE,    // الداكن العصري (#121417 + #FF8A3D)
    LIGHT,          // النهاري الفاتح
    AMOLED_BLACK,   // الأسود الفاحم
    MIDNIGHT_BLUE,  // الأزرق الليلي
    EMERALD         // الزمردي الأنيق
}

enum class NumberFormatStyle {
    WESTERN, // 0123456789
    ARABIC   // ٠١٢٣٤٥٦٧٨٩
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK_ORANGE,
    val numberFormat: NumberFormatStyle = NumberFormatStyle.ARABIC,
    val useGroupingSeparator: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val decimalPrecision: Int = 8,
    val angleInDegrees: Boolean = true // DEG vs RAD
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alhasiba_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeName = prefs.getString("theme_mode", AppThemeMode.DARK_ORANGE.name) ?: AppThemeMode.DARK_ORANGE.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeName) }.getOrDefault(AppThemeMode.DARK_ORANGE)

        val numStyleName = prefs.getString("number_format", NumberFormatStyle.ARABIC.name) ?: NumberFormatStyle.ARABIC.name
        val numberFormat = runCatching { NumberFormatStyle.valueOf(numStyleName) }.getOrDefault(NumberFormatStyle.ARABIC)

        return AppSettings(
            themeMode = themeMode,
            numberFormat = numberFormat,
            useGroupingSeparator = prefs.getBoolean("use_grouping", true),
            hapticFeedbackEnabled = prefs.getBoolean("haptic_feedback", true),
            soundEnabled = prefs.getBoolean("sound_enabled", false),
            decimalPrecision = prefs.getInt("decimal_precision", 8),
            angleInDegrees = prefs.getBoolean("angle_degrees", true)
        )
    }

    fun updateTheme(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun updateNumberFormat(format: NumberFormatStyle) {
        prefs.edit().putString("number_format", format.name).apply()
        _settings.value = _settings.value.copy(numberFormat = format)
    }

    fun toggleGrouping(enabled: Boolean) {
        prefs.edit().putBoolean("use_grouping", enabled).apply()
        _settings.value = _settings.value.copy(useGroupingSeparator = enabled)
    }

    fun toggleHaptic(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled)
    }

    fun toggleSound(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun toggleAngleDegrees(degrees: Boolean) {
        prefs.edit().putBoolean("angle_degrees", degrees).apply()
        _settings.value = _settings.value.copy(angleInDegrees = degrees)
    }

    fun updateDecimalPrecision(precision: Int) {
        val safePrec = precision.coerceIn(2, 12)
        prefs.edit().putInt("decimal_precision", safePrec).apply()
        _settings.value = _settings.value.copy(decimalPrecision = safePrec)
    }
}
