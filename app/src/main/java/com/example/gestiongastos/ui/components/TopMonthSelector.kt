package com.example.gestiongastos.ui.components

import androidx.compose.material.icons.filled.Language
import com.example.gestiongastos.i18n.I18n
import com.example.gestiongastos.i18n.AppLanguage
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import java.util.Date
import java.text.SimpleDateFormat
import com.example.gestiongastos.ui.theme.FinanceColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.ui.theme.AppColorTheme
import com.example.gestiongastos.ui.theme.AppThemeMode
import com.example.gestiongastos.ui.theme.isAppDark
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.ExpenseSortOrder
import androidx.compose.material.icons.filled.Sort

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

@Composable
fun SettingsDialog(
    currentThemeMode: AppThemeMode,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    currentLanguage: AppLanguage = AppLanguage.ES,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    currentColorTheme: AppColorTheme,
    onColorThemeSelected: (AppColorTheme) -> Unit,
    isBiometricEnabled: Boolean = false,
    onToggleBiometric: (Boolean) -> Unit = {},
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
    onDismiss: () -> Unit
) {
    val isDarkTheme = isAppDark()
    var selectedSettingsTab by remember { mutableIntStateOf(0) } // 0 = Apariencia, 1 = Preferencias, 2 = Datos

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Ajustes y Configuración", "Settings"),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Modern 3-Tab Segmented Selector
                val tabs = listOf(tr("Apariencia", "Appearance"), tr("Preferencias", "Preferences"), tr("Datos", "Data"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedSettingsTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .clickable { selectedSettingsTab = index },
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.5f else 0.4f),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedSettingsTab) {
                    0 -> {
                        // TAB 1: APARIENCIA
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Idioma de la app
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tr("Idioma", "Language"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AppLanguage.entries.forEach { language ->
                                        val isSelected = language == currentLanguage
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { onLanguageSelected(language) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.5f else 0.4f),
                                            border = BorderStroke(
                                                if (isSelected) 2.dp else 1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = language.label,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Section 1: Color Palette Selection
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tr("Color de la Aplicación", "App color"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tr("Selecciona un color para personalizar la interfaz:", "Choose a color to personalize the app:"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val colorOptionsList = remember { AppColorTheme.values().toList() }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    colorOptionsList.chunked(2).forEach { rowColors ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (theme in rowColors) {
                                                val isSelected = currentColorTheme == theme
                                                val previewColor = if (isDarkTheme) theme.primaryDark else theme.primaryLight

                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable { onColorThemeSelected(theme) },
                                                    color = if (isSelected) previewColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDarkTheme) 0.5f else 0.4f),
                                                    border = if (isSelected) BorderStroke(2.dp, previewColor) else null,
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .clip(CircleShape)
                                                                .background(previewColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = theme.displayName,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) previewColor else MaterialTheme.colorScheme.onSurface
                                                            ),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                            if (rowColors.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Section 2: App Theme Mode (Light / Dark / System)
                            Column {
                                Text(
                                    text = tr("Modo de Pantalla", "Display mode"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onThemeModeSelected(AppThemeMode.LIGHT) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentThemeMode == AppThemeMode.LIGHT,
                                        onClick = { onThemeModeSelected(AppThemeMode.LIGHT) }
                                    )
                                    Text(tr("Modo Claro", "Light mode"), modifier = Modifier.padding(start = 4.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onThemeModeSelected(AppThemeMode.DARK) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentThemeMode == AppThemeMode.DARK,
                                        onClick = { onThemeModeSelected(AppThemeMode.DARK) }
                                    )
                                    Text(tr("Modo Oscuro", "Dark mode"), modifier = Modifier.padding(start = 4.dp))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onThemeModeSelected(AppThemeMode.SYSTEM) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentThemeMode == AppThemeMode.SYSTEM,
                                        onClick = { onThemeModeSelected(AppThemeMode.SYSTEM) }
                                    )
                                    Text(tr("Seguir al Sistema", "Follow system"), modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 2: PREFERENCIAS
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Section 3: Expense and Installment Ordering
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tr("Organización de Gastos y Cuotas", "Expense and installment order"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tr("Configura cómo se ordenan las compras del mes y las cuotas diferidas.", "Choose how this month's purchases and installments are sorted."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = tr("Cuotas de meses anteriores (diferidas / recurrentes):", "Installments from previous months (deferred / recurring):"),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onInstallmentsPositionSelected(InstallmentPosition.LAST) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentInstallmentsPosition == InstallmentPosition.LAST,
                                        onClick = { onInstallmentsPositionSelected(InstallmentPosition.LAST) }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(tr("Al final (De últimas)", "At the end (last)"), fontWeight = FontWeight.SemiBold)
                                        Text(tr("Compras del mes arriba y cuotas pasadas al final", "This month's purchases on top, older installments at the end"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onInstallmentsPositionSelected(InstallmentPosition.FIRST) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentInstallmentsPosition == InstallmentPosition.FIRST,
                                        onClick = { onInstallmentsPositionSelected(InstallmentPosition.FIRST) }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(tr("Al inicio (De primeras)", "At the start (first)"), fontWeight = FontWeight.SemiBold)
                                        Text(tr("Cuotas pasadas arriba y compras del mes abajo", "Older installments on top, this month's purchases below"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = tr("Orden cronológico de compras del mes:", "Order of this month's purchases:"),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onExpenseSortOrderSelected(ExpenseSortOrder.NEWEST_FIRST) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentExpenseSortOrder == ExpenseSortOrder.NEWEST_FIRST,
                                        onClick = { onExpenseSortOrderSelected(ExpenseSortOrder.NEWEST_FIRST) }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(tr("Más recientes primero (Nuevos arriba)", "Newest first"), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onExpenseSortOrderSelected(ExpenseSortOrder.OLDEST_FIRST) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = currentExpenseSortOrder == ExpenseSortOrder.OLDEST_FIRST,
                                        onClick = { onExpenseSortOrderSelected(ExpenseSortOrder.OLDEST_FIRST) }
                                    )
                                    Column(modifier = Modifier.padding(start = 4.dp)) {
                                        Text(tr("En orden de registro (Antiguos primero)", "In order added (oldest first)"), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onToggleMovePaidToTop(!isMovePaidToTop) }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(tr("Ubicar pagados e ingresados arriba", "Move paid and received to the top"), fontWeight = FontWeight.SemiBold)
                                        Text(
                                            tr("Al marcar un gasto fijo como pagado o un ingreso como recibido, se ubicará en la parte superior.", "When you mark an expense as paid or income as received, it moves to the top."),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isMovePaidToTop,
                                        onCheckedChange = { onToggleMovePaidToTop(it) }
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Section 4: Biometric Lock
                            Column {
                                Text(
                                    text = tr("Seguridad y Acceso", "Security and access"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tr("Protege tu información financiera requiriendo huella digital al ingresar.", "Protect your financial info by requiring your fingerprint to open the app."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onToggleBiometric(!isBiometricEnabled) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = tr("Bloqueo con Huella Digital", "Fingerprint lock"),
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = if (isBiometricEnabled) tr("Activado (Requiere huella al abrir)", "On (fingerprint required to open)") else tr("Desactivado", "Off"),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isBiometricEnabled) FinanceColors.incomeText(isDarkTheme) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isBiometricEnabled,
                                        onCheckedChange = onToggleBiometric
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: DATOS Y REPORTES
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("Copias de Seguridad y Reportes", "Backups and reports"),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Text(
                                text = tr("Exporta tus reportes para Excel o gestiona copias de respaldo completas de tu información.", "Export reports for Excel or manage full backups of your data."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = onExportCsv,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr("Exportar Reporte Mensual (CSV)", "Export monthly report (CSV)"))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            OutlinedButton(
                                onClick = onExportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr("Exportar Copia de Seguridad (.json)", "Export backup (.json)"))
                            }

                            OutlinedButton(
                                onClick = onRestoreBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr("Restaurar desde Archivo (.json)", "Restore from file (.json)"))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Button(
                                onClick = onShareAppApk,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tr("Compartir APK", "Share APK"))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            // Versión de la app (versionName de app/build.gradle.kts)
            val context = LocalContext.current
            val versionName = remember {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    null
                } ?: "?"
            }
            Text(
                text = tr("🍊 CapyCoin · Versión $versionName", "🍊 CapyCoin · Version $versionName"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    )
}

@Composable
fun MonthYearPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(initialYearMonth.monthValue) }
    var selectedYear by remember { mutableIntStateOf(initialYearMonth.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = tr("Seleccionar Mes y Año", "Select month and year"), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = tr("Año:", "Year:"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedYear-- }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = tr("Año anterior", "Previous year"))
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { selectedYear++ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = tr("Año siguiente", "Next year"))
                        }
                    }
                }

                HorizontalDivider()

                // Month Grid (3 columns x 4 rows)
                Text(text = tr("Mes:", "Month:"), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                val months = listOf(
                    tr("Ene", "Jan"), tr("Feb", "Feb"), tr("Mar", "Mar"), tr("Abr", "Apr"),
                    tr("May", "May"), tr("Jun", "Jun"), tr("Jul", "Jul"), tr("Ago", "Aug"),
                    tr("Sep", "Sep"), tr("Oct", "Oct"), tr("Nov", "Nov"), tr("Dic", "Dec")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    months.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMonths.forEachIndexed { colIndex, name ->
                                val monthValue = rowIndex * 3 + colIndex + 1
                                val isSelected = monthValue == selectedMonth

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        )
                                        .clickable { selectedMonth = monthValue },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(YearMonth.of(selectedYear, selectedMonth))
                }
            ) {
                Text(tr("Aceptar", "OK"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}
