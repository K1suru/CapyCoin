package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.formatCurrency

// Detalle y configuración de una tarjeta de crédito.
@Composable
fun CardInfoDetailDialog(
    cardSummary: CardMonthSummary,
    payableThisMonth: Double,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    val isDark = isAppDark()

    FormSheet(
        onDismissRequest = onDismiss,
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
                    text = cardSummary.cardName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = tr("Detalles y configuración de la tarjeta", "Card details and settings"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Card 1: Condiciones Financieras
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Día de Corte", "Statement day"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tr("Día ${cardSummary.cutoffDay} de cada mes", "Day ${cardSummary.cutoffDay} of each month"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Día Límite de Pago", "Payment due day"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.paymentDay in 1..31) tr("Día ${cardSummary.paymentDay} de cada mes", "Day ${cardSummary.paymentDay} of each month") else tr("No configurado", "Not set"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Tasa de Interés", "Interest rate"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${cardSummary.interestRate}% M.V.", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("1ª Cuota Sin Interés", "1st installment interest-free"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.isFirstInstallmentInterestFree) tr("0% Activado", "0% on") else tr("No", "No"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cardSummary.isFirstInstallmentInterestFree) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Bento Card 2: Cuota de Manejo y Exoneración
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Cuota de Manejo", "Card fee"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.isFeeWaivedThisMonth) tr("$0 (Exonerada)", "$0 (Waived)") else formatCurrency(cardSummary.maintenanceFee),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cardSummary.isFeeWaivedThisMonth) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        if (cardSummary.isFeeWaiverEnabled) {
                            val isWaived = cardSummary.isFeeWaivedThisMonth
                            val minRequired = cardSummary.minSpendForFeeWaiver
                            val currentSpending = cardSummary.currentMonthSpending

                            Surface(
                                color = if (isWaived) FinanceColors.incomeContainer(isDark) else FinanceColors.goldContainer(isDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isWaived) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isWaived) FinanceColors.incomeAccent(isDark) else FinanceColors.goldAccent(isDark),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isWaived) {
                                            tr("¡Exoneración cumplida! Superaste el mínimo de ${formatCurrency(minRequired)}", "Fee waived! You passed the minimum of ${formatCurrency(minRequired)}")
                                        } else {
                                            tr("Meta para exonerar: ${formatCurrency(minRequired)} (Llevas ${formatCurrency(currentSpending)})", "Goal to waive the fee: ${formatCurrency(minRequired)} (So far ${formatCurrency(currentSpending)})")
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isWaived) FinanceColors.incomeText(isDark) else FinanceColors.goldText(isDark)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bento Card 3: Totales de la Tarjeta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceColors.cardContainer(isDark).copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, FinanceColors.cardContainer(isDark))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Pago este Mes", "Due this month"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceColors.cardText(isDark)
                                )
                            )
                            Text(
                                text = formatCurrency(payableThisMonth),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.cardText(isDark)
                                )
                            )
                        }

                        HorizontalDivider(color = FinanceColors.cardContainer(isDark).copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("Deuda Total", "Total debt"),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = tr("Saldo total restante por pagar", "Total remaining balance to pay"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = formatCurrency(cardSummary.currentMonthSpending),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.expenseText(isDark)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Single Action Button: Configuración de la Tarjeta
                Button(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Configuración de la Tarjeta", "Card settings"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
