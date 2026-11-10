package com.example.gestiongastos.data.repository

import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.model.calculateExpenseInstallment
import com.example.gestiongastos.data.model.ExpenseInstallmentInfo
import com.example.gestiongastos.data.local.ExpensePaymentEntity
import com.example.gestiongastos.data.local.AppDatabase
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CardPurchasePaymentEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.OmittedInstallmentEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.model.PurchasePayment
import com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail
import com.example.gestiongastos.data.model.incomeAppliesInMonth
import com.example.gestiongastos.data.model.payableThisMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

import com.example.gestiongastos.data.local.IgnoredExpenseDao
import com.example.gestiongastos.data.local.IgnoredExpenseEntity
import com.example.gestiongastos.data.local.PaidCreditCardDao
import com.example.gestiongastos.data.local.PaidExpenseDao
import com.example.gestiongastos.data.local.ReceivedIncomeDao
import com.example.gestiongastos.data.local.SettledPurchaseDao
import com.example.gestiongastos.data.local.PaidCreditCardEntity
import com.example.gestiongastos.data.local.PaidExpenseEntity
import com.example.gestiongastos.data.local.ReceivedIncomeEntity
import com.example.gestiongastos.data.local.SettledPurchaseEntity
import androidx.room.withTransaction
import com.example.gestiongastos.data.model.ItemKeys

/** Mes (zona local) de un timestamp en milisegundos. */
/** Fecha local de un instante guardado en milisegundos (hoy si el valor no es una fecha válida). */
fun localDateOf(millis: Long): LocalDate = try {
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
} catch (e: Exception) {
    LocalDate.now()
}

fun yearMonthOf(millis: Long): YearMonth = YearMonth.from(localDateOf(millis))

/** Un registro único aplica solo en su mes; uno recurrente desde su mes hasta [endDateMillis] (incluido). */
fun isInMonth(target: YearMonth, start: YearMonth, isRecurring: Boolean, endDateMillis: Long?): Boolean {
    if (!isRecurring) return target == start
    if (target.isBefore(start)) return false
    return endDateMillis == null || !target.isAfter(yearMonthOf(endDateMillis))
}

/** Timestamp del último instante de [yearMonth], usado como fecha de fin de un recurrente. */
fun endOfMonthMillis(yearMonth: YearMonth): Long =
    yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Marca o desmarca un ingreso como recibido en un mes. Devuelve true si quedó marcado. */
internal suspend fun toggleReceivedIncome(dao: ReceivedIncomeDao, incomeId: Long, yearMonthText: String): Boolean {
    if (dao.deleteReceivedIncome(incomeId, yearMonthText) > 0) return false
    dao.insertReceivedIncome(ReceivedIncomeEntity(incomeId = incomeId, yearMonthText = yearMonthText))
    return true
}

/** Marca o desmarca un ítem como ignorado en un mes. Devuelve true si quedó marcado. */
internal suspend fun toggleIgnoredExpense(dao: IgnoredExpenseDao, itemKey: String, yearMonthText: String): Boolean {
    if (dao.deleteIgnoredExpense(itemKey, yearMonthText) > 0) return false
    dao.insertIgnoredExpense(IgnoredExpenseEntity(itemKey = itemKey, yearMonthText = yearMonthText))
    return true
}

/** Marca o desmarca una tarjeta como pagada en un mes. Devuelve true si quedó marcada. */
internal suspend fun togglePaidCreditCard(dao: PaidCreditCardDao, cardId: Long, yearMonthText: String): Boolean {
    if (dao.deletePaidCreditCard(cardId, yearMonthText) > 0) return false
    dao.insertPaidCreditCard(PaidCreditCardEntity(cardId = cardId, yearMonthText = yearMonthText))
    return true
}

/** Marca o desmarca un ítem como pagado en un mes. Devuelve true si quedó marcado. */
internal suspend fun togglePaidExpense(dao: PaidExpenseDao, itemKey: String, yearMonthText: String): Boolean {
    if (dao.deletePaidExpense(itemKey, yearMonthText) > 0) return false
    dao.insertPaidExpense(PaidExpenseEntity(itemKey = itemKey, yearMonthText = yearMonthText))
    return true
}

/**
 * Liquida una compra en [yearMonthText], o revierte la liquidación si ya estaba liquidada en ese mes.
 * Una compra solo puede estar liquidada en un mes: si lo estaba en otro, se mueve a este.
 * Devuelve true si quedó liquidada.
 */
