package com.example.gestiongastos.ui.components

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, currencySymbol: String = "$"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "$currencySymbol ${formatter.format(amount)}"
}

fun formatCurrencyWithDecimals(amount: Double, currencySymbol: String = "$"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 0
    return "$currencySymbol ${formatter.format(amount)}"
}

fun formatUsd(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2
    return "USD $${formatter.format(amount)}"
}

fun parseAmount(input: String): Double? {
    val clean = input.trim()
    if (clean.isBlank()) return null

    val sanitized = clean.replace(" ", "").replace("$", "")

    return if (sanitized.contains(".") && !sanitized.contains(",")) {
        val parts = sanitized.split(".")
        if (parts.size > 2 || (parts.size == 2 && parts[1].length == 3)) {
            sanitized.replace(".", "").toDoubleOrNull()
        } else {
            sanitized.toDoubleOrNull()
        }
    } else if (sanitized.contains(",") && !sanitized.contains(".")) {
        val parts = sanitized.split(",")
        if (parts.size == 2 && parts[1].length != 3) {
            sanitized.replace(",", ".").toDoubleOrNull()
        } else {
            sanitized.replace(",", "").toDoubleOrNull()
        }
    } else if (sanitized.contains(".") && sanitized.contains(",")) {
        if (sanitized.lastIndexOf(".") > sanitized.lastIndexOf(",")) {
            sanitized.replace(",", "").toDoubleOrNull()
        } else {
            sanitized.replace(".", "").replace(",", ".").toDoubleOrNull()
        }
    } else {
        sanitized.toDoubleOrNull()
    }
}
