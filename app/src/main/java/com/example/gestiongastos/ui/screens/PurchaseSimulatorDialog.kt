package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.formatUsdWithCop
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount

// Simulador de cuotas de una compra.
@Composable
fun PurchaseSimulatorDialog(
    cardSummary: CardMonthSummary,
    usdRate: Double = 4150.0,
    onDismiss: () -> Unit
) {
    val isDark = isAppDark()
    var amountInput by remember { mutableStateOf("") }
    var installmentsInput by remember { mutableStateOf("1") }
    var isInterestFree by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(cardSummary.currency) }

    val amount = parseAmount(amountInput) ?: 0.0
    val installments = installmentsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1

    // Usa exactamente el mismo plan de cuotas que las compras reales
    val (monthlyPayment, totalToPay, totalInterest) = remember(amount, installments, isInterestFree, cardSummary.interestRate, cardSummary.isFirstInstallmentInterestFree) {
        if (amount <= 0) {
            Triple(0.0, 0.0, 0.0)
        } else {
            val schedule = installmentSchedule(
                principal = amount,
                installments = installments,
                monthlyRatePercent = cardSummary.interestRate,
                isInterestFree = isInterestFree,
                isFirstInstallmentInterestFree = cardSummary.isFirstInstallmentInterestFree
            )
            val total = schedule.sum()
            Triple(schedule.last(), total, (total - amount).coerceAtLeast(0.0))
        }
    }

    val currencySymbol = if (selectedCurrency == "USD") "USD $" else "$"

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FinanceColors.cardContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = FinanceColors.cardAccent(isDark),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tr("Simulador de Cuotas", "Installment simulator"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = tr("${cardSummary.cardName} (${cardSummary.interestRate}% int. mensual)", "${cardSummary.cardName} (${cardSummary.interestRate}% monthly int.)"),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmountTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = tr("Monto de la compra", "Purchase amount"),
                    placeholder = tr("Ej: 250.000", "E.g. 250.000"),
                    currency = selectedCurrency,
                    prominent = true
                )

                // Quick Installment Selector Chips
                Text(tr("Número de cuotas:", "Number of installments:"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 6, 12, 24).forEach { cuota ->
                        val isSelected = installmentsInput == cuota.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FinanceColors.cardAccent(isDark) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { installmentsInput = cuota.toString() }
                        ) {
                            Text(
                                text = "$cuota",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FinanceColors.onAccent(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // 0% Interest Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr("¿Promoción 0% Interés?", "0% interest promotion?"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isInterestFree,
                        onCheckedChange = { isInterestFree = it }
                    )
                }

                if (amount > 0) {
                    HorizontalDivider()

                    // Result Bento Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FinanceColors.cardContainer(isDark).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, FinanceColors.cardAccent(isDark).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Pago Mensual Estimado:", "Estimated monthly payment:"), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (selectedCurrency == "USD") formatUsdWithCop(monthlyPayment, usdRate) else formatCurrency(monthlyPayment),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinanceColors.cardText(isDark)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Total a Pagar ($installments cuotas):", "Total to pay ($installments installments):"), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (selectedCurrency == "USD") formatUsd(totalToPay) else formatCurrency(totalToPay),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Total en Intereses:", "Total interest:"), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (totalInterest <= 0.0) tr("$0 (Sin Interés)", "$0 (No interest)") else if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalInterest <= 0.0) FinanceColors.incomeText(isDark) else FinanceColors.expenseText(isDark)
                                    )
                                )
                            }

                            if (totalInterest > 0) {
                                Surface(
                                    color = FinanceColors.incomeContainer(isDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = tr("💡 ¡Ahorras ${if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest)} si pagas a 1 cuota sin interés!", "💡 You save ${if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest)} by paying in 1 installment with no interest!"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = FinanceColors.incomeText(isDark)
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
            }
        }
    )
}
