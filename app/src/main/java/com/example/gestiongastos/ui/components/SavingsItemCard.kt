package com.example.gestiongastos.ui.components

import com.example.gestiongastos.ui.components.shortDateFormat
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.gestiongastos.data.local.SavingsEntity
import java.util.Date

// Tarjeta de un ahorro en la lista.
@Composable
fun SavingsItemCard(
    saving: SavingsEntity,
    isPrivacyMode: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = shortDateFormat()
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
