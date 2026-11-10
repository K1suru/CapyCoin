package com.example.gestiongastos.ui.components

import com.example.gestiongastos.ui.components.longDateFormat
import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import java.time.YearMonth
import java.util.Date

// Formulario para agregar o editar un ahorro.
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
    val savingDateFormat = remember { longDateFormat() }

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

                errorMessage?.let { errorText ->
                    Text(
                        text = errorText,
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
                        val base = savingToEdit ?: SavingsEntity(concept = concept.trim(), amountCents = toCents(amount), date = savingDateMillis, isRecurring = isRecurring)
                        onSave(base.copy(concept = concept.trim(), amountCents = toCents(amount), date = savingDateMillis, isRecurring = isRecurring))
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
