package com.example.gestiongastos

import com.example.gestiongastos.data.backup.backupLong
import com.example.gestiongastos.data.backup.backupMoney
import com.example.gestiongastos.data.backup.backupYearMonth
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.model.installmentInCents
import com.example.gestiongastos.ui.components.CapyMood
import com.example.gestiongastos.ui.components.capyMoodFor
import com.example.gestiongastos.ui.components.longDateFormat
import com.example.gestiongastos.ui.components.shortDateFormat
import com.example.gestiongastos.ui.screens.UnifiedExpenseItem
import com.example.gestiongastos.ui.screens.amountInCop
import com.example.gestiongastos.ui.screens.expenseAmountInCop
import com.example.gestiongastos.ui.screens.formatIdsSet
import com.example.gestiongastos.ui.screens.parseIdsSet
import com.example.gestiongastos.ui.screens.groupExpenseItems
import com.example.gestiongastos.ui.screens.parseExpenseInstallments
import com.example.gestiongastos.ui.theme.CardPalettes
import com.example.gestiongastos.ui.theme.autoCardColorIndex
import com.example.gestiongastos.ui.theme.cardPaletteFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/** Lógica pequeña que usan las pantallas y que no tenía ninguna prueba. */
class PureLogicTest {

    private fun millis(date: LocalDate) = date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun direct(id: Long, date: LocalDate, carried: Boolean = false, amountCents: Long = 100_00, currency: String = "COP") =
        UnifiedExpenseItem.DirectExpense(ExpenseEntity(id = id, concept = "Gasto $id", amountCents = amountCents, date = millis(date), currency = currency), carried)

    @Test
    fun capybaraMoodFollowsHowMuchOfTheIncomeIsUsed() {
        assertEquals(CapyMood.RELAXED, capyMoodFor(0f))
        assertEquals(CapyMood.RELAXED, capyMoodFor(0.69f))
        assertEquals(CapyMood.CAUTIOUS, capyMoodFor(0.7f))
        assertEquals(CapyMood.WORRIED, capyMoodFor(0.9f))
        assertEquals(CapyMood.WORRIED, capyMoodFor(1.0f)) // justo el 100 % todavía no es pánico
        assertEquals(CapyMood.PANIC, capyMoodFor(1.01f))
    }

    @Test
    fun cardColorIsTheChosenOneOrAStableAutomaticOne() {
        listOf(0L, 1L, 6L, 7L, 8L, 9_999L, -3L).forEach { id ->
            assertTrue("índice válido para $id", autoCardColorIndex(id) in CardPalettes.indices)
            assertEquals(autoCardColorIndex(id), autoCardColorIndex(id))
        }
        assertEquals(CardPalettes[2], cardPaletteFor(cardId = 8, colorIndex = 2))
        // -1 (sin elegir) o un índice que ya no existe: color automático, nunca un fallo
        assertEquals(CardPalettes[autoCardColorIndex(8)], cardPaletteFor(8, -1))
        assertEquals(CardPalettes[autoCardColorIndex(8)], cardPaletteFor(8, 999))
    }

    @Test
    fun expenseInstallmentCountMustBeBetweenOneAndThreeHundredSixty() {
        assertEquals(1, parseExpenseInstallments("1"))
        assertEquals(360, parseExpenseInstallments("360"))
        listOf("0", "361", "-2", "", "abc", "1.5", " 12").forEach {
            assertNull("\"$it\" no es un número de cuotas válido", parseExpenseInstallments(it))
        }
    }

    @Test
    fun backupValuesAreReadStrictly() {
        assertEquals(5L, backupLong(5))
        assertEquals(5L, backupLong(5.0))
        assertEquals(7L, backupLong(" 7 "))
        listOf<Any?>(5.5, "x", null, 1e19, Double.NaN, listOf(1)).forEach { assertNull("backupLong($it)", backupLong(it)) }

        assertEquals(3.5, backupMoney("3.5")!!, 0.0)
        assertEquals(0.0, backupMoney(0)!!, 0.0)
        listOf<Any?>(-1, Double.NaN, Double.POSITIVE_INFINITY, "abc", null, true).forEach { assertNull("backupMoney($it)", backupMoney(it)) }

        assertEquals("2026-08", backupYearMonth("2026-08"))
        assertEquals("2026-08", backupYearMonth(" 2026-08 "))
        listOf<Any?>("2026-13", "agosto", "", null, 202608).forEach { assertNull("backupYearMonth($it)", backupYearMonth(it)) }
    }

