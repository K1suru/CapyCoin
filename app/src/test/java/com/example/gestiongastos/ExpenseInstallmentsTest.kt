package com.example.gestiongastos

import com.example.gestiongastos.data.model.PurchasePayment
import com.example.gestiongastos.data.model.calculateExpenseInstallment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class ExpenseInstallmentsTest {
    private val start = YearMonth.of(2026, 11)
    private fun at(ym: YearMonth, payments: List<PurchasePayment> = emptyList()) =
        calculateExpenseInstallment(385_000.0, 36, start, ym, payments)

    @Test
    fun countsInstallmentsMonthByMonth() {
        assertNull(at(YearMonth.of(2026, 10)))
        val first = at(start)!!
        assertEquals(1, first.currentInstallment)
        assertEquals(36, first.totalInstallments)
        assertEquals(385_000.0, first.monthCharge, 0.01)
        assertEquals(385_000.0 * 36, first.remainingBalance, 0.01)
        assertEquals(2, at(YearMonth.of(2026, 12))!!.currentInstallment)
        assertEquals(36, at(start.plusMonths(35))!!.currentInstallment)
        assertNull(at(start.plusMonths(36)))
    }

    @Test
    fun extraPaymentLowersBalanceKeepsInstallmentValue() {
        // Abono de 2 cuotas en el mes 3: termina 2 meses antes y la cuota sigue igual
        val pay = listOf(PurchasePayment(start.plusMonths(2), 770_000.0))
        assertEquals(36, at(start.plusMonths(1), pay)!!.totalInstallments) // antes del abono no cambia
        val m3 = at(start.plusMonths(2), pay)!!
        assertEquals(34, m3.totalInstallments)
        assertEquals(385_000.0, m3.monthCharge, 0.01)
        assertEquals(385_000.0 * 32, m3.remainingBalance, 0.01)
        assertNull(at(start.plusMonths(34), pay))
    }

    @Test
    fun partialExtraPaymentMakesLastInstallmentSmaller() {
        val pay = listOf(PurchasePayment(start, 100_000.0))
        val last = at(start.plusMonths(35), pay)!!
        assertEquals(36, last.totalInstallments)
        assertEquals(285_000.0, last.monthCharge, 0.01)
    }

    @Test
    fun payingEverythingFinishesThisMonth() {
        val pay = listOf(PurchasePayment(start.plusMonths(4), 385_000.0 * 32))
        val m5 = at(start.plusMonths(4), pay)!!
        assertTrue(m5.isPaidOff)
        assertEquals(0.0, m5.monthCharge, 0.01)
        assertNull(at(start.plusMonths(5), pay))
    }
}
