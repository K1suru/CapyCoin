package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.formatUsdWithCop
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.PawCheckButton

// Tarjeta de un ítem en la lista de Gastos.
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseItemCard(
    item: UnifiedExpenseItem,
    usdRate: Double = 4150.0,
    isIgnored: Boolean = false,
    isPaid: Boolean = false,
    isPrivacyMode: Boolean = false,
    onToggleIgnore: () -> Unit = {},
    onTogglePaid: () -> Unit = {},
    onAddPayment: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isAppDark()
    val haptic = LocalHapticFeedback.current
    val (icon, iconBg, iconColor, categoryName) = when (item) {
        is UnifiedExpenseItem.DirectExpense -> {
            val inst = item.installment
            val label = when {
                inst?.isPaidOff == true -> tr("Saldado ✓", "Paid off ✓")
                inst != null -> tr("Cuota ${inst.currentInstallment} de ${inst.totalInstallments}", "Installment ${inst.currentInstallment} of ${inst.totalInstallments}")
                item.expense.isRecurring -> tr("Recurrente", "Recurring")
                else -> tr("Gasto Fijo", "Fixed expense")
            }
            Quadruple(
                Icons.AutoMirrored.Filled.TrendingDown,
                FinanceColors.expenseContainer(isDark),
                FinanceColors.expenseText(isDark),
                label
            )
        }
        is UnifiedExpenseItem.CardInstallmentExpense -> {
            val installmentInfo = when {
                item.detail.isRecurring -> tr(" • Recurrente", " • Recurring")
                item.detail.totalInstallments > 1 -> tr(" • Cuota ${item.detail.currentInstallment}/${item.detail.totalInstallments}", " • Installment ${item.detail.currentInstallment}/${item.detail.totalInstallments}")
                else -> tr(" • 1 cuota", " • 1 installment")
            }
            Quadruple(
                Icons.Default.CreditCard,
                FinanceColors.cardContainer(isDark),
                FinanceColors.cardText(isDark),
                "${item.cardName}$installmentInfo"
            )
        }
        is UnifiedExpenseItem.CardFeeExpense -> Quadruple(
            Icons.Default.Payment,
            FinanceColors.cardContainer(isDark),
            FinanceColors.cardText(isDark),
            tr("${item.cardName} • Cuota de manejo", "${item.cardName} • Card fee")
        )
        is UnifiedExpenseItem.SavingsExpense -> {
            val label = if (item.saving.isRecurring) tr("Ahorro • Recurrente", "Savings • Recurring") else tr("Ahorro", "Savings")
            Quadruple(
                Icons.Default.Savings,
                FinanceColors.savingsContainer(isDark),
                FinanceColors.savingsText(isDark),
                label
            )
        }
    }

    val isUsd = when (item) {
        is UnifiedExpenseItem.DirectExpense -> item.expense.currency == "USD"
        is UnifiedExpenseItem.CardInstallmentExpense -> item.detail.currency == "USD"
        else -> false
    }
    val usdAmount = when (item) {
        is UnifiedExpenseItem.DirectExpense -> item.expense.amount
        is UnifiedExpenseItem.CardInstallmentExpense -> item.detail.monthlyPayment
        else -> 0.0
    }
    val copConvertedAmount = usdAmount * usdRate

    val amountStr = when (item) {
        is UnifiedExpenseItem.DirectExpense -> {
            if (item.expense.currency == "USD") formatUsdWithCop(item.expense.amount, usdRate)
            else formatCurrency(item.amount)
        }
        is UnifiedExpenseItem.CardInstallmentExpense -> {
            if (item.detail.currency == "USD") formatUsdWithCop(item.detail.monthlyPayment, usdRate)
            else formatCurrency(item.amount, item.currencySymbol)
        }
        is UnifiedExpenseItem.CardFeeExpense -> formatCurrency(item.amount, item.currencySymbol)
        is UnifiedExpenseItem.SavingsExpense -> formatCurrency(item.amount)
    }

    val displayAmountColor = when {
        isIgnored -> MaterialTheme.colorScheme.onSurfaceVariant
        isPaid -> FinanceColors.incomeText(isDark)
        else -> iconColor
    }

    var showCardDetailDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPaid -> FinanceColors.incomeContainer(isDark).copy(alpha = if (isDark) 0.35f else 0.25f)
                isIgnored -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.25f else 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                isPaid -> FinanceColors.incomeAccent(isDark).copy(alpha = if (isDark) 0.6f else 0.45f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isPaid) FinanceColors.incomeContainer(isDark) else iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (isPaid) FinanceColors.incomeAccent(isDark) else iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.concept,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp
                        ),
                        softWrap = true
                    )

                    // Si las etiquetas no caben juntas, "Ignorado" pasa a la siguiente línea en vez de cortarse
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Surface(
                            color = iconBg,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.then(
                                if (item is UnifiedExpenseItem.CardInstallmentExpense) {
                                    Modifier.clickable { showCardDetailDialog = true }
                                } else Modifier
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = iconColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item is UnifiedExpenseItem.CardInstallmentExpense) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = tr("Ver detalle", "See details"),
                                        tint = iconColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        if (isIgnored) {
                            Surface(
                                color = FinanceColors.goldContainer(isDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tr("Ignorado", "Ignored"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FinanceColors.goldText(isDark),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isPrivacyMode) {
                    Text(
                        text = "$ ••••••",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = displayAmountColor,
                            textDecoration = if (isIgnored) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1
                    )
                } else if (isUsd) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatUsd(usdAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = displayAmountColor,
                                textDecoration = if (isIgnored) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "(~${formatCurrency(copConvertedAmount)})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textDecoration = if (isIgnored) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = amountStr,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = displayAmountColor,
                            textDecoration = if (isIgnored) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        maxLines = 1
                    )
                }

                // Botón rápido ✓: marca/desmarca como pagado con un toque
                PawCheckButton(
                    checked = isPaid,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePaid()
                    },
                    contentDescription = if (isPaid) tr("Desmarcar como pagado", "Mark as unpaid") else tr("Marcar como pagado", "Mark as paid"),
                    checkedColor = FinanceColors.incomeAccent(isDark),
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                var showOverflowMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = tr("Opciones del gasto", "Expense options"),
                            tint = if (isIgnored) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPaid) tr("Desmarcar como pagado", "Mark as unpaid") else tr("Marcar como pagado", "Mark as paid"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isPaid) FinanceColors.incomeAccent(isDark) else MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showOverflowMenu = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePaid()
                            }
                        )

                        if (onAddPayment != null) {
                            DropdownMenuItem(
                                text = { Text(tr("Hacer abono", "Make extra payment"), style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = FinanceColors.incomeAccent(isDark)
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onAddPayment()
                                }
                            )
                        }

                        if (item !is UnifiedExpenseItem.CardFeeExpense) {
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
                                    showOverflowMenu = false
                                    onEdit()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isIgnored) tr("Dejar de ignorar", "Stop ignoring") else tr("Ignorar", "Ignore"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isIgnored) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (isIgnored) FinanceColors.goldAccent(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                showOverflowMenu = false
                                onToggleIgnore()
                            }
                        )

                        if (item !is UnifiedExpenseItem.CardFeeExpense) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = tr("Borrar", "Delete"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = tr("Borrar", "Delete"),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Avance de la deuda a todo el ancho, alineado con el nombre (debajo del ícono no va nada)
        if (item is UnifiedExpenseItem.DirectExpense && item.installment != null) {
            ExpenseInstallmentProgress(
                info = item.installment,
                currency = item.expense.currency,
                isPrivacyMode = isPrivacyMode,
                modifier = Modifier.padding(start = 60.dp, end = 16.dp, bottom = 12.dp)
            )
        }
    }

    if (showCardDetailDialog && item is UnifiedExpenseItem.CardInstallmentExpense) {
        val detail = item.detail
        AlertDialog(
            onDismissRequest = { showCardDetailDialog = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FinanceColors.cardContainer(isDark)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = FinanceColors.cardAccent(isDark),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.concept,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = item.cardName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardDetailRow(label = tr("Tarjeta de Crédito", "Credit card"), value = item.cardName)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        CardDetailRow(
                            label = tr("Estado de Cuotas", "Installment status"),
                            value = if (detail.totalInstallments > 1) tr("Cuota ${detail.currentInstallment} de ${detail.totalInstallments}", "Installment ${detail.currentInstallment} of ${detail.totalInstallments}") else tr("1 cuota", "1 installment")
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        CardDetailRow(
                            label = tr("Intereses de Cuota", "Installment interest"),
                            value = when {
                                detail.isFirstInstallmentInterestFree && detail.currentInstallment == 1 -> tr("1ª cuota sin interés (0%)", "1st installment interest-free (0%)")
                                detail.isInterestFree -> tr("0% interés", "0% interest")
                                detail.interestRate > 0.0 -> tr("${detail.interestRate}% mensual", "${detail.interestRate}% monthly")
                                else -> tr("0% interés", "0% interest")
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        CardDetailRow(
                            label = tr("Cuota del Mes", "This month's installment"),
                            value = if (detail.currency == "USD") formatUsdWithCop(detail.monthlyPayment, usdRate)
                                    else formatCurrency(detail.monthlyPayment, item.currencySymbol),
                            isHighlight = true,
                            highlightColor = FinanceColors.cardText(isDark)
                        )
                        if (detail.totalInstallments > 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            CardDetailRow(
                                label = "Saldo Restante",
                                value = if (detail.currency == "USD") formatUsdWithCop(detail.remainingBalanceToPay, usdRate)
                                        else formatCurrency(detail.remainingBalanceToPay, item.currencySymbol)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            CardDetailRow(
                                label = "Monto Total Compra",
                                value = if (detail.currency == "USD") formatUsdWithCop(detail.totalAmount, usdRate)
                                        else formatCurrency(detail.totalAmount, item.currencySymbol)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { showCardDetailDialog = false }) {
                        Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
private fun CardDetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    highlightColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isHighlight) highlightColor else MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.End
        )
    }
}
