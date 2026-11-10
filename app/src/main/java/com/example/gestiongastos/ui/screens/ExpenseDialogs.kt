package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.longDateFormat
import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.rememberDatePicker
import com.example.gestiongastos.ui.components.amountToInput
import androidx.compose.material3.Button
import com.example.gestiongastos.data.model.installmentSchedule
import androidx.compose.ui.platform.LocalContext
import java.time.YearMonth
import java.util.Date

// Formularios para registrar un gasto, una compra con tarjeta o un ahorro, y para editar un gasto fijo.
@Composable
fun AddEditExpenseDialog(
    allCards: List<CreditCardEntity>,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSaveExpense: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean, currency: String, installments: Int) -> Unit,
    onSaveCardPurchase: (cardId: Long, concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit,
    onSaveSavings: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Gasto Fijo, 1 = Compra Tarjeta, 2 = Ahorro

    // Form Gasto Fijo / Ahorro
    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var directCurrency by remember { mutableStateOf("COP") }
    var isInInstallments by remember { mutableStateOf(false) }
    var expenseInstallmentsText by remember { mutableStateOf("") }

    // Form Compra Tarjeta
    var selectedCardId by remember { mutableLongStateOf(allCards.firstOrNull()?.id ?: 0L) }
    var cardConcept by remember { mutableStateOf("") }
    var cardTotalAmountText by remember { mutableStateOf("") }
    var installmentsText by remember { mutableStateOf("1") }
    var isInterestFree by remember { mutableStateOf(false) }
    var isCardRecurring by remember { mutableStateOf(false) }
    var cardCurrency by remember { mutableStateOf("COP") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val initialDateMillis = remember(selectedYearMonth) {
        calculateSaveDateForMonth(selectedYearMonth)
    }
    var expenseDateMillis by remember(selectedYearMonth) { mutableLongStateOf(initialDateMillis) }

    val context = LocalContext.current
    val datePickerDialog = rememberDatePicker(expenseDateMillis) { expenseDateMillis = it }
    // Las compras con tarjeta guardan la fecha real; el mes de cobro lo decide el día de corte
    var cardDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val cardDatePickerDialog = rememberDatePicker(cardDateMillis) { cardDateMillis = it }

    val purchaseDateFormat = remember { longDateFormat() }

    val parsedTotalAmount = parseAmount(cardTotalAmountText) ?: 0.0
    val parsedInstallments = installmentsText.toIntOrNull() ?: 1

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = tr("Registrar Nuevo Gasto / Ahorro", "Add expense / savings"), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(tr("Gasto Fijo", "Fixed expense"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(tr("Tarjeta", "Card"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )

                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            errorMessage = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(tr("Ahorro", "Savings"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0 || selectedTab == 2) {
                    // Form Gasto Fijo or Ahorro
                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text(if (selectedTab == 0) tr("Nombre / Concepto del Gasto", "Expense name") else tr("Nombre / Concepto del Ahorro", "Savings name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AmountTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = if (selectedTab == 0 && isInInstallments) tr("Cuota mensual", "Monthly payment") else tr("Monto", "Amount"),
                        currency = if (selectedTab == 0) directCurrency else "COP",
                        prominent = true
                    )

                    if (selectedTab == 0) {
                        Text(
                            text = tr("Moneda del gasto:", "Expense currency:"),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = directCurrency == "COP",
                                onClick = { directCurrency = "COP" }
                            )
                            Text("COP ($)", modifier = Modifier.clickable { directCurrency = "COP" })

                            Spacer(modifier = Modifier.width(16.dp))

                            RadioButton(
                                selected = directCurrency == "USD",
                                onClick = { directCurrency = "USD" }
                            )
                            Text("USD ($)", modifier = Modifier.clickable { directCurrency = "USD" })
                        }
                    }

                    // Date Picker Field for Gasto Fijo / Ahorro
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
                                    text = if (selectedTab == 0) tr("Fecha del gasto", "Expense date") else tr("Fecha del ahorro", "Savings date"),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = purchaseDateFormat.format(Date(expenseDateMillis)),
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

                    if (selectedTab == 0) {
                        ExpenseInstallmentsFields(
                            enabled = isInInstallments,
                            onEnabledChange = { isInInstallments = it },
                            countText = expenseInstallmentsText,
                            onCountTextChange = { expenseInstallmentsText = it },
                            installmentAmount = parseAmount(amountText) ?: 0.0,
                            currency = directCurrency,
                            startDateMillis = expenseDateMillis
                        )
                    }

                    if (selectedTab == 2 || !isInInstallments) Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("Recurrente mensual", "Repeats monthly"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tr("Se aplicará a los meses siguientes", "It will apply to the following months"),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                    }
                } else {
                    // Form Compra Tarjeta
                    if (allCards.isEmpty()) {
                        Text(
                            text = tr("No tienes tarjetas de crédito configuradas. Agrega una en la pestaña Tarjetas.", "You don't have any credit cards yet. Add one in the Cards tab."),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = tr("Seleccionar Tarjeta:", "Select card:"),
                            style = MaterialTheme.typography.labelLarge
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            allCards.forEach { card ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedCardId = card.id }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedCardId == card.id,
                                        onClick = { selectedCardId = card.id }
                                    )
                                    Text(
                                        text = card.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selectedCardId == card.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = cardConcept,
                            onValueChange = { cardConcept = it },
                            label = { Text(tr("Concepto de la compra", "Purchase name")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        AmountTextField(
                            value = cardTotalAmountText,
                            onValueChange = { cardTotalAmountText = it },
                            label = "Monto total",
                            currency = cardCurrency,
                            prominent = true
                        )

                        Text(
                            text = tr("Moneda de la compra:", "Purchase currency:"),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = cardCurrency == "COP",
                                onClick = { cardCurrency = "COP" }
                            )
                            Text("COP ($)", modifier = Modifier.clickable { cardCurrency = "COP" })

                            Spacer(modifier = Modifier.width(16.dp))

                            RadioButton(
                                selected = cardCurrency == "USD",
                                onClick = { cardCurrency = "USD" }
                            )
                            Text("USD ($)", modifier = Modifier.clickable { cardCurrency = "USD" })
                        }

                        // Date Picker Field for Card Purchase
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
                                        text = purchaseDateFormat.format(Date(cardDateMillis)),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                                IconButton(
                                    onClick = { cardDatePickerDialog.show() },
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
                            label = { Text(tr("Número de cuotas (ej. 1, 3, 12)", "Number of installments (e.g. 1, 3, 12)")) },
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
                                checked = isCardRecurring,
                                onCheckedChange = { isCardRecurring = it }
                            )
                        }

                        if (parsedTotalAmount > 0.0 && parsedInstallments > 0) {
                            val previewSymbol = if (cardCurrency == "USD") "USD $" else "$"
                            // Mismo cálculo que la compra real (incluye el interés de la tarjeta)
                            val selectedCard = allCards.find { it.id == selectedCardId }
                            val schedule = installmentSchedule(
                                principal = parsedTotalAmount,
                                installments = parsedInstallments,
                                monthlyRatePercent = selectedCard?.interestRate ?: 0.0,
                                isInterestFree = isInterestFree,
                                isFirstInstallmentInterestFree = selectedCard?.isFirstInstallmentInterestFree ?: false
                            )
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
                                        text = tr("${formatCurrency(schedule.last(), previewSymbol)} / mes", "${formatCurrency(schedule.last(), previewSymbol)} / month"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = FinanceColors.cardAccent(isAppDark())
                                        )
                                    )
                                }
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
                    if (selectedTab == 0) {
                        val amount = parseAmount(amountText)
                        val installments = if (isInInstallments) parseExpenseInstallments(expenseInstallmentsText) else 0
                        if (concept.isBlank()) {
                            errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                        } else if (amount == null || amount <= 0) {
                            errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                        } else if (installments == null) {
                            errorMessage = tr("Ingresa un número de cuotas entre 1 y $MAX_EXPENSE_INSTALLMENTS", "Enter a number of installments between 1 and $MAX_EXPENSE_INSTALLMENTS")
                        } else {
                            onSaveExpense(concept.trim(), amount, expenseDateMillis, isRecurring && installments == 0, directCurrency, installments)
                        }
                    } else if (selectedTab == 1) {
                        val totalAmount = parseAmount(cardTotalAmountText)
                        val installments = installmentsText.toIntOrNull()

                        if (selectedCardId == 0L) {
                            errorMessage = tr("Selecciona una tarjeta de crédito", "Select a credit card")
                        } else if (cardConcept.isBlank()) {
                            errorMessage = tr("Ingresa un concepto de compra", "Enter a purchase name")
                        } else if (totalAmount == null || totalAmount <= 0) {
                            errorMessage = tr("Ingresa un monto total válido mayor a 0", "Enter a valid total greater than 0")
                        } else if (installments == null || installments <= 0) {
                            errorMessage = tr("Ingresa al menos 1 cuota", "Enter at least 1 installment")
                        } else {
                            val finalIsInterestFree = if (installments <= 1) false else isInterestFree
                            onSaveCardPurchase(selectedCardId, cardConcept.trim(), totalAmount, installments, cardDateMillis, finalIsInterestFree, isCardRecurring, cardCurrency)
                        }
                    } else {
                        val amount = parseAmount(amountText)
                        if (concept.isBlank()) {
                            errorMessage = tr("Por favor ingresa un concepto para el ahorro", "Enter a name for the savings")
                        } else if (amount == null || amount <= 0) {
                            errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                        } else {
                            onSaveSavings(concept.trim(), amount, expenseDateMillis, isRecurring)
                        }
                    }
                }
            ) {
                Text(tr("Guardar", "Save"))
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
fun AddEditDirectExpenseDialog(
    expenseToEdit: ExpenseEntity?,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var concept by remember { mutableStateOf(expenseToEdit?.concept ?: "") }
    var amountText by remember { mutableStateOf(amountToInput(expenseToEdit?.amount, expenseToEdit?.currency == "USD")) }
    var isRecurring by remember { mutableStateOf(expenseToEdit?.isRecurring ?: false) }
    var currency by remember { mutableStateOf(expenseToEdit?.currency ?: "COP") }
    var isInInstallments by remember { mutableStateOf((expenseToEdit?.installments ?: 0) > 0) }
    var installmentsText by remember { mutableStateOf(expenseToEdit?.installments?.takeIf { it > 0 }?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val initialDateMillis = remember(expenseToEdit, selectedYearMonth) {
        calculateSaveDateForMonth(selectedYearMonth, expenseToEdit?.date)
    }
    var expenseDateMillis by remember(expenseToEdit, selectedYearMonth) { mutableLongStateOf(initialDateMillis) }

    val context = LocalContext.current
    val datePickerDialog = rememberDatePicker(expenseDateMillis) { expenseDateMillis = it }
    val expenseDateFormat = remember { longDateFormat() }

    FormSheet(
        onDismissRequest = onDismiss,
        title = { Text(if (expenseToEdit == null) tr("Nuevo Gasto Fijo", "New fixed expense") else tr("Editar Gasto Fijo", "Edit fixed expense")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(tr("Concepto del Gasto", "Expense name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AmountTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = if (isInInstallments) tr("Cuota mensual", "Monthly payment") else tr("Monto", "Amount"),
                    currency = currency,
                    prominent = true
                )

                Text(
                    text = tr("Moneda del gasto:", "Expense currency:"),
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

                // Date Picker Field for Gasto Fijo
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
                                text = tr("Fecha del gasto", "Expense date"),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = expenseDateFormat.format(Date(expenseDateMillis)),
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

                ExpenseInstallmentsFields(
                    enabled = isInInstallments,
                    onEnabledChange = { isInInstallments = it },
                    countText = installmentsText,
                    onCountTextChange = { installmentsText = it },
                    installmentAmount = parseAmount(amountText) ?: 0.0,
                    currency = currency,
                    startDateMillis = expenseDateMillis
                )

                if (!isInInstallments) Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tr("Recurrente mensual", "Repeats monthly"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Se aplicará a los meses siguientes", "It will apply to the following months"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
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
                    val amount = parseAmount(amountText)
                    val installments = if (isInInstallments) parseExpenseInstallments(installmentsText) else 0
                    if (concept.isBlank()) {
                        errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                    } else if (amount == null || amount <= 0) {
                        errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                    } else if (installments == null) {
                        errorMessage = tr("Ingresa un número de cuotas entre 1 y $MAX_EXPENSE_INSTALLMENTS", "Enter a number of installments between 1 and $MAX_EXPENSE_INSTALLMENTS")
                    } else {
                        val base = expenseToEdit ?: ExpenseEntity(concept = concept.trim(), amountCents = toCents(amount), date = expenseDateMillis, isRecurring = isRecurring, currency = currency)
                        onSave(base.copy(concept = concept.trim(), amountCents = toCents(amount), date = expenseDateMillis, isRecurring = isRecurring && installments == 0, currency = currency, installments = installments))
                    }
                }
            ) {
                Text(tr("Guardar", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}
