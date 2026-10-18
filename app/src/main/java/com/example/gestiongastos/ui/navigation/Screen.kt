package com.example.gestiongastos.ui.navigation

import com.example.gestiongastos.i18n.tr
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.rounded.Savings

sealed class Screen(val route: String, private val titleEs: String, private val titleEn: String, val icon: ImageVector) {
    /** Título en el idioma actual (se calcula al mostrarlo para seguir los cambios de idioma). */
    val title: String get() = tr(titleEs, titleEn)

    object Resumen : Screen("resumen", "Resumen", "Summary", Icons.Rounded.Dashboard)
    object Ingresos : Screen("ingresos", "Ingresos", "Income", Icons.AutoMirrored.Rounded.TrendingUp)
    object Gastos : Screen("gastos", "Gastos", "Expenses", Icons.AutoMirrored.Rounded.TrendingDown)
    object Ahorros : Screen("ahorros", "Ahorros", "Savings", Icons.Rounded.Savings)
    object Tarjetas : Screen("tarjetas", "Tarjetas", "Cards", Icons.Rounded.CreditCard)
}

val defaultBottomNavScreens = listOf(
    Screen.Resumen,
    Screen.Ingresos,
    Screen.Gastos,
    Screen.Ahorros,
    Screen.Tarjetas
)
