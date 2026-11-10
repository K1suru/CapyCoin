package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.YearMonth


// Selector de mes y año.
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
