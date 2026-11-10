package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.shortDateFormat
import com.example.gestiongastos.data.model.incomeInstallmentNumber
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.ui.components.privateText
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.PawCheckButton
import com.example.gestiongastos.data.repository.yearMonthOf
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date

import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark

// Tarjeta de un ingreso en la lista de Ingresos.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomeItemCard(
    income: IncomeEntity,
    targetYearMonth: YearMonth,
    isReceived: Boolean = false,
    isPrivacyMode: Boolean = false,
    onToggleReceived: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowReimbursementDetail: () -> Unit = {}
) {
    val dateFormat = shortDateFormat()
    val formattedDate = dateFormat.format(Date(income.date))
    val isReimbursement = !income.linkedExpenseIdsText.isNullOrBlank() || !income.linkedCardPurchaseIdsText.isNullOrBlank() || income.extraAmount > 0
    val isDark = isAppDark()
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val currentInstallment = incomeInstallmentNumber(income, targetYearMonth)

    val endYearMonth = if (income.isRecurring && income.endDateMillis != null) {
        yearMonthOf(income.endDateMillis)
    } else null
    val endMonthFormatted = endYearMonth?.let {
        it.month.getDisplayName(TextStyle.SHORT, appLocale()).replaceFirstChar { c -> c.uppercase() } + " " + it.year
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isReimbursement) {
                onShowReimbursementDetail()
            },
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
                        .background(FinanceColors.incomeContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = FinanceColors.incomeAccent(isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = income.concept,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val subtitleText = when {
                        income.totalInstallments > 1 -> {
                            val totalDisplay = if (isPrivacyMode) "$ ••••••" else formatCurrency(income.totalAmount)
                            tr("Cuota $currentInstallment de ${income.totalInstallments} • Total: $totalDisplay", "Installment $currentInstallment of ${income.totalInstallments} • Total: $totalDisplay")
                        }
                        income.isRecurring && endMonthFormatted != null -> {
                            tr("$formattedDate • Hasta $endMonthFormatted", "$formattedDate • Until $endMonthFormatted")
                        }
                        else -> formattedDate
                    }

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Las etiquetas saltan a otra línea si no caben (antes se aplastaban)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (isReceived) {
                            Surface(
                                color = FinanceColors.incomeContainer(isDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FinanceColors.incomeAccent(isDark),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = tr("Ingresado", "Received"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.incomeText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        if (isReimbursement) {
                            Surface(
                                color = FinanceColors.incomeContainer(isDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tr("Reembolso", "Refund"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.incomeText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = tr("Ver detalle", "See details"),
                                        tint = FinanceColors.incomeAccent(isDark),
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        } else if (income.totalInstallments > 1) {
                            Surface(
                                color = FinanceColors.incomeContainer(isDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = FinanceColors.incomeAccent(isDark),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = tr("Cuota $currentInstallment/${income.totalInstallments}", "Installment $currentInstallment/${income.totalInstallments}"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.incomeText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        } else if (income.isRecurring) {
                            Surface(
                                color = FinanceColors.incomeContainer(isDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (endMonthFormatted != null) tr("Hasta $endMonthFormatted", "Until $endMonthFormatted") else tr("Recurrente", "Recurring"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FinanceColors.incomeText(isDark),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    AutoResizedText(
                        text = privateText(formatCurrency(kotlin.math.round(income.amount)), isPrivacyMode, "$ ••••••"),
                        isPrivate = isPrivacyMode,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FinanceColors.incomeText(isDark)
                        )
                    )
                }

                // Botón rápido ✓: marca/desmarca como ingresado con un toque
                PawCheckButton(
                    checked = isReceived,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleReceived()
                    },
                    contentDescription = if (isReceived) tr("Desmarcar como ingresado", "Mark as not received") else tr("Marcar como ingresado", "Mark as received"),
                    checkedColor = FinanceColors.incomeAccent(isDark),
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = tr("Opciones del ingreso", "Income options"),
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
                            text = {
                                Text(
                                    text = if (isReceived) tr("Desmarcar como ingresado", "Mark as not received") else tr("Marcar como ingresado", "Mark as received"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isReceived) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isReceived) FinanceColors.incomeAccent(isDark) else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleReceived()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(tr("Editar", "Edit"), style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = tr("Editar", "Edit"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = tr("Eliminar", "Delete"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = tr("Eliminar", "Delete"),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
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
