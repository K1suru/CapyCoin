package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.longDateFormat
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.data.model.ExtraPaymentCheck
import com.example.gestiongastos.data.model.checkExtraPayment
import com.example.gestiongastos.ui.theme.CardPalettes
import com.example.gestiongastos.ui.theme.autoCardColorIndex
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.rememberDatePicker
import com.example.gestiongastos.ui.components.amountToInput
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatMoney
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.parsePercentRate
import java.time.YearMonth
import java.util.Date

// Formularios de Tarjetas: abono a una compra, agregar o editar una tarjeta y agregar o editar una compra.
@Composable
fun AddPaymentDialog(
    purchase: PurchaseInstallmentDetail,
    onDismiss: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedAmount = parseAmount(amountText) ?: 0.0
    val newBalance = (purchase.remainingBalanceToPay - parsedAmount).coerceAtLeast(0.0)

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = tr("Abonar a: ${purchase.concept}", "Extra payment to: ${purchase.concept}"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = tr("Monto inicial compra: ${formatMoney(purchase.totalAmount, purchase.currency)}", "Original amount: ${formatMoney(purchase.totalAmount, purchase.currency)}"),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (purchase.totalPaidAmount > 0) {
                    Text(
                        text = tr("Abonado acumulado: ${formatMoney(purchase.totalPaidAmount, purchase.currency)}", "Extra paid so far: ${formatMoney(purchase.totalPaidAmount, purchase.currency)}"),
                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeAccent(isAppDark()))
                    )
                }
                Text(
                    text = tr("Saldo pendiente: ${formatMoney(purchase.remainingBalanceToPay, purchase.currency)}", "Remaining balance: ${formatMoney(purchase.remainingBalanceToPay, purchase.currency)}"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                AmountTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = tr("Monto a abonar", "Payment amount"),
                    currency = purchase.currency,
                    prominent = true
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
                                text = formatMoney(newBalance, purchase.currency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.incomeAccent(isAppDark())
                                )
                            )
                        }
                    }
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
            Button(
                onClick = {
                    when (val check = checkExtraPayment(parsedAmount, purchase.remainingBalanceToPay, purchase.currency)) {
                        ExtraPaymentCheck.NotPositive ->
                            errorMessage = tr("Ingresa un monto de abono mayor a 0", "Enter a payment greater than 0")
                        ExtraPaymentCheck.ExceedsBalance ->
                            errorMessage = tr("El abono no puede ser mayor al saldo de ${formatMoney(purchase.remainingBalanceToPay, purchase.currency)}", "The payment can't be more than the balance of ${formatMoney(purchase.remainingBalanceToPay, purchase.currency)}")
                        is ExtraPaymentCheck.Valid -> onSave(check.amount)
                    }
                }
            ) {
                Text(tr("Guardar Abono", "Save payment"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}

@Composable
fun AddEditCardDialog(
    cardToEdit: CreditCardEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, cutoffDay: Int, paymentDay: Int, interestRate: Double, maintenanceFee: Double, currency: String, isFeeWaiverEnabled: Boolean, minSpendForFeeWaiver: Double, isFirstInstallmentInterestFree: Boolean, creditLimit: Double, colorIndex: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var cutoffDayText by remember { mutableStateOf(cardToEdit?.cutoffDay?.toString() ?: "15") }
    var paymentDayText by remember { mutableStateOf(cardToEdit?.paymentDay?.let { if (it > 0) it.toString() else "" } ?: "") }
    var interestRateText by remember { mutableStateOf(cardToEdit?.interestRate?.toString() ?: "0.0") }
    var maintenanceFeeText by remember { mutableStateOf(amountToInput(cardToEdit?.maintenanceFee)) }
    var creditLimitText by remember { mutableStateOf(amountToInput(cardToEdit?.creditLimit)) }
    // Color elegido; al editar una tarjeta sin color propio se parte del automático que ya tenía
    var selectedColorIndex by remember {
        mutableIntStateOf(
            when {
                cardToEdit == null -> 0
                cardToEdit.colorIndex in CardPalettes.indices -> cardToEdit.colorIndex
                else -> autoCardColorIndex(cardToEdit.id)
            }
        )
    }
    val currency = "COP"

    var isFeeWaiverEnabled by remember { mutableStateOf(cardToEdit?.isFeeWaiverEnabled ?: false) }
    var minSpendText by remember { mutableStateOf(amountToInput(cardToEdit?.minSpendForFeeWaiver)) }
    var isFirstInstallmentInterestFree by remember { mutableStateOf(cardToEdit?.isFirstInstallmentInterestFree ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (cardToEdit == null) tr("Agregar Tarjeta", "Add card") else tr("Configurar Tarjeta", "Card settings"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("Nombre de la Tarjeta", "Card name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                CardColorPicker(
                    cardName = name.ifBlank { tr("Mi tarjeta", "My card") },
                    selectedIndex = selectedColorIndex,
                    onSelect = { selectedColorIndex = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cutoffDayText,
                        onValueChange = { cutoffDayText = it },
                        label = { Text(tr("Día de corte (1-31)", "Statement day (1-31)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paymentDayText,
                        onValueChange = { paymentDayText = it },
                        label = { Text(tr("Día de pago (1-31)", "Payment day (1-31)")) },
                        placeholder = { Text(tr("Opcional", "Optional")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic cycle hint
                val parsedCutoff = cutoffDayText.toIntOrNull()
                val parsedPayment = paymentDayText.toIntOrNull() ?: 0
                if (parsedCutoff != null && parsedCutoff in 1..31) {
                    val cycleHint = when {
                        parsedPayment in 1..31 -> {
                            if (parsedPayment > parsedCutoff) tr("💳 Corte día $parsedCutoff y pago día $parsedPayment: compras del ciclo se pagan este mismo mes.", "💳 Statement on day $parsedCutoff and payment on day $parsedPayment: purchases in the cycle are paid the same month.")
                            else tr("💳 Corte día $parsedCutoff y pago día $parsedPayment: compras del ciclo se pagan al mes siguiente.", "💳 Statement on day $parsedCutoff and payment on day $parsedPayment: purchases in the cycle are paid the following month.")
                        }
                        parsedCutoff >= 28 -> tr("💳 Corte día $parsedCutoff (fin de mes): compras se pagan al mes siguiente.", "💳 Statement on day $parsedCutoff (end of month): purchases are paid the following month.")
                        else -> tr("💳 Corte día $parsedCutoff: compras se pagan en el mismo mes.", "💳 Statement on day $parsedCutoff: purchases are paid the same month.")
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cycleHint,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                AmountTextField(
                    value = creditLimitText,
                    onValueChange = { creditLimitText = it },
                    label = tr("Cupo total de la tarjeta (opcional)", "Card credit limit (optional)"),
                    supportingText = { Text(tr("Para mostrar cuánto cupo llevas usado", "To show how much of your limit you've used")) }
                )

                AmountTextField(
                    value = maintenanceFeeText,
                    onValueChange = { maintenanceFeeText = it },
                    label = tr("Cuota de manejo", "Card fee"),
                    placeholder = "0"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("Exoneración por consumo mínimo", "Fee waiver for minimum spending"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Si el cupo gastado total alcanza un monto mínimo, la cuota es $0", "If your total spending reaches a minimum amount, the fee is $0"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isFeeWaiverEnabled,
                        onCheckedChange = { isFeeWaiverEnabled = it }
                    )
                }

                if (isFeeWaiverEnabled) {
                    AmountTextField(
                        value = minSpendText,
                        onValueChange = { minSpendText = it },
                        label = tr("Cupo gastado mínimo para exonerar", "Minimum spending to waive the fee")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("1ª Cuota Sin Interés (0%)", "1st installment interest-free (0%)"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("La primera cuota de cualquier compra no cobra intereses", "The first installment of any purchase has no interest"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isFirstInstallmentInterestFree,
                        onCheckedChange = { isFirstInstallmentInterestFree = it }
                    )
                }

                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text(tr("Tasa de interés % mensual (ej. 2.2)", "Monthly interest rate % (e.g. 2.2)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
            Button(
                onClick = {
                    val cutoffDay = cutoffDayText.toIntOrNull()
                    val paymentDay = paymentDayText.toIntOrNull() ?: 0
                    // La tasa se lee como porcentaje: "1.875" es 1,875 %, no 1875
                    val interestRate = parsePercentRate(interestRateText)
                    val maintenanceFee = parseAmount(maintenanceFeeText) ?: 0.0
                    val minSpend = if (isFeeWaiverEnabled) parseAmount(minSpendText) ?: 0.0 else 0.0
                    val creditLimit = parseAmount(creditLimitText) ?: 0.0

                    if (name.isBlank()) {
                        errorMessage = tr("Por favor ingresa un nombre para la tarjeta", "Enter a name for the card")
                    } else if (cutoffDay == null || cutoffDay !in 1..31) {
                        errorMessage = tr("El día de corte debe ser entre 1 y 31", "The statement day must be between 1 and 31")
                    } else if (paymentDay !in 0..31) {
                        errorMessage = tr("El día de pago debe ser entre 1 y 31 (o déjalo vacío)", "The payment day must be between 1 and 31 (or leave it empty)")
                    } else if (interestRate == null) {
                        errorMessage = tr("La tasa de interés debe ser un número entre 0 y 100", "The interest rate must be a number between 0 and 100")
                    } else if (isFeeWaiverEnabled && minSpend <= 0) {
                        errorMessage = tr("Ingresa un monto mínimo de consumo válido para la exoneración", "Enter a valid minimum spending amount for the fee waiver")
                    } else {
                        onSave(name.trim(), cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, selectedColorIndex)
                    }
                }
            ) {
                Text(tr("Guardar", "Save"))
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(tr("Eliminar", "Delete"), color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text(tr("Cancelar", "Cancel"))
                }
            }
        }
    )
}

@Composable
fun AddEditPurchaseDialog(
    cardName: String,
    cardInterestRate: Double = 0.0,
    cardFirstInstallmentInterestFree: Boolean = false,
    purchaseToEdit: PurchaseInstallmentDetail?,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSave: (concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit
) {
    var concept by remember { mutableStateOf(purchaseToEdit?.concept ?: "") }
    var totalAmountText by remember { mutableStateOf(amountToInput(purchaseToEdit?.totalAmount, purchaseToEdit?.currency == "USD")) }
    var installmentsText by remember { mutableStateOf(purchaseToEdit?.totalInstallments?.toString() ?: "1") }
    var isInterestFree by remember { mutableStateOf(purchaseToEdit?.isInterestFree ?: false) }
    var isRecurring by remember { mutableStateOf(purchaseToEdit?.isRecurring ?: false) }
    var currency by remember { mutableStateOf(purchaseToEdit?.currency ?: "COP") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Compra nueva: fecha real de hoy. El mes en que se cobra lo decide el día de corte de la tarjeta.
    val initialDateMillis = remember(purchaseToEdit) {
        purchaseToEdit?.purchaseDateMillis ?: System.currentTimeMillis()
    }
    var purchaseDateMillis by remember(purchaseToEdit, selectedYearMonth) { mutableLongStateOf(initialDateMillis) }

    val context = LocalContext.current
    val datePickerDialog = rememberDatePicker(purchaseDateMillis) { purchaseDateMillis = it }

    val purchaseDateFormat = remember { longDateFormat() }

    // Live preview calculation of monthly quota
    val parsedTotalAmount = parseAmount(totalAmountText) ?: 0.0
    val parsedInstallments = installmentsText.toIntOrNull() ?: 1
    // Mismo cálculo que la compra real (cuota regular; la 1ª puede ir sin interés)
    val previewMonthlyPayment = remember(parsedTotalAmount, parsedInstallments, isInterestFree, cardInterestRate, cardFirstInstallmentInterestFree) {
        if (parsedTotalAmount <= 0.0) 0.0
        else installmentSchedule(parsedTotalAmount, parsedInstallments, cardInterestRate, isInterestFree, cardFirstInstallmentInterestFree).last()
    }

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (purchaseToEdit == null) tr("Nueva Compra en $cardName", "New purchase on $cardName") else tr("Editar Compra / Cuotas", "Edit purchase / installments"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(tr("Concepto de la compra", "Purchase name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AmountTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = "Monto total",
                    currency = currency,
                    prominent = true
                )

                Text(
                    text = tr("Moneda de la compra:", "Purchase currency:"),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = currency == "COP",
                        onClick = { currency = "COP" }
                    )
                    Text("COP ($)", modifier = Modifier.clickable { currency = "COP" })

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = currency == "USD",
                        onClick = { currency = "USD" }
                    )
                    Text("USD ($)", modifier = Modifier.clickable { currency = "USD" })
                }

                // Date Picker Field
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
                                text = tr("Fecha de la compra", "Purchase date"),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = purchaseDateFormat.format(Date(purchaseDateMillis)),
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

                OutlinedTextField(
                    value = installmentsText,
                    onValueChange = { installmentsText = it },
                    label = { Text(tr("Número de cuotas (ej. 1, 3, 6, 12, 24)", "Number of installments (e.g. 1, 3, 6, 12, 24)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsedInstallments > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("Exenta de intereses (0% interés)", "Interest-free (0% interest)"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tr("No aplica tasa de interés en esta compra", "No interest rate applies to this purchase"),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = isInterestFree,
                            onCheckedChange = { isInterestFree = it }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("Recurrente mensual (ej. Suscripciones)", "Repeats monthly (e.g. subscriptions)"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Se cobra automáticamente mes a mes", "Charged automatically every month"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }

                // Live Preview Card
                if (parsedTotalAmount > 0.0 && parsedInstallments > 0) {
                    val previewCurrencySymbol = if (currency == "USD") "USD $" else "$"
                    Surface(
                        color = FinanceColors.cardContainer(isAppDark()),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tr("Cuota mensual estimada:", "Estimated monthly payment:"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.cardText(isAppDark()))
                            )
                            Text(
                                text = tr("${formatCurrency(previewMonthlyPayment, previewCurrencySymbol)} / mes (${parsedInstallments} cuota(s))", "${formatCurrency(previewMonthlyPayment, previewCurrencySymbol)} / month (${parsedInstallments} installment(s))"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.cardAccent(isAppDark())
                                )
                            )
                            if (!isInterestFree && cardInterestRate > 0 && parsedInstallments > 1) {
                                Text(
                                    text = tr("Incluye ${cardInterestRate}% interés mensual de la tarjeta", "Includes the card's ${cardInterestRate}% monthly interest"),
                                    style = MaterialTheme.typography.labelSmall.copy(color = FinanceColors.cardText(isAppDark()), fontSize = 11.sp)
                                )
                            }
                        }
                    }
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
            Button(
                onClick = {
                    val amount = parseAmount(totalAmountText)
                    val installments = installmentsText.toIntOrNull()

                    if (concept.isBlank()) {
                        errorMessage = tr("Ingresa el concepto de la compra", "Enter the purchase name")
                    } else if (amount == null || amount <= 0) {
                        errorMessage = tr("Ingresa un monto total válido mayor a 0", "Enter a valid total greater than 0")
                    } else if (installments == null || installments <= 0) {
                        errorMessage = tr("El número de cuotas debe ser al menos 1", "The number of installments must be at least 1")
                    } else {
                        val finalIsInterestFree = if (installments <= 1) false else isInterestFree
                        onSave(concept.trim(), amount, installments, purchaseDateMillis, finalIsInterestFree, isRecurring, currency)
                    }
                }
            ) {
                Text(tr("Guardar Compra", "Save purchase"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}
