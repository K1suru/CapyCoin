package com.example.gestiongastos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIncomes(incomes: List<IncomeEntity>)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes")
    suspend fun clearAllIncomes()
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesList(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY id ASC")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards ORDER BY id ASC")
    suspend fun getCreditCardsList(): List<CreditCardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCreditCards(cards: List<CreditCardEntity>)

    @Update
    suspend fun updateCreditCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCreditCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards")
    suspend fun clearAllCreditCards()
}

@Dao
interface CardPurchaseDao {
    @Query("SELECT * FROM card_purchases ORDER BY purchaseDate DESC")
    fun getAllCardPurchases(): Flow<List<CardPurchaseEntity>>

    @Query("SELECT * FROM card_purchases WHERE cardId = :cardId ORDER BY purchaseDate DESC")
    fun getPurchasesByCard(cardId: Long): Flow<List<CardPurchaseEntity>>

    @Query("SELECT * FROM card_purchases WHERE cardId = :cardId ORDER BY purchaseDate DESC")
    suspend fun getPurchasesByCardList(cardId: Long): List<CardPurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardPurchase(purchase: CardPurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCardPurchases(purchases: List<CardPurchaseEntity>)

    @Update
    suspend fun updateCardPurchase(purchase: CardPurchaseEntity)

    @Delete
    suspend fun deleteCardPurchase(purchase: CardPurchaseEntity)

    @Query("DELETE FROM card_purchases WHERE id = :purchaseId")
    suspend fun deleteCardPurchaseById(purchaseId: Long)

    @Query("DELETE FROM card_purchases")
    suspend fun clearAllCardPurchases()
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY date DESC")
    fun getAllSavings(): Flow<List<SavingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: SavingsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavings(savings: List<SavingsEntity>)

    @Update
    suspend fun updateSavings(savings: SavingsEntity)

    @Delete
    suspend fun deleteSavings(savings: SavingsEntity)

    @Query("DELETE FROM savings")
    suspend fun clearAllSavings()
}

@Dao
interface OmittedInstallmentDao {
    @Query("SELECT * FROM omitted_installments")
    fun getAllOmittedInstallments(): Flow<List<OmittedInstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOmittedInstallment(item: OmittedInstallmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOmittedInstallments(items: List<OmittedInstallmentEntity>)

    @Query("DELETE FROM omitted_installments WHERE purchaseId = :purchaseId AND yearMonthText = :yearMonthText")
    suspend fun deleteOmittedInstallment(purchaseId: Long, yearMonthText: String)

    @Query("DELETE FROM omitted_installments WHERE purchaseId = :purchaseId")
    suspend fun deleteOmittedForPurchase(purchaseId: Long)

    @Query("DELETE FROM omitted_installments")
    suspend fun clearAllOmittedInstallments()
}

@Dao
interface CardPurchasePaymentDao {
    @Query("SELECT * FROM card_purchase_payments")
    fun getAllCardPurchasePayments(): Flow<List<CardPurchasePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CardPurchasePaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPayments(payments: List<CardPurchasePaymentEntity>)

    @Query("DELETE FROM card_purchase_payments WHERE purchaseId = :purchaseId")
    suspend fun deletePaymentsForPurchase(purchaseId: Long)

    @Query("DELETE FROM card_purchase_payments")
    suspend fun clearAllPayments()
}

@Dao
interface IgnoredExpenseDao {
    @Query("SELECT * FROM ignored_expenses")
    fun getAllIgnoredExpenses(): Flow<List<IgnoredExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIgnoredExpense(item: IgnoredExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIgnoredExpenses(items: List<IgnoredExpenseEntity>)

    @Query("DELETE FROM ignored_expenses WHERE itemKey = :itemKey AND yearMonthText = :yearMonthText")
    suspend fun deleteIgnoredExpense(itemKey: String, yearMonthText: String)

    @Query("DELETE FROM ignored_expenses WHERE itemKey = :itemKey")
    suspend fun deleteAllForItem(itemKey: String)

    @Query("DELETE FROM ignored_expenses")
    suspend fun clearAllIgnoredExpenses()
}

@Dao
interface PaidCreditCardDao {
    @Query("SELECT * FROM paid_credit_cards")
    fun getAllPaidCreditCards(): Flow<List<PaidCreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaidCreditCard(item: PaidCreditCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPaidCreditCards(items: List<PaidCreditCardEntity>)

    @Query("DELETE FROM paid_credit_cards WHERE cardId = :cardId AND yearMonthText = :yearMonthText")
    suspend fun deletePaidCreditCard(cardId: Long, yearMonthText: String)

    @Query("DELETE FROM paid_credit_cards WHERE cardId = :cardId")
    suspend fun deleteAllForCard(cardId: Long)

    @Query("DELETE FROM paid_credit_cards")
    suspend fun clearAllPaidCreditCards()
}

@Dao
interface PaidExpenseDao {
    @Query("SELECT * FROM paid_expenses")
    fun getAllPaidExpenses(): Flow<List<PaidExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaidExpense(item: PaidExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPaidExpenses(items: List<PaidExpenseEntity>)

    @Query("DELETE FROM paid_expenses WHERE itemKey = :itemKey AND yearMonthText = :yearMonthText")
    suspend fun deletePaidExpense(itemKey: String, yearMonthText: String)

    @Query("DELETE FROM paid_expenses WHERE itemKey = :itemKey")
    suspend fun deleteAllForItem(itemKey: String)

    @Query("DELETE FROM paid_expenses")
    suspend fun clearAllPaidExpenses()
}

@Dao
interface SettledPurchaseDao {
    @Query("SELECT * FROM settled_purchases")
    fun getAllSettledPurchases(): Flow<List<SettledPurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettledPurchase(item: SettledPurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSettledPurchases(items: List<SettledPurchaseEntity>)

    @Query("DELETE FROM settled_purchases WHERE purchaseId = :purchaseId AND yearMonthText = :yearMonthText")
    suspend fun deleteSettledPurchase(purchaseId: Long, yearMonthText: String)

    @Query("DELETE FROM settled_purchases WHERE purchaseId = :purchaseId")
    suspend fun deleteSettledPurchasesForPurchase(purchaseId: Long)

    @Query("DELETE FROM settled_purchases")
    suspend fun clearAllSettledPurchases()
}

@Dao
interface ReceivedIncomeDao {
    @Query("SELECT * FROM received_incomes")
    fun getAllReceivedIncomes(): Flow<List<ReceivedIncomeEntity>>

    @Query("SELECT * FROM received_incomes")
    suspend fun getReceivedIncomesList(): List<ReceivedIncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivedIncome(item: ReceivedIncomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReceivedIncomes(items: List<ReceivedIncomeEntity>)

    @Query("DELETE FROM received_incomes WHERE incomeId = :incomeId AND yearMonthText = :yearMonthText")
    suspend fun deleteReceivedIncome(incomeId: Long, yearMonthText: String)

    @Query("DELETE FROM received_incomes WHERE incomeId = :incomeId")
    suspend fun deleteAllForIncome(incomeId: Long)

    @Query("DELETE FROM received_incomes")
    suspend fun clearAllReceivedIncomes()
}

