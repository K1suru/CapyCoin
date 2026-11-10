package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.currencySymbolOf
import com.example.gestiongastos.data.repository.yearMonthOf
import com.example.gestiongastos.data.model.toCents
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.gestiongastos.data.model.markedFirst
import com.example.gestiongastos.data.model.arrangeByRegistration
import com.example.gestiongastos.data.model.ExpenseInstallmentInfo
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.privacyBlur
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.ExpenseSortOrder
import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.AddEditSavingsDialog
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.data.model.maintenanceFeeInCop
import com.example.gestiongastos.data.model.isDeferredOrRecurring
import java.time.YearMonth

@Composable
fun ExpenseScreen(
    expenses: List<ExpenseEntity>,
    cardSummaries: List<CardMonthSummary>,
    savings: List<SavingsEntity>,
    allCards: List<CreditCardEntity>,
    selectedYearMonth: YearMonth,
    usdRate: Double = 4150.0,
    ignoredKeys: Set<String> = emptySet(),
    paidExpenseKeys: Set<String> = emptySet(),
    isPrivacyMode: Boolean = false,
    movePaidToTop: Boolean = true,
    installmentsPosition: InstallmentPosition = InstallmentPosition.LAST,
    onInstallmentsPositionSelected: (InstallmentPosition) -> Unit = {},
    expenseSortOrder: ExpenseSortOrder = ExpenseSortOrder.NEWEST_FIRST,
    onExpenseSortOrderSelected: (ExpenseSortOrder) -> Unit = {},
    openAddDialogRequest: Boolean = false,
    onAddDialogRequestHandled: () -> Unit = {},
    onToggleIgnoreExpense: (itemKey: String) -> Unit = {},
    onTogglePaidExpense: (itemKey: String) -> Unit = {},
    onAddExpense: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean, currency: String, installments: Int) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    expenseInstallments: Map<Long, ExpenseInstallmentInfo> = emptyMap(),
    onAddExpensePayment: (expenseId: Long, amount: Double) -> Unit = { _, _ -> },
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onEndExpenseFromMonth: (ExpenseEntity) -> Unit = {},
    onEndSavingFromMonth: (SavingsEntity) -> Unit = {},
    onAddCardPurchase: (cardId: Long, concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit,
    onUpdateCardPurchase: (CardPurchaseEntity) -> Unit,
    onDeleteCardPurchase: (purchaseId: Long) -> Unit,
    onAddSavings: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) -> Unit,
    onUpdateSavings: (SavingsEntity) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = Todos, 1 = Gastos Fijos, 2 = Tarjetas, 3 = Ahorros
    var searchQuery by remember { mutableStateOf("") }

    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToPay by remember { mutableStateOf<UnifiedExpenseItem.DirectExpense?>(null) }
    var purchaseToEdit by remember { mutableStateOf<PurchaseInstallmentDetail?>(null) }
    var savingToEdit by remember { mutableStateOf<SavingsEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<UnifiedExpenseItem?>(null) }

    // Solicitud externa (atajo del ícono) para abrir el formulario de nuevo gasto
    LaunchedEffect(openAddDialogRequest) {
        if (openAddDialogRequest) {
            showAddDialog = true
            onAddDialogRequestHandled()
        }
    }

    // Build unified expenses list
    val allUnifiedItems = remember(expenses, expenseInstallments, cardSummaries, savings, selectedYearMonth, installmentsPosition, expenseSortOrder, usdRate) {
        val rawList = mutableListOf<UnifiedExpenseItem>()

        // 1. Direct Expenses
        expenses.forEach { exp ->
            val expYm = yearMonthOf(exp.date)
            val installment = expenseInstallments[exp.id]
            val isCarriedOver = exp.isRecurring || expYm.isBefore(selectedYearMonth) || installment != null
            rawList.add(UnifiedExpenseItem.DirectExpense(exp, isCarriedOver, installment))
        }

        // 2. Card Purchases and Maintenance Fees
        cardSummaries.forEach { summary ->
            summary.purchases.filter { it.statusType == InstallmentStatusType.ACTIVE }.forEach { detail ->
                val symbol = currencySymbolOf(detail.currency)
                val isCarriedOver = detail.isDeferredOrRecurring(selectedYearMonth)
                rawList.add(UnifiedExpenseItem.CardInstallmentExpense(summary.cardName, detail, symbol, isCarriedOver))
            }
            val feeInCop = summary.maintenanceFeeInCop(usdRate)
            if (feeInCop > 0) {
                rawList.add(UnifiedExpenseItem.CardFeeExpense(summary.cardId, summary.cardName, feeInCop, "$"))
            }
        }

        // 3. Savings
        savings.forEach { sav ->
            val savYm = yearMonthOf(sav.date)
            val isCarriedOver = sav.isRecurring || savYm.isBefore(selectedYearMonth)
            rawList.add(UnifiedExpenseItem.SavingsExpense(sav, isCarriedOver))
        }

        // Lo del mes y lo arrastrado siguen la preferencia del usuario (ver registrationOrder)
        arrangeByRegistration(rawList, registrationOrder, expenseSortOrder, installmentsPosition) { it.isCarriedOver }
    }

    val filteredItems = remember(allUnifiedItems, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> allUnifiedItems.filterIsInstance<UnifiedExpenseItem.DirectExpense>()
            2 -> allUnifiedItems.filter { it is UnifiedExpenseItem.CardInstallmentExpense || it is UnifiedExpenseItem.CardFeeExpense }
            3 -> allUnifiedItems.filterIsInstance<UnifiedExpenseItem.SavingsExpense>()
            else -> allUnifiedItems
        }
    }

    val displayedItems = remember(filteredItems, searchQuery, paidExpenseKeys, movePaidToTop) {
        val searched = if (searchQuery.isBlank()) {
            filteredItems
        } else {
            val q = searchQuery.trim().lowercase()
            val qDigits = q.filter { it.isDigit() }
            filteredItems.filter { item ->
                // Monto sin notación científica ni decimales (1.2E7 -> 12000000)
                val plainAmount = java.math.BigDecimal(item.amount).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString()
                item.concept.lowercase().contains(q) ||
                (qDigits.isNotEmpty() && plainAmount.contains(qDigits))
            }
        }
        // Los pagados se suben dentro de cada grupo (ver sections), no en toda la lista
        searched
    }

    val totalExpensesSum = remember(allUnifiedItems, usdRate, ignoredKeys) {
        allUnifiedItems
            .filter { it.key !in ignoredKeys }
            .sumOf { it.amountInCop(usdRate) }
    }

    // Lista agrupada: "Hoy", "Ayer", días del mes y el bloque de cuotas/fijos/recurrentes
    val sections = remember(displayedItems, paidExpenseKeys, movePaidToTop) {
        groupExpenseItems(displayedItems).map { (title, items) ->
            title to markedFirst(items, movePaidToTop) { it.key in paidExpenseKeys }
        }
    }

    val isDark = isAppDark()

    // Al cambiar el orden la lista vuelve al inicio; si no, se quedaba anclada donde estaba
    // y parecía que el orden no había cambiado.
    val expenseListState = rememberLazyListState()
    var appliedSortOrder by remember { mutableStateOf(expenseSortOrder) }
    LaunchedEffect(expenseSortOrder) {
        if (appliedSortOrder != expenseSortOrder) {
            appliedSortOrder = expenseSortOrder
            expenseListState.scrollToItem(0)
        }
    }

    Scaffold(
        modifier = modifier,
        // El Scaffold principal ya aplica las barras del sistema; sin esto quedaba un espacio en blanco arriba
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FinanceColors.expenseAccent(isDark),
                contentColor = FinanceColors.onAccent(isDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = tr("Nuevo Gasto", "New expense"))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Registrar Gasto", "Add expense"), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Total Monthly Expense Minimalist Bento Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FinanceColors.expenseContainer(isDark).copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, FinanceColors.expenseContainer(isDark)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .capyPattern(FinanceColors.expenseAccent(isDark).copy(alpha = 0.10f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.capy_poor),
                                contentDescription = tr("Capibara Gastos", "Expenses capybara"),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tr("Gastos de ${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }}", "${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }} expenses"),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.expenseText(isDark)
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = tr("Fijos + Tarjetas + Ahorros", "Fixed + Cards + Savings"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FinanceColors.expenseAccent(isDark),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AutoResizedText(
                        text = privateText(formatCurrency(rememberAnimatedAmount(totalExpensesSum)), isPrivacyMode, "$ ••••••"),
                        isPrivate = isPrivacyMode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FinanceColors.expenseText(isDark)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(tr("Buscar por concepto o monto...", "Search by name or amount..."), style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = tr("Buscar", "Search"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = tr("Limpiar búsqueda", "Clear search"),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(22.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.25f else 0.15f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Modern Pill-Shaped Filter Row
            val filters = listOf(tr("Todos", "All"), tr("Gastos Fijos", "Fixed"), tr("Tarjetas", "Cards"), tr("Ahorros", "Savings"))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEachIndexed { index, label ->
                    val isSelected = selectedFilterIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { selectedFilterIndex = index },
                        shape = CircleShape,
                        color = if (isSelected) FinanceColors.expenseAccent(isDark) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) FinanceColors.onAccent(isDark) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sort & Info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("${displayedItems.size} registro(s)", "${displayedItems.size} item(s)"),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        onExpenseSortOrderSelected(
                            if (expenseSortOrder == ExpenseSortOrder.NEWEST_FIRST) ExpenseSortOrder.OLDEST_FIRST
                            else ExpenseSortOrder.NEWEST_FIRST
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = tr("Cambiar orden", "Change order"),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (expenseSortOrder == ExpenseSortOrder.NEWEST_FIRST) tr("Nuevos arriba", "Newest first") else tr("Antiguos arriba", "Oldest first"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // List of items
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.capy_savings),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) tr("¡No se encontraron coincidencias!", "No matches found!") else tr("¡Sin gastos en esta categoría!", "No expenses in this category!"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) tr("Prueba con otra palabra clave o monto.", "Try another keyword or amount.") else tr("¡Tus finanzas están relajadas como un capibara! ✨", "Your finances are as relaxed as a capybara! ✨"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = expenseListState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    sections.forEach { (title, sectionItems) ->
                    item(key = "header_$title") {
                        val sectionTotal = sectionItems.filter { it.key !in ignoredKeys }.sumOf { it.amountInCop(usdRate) }
                        SectionHeader(
                            title = title,
                            amountText = privateText(formatCurrency(sectionTotal), isPrivacyMode),
                            isPrivacyMode = isPrivacyMode
                        )
                    }
                    items(sectionItems, key = { it.key }) { item ->
                        ExpenseItemCard(
                            item = item,
                            usdRate = usdRate,
                            isIgnored = item.key in ignoredKeys,
                            isPaid = item.key in paidExpenseKeys,
                            isPrivacyMode = isPrivacyMode,
                            onToggleIgnore = { onToggleIgnoreExpense(item.key) },
                            onTogglePaid = { onTogglePaidExpense(item.key) },
                            onAddPayment = if (item is UnifiedExpenseItem.DirectExpense && item.installment?.isPaidOff == false) {
                                { expenseToPay = item }
                            } else null,
                            onEdit = {
                                when (item) {
                                    // En cuotas, `amount` es el cobro del mes: se edita con el valor fijo de la cuota
                                    is UnifiedExpenseItem.DirectExpense -> expenseToEdit = item.installment
                                        ?.let { item.expense.copy(amountCents = toCents(it.installmentAmount)) } ?: item.expense
                                    is UnifiedExpenseItem.CardInstallmentExpense -> purchaseToEdit = item.detail
                                    is UnifiedExpenseItem.SavingsExpense -> savingToEdit = item.saving
                                    is UnifiedExpenseItem.CardFeeExpense -> { /* Read-only fee */ }
                                }
                            },
                            onDelete = {
                                if (item !is UnifiedExpenseItem.CardFeeExpense) itemToDelete = item
                            }
                        )
                    }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditExpenseDialog(
            allCards = allCards,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { showAddDialog = false },
            onSaveExpense = { concept, amount, dateMillis, isRecurring, currency, installments ->
                onAddExpense(concept, amount, dateMillis, isRecurring, currency, installments)
                showAddDialog = false
            },
            onSaveCardPurchase = { cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency ->
                onAddCardPurchase(cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency)
                showAddDialog = false
            },
            onSaveSavings = { concept, amount, dateMillis, isRecurring ->
                onAddSavings(concept, amount, dateMillis, isRecurring)
                showAddDialog = false
            }
        )
    }

    if (expenseToEdit != null) {
        AddEditDirectExpenseDialog(
            expenseToEdit = expenseToEdit,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { expenseToEdit = null },
            onSave = { updated ->
                onUpdateExpense(updated)
                expenseToEdit = null
            }
        )
    }

    expenseToPay?.let { item ->
        item.installment?.let { info ->
            ExpensePaymentDialog(
                expense = item.expense,
                info = info,
                onDismiss = { expenseToPay = null },
                onSave = { amount ->
                    onAddExpensePayment(item.expense.id, amount)
                    expenseToPay = null
                }
            )
        }
    }

    if (savingToEdit != null) {
        AddEditSavingsDialog(
            savingToEdit = savingToEdit,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { savingToEdit = null },
            onSave = { updated ->
                onUpdateSavings(updated)
                savingToEdit = null
            }
        )
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            itemName = item.concept,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { itemToDelete = null },
            onDeleteAll = {
                when (item) {
                    is UnifiedExpenseItem.DirectExpense -> onDeleteExpense(item.expense)
                    is UnifiedExpenseItem.CardInstallmentExpense -> onDeleteCardPurchase(item.detail.purchaseId)
                    is UnifiedExpenseItem.SavingsExpense -> onDeleteSavings(item.saving)
                    is UnifiedExpenseItem.CardFeeExpense -> { /* Read-only fee */ }
                }
            },
            onEndFromMonth = when {
                item is UnifiedExpenseItem.DirectExpense && item.expense.isRecurring -> ({ onEndExpenseFromMonth(item.expense) })
                item is UnifiedExpenseItem.SavingsExpense && item.saving.isRecurring -> ({ onEndSavingFromMonth(item.saving) })
                else -> null
            },
            extraMessage = if (item is UnifiedExpenseItem.DirectExpense && item.installment != null) {
                tr("Se eliminará la deuda completa con todas sus cuotas y abonos.", "The whole debt will be deleted, with all its installments and extra payments.")
            } else if (item is UnifiedExpenseItem.CardInstallmentExpense) {
                tr("Se eliminará la compra completa con todas sus cuotas y abonos. Para saltar solo este mes usa \"Omitir este Mes\" en Tarjetas.", "The whole purchase will be deleted, with all its installments and payments. To skip just this month, use \"Skip this month\" in Cards.")
            } else null
        )
    }

    purchaseToEdit?.let { editing ->
        val card = allCards.find { it.id == editing.cardId }
        AddEditPurchaseDialog(
            cardName = card?.name ?: tr("Tarjeta", "Card"),
            cardInterestRate = card?.interestRate ?: 0.0,
            cardFirstInstallmentInterestFree = card?.isFirstInstallmentInterestFree ?: false,
            purchaseToEdit = editing,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { purchaseToEdit = null },
            onSave = { concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency ->
                onUpdateCardPurchase(
                    CardPurchaseEntity(
                        id = editing.purchaseId,
                        cardId = editing.cardId,
                        concept = concept,
                        totalAmountCents = toCents(totalAmount),
                        installments = installments,
                        purchaseDate = dateMillis,
                        isInterestFree = isInterestFree,
                        isRecurring = isRecurring,
                        currency = currency
                    )
                )
                purchaseToEdit = null
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, amountText: String? = null, isPrivacyMode: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        if (amountText != null) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.privacyBlur(isPrivacyMode)
            )
        }
    }
}
