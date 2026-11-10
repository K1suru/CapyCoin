package com.example.gestiongastos

import com.example.gestiongastos.data.remote.parseTrmValue
import com.example.gestiongastos.ui.components.MAX_AMOUNT
import com.example.gestiongastos.ui.components.addToAmountInput
import com.example.gestiongastos.ui.components.amountToInput
import com.example.gestiongastos.ui.components.formatMoney
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.parsePercentRate
import com.example.gestiongastos.ui.components.sanitizeAmountInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Lectura de montos escritos por el usuario: nunca debe cambiar un monto en silencio. */
class AmountParsingTest {

    private fun assertAmount(expected: Double, text: String) {
        val parsed = parseAmount(text)
        assertEquals("parseAmount(\"$text\")", expected, parsed ?: Double.NaN, 1e-9)
    }

    @Test
    fun rejectsNonFiniteAndScientificNotation() {
        // toDoubleOrNull acepta todo esto; un monto no
        listOf("Infinity", "-Infinity", "NaN", "1e308", "1e5", "1E5", "0x10", "15f", "15d", "1_000").forEach {
            assertNull("parseAmount(\"$it\") debe rechazarse", parseAmount(it))
        }
    }

    @Test
    fun rejectsNegativesAndSigns() {
        assertNull(parseAmount("-5"))
        assertNull(parseAmount("-150.000"))
        assertNull(parseAmount("+5"))
        assertNull(parseAmount("5-"))
    }

    @Test
    fun rejectsAmountsAboveTheLimit() {
        assertAmount(MAX_AMOUNT, "9999999999999")
        assertAmount(MAX_AMOUNT, sanitizeAmountInput("99999999999999999", allowDecimals = false)) // tope del campo
        assertNull(parseAmount("10000000000000"))
        assertNull(parseAmount("9999999999999999999999"))
    }

    @Test
    fun rejectsMisplacedSeparatorsInsteadOfGuessing() {
        // Antes se quitaban los separadores sin mirar: "1.5.0" se guardaba como 150
        listOf("1.5.0", "1.50.000", "1,5,0", "1.5,5", "1.500.00", "1..5", "1.,5", ".", ",", "1234.567", "1.500,5,0", "12.3456.789").forEach {
            assertNull("parseAmount(\"$it\") debe rechazarse", parseAmount(it))
        }
    }

    @Test
    fun readsThousandsAndDecimalsInBothStyles() {
        assertAmount(1500.0, "1.500")
        assertAmount(25500.0, "25,500")
        assertAmount(1_500_000.0, "1.500.000")
        assertAmount(1_500_000.0, "1,500,000")
        assertAmount(1500.5, "1.500,50")
        assertAmount(1500.5, "1,500.50")
        assertAmount(1_234_567.89, "1.234.567,89")
        assertAmount(1500.5, "1500,5")
        assertAmount(1.5, "1,5")
        assertAmount(12.0, "12,")
        assertAmount(150000.0, " $ 150 000 ")
        assertAmount(0.0, "0")
    }

    @Test
    fun zeroBeforeTheSeparatorIsAlwaysADecimal() {
        // Antes "0.500" y ".500" se leían como 500
        assertAmount(0.5, "0.500")
        assertAmount(0.5, "0,500")
        assertAmount(0.5, ".500")
        assertAmount(0.5, ",5")
    }

    @Test
    fun amountFieldRoundTripKeepsTheValue() {
        // Lo que produce el campo de monto se lee igual al volver
        assertAmount(1_500_000.0, sanitizeAmountInput("1.500.000", allowDecimals = false))
        assertAmount(12.5, sanitizeAmountInput("12.5", allowDecimals = true))
        assertAmount(1234.56, amountToInput(1234.56, allowDecimals = true))
        assertAmount(385_000.0, amountToInput(385_000.0))
    }

    @Test
    fun pastedAmountFollowsTheSameRulesAsParseAmount() {
        // Antes pegar "1.50" en un campo en pesos lo dejaba en 150
        assertEquals("2", sanitizeAmountInput("1.50", allowDecimals = false))
        assertEquals("1,5", sanitizeAmountInput("1.50", allowDecimals = true))
        assertEquals("15", sanitizeAmountInput("14,6", allowDecimals = false))
        // Miles bien formados siguen siendo miles en las dos monedas
        assertEquals("1500", sanitizeAmountInput("1.500", allowDecimals = false))
        assertEquals("25500", sanitizeAmountInput("25,500", allowDecimals = false))
        assertEquals("25500", sanitizeAmountInput("25,500", allowDecimals = true))
        assertEquals("1500000", sanitizeAmountInput("$ 1.500.000", allowDecimals = false))
        // Miles y decimales juntos
        assertEquals("1500,5", sanitizeAmountInput("1.500,50", allowDecimals = true))
        assertEquals("1500,5", sanitizeAmountInput("USD 1,500.50", allowDecimals = true))
        assertEquals("1501", sanitizeAmountInput("1.500,50", allowDecimals = false))
        assertEquals("0,5", sanitizeAmountInput("0.500", allowDecimals = true))
        assertEquals("1234567", sanitizeAmountInput("abc1234567", allowDecimals = false))
    }

    @Test
    fun ambiguousPasteLeavesTheFieldAsItWas() {
        // parseAmount rechaza estos textos; el campo no debe inventar un monto (antes "1234.567" quedaba en 1234567)
        listOf("1234.567", "1.5.0", "1.50.000", "1.,5").forEach {
            assertEquals("pegar \"$it\"", "", sanitizeAmountInput(it, allowDecimals = false))
            assertEquals("pegar \"$it\"", "800", sanitizeAmountInput(it, allowDecimals = true, previous = "800"))
        }
    }

