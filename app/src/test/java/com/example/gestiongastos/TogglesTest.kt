package com.example.gestiongastos

import com.example.gestiongastos.data.local.IgnoredExpenseDao
import com.example.gestiongastos.data.local.IgnoredExpenseEntity
import com.example.gestiongastos.data.local.PaidCreditCardDao
import com.example.gestiongastos.data.local.PaidCreditCardEntity
import com.example.gestiongastos.data.local.PaidExpenseDao
import com.example.gestiongastos.data.local.PaidExpenseEntity
import com.example.gestiongastos.data.local.ReceivedIncomeDao
import com.example.gestiongastos.data.local.ReceivedIncomeEntity
import com.example.gestiongastos.data.local.SettledPurchaseDao
import com.example.gestiongastos.data.local.SettledPurchaseEntity
import com.example.gestiongastos.data.repository.toggleIgnoredExpense
import com.example.gestiongastos.data.repository.togglePaidCreditCard
import com.example.gestiongastos.data.repository.togglePaidExpense
import com.example.gestiongastos.data.repository.toggleReceivedIncome
import com.example.gestiongastos.data.repository.toggleSettledPurchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Marcar/desmarcar (recibido, pagado, ignorado, liquidado) con DAOs de mentira en memoria.
 * Leer la tabla entera lanza un error: así se comprueba que el toggle ya no lo hace.
 * (Que todo vaya en una transacción de Room solo se puede comprobar en un dispositivo.)
 */
class TogglesTest {

    private class FakeReceived(val rows: MutableList<ReceivedIncomeEntity> = mutableListOf()) : ReceivedIncomeDao {
        override fun getAllReceivedIncomes(): Flow<List<ReceivedIncomeEntity>> = throw AssertionError("lectura completa")
        override suspend fun insertReceivedIncome(item: ReceivedIncomeEntity): Long { rows.add(item); return rows.size.toLong() }
        override suspend fun insertAllReceivedIncomes(items: List<ReceivedIncomeEntity>) { rows.addAll(items) }
        override suspend fun deleteReceivedIncome(incomeId: Long, yearMonthText: String): Int {
            val before = rows.size
            rows.removeAll { it.incomeId == incomeId && it.yearMonthText == yearMonthText }
            return before - rows.size
        }
        override suspend fun deleteAllForIncome(incomeId: Long) { rows.removeAll { it.incomeId == incomeId } }
        override suspend fun clearAllReceivedIncomes() { rows.clear() }
    }

    private class FakeIgnored(val rows: MutableList<IgnoredExpenseEntity> = mutableListOf()) : IgnoredExpenseDao {
        override fun getAllIgnoredExpenses(): Flow<List<IgnoredExpenseEntity>> = throw AssertionError("lectura completa")
        override suspend fun insertIgnoredExpense(item: IgnoredExpenseEntity): Long { rows.add(item); return rows.size.toLong() }
        override suspend fun insertAllIgnoredExpenses(items: List<IgnoredExpenseEntity>) { rows.addAll(items) }
        override suspend fun deleteIgnoredExpense(itemKey: String, yearMonthText: String): Int {
            val before = rows.size
            rows.removeAll { it.itemKey == itemKey && it.yearMonthText == yearMonthText }
            return before - rows.size
        }
        override suspend fun deleteAllForItem(itemKey: String) { rows.removeAll { it.itemKey == itemKey } }
        override suspend fun clearAllIgnoredExpenses() { rows.clear() }
    }

    private class FakePaidCards(val rows: MutableList<PaidCreditCardEntity> = mutableListOf()) : PaidCreditCardDao {
        override fun getAllPaidCreditCards(): Flow<List<PaidCreditCardEntity>> = throw AssertionError("lectura completa")
        override suspend fun insertPaidCreditCard(item: PaidCreditCardEntity): Long { rows.add(item); return rows.size.toLong() }
        override suspend fun insertAllPaidCreditCards(items: List<PaidCreditCardEntity>) { rows.addAll(items) }
        override suspend fun deletePaidCreditCard(cardId: Long, yearMonthText: String): Int {
            val before = rows.size
            rows.removeAll { it.cardId == cardId && it.yearMonthText == yearMonthText }
            return before - rows.size
        }
        override suspend fun deleteAllForCard(cardId: Long) { rows.removeAll { it.cardId == cardId } }
        override suspend fun clearAllPaidCreditCards() { rows.clear() }
    }

