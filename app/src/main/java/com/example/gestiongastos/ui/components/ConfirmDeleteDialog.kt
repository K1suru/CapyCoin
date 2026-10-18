package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Confirmación antes de borrar. Si se pasa [onEndFromMonth] (registros recurrentes),
 * ofrece terminarlo desde [selectedYearMonth] conservando el historial de los meses anteriores.
 */
@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onDeleteAll: () -> Unit,
    onEndFromMonth: (() -> Unit)? = null,
    extraMessage: String? = null
) {
    val monthName = selectedYearMonth.month
        .getDisplayName(TextStyle.FULL, appLocale())
        .replaceFirstChar { it.uppercase() } + " " + selectedYearMonth.year

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(tr("¿Eliminar '$itemName'?", "Delete '$itemName'?"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onEndFromMonth != null) {
                    Text(
                        text = tr("Es un registro recurrente. Puedes finalizarlo desde $monthName (los meses anteriores se conservan) o eliminarlo de todo el historial.", "This item repeats. You can end it from $monthName (earlier months are kept) or delete it from your whole history."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = tr("Esta acción no se puede deshacer.", "This can't be undone."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (extraMessage != null) {
                    Text(
                        text = extraMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                if (onEndFromMonth != null) {
                    TextButton(onClick = {
                        onEndFromMonth()
                        onDismiss()
                    }) {
                        Text(tr("Finalizar desde $monthName", "End from $monthName"), fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = {
                    onDeleteAll()
                    onDismiss()
                }) {
                    Text(
                        text = if (onEndFromMonth != null) tr("Eliminar de todo el historial", "Delete from whole history") else tr("Eliminar", "Delete"),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(tr("Cancelar", "Cancel"))
                }
            }
        }
    )
}