    @Test
    fun itemAmountIsConvertedToPesosOnlyWhenItIsInDollars() {
        val today = LocalDate.of(2026, 11, 9)
        assertEquals(100.0, direct(1, today).amountInCop(4000.0), 0.0)
        assertEquals(40_000.0, direct(2, today, amountCents = 10_00, currency = "USD").amountInCop(4000.0), 0.0)
        assertEquals(15_900.0, UnifiedExpenseItem.CardFeeExpense(1, "Visa", 15_900.0, "$").amountInCop(4000.0), 0.0)
    }

    @Test
    fun expensesAreGroupedByDayKeepingTheirOrder() {
        val today = LocalDate.now()
        val old = today.minusDays(10)
        val groups = groupExpenseItems(listOf(
            direct(1, today), direct(2, today), direct(3, today.minusDays(1)), direct(4, old),
            direct(5, old.minusMonths(2), carried = true), direct(6, today, carried = true)
        ))
        assertEquals(listOf(listOf(1L, 2L), listOf(3L), listOf(4L), listOf(5L, 6L)), groups.map { g -> g.second.map { (it as UnifiedExpenseItem.DirectExpense).expense.id } })
        assertEquals("Hoy", groups[0].first)
        assertEquals("Ayer", groups[1].first)
        // El día se escribe con mayúscula inicial y lo que se repite va en su propio bloque, sin importar la fecha
        assertTrue(groups[2].first.first().isUpperCase() && groups[2].first.contains(old.dayOfMonth.toString()))
        assertEquals("Cuotas, fijos y recurrentes", groups[3].first)
        assertTrue(groupExpenseItems(emptyList()).isEmpty())
    }

    @Test
    fun onlyTheLastInstallmentIsRoundedUpToCloseTheBalance() {
        assertEquals(33.33, installmentInCents(100.0, 0.0, 3, isLast = false), 0.0)
        assertEquals(33.34, installmentInCents(33.34, 0.0, 1, isLast = true), 0.0)
        // La última paga el saldo más su interés del mes
        assertEquals(102.0, installmentInCents(100.0, 0.02, 1, isLast = true), 0.0)
        assertEquals(0.0, installmentInCents(0.0, 0.02, 5, isLast = false), 0.0)
    }

    @Test
    fun datesAreWrittenInSpanishByDefault() {
        val date = java.util.Date(millis(LocalDate.of(2026, 11, 9)))
        assertEquals("09 de noviembre de 2026", longDateFormat().format(date))
        assertTrue(shortDateFormat().format(date).startsWith("09 nov"))
    }

    @Test
    fun linkedIdsOfAReimbursementSurviveTheRoundTrip() {
        assertEquals(setOf(1L, 2L, 54L), parseIdsSet("1,2,54"))
        assertEquals(setOf(1L, 2L), parseIdsSet(" 1 , 2 ,,x, 2"))   // espacios, vacíos, basura y repetidos
        assertEquals(emptySet<Long>(), parseIdsSet(null))
        assertEquals(emptySet<Long>(), parseIdsSet(""))
        assertNull(formatIdsSet(emptySet()))                        // sin vínculos no se guarda texto
        assertEquals("7,3", formatIdsSet(linkedSetOf(7L, 3L)))
        assertEquals(setOf(7L, 3L), parseIdsSet(formatIdsSet(linkedSetOf(7L, 3L))))
    }

    @Test
    fun reimbursedExpenseIsValuedInPesos() {
        val cop = ExpenseEntity(concept = "Arriendo", amountCents = 1_200_000_00, date = 0)
        val usd = ExpenseEntity(concept = "Hosting", amountCents = 10_50, date = 0, currency = "USD")
        assertEquals(1_200_000.0, expenseAmountInCop(cop, 4000.0), 0.0)
        assertEquals(42_000.0, expenseAmountInCop(usd, 4000.0), 0.0)
    }
}
