package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.privacyBlur
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.AddEditSavingsDialog
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.ui.components.SavingsItemCard
import com.example.gestiongastos.data.model.ItemKeys
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@Composable
fun SavingsScreen(
    savings: List<SavingsEntity>,
    selectedYearMonth: YearMonth,
    isPrivacyMode: Boolean = false,
    onAddSavings: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) -> Unit,
    onUpdateSavings: (SavingsEntity) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit,
    onEndSavingFromMonth: (SavingsEntity) -> Unit = {},
    ignoredKeys: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var savingToEdit by remember { mutableStateOf<SavingsEntity?>(null) }
    var savingToDelete by remember { mutableStateOf<SavingsEntity?>(null) }

    val isDark = isAppDark()
    // Mismo criterio que el Resumen: los ahorros ignorados este mes no suman
    val totalSavingsMonth = remember(savings, ignoredKeys) {
        savings.filter { ItemKeys.saving(it.id) !in ignoredKeys }.sumOf { it.amount }
    }

    Scaffold(
        modifier = modifier,
        // El Scaffold principal ya aplica las barras del sistema; sin esto quedaba un espacio en blanco arriba
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FinanceColors.savingsAccent(isDark),
                contentColor = FinanceColors.onAccent(isDark)
            ) {
                Icon(Icons.Default.Add, contentDescription = tr("Agregar Ahorro", "Add savings"))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Card with Capy Savings Mascot
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .capyPattern(FinanceColors.savingsAccent(isDark).copy(alpha = 0.10f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.capy_savings),
                                contentDescription = tr("Capibara Ahorros", "Savings capybara"),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tr("Ahorros de ${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }}", "${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }} savings"),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.savingsText(isDark)
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = tr("${savings.size} meta(s) de ahorro", "${savings.size} savings goal(s)"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FinanceColors.savingsAccent(isDark),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AutoResizedText(
                        text = privateText(formatCurrency(rememberAnimatedAmount(totalSavingsMonth)), isPrivacyMode, "$ ••••••"),
                        isPrivate = isPrivacyMode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FinanceColors.savingsText(isDark)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (savings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.capy_savings),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("¡Comienza tu meta de ahorro!", "Start your savings goal!"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr("Un capibara previsor vale por dos 🍊🪙", "A capybara who plans ahead is worth two 🍊🪙"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(savings, key = { it.id }) { saving ->
                        SavingsItemCard(
                            saving = saving,
                            isPrivacyMode = isPrivacyMode,
                            onEdit = { savingToEdit = saving },
                            onDelete = { savingToDelete = saving }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditSavingsDialog(
            savingToEdit = null,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { showAddDialog = false },
            onSave = { saving ->
                onAddSavings(saving.concept, saving.amount, saving.date, saving.isRecurring)
                showAddDialog = false
            }
        )
    }

    if (savingToEdit != null) {
        AddEditSavingsDialog(
            savingToEdit = savingToEdit,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { savingToEdit = null },
            onSave = { updated ->
                onUpdateSavings(updated)
                savingToEdit = null
            }
        )
    }

    savingToDelete?.let { saving ->
        ConfirmDeleteDialog(
            itemName = saving.concept,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { savingToDelete = null },
            onDeleteAll = { onDeleteSavings(saving) },
            onEndFromMonth = if (saving.isRecurring) ({ onEndSavingFromMonth(saving) }) else null
        )
    }
}
