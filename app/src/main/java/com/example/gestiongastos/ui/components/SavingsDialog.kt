package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.ItemKeys
import java.time.YearMonth

@Composable
fun SavingsManagerDialog(
    savingsList: List<SavingsEntity>,
    selectedYearMonth: YearMonth,
    ignoredKeys: Set<String> = emptySet(),
    isPrivacyMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddSavings: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) -> Unit,
    onUpdateSavings: (SavingsEntity) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit,
    onEndSavingFromMonth: (SavingsEntity) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var savingToEdit by remember { mutableStateOf<SavingsEntity?>(null) }
    var savingToDelete by remember { mutableStateOf<SavingsEntity?>(null) }
    val isDark = isAppDark()

    // Mismo criterio que el Resumen: los ahorros ignorados este mes no suman
    val totalSavingsMonth = remember(savingsList, ignoredKeys) {
        savingsList.filter { ItemKeys.saving(it.id) !in ignoredKeys }.sumOf { it.amount }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.capy_savings),
                        contentDescription = tr("Capibara Ahorros", "Savings capybara"),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Gestión de Ahorros", "Savings"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = tr("Agregar Ahorro", "Add savings"), tint = FinanceColors.savingsAccent(isDark))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header card for savings total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceColors.savingsContainer(isDark))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("Ahorros del Mes", "Savings this month"),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = FinanceColors.savingsText(isDark),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = tr("${savingsList.size} registro(s)", "${savingsList.size} item(s)"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.savingsAccent(isDark))
                            )
                        }
                        Text(
                            text = privateText(formatCurrency(rememberAnimatedAmount(totalSavingsMonth)), isPrivacyMode, "$ ••••••"),
                            modifier = Modifier.privacyBlur(isPrivacyMode),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.savingsText(isDark)
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (savingsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.capy_savings),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = tr("No tienes ahorros registrados en este mes.", "You don't have any savings this month."),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savingsList, key = { it.id }) { saving ->
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cerrar", "Close"))
            }
        }
    )

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
