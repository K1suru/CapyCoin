package com.example.gestiongastos.ui.viewmodel

import com.example.gestiongastos.i18n.I18n
import com.example.gestiongastos.i18n.AppLanguage
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestiongastos.data.local.AppDatabase
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CardPurchasePaymentEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.remote.ExchangeRateService
import com.example.gestiongastos.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.gestiongastos.data.backup.BackupManager
import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.ExpenseSortOrder
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.repository.endOfMonthMillis
import com.example.gestiongastos.data.repository.yearMonthOf
import java.time.YearMonth

data class SummaryUiState(
    val totalIncome: Double = 0.0,
    val directExpenses: Double = 0.0,
    val creditCardExpenses: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val neededAmount: Double = 0.0,
    val freeAmount: Double = 0.0,
    val spentPercentage: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val prefs: SharedPreferences = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Idioma de la app (Ajustes). Primer uso: el del teléfono si es inglés; si no, español.
    private val _language = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", null)) ?: AppLanguage.systemDefault()
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        I18n.language = _language.value
    }

    private val _selectedYearMonth = MutableStateFlow(
        prefs.getString("selected_year_month", null)?.let {
            try { YearMonth.parse(it) } catch (e: Exception) { YearMonth.now() }
        } ?: YearMonth.now()
    )
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    private val _installmentsPosition = MutableStateFlow(
        prefs.getString("installments_position", InstallmentPosition.LAST.name)?.let {
            try { InstallmentPosition.valueOf(it) } catch (e: Exception) { InstallmentPosition.LAST }
        } ?: InstallmentPosition.LAST
    )
    val installmentsPosition: StateFlow<InstallmentPosition> = _installmentsPosition.asStateFlow()

    private val _expenseSortOrder = MutableStateFlow(
        prefs.getString("expense_sort_order", ExpenseSortOrder.NEWEST_FIRST.name)?.let {
            try { ExpenseSortOrder.valueOf(it) } catch (e: Exception) { ExpenseSortOrder.NEWEST_FIRST }
        } ?: ExpenseSortOrder.NEWEST_FIRST
    )
    val expenseSortOrder: StateFlow<ExpenseSortOrder> = _expenseSortOrder.asStateFlow()

    private val _movePaidToTop = MutableStateFlow(
        prefs.getBoolean("move_paid_to_top", true)
    )
    val movePaidToTop: StateFlow<Boolean> = _movePaidToTop.asStateFlow()

    private val _isPrivacyModeEnabled = MutableStateFlow(
        prefs.getBoolean("privacy_mode_enabled", false)
    )
    val isPrivacyModeEnabled: StateFlow<Boolean> = _isPrivacyModeEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(
        prefs.getBoolean("biometric_enabled", false)
    )
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(
        !prefs.getBoolean("biometric_enabled", false)
    )
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    private fun loadCardOrder(): List<Long> {
        val raw = prefs.getString("credit_cards_order", null) ?: return emptyList()
        return raw.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }

    private val _cardOrderIds = MutableStateFlow<List<Long>>(loadCardOrder())
    val cardOrderIds: StateFlow<List<Long>> = _cardOrderIds.asStateFlow()

    fun setCardOrder(orderedIds: List<Long>) {
        _cardOrderIds.value = orderedIds
        prefs.edit().putString("credit_cards_order", orderedIds.joinToString(",")).apply()
    }

    val monthlyIncomes: StateFlow<List<IncomeEntity>>
    val monthlyExpenses: StateFlow<List<ExpenseEntity>>
    val monthlySavings: StateFlow<List<SavingsEntity>>
    val cardSummaries: StateFlow<List<CardMonthSummary>>
    val allCreditCards: StateFlow<List<CreditCardEntity>>
    val summaryUiState: StateFlow<SummaryUiState>

    // TRM USD → COP (Banco de la República). Se guarda la última conocida para usarla sin internet.
    private val _usdRate = MutableStateFlow(
        prefs.getFloat("last_usd_rate", DEFAULT_USD_RATE.toFloat()).toDouble()
    )
    val usdRate: StateFlow<Double> = _usdRate.asStateFlow()

    val ignoredExpenseKeys: StateFlow<Set<String>>
    val paidCreditCardIds: StateFlow<Set<Long>>
    val paidExpenseKeys: StateFlow<Set<String>>
    val receivedIncomeIds: StateFlow<Set<Long>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        allCreditCards = combine(repository.creditCards, _cardOrderIds) { cards, orderIds ->
            if (orderIds.isEmpty()) {
                cards
            } else {
                val orderMap = orderIds.withIndex().associate { it.value to it.index }
                cards.sortedWith(compareBy(
                    { orderMap[it.id] ?: Int.MAX_VALUE },
                    { it.id }
                ))
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        paidCreditCardIds = combine(repository.paidCreditCards, _selectedYearMonth) { list, ym ->
            list.filter { it.yearMonthText == ym.toString() }.map { it.cardId }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

        paidExpenseKeys = combine(repository.paidExpenses, _selectedYearMonth) { list, ym ->
            list.filter { it.yearMonthText == ym.toString() }.map { it.itemKey }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

        ignoredExpenseKeys = combine(repository.ignoredExpenses, _selectedYearMonth) { list, ym ->
            list.filter { it.yearMonthText == ym.toString() }.map { it.itemKey }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

        receivedIncomeIds = combine(repository.receivedIncomes, _selectedYearMonth) { list, ym ->
            list.filter { it.yearMonthText == ym.toString() }.map { it.incomeId }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())


        monthlyIncomes = _selectedYearMonth.flatMapLatest { ym ->
            repository.getIncomesForMonth(ym)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        monthlyExpenses = _selectedYearMonth.flatMapLatest { ym ->
            repository.getExpensesForMonth(ym)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        monthlySavings = _selectedYearMonth.flatMapLatest { ym ->
            repository.getSavingsForMonth(ym)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Fetch live exchange rate on startup
        refreshUsdRate()

        // El idioma también entra: los estados de las cuotas ("Cuota 1 de 3") son textos traducidos
        cardSummaries = combine(_selectedYearMonth, _usdRate, _cardOrderIds, _language) { ym, rate, orderIds, _ ->
            Triple(ym, rate, orderIds)
        }.flatMapLatest { (ym, rate, orderIds) ->
            repository.getCardMonthSummaries(ym, rate).map { summaries ->
                if (orderIds.isEmpty()) {
                    summaries
                } else {
                    val orderMap = orderIds.withIndex().associate { it.value to it.index }
                    summaries.sortedWith(compareBy(
                        { orderMap[it.cardId] ?: Int.MAX_VALUE },
                        { it.cardId }
                    ))
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        summaryUiState = combine(monthlyIncomes, monthlyExpenses, monthlySavings, cardSummaries, _usdRate, ignoredExpenseKeys) { args ->
            @Suppress("UNCHECKED_CAST")
            val incomes = args[0] as List<IncomeEntity>
            @Suppress("UNCHECKED_CAST")
            val expenses = args[1] as List<ExpenseEntity>
            @Suppress("UNCHECKED_CAST")
            val savings = args[2] as List<SavingsEntity>
            @Suppress("UNCHECKED_CAST")
            val cards = args[3] as List<CardMonthSummary>
            val rate = args[4] as Double
            @Suppress("UNCHECKED_CAST")
            val ignoredKeys = args[5] as Set<String>

            val totalInc = incomes.sumOf { it.amount }
            val directExp = expenses
                .filter { "direct_${it.id}" !in ignoredKeys }
                .sumOf { if (it.currency == "USD") it.amount * rate else it.amount }

            val ccExp = cards.sumOf { card -> card.payableThisMonth(rate, ignoredKeys) }

            val totalExp = directExp + ccExp
            val totalSav = savings
                .filter { "saving_${it.id}" !in ignoredKeys }
                .sumOf { it.amount }

            val needed = totalExp + totalSav
            val free = totalInc - (totalExp + totalSav)
            val totalOutflow = totalExp + totalSav
            val pct = if (totalInc > 0) (totalOutflow / totalInc).toFloat() else if (totalOutflow > 0) 1.0f else 0f

            SummaryUiState(
                totalIncome = totalInc,
                directExpenses = directExp,
                creditCardExpenses = ccExp,
                totalExpenses = totalExp,
                totalSavings = totalSav,
                neededAmount = needed,
                freeAmount = free,
                spentPercentage = pct.coerceIn(0f, 2f)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryUiState())

    }

    fun toggleIgnoredExpense(itemKey: String) {
        viewModelScope.launch {
            repository.toggleIgnoredExpense(itemKey, _selectedYearMonth.value)
        }
    }

    fun togglePaidCreditCard(cardId: Long) {
        viewModelScope.launch {
            repository.togglePaidCreditCard(cardId, _selectedYearMonth.value)
        }
    }

    fun togglePaidExpense(itemKey: String) {
        viewModelScope.launch {
            repository.togglePaidExpense(itemKey, _selectedYearMonth.value)
        }
    }

    fun toggleReceivedIncome(incomeId: Long) {
        viewModelScope.launch {
            repository.toggleReceivedIncome(incomeId, _selectedYearMonth.value)
        }
    }


    fun setLanguage(newLanguage: AppLanguage) {
        if (newLanguage == _language.value) return
        I18n.reopenSettings = true
        I18n.language = newLanguage
        _language.value = newLanguage
        prefs.edit().putString("app_language", newLanguage.code).apply()
    }

    fun setSelectedYearMonth(yearMonth: YearMonth) {
        _selectedYearMonth.value = yearMonth
        prefs.edit().putString("selected_year_month", yearMonth.toString()).apply()
    }

    fun setInstallmentsPosition(position: InstallmentPosition) {
        _installmentsPosition.value = position
        prefs.edit().putString("installments_position", position.name).apply()
    }

    fun setExpenseSortOrder(order: ExpenseSortOrder) {
        _expenseSortOrder.value = order
        prefs.edit().putString("expense_sort_order", order.name).apply()
    }

    fun setMovePaidToTop(enabled: Boolean) {
        _movePaidToTop.value = enabled
        prefs.edit().putBoolean("move_paid_to_top", enabled).apply()
    }

    fun refreshData() {
        refreshUsdRate()
    }

    private fun refreshUsdRate() {
        viewModelScope.launch {
            // null = sin conexión: se conserva la última TRM conocida
            val liveRate = ExchangeRateService.getUsdToCopRate() ?: return@launch
            _usdRate.value = liveRate
            prefs.edit().putFloat("last_usd_rate", liveRate.toFloat()).apply()
        }
    }

    fun previousMonth() {
        val newYm = _selectedYearMonth.value.minusMonths(1)
        _selectedYearMonth.value = newYm
        prefs.edit().putString("selected_year_month", newYm.toString()).apply()
    }

    fun nextMonth() {
        val newYm = _selectedYearMonth.value.plusMonths(1)
        _selectedYearMonth.value = newYm
        prefs.edit().putString("selected_year_month", newYm.toString()).apply()
    }

    fun togglePrivacyMode() {
        val next = !_isPrivacyModeEnabled.value
        _isPrivacyModeEnabled.value = next
        prefs.edit().putBoolean("privacy_mode_enabled", next).apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun setAppUnlocked(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    // CRUD Incomes
    fun addIncome(
        concept: String,
        amount: Double,
        dateMillis: Long,
        isRecurring: Boolean,
        linkedExpenseIdsText: String? = null,
        linkedCardPurchaseIdsText: String? = null,
        extraAmount: Double = 0.0,
        extraConcept: String? = null,
        totalInstallments: Int = 1,
        totalAmount: Double = 0.0,
        endDateMillis: Long? = null
    ) {
        viewModelScope.launch {
            repository.addIncome(
                IncomeEntity(
                    concept = concept,
                    amount = amount,
                    date = dateMillis,
                    isRecurring = isRecurring,
                    linkedExpenseIdsText = linkedExpenseIdsText,
                    linkedCardPurchaseIdsText = linkedCardPurchaseIdsText,
                    extraAmount = extraAmount,
                    extraConcept = extraConcept,
                    totalInstallments = totalInstallments,
                    totalAmount = totalAmount,
                    endDateMillis = endDateMillis
                )
            )
        }
    }

    fun updateIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.updateIncome(income)
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    /**
     * Termina un ingreso recurrente: deja de aparecer desde el mes seleccionado y conserva los meses anteriores.
     * Si el mes seleccionado es el de inicio, no hay historial que conservar y se elimina.
     */
    fun endIncomeFromSelectedMonth(income: IncomeEntity) {
        val ym = _selectedYearMonth.value
        if (!ym.isAfter(yearMonthOf(income.date))) deleteIncome(income)
        else updateIncome(income.copy(endDateMillis = endOfMonthMillis(ym.minusMonths(1))))
    }

    // CRUD Expenses
    fun addExpense(concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean, currency: String = "COP") {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    concept = concept,
                    amount = amount,
                    date = dateMillis,
                    isRecurring = isRecurring,
                    currency = currency
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    /** Igual que [endIncomeFromSelectedMonth] pero para gastos fijos recurrentes. */
    fun endExpenseFromSelectedMonth(expense: ExpenseEntity) {
        val ym = _selectedYearMonth.value
        if (!ym.isAfter(yearMonthOf(expense.date))) deleteExpense(expense)
        else updateExpense(expense.copy(endDateMillis = endOfMonthMillis(ym.minusMonths(1))))
    }

    // CRUD Savings
    fun addSavings(concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) {
        viewModelScope.launch {
            repository.addSavings(
                SavingsEntity(
                    concept = concept,
                    amount = amount,
                    date = dateMillis,
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun updateSavings(saving: SavingsEntity) {
        viewModelScope.launch {
            repository.updateSavings(saving)
        }
    }

    fun deleteSavings(saving: SavingsEntity) {
        viewModelScope.launch {
            repository.deleteSavings(saving)
        }
    }

    /** Igual que [endIncomeFromSelectedMonth] pero para ahorros recurrentes. */
    fun endSavingFromSelectedMonth(saving: SavingsEntity) {
        val ym = _selectedYearMonth.value
        if (!ym.isAfter(yearMonthOf(saving.date))) deleteSavings(saving)
        else updateSavings(saving.copy(endDateMillis = endOfMonthMillis(ym.minusMonths(1))))
    }

    // CRUD Credit Cards
    fun addCreditCard(
        name: String,
        cutoffDay: Int,
        paymentDay: Int = 0,
        interestRate: Double,
        maintenanceFee: Double,
        currency: String = "COP",
        isFeeWaiverEnabled: Boolean = false,
        minSpendForFeeWaiver: Double = 0.0,
        isFirstInstallmentInterestFree: Boolean = false,
        creditLimit: Double = 0.0,
        colorIndex: Int = -1
    ) {
        viewModelScope.launch {
            repository.addCreditCard(
                CreditCardEntity(
                    name = name,
                    cutoffDay = cutoffDay,
                    paymentDay = paymentDay,
                    interestRate = interestRate,
                    maintenanceFee = maintenanceFee,
                    currency = currency,
                    isFeeWaiverEnabled = isFeeWaiverEnabled,
                    minSpendForFeeWaiver = minSpendForFeeWaiver,
                    isFirstInstallmentInterestFree = isFirstInstallmentInterestFree,
                    creditLimit = creditLimit,
                    colorIndex = colorIndex
                )
            )
        }
    }

    fun updateCreditCard(card: CreditCardEntity) {
        viewModelScope.launch {
            repository.updateCreditCard(card)
        }
    }

    fun deleteCreditCard(card: CreditCardEntity) {
        viewModelScope.launch {
            repository.deleteCreditCard(card)
        }
    }

    // CRUD Card Purchases
    fun addCardPurchase(
        cardId: Long,
        concept: String,
        totalAmount: Double,
        installments: Int,
        purchaseDateMillis: Long,
        isInterestFree: Boolean = false,
        isRecurring: Boolean = false,
        currency: String = "COP"
    ) {
        viewModelScope.launch {
            repository.addCardPurchase(
                CardPurchaseEntity(
                    cardId = cardId,
                    concept = concept,
                    totalAmount = totalAmount,
                    installments = installments,
                    purchaseDate = purchaseDateMillis,
                    isInterestFree = isInterestFree,
                    isRecurring = isRecurring,
                    currency = currency
                )
            )
        }
    }

    fun updateCardPurchase(purchase: CardPurchaseEntity) {
        viewModelScope.launch {
            repository.updateCardPurchase(purchase)
        }
    }

    fun deleteCardPurchase(purchaseId: Long) {
        viewModelScope.launch {
            repository.deleteCardPurchaseById(purchaseId)
        }
    }

    fun addCardPurchasePayment(purchaseId: Long, amount: Double, dateMillis: Long, yearMonth: YearMonth) {
        viewModelScope.launch {
            repository.addCardPurchasePayment(
                CardPurchasePaymentEntity(
                    purchaseId = purchaseId,
                    amount = amount,
                    date = dateMillis,
                    yearMonthText = yearMonth.toString()
                )
            )
        }
    }

    fun omitCardPurchaseInstallmentForMonth(purchaseId: Long, targetYearMonth: YearMonth) {
        viewModelScope.launch {
            repository.omitPurchaseInstallmentForMonth(purchaseId, targetYearMonth)
        }
    }

    fun toggleSettleCardPurchase(purchaseId: Long) {
        viewModelScope.launch {
            repository.toggleSettlePurchase(purchaseId, _selectedYearMonth.value)
        }
    }

    fun exportBackup(context: Context, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val result = BackupManager.exportBackup(context, db)
            onResult(result)
        }
    }

    fun restoreBackup(context: Context, uri: Uri, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val result = BackupManager.restoreBackup(context, uri, db)
            if (result.isSuccess) {
                refreshData()
            }
            onResult(result)
        }
    }
}