internal suspend fun toggleSettledPurchase(dao: SettledPurchaseDao, purchaseId: Long, yearMonthText: String): Boolean {
    val wasSettledThisMonth = dao.getSettledForPurchase(purchaseId).any { it.yearMonthText == yearMonthText }
    dao.deleteSettledPurchasesForPurchase(purchaseId)
    if (wasSettledThisMonth) return false
    dao.insertSettledPurchase(SettledPurchaseEntity(purchaseId = purchaseId, yearMonthText = yearMonthText))
    return true
}

class FinanceRepository(private val db: AppDatabase) {

    val incomes: Flow<List<IncomeEntity>> = db.incomeDao().getAllIncomes()
    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val creditCards: Flow<List<CreditCardEntity>> = db.creditCardDao().getAllCreditCards()
    val cardPurchases: Flow<List<CardPurchaseEntity>> = db.cardPurchaseDao().getAllCardPurchases()
    val savings: Flow<List<SavingsEntity>> = db.savingsDao().getAllSavings()
    val omittedInstallments: Flow<List<OmittedInstallmentEntity>> = db.omittedInstallmentDao().getAllOmittedInstallments()
    val cardPurchasePayments: Flow<List<CardPurchasePaymentEntity>> = db.cardPurchasePaymentDao().getAllCardPurchasePayments()
    val ignoredExpenses: Flow<List<IgnoredExpenseEntity>> = db.ignoredExpenseDao().getAllIgnoredExpenses()
    val paidCreditCards: Flow<List<PaidCreditCardEntity>> = db.paidCreditCardDao().getAllPaidCreditCards()
    val paidExpenses: Flow<List<PaidExpenseEntity>> = db.paidExpenseDao().getAllPaidExpenses()
    val settledPurchases: Flow<List<SettledPurchaseEntity>> = db.settledPurchaseDao().getAllSettledPurchases()
    val receivedIncomes: Flow<List<ReceivedIncomeEntity>> = db.receivedIncomeDao().getAllReceivedIncomes()
    val expensePayments: Flow<List<ExpensePaymentEntity>> = db.expensePaymentDao().getAllExpensePayments()

    // Marcar/desmarcar va en una transacción y no lee la tabla entera: se intenta borrar la marca
    // y, si no existía, se crea. Así dos toques seguidos no dejan marcas duplicadas ni a medias.
    suspend fun toggleReceivedIncome(incomeId: Long, targetYearMonth: YearMonth) = db.withTransaction {
        toggleReceivedIncome(db.receivedIncomeDao(), incomeId, targetYearMonth.toString())
    }

    suspend fun toggleIgnoredExpense(itemKey: String, targetYearMonth: YearMonth) = db.withTransaction {
        toggleIgnoredExpense(db.ignoredExpenseDao(), itemKey, targetYearMonth.toString())
    }

    suspend fun togglePaidCreditCard(cardId: Long, targetYearMonth: YearMonth) = db.withTransaction {
        togglePaidCreditCard(db.paidCreditCardDao(), cardId, targetYearMonth.toString())
    }

    suspend fun togglePaidExpense(itemKey: String, targetYearMonth: YearMonth) = db.withTransaction {
        togglePaidExpense(db.paidExpenseDao(), itemKey, targetYearMonth.toString())
    }

    suspend fun toggleSettlePurchase(purchaseId: Long, targetYearMonth: YearMonth) = db.withTransaction {
        toggleSettledPurchase(db.settledPurchaseDao(), purchaseId, targetYearMonth.toString())
    }

    suspend fun deleteSettledPurchasesForPurchase(purchaseId: Long) {
        db.settledPurchaseDao().deleteSettledPurchasesForPurchase(purchaseId)
    }

    suspend fun addIncome(income: IncomeEntity) = db.incomeDao().insertIncome(income)
    suspend fun updateIncome(income: IncomeEntity) = db.incomeDao().updateIncome(income)
    suspend fun deleteIncome(income: IncomeEntity) = db.withTransaction {
        db.receivedIncomeDao().deleteAllForIncome(income.id)
        db.incomeDao().deleteIncome(income)
    }

    suspend fun addExpense(expense: ExpenseEntity) = db.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = db.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = db.withTransaction {
        deleteItemMarks(ItemKeys.direct(expense.id))
        db.expensePaymentDao().deletePaymentsForExpense(expense.id)
        db.expenseDao().deleteExpense(expense)
    }

    suspend fun addSavings(saving: SavingsEntity) = db.savingsDao().insertSavings(saving)
    suspend fun updateSavings(saving: SavingsEntity) = db.savingsDao().updateSavings(saving)
    suspend fun deleteSavings(saving: SavingsEntity) = db.withTransaction {
        deleteItemMarks(ItemKeys.saving(saving.id))
        db.savingsDao().deleteSavings(saving)
    }

