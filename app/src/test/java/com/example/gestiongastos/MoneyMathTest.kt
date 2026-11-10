package com.example.gestiongastos

import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.ExtraPaymentCheck
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.data.model.checkExtraPayment
import com.example.gestiongastos.data.model.incomeInstallmentNumber
import com.example.gestiongastos.data.model.monthUsesUsd
import com.example.gestiongastos.data.model.safePercent
import com.example.gestiongastos.data.model.safeRatio
import com.example.gestiongastos.data.repository.localDateOf
import com.example.gestiongastos.data.repository.yearMonthOf
import com.example.gestiongastos.data.repository.yearMonthOrNull
import com.example.gestiongastos.ui.components.currencySymbolOf
import com.example.gestiongastos.ui.components.formatUsdWithCop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyMathTest {

    @Test
    fun ratioNeverReturnsNaNOrInfinity() {
        assertEquals(0.0, safeRatio(0.0, 0.0), 0.0)        // 0/0 sería NaN
        assertEquals(0.0, safeRatio(500.0, 0.0), 0.0)      // x/0 sería infinito
        assertEquals(0.0, safeRatio(500.0, -10.0), 0.0)
        assertEquals(0.0, safeRatio(Double.NaN, 100.0), 0.0)
        assertEquals(0.0, safeRatio(100.0, Double.NaN), 0.0)
        assertEquals(0.0, safeRatio(Double.POSITIVE_INFINITY, 100.0), 0.0)
        assertEquals(0.25, safeRatio(250.0, 1000.0), 1e-12)
    }

    @Test
    fun percentIsTruncatedAndCanExceedOneHundred() {
        assertEquals(33, safePercent(1.0, 3.0))
        assertEquals(120, safePercent(1_200_000.0, 1_000_000.0)) // cupo sobrepasado
        assertEquals(0, safePercent(500.0, 0.0))
    }

    @Test
    fun extraPaymentMustBePositive() {
        assertEquals(ExtraPaymentCheck.NotPositive, checkExtraPayment(0.0, 1000.0, "COP"))
        assertEquals(ExtraPaymentCheck.NotPositive, checkExtraPayment(-5.0, 1000.0, "COP"))
        assertEquals(ExtraPaymentCheck.NotPositive, checkExtraPayment(Double.NaN, 1000.0, "COP"))
    }

    @Test
    fun extraPaymentCannotExceedBalance() {
        assertEquals(ExtraPaymentCheck.ExceedsBalance, checkExtraPayment(100_001.0, 100_000.0, "COP"))
        assertEquals(ExtraPaymentCheck.ExceedsBalance, checkExtraPayment(33.35, 33.336, "USD"))
        assertEquals(ExtraPaymentCheck.ExceedsBalance, checkExtraPayment(10.0, Double.NaN, "COP"))
        assertEquals(ExtraPaymentCheck.Valid(40_000.0), checkExtraPayment(40_000.0, 100_000.0, "COP"))
    }

    @Test
    fun payingTheBalanceShownOnScreenIsAccepted() {
        // Saldo real 99.999,6 (por intereses): en pantalla se ve "$ 100.000". Antes, en tarjetas,
        // abonar esos 100.000 se rechazaba por "mayor al saldo". Ahora se acepta y se guarda el saldo real.
        val cop = checkExtraPayment(100_000.0, 99_999.6, "COP")
        assertTrue(cop is ExtraPaymentCheck.Valid)
        assertEquals(99_999.6, (cop as ExtraPaymentCheck.Valid).amount, 1e-9)

        // En dólares el saldo 33,336 se ve como 33,34
        val usd = checkExtraPayment(33.34, 33.336, "USD")
        assertEquals(33.336, (usd as ExtraPaymentCheck.Valid).amount, 1e-9)
    }

    @Test
    fun exchangeRateOnlyMattersWhenTheMonthHasDollars() {
        val cop = ExpenseEntity(id = 1, concept = "Arriendo", amountCents = toCents(1_000_000.0), date = 0)
        val purchase = PurchaseInstallmentDetail(
            purchaseId = 1, cardId = 1, concept = "Curso", totalAmount = 30.0, totalInstallments = 1, currentInstallment = 1,
            monthlyPayment = 30.0, statusType = InstallmentStatusType.ACTIVE, statusText = "", purchaseDateMillis = 0, currency = "USD"
        )
        fun card(purchases: List<PurchaseInstallmentDetail>, currency: String = "COP", fee: Double = 0.0, waived: Boolean = false) =
            CardMonthSummary(
                cardId = 1, cardName = "Visa", cutoffDay = 15, interestRate = 0.0, maintenanceFee = fee, currency = currency,
                isFeeWaivedThisMonth = waived, purchases = purchases, totalActiveInstallmentsPayment = 0.0, totalMonthlyCardExpense = 0.0
            )

        assertFalse(monthUsesUsd(listOf(cop), listOf(card(listOf(purchase.copy(currency = "COP"))))))
        assertTrue(monthUsesUsd(listOf(cop, cop.copy(id = 2, currency = "USD")), emptyList()))
        assertTrue(monthUsesUsd(emptyList(), listOf(card(listOf(purchase)))))
        // Una compra en dólares que ya terminó o aún no empieza no entra en los totales del mes
        assertFalse(monthUsesUsd(emptyList(), listOf(card(listOf(purchase.copy(statusType = InstallmentStatusType.FINISHED))))))
        // Cuota de manejo en dólares: cuenta salvo que esté exonerada
        assertTrue(monthUsesUsd(emptyList(), listOf(card(emptyList(), currency = "USD", fee = 5.0))))
        assertFalse(monthUsesUsd(emptyList(), listOf(card(emptyList(), currency = "USD", fee = 5.0, waived = true))))
    }

    @Test
    fun incomeInstallmentNumberStaysBetweenOneAndTheTotal() {
        val date = java.time.LocalDate.of(2026, 11, 20).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val income = com.example.gestiongastos.data.local.IncomeEntity(concept = "Venta", amountCents = toCents(100.0), date = date, totalInstallments = 3)
        val nov = java.time.YearMonth.of(2026, 11)

        assertEquals(1, incomeInstallmentNumber(income, nov))
        assertEquals(3, incomeInstallmentNumber(income, nov.plusMonths(2))) // cruza de año: enero de 2027
        assertEquals(3, incomeInstallmentNumber(income, nov.plusMonths(9))) // después de terminar
        assertEquals(1, incomeInstallmentNumber(income, nov.minusMonths(4))) // antes de empezar
        assertEquals(1, incomeInstallmentNumber(income.copy(totalInstallments = 1), nov.plusMonths(5)))
        // Un total inválido no debe tumbar la pantalla: antes (1).coerceIn(1, 0) lanzaba una excepción
        assertEquals(1, incomeInstallmentNumber(income.copy(totalInstallments = 0), nov))
    }

    @Test
    fun dollarsAreShownWithTheirPesoEquivalent() {
        assertEquals("USD $33.34 (~$ 109.018)", formatUsdWithCop(33.34, 3269.88))
        assertEquals("USD $0.00 (~$ 0)", formatUsdWithCop(0.0, 4000.0))
        assertEquals("USD $", currencySymbolOf("USD"))
        assertEquals("$", currencySymbolOf("COP"))
        assertEquals("$", currencySymbolOf("EUR")) // cualquier otra cosa se trata como pesos
    }

    @Test
    fun storedInstantsBecomeLocalDates() {
        val zone = java.time.ZoneId.systemDefault()
        val noon = java.time.LocalDate.of(2026, 10, 31).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        assertEquals(java.time.LocalDate.of(2026, 10, 31), localDateOf(noon))
        assertEquals(java.time.YearMonth.of(2026, 10), yearMonthOf(noon))
        // El último instante del mes sigue siendo de ese mes y el siguiente milisegundo ya no
        val lastMillis = java.time.LocalDate.of(2026, 11, 1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        assertEquals(java.time.YearMonth.of(2026, 10), yearMonthOf(lastMillis))
        assertEquals(java.time.YearMonth.of(2026, 11), yearMonthOf(lastMillis + 1))
        // Un valor absurdo no tumba la app
        localDateOf(Long.MAX_VALUE)
        localDateOf(Long.MIN_VALUE)
    }

    @Test
    fun storedMonthTextIsReadOrIgnored() {
        assertEquals(java.time.YearMonth.of(2026, 8), yearMonthOrNull("2026-08"))
        assertEquals(java.time.YearMonth.of(2026, 8), yearMonthOrNull(" 2026-08 "))
        // Un texto que no es un mes no tumba la app ni se convierte en otro mes
        listOf(null, "", "2026-13", "agosto", "2026-8", "2026-08-01").forEach {
            assertEquals("yearMonthOrNull($it)", null, yearMonthOrNull(it))
        }
    }
}
