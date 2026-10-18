package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.AppThemeMode

data class CalculatorKeyColors(
    val numKeyBg: Color,
    val fnKeyBg: Color,
    val opKeyBg: Color,
    val opText: Color,
    val numText: Color,
    val fnText: Color,
    val equalsBg: Color,
    val equalsText: Color
)

val LocalCalculatorKeyColors = staticCompositionLocalOf {
    CalculatorKeyColors(
        numKeyBg = DarkNumKeyBackground,
        fnKeyBg = DarkFnKeyBackground,
        opKeyBg = OrangePrimary,
        opText = Color.White,
        numText = DarkTextPrimary,
        fnText = DarkTextPrimary,
        equalsBg = OrangePrimary,
        equalsText = Color.White
    )
}

private val DarkThemeColors = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangePrimaryVariant,
    onPrimaryContainer = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary
)

private val LightThemeColors = lightColorScheme(
    primary = LightOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF321300),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary
)

private val AmoledThemeColors = darkColorScheme(
    primary = AmoledOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD47A00),
    onPrimaryContainer = Color.White,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFAAAAAA)
)

private val MidnightThemeColors = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF0284C7),
    onPrimaryContainer = Color.White,
    background = MidnightBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = MidnightSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val EmeraldThemeColors = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF06231B),
    primaryContainer = Color(0xFF059669),
    onPrimaryContainer = Color.White,
    background = EmeraldBackground,
    onBackground = Color(0xFFECFDF5),
    surface = EmeraldSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = Color(0xFF6EE7B7)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK_ORANGE,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme
    val keyColors: CalculatorKeyColors

    when (themeMode) {
        AppThemeMode.DARK_ORANGE -> {
            colorScheme = DarkThemeColors
            keyColors = CalculatorKeyColors(
                numKeyBg = DarkNumKeyBackground,
                fnKeyBg = DarkFnKeyBackground,
                opKeyBg = OrangePrimary,
                opText = Color.White,
                numText = DarkTextPrimary,
                fnText = Color(0xFFFFB280),
                equalsBg = OrangePrimary,
                equalsText = Color.White
            )
        }
        AppThemeMode.LIGHT -> {
            colorScheme = LightThemeColors
            keyColors = CalculatorKeyColors(
                numKeyBg = LightNumKeyBackground,
                fnKeyBg = LightFnKeyBackground,
                opKeyBg = LightOrangePrimary,
                opText = Color.White,
                numText = LightTextPrimary,
                fnText = Color(0xFF0284C7),
                equalsBg = LightOrangePrimary,
                equalsText = Color.White
            )
        }
        AppThemeMode.AMOLED_BLACK -> {
            colorScheme = AmoledThemeColors
            keyColors = CalculatorKeyColors(
                numKeyBg = AmoledNumKeyBackground,
                fnKeyBg = AmoledFnKeyBackground,
                opKeyBg = AmoledOrangePrimary,
                opText = Color.White,
                numText = Color.White,
                fnText = Color(0xFFFFB340),
                equalsBg = AmoledOrangePrimary,
                equalsText = Color.White
            )
        }
        AppThemeMode.MIDNIGHT_BLUE -> {
            colorScheme = MidnightThemeColors
            keyColors = CalculatorKeyColors(
                numKeyBg = MidnightNumKeyBackground,
                fnKeyBg = MidnightFnKeyBackground,
                opKeyBg = MidnightPrimary,
                opText = Color(0xFF0F172A),
                numText = Color(0xFFF8FAFC),
                fnText = Color(0xFF7DD3FC),
                equalsBg = MidnightPrimary,
                equalsText = Color(0xFF0F172A)
            )
        }
        AppThemeMode.EMERALD -> {
            colorScheme = EmeraldThemeColors
            keyColors = CalculatorKeyColors(
                numKeyBg = EmeraldNumKeyBackground,
                fnKeyBg = EmeraldFnKeyBackground,
                opKeyBg = EmeraldPrimary,
                opText = Color(0xFF06231B),
                numText = Color(0xFFECFDF5),
                fnText = Color(0xFF34D399),
                equalsBg = EmeraldPrimary,
                equalsText = Color(0xFF06231B)
            )
        }
    }

    CompositionLocalProvider(LocalCalculatorKeyColors provides keyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
