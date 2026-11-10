package com.example.gestiongastos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.gestiongastos.data.local.ExpenseEntity


// Ayudantes de los ingresos de tipo reembolso: los ids de los gastos y compras vinculados se guardan como texto ("1,2,5").
internal fun parseIdsSet(text: String?): Set<Long> =
    text?.split(",")?.mapNotNull { it.trim().toLongOrNull() }?.toSet() ?: emptySet()

internal fun formatIdsSet(ids: Set<Long>): String? =
    if (ids.isEmpty()) null else ids.joinToString(",")

internal fun expenseAmountInCop(expense: ExpenseEntity, usdRate: Double): Double =
    if (expense.currency == "USD") expense.amount * usdRate else expense.amount