    private class FakePaidExpenses(val rows: MutableList<PaidExpenseEntity> = mutableListOf()) : PaidExpenseDao {
        override fun getAllPaidExpenses(): Flow<List<PaidExpenseEntity>> = throw AssertionError("lectura completa")
        override suspend fun insertPaidExpense(item: PaidExpenseEntity): Long { rows.add(item); return rows.size.toLong() }
        override suspend fun insertAllPaidExpenses(items: List<PaidExpenseEntity>) { rows.addAll(items) }
        override suspend fun deletePaidExpense(itemKey: String, yearMonthText: String): Int {
            val before = rows.size
            rows.removeAll { it.itemKey == itemKey && it.yearMonthText == yearMonthText }
            return before - rows.size
        }
        override suspend fun deleteAllForItem(itemKey: String) { rows.removeAll { it.itemKey == itemKey } }
        override suspend fun clearAllPaidExpenses() { rows.clear() }
    }

    private class FakeSettled(val rows: MutableList<SettledPurchaseEntity> = mutableListOf()) : SettledPurchaseDao {
        override fun getAllSettledPurchases(): Flow<List<SettledPurchaseEntity>> = throw AssertionError("lectura completa")
        override suspend fun insertSettledPurchase(item: SettledPurchaseEntity): Long { rows.add(item); return rows.size.toLong() }
        override suspend fun insertAllSettledPurchases(items: List<SettledPurchaseEntity>) { rows.addAll(items) }
        override suspend fun getSettledForPurchase(purchaseId: Long) = rows.filter { it.purchaseId == purchaseId }
        override suspend fun deleteSettledPurchasesForPurchase(purchaseId: Long) { rows.removeAll { it.purchaseId == purchaseId } }
        override suspend fun clearAllSettledPurchases() { rows.clear() }
    }

    @Test
    fun receivedIncomeTogglesOnAndOffWithoutTouchingOtherMonths() = runBlocking {
        val dao = FakeReceived(mutableListOf(ReceivedIncomeEntity(incomeId = 7, yearMonthText = "2026-10")))

        assertTrue(toggleReceivedIncome(dao, 7, "2026-11"))
        assertEquals(listOf("2026-10", "2026-11"), dao.rows.map { it.yearMonthText })

        assertFalse(toggleReceivedIncome(dao, 7, "2026-11"))
        assertEquals(listOf("2026-10"), dao.rows.map { it.yearMonthText })
    }

    @Test
    fun toggleOffRemovesDuplicatedMarks() = runBlocking {
        // Dos toques seguidos con el código anterior podían dejar la marca repetida
        val dao = FakePaidExpenses(mutableListOf(
            PaidExpenseEntity(itemKey = "direct_1", yearMonthText = "2026-11"),
            PaidExpenseEntity(itemKey = "direct_1", yearMonthText = "2026-11")
        ))
        assertFalse(togglePaidExpense(dao, "direct_1", "2026-11"))
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun ignoredAndPaidCardToggles() = runBlocking {
        val ignored = FakeIgnored()
        assertTrue(toggleIgnoredExpense(ignored, "card_fee_3", "2026-11"))
        assertEquals(1, ignored.rows.size)
        assertFalse(toggleIgnoredExpense(ignored, "card_fee_3", "2026-11"))
        assertTrue(ignored.rows.isEmpty())

        val paid = FakePaidCards()
        assertTrue(togglePaidCreditCard(paid, 3, "2026-11"))
        assertTrue(togglePaidCreditCard(paid, 3, "2026-12"))
        assertFalse(togglePaidCreditCard(paid, 3, "2026-11"))
        assertEquals(listOf("2026-12"), paid.rows.map { it.yearMonthText })
    }

    @Test
    fun settledPurchaseLivesInASingleMonth() = runBlocking {
        val dao = FakeSettled()
        assertTrue(toggleSettledPurchase(dao, 5, "2026-04"))
        // Liquidarla en otro mes la mueve; nunca queda liquidada en dos meses
        assertTrue(toggleSettledPurchase(dao, 5, "2026-06"))
        assertEquals(listOf("2026-06"), dao.rows.map { it.yearMonthText })
        // Repetir en el mismo mes revierte la liquidación
        assertFalse(toggleSettledPurchase(dao, 5, "2026-06"))
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun settleToggleCleansUpInconsistentRows() = runBlocking {
        // Datos viejos con la compra liquidada en dos meses: revertir debe quitar ambas filas
        val dao = FakeSettled(mutableListOf(
            SettledPurchaseEntity(purchaseId = 5, yearMonthText = "2026-04"),
            SettledPurchaseEntity(purchaseId = 5, yearMonthText = "2026-06"),
            SettledPurchaseEntity(purchaseId = 9, yearMonthText = "2026-06")
        ))
        assertFalse(toggleSettledPurchase(dao, 5, "2026-06"))
        assertEquals(listOf(9L), dao.rows.map { it.purchaseId })
    }
}