    suspend fun addCreditCard(card: CreditCardEntity) = db.creditCardDao().insertCreditCard(card)
    suspend fun updateCreditCard(card: CreditCardEntity) = db.creditCardDao().updateCreditCard(card)

    /** Borra la tarjeta junto con sus compras, abonos y marcas (no deja datos huérfanos). */
    suspend fun deleteCreditCard(card: CreditCardEntity) = db.withTransaction {
        db.cardPurchaseDao().getPurchasesByCardList(card.id).forEach { purchase ->
            deletePurchaseData(purchase.id)
        }
        deleteItemMarks(ItemKeys.cardFee(card.id))
        db.paidCreditCardDao().deleteAllForCard(card.id)
        db.creditCardDao().deleteCreditCard(card)
    }

    suspend fun addCardPurchase(purchase: CardPurchaseEntity) = db.cardPurchaseDao().insertCardPurchase(purchase)
    suspend fun updateCardPurchase(purchase: CardPurchaseEntity) = db.cardPurchaseDao().updateCardPurchase(purchase)

    /**
     * Borra las compras cuya tarjeta ya no existe, y los abonos, cuotas omitidas y marcas de "liquidada"
     * que quedaron sin compra. Venían de versiones viejas, cuando borrar una tarjeta no borraba sus compras;
     * no se veían en ninguna pantalla. Devuelve cuántas compras se borraron.
     */
    suspend fun deleteOrphanedCardData(): Int = db.withTransaction {
        val dao = db.cardPurchaseDao()
        val deletedPurchases = dao.deletePurchasesWithoutCard()
        dao.deletePaymentsWithoutPurchase()
        dao.deleteOmittedWithoutPurchase()
        dao.deleteSettledWithoutPurchase()
        deletedPurchases
    }

    suspend fun deleteCardPurchaseById(purchaseId: Long) = db.withTransaction {
        deletePurchaseData(purchaseId)
    }

    private suspend fun deletePurchaseData(purchaseId: Long) {
        db.cardPurchasePaymentDao().deletePaymentsForPurchase(purchaseId)
        db.settledPurchaseDao().deleteSettledPurchasesForPurchase(purchaseId)
        db.omittedInstallmentDao().deleteOmittedForPurchase(purchaseId)
        deleteItemMarks(ItemKeys.cardInstallment(purchaseId))
        db.cardPurchaseDao().deleteCardPurchaseById(purchaseId)
    }

    private suspend fun deleteItemMarks(itemKey: String) {
        db.paidExpenseDao().deleteAllForItem(itemKey)
        db.ignoredExpenseDao().deleteAllForItem(itemKey)
    }

    suspend fun addCardPurchasePayment(payment: CardPurchasePaymentEntity) = db.cardPurchasePaymentDao().insertPayment(payment)

    suspend fun omitPurchaseInstallmentForMonth(purchaseId: Long, targetYearMonth: YearMonth) {
        db.omittedInstallmentDao().insertOmittedInstallment(
            OmittedInstallmentEntity(purchaseId = purchaseId, yearMonthText = targetYearMonth.toString())
        )
    }

    fun getIncomesForMonth(targetYearMonth: YearMonth): Flow<List<IncomeEntity>> {
        return incomes.map { incomesList ->
            incomesList.filter { income -> incomeAppliesInMonth(income, targetYearMonth) }
        }
    }

    suspend fun addExpensePayment(payment: ExpensePaymentEntity) = db.expensePaymentDao().insertPayment(payment)

    /**
     * Gastos del mes. Los gastos en cuotas aparecen mientras dure la deuda y con el monto que toca
     * pagar ese mes (la última cuota puede ser menor por los abonos); su detalle va en el mapa.
     * Ojo: en esos gastos `amount` es el cobro del mes; el valor fijo de la cuota está en el mapa.
     */
    fun getExpensesForMonth(targetYearMonth: YearMonth): Flow<Pair<List<ExpenseEntity>, Map<Long, ExpenseInstallmentInfo>>> {
        return combine(expenses, expensePayments) { expensesList, payments ->
            val paymentsByExpense = payments.groupBy({ it.expenseId }) { p ->
                PurchasePayment(try { YearMonth.parse(p.yearMonthText) } catch (e: Exception) { null }, p.amount)
            }
            val infos = mutableMapOf<Long, ExpenseInstallmentInfo>()
            val list = expensesList.mapNotNull { expense ->
                if (expense.installments > 0) {
                    val info = calculateExpenseInstallment(
                        installmentAmount = expense.amount,
                        installments = expense.installments,
                        startYearMonth = yearMonthOf(expense.date),
                        targetYearMonth = targetYearMonth,
                        payments = paymentsByExpense[expense.id].orEmpty()
                    ) ?: return@mapNotNull null
                    infos[expense.id] = info
                    expense.copy(amountCents = toCents(info.monthCharge))
                } else if (isInMonth(targetYearMonth, yearMonthOf(expense.date), expense.isRecurring, expense.endDateMillis)) {
                    expense
                } else null
            }
            list to infos
        }
    }

    fun getSavingsForMonth(targetYearMonth: YearMonth): Flow<List<SavingsEntity>> {
        return savings.map { savingsList ->
            savingsList.filter { saving ->
                isInMonth(targetYearMonth, yearMonthOf(saving.date), saving.isRecurring, saving.endDateMillis)
            }
        }
    }

    fun getCardMonthSummaries(targetYearMonth: YearMonth, usdRate: Double = DEFAULT_USD_RATE): Flow<List<CardMonthSummary>> {
        return combine(creditCards, cardPurchases, omittedInstallments, cardPurchasePayments, settledPurchases) { cards, purchases, omitted, payments, settled ->
            val omittedSet = omitted
                .filter { it.yearMonthText == targetYearMonth.toString() }
                .map { it.purchaseId }
                .toSet()

            // Cada abono conserva su mes para no afectar meses anteriores
            val paymentsMap = payments.groupBy({ it.purchaseId }) { payment ->
                PurchasePayment(
                    yearMonth = try { YearMonth.parse(payment.yearMonthText) } catch (e: Exception) { null },
                    amount = payment.amount
                )
            }
            val settledMap = settled.mapNotNull { s ->
                try {
                    s.purchaseId to YearMonth.parse(s.yearMonthText)
                } catch (e: Exception) {
                    null
                }
            }.toMap()

            cards.map { card ->
                val cardPurchasesList = purchases.filter { it.cardId == card.id }

                // Calculate installment details for target month (una sola vez por compra)
                val allDetails = cardPurchasesList.map { purchase ->
                    calculatePurchaseInstallmentDetail(
                        purchaseId = purchase.id,
                        cardId = card.id,
                        concept = purchase.concept,
                        totalAmount = purchase.totalAmount,
                        totalInstallments = purchase.installments,
                        purchaseDateMillis = purchase.purchaseDate,
                        targetYearMonth = targetYearMonth,
                        cutoffDay = card.cutoffDay,
                        paymentDay = card.paymentDay,
                        interestRate = card.interestRate,
                        isInterestFree = purchase.isInterestFree,
                        isFirstInstallmentInterestFree = card.isFirstInstallmentInterestFree,
                        isRecurring = purchase.isRecurring,
                        payments = paymentsMap[purchase.id].orEmpty(),
                        currency = purchase.currency,
                        settledYearMonth = settledMap[purchase.id]
                    )
                }

                // Only ACTIVE installments for this month, except purchases omitted for this month
                val activeMonthDetails = allDetails.filter {
                    it.purchaseId !in omittedSet && it.statusType == InstallmentStatusType.ACTIVE
                }

                // Calculate monthly payment sum for active installments in this month (converted to COP if USD)
                val totalActiveInstallments = activeMonthDetails.sumOf { it.monthlyPaymentInCop(usdRate) }

                // Total spent debt across all active and future purchases on this card (Cupo Gastado Total)
                val totalCardDebt = allDetails
                    .filter { it.statusType != InstallmentStatusType.FINISHED }
                    .sumOf { it.remainingBalanceToPayInCop(usdRate) }

                // Fee waiver is met if total card debt >= minSpendForFeeWaiver
                val isFeeWaived = card.isFeeWaiverEnabled &&
                        card.minSpendForFeeWaiver > 0 &&
                        totalCardDebt >= card.minSpendForFeeWaiver

                val summary = CardMonthSummary(
                    cardId = card.id,
                    cardName = card.name,
                    cutoffDay = card.cutoffDay,
                    paymentDay = card.paymentDay,
                    interestRate = card.interestRate,
                    maintenanceFee = card.maintenanceFee,
                    currency = card.currency,
                    isFeeWaiverEnabled = card.isFeeWaiverEnabled,
                    minSpendForFeeWaiver = card.minSpendForFeeWaiver,
                    isFeeWaivedThisMonth = isFeeWaived,
                    isFirstInstallmentInterestFree = card.isFirstInstallmentInterestFree,
                    currentMonthSpending = totalCardDebt, // Cupo Gastado Total
                    creditLimit = card.creditLimit,
                    colorIndex = card.colorIndex,
                    purchases = activeMonthDetails,
                    totalActiveInstallmentsPayment = totalActiveInstallments,
                    totalMonthlyCardExpense = 0.0
                )
                summary.copy(totalMonthlyCardExpense = summary.payableThisMonth(usdRate))
            }
        }
    }
}
