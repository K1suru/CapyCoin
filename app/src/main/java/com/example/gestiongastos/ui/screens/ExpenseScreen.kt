package com.example.gestiongastos.ui.screens

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.AddEditSavingsDialog
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.ui.components.PawCheckButton
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.rememberDatePicker
import com.example.gestiongastos.ui.components.amountToInput
import androidx.compose.material3.Button
import com.example.gestiongastos.data.model.maintenanceFeeInCop
import com.example.gestiongastos.data.model.installmentSchedule
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class UnifiedExpenseItem {
    abstract val key: String
    abstract val concept: String
    abstract val amount: Double
    abstract val dateMillis: Long
    abstract val isCarriedOver: Boolean

    data class DirectExpense(
        val expense: ExpenseEntity,
        override val isCarriedOver: Boolean,
        val installment: ExpenseInstallmentInfo? = null // solo si es un gasto fijo en cuotas
    ) : UnifiedExpenseItem() {
        override val key: String = "direct_${expense.id}"
        override val concept: String = expense.concept
        override val amount: Double = expense.amount
        override val dateMillis: Long = expense.date
    }

    data class CardInstallmentExpense(
        val cardName: String,
        val detail: PurchaseInstallmentDetail,
        val currencySymbol: String,
        override val isCarriedOver: Boolean
    ) : UnifiedExpenseItem() {
        override val key: String = "card_inst_${detail.purchaseId}"
        override val concept: String = detail.concept
        override val amount: Double = detail.monthlyPayment
        override val dateMillis: Long = detail.purchaseDateMillis
    }

    data class CardFeeExpense(
        val cardId: Long,
        val cardName: String,
        val fee: Double,
        val currencySymbol: String
    ) : UnifiedExpenseItem() {
        override val key: String = "card_fee_${cardId}"
        override val concept: String = tr("Cuota de manejo ($cardName)", "Card fee ($cardName)")
        override val amount: Double = fee
        override val dateMillis: Long = 0L
        override val isCarriedOver: Boolean = true
    }

    data class SavingsExpense(
        val saving: SavingsEntity,
        override val isCarriedOver: Boolean
    ) : UnifiedExpenseItem() {
        override val key: String = "saving_${saving.id}"
        override val concept: String = saving.concept
        override val amount: Double = saving.amount
        override val dateMillis: Long = saving.date
    }
}

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
            val expYm = YearMonth.from(Instant.ofEpochMilli(exp.date).atZone(ZoneId.systemDefault()).toLocalDate())
            val installment = expenseInstallments[exp.id]
            val isCarriedOver = exp.isRecurring || expYm.isBefore(selectedYearMonth) || installment != null
            rawList.add(UnifiedExpenseItem.DirectExpense(exp, isCarriedOver, installment))
        }

        // 2. Card Purchases and Maintenance Fees
        cardSummaries.forEach { summary ->
            summary.purchases.filter { it.statusType == InstallmentStatusType.ACTIVE }.forEach { detail ->
                val symbol = if (detail.currency == "USD") "USD $" else "$"
                val purchaseYm = YearMonth.from(Instant.ofEpochMilli(detail.purchaseDateMillis).atZone(ZoneId.systemDefault()).toLocalDate())
                val isCarriedOver = detail.isRecurring || (detail.totalInstallments > 1 && (detail.currentInstallment > 1 || purchaseYm.isBefore(selectedYearMonth)))
                rawList.add(UnifiedExpenseItem.CardInstallmentExpense(summary.cardName, detail, symbol, isCarriedOver))
            }
            val feeInCop = summary.maintenanceFeeInCop(usdRate)
            if (feeInCop > 0) {
                rawList.add(UnifiedExpenseItem.CardFeeExpense(summary.cardId, summary.cardName, feeInCop, "$"))
            }
        }

        // 3. Savings
        savings.forEach { sav ->
            val savYm = YearMonth.from(Instant.ofEpochMilli(sav.date).atZone(ZoneId.systemDefault()).toLocalDate())
            val isCarriedOver = sav.isRecurring || savYm.isBefore(selectedYearMonth)
            rawList.add(UnifiedExpenseItem.SavingsExpense(sav, isCarriedOver))
        }

        // Separate items into current month registrations and carried-over monthly installments
        val currentMonthItems = rawList.filter { !it.isCarriedOver }
        val carriedOverItems = rawList.filter { it.isCarriedOver }

        // Sort current month items according to user preference
        val sortedCurrentMonthItems = when (expenseSortOrder) {
            ExpenseSortOrder.NEWEST_FIRST -> currentMonthItems.sortedWith(
                compareByDescending<UnifiedExpenseItem> { it.dateMillis }.thenByDescending { it.key }
            )
            ExpenseSortOrder.OLDEST_FIRST -> currentMonthItems.sortedWith(
                compareBy<UnifiedExpenseItem> { it.dateMillis }.thenBy { it.key }
            )
        }

        // Sort carried over items cleanly by date / key
        val sortedCarriedOverItems = carriedOverItems.sortedWith(
            compareByDescending<UnifiedExpenseItem> { it.dateMillis }.thenBy { it.key }
        )

        // Assemble list based on installmentsPosition setting
        if (installmentsPosition == InstallmentPosition.FIRST) {
            sortedCarriedOverItems + sortedCurrentMonthItems
        } else {
            sortedCurrentMonthItems + sortedCarriedOverItems
        }
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
            title to if (movePaidToTop) items.sortedWith(compareByDescending { it.key in paidExpenseKeys }) else items
        }
    }

    val isDark = isAppDark()

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
                                        ?.let { item.expense.copy(amount = it.installmentAmount) } ?: item.expense
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

    if (purchaseToEdit != null) {
        val card = allCards.find { it.id == purchaseToEdit!!.cardId }
        AddEditPurchaseDialog(
            cardName = card?.name ?: tr("Tarjeta", "Card"),
            cardInterestRate = card?.interestRate ?: 0.0,
            cardFirstInstallmentInterestFree = card?.isFirstInstallmentInterestFree ?: false,
            purchaseToEdit = purchaseToEdit,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { purchaseToEdit = null },
            onSave = { concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency ->
                onUpdateCardPurchase(
                    CardPurchaseEntity(
                        id = purchaseToEdit!!.purchaseId,
                        cardId = purchaseToEdit!!.cardId,
                        concept = concept,
                        totalAmount = totalAmount,
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

/** Monto del ítem en COP (los USD se convierten con la TRM). */
fun UnifiedExpenseItem.amountInCop(usdRate: Double): Double = when (this) {
    is UnifiedExpenseItem.DirectExpense -> if (expense.currency == "USD") expense.amount * usdRate else expense.amount
    is UnifiedExpenseItem.CardInstallmentExpense -> detail.monthlyPaymentInCop(usdRate)
    else -> amount
}

private val CARRIED_OVER_SECTION: String get() = tr("Cuotas, fijos y recurrentes", "Installments, fixed and recurring")

/** Agrupa conservando el orden: cada día del mes ("Hoy", "Ayer", "Jueves 9 de octubre") y un bloque para lo que se repite. */
fun groupExpenseItems(items: List<UnifiedExpenseItem>): List<Pair<String, List<UnifiedExpenseItem>>> {
    val today = java.time.LocalDate.now()
    val dayFormatter = java.time.format.DateTimeFormatter.ofPattern(tr("EEEE d 'de' MMMM", "EEEE, MMMM d"), appLocale())
    val groups = LinkedHashMap<String, MutableList<UnifiedExpenseItem>>()
    items.forEach { item ->
        val title = if (item.isCarriedOver) {
            CARRIED_OVER_SECTION
        } else {
            val day = Instant.ofEpochMilli(item.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            when (day) {
                today -> tr("Hoy", "Today")
                today.minusDays(1) -> tr("Ayer", "Yesterday")
                else -> day.format(dayFormatter).replaceFirstChar { it.uppercase() }
            }
        }
        groups.getOrPut(title) { mutableListOf() }.add(item)
    }
    return groups.map { it.key to it.value }
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
            if (item.expense.currency == "USD") "${formatUsd(item.expense.amount)} (~${formatCurrency(item.expense.amount * usdRate)})"
            else formatCurrency(item.amount)
        }
        is UnifiedExpenseItem.CardInstallmentExpense -> {
            if (item.detail.currency == "USD") "${formatUsd(item.detail.monthlyPayment)} (~${formatCurrency(item.detail.monthlyPayment * usdRate)})"
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
                            value = if (detail.currency == "USD") "${formatUsd(detail.monthlyPayment)} (~${formatCurrency(detail.monthlyPayment * usdRate)})"
                                    else formatCurrency(detail.monthlyPayment, item.currencySymbol),
                            isHighlight = true,
                            highlightColor = FinanceColors.cardText(isDark)
                        )
                        if (detail.totalInstallments > 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            CardDetailRow(
                                label = "Saldo Restante",
                                value = if (detail.currency == "USD") "${formatUsd(detail.remainingBalanceToPay)} (~${formatCurrency(detail.remainingBalanceToPay * usdRate)})"
                                        else formatCurrency(detail.remainingBalanceToPay, item.currencySymbol)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            CardDetailRow(
                                label = "Monto Total Compra",
                                value = if (detail.currency == "USD") "${formatUsd(detail.totalAmount)} (~${formatCurrency(detail.totalAmount * usdRate)})"
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

    val purchaseDateFormat = remember { SimpleDateFormat(tr("dd 'de' MMMM 'de' yyyy", "MMMM d, yyyy"), appLocale()) }

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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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
                        if (concept.isBlank()) {
                            errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                        } else if (amount == null || amount <= 0) {
                            errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                        } else if (isInInstallments && parseExpenseInstallments(expenseInstallmentsText) == null) {
                            errorMessage = tr("Ingresa un número de cuotas entre 1 y $MAX_EXPENSE_INSTALLMENTS", "Enter a number of installments between 1 and $MAX_EXPENSE_INSTALLMENTS")
                        } else {
                            val installments = if (isInInstallments) parseExpenseInstallments(expenseInstallmentsText)!! else 0
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
                            onSaveCardPurchase(selectedCardId, cardConcept.trim(), totalAmount, installments, expenseDateMillis, finalIsInterestFree, isCardRecurring, cardCurrency)
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
    val expenseDateFormat = remember { SimpleDateFormat(tr("dd 'de' MMMM 'de' yyyy", "MMMM d, yyyy"), appLocale()) }

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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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
                    if (concept.isBlank()) {
                        errorMessage = tr("Por favor ingresa un concepto", "Enter a name")
                    } else if (amount == null || amount <= 0) {
                        errorMessage = tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
                    } else if (isInInstallments && parseExpenseInstallments(installmentsText) == null) {
                        errorMessage = tr("Ingresa un número de cuotas entre 1 y $MAX_EXPENSE_INSTALLMENTS", "Enter a number of installments between 1 and $MAX_EXPENSE_INSTALLMENTS")
                    } else {
                        val installments = if (isInInstallments) parseExpenseInstallments(installmentsText)!! else 0
                        val base = expenseToEdit ?: ExpenseEntity(concept = concept.trim(), amount = amount, date = expenseDateMillis, isRecurring = isRecurring, currency = currency)
                        onSave(base.copy(concept = concept.trim(), amount = amount, date = expenseDateMillis, isRecurring = isRecurring && installments == 0, currency = currency, installments = installments))
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
