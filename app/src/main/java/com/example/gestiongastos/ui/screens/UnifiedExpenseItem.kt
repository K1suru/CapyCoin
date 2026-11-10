package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.data.repository.localDateOf
import com.example.gestiongastos.data.model.ExpenseInstallmentInfo
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail

// Ítems de la lista de Gastos (gasto fijo, cuota de tarjeta, cuota de manejo, ahorro) y cómo se ordenan y agrupan.
sealed class UnifiedExpenseItem {
    abstract val key: String
    abstract val concept: String
    abstract val amount: Double
    abstract val dateMillis: Long
    abstract val isCarriedOver: Boolean

    data class DirectExpense(
        val expense: ExpenseEntity,
        override val isCarriedOver: Boolean,
        val installment: ExpenseInstallmentInfo? = null // solo si es un gasto fijo en cuotas
    ) : UnifiedExpenseItem() {
        override val key: String = "direct_${expense.id}"
        override val concept: String = expense.concept
        override val amount: Double = expense.amount
        override val dateMillis: Long = expense.date
    }

    data class CardInstallmentExpense(
        val cardName: String,
        val detail: PurchaseInstallmentDetail,
        val currencySymbol: String,
        override val isCarriedOver: Boolean
    ) : UnifiedExpenseItem() {
        override val key: String = "card_inst_${detail.purchaseId}"
        override val concept: String = detail.concept
        override val amount: Double = detail.monthlyPayment
        override val dateMillis: Long = detail.purchaseDateMillis
    }

    data class CardFeeExpense(
        val cardId: Long,
        val cardName: String,
        val fee: Double,
        val currencySymbol: String
    ) : UnifiedExpenseItem() {
        override val key: String = "card_fee_${cardId}"
        override val concept: String = tr("Cuota de manejo ($cardName)", "Card fee ($cardName)")
        override val amount: Double = fee
        override val dateMillis: Long = 0L
        override val isCarriedOver: Boolean = true
    }

    data class SavingsExpense(
        val saving: SavingsEntity,
        override val isCarriedOver: Boolean
    ) : UnifiedExpenseItem() {
        override val key: String = "saving_${saving.id}"
        override val concept: String = saving.concept
        override val amount: Double = saving.amount
        override val dateMillis: Long = saving.date
    }
}

/**
 * Orden de registro (antiguos primero): por día y, dentro del mismo día, por tipo e id.
 * No se usa la hora porque lo registrado viendo otro mes queda el día 15 con la hora del momento,
 * y entonces lo anotado de noche quedaba después de lo anotado en la mañana de días posteriores.
 */
internal val registrationOrder: Comparator<UnifiedExpenseItem> = compareBy(
    { localDateOf(it.dateMillis) },
    {
        when (it) {
            is UnifiedExpenseItem.DirectExpense -> 0
            is UnifiedExpenseItem.CardInstallmentExpense -> 1
            is UnifiedExpenseItem.CardFeeExpense -> 2
            is UnifiedExpenseItem.SavingsExpense -> 3
        }
    },
    {
        when (it) {
            is UnifiedExpenseItem.DirectExpense -> it.expense.id
            is UnifiedExpenseItem.CardInstallmentExpense -> it.detail.purchaseId
            is UnifiedExpenseItem.CardFeeExpense -> it.cardId
            is UnifiedExpenseItem.SavingsExpense -> it.saving.id
        }
    }
)

/** Monto del ítem en COP (los USD se convierten con la TRM). */
fun UnifiedExpenseItem.amountInCop(usdRate: Double): Double = when (this) {
    is UnifiedExpenseItem.DirectExpense -> if (expense.currency == "USD") expense.amount * usdRate else expense.amount
    is UnifiedExpenseItem.CardInstallmentExpense -> detail.monthlyPaymentInCop(usdRate)
    else -> amount
}

private val CARRIED_OVER_SECTION: String get() = tr("Cuotas, fijos y recurrentes", "Installments, fixed and recurring")

/** Agrupa conservando el orden: cada día del mes ("Hoy", "Ayer", "Jueves 9 de octubre") y un bloque para lo que se repite. */
fun groupExpenseItems(items: List<UnifiedExpenseItem>): List<Pair<String, List<UnifiedExpenseItem>>> {
    val today = java.time.LocalDate.now()
    val dayFormatter = java.time.format.DateTimeFormatter.ofPattern(tr("EEEE d 'de' MMMM", "EEEE, MMMM d"), appLocale())
    val groups = LinkedHashMap<String, MutableList<UnifiedExpenseItem>>()
    items.forEach { item ->
        val title = if (item.isCarriedOver) {
            CARRIED_OVER_SECTION
        } else {
            val day = localDateOf(item.dateMillis)
            when (day) {
                today -> tr("Hoy", "Today")
                today.minusDays(1) -> tr("Ayer", "Yesterday")
                else -> day.format(dayFormatter).replaceFirstChar { it.uppercase() }
            }
        }
        groups.getOrPut(title) { mutableListOf() }.add(item)
    }
    return groups.map { it.key to it.value }
}
