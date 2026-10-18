package com.example.gestiongastos

import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.PurchasePayment
import com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.i18n.AppLanguage
import com.example.gestiongastos.i18n.I18n
import com.example.gestiongastos.data.repository.endOfMonthMillis
import com.example.gestiongastos.data.repository.isInMonth
import com.example.gestiongastos.ui.components.csvNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CorrectionsTest {

    private fun millisOf(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun detail(
        target: YearMonth,
        payments: List<PurchasePayment> = emptyList(),
        installments: Int = 12,
        total: Double = 1_200_000.0
    ) = calculatePurchaseInstallmentDetail(
        purchaseId = 1L,
        cardId = 1L,
        concept = "TV",
        totalAmount = total,
        totalInstallments = installments,
        purchaseDateMillis = millisOf(2026, 1, 5),
        targetYearMonth = target,
        isInterestFree = true,
        payments = payments
    )

    @Test
    fun futurePurchaseDebtIsNotInflated() {
        // Compra de enero vista en diciembre del año anterior: aún no empieza
        val future = detail(YearMonth.of(2025, 12))
        assertEquals(InstallmentStatusType.FUTURE, future.statusType)
        assertEquals(12, future.remainingInstallmentsCount)
        assertEquals(1_200_000.0, future.remainingBalanceToPay, 0.01)
    }

    @Test
    fun paymentDoesNotChangePreviousMonths() {
        val payments = listOf(PurchasePayment(YearMonth.of(2026, 6), 600_000.0))

        // Antes del abono la cuota es la normal
        assertEquals(100_000.0, detail(YearMonth.of(2026, 3), payments).monthlyPayment, 0.01)
        assertEquals(0.0, detail(YearMonth.of(2026, 3), payments).totalPaidAmount, 0.01)

        // En junio: saldo 700.000 - 600.000 = 100.000 repartido en las 7 cuotas restantes
        val june = detail(YearMonth.of(2026, 6), payments)
        assertEquals(100_000.0, june.remainingBalanceToPay, 0.01)
        assertEquals(100_000.0 / 7, june.monthlyPayment, 0.01)

        // La cuota reducida se mantiene hasta el final y el total cuadra
        val july = detail(YearMonth.of(2026, 7), payments)
        assertEquals(100_000.0 / 7, july.monthlyPayment, 0.01)
        assertEquals(InstallmentStatusType.FINISHED, detail(YearMonth.of(2027, 1), payments).statusType)
    }

    @Test
    fun fullPaymentFinishesPurchase() {
        val payments = listOf(PurchasePayment(YearMonth.of(2026, 2), 1_100_000.0))
        assertEquals(InstallmentStatusType.FINISHED, detail(YearMonth.of(2026, 3), payments).statusType)
        assertEquals(InstallmentStatusType.ACTIVE, detail(YearMonth.of(2026, 1), payments).statusType)
    }

    @Test
    fun scheduleMatchesFrenchFormulaAndSingleInstallmentHasNoInterest() {
        val schedule = installmentSchedule(1_000_000.0, 12, 2.0, isInterestFree = false, isFirstInstallmentInterestFree = false)
        assertTrue(schedule.all { it in 94_500.0..94_600.0 })
        assertEquals(listOf(250_000.0), installmentSchedule(250_000.0, 1, 2.0, false, false))
    }

    @Test
    fun csvNumbersHaveNoScientificNotation() {
        assertEquals("12000000", csvNumber(12_000_000.0))
        assertEquals("1234,5", csvNumber(1234.5))
        assertEquals("-50000", csvNumber(-50_000.0))
    }

    @Test
    fun recurringEndDateKeepsHistory() {
        val start = YearMonth.of(2026, 1)
        val end = endOfMonthMillis(YearMonth.of(2026, 4))
        assertTrue(isInMonth(YearMonth.of(2026, 4), start, true, end))
        assertFalse(isInMonth(YearMonth.of(2026, 5), start, true, end))
        assertFalse(isInMonth(YearMonth.of(2025, 12), start, true, end))
        assertTrue(isInMonth(YearMonth.of(2030, 1), start, true, null))
    }

    @Test
    fun statusTextFollowsAppLanguage() {
        try {
            I18n.language = AppLanguage.EN
            assertEquals("Installment 3 of 12", detail(YearMonth.of(2026, 3)).statusText)
            I18n.language = AppLanguage.ES
            assertEquals("Cuota 3 de 12", detail(YearMonth.of(2026, 3)).statusText)
        } finally {
            I18n.language = AppLanguage.ES
        }
    }
}
