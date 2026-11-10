package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.formatUsdWithCop
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import androidx.compose.material3.Button
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.repository.yearMonthOf
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.MonthYearPickerDialog
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle

import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark

// Formulario para agregar o editar un ingreso (único, en cuotas, recurrente o reembolso).
private enum class IncomeMode {
    UNIQUE,
    INSTALLMENTS,
    RECURRING
}

@Composable
fun AddEditIncomeDialog(
    incomeToEdit: IncomeEntity?,
    expenses: List<ExpenseEntity>,
    cardSummaries: List<CardMonthSummary>,
    usdRate: Double = DEFAULT_USD_RATE,
    defaultYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSave: (
        concept: String,
        amount: Double,
        dateMillis: Long,
        isRecurring: Boolean,
        linkedExpenseIdsText: String?,
        linkedCardPurchaseIdsText: String?,
        extraAmount: Double,
        extraConcept: String?,
        totalInstallments: Int,
        totalAmount: Double,
        endDateMillis: Long?
    ) -> Unit
) {
    val isDark = isAppDark()
    var concept by remember { mutableStateOf(incomeToEdit?.concept ?: "") }

    val initialMode = when {
        (incomeToEdit?.totalInstallments ?: 1) > 1 -> IncomeMode.INSTALLMENTS
        incomeToEdit?.isRecurring == true -> IncomeMode.RECURRING
        else -> IncomeMode.UNIQUE
    }
    var incomeMode by remember { mutableStateOf(initialMode) }

    var amountText by remember {
        mutableStateOf(
            if ((incomeToEdit?.totalInstallments ?: 1) <= 1) {
                incomeToEdit?.amount?.let { kotlin.math.round(it).toLong().toString() } ?: ""
            } else ""
        )
    }

    var totalAmountText by remember {
        mutableStateOf(
            if ((incomeToEdit?.totalInstallments ?: 1) > 1) {
                val t = if ((incomeToEdit?.totalAmount ?: 0.0) > 0.0) {
                    incomeToEdit?.totalAmount ?: 0.0
                } else {
                    (incomeToEdit?.amount ?: 0.0) * (incomeToEdit?.totalInstallments ?: 1)
                }
                if (t > 0) kotlin.math.round(t).toLong().toString() else ""
            } else ""
        )
    }

    var installmentsCountText by remember {
        mutableStateOf(
            if ((incomeToEdit?.totalInstallments ?: 1) > 1) {
                incomeToEdit?.totalInstallments?.toString() ?: "3"
            } else {
                "3"
            }
        )
    }

    var hasEndDate by remember { mutableStateOf(incomeToEdit?.endDateMillis != null) }
    var endYearMonth by remember {
        mutableStateOf(
            incomeToEdit?.endDateMillis?.let { yearMonthOf(it) } ?: defaultYearMonth.plusMonths(3)
        )
    }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var selectedExpenseIds by remember { mutableStateOf(parseIdsSet(incomeToEdit?.linkedExpenseIdsText)) }
    var selectedCardPurchaseIds by remember { mutableStateOf(parseIdsSet(incomeToEdit?.linkedCardPurchaseIdsText)) }
    var extraAmountText by remember {
        mutableStateOf(
            incomeToEdit?.extraAmount?.let {
                if (it > 0) kotlin.math.round(it).toLong().toString() else ""
            } ?: ""
        )
    }
    var extraConceptText by remember { mutableStateOf(incomeToEdit?.extraConcept ?: "") }
    var isReimbursement by remember {
        mutableStateOf(
            selectedExpenseIds.isNotEmpty() || selectedCardPurchaseIds.isNotEmpty() || (incomeToEdit?.extraAmount ?: 0.0) > 0
        )
    }
    var showLinkPicker by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val generateSaveDate: () -> Long = {
        calculateSaveDateForMonth(defaultYearMonth, incomeToEdit?.date)
    }

    val allCardPurchases = remember(cardSummaries) {
        cardSummaries.flatMap { summary ->
            summary.purchases.map { detail -> Pair(summary.cardName, detail) }
        }
    }

    // Todo se suma en COP: los gastos y compras en USD se convierten con la TRM
    val calcLinkedSum = { expIds: Set<Long>, cardIds: Set<Long> ->
        val sumExpenses = expenses.filter { it.id in expIds }.sumOf { expenseAmountInCop(it, usdRate) }
        val sumPurchases = allCardPurchases.filter { it.second.purchaseId in cardIds }.sumOf { it.second.monthlyPaymentInCop(usdRate) }
        sumExpenses + sumPurchases
    }

    // Mes de inicio real del ingreso (al editar, el de su fecha; al crear, el mes seleccionado)
    val startYearMonth = remember(incomeToEdit, defaultYearMonth) {
        incomeToEdit?.let { yearMonthOf(it.date) } ?: defaultYearMonth
    }

    val recalculateAmount = { expIds: Set<Long>, cardIds: Set<Long>, extraStr: String ->
        val linked = calcLinkedSum(expIds, cardIds)
        val extra = parseAmount(extraStr) ?: 0.0
        val total = kotlin.math.round(linked + extra)
        if (total > 0 || linked > 0 || extra > 0) {
            amountText = total.toLong().toString()
        }
    }

    val selectedCount = selectedExpenseIds.size + selectedCardPurchaseIds.size

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (incomeToEdit == null) tr("Agregar Ingreso", "Add income") else tr("Editar Ingreso", "Edit income"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(tr("Nombre / Concepto del Ingreso", "Income name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isReimbursement) {
                    Text(
                        text = tr("Tipo de ingreso", "Income type"),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(IncomeMode.UNIQUE, tr("Único", "One-time"), Icons.AutoMirrored.Filled.TrendingUp),
                            Triple(IncomeMode.INSTALLMENTS, tr("En Cuotas", "Installments"), Icons.Default.DateRange),
                            Triple(IncomeMode.RECURRING, tr("Recurrente", "Recurring"), Icons.Default.Autorenew)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = incomeMode == mode
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { incomeMode = mode },
                                color = if (isSelected) FinanceColors.incomeContainer(isDark) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FinanceColors.incomeAccent(isDark) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (isSelected) FinanceColors.incomeAccent(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isReimbursement && incomeMode == IncomeMode.INSTALLMENTS) {
                    AmountTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = "Monto total a recibir",
                        prominent = true
                    )

                    OutlinedTextField(
                        value = installmentsCountText,
                        onValueChange = { installmentsCountText = it },
                        label = { Text(tr("Número de cuotas", "Number of installments")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(2, 3, 4, 6, 12).forEach { num ->
                            val isSelected = installmentsCountText == num.toString()
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { installmentsCountText = num.toString() },
                                color = if (isSelected) FinanceColors.incomeContainer(isDark) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) FinanceColors.incomeAccent(isDark) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$num",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val parsedTotal = parseAmount(totalAmountText) ?: 0.0
                    val parsedInstallments = installmentsCountText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val calculatedMonthly = if (parsedInstallments > 0) parsedTotal / parsedInstallments else 0.0
                    val endInstallmentYm = startYearMonth.plusMonths((parsedInstallments - 1).toLong())

                    Surface(
                        color = FinanceColors.incomeContainer(isDark).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, FinanceColors.incomeAccent(isDark).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tr("Cuota mensual:", "Monthly installment:"),
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = formatCurrency(kotlin.math.round(calculatedMonthly)),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceColors.incomeText(isDark)
                                    )
                                )
                            }
                            val startMonthName = startYearMonth.month.getDisplayName(TextStyle.SHORT, appLocale()).replaceFirstChar { it.uppercase() } + " " + startYearMonth.year
                            val endMonthName = endInstallmentYm.month.getDisplayName(TextStyle.SHORT, appLocale()).replaceFirstChar { it.uppercase() } + " " + endInstallmentYm.year
                            Text(
                                text = tr("Total $parsedInstallments cuotas • De $startMonthName a $endMonthName", "$parsedInstallments installments in total • From $startMonthName to $endMonthName"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else if (!isReimbursement && incomeMode == IncomeMode.RECURRING) {
                    AmountTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = if (incomeMode == IncomeMode.RECURRING && !isReimbursement) tr("Monto mensual", "Monthly amount") else tr("Monto", "Amount"),
                        prominent = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("¿Tiene fecha límite?", "Does it have an end date?"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tr("Finaliza en un mes específico", "Ends in a specific month"),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = hasEndDate,
                            onCheckedChange = { hasEndDate = it }
                        )
                    }

                    if (hasEndDate) {
                        val endMonthFull = endYearMonth.month.getDisplayName(TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() } + " " + endYearMonth.year
                        val nextMonthFull = endYearMonth.plusMonths(1).month.getDisplayName(TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() } + " " + endYearMonth.plusMonths(1).year

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndDatePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tr("Termina en:", "Ends in:"),
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = endMonthFull,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                                Text(
                                    text = tr("Cambiar", "Change"),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Text(
                            text = tr("Se recibirá cada mes hasta $endMonthFull. En $nextMonthFull ya no aparecerá.", "You'll receive it every month until $endMonthFull. It won't appear from $nextMonthFull on."),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                } else {
                    AmountTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = if (incomeMode == IncomeMode.RECURRING && !isReimbursement) tr("Monto mensual", "Monthly amount") else tr("Monto", "Amount"),
                        prominent = true
                    )

                    // Compact Reimbursement Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("Vincular como reembolso", "Link as a refund"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tr("Agrupa y devuelves uno o varios gastos", "Group one or more expenses that are paid back to you"),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = isReimbursement,
                            onCheckedChange = {
                                isReimbursement = it
                                if (it) {
                                    incomeMode = IncomeMode.UNIQUE
                                    showLinkPicker = true
                                } else {
                                    selectedExpenseIds = emptySet()
                                    selectedCardPurchaseIds = emptySet()
                                    extraAmountText = ""
                                    extraConceptText = ""
                                }
                            }
                        )
                    }

                    if (isReimbursement) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLinkPicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedCount > 0) tr("Vinculado a $selectedCount gasto(s)/compra(s)", "Linked to $selectedCount expense(s)/purchase(s)") else tr("Seleccionar Gastos / Compras...", "Select expenses / purchases..."),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tr("Seleccionar", "Select"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Extra section
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = tr("Extra", "Extra"),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                AmountTextField(
                                    value = extraAmountText,
                                    onValueChange = { newExtra ->
                                        extraAmountText = newExtra
                                        recalculateAmount(selectedExpenseIds, selectedCardPurchaseIds, newExtra)
                                    },
                                    label = "Monto extra",
                                    placeholder = "0"
                                )

                                OutlinedTextField(
                                    value = extraConceptText,
                                    onValueChange = { extraConceptText = it },
                                    label = { Text(tr("Detalle / Concepto extra", "Extra details")) },
                                    placeholder = { Text(tr("ej. Efectivo prestado", "e.g. Cash loaned")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
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
                    if (concept.isBlank()) {
                        errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                        return@Button
                    }

                    when {
                        isReimbursement -> {
                            val parsedAmount = parseAmount(amountText)
                            val amount = parsedAmount?.let { kotlin.math.round(it).toDouble() }
                            val extraAmt = parseAmount(extraAmountText)?.let { kotlin.math.round(it).toDouble() } ?: 0.0
                            val extraCpt = if (extraConceptText.isNotBlank()) extraConceptText.trim() else null

                            if (amount == null || amount <= 0) {
                                errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                                return@Button
                            }
                            onSave(
                                concept.trim(),
                                amount,
                                generateSaveDate(),
                                false,
                                formatIdsSet(selectedExpenseIds),
                                formatIdsSet(selectedCardPurchaseIds),
                                extraAmt,
                                extraCpt,
                                1,
                                0.0,
                                null
                            )
                        }

                        incomeMode == IncomeMode.INSTALLMENTS -> {
                            val total = parseAmount(totalAmountText)
                            val installments = installmentsCountText.toIntOrNull() ?: 0

                            if (total == null || total <= 0) {
                                errorMessage = tr("Ingresa un monto total válido mayor a 0", "Enter a valid total greater than 0")
                                return@Button
                            }
                            if (installments < 2) {
                                errorMessage = tr("El número de cuotas debe ser al menos 2", "The number of installments must be at least 2")
                                return@Button
                            }
                            val monthlyAmt = total / installments
                            onSave(
                                concept.trim(),
                                monthlyAmt,
                                generateSaveDate(),
                                false,
                                null,
                                null,
                                0.0,
                                null,
                                installments,
                                total,
                                null
                            )
                        }

                        incomeMode == IncomeMode.RECURRING -> {
                            val parsedAmount = parseAmount(amountText)
                            val amount = parsedAmount?.let { kotlin.math.round(it).toDouble() }

                            if (amount == null || amount <= 0) {
                                errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                                return@Button
                            }
                            if (hasEndDate && endYearMonth.isBefore(startYearMonth)) {
                                errorMessage = tr("La fecha límite no puede ser anterior al mes de inicio", "The end date can't be before the start month")
                                return@Button
                            }
                            val endDateMillis = if (hasEndDate) {
                                endYearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            } else null

                            onSave(
                                concept.trim(),
                                amount,
                                generateSaveDate(),
                                true,
                                null,
                                null,
                                0.0,
                                null,
                                1,
                                0.0,
                                endDateMillis
                            )
                        }

                        else -> { // IncomeMode.UNIQUE
                            val parsedAmount = parseAmount(amountText)
                            val amount = parsedAmount?.let { kotlin.math.round(it).toDouble() }

                            if (amount == null || amount <= 0) {
                                errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                                return@Button
                            }
                            onSave(
                                concept.trim(),
                                amount,
                                generateSaveDate(),
                                false,
                                null,
                                null,
                                0.0,
                                null,
                                1,
                                0.0,
                                null
                            )
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

    if (showEndDatePicker) {
        MonthYearPickerDialog(
            initialYearMonth = endYearMonth,
            onDismiss = { showEndDatePicker = false },
            onConfirm = { picked ->
                endYearMonth = picked
                showEndDatePicker = false
            }
        )
    }

    // Multi-select Picker Dialog
    if (showLinkPicker) {
        val updateSumAndConcept = { newExpenseIds: Set<Long>, newCardPurchaseIds: Set<Long> ->
            recalculateAmount(newExpenseIds, newCardPurchaseIds, extraAmountText)
            if (concept.isBlank()) {
                concept = tr("Reembolso", "Refund")
            }
        }

        AlertDialog(
            onDismissRequest = { showLinkPicker = false },
            title = {
                Text(tr("Seleccionar Gastos / Compras a Vincular", "Select expenses / purchases to link"), style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr("Puedes marcar varios ítems para un solo concepto (ej. Reembolso de gastos):", "You can check several items for a single entry (e.g. expense refund):"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (expenses.isEmpty() && allCardPurchases.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tr("No hay gastos fijos ni compras registradas en este mes.", "There are no fixed expenses or purchases this month."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(260.dp)
                        ) {
                            if (expenses.isNotEmpty()) {
                                item {
                                    Text(tr("Gastos Fijos:", "Fixed expenses:"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                items(expenses, key = { "exp_${it.id}" }) { exp ->
                                    val isChecked = exp.id in selectedExpenseIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val nextSet = if (isChecked) selectedExpenseIds - exp.id else selectedExpenseIds + exp.id
                                                selectedExpenseIds = nextSet
                                                updateSumAndConcept(nextSet, selectedCardPurchaseIds)
                                            }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val nextSet = if (checked == true) selectedExpenseIds + exp.id else selectedExpenseIds - exp.id
                                                selectedExpenseIds = nextSet
                                                updateSumAndConcept(nextSet, selectedCardPurchaseIds)
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(exp.concept, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (exp.currency == "USD") formatUsdWithCop(exp.amount, usdRate) else formatCurrency(exp.amount),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            if (allCardPurchases.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(tr("Compras con Tarjeta:", "Card purchases:"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                items(allCardPurchases, key = { "card_${it.second.purchaseId}" }) { (cardName, purchase) ->
                                    val isChecked = purchase.purchaseId in selectedCardPurchaseIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val nextSet = if (isChecked) selectedCardPurchaseIds - purchase.purchaseId else selectedCardPurchaseIds + purchase.purchaseId
                                                selectedCardPurchaseIds = nextSet
                                                updateSumAndConcept(selectedExpenseIds, nextSet)
                                            }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val nextSet = if (checked == true) selectedCardPurchaseIds + purchase.purchaseId else selectedCardPurchaseIds - purchase.purchaseId
                                                selectedCardPurchaseIds = nextSet
                                                updateSumAndConcept(selectedExpenseIds, nextSet)
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("[$cardName] ${purchase.concept}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(tr("Cuota mes: ${formatCurrency(purchase.monthlyPaymentInCop(usdRate))} | ${purchase.statusText}", "This month: ${formatCurrency(purchase.monthlyPaymentInCop(usdRate))} | ${purchase.statusText}"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkPicker = false }) {
                    Text(tr("Listo (${selectedExpenseIds.size + selectedCardPurchaseIds.size} seleccionados)", "Done (${selectedExpenseIds.size + selectedCardPurchaseIds.size} selected)"))
                }
            }
        )
    }
}
