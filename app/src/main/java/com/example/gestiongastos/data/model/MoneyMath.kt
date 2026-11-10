package com.example.gestiongastos.data.model

import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.repository.isInMonth
import com.example.gestiongastos.data.repository.yearMonthOf
import java.time.YearMonth

// Cálculos puros que comparten varias pantallas (sin Android ni Compose) para poder probarlos.

/** [part] / [total], o 0 si el total no es positivo o el resultado no es un número finito (nunca NaN ni infinito). */
fun safeRatio(part: Double, total: Double): Double {
    if (!(total > 0.0)) return 0.0
    val ratio = part / total
    return if (ratio.isFinite()) ratio else 0.0
}

/** Porcentaje entero (truncado) de [part] sobre [total]; 0 si no se puede calcular. */
fun safePercent(part: Double, total: Double): Int = (safeRatio(part, total) * 100).toInt()

/** Resultado de validar un abono contra el saldo pendiente. */
sealed interface ExtraPaymentCheck {
    /** Abono válido; [amount] es lo que se guarda (nunca más que el saldo). */
    data class Valid(val amount: Double) : ExtraPaymentCheck
    object NotPositive : ExtraPaymentCheck
    object ExceedsBalance : ExtraPaymentCheck
}

/**
 * Valida un abono a una compra o a un gasto en cuotas.
 * El saldo real suele tener decimales (intereses, división en cuotas) pero en pantalla se ve redondeado,
 * así que se acepta el monto que el usuario ve (medio peso o medio centavo de margen) y se guarda como
 * mucho el saldo real, para no dejar la deuda en negativo.
 */
fun checkExtraPayment(amount: Double, balance: Double, currency: String): ExtraPaymentCheck {
    if (!amount.isFinite() || amount <= 0.0) return ExtraPaymentCheck.NotPositive
    val tolerance = if (currency == "USD") 0.005 else 0.5
    if (!balance.isFinite() || amount > balance + tolerance + 1e-9) return ExtraPaymentCheck.ExceedsBalance
    return ExtraPaymentCheck.Valid(amount.coerceAtMost(balance))
}

/**
 * Indica si un ingreso aplica en [targetYearMonth]: uno en cuotas, durante sus cuotas desde el mes de su fecha;
 * uno recurrente, desde su mes hasta su fecha de fin; uno único, solo en su mes.
 */
fun incomeAppliesInMonth(income: IncomeEntity, targetYearMonth: YearMonth): Boolean {
    val entryYearMonth = yearMonthOf(income.date)
    return if (income.totalInstallments > 1) {
        val diffMonths = (targetYearMonth.year - entryYearMonth.year) * 12 + (targetYearMonth.monthValue - entryYearMonth.monthValue)
        diffMonths in 0 until income.totalInstallments
    } else {
        isInMonth(targetYearMonth, entryYearMonth, income.isRecurring, income.endDateMillis)
    }
}

/**
 * true si la compra no es "del mes": es recurrente, o va en cuotas y viene de antes
 * (ya pasó la primera cuota o se compró en un mes anterior a [selectedYearMonth]).
 * Las pantallas la usan para separar las compras del mes de las cuotas arrastradas.
 */
fun PurchaseInstallmentDetail.isDeferredOrRecurring(selectedYearMonth: YearMonth): Boolean =
    isRecurring || (totalInstallments > 1 &&
        (currentInstallment > 1 || yearMonthOf(purchaseDateMillis).isBefore(selectedYearMonth)))

/** Totales del mes en pesos. Es la única cuenta que usan el Resumen y el reporte CSV. */
data class MonthTotals(
    val totalIncome: Double,
    val directExpenses: Double,
    val creditCardExpenses: Double,
    val totalSavings: Double
) {
    val totalExpenses: Double get() = directExpenses + creditCardExpenses
    val neededAmount: Double get() = totalExpenses + totalSavings
    val freeAmount: Double get() = totalIncome - neededAmount

    /** Parte del ingreso ya comprometida (0 a 2). Sin ingresos pero con gastos cuenta como 1. */
    val spentRatio: Float
        get() = when {
            totalIncome > 0 -> (neededAmount / totalIncome).toFloat().coerceIn(0f, 2f)
            neededAmount > 0 -> 1f
            else -> 0f
        }
}

