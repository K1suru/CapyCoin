package com.example.gestiongastos.data.model

import com.example.gestiongastos.i18n.tr
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth

enum class InstallmentStatusType {
    FUTURE,
    ACTIVE,
    FINISHED
}

enum class InstallmentPosition(private val es: String, private val en: String) {
    LAST("Al final (De últimas)", "At the end (last)"),
    FIRST("Al inicio (De primeras)", "At the start (first)");

    val displayName: String get() = tr(es, en)
}

enum class ExpenseSortOrder(private val es: String, private val en: String) {
    NEWEST_FIRST("Más recientes primero", "Newest first"),
    OLDEST_FIRST("En orden de registro (Antiguos primero)", "In order added (oldest first)");

    val displayName: String get() = tr(es, en)
}

/** Abono a una compra; [yearMonth] es el mes en que se registró (null = datos antiguos, aplica desde el inicio). */
data class PurchasePayment(
    val yearMonth: YearMonth?,
    val amount: Double
)

data class PurchaseInstallmentDetail(
    val purchaseId: Long,
    val cardId: Long,
    val concept: String,
    val totalAmount: Double,
    val totalInstallments: Int,
    val currentInstallment: Int,
    val monthlyPayment: Double,
    val statusType: InstallmentStatusType,
    val statusText: String,
    val purchaseDateMillis: Long,
    val isInterestFree: Boolean = false,
    val isFirstInstallmentInterestFree: Boolean = false,
    val interestRate: Double = 0.0,
    val isRecurring: Boolean = false,
    val totalPaidAmount: Double = 0.0,
    val currency: String = "COP",
    val isSettledThisMonth: Boolean = false,
    val settledYearMonth: YearMonth? = null,
    /** Saldo de capital pendiente al inicio del mes consultado (incluye la cuota de este mes). */
    val remainingBalanceAmount: Double = 0.0
) {
    val remainingInstallmentsCount: Int
        get() = (totalInstallments - currentInstallment + 1).coerceIn(0, totalInstallments.coerceAtLeast(1))

    val remainingBalanceToPay: Double
        get() = if (statusType == InstallmentStatusType.FINISHED) 0.0 else remainingBalanceAmount.coerceAtLeast(0.0)

    fun monthlyPaymentInCop(usdRate: Double = DEFAULT_USD_RATE): Double {
        return if (currency == "USD") monthlyPayment * usdRate else monthlyPayment
    }

    fun remainingBalanceToPayInCop(usdRate: Double = DEFAULT_USD_RATE): Double {
        return if (currency == "USD") remainingBalanceToPay * usdRate else remainingBalanceToPay
    }

    fun totalAmountInCop(usdRate: Double = DEFAULT_USD_RATE): Double {
        return if (currency == "USD") totalAmount * usdRate else totalAmount
    }
}

data class CardMonthSummary(
    val cardId: Long,
    val cardName: String,
    val cutoffDay: Int,
    val paymentDay: Int = 0,
    val interestRate: Double,
    val maintenanceFee: Double,
    val currency: String,
    val isFeeWaiverEnabled: Boolean = false,
    val minSpendForFeeWaiver: Double = 0.0,
    val isFeeWaivedThisMonth: Boolean = false,
    val isFirstInstallmentInterestFree: Boolean = false,
    val currentMonthSpending: Double = 0.0,
    val creditLimit: Double = 0.0,
    val colorIndex: Int = -1,
    val purchases: List<PurchaseInstallmentDetail>,
    val totalActiveInstallmentsPayment: Double,
    val totalMonthlyCardExpense: Double // active installments (converted to COP if USD) + maintenance fee
)

const val DEFAULT_USD_RATE = 4150.0

/** Claves con las que se marcan ítems como pagados/ignorados por mes. */
object ItemKeys {
    fun direct(expenseId: Long) = "direct_$expenseId"
    fun cardInstallment(purchaseId: Long) = "card_inst_$purchaseId"
    fun cardFee(cardId: Long) = "card_fee_$cardId"
    fun saving(savingId: Long) = "saving_$savingId"
}

/** Cuota de manejo cobrada este mes en COP (0 si está exonerada o no aplica). */
fun CardMonthSummary.maintenanceFeeInCop(usdRate: Double): Double = when {
    isFeeWaivedThisMonth || maintenanceFee <= 0.0 -> 0.0
    currency == "USD" -> maintenanceFee * usdRate
    else -> maintenanceFee
}

