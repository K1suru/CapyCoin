package com.example.gestiongastos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.model.ExpenseInstallmentInfo
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.amountToInput
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.privacyBlur
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val MAX_EXPENSE_INSTALLMENTS = 360

private fun formatMoney(amount: Double, currency: String) =
    if (currency == "USD") formatUsd(amount) else formatCurrency(amount)

/** Valida el número de cuotas escrito: null si no es válido. */
fun parseExpenseInstallments(text: String): Int? =
    text.toIntOrNull()?.takeIf { it in 1..MAX_EXPENSE_INSTALLMENTS }

/**
 * Interruptor "En cuotas" del gasto fijo: cuota mensual fija durante N meses
 * desde el mes de la fecha del gasto (p. ej. una deuda con una persona).
 */
@Composable
fun ExpenseInstallmentsFields(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    countText: String,
    onCountTextChange: (String) -> Unit,
    installmentAmount: Double,
    currency: String,
    startDateMillis: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tr("En cuotas", "In installments"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = tr("Una cuota fija cada mes, p. ej. una deuda con alguien", "A fixed payment every month, e.g. money you owe someone"),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }

    if (!enabled) return

    OutlinedTextField(
        value = countText,
        onValueChange = { text -> onCountTextChange(text.filter { it.isDigit() }.take(3)) },
        label = { Text(tr("Número de cuotas", "Number of installments")) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    val count = parseExpenseInstallments(countText)
    if (count != null && installmentAmount > 0) {
        val start = YearMonth.from(Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate())
        val monthFormat = DateTimeFormatter.ofPattern("MMM yyyy", appLocale())
        val range = "${start.format(monthFormat)} → ${start.plusMonths(count - 1L).format(monthFormat)}"
        Surface(
            color = FinanceColors.expenseContainer(isAppDark()),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tr("Total de la deuda", "Total debt"),
                    style = MaterialTheme.typography.labelSmall.copy(color = FinanceColors.expenseText(isAppDark()))
                )
                Text(
                    text = formatMoney(installmentAmount * count, currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = FinanceColors.expenseText(isAppDark()))
                )
                Text(
                    text = tr("$count cuotas de ${formatMoney(installmentAmount, currency)} • $range", "$count payments of ${formatMoney(installmentAmount, currency)} • $range"),
                    style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.expenseText(isAppDark()))
                )
            }
        }
    }
}

/** Barra de avance y saldo de un gasto fijo en cuotas (va a todo el ancho, debajo de la fila del gasto). */
@Composable
fun ExpenseInstallmentProgress(
    info: ExpenseInstallmentInfo,
    currency: String,
    isPrivacyMode: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDark()
    val pending = (info.remainingBalance - info.monthCharge).coerceAtLeast(0.0)
    val total = info.installmentAmount * (info.currentInstallment - 1) + info.paidExtraTotal + info.remainingBalance
    val paidFraction = if (total > 0) ((total - pending) / total).toFloat().coerceIn(0f, 1f) else 1f
    val installmentsLeft = info.totalInstallments - info.currentInstallment
    val smallStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { paidFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = FinanceColors.expenseText(isDark),
            trackColor = FinanceColors.expenseContainer(isDark),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (info.isPaidOff || pending <= 0.0) tr("¡Última cuota! 🎉", "Last payment! 🎉")
                else privateText(tr("Te faltan ${formatMoney(pending, currency)}", "${formatMoney(pending, currency)} to go"), isPrivacyMode, mask = tr("Te faltan $ ••••••", "$ •••••• to go")),
                style = smallStyle.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .privacyBlur(isPrivacyMode && pending > 0.0)
            )
            if (installmentsLeft > 0 && !info.isPaidOff) {
                Text(
                    text = tr("$installmentsLeft cuotas más", "$installmentsLeft more"),
                    style = smallStyle,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Abono a un gasto fijo en cuotas: baja el saldo sin cambiar el valor de la cuota,
 * así la deuda termina antes (y la última cuota puede ser menor).
 */
@Composable
fun ExpensePaymentDialog(
    expense: ExpenseEntity,
    info: ExpenseInstallmentInfo,
    onDismiss: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currency = expense.currency
    val balance = info.remainingBalance
    val parsedAmount = parseAmount(amountText) ?: 0.0
    val newBalance = (balance - parsedAmount).coerceAtLeast(0.0)
    val monthsLeft = if (info.installmentAmount > 0) kotlin.math.ceil(newBalance / info.installmentAmount - 1e-9).toInt() else 0

    FormSheet(
        onDismissRequest = onDismiss,
        title = { Text(tr("Abonar a: ${expense.concept}", "Extra payment to: ${expense.concept}")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = tr("Cuota ${info.currentInstallment} de ${info.totalInstallments} • ${formatMoney(info.installmentAmount, currency)} al mes", "Installment ${info.currentInstallment} of ${info.totalInstallments} • ${formatMoney(info.installmentAmount, currency)} a month"),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (info.paidExtraTotal > 0) {
                    Text(
                        text = tr("Abonado acumulado: ${formatMoney(info.paidExtraTotal, currency)}", "Extra paid so far: ${formatMoney(info.paidExtraTotal, currency)}"),
                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeAccent(isAppDark()))
                    )
                }
                Text(
                    text = tr("Saldo pendiente (con la cuota de este mes): ${formatMoney(balance, currency)}", "Remaining balance (including this month): ${formatMoney(balance, currency)}"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                AmountTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = tr("Monto a abonar", "Payment amount"),
                    currency = currency,
                    prominent = true
                )

                AssistChip(
                    onClick = { amountText = amountToInput(balance, currency == "USD") },
                    label = { Text(tr("Pagar todo (${formatMoney(balance, currency)})", "Pay it all (${formatMoney(balance, currency)})")) }
                )

                if (parsedAmount > 0) {
                    Surface(
                        color = FinanceColors.incomeContainer(isAppDark()),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tr("Nuevo saldo estimado:", "Estimated new balance:"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeText(isAppDark()))
                            )
                            Text(
                                text = formatMoney(newBalance, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = FinanceColors.incomeAccent(isAppDark()))
                            )
                            Text(
                                text = if (newBalance <= 0.0) tr("¡Quedas a paz y salvo! 🎉", "You're all paid up! 🎉")
                                else tr("Te quedarían $monthsLeft cuotas (contando este mes)", "You'd have $monthsLeft payments left (counting this month)"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeText(isAppDark()))
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    parsedAmount <= 0 -> errorMessage = tr("Ingresa un monto de abono mayor a 0", "Enter a payment greater than 0")
                    parsedAmount > balance + 0.01 -> errorMessage = tr("El abono no puede ser mayor al saldo de ${formatMoney(balance, currency)}", "The payment can't be more than the balance of ${formatMoney(balance, currency)}")
                    else -> onSave(parsedAmount.coerceAtMost(balance))
                }
            }) { Text(tr("Guardar Abono", "Save payment")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("Cancelar", "Cancel")) }
        }
    )
}