/**
 * Suma el mes: ingresos tal cual (marcarlos como recibidos es solo visual y no entra aquí),
 * gastos en dólares convertidos con [usdRate] y sin contar lo que esté en [ignoredKeys].
 */
fun computeMonthTotals(
    incomes: List<IncomeEntity>,
    expenses: List<ExpenseEntity>,
    savings: List<SavingsEntity>,
    cards: List<CardMonthSummary>,
    usdRate: Double,
    ignoredKeys: Set<String> = emptySet()
): MonthTotals = MonthTotals(
    // Los pesos se suman en centavos enteros (exacto); solo lo que está en dólares pasa por la TRM
    totalIncome = centsToAmount(incomes.sumOf { it.amountCents }),
    directExpenses = expenses
        .filter { ItemKeys.direct(it.id) !in ignoredKeys }
        .let { counted ->
            centsToAmount(counted.filter { it.currency != "USD" }.sumOf { it.amountCents }) +
                counted.filter { it.currency == "USD" }.sumOf { it.amount * usdRate }
        },
    creditCardExpenses = cards.sumOf { it.payableThisMonth(usdRate, ignoredKeys) },
    totalSavings = centsToAmount(
        savings.filter { ItemKeys.saving(it.id) !in ignoredKeys }.sumOf { it.amountCents }
    )
)

/**
 * Sube al inicio los ítems marcados (pagados o recibidos) sin cambiar el orden entre los demás.
 * Con [enabled] en false devuelve la lista tal cual.
 */
fun <T> markedFirst(items: List<T>, enabled: Boolean, isMarked: (T) -> Boolean): List<T> =
    if (enabled) items.sortedWith(compareByDescending(isMarked)) else items

/**
 * Ordena una lista de gastos o compras según las preferencias del usuario: cada grupo (lo del mes
 * y lo arrastrado: cuotas, fijos, recurrentes) va en [sortOrder], y [position] decide cuál grupo va primero.
 */
fun <T> arrangeByRegistration(
    items: List<T>,
    oldestFirst: Comparator<T>,
    sortOrder: ExpenseSortOrder,
    position: InstallmentPosition,
    isCarriedOver: (T) -> Boolean
): List<T> {
    val order = if (sortOrder == ExpenseSortOrder.OLDEST_FIRST) oldestFirst else oldestFirst.reversed()
    val (carriedOver, currentMonth) = items.partition(isCarriedOver)
    val sortedCarriedOver = carriedOver.sortedWith(order)
    val sortedCurrentMonth = currentMonth.sortedWith(order)
    return if (position == InstallmentPosition.FIRST) sortedCarriedOver + sortedCurrentMonth
    else sortedCurrentMonth + sortedCarriedOver
}

/** true si en el mes hay algo en dólares (un gasto, una cuota activa o una cuota de manejo), es decir, si la TRM influye en los totales. */
fun monthUsesUsd(expenses: List<ExpenseEntity>, cards: List<CardMonthSummary>): Boolean =
    expenses.any { it.currency == "USD" } || cards.any { card ->
        (card.currency == "USD" && !card.isFeeWaivedThisMonth && card.maintenanceFee > 0.0) ||
            card.purchases.any { it.currency == "USD" && it.statusType == InstallmentStatusType.ACTIVE }
    }

/**
 * Número de cuota de un ingreso en cuotas en [targetYearMonth] (1 en el mes de su fecha), siempre entre
 * 1 y el total. Un ingreso sin cuotas (o con un total inválido) es la cuota 1.
 */
fun incomeInstallmentNumber(income: IncomeEntity, targetYearMonth: YearMonth): Int {
    if (income.totalInstallments <= 1) return 1
    val entryYearMonth = yearMonthOf(income.date)
    val diffMonths = (targetYearMonth.year - entryYearMonth.year) * 12 + (targetYearMonth.monthValue - entryYearMonth.monthValue)
    return (diffMonths + 1).coerceIn(1, income.totalInstallments)
}
