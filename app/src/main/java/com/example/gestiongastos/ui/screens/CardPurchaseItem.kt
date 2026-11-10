package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.shortDateFormat
import com.example.gestiongastos.ui.components.currencySymbolOf
import com.example.gestiongastos.ui.components.formatUsdWithCop
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import java.util.Date

// Tarjeta de una compra en la lista de una tarjeta de crédito.
@Composable
fun CardPurchaseItem(
    purchase: PurchaseInstallmentDetail,
    usdRate: Double = 4150.0,
    isPrivacyMode: Boolean = false,
    onSettle: () -> Unit,
    onAddPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = shortDateFormat()
    val formattedDate = dateFormat.format(Date(purchase.purchaseDateMillis))
    val currencySymbol = currencySymbolOf(purchase.currency)
    val isDark = isAppDark()

    val (badgeBg, badgeText) = when {
        purchase.isSettledThisMonth -> Pair(FinanceColors.goldContainer(isDark), FinanceColors.goldText(isDark))
        purchase.statusType == InstallmentStatusType.ACTIVE -> Pair(FinanceColors.cardContainer(isDark), FinanceColors.cardText(isDark))
        purchase.statusType == InstallmentStatusType.FINISHED -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        purchase.statusType == InstallmentStatusType.FUTURE -> Pair(FinanceColors.goldContainer(isDark), FinanceColors.goldText(isDark))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val showRemaining = purchase.totalInstallments > 1 && purchase.statusType == InstallmentStatusType.ACTIVE

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (purchase.isSettledThisMonth) {
                FinanceColors.incomeContainer(isDark).copy(alpha = if (isDark) 0.35f else 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (purchase.isSettledThisMonth) {
                FinanceColors.goldAccent(isDark).copy(alpha = if (isDark) 0.6f else 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (purchase.isSettledThisMonth) FinanceColors.goldContainer(isDark)
                                else FinanceColors.cardContainer(isDark)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (purchase.isSettledThisMonth) Icons.Default.CheckCircle else Icons.Default.Payment,
                            contentDescription = null,
                            tint = if (purchase.isSettledThisMonth) FinanceColors.goldAccent(isDark) else FinanceColors.cardAccent(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = purchase.concept,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                ),
                                modifier = Modifier.weight(1f, fill = false),
                                softWrap = true
                            )
                            if (purchase.isInterestFree && purchase.totalInstallments > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = FinanceColors.incomeContainer(isDark),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tr("0% int.", "0% int."),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.incomeText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        val totalStr = if (isPrivacyMode) tr("Total: $ ••••••", "Total: $ ••••••") else if (purchase.currency == "USD") tr("Total: ${formatUsdWithCop(purchase.totalAmount, usdRate)}", "Total: ${formatUsdWithCop(purchase.totalAmount, usdRate)}") else tr("Total: ${formatCurrency(purchase.totalAmount, currencySymbol)}", "Total: ${formatCurrency(purchase.totalAmount, currencySymbol)}")
                        val remainingStr = if (!isPrivacyMode && showRemaining) {
                            if (purchase.currency == "USD") tr(" | Restante: ${formatUsdWithCop(purchase.remainingBalanceToPay, usdRate)}", " | Remaining: ${formatUsdWithCop(purchase.remainingBalanceToPay, usdRate)}")
                            else tr(" | Restante: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}", " | Remaining: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}")
                        } else ""

                        Text(
                            text = "$totalStr$remainingStr",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        if (purchase.totalPaidAmount > 0) {
                            val abonoStr = if (isPrivacyMode) tr("Abonado: $ ••••••", "Paid extra: $ ••••••") else if (purchase.currency == "USD") tr("Abonado: ${formatUsdWithCop(purchase.totalPaidAmount, usdRate)}", "Paid extra: ${formatUsdWithCop(purchase.totalPaidAmount, usdRate)}") else tr("Abonado: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}", "Paid extra: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}")
                            Text(
                                text = abonoStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FinanceColors.incomeText(isDark),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = tr("Opciones de la compra", "Purchase options"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
                    ) {
                        if (purchase.isSettledThisMonth) {
                            DropdownMenuItem(
                                text = { Text(tr("Revertir liquidación", "Undo full payment"), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FinanceColors.goldAccent(isDark)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onSettle()
                                }
                            )
                        } else if (purchase.statusType == InstallmentStatusType.ACTIVE && (purchase.totalInstallments > 1 || purchase.isRecurring)) {
                            DropdownMenuItem(
                                text = { Text(tr("Pagar saldo completo este mes", "Pay full balance this month"), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = FinanceColors.incomeAccent(isDark)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onSettle()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(tr("Hacer abono", "Make extra payment"), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = FinanceColors.incomeAccent(isDark)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onAddPayment()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(tr("Editar", "Edit"), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = { Text(tr("Eliminar compra", "Delete purchase"), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
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

            if (purchase.totalInstallments > 1 && purchase.statusType == InstallmentStatusType.ACTIVE) {
                val progress = (purchase.currentInstallment.toFloat() / purchase.totalInstallments.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(FinanceColors.cardAccent(isDark))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = purchase.statusText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tr("Pago este mes", "Due this month"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPrivacyMode) {
                        Text(
                            text = "$ ••••••",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                    } else if (purchase.currency == "USD") {
                        Text(
                            text = formatUsd(purchase.monthlyPayment),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                        Text(
                            text = "(~${formatCurrency(purchase.monthlyPayment * usdRate)})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = formatCurrency(purchase.monthlyPayment, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                    }
                }
            }
        }
    }
}
