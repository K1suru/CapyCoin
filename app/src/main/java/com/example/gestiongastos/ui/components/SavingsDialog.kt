package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import com.example.gestiongastos.data.model.ItemKeys
import java.util.Calendar
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

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

@Composable
fun SavingsItemCard(
    saving: SavingsEntity,
    isPrivacyMode: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat(tr("dd MMM yyyy", "MMM d, yyyy"), appLocale())
    val formattedDate = dateFormat.format(Date(saving.date))
    val isDark = isAppDark()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FinanceColors.savingsContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = FinanceColors.savingsAccent(isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = saving.concept,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (saving.isRecurring) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = FinanceColors.savingsContainer(isDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Autorenew,
                                        contentDescription = tr("Recurrente", "Recurring"),
                                        tint = FinanceColors.savingsAccent(isDark),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = tr("Recurrente", "Recurring"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.savingsText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AutoResizedText(
                    text = privateText(formatCurrency(saving.amount), isPrivacyMode, "$ ••••••"),
                    isPrivate = isPrivacyMode,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = FinanceColors.savingsText(isDark)
                    )
                )

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = tr("Opciones del ahorro", "Savings options"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text(tr("Editar", "Edit"), style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(tr("Eliminar", "Delete"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditSavingsDialog(
    savingToEdit: SavingsEntity?,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSave: (SavingsEntity) -> Unit
) {
    var concept by remember { mutableStateOf(savingToEdit?.concept ?: "") }
    var amountText by remember { mutableStateOf(amountToInput(savingToEdit?.amount)) }
    var isRecurring by remember { mutableStateOf(savingToEdit?.isRecurring ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val initialDateMillis = remember(savingToEdit, selectedYearMonth) {
        calculateSaveDateForMonth(selectedYearMonth, savingToEdit?.date)
    }
    var savingDateMillis by remember(savingToEdit, selectedYearMonth) { mutableLongStateOf(initialDateMillis) }

    val context = LocalContext.current
    val datePickerDialog = rememberDatePicker(savingDateMillis) { savingDateMillis = it }
    val savingDateFormat = remember { SimpleDateFormat(tr("dd 'de' MMMM 'de' yyyy", "MMMM d, yyyy"), appLocale()) }

    FormSheet(
        onDismissRequest = onDismiss,
        title = { Text(if (savingToEdit == null) tr("Nuevo Ahorro", "New savings") else tr("Editar Ahorro", "Edit savings")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(tr("Concepto del Ahorro", "Savings name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AmountTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = tr("Monto", "Amount"),
                    prominent = true
                )

                // Date Picker Field for Ahorro
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("Fecha del ahorro", "Savings date"),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = savingDateFormat.format(Date(savingDateMillis)),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = tr("Cambiar fecha", "Change date"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tr("Recurrente mensual", "Repeats monthly"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Se aplicará a los meses siguientes", "It will apply to the following months"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    val amount = parseAmount(amountText)
                    if (concept.isBlank()) {
                        errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                    } else if (amount == null || amount <= 0) {
                        errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                    } else {
                        val base = savingToEdit ?: SavingsEntity(concept = concept.trim(), amount = amount, date = savingDateMillis, isRecurring = isRecurring)
                        onSave(base.copy(concept = concept.trim(), amount = amount, date = savingDateMillis, isRecurring = isRecurring))
                    }
                }
            ) {
                Text(tr("Guardar", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}