/** Total a pagar de la tarjeta en el mes (COP), excluyendo los ítems ignorados. */
fun CardMonthSummary.payableThisMonth(usdRate: Double, ignoredKeys: Set<String> = emptySet()): Double {
    val installments = purchases
        .filter { it.statusType == InstallmentStatusType.ACTIVE && ItemKeys.cardInstallment(it.purchaseId) !in ignoredKeys }
        .sumOf { it.monthlyPaymentInCop(usdRate) }
    val fee = if (ItemKeys.cardFee(cardId) in ignoredKeys) 0.0 else maintenanceFeeInCop(usdRate)
    return installments + fee
}

private fun monthsBetween(from: YearMonth, to: YearMonth): Int =
    (to.year - from.year) * 12 + (to.monthValue - from.monthValue)

/** Cuota fija (sistema francés) para [principal] en [installments] cuotas con tasa mensual [rate] (fracción). */
fun frenchPayment(principal: Double, rate: Double, installments: Int): Double {
    val n = installments.coerceAtLeast(1)
    if (principal <= 0.0) return 0.0
    if (rate <= 0.0) return principal / n
    val factor = Math.pow(1.0 + rate, n.toDouble())
    return principal * (rate * factor) / (factor - 1.0)
}

/**
 * Plan de cuotas sin abonos. Es el mismo cálculo que usa la app para las compras,
 * así el simulador y las compras reales coinciden.
 */
fun installmentSchedule(
    principal: Double,
    installments: Int,
    monthlyRatePercent: Double,
    isInterestFree: Boolean,
    isFirstInstallmentInterestFree: Boolean
): List<Double> {
    val n = installments.coerceAtLeast(1)
    val baseRate = if (isInterestFree || monthlyRatePercent <= 0.0 || n <= 1) 0.0 else monthlyRatePercent / 100.0
    var balance = principal
    val result = mutableListOf<Double>()
    for (i in 1..n) {
        val rate = if (isFirstInstallmentInterestFree && i == 1) 0.0 else baseRate
        val cuota = frenchPayment(balance, rate, n - i + 1)
        result.add(cuota)
        balance = (balance * (1.0 + rate) - cuota).coerceAtLeast(0.0)
    }
    return result
}

fun calculatePurchaseInstallmentDetail(
    purchaseId: Long,
    cardId: Long,
    concept: String,
    totalAmount: Double,
    totalInstallments: Int,
    purchaseDateMillis: Long,
    targetYearMonth: YearMonth,
    cutoffDay: Int = 0,
    paymentDay: Int = 0,
    interestRate: Double = 0.0,
    isInterestFree: Boolean = false,
    isFirstInstallmentInterestFree: Boolean = false,
    isRecurring: Boolean = false,
    payments: List<PurchasePayment> = emptyList(),
    currency: String = "COP",
    settledYearMonth: YearMonth? = null
): PurchaseInstallmentDetail {
    val purchaseLocalDate = try {
        Instant.ofEpochMilli(purchaseDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } catch (e: Exception) {
        java.time.LocalDate.now()
    }
    val baseYearMonth = YearMonth.from(purchaseLocalDate)

    // 1. Determine which billing cycle (cutoff month) this purchase belongs to:
    val cutoffYearMonth = if (cutoffDay in 1..31 && purchaseLocalDate.dayOfMonth > cutoffDay) {
        baseYearMonth.plusMonths(1)
    } else {
        baseYearMonth
    }

    // 2. Determine which month the bill for that cycle is actually due/paid:
    val startYearMonth = when {
        paymentDay in 1..31 -> {
            if (paymentDay > cutoffDay) {
                cutoffYearMonth
            } else {
                cutoffYearMonth.plusMonths(1)
            }
        }
        cutoffDay in 28..31 -> {
            cutoffYearMonth.plusMonths(1)
        }
        else -> {
            cutoffYearMonth
        }
    }

    val currentInstallment = monthsBetween(startYearMonth, targetYearMonth) + 1
    val n = totalInstallments.coerceAtLeast(1)
    val baseRate = if (isInterestFree || interestRate <= 0.0 || n <= 1) 0.0 else interestRate / 100.0
    val rateFor: (Int) -> Double = { installment ->
        if (isFirstInstallmentInterestFree && installment == 1) 0.0 else baseRate
    }

    // Solo cuentan los abonos hechos hasta el mes consultado (no afectan meses anteriores)
    val paymentsUpToTarget = payments.filter { it.yearMonth == null || !it.yearMonth.isAfter(targetYearMonth) }
    val totalPaidUpToTarget = paymentsUpToTarget.sumOf { it.amount }
    // Cuota (1..n) en la que se aplica cada abono
    val installmentOfPayment: (PurchasePayment) -> Int = { p ->
        if (p.yearMonth == null) 1 else (monthsBetween(startYearMonth, p.yearMonth) + 1).coerceAtLeast(1)
    }

    val isSettledThisMonth = settledYearMonth != null && targetYearMonth == settledYearMonth
    val isAfterSettlement = settledYearMonth != null && targetYearMonth.isAfter(settledYearMonth)

    var remainingBalance = 0.0
    val (statusType, statusText, payment) = when {
        isAfterSettlement -> {
            Triple(
                InstallmentStatusType.FINISHED,
                tr("Liquidado en ${settledYearMonth!!.monthValue}/${settledYearMonth.year}", "Paid off in ${settledYearMonth!!.monthValue}/${settledYearMonth.year}"),
                0.0
            )
        }
        isRecurring -> {
            // Cobro mensual fijo; un abono solo reduce el cobro del mes en que se hizo
            val baseCharge = frenchPayment(totalAmount, baseRate, n)
            val paidThisMonth = payments.filter { it.yearMonth == targetYearMonth }.sumOf { it.amount }
            val charge = (baseCharge - paidThisMonth).coerceAtLeast(0.0)
            remainingBalance = charge
            when {
                currentInstallment < 1 && !isSettledThisMonth -> Triple(
                    InstallmentStatusType.FUTURE,
                    tr("Inicia en ${startYearMonth.monthValue}/${startYearMonth.year}", "Starts in ${startYearMonth.monthValue}/${startYearMonth.year}"),
                    0.0
                )
                isSettledThisMonth -> Triple(InstallmentStatusType.ACTIVE, tr("Cancelado este mes (Último cobro)", "Cancelled this month (last charge)"), charge)
                charge <= 0.0 -> Triple(InstallmentStatusType.ACTIVE, tr("Abonado este mes", "Paid extra this month"), 0.0)
                else -> Triple(InstallmentStatusType.ACTIVE, tr("Recurrente mensual", "Repeats monthly"), charge)
            }
        }
        currentInstallment < 1 -> {
            remainingBalance = (totalAmount - totalPaidUpToTarget).coerceAtLeast(0.0)
            when {
                remainingBalance <= 0.0 -> Triple(InstallmentStatusType.FINISHED, tr("Totalmente Abonado", "Fully paid"), 0.0)
                isSettledThisMonth -> Triple(InstallmentStatusType.ACTIVE, tr("Pagado completo", "Paid in full"), remainingBalance)
                else -> Triple(
                    InstallmentStatusType.FUTURE,
                    tr("Inicia en ${startYearMonth.monthValue}/${startYearMonth.year}", "Starts in ${startYearMonth.monthValue}/${startYearMonth.year}"),
                    0.0
                )
            }
        }
        currentInstallment > n -> {
            Triple(InstallmentStatusType.FINISHED, tr("Finalizado", "Finished"), 0.0)
        }
        else -> {
            // Recorre el plan mes a mes aplicando cada abono en la cuota que le corresponde
            var balance = totalAmount
            var cuota = 0.0
            for (i in 1..currentInstallment) {
                balance -= paymentsUpToTarget.filter { installmentOfPayment(it) == i }.sumOf { it.amount }
                if (balance <= 0.000001) {
                    balance = 0.0
                    cuota = 0.0
                    break
                }
                val rate = rateFor(i)
                cuota = frenchPayment(balance, rate, n - i + 1)
                if (i < currentInstallment) {
                    balance = (balance * (1.0 + rate) - cuota).coerceAtLeast(0.0)
                }
            }
            remainingBalance = balance
            when {
                balance <= 0.0 -> Triple(InstallmentStatusType.FINISHED, tr("Totalmente Abonado", "Fully paid"), 0.0)
                isSettledThisMonth -> {
                    val label = if (n > 1) tr("Liquidado este mes (Cuotas $currentInstallment a $n)", "Paid off this month (installments $currentInstallment to $n)") else tr("Pagado completo", "Paid in full")
                    Triple(InstallmentStatusType.ACTIVE, label, balance)
                }
                else -> {
                    val label = if (currentInstallment == 1 && isFirstInstallmentInterestFree && !isInterestFree && interestRate > 0.0 && n > 1) {
                        tr("Cuota 1 de $n (1ª cuota sin interés)", "Installment 1 of $n (1st installment interest-free)")
                    } else {
                        tr("Cuota $currentInstallment de $totalInstallments", "Installment $currentInstallment of $totalInstallments")
                    }
                    Triple(InstallmentStatusType.ACTIVE, label, cuota)
                }
            }
        }
    }

    return PurchaseInstallmentDetail(
        purchaseId = purchaseId,
        cardId = cardId,
        concept = concept,
        totalAmount = totalAmount,
        totalInstallments = totalInstallments,
        currentInstallment = currentInstallment,
        monthlyPayment = payment,
        statusType = statusType,
        statusText = statusText,
        purchaseDateMillis = purchaseDateMillis,
        isInterestFree = isInterestFree,
        isFirstInstallmentInterestFree = isFirstInstallmentInterestFree,
        interestRate = interestRate,
        isRecurring = isRecurring,
        totalPaidAmount = if (isRecurring) payments.filter { it.yearMonth == targetYearMonth }.sumOf { it.amount } else totalPaidUpToTarget,
        currency = currency,
        isSettledThisMonth = isSettledThisMonth,
        settledYearMonth = settledYearMonth,
        remainingBalanceAmount = remainingBalance
    )
}

/**
 * Estado de un gasto fijo en cuotas en un mes (p. ej. una deuda con una persona).
 * No hay intereses: el saldo es cuota × cuotas y los abonos lo bajan sin cambiar el valor de la cuota,
 * así que la deuda termina antes (y la última cuota puede ser más pequeña).
 */
data class ExpenseInstallmentInfo(
    val installmentAmount: Double,   // valor fijo de la cuota
    val currentInstallment: Int,     // cuota de este mes (1..totalInstallments)
    val totalInstallments: Int,      // total de cuotas contando los abonos
    val monthCharge: Double,         // lo que se paga este mes (0 si un abono ya dejó todo pagado)
    val remainingBalance: Double,    // saldo pendiente al empezar el mes (incluye la cuota de este mes)
    val paidExtraTotal: Double,      // abonos hechos hasta este mes
    val isPaidOff: Boolean           // un abono de este mes terminó la deuda
)

/**
 * Calcula la cuota de [targetYearMonth]. Devuelve null si ese mes el gasto no aplica
 * (antes de empezar o cuando la deuda ya terminó en un mes anterior).
 * Un abono cuenta desde el mes en que se hizo y no cambia los meses anteriores.
 */
fun calculateExpenseInstallment(
    installmentAmount: Double,
    installments: Int,
    startYearMonth: YearMonth,
    targetYearMonth: YearMonth,
    payments: List<PurchasePayment> = emptyList()
): ExpenseInstallmentInfo? {
    if (installments <= 0 || installmentAmount <= 0.0) return null
    val current = monthsBetween(startYearMonth, targetYearMonth) + 1
    if (current < 1) return null

    val paymentsUpToTarget = payments.filter { it.yearMonth == null || !it.yearMonth.isAfter(targetYearMonth) }
    val paymentMonth: (PurchasePayment) -> Int = { p ->
        if (p.yearMonth == null) 1 else (monthsBetween(startYearMonth, p.yearMonth) + 1).coerceAtLeast(1)
    }

    var balance = installmentAmount * installments
    for (i in 1..current) {
        balance -= paymentsUpToTarget.filter { paymentMonth(it) == i }.sumOf { it.amount }
        if (balance <= 0.000001) {
            balance = 0.0
            // Terminó este mes con un abono: se muestra este mes (pagado) y ya no después
            return if (i == current) {
                ExpenseInstallmentInfo(installmentAmount, current, current, 0.0, 0.0, paymentsUpToTarget.sumOf { it.amount }, true)
            } else null
        }
        if (i < current) balance -= minOf(installmentAmount, balance)
        if (balance <= 0.000001 && i < current) return null // ya se pagó la última cuota
    }
    val charge = minOf(installmentAmount, balance)
    val remainingAfterThis = Math.ceil(((balance - charge) / installmentAmount) - 0.000001).toInt().coerceAtLeast(0)
    return ExpenseInstallmentInfo(
        installmentAmount = installmentAmount,
        currentInstallment = current,
        totalInstallments = current + remainingAfterThis,
        monthCharge = charge,
        remainingBalance = balance,
        paidExtraTotal = paymentsUpToTarget.sumOf { it.amount },
        isPaidOff = false
    )
}
