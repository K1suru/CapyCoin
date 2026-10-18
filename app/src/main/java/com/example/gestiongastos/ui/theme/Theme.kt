package com.example.gestiongastos.ui.theme

import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AppColorTheme(
    private val nameEs: String,
    private val nameEn: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color = Color(0xFF261905)
) {
    YELLOW("Capy Dorado", "Capy Gold", Color(0xFFD87D2A), Color(0xFFFFB74D), Color(0xFF261905)),
    PINK("Rosa Capy", "Capy Pink", Color(0xFFD81B60), Color(0xFFFF80AB), Color(0xFF380017));

    val displayName: String get() = tr(nameEs, nameEn)
}

/**
 * Paleta capibara: tonos tierra y cálidos para que toda la app tenga un solo estilo.
 * Ingresos = matcha, Gastos fijos = terracota, Ahorros = yuzu, Tarjetas = cacao, Libre = miel.
 */
object FinanceColors {
    // Ingresos (Matcha)
    fun incomeContainer(isDark: Boolean): Color = if (isDark) Color(0xFF1F2A17) else Color(0xFFEDF4E3)
    fun incomeText(isDark: Boolean): Color = if (isDark) Color(0xFFA9CF8A) else Color(0xFF3F6B26)
    fun incomeAccent(isDark: Boolean): Color = if (isDark) Color(0xFF8DBA67) else Color(0xFF5E8F3C)

    // Gastos (Terracota)
    fun expenseContainer(isDark: Boolean): Color = if (isDark) Color(0xFF3A1D16) else Color(0xFFFBE9E3)
    fun expenseText(isDark: Boolean): Color = if (isDark) Color(0xFFF0A08C) else Color(0xFF9A3B27)
    fun expenseAccent(isDark: Boolean): Color = if (isDark) Color(0xFFE07A5F) else Color(0xFFC8553D)

    // Ahorros (Yuzu)
    fun savingsContainer(isDark: Boolean): Color = if (isDark) Color(0xFF3A2A10) else Color(0xFFFFF0D9)
    fun savingsText(isDark: Boolean): Color = if (isDark) Color(0xFFFFC978) else Color(0xFF8F5300)
    fun savingsAccent(isDark: Boolean): Color = if (isDark) Color(0xFFF2A541) else Color(0xFFDC8A1E)

    // Tarjetas de crédito (Cacao)
    fun cardContainer(isDark: Boolean): Color = if (isDark) Color(0xFF2E2119) else Color(0xFFF3E8DF)
    fun cardText(isDark: Boolean): Color = if (isDark) Color(0xFFD9B79A) else Color(0xFF5C3A24)
    fun cardAccent(isDark: Boolean): Color = if (isDark) Color(0xFFB9875F) else Color(0xFF8B5A3C)

    // Saldo libre (Miel)
    fun goldContainer(isDark: Boolean): Color = if (isDark) Color(0xFF362610) else Color(0xFFFFF6E0)
    fun goldText(isDark: Boolean): Color = if (isDark) Color(0xFFFFD54F) else Color(0xFFB0661A)
    fun goldAccent(isDark: Boolean): Color = if (isDark) Color(0xFFFFE082) else Color(0xFFE8961E)

    // Superficies neutras
    fun cardSurface(isDark: Boolean): Color = if (isDark) Color(0xFF1E1C1A) else Color(0xFFF7F5F2)

    /** Texto/ícono sobre un botón relleno con un color *Accent (claro en modo oscuro). */
    fun onAccent(isDark: Boolean): Color = if (isDark) Color(0xFF241810) else Color.White
}

@Composable
fun isAppDark(): Boolean {
    return MaterialTheme.colorScheme.surface.luminance() < 0.5f
}

fun buildLightColorScheme(colorTheme: AppColorTheme): ColorScheme {
    val primary = colorTheme.primaryLight
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.15f),
        onPrimaryContainer = primary,
        secondary = primary,
        secondaryContainer = primary.copy(alpha = 0.12f),
        onSecondaryContainer = primary,
        background = Color(0xFFFAF8F5),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF2ECE6),
        onSurfaceVariant = Color(0xFF6B655E),
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        outline = Color(0xFFD7CCC8),
        outlineVariant = Color(0xFFE6DCD6)
    )
}

fun buildDarkColorScheme(colorTheme: AppColorTheme): ColorScheme {
    val primary = colorTheme.primaryDark
    return darkColorScheme(
        primary = primary,
        onPrimary = colorTheme.onPrimaryDark,
        primaryContainer = primary.copy(alpha = 0.22f),
        onPrimaryContainer = primary,
        secondary = primary,
        secondaryContainer = primary.copy(alpha = 0.20f),
        onSecondaryContainer = primary,
        surface = Color(0xFF181614),
        onSurface = Color(0xFFF2ECE6),
        surfaceVariant = Color(0xFF262320),
        onSurfaceVariant = Color(0xFFA69E96),
        background = Color(0xFF12100E),
        onBackground = Color(0xFFF2ECE6),
        outline = Color(0xFF4A443E),
        outlineVariant = Color(0xFF36322E)
    )
}

@Composable
fun GestionGastosTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorTheme: AppColorTheme = AppColorTheme.YELLOW,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) buildDarkColorScheme(colorTheme) else buildLightColorScheme(colorTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}