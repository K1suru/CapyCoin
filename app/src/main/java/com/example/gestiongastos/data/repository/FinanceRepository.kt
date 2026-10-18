package com.example.gestiongastos.data.repository

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
import com.example.gestiongastos.data.model.payableThisMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

import com.example.gestiongastos.data.local.IgnoredExpenseEntity
import com.example.gestiongastos.data.local.PaidCreditCardEntity
import com.example.gestiongastos.data.local.PaidExpenseEntity
import com.example.gestiongastos.data.local.ReceivedIncomeEntity
import com.example.gestiongastos.data.local.SettledPurchaseEntity
import kotlinx.coroutines.flow.first
import androidx.room.withTransaction
import com.example.gestiongastos.data.model.ItemKeys

/** Mes (zona local) de un timestamp en milisegundos. */
fun yearMonthOf(millis: Long): YearMonth = try {
    YearMonth.from(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
} catch (e: Exception) {
    YearMonth.now()
}

/** Un registro único aplica solo en su mes; uno recurrente desde su mes hasta [endDateMillis] (incluido). */
fun isInMonth(target: YearMonth, start: YearMonth, isRecurring: Boolean, endDateMillis: Long?): Boolean {
    if (!isRecurring) return target == start
    if (target.isBefore(start)) return false
    return endDateMillis == null || !target.isAfter(yearMonthOf(endDateMillis))
}

/** Timestamp del último instante de [yearMonth], usado como fecha de fin de un recurrente. */
fun endOfMonthMillis(yearMonth: YearMonth): Long =
    yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

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

    suspend fun toggleReceivedIncome(incomeId: Long, targetYearMonth: YearMonth) {
        val ymText = targetYearMonth.toString()
        val currentReceived = db.receivedIncomeDao().getAllReceivedIncomes().first()
        val isCurrentlyReceived = currentReceived.any { it.incomeId == incomeId && it.yearMonthText == ymText }
        if (isCurrentlyReceived) {
            db.receivedIncomeDao().deleteReceivedIncome(incomeId, ymText)
        } else {
            db.receivedIncomeDao().insertReceivedIncome(
                ReceivedIncomeEntity(incomeId = incomeId, yearMonthText = ymText)
            )
        }
    }

    suspend fun toggleIgnoredExpense(itemKey: String, targetYearMonth: YearMonth) {
        val currentIgnored = db.ignoredExpenseDao().getAllIgnoredExpenses().first()
        val isCurrentlyIgnored = currentIgnored.any { it.itemKey == itemKey && it.yearMonthText == targetYearMonth.toString() }
        if (isCurrentlyIgnored) {
            db.ignoredExpenseDao().deleteIgnoredExpense(itemKey, targetYearMonth.toString())
        } else {
            db.ignoredExpenseDao().insertIgnoredExpense(
                IgnoredExpenseEntity(itemKey = itemKey, yearMonthText = targetYearMonth.toString())
            )
        }
    }

    suspend fun togglePaidCreditCard(cardId: Long, targetYearMonth: YearMonth) {
        val ymText = targetYearMonth.toString()
        val currentPaid = db.paidCreditCardDao().getAllPaidCreditCards().first()
        val isCurrentlyPaid = currentPaid.any { it.cardId == cardId && it.yearMonthText == ymText }
        if (isCurrentlyPaid) {
            db.paidCreditCardDao().deletePaidCreditCard(cardId, ymText)
        } else {
            db.paidCreditCardDao().insertPaidCreditCard(
                PaidCreditCardEntity(cardId = cardId, yearMonthText = ymText)
            )
        }
    }

    suspend fun togglePaidExpense(itemKey: String, targetYearMonth: YearMonth) {
        val ymText = targetYearMonth.toString()
        val currentPaid = db.paidExpenseDao().getAllPaidExpenses().first()
        val isCurrentlyPaid = currentPaid.any { it.itemKey == itemKey && it.yearMonthText == ymText }
        if (isCurrentlyPaid) {
            db.paidExpenseDao().deletePaidExpense(itemKey, ymText)
        } else {
            db.paidExpenseDao().insertPaidExpense(
                PaidExpenseEntity(itemKey = itemKey, yearMonthText = ymText)
            )
        }
    }

    suspend fun toggleSettlePurchase(purchaseId: Long, targetYearMonth: YearMonth) {
        val ymText = targetYearMonth.toString()
        val currentSettled = db.settledPurchaseDao().getAllSettledPurchases().first()
        val existing = currentSettled.firstOrNull { it.purchaseId == purchaseId }
        if (existing != null) {
            db.settledPurchaseDao().deleteSettledPurchase(purchaseId, existing.yearMonthText)
            if (existing.yearMonthText != ymText) {
                db.settledPurchaseDao().insertSettledPurchase(
                    SettledPurchaseEntity(purchaseId = purchaseId, yearMonthText = ymText)
                )
            }
        } else {
            db.settledPurchaseDao().insertSettledPurchase(
                SettledPurchaseEntity(purchaseId = purchaseId, yearMonthText = ymText)
            )
        }
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
            incomesList.filter { income ->
                val entryYearMonth = yearMonthOf(income.date)
                if (income.totalInstallments > 1) {
                    val diffMonths = (targetYearMonth.year - entryYearMonth.year) * 12 + (targetYearMonth.monthValue - entryYearMonth.monthValue)
                    diffMonths in 0 until income.totalInstallments
                } else {
                    isInMonth(targetYearMonth, entryYearMonth, income.isRecurring, income.endDateMillis)
                }
            }
        }
    }

    fun getExpensesForMonth(targetYearMonth: YearMonth): Flow<List<ExpenseEntity>> {
        return expenses.map { expensesList ->
            expensesList.filter { expense ->
                isInMonth(targetYearMonth, yearMonthOf(expense.date), expense.isRecurring, expense.endDateMillis)
            }
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
