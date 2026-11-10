package com.example.gestiongastos.ui.components

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, currencySymbol: String = "$"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "$currencySymbol ${formatter.format(amount)}"
}

fun formatUsd(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2
    return "USD $${formatter.format(amount)}"
}

/** Dólares con su equivalente aproximado en pesos: "USD $33.34 (~$ 109.018)". */
fun formatUsdWithCop(amount: Double, usdRate: Double): String = "${formatUsd(amount)} (~${formatCurrency(amount * usdRate)})"

/** Símbolo con el que se muestra cada moneda. */
fun currencySymbolOf(currency: String): String = if (currency == "USD") "USD $" else "$"

/** Monto en su propia moneda: dólares con centavos ("USD $33.34"), pesos sin decimales ("$ 1.500"). */
fun formatMoney(amount: Double, currency: String): String =
    if (currency == "USD") formatUsd(amount) else formatCurrency(amount)

/**
 * Monto máximo que acepta la app: 13 dígitos, lo mismo que deja escribir el campo de monto.
 * Hasta ahí un Double guarda los centavos sin error; mucho más arriba ya no.
 */
const val MAX_AMOUNT = 9_999_999_999_999.0

private val DIGITS = Regex("\\d+")
private val GROUPED_INTEGER = Regex("\\d{1,3}(?:[.,]\\d{3})+")

/** Parte entera con o sin separadores de miles bien formados ("1500", "1.500", "1.500.000"). */
private fun parseIntegerPart(text: String, groupSeparator: Char): String? = when {
    DIGITS.matches(text) -> text
    GROUPED_INTEGER.matches(text) && text.none { (it == '.' || it == ',') && it != groupSeparator } ->
        text.replace(groupSeparator.toString(), "")
    else -> null
}

/**
 * Convierte lo escrito por el usuario en un monto. Devuelve null si no es un monto válido:
 * vacío, con letras o signos ("1e5", "Infinity", "-5"), con separadores mal puestos ("1.50.000")
 * o mayor que [MAX_AMOUNT]. Nunca devuelve NaN, infinito ni negativos.
 *
 * Acepta punto o coma como separador de miles o de decimales:
 * - Con los dos, el último es el decimal: "1.500,50" y "1,500.50" son 1500.5.
 * - Con uno solo repetido son miles: "1.500.000".
 * - Con uno solo y exactamente 3 dígitos detrás son miles ("1.500" = 1500, "25,500" = 25500),
 *   salvo que la parte entera sea 0 o vacía ("0,500" = 0.5). En cualquier otro caso es decimal.
 *   Para porcentajes, donde "1.875" es un decimal, usa [parsePercentRate].
 */
fun parseAmount(input: String): Double? {
    val text = input.filterNot { it.isWhitespace() || it == '\u00A0' || it == '$' }
    if (text.isEmpty() || text.any { !(it.isDigit() || it == '.' || it == ',') }) return null
    if (text.none { it.isDigit() }) return null

    val lastDot = text.lastIndexOf('.')
    val lastComma = text.lastIndexOf(',')
    val normalized: String? = when {
        lastDot < 0 && lastComma < 0 -> text
        lastDot >= 0 && lastComma >= 0 -> {
            // Los dos separadores: el último es el decimal y el otro agrupa miles
            val decimalIndex = maxOf(lastDot, lastComma)
            val groupSeparator = if (lastDot > lastComma) ',' else '.'
            val decimals = text.substring(decimalIndex + 1)
            val integer = parseIntegerPart(text.substring(0, decimalIndex), groupSeparator)
            if (integer == null || !(decimals.isEmpty() || DIGITS.matches(decimals))) null
            else "$integer.$decimals"
        }
        else -> {
            val separator = if (lastDot >= 0) '.' else ','
            val parts = text.split(separator)
            val integer = parts[0]
            when {
                parts.size > 2 -> parseIntegerPart(text, separator)
                !(integer.isEmpty() || DIGITS.matches(integer)) -> null
                // "1.500" son mil quinientos, pero "0.500" o ".500" solo pueden ser un decimal
                parts[1].length == 3 && integer.isNotEmpty() && integer.trimStart('0').isNotEmpty() ->
                    if (integer.length <= 3) integer + parts[1] else null
                else -> "${integer.ifEmpty { "0" }}.${parts[1]}"
            }
        }
    }
    val value = normalized?.toDoubleOrNull() ?: return null
    return value.takeIf { it.isFinite() && it >= 0.0 && it <= MAX_AMOUNT }
}

/**
 * Tasa en porcentaje (p. ej. el interés mensual de una tarjeta): aquí punto y coma son siempre
 * el separador decimal, así "1.875" es 1,875 % y no 1875. Vacío cuenta como 0.
 * Devuelve null si no es un número entre 0 y 100.
 */
fun parsePercentRate(input: String): Double? {
    val text = input.filterNot { it.isWhitespace() || it == '%' }.replace(',', '.')
    if (text.isEmpty()) return 0.0
    if (text.any { !(it.isDigit() || it == '.') } || text.count { it == '.' } > 1 || text.none { it.isDigit() }) return null
    return text.toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..100.0 }
}
