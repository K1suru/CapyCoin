package com.example.gestiongastos

import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.model.arrangeByRegistration
import com.example.gestiongastos.data.model.computeMonthTotals
import com.example.gestiongastos.data.model.frenchPayment
import com.example.gestiongastos.data.model.incomeAppliesInMonth
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.data.model.isDeferredOrRecurring
import com.example.gestiongastos.data.model.markedFirst
import com.example.gestiongastos.data.repository.isInMonth
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculationsTest {

    @Test
    fun testFrenchAmortizationFormula() {
        // 1.000.000 a 12 cuotas al 2 % mensual: cuota fija de 94.559,60 (sistema francés)
        val monthlyPayment = frenchPayment(1_000_000.0, 0.02, 12)
        assertEquals(94_559.60, monthlyPayment, 0.01)

        // El plan real de la app da esa misma cuota todos los meses y termina con saldo 0
        val schedule = installmentSchedule(1_000_000.0, 12, 2.0, isInterestFree = false, isFirstInstallmentInterestFree = false)
        assertEquals(12, schedule.size)
        schedule.forEach { assertEquals(monthlyPayment, it, 0.01) }
        assertEquals(134_715.16, schedule.sum() - 1_000_000.0, 0.01) // intereses totales

        var balance = 1_000_000.0
        schedule.forEach { balance = balance * 1.02 - it }
        assertEquals(0.0, balance, 0.01)
    }

    @Test
    fun testZeroInterestPurchase() {
        // Sin interés (o con tasa 0) el capital se reparte en partes iguales
        assertEquals(100_000.0, frenchPayment(600_000.0, 0.0, 6), 0.001)
        val interestFree = installmentSchedule(600_000.0, 6, 2.5, isInterestFree = true, isFirstInstallmentInterestFree = false)
        assertEquals(List(6) { 100_000.0 }, interestFree)
        assertEquals(600_000.0, interestFree.sum(), 0.001)

        // Casos límite que no deben dar NaN ni dividir por cero
        assertEquals(0.0, frenchPayment(0.0, 0.02, 12), 0.0)
        assertEquals(510.0, frenchPayment(500.0, 0.02, 0), 0.01) // 0 cuotas cuenta como 1
    }

    @Test
    fun testFirstInstallmentInterestFreeBehavior() {
        // A una cuota nunca hay interés
        assertEquals(listOf(250_000.0), installmentSchedule(250_000.0, 1, 2.0, isInterestFree = false, isFirstInstallmentInterestFree = true))

        // A 3 cuotas con la 1ª sin interés: la primera es capital/3 y las otras dos llevan el 2 % sobre el saldo
        val schedule = installmentSchedule(300_000.0, 3, 2.0, isInterestFree = false, isFirstInstallmentInterestFree = true)
        assertEquals(100_000.0, schedule[0], 0.01)
        assertEquals(frenchPayment(200_000.0, 0.02, 2), schedule[1], 0.01)
        assertEquals(schedule[1], schedule[2], 0.01)
        assertTrue(schedule[1] > 100_000.0)

        // Paga menos intereses que la misma compra sin ese beneficio
        val regular = installmentSchedule(300_000.0, 3, 2.0, isInterestFree = false, isFirstInstallmentInterestFree = false)
        assertTrue(schedule.sum() < regular.sum())

        // Y la compra real usa ese mismo plan
        val purchaseDate = java.time.LocalDate.of(2026, 1, 5).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        (1..3).forEach { month ->
            val detail = com.example.gestiongastos.data.model.calculatePurchaseInstallmentDetail(
                purchaseId = 1L, cardId = 1L, concept = "Compra", totalAmount = 300_000.0, totalInstallments = 3,
                purchaseDateMillis = purchaseDate, targetYearMonth = java.time.YearMonth.of(2026, month),
                interestRate = 2.0, isFirstInstallmentInterestFree = true
            )
            assertEquals(schedule[month - 1], detail.monthlyPayment, 0.01)
        }
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
        // Pesos: punto de miles y sin decimales (se redondea)
        assertEquals("$ 50.000", formatCurrency(50000.0))
        assertEquals("$ 1.234.568", formatCurrency(1_234_567.6))
        assertEquals("USD $ 1.500", formatCurrency(1500.0, "USD $"))
        assertEquals("$ 0", formatCurrency(0.0))

        // Dólares: coma de miles y siempre dos decimales
        assertEquals("USD $25.00", formatUsd(25.0))
        assertEquals("USD $1,234.57", formatUsd(1234.567))
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

        // Clasificación real que usan las pantallas de Gastos y Tarjetas
        val isDeferredOrRecurring = { detail: com.example.gestiongastos.data.model.PurchaseInstallmentDetail ->
            detail.isDeferredOrRecurring(ym)
        }

        // Una compra a cuotas hecha este mes y en su 1ª cuota todavía es "del mes"; desde la 2ª ya viene arrastrada
        val installmentsFirst = regularPurchase.copy(purchaseId = 103L, totalInstallments = 6, currentInstallment = 1)
        assertTrue(!isDeferredOrRecurring(installmentsFirst))
        assertTrue(isDeferredOrRecurring(installmentsFirst.copy(currentInstallment = 2)))
        // ...y también si se compró en un mes anterior al que se está viendo
        assertTrue(installmentsFirst.isDeferredOrRecurring(ym.plusMonths(1)))
        // A una sola cuota nunca es diferida, aunque sea de un mes anterior
        assertTrue(!regularPurchase.isDeferredOrRecurring(ym.plusMonths(1)))

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
        val incomes = listOf(
            com.example.gestiongastos.data.local.IncomeEntity(id = 1L, concept = "Sueldo", amountCents = toCents(3_500_000.0), date = 1000L, isRecurring = true),
            com.example.gestiongastos.data.local.IncomeEntity(id = 2L, concept = "Freelance", amountCents = toCents(800_000.0), date = 2000L)
        )
        val expenses = listOf(
            com.example.gestiongastos.data.local.ExpenseEntity(id = 1L, concept = "Arriendo", amountCents = toCents(1_200_000.0), date = 1000L),
            com.example.gestiongastos.data.local.ExpenseEntity(id = 2L, concept = "Hosting", amountCents = toCents(10.0), date = 1000L, currency = "USD"),
            com.example.gestiongastos.data.local.ExpenseEntity(id = 3L, concept = "Ignorado", amountCents = toCents(999_999.0), date = 1000L)
        )
        val savings = listOf(
            com.example.gestiongastos.data.local.SavingsEntity(id = 1L, concept = "Viaje", amountCents = toCents(300_000.0), date = 1000L),
            com.example.gestiongastos.data.local.SavingsEntity(id = 2L, concept = "Ignorado", amountCents = toCents(50_000.0), date = 1000L)
        )

        // La cuenta real del Resumen y del CSV: no recibe los ingresos marcados como recibidos,
        // así que marcarlos no puede cambiar ningún total
        val totals = computeMonthTotals(incomes, expenses, savings, emptyList(), usdRate = 4000.0, ignoredKeys = setOf("direct_3", "saving_2"))

        assertEquals(4_300_000.0, totals.totalIncome, 0.001)
        assertEquals(1_240_000.0, totals.directExpenses, 0.001) // 1.200.000 + 10 USD a 4.000; el ignorado no suma
        assertEquals(300_000.0, totals.totalSavings, 0.001)
        assertEquals(1_540_000.0, totals.neededAmount, 0.001)
        assertEquals(2_760_000.0, totals.freeAmount, 0.001)
        assertEquals(1_540_000f / 4_300_000f, totals.spentRatio, 1e-6f)

        // Sin ingresos pero con gastos la barra va llena; sin nada, vacía; y nunca pasa de 2
        assertEquals(1f, computeMonthTotals(emptyList(), expenses, savings, emptyList(), 4000.0).spentRatio, 0f)
        assertEquals(0f, computeMonthTotals(emptyList(), emptyList(), emptyList(), emptyList(), 4000.0).spentRatio, 0f)
        assertEquals(2f, computeMonthTotals(incomes.take(0) + incomes[1].copy(amountCents = 100), expenses, savings, emptyList(), 4000.0).spentRatio, 0f)
    }

    @Test
    fun monthTotalsIncludeCardInstallmentsAndFeesUnlessIgnored() {
        val purchase = com.example.gestiongastos.data.model.PurchaseInstallmentDetail(
            purchaseId = 7L, cardId = 1L, concept = "TV", totalAmount = 600_000.0, totalInstallments = 6,
            currentInstallment = 2, monthlyPayment = 100_000.0,
            statusType = com.example.gestiongastos.data.model.InstallmentStatusType.ACTIVE,
            statusText = "", purchaseDateMillis = 1000L
        )
        val card = com.example.gestiongastos.data.model.CardMonthSummary(
            cardId = 1L, cardName = "Visa", cutoffDay = 15, interestRate = 0.0, maintenanceFee = 20_000.0, currency = "COP",
            purchases = listOf(purchase, purchase.copy(purchaseId = 8L, monthlyPayment = 5.0, currency = "USD")),
            totalActiveInstallmentsPayment = 0.0, totalMonthlyCardExpense = 0.0
        )
        fun cardTotal(ignored: Set<String>) =
            computeMonthTotals(emptyList(), emptyList(), emptyList(), listOf(card), 4000.0, ignored).creditCardExpenses

        assertEquals(140_000.0, cardTotal(emptySet()), 0.001) // 100.000 + 5 USD a 4.000 + cuota de manejo
        assertEquals(40_000.0, cardTotal(setOf("card_inst_7")), 0.001)
        assertEquals(120_000.0, cardTotal(setOf("card_fee_1")), 0.001)
    }

    @Test
    fun testMovePaidAndReceivedToTopSorting() {
        val ids = listOf(10L, 20L, 30L, 40L)

        // Lo marcado sube y el resto conserva su orden (orden estable)
        assertEquals(listOf(20L, 40L, 10L, 30L), markedFirst(ids, enabled = true) { it in setOf(20L, 40L) })
        // Con la preferencia apagada la lista no se toca
        assertEquals(ids, markedFirst(ids, enabled = false) { it in setOf(20L, 40L) })
        // Sin nada marcado, o con todo marcado, tampoco cambia
        assertEquals(ids, markedFirst(ids, enabled = true) { false })
        assertEquals(ids, markedFirst(ids, enabled = true) { true })
    }

    @Test
    fun listIsArrangedBySortOrderAndInstallmentPosition() {
        // (día, id, esArrastrado): 1 y 2 son del mes; 3 y 4 son cuotas que vienen de antes
        data class Row(val day: Int, val id: Long, val carried: Boolean)
        val rows = listOf(Row(5, 2, false), Row(5, 1, false), Row(3, 4, true), Row(9, 3, true))
        val oldestFirst = compareBy<Row>({ it.day }, { it.id })
        fun arrange(order: com.example.gestiongastos.data.model.ExpenseSortOrder, position: com.example.gestiongastos.data.model.InstallmentPosition) =
            arrangeByRegistration(rows, oldestFirst, order, position) { it.carried }.map { it.id }

        val newest = com.example.gestiongastos.data.model.ExpenseSortOrder.NEWEST_FIRST
        val oldest = com.example.gestiongastos.data.model.ExpenseSortOrder.OLDEST_FIRST
        val last = com.example.gestiongastos.data.model.InstallmentPosition.LAST
        val first = com.example.gestiongastos.data.model.InstallmentPosition.FIRST

        // Mismo día: lo registrado después (id mayor) queda arriba con "más recientes primero"
        assertEquals(listOf(2L, 1L, 3L, 4L), arrange(newest, last))
        assertEquals(listOf(1L, 2L, 4L, 3L), arrange(oldest, last))
        assertEquals(listOf(3L, 4L, 2L, 1L), arrange(newest, first))
        assertEquals(listOf(4L, 3L, 1L, 2L), arrange(oldest, first))
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

        // El filtro mensual real del repositorio lo ubica en ese mes y solo en ese mes
        val savedYm = com.example.gestiongastos.data.repository.yearMonthOf(generatedMillis)
        assertTrue(isInMonth(targetYm, savedYm, isRecurring = false, endDateMillis = null))
        org.junit.Assert.assertFalse(isInMonth(targetYm.plusMonths(1), savedYm, isRecurring = false, endDateMillis = null))
        assertTrue(isInMonth(targetYm.plusMonths(1), savedYm, isRecurring = true, endDateMillis = null))

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
            amountCents = toCents(monthlyAmount),
            date = startDateMillis,
            isRecurring = false,
            totalInstallments = totalInstallments,
            totalAmountCents = toCents(totalAmount)
        )

        // Mismo filtro que usa el repositorio para decidir qué ingresos salen en cada mes
        fun isIncomeInMonth(targetYm: java.time.YearMonth): Boolean = incomeAppliesInMonth(income, targetYm)

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
            amountCents = toCents(1_500_000.0),
            date = startDateMillis,
            isRecurring = true,
            totalInstallments = 1,
            endDateMillis = endDateMillis
        )

        fun isIncomeInMonth(targetYm: java.time.YearMonth): Boolean = incomeAppliesInMonth(income, targetYm)

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

        // Sin fecha límite sigue apareciendo; un ingreso único solo sale en su mes
        assertTrue(incomeAppliesInMonth(income.copy(endDateMillis = null), java.time.YearMonth.of(2030, 1)))
        val unique = income.copy(isRecurring = false, endDateMillis = null)
        assertTrue(incomeAppliesInMonth(unique, java.time.YearMonth.of(2026, 9)))
        org.junit.Assert.assertFalse(incomeAppliesInMonth(unique, java.time.YearMonth.of(2026, 10)))
    }
}


