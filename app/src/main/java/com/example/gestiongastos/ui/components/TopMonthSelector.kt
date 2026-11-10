package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.I18n
import com.example.gestiongastos.i18n.AppLanguage
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.gestiongastos.ui.theme.FinanceColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.ui.theme.AppColorTheme
import com.example.gestiongastos.ui.theme.AppThemeMode
import com.example.gestiongastos.ui.theme.isAppDark
import java.time.YearMonth
import java.time.format.TextStyle

import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.ExpenseSortOrder

@Composable
fun TopMonthSelector(
    selectedYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit = {},
    isPrivacyMode: Boolean = false,
    onTogglePrivacyMode: () -> Unit = {},
    isBiometricEnabled: Boolean = false,
    onToggleBiometric: (Boolean) -> Unit = {},
    currentThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    currentLanguage: AppLanguage = AppLanguage.ES,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    currentColorTheme: AppColorTheme = AppColorTheme.YELLOW,
    onColorThemeSelected: (AppColorTheme) -> Unit = {},
    currentInstallmentsPosition: InstallmentPosition = InstallmentPosition.LAST,
    onInstallmentsPositionSelected: (InstallmentPosition) -> Unit = {},
    currentExpenseSortOrder: ExpenseSortOrder = ExpenseSortOrder.NEWEST_FIRST,
    onExpenseSortOrderSelected: (ExpenseSortOrder) -> Unit = {},
    isMovePaidToTop: Boolean = true,
    onToggleMovePaidToTop: (Boolean) -> Unit = {},
    onExportBackup: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onShareAppApk: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMonthDialog by remember { mutableStateOf(false) }
    // Tras cambiar el idioma la app se redibuja; Ajustes se vuelve a abrir para no perder el sitio
    var showSettingsDialog by remember { mutableStateOf(I18n.reopenSettings.also { I18n.reopenSettings = false }) }
    val isDark = isAppDark()

    val monthName = selectedYearMonth.month
        .getDisplayName(TextStyle.FULL, appLocale())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(appLocale()) else it.toString() }
    val yearText = selectedYearMonth.year.toString()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left Action: Settings
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = tr("Ajustes y Configuración", "Settings"),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Action: Month / Year Selector (Mathematically centered)
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = tr("Mes anterior", "Previous month"),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showMonthDialog = true },
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$monthName $yearText",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.5.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = tr("Mes siguiente", "Next month"),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Right Actions: Privacy Eye + Refresh
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onTogglePrivacyMode,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPrivacyMode) tr("Mostrar montos", "Show amounts") else tr("Ocultar montos (Modo Privacidad)", "Hide amounts (privacy mode)"),
                        tint = if (isPrivacyMode) FinanceColors.goldAccent(isDark) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = tr("Actualizar datos", "Refresh data"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showMonthDialog) {
        MonthYearPickerDialog(
            initialYearMonth = selectedYearMonth,
            onDismiss = { showMonthDialog = false },
            onConfirm = { yearMonth ->
                onYearMonthSelected(yearMonth)
                showMonthDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = currentThemeMode,
            onThemeModeSelected = onThemeModeSelected,
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            currentColorTheme = currentColorTheme,
            onColorThemeSelected = onColorThemeSelected,
            isBiometricEnabled = isBiometricEnabled,
            onToggleBiometric = onToggleBiometric,
            currentInstallmentsPosition = currentInstallmentsPosition,
            onInstallmentsPositionSelected = onInstallmentsPositionSelected,
            currentExpenseSortOrder = currentExpenseSortOrder,
            onExpenseSortOrderSelected = onExpenseSortOrderSelected,
            isMovePaidToTop = isMovePaidToTop,
            onToggleMovePaidToTop = onToggleMovePaidToTop,
            onExportBackup = {
                showSettingsDialog = false
                onExportBackup()
            },
            onRestoreBackup = {
                showSettingsDialog = false
                onRestoreBackup()
            },
            onExportCsv = {
                showSettingsDialog = false
                onExportCsv()
            },
            onShareAppApk = {
                showSettingsDialog = false
                onShareAppApk()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