    @Test
    fun typingKeepsTheSeparatorAsDecimal() {
        // En dólares, un tercer decimal se ignora. Antes "25,50" + "0" quedaba en 25500
        assertEquals("25,50", sanitizeAmountInput("25,500", allowDecimals = true, previous = "25,50"))
        assertEquals("1,50", sanitizeAmountInput("1,505", allowDecimals = true, previous = "1,50"))
        // Escribir el separador, con punto o con coma
        assertEquals("12,", sanitizeAmountInput("12.", allowDecimals = true, previous = "12"))
        assertEquals("12,", sanitizeAmountInput("12,", allowDecimals = true, previous = "12"))
        assertEquals("0,", sanitizeAmountInput(",", allowDecimals = true, previous = ""))
        assertEquals("12,5", sanitizeAmountInput("12,5", allowDecimals = true, previous = "12,"))
        assertEquals("0,05", sanitizeAmountInput("0,05", allowDecimals = true, previous = "0,0"))
        // Un segundo separador se ignora
        assertEquals("1,5", sanitizeAmountInput("1,5,", allowDecimals = true, previous = "1,5"))
        // Borrar
        assertEquals("12,", sanitizeAmountInput("12,", allowDecimals = true, previous = "12,5"))
        assertEquals("125", sanitizeAmountInput("125", allowDecimals = true, previous = "12,5"))
        // En pesos el separador que se escribe se ignora y el monto no cambia
        assertEquals("1500", sanitizeAmountInput("1500.", allowDecimals = false, previous = "1500"))
        assertEquals("1500", sanitizeAmountInput("1.500", allowDecimals = false, previous = "1500"))
        assertEquals("15005", sanitizeAmountInput("15005", allowDecimals = false, previous = "1500"))
        assertEquals("5", sanitizeAmountInput("05", allowDecimals = false, previous = "0"))
    }

    @Test
    fun sameTextAgainIsLeftUntouched() {
        // Visto en el teléfono: al ignorar una tecla, Compose vuelve a entregar el texto que ya había.
        // Se leía como un pegado: "25,50" quedaba en "25,5" y "0," se borraba.
        listOf("25,50", "0,", "0,0", "12,", "1500").forEach {
            assertEquals(it, sanitizeAmountInput(it, allowDecimals = true, previous = it))
        }
        assertEquals("1500", sanitizeAmountInput("1500", allowDecimals = false, previous = "1500"))
        // La secuencia completa de escribir 0,05 y 25,50 tecla por tecla
        fun type(keys: String) = keys.fold("") { field, key -> sanitizeAmountInput(field + key, allowDecimals = true, previous = field) }
        assertEquals("0,05", type(",05"))
        assertEquals("25,50", type("25,500"))
        assertEquals("1,57", type("1,5,7"))
    }

    @Test
    fun moneyIsFormattedInItsOwnCurrency() {
        // El diálogo de abono de tarjetas mostraba el saldo en dólares sin centavos ("USD $ 33")
        assertEquals("USD $33.34", formatMoney(33.336, "USD"))
        assertEquals("USD $1,250.50", formatMoney(1250.5, "USD"))
        assertEquals("USD $0.40", formatMoney(0.4, "USD"))
        assertEquals("$ 1.500", formatMoney(1500.4, "COP"))
    }

    @Test
    fun percentRateTreatsSeparatorAsDecimal() {
        // Con parseAmount una tasa de 1.875 % mensual se guardaba como 1875 %
        assertEquals(1.875, parsePercentRate("1.875")!!, 1e-9)
        assertEquals(1.875, parsePercentRate("1,875")!!, 1e-9)
        assertEquals(2.2, parsePercentRate("2.2")!!, 1e-9)
        assertEquals(2.0, parsePercentRate(" 2 % ")!!, 1e-9)
        assertEquals(0.0, parsePercentRate("")!!, 1e-9)
        assertEquals(100.0, parsePercentRate("100")!!, 1e-9)
    }

    @Test
    fun percentRateRejectsInvalidValues() {
        listOf("abc", "-1", "100.01", "1875", "1.2.3", "NaN", "Infinity", "1e2", ".").forEach {
            assertNull("parsePercentRate(\"$it\") debe rechazarse", parsePercentRate(it))
        }
    }

    @Test
    fun quickIncrementKeepsDecimalsAndStaysInRange() {
        // Antes (cur + inc).toLong() botaba los centavos: "12,5" + 5 quedaba en "17"
        assertEquals("17,5", addToAmountInput("12,5", 5, allowDecimals = true))
        assertEquals("15000", addToAmountInput("10000", 5000, allowDecimals = false))
        assertEquals("5000", addToAmountInput("", 5000, allowDecimals = false))
        assertEquals("9999999999999", addToAmountInput("9999999999999", 50000, allowDecimals = false))
    }

    @Test
    fun exchangeRateMustBeFiniteAndPlausible() {
        assertEquals(4150.25, parseTrmValue("4150.25")!!, 1e-9)
        assertEquals(4150.25, parseTrmValue(" 4150,25 ")!!, 1e-9)
        // Antes "NaN" pasaba el filtro (NaN <= 0 es falso) y todos los montos en USD quedaban en NaN
        listOf("NaN", "Infinity", "0", "-4150", "4.15", "1e9", "", "abc").forEach {
            assertNull("parseTrmValue(\"$it\") debe rechazarse", parseTrmValue(it))
        }
    }
}
