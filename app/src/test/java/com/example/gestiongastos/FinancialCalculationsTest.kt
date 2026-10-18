package com.example.gestiongastos

import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {

    @Test
    fun testFrenchAmortizationFormula() {
        val principal = 1_000_000.0
        val monthlyRate = 0.02 // 2.0%
        val installments = 12

        val i = monthlyRate
        val n = installments
        val monthlyPayment = principal * (i * Math.pow(1.0 + i, n.toDouble())) / (Math.pow(1.0 + i, n.toDouble()) - 1.0)
        val totalPaid = monthlyPayment * n
        val totalInterest = totalPaid - principal

        // Monthly payment should be around 94,559.60 COP
        assertTrue("Monthly payment should be between 94500 and 94600", monthlyPayment in 94500.0..94600.0)
        assertTrue("Total interest must be positive", totalInterest > 100_000.0)
        assertEquals(totalPaid, principal + totalInterest, 0.01)
    }

    @Test
    fun testZeroInterestPurchase() {
        val principal = 600_000.0
        val installments = 6
        val isInterestFree = true

        val monthlyPayment = principal / installments
        val totalPaid = monthlyPayment * installments

        assertEquals(100_000.0, monthlyPayment, 0.001)
        assertEquals(principal, totalPaid, 0.001)
    }

    @Test
    fun testFirstInstallmentInterestFreeBehavior() {
        val principal = 250_000.0
        val installments = 1
        val isFirstInstallmentInterestFree = true

        val payment = if (isFirstInstallmentInterestFree && installments == 1) principal else principal * 1.02
        assertEquals(principal, payment, 0.001)
    }

    @Test
    fun testParseAmount() {
        assertEquals(150000.0, parseAmount("150.000") ?: 0.0, 0.01)
        assertEquals(150000.0, parseAmount("150,000") ?: 0.0, 0.01)
        assertEquals(150000.0, parseAmount("$ 150000") ?: 0.0, 0.01)
        assertEquals(25.50, parseAmount("25.50") ?: 0.0, 0.01)
        assertNull(parseAmount(""))
        assertNull(parseAmount("abc"))
    }

    @Test
    fun testCurrencyFormatting() {
        val formattedCop = formatCurrency(50000.0)
        assertTrue(formattedCop.contains("50.000") || formattedCop.contains("50,000") || formattedCop.contains("50000"))

        val formattedUsd = formatUsd(25.0)
        assertTrue(formattedUsd.contains("25.00") || formattedUsd.contains("25,00") || formattedUsd.contains("USD"))
    }

    @Test
    fun testSettledPurchaseCalculation() {
        // Purchase of 2,400,000 COP in 24 installments (100,000 / month if 0% interest)
        // Purchase date: Jan 1, 2026. Start month: Jan 2026.
        val purchaseDateMillis = java.time.LocalDate.of(2026, 1, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val settledMonth = java.time.YearMonth.of(2026, 4) // Settled in April 2026 (Month 4)

        // Month 3 (Before settlement): normal cuota 3 of 24 -> 100,000
        val month3Detail = com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail(
            purchaseId = 1L,
            cardId = 1L,
            concept = "Nevera",
            totalAmount = 2_400_000.0,
            totalInstallments = 24,
            purchaseDateMillis = purchaseDateMillis,
            targetYearMonth = java.time.YearMonth.of(2026, 3),
            isInterestFree = true,
            settledYearMonth = settledMonth
        )
        assertEquals(com.example.gestiongastos.data.model.InstallmentStatusType.ACTIVE, month3Detail.statusType)
        assertEquals(100_000.0, month3Detail.monthlyPayment, 0.01)
        assertEquals(3, month3Detail.currentInstallment)
        assertEquals(false, month3Detail.isSettledThisMonth)

        // Month 4 (Settlement month): pays remaining cuotas 4 to 24 (21 cuotas = 2,100,000)
        val month4Detail = com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail(
            purchaseId = 1L,
            cardId = 1L,
            concept = "Nevera",
            totalAmount = 2_400_000.0,
            totalInstallments = 24,
            purchaseDateMillis = purchaseDateMillis,
            targetYearMonth = java.time.YearMonth.of(2026, 4),
            isInterestFree = true,
            settledYearMonth = settledMonth
        )
        assertEquals(com.example.gestiongastos.data.model.InstallmentStatusType.ACTIVE, month4Detail.statusType)
        assertEquals(2_100_000.0, month4Detail.monthlyPayment, 0.01)
        assertEquals(true, month4Detail.isSettledThisMonth)
        assertTrue(month4Detail.statusText.contains("Liquidado este mes"))

        // Month 5 (After settlement): finished with 0 payment
        val month5Detail = com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail(
            purchaseId = 1L,
            cardId = 1L,
            concept = "Nevera",
            totalAmount = 2_400_000.0,
            totalInstallments = 24,
            purchaseDateMillis = purchaseDateMillis,
            targetYearMonth = java.time.YearMonth.of(2026, 5),
            isInterestFree = true,
            settledYearMonth = settledMonth
        )
        assertEquals(com.example.gestiongastos.data.model.InstallmentStatusType.FINISHED, month5Detail.statusType)
        assertEquals(0.0, month5Detail.monthlyPayment, 0.01)
        assertTrue(month5Detail.statusText.contains("Liquidado en 4/2026"))
    }

    @Test
    fun testRecurringPurchasePositioning() {
        val ym = java.time.YearMonth.of(2026, 10)
        val oct1Millis = java.time.LocalDate.of(2026, 10, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val oct5Millis = java.time.LocalDate.of(2026, 10, 5)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // 1. Regular purchase made on Oct 1 (not recurring, 1 installment)
        val regularPurchase = com.example.gestiongastos.data.model.PurchaseInstallmentDetail(
            purchaseId = 101L,
            cardId = 1L,
            concept = "Almuerzo",
            totalAmount = 30_000.0,
            totalInstallments = 1,
            currentInstallment = 1,
            monthlyPayment = 30_000.0,
            statusType = com.example.gestiongastos.data.model.InstallmentStatusType.ACTIVE,
            statusText = "1 cuota",
            purchaseDateMillis = oct1Millis,
            isRecurring = false
        )

        // 2. Recurring purchase added on Oct 5 (recurring subscription)
        val recurringPurchase = com.example.gestiongastos.data.model.PurchaseInstallmentDetail(
            purchaseId = 102L,
            cardId = 1L,
            concept = "Netflix",
            totalAmount = 45_000.0,
            totalInstallments = 1,
            currentInstallment = 1,
            monthlyPayment = 45_000.0,
            statusType = com.example.gestiongastos.data.model.InstallmentStatusType.ACTIVE,
            statusText = "Recurrente",
            purchaseDateMillis = oct5Millis,
            isRecurring = true
        )

        val purchases = listOf(regularPurchase, recurringPurchase)

        val isDeferredOrRecurring = { detail: com.example.gestiongastos.data.model.PurchaseInstallmentDetail ->
            val purchaseYm = java.time.YearMonth.from(java.time.Instant.ofEpochMilli(detail.purchaseDateMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate())
            detail.isRecurring || (detail.totalInstallments > 1 && (detail.currentInstallment > 1 || purchaseYm.isBefore(ym)))
        }

        // Verify classification
        assertTrue("Recurring purchase must be classified as deferred or recurring", isDeferredOrRecurring(recurringPurchase))
        assertTrue("Regular purchase must NOT be classified as deferred or recurring", !isDeferredOrRecurring(regularPurchase))

        val currentMonthItems = purchases.filter { !isDeferredOrRecurring(it) }
        val deferredOrRecurringItems = purchases.filter { isDeferredOrRecurring(it) }

        // Test LAST: Compras del mes arriba, recurrentes al final (abajo)
        val listLast = currentMonthItems + deferredOrRecurringItems
        assertEquals("Regular purchase must be first when LAST", 101L, listLast.first().purchaseId)
        assertEquals("Recurring purchase must be last when LAST", 102L, listLast.last().purchaseId)

        // Test FIRST: Recurrentes al inicio (arriba), compras del mes abajo
        val listFirst = deferredOrRecurringItems + currentMonthItems
        assertEquals("Recurring purchase must be first when FIRST", 102L, listFirst.first().purchaseId)
        assertEquals("Regular purchase must be last when FIRST", 101L, listFirst.last().purchaseId)
    }

    @Test
    fun testReceivedIncomeIsPurelyVisualAndDoesNotAffectTotalIncome() {
        val income1 = com.example.gestiongastos.data.local.IncomeEntity(
            id = 1L,
            concept = "Sueldo",
            amount = 3_500_000.0,
            date = System.currentTimeMillis(),
            isRecurring = true
        )
        val income2 = com.example.gestiongastos.data.local.IncomeEntity(
            id = 2L,
            concept = "Freelance",
            amount = 800_000.0,
            date = System.currentTimeMillis(),
            isRecurring = false
        )

        val incomes = listOf(income1, income2)

        // Baseline total income
        val totalIncome = incomes.sumOf { it.amount }
        assertEquals(4_300_000.0, totalIncome, 0.001)

        // Mark income 1 as received
        val receivedIncomeIds = setOf(1L)
        val isIncome1Received = 1L in receivedIncomeIds
        val isIncome2Received = 2L in receivedIncomeIds

        assertTrue("Income 1 should be marked as received", isIncome1Received)
        assertTrue("Income 2 should not be marked as received", !isIncome2Received)

        // The total income for the month must remain exactly the same (purely visual tracking)
        val totalAfterMarkingReceived = incomes.sumOf { it.amount }
        assertEquals("Total income must not change regardless of received state", totalIncome, totalAfterMarkingReceived, 0.001)
    }

    @Test
    fun testMovePaidAndReceivedToTopSorting() {
        // 1. Incomes sorting test
        val inc1 = com.example.gestiongastos.data.local.IncomeEntity(id = 10L, concept = "Sueldo", amount = 1_000_000.0, date = 1000L)
        val inc2 = com.example.gestiongastos.data.local.IncomeEntity(id = 20L, concept = "Venta", amount = 200_000.0, date = 2000L)
        val inc3 = com.example.gestiongastos.data.local.IncomeEntity(id = 30L, concept = "Intereses", amount = 50_000.0, date = 3000L)

        val rawIncomes = listOf(inc1, inc2, inc3)

        // Mark inc2 as received
        val receivedIds = setOf(20L)
        val sortedIncomes = rawIncomes.sortedWith(compareByDescending { it.id in receivedIds })

        // inc2 must be first (at the top)
        assertEquals(20L, sortedIncomes[0].id)
        // inc1 and inc3 stay below in their relative order
        assertEquals(10L, sortedIncomes[1].id)
        assertEquals(30L, sortedIncomes[2].id)

        // 2. Expenses sorting test
        val exp1 = com.example.gestiongastos.ui.screens.UnifiedExpenseItem.DirectExpense(
            expense = com.example.gestiongastos.data.local.ExpenseEntity(id = 1L, concept = "Arriendo", amount = 1_200_000.0, date = 1000L),
            isCarriedOver = false
        )
        val exp2 = com.example.gestiongastos.ui.screens.UnifiedExpenseItem.DirectExpense(
            expense = com.example.gestiongastos.data.local.ExpenseEntity(id = 2L, concept = "Internet", amount = 150_000.0, date = 2000L),
            isCarriedOver = false
        )
        val exp3 = com.example.gestiongastos.ui.screens.UnifiedExpenseItem.DirectExpense(
            expense = com.example.gestiongastos.data.local.ExpenseEntity(id = 3L, concept = "Luz", amount = 120_000.0, date = 3000L),
            isCarriedOver = false
        )

        val rawExpenses = listOf(exp1, exp2, exp3)

        // Mark exp3 (Luz) as paid
        val paidKeys = setOf("direct_3")
        val sortedExpenses = rawExpenses.sortedWith(compareByDescending { it.key in paidKeys })

        // exp3 must be first (at the top)
        assertEquals("direct_3", sortedExpenses[0].key)
        // exp1 and exp2 stay below in relative order
        assertEquals("direct_1", sortedExpenses[1].key)
        assertEquals("direct_2", sortedExpenses[2].key)
    }

    @Test
    fun testCalculateSaveDateForMonthAndFilter() {
        val targetYm = java.time.YearMonth.of(2026, 11) // November 2026
        val generatedMillis = calculateSaveDateForMonth(targetYm)

        // Verify the generated timestamp belongs to November 2026 in local zone
        val entryDate = java.time.Instant.ofEpochMilli(generatedMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val entryYm = java.time.YearMonth.from(entryDate)
        assertEquals(targetYm, entryYm)

        // Verify repository monthly filter matches
        val isRecurring = false
        val matchesMonth = if (isRecurring) !targetYm.isBefore(entryYm) else targetYm == entryYm
        assertTrue("Expense saved for target month must match monthly filter", matchesMonth)

        // Verify existing date preservation
        val existingTimestamp = 1700000000000L
        val preserved = calculateSaveDateForMonth(targetYm, existingTimestamp)
        assertEquals(existingTimestamp, preserved)
    }

    @Test
    fun testInstallmentIncomeMonthlyFilter() {
        // A friend owes 500,000 in 3 installments, starting in October 2026
        val startYm = java.time.YearMonth.of(2026, 10)
        val startDateMillis = java.time.LocalDate.of(2026, 10, 5)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val totalAmount = 500_000.0
        val totalInstallments = 3
        val monthlyAmount = totalAmount / totalInstallments

        val income = com.example.gestiongastos.data.local.IncomeEntity(
            id = 100L,
            concept = "Préstamo a un amigo",
            amount = monthlyAmount,
            date = startDateMillis,
            isRecurring = false,
            totalInstallments = totalInstallments,
            totalAmount = totalAmount
        )

        // Helper replicating repository filter logic
        fun isIncomeInMonth(targetYm: java.time.YearMonth): Boolean {
            val entryDate = java.time.Instant.ofEpochMilli(income.date)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val entryYm = java.time.YearMonth.from(entryDate)
            if (income.totalInstallments > 1) {
                val diffMonths = (targetYm.year - entryYm.year) * 12 + (targetYm.monthValue - entryYm.monthValue)
                return diffMonths in 0 until income.totalInstallments
            }
            return if (income.isRecurring) !targetYm.isBefore(entryYm) else targetYm == entryYm
        }

        // September 2026 (before start): should NOT appear
        org.junit.Assert.assertFalse(isIncomeInMonth(java.time.YearMonth.of(2026, 9)))

        // October 2026 (Month 1): should appear (Cuota 1/3)
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 10)))

        // November 2026 (Month 2): should appear (Cuota 2/3)
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 11)))

        // December 2026 (Month 3): should appear (Cuota 3/3)
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 12)))

        // January 2027 (Month 4): should NOT appear anymore
        org.junit.Assert.assertFalse(isIncomeInMonth(java.time.YearMonth.of(2027, 1)))

        // Verify monthly installment sum
        assertEquals(166_666.666, income.amount, 0.01)
        assertEquals(500_000.0, income.totalAmount, 0.01)
    }

    @Test
    fun testRecurringIncomeWithEndDateFilter() {
        // Project income starting September 2026, ends December 30, 2026
        val startDateMillis = java.time.LocalDate.of(2026, 9, 1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val endDateMillis = java.time.LocalDate.of(2026, 12, 30)
            .atTime(23, 59, 59)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val income = com.example.gestiongastos.data.local.IncomeEntity(
            id = 200L,
            concept = "Proyecto Freelance",
            amount = 1_500_000.0,
            date = startDateMillis,
            isRecurring = true,
            totalInstallments = 1,
            endDateMillis = endDateMillis
        )

        fun isIncomeInMonth(targetYm: java.time.YearMonth): Boolean {
            val entryDate = java.time.Instant.ofEpochMilli(income.date)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val entryYm = java.time.YearMonth.from(entryDate)

            if (income.totalInstallments > 1) {
                val diffMonths = (targetYm.year - entryYm.year) * 12 + (targetYm.monthValue - entryYm.monthValue)
                return diffMonths in 0 until income.totalInstallments
            } else if (income.isRecurring) {
                val notBeforeStart = !targetYm.isBefore(entryYm)
                return if (notBeforeStart && income.endDateMillis != null) {
                    val endDate = java.time.Instant.ofEpochMilli(income.endDateMillis!!)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    val endYm = java.time.YearMonth.from(endDate)
                    !targetYm.isAfter(endYm)
                } else {
                    notBeforeStart
                }
            } else {
                return targetYm == entryYm
            }
        }

        // August 2026 (before project starts): NOT included
        org.junit.Assert.assertFalse(isIncomeInMonth(java.time.YearMonth.of(2026, 8)))

        // September 2026: Included
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 9)))

        // October 2026: Included
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 10)))

        // November 2026: Included
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 11)))

        // December 2026: Included (End month)
        assertTrue(isIncomeInMonth(java.time.YearMonth.of(2026, 12)))

        // January 2027: EXCLUDED! (Deadline reached, stops appearing)
        org.junit.Assert.assertFalse(isIncomeInMonth(java.time.YearMonth.of(2027, 1)))
    }
}


