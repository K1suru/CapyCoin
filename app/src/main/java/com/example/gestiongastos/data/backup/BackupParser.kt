package com.example.gestiongastos.data.backup

import com.example.gestiongastos.data.repository.yearMonthOrNull
import com.example.gestiongastos.data.model.MAX_AMOUNT_CENTS
import com.example.gestiongastos.data.model.toCentsOrNull
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CardPurchasePaymentEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.ExpensePaymentEntity
import com.example.gestiongastos.data.local.IgnoredExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.OmittedInstallmentEntity
import com.example.gestiongastos.data.local.PaidCreditCardEntity
import com.example.gestiongastos.data.local.PaidExpenseEntity
import com.example.gestiongastos.data.local.ReceivedIncomeEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.local.SettledPurchaseEntity

// Validación de una copia de seguridad ANTES de tocar la base de datos.
// Es lógica pura (mapas y listas, sin Android ni org.json) para poder probarla con tests unitarios.

/** Versión del formato que escribe y entiende esta versión de la app. */
// v1: montos con decimales ("amount"). v2: montos en centavos enteros ("amountCents"). Se leen las dos.
const val BACKUP_FORMAT_VERSION = 2

/** Nombres de las listas de la copia, en el orden en que se muestran al usuario. */
object BackupSections {
    const val INCOMES = "incomes"
    const val EXPENSES = "expenses"
    const val EXPENSE_PAYMENTS = "expense_payments"
    const val CREDIT_CARDS = "credit_cards"
    const val CARD_PURCHASES = "card_purchases"
    const val SAVINGS = "savings"
    const val OMITTED_INSTALLMENTS = "omitted_installments"
    const val CARD_PURCHASE_PAYMENTS = "card_purchase_payments"
    const val IGNORED_EXPENSES = "ignored_expenses"
    const val PAID_CREDIT_CARDS = "paid_credit_cards"
    const val PAID_EXPENSES = "paid_expenses"
    const val SETTLED_PURCHASES = "settled_purchases"
    const val RECEIVED_INCOMES = "received_incomes"

    val ALL = listOf(
        INCOMES, EXPENSES, EXPENSE_PAYMENTS, CREDIT_CARDS, CARD_PURCHASES, SAVINGS,
        OMITTED_INSTALLMENTS, CARD_PURCHASE_PAYMENTS, IGNORED_EXPENSES, PAID_CREDIT_CARDS,
        PAID_EXPENSES, SETTLED_PURCHASES, RECEIVED_INCOMES
    )
}

data class BackupData(
    val incomes: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val expensePayments: List<ExpensePaymentEntity> = emptyList(),
    val cards: List<CreditCardEntity> = emptyList(),
    val purchases: List<CardPurchaseEntity> = emptyList(),
    val savings: List<SavingsEntity> = emptyList(),
    val omittedInstallments: List<OmittedInstallmentEntity> = emptyList(),
    val purchasePayments: List<CardPurchasePaymentEntity> = emptyList(),
    val ignoredExpenses: List<IgnoredExpenseEntity> = emptyList(),
    val paidCards: List<PaidCreditCardEntity> = emptyList(),
    val paidExpenses: List<PaidExpenseEntity> = emptyList(),
    val settledPurchases: List<SettledPurchaseEntity> = emptyList(),
    val receivedIncomes: List<ReceivedIncomeEntity> = emptyList()
) {
    val totalRecords: Int
        get() = incomes.size + expenses.size + expensePayments.size + cards.size + purchases.size +
            savings.size + omittedInstallments.size + purchasePayments.size + ignoredExpenses.size +
            paidCards.size + paidExpenses.size + settledPurchases.size + receivedIncomes.size
}

/** Motivo por el que una copia se rechaza entera (no se borra ni se cambia nada). */
enum class BackupRejection {
    /** No tiene ninguna lista de datos de CapyCoin. */
    NOT_A_BACKUP,
    /** Dice ser de otra aplicación. */
    WRONG_APP,
    /** La versión no es un número válido o es más nueva que la que entiende esta app. */
    UNSUPPORTED_VERSION,
    /** Alguna lista de datos no es una lista. */
    INVALID_STRUCTURE,
    /** No queda ningún registro válido: restaurarla solo borraría los datos actuales. */
    EMPTY
}

sealed interface BackupParseOutcome {
    /**
     * Copia válida. Cuenta, por lista, los registros que NO se van a restaurar:
     * [discarded] por datos inválidos o repetidos (p. ej. un monto que no es un número) y
     * [orphaned] porque dependen de algo que no viene en la copia (p. ej. un abono de una compra borrada),
     * así que en la app no se verían.
     */
    data class Ok(
        val data: BackupData,
        val discarded: Map<String, Int> = emptyMap(),
        val orphaned: Map<String, Int> = emptyMap()
    ) : BackupParseOutcome {
        val discardedTotal: Int get() = discarded.values.sum()
        val orphanedTotal: Int get() = orphaned.values.sum()
    }

    data class Rejected(val reason: BackupRejection) : BackupParseOutcome
}

// ---- Lectura estricta de valores ----

private fun Map<String, Any?>.present(key: String): Boolean = this[key] != null

/** Entero: acepta números sin parte decimal y textos numéricos. */
internal fun backupLong(value: Any?): Long? = when (value) {
    is Double, is Float -> (value as Number).toDouble()
        .takeIf { it.isFinite() && it % 1.0 == 0.0 && kotlin.math.abs(it) < 9.0e18 }?.toLong()
    is Number -> value.toLong()
    is String -> value.trim().toLongOrNull()
    else -> null
}

private fun backupInt(value: Any?): Int? =
    backupLong(value)?.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.toInt()

/** Monto o tasa: un número finito y no negativo. Cualquier otra cosa (NaN, texto, negativo) es null. */
internal fun backupMoney(value: Any?): Double? {
    val number = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }
    return number?.takeIf { it.isFinite() && it >= 0.0 }
}

private fun backupBoolean(value: Any?, default: Boolean): Boolean = when (value) {
    is Boolean -> value
    is String -> value.equals("true", ignoreCase = true)
    else -> default
}

private fun backupText(value: Any?): String? = when (value) {
    null -> null
    is Map<*, *>, is List<*> -> null
    else -> value.toString()
}

/** Mes "2026-08" válido, o null. */
internal fun backupYearMonth(value: Any?): String? {
    return yearMonthOrNull(value as? String)?.toString()
}

/** Señal interna: el registro no se puede restaurar. */
private class InvalidRecord : Exception()

private class RecordReader(private val obj: Map<String, Any?>) {
    /** Identificador: si falta es 0 (Room asigna uno); si está, debe ser un entero no negativo. */
    fun id(key: String = "id"): Long {
        if (!obj.present(key)) return 0L
        return backupLong(obj[key])?.takeIf { it >= 0L } ?: throw InvalidRecord()
    }

    /** Referencia a otro registro: obligatoria y positiva. */
    fun ref(key: String): Long = backupLong(obj[key])?.takeIf { it > 0L } ?: throw InvalidRecord()

    /** Monto obligatorio: si falta o no es válido el registro se descarta (nunca se convierte en 0). */
    fun money(key: String): Double = backupMoney(obj[key]) ?: throw InvalidRecord()

    /**
     * Monto obligatorio en centavos. Una copia v2 lo trae como entero en "<key>Cents"; una v1, con decimales
     * en "<key>" y aquí se redondea al centavo. Si falta o no es válido el registro se descarta.
     */
    fun cents(key: String): Long = centsOrNull(key) ?: throw InvalidRecord()

    /** Monto opcional en centavos: si falta vale 0; si está pero no es válido el registro se descarta. */
    fun centsOr(key: String): Long =
        if (!obj.present("${key}Cents") && !obj.present(key)) 0L else centsOrNull(key) ?: throw InvalidRecord()

    private fun centsOrNull(key: String): Long? =
        if (obj.present("${key}Cents")) backupLong(obj["${key}Cents"])?.takeIf { it in 0L..MAX_AMOUNT_CENTS }
        else backupMoney(obj[key])?.let { toCentsOrNull(it) }

    /** Monto opcional: si falta vale [default]; si está pero no es válido el registro se descarta. */
    fun moneyOr(key: String, default: Double): Double =
        if (!obj.present(key)) default else backupMoney(obj[key]) ?: throw InvalidRecord()

    /** Fecha obligatoria en milisegundos. */
    fun date(key: String): Long = backupLong(obj[key])?.takeIf { it > 0L } ?: throw InvalidRecord()

    fun dateOr(key: String, default: Long): Long =
        if (!obj.present(key)) default else date(key)

    fun nullableLong(key: String): Long? =
        if (!obj.present(key)) null else backupLong(obj[key]) ?: throw InvalidRecord()

    fun intOr(key: String, default: Int): Int =
        if (!obj.present(key)) default else backupInt(obj[key]) ?: throw InvalidRecord()

    fun boolean(key: String, default: Boolean = false): Boolean = backupBoolean(obj[key], default)

    fun textOr(key: String, default: String): String = backupText(obj[key])?.takeIf { it.isNotBlank() } ?: default

    fun nullableText(key: String): String? = backupText(obj[key])?.takeIf { it.isNotBlank() }

    fun yearMonth(key: String): String = backupYearMonth(obj[key]) ?: throw InvalidRecord()

    /** Mes de un abono: los datos antiguos lo traen vacío (el abono aplica desde el inicio) y se conserva así. */
    fun legacyYearMonth(key: String): String = backupText(obj[key]).orEmpty()
}

private class SectionParser(private val root: Map<String, Any?>) {
    val discarded = linkedMapOf<String, Int>()
    val orphaned = linkedMapOf<String, Int>()

    private fun discard(section: String) {
        discarded[section] = (discarded[section] ?: 0) + 1
    }

    /** Lee una lista: descarta (y cuenta) lo que no es un registro válido o repite un id. */
    fun <T> parse(section: String, idOf: (T) -> Long, build: RecordReader.() -> T): List<T> {
        val array = root[section] as? List<*> ?: return emptyList()
        val seenIds = hashSetOf<Long>()
        val result = ArrayList<T>(array.size)
        for (item in array) {
            @Suppress("UNCHECKED_CAST")
            val obj = item as? Map<String, Any?>
            val record = if (obj == null) null else try {
                RecordReader(obj).build()
            } catch (e: InvalidRecord) {
                null
            }
            val id = record?.let(idOf)
            if (record == null || (id != null && id != 0L && !seenIds.add(id))) {
                discard(section)
            } else {
                result.add(record)
            }
        }
        return result
    }

    /** Quita (y cuenta) los registros cuyo "padre" no viene en la copia: quedarían invisibles en la app. */
    fun <T> keepLinked(section: String, items: List<T>, parentIds: Set<Long>, parentOf: (T) -> Long): List<T> {
        val kept = items.filter { parentOf(it) in parentIds }
        if (kept.size < items.size) orphaned[section] = items.size - kept.size
        return kept
    }
}

/**
 * Valida y convierte una copia de seguridad ya leída como mapa.
 * No toca la base de datos: quien llama solo debe borrar e insertar si el resultado es [BackupParseOutcome.Ok].
 * [nowMillis] se usa como fecha de los abonos que no la traen.
 */
fun parseBackup(root: Map<String, Any?>, nowMillis: Long = System.currentTimeMillis()): BackupParseOutcome {
    val app = root["app"]
    if (app != null && app != "CapyCoin") return BackupParseOutcome.Rejected(BackupRejection.WRONG_APP)

    if (root.containsKey("version")) {
        val version = backupLong(root["version"])
        if (version == null || version < 1 || version > BACKUP_FORMAT_VERSION) {
            return BackupParseOutcome.Rejected(BackupRejection.UNSUPPORTED_VERSION)
        }
    }

    val presentSections = BackupSections.ALL.filter { root[it] != null }
    if (presentSections.isEmpty()) return BackupParseOutcome.Rejected(BackupRejection.NOT_A_BACKUP)
    if (presentSections.any { root[it] !is List<*> }) return BackupParseOutcome.Rejected(BackupRejection.INVALID_STRUCTURE)

    val p = SectionParser(root)

    val incomes = p.parse(BackupSections.INCOMES, IncomeEntity::id) {
        IncomeEntity(
            id = id(),
            concept = textOr("concept", "Ingreso"),
            amountCents = cents("amount"),
            date = date("date"),
            isRecurring = boolean("isRecurring"),
            linkedExpenseIdsText = nullableText("linkedExpenseIdsText"),
            linkedCardPurchaseIdsText = nullableText("linkedCardPurchaseIdsText"),
            extraAmountCents = centsOr("extraAmount"),
            extraConcept = nullableText("extraConcept"),
            totalInstallments = intOr("totalInstallments", 1).coerceAtLeast(1),
            totalAmountCents = centsOr("totalAmount"),
            endDateMillis = nullableLong("endDateMillis")
        )
    }

    val expenses = p.parse(BackupSections.EXPENSES, ExpenseEntity::id) {
        ExpenseEntity(
            id = id(),
            concept = textOr("concept", "Gasto"),
            amountCents = cents("amount"),
            date = date("date"),
            isRecurring = boolean("isRecurring"),
            currency = textOr("currency", "COP"),
            endDateMillis = nullableLong("endDateMillis"),
            installments = intOr("installments", 0).coerceAtLeast(0)
        )
    }

    val cards = p.parse(BackupSections.CREDIT_CARDS, CreditCardEntity::id) {
        CreditCardEntity(
            id = id(),
            name = textOr("name", "Tarjeta"),
            cutoffDay = intOr("cutoffDay", 15),
            paymentDay = intOr("paymentDay", 0),
            interestRate = moneyOr("interestRate", 0.0),
            maintenanceFeeCents = centsOr("maintenanceFee"),
            currency = textOr("currency", "COP"),
            isFeeWaiverEnabled = boolean("isFeeWaiverEnabled"),
            minSpendForFeeWaiverCents = centsOr("minSpendForFeeWaiver"),
            isFirstInstallmentInterestFree = boolean("isFirstInstallmentInterestFree"),
            creditLimitCents = centsOr("creditLimit"),
            colorIndex = intOr("colorIndex", -1)
        )
    }

    val savings = p.parse(BackupSections.SAVINGS, SavingsEntity::id) {
        SavingsEntity(
            id = id(),
            concept = textOr("concept", "Ahorro"),
            amountCents = cents("amount"),
            date = date("date"),
            isRecurring = boolean("isRecurring"),
            endDateMillis = nullableLong("endDateMillis")
        )
    }

    val incomeIds = incomes.mapTo(hashSetOf()) { it.id }
    val expenseIds = expenses.mapTo(hashSetOf()) { it.id }
    val cardIds = cards.mapTo(hashSetOf()) { it.id }

    val purchases = p.keepLinked(
        BackupSections.CARD_PURCHASES,
        p.parse(BackupSections.CARD_PURCHASES, CardPurchaseEntity::id) {
            CardPurchaseEntity(
                id = id(),
                cardId = ref("cardId"),
                concept = textOr("concept", "Compra"),
                totalAmountCents = cents("totalAmount"),
                installments = intOr("installments", 1).coerceAtLeast(1),
                purchaseDate = date("purchaseDate"),
                isInterestFree = boolean("isInterestFree"),
                isRecurring = boolean("isRecurring"),
                currency = textOr("currency", "COP")
            )
        },
        cardIds
    ) { it.cardId }
    val purchaseIds = purchases.mapTo(hashSetOf()) { it.id }

    val expensePayments = p.keepLinked(
        BackupSections.EXPENSE_PAYMENTS,
        p.parse(BackupSections.EXPENSE_PAYMENTS, ExpensePaymentEntity::id) {
            ExpensePaymentEntity(
                id = id(),
                expenseId = ref("expenseId"),
                amountCents = cents("amount"),
                date = dateOr("date", nowMillis),
                yearMonthText = legacyYearMonth("yearMonthText")
            )
        },
        expenseIds
    ) { it.expenseId }

    val purchasePayments = p.keepLinked(
        BackupSections.CARD_PURCHASE_PAYMENTS,
        p.parse(BackupSections.CARD_PURCHASE_PAYMENTS, CardPurchasePaymentEntity::id) {
            CardPurchasePaymentEntity(
                id = id(),
                purchaseId = ref("purchaseId"),
                amountCents = cents("amount"),
                date = dateOr("date", nowMillis),
                yearMonthText = legacyYearMonth("yearMonthText")
            )
        },
        purchaseIds
    ) { it.purchaseId }

    val omitted = p.keepLinked(
        BackupSections.OMITTED_INSTALLMENTS,
        p.parse(BackupSections.OMITTED_INSTALLMENTS, OmittedInstallmentEntity::id) {
            OmittedInstallmentEntity(id = id(), purchaseId = ref("purchaseId"), yearMonthText = yearMonth("yearMonthText"))
        },
        purchaseIds
    ) { it.purchaseId }

    val settled = p.keepLinked(
        BackupSections.SETTLED_PURCHASES,
        p.parse(BackupSections.SETTLED_PURCHASES, SettledPurchaseEntity::id) {
            SettledPurchaseEntity(id = id(), purchaseId = ref("purchaseId"), yearMonthText = yearMonth("yearMonthText"))
        },
        purchaseIds
    ) { it.purchaseId }

    val paidCards = p.keepLinked(
        BackupSections.PAID_CREDIT_CARDS,
        p.parse(BackupSections.PAID_CREDIT_CARDS, PaidCreditCardEntity::id) {
            PaidCreditCardEntity(id = id(), cardId = ref("cardId"), yearMonthText = yearMonth("yearMonthText"))
        },
        cardIds
    ) { it.cardId }

    val receivedIncomes = p.keepLinked(
        BackupSections.RECEIVED_INCOMES,
        p.parse(BackupSections.RECEIVED_INCOMES, ReceivedIncomeEntity::id) {
            ReceivedIncomeEntity(id = id(), incomeId = ref("incomeId"), yearMonthText = yearMonth("yearMonthText"))
        },
        incomeIds
    ) { it.incomeId }

    // Las marcas por clave ("direct_1", "card_inst_2"…) se conservan tal cual: solo se exige clave y mes válidos
    val ignored = p.parse(BackupSections.IGNORED_EXPENSES, IgnoredExpenseEntity::id) {
        IgnoredExpenseEntity(id = id(), itemKey = nullableText("itemKey") ?: throw InvalidRecord(), yearMonthText = yearMonth("yearMonthText"))
    }
    val paidExpenses = p.parse(BackupSections.PAID_EXPENSES, PaidExpenseEntity::id) {
        PaidExpenseEntity(id = id(), itemKey = nullableText("itemKey") ?: throw InvalidRecord(), yearMonthText = yearMonth("yearMonthText"))
    }

    val data = BackupData(
        incomes = incomes,
        expenses = expenses,
        expensePayments = expensePayments,
        cards = cards,
        purchases = purchases,
        savings = savings,
        omittedInstallments = omitted,
        purchasePayments = purchasePayments,
        ignoredExpenses = ignored,
        paidCards = paidCards,
        paidExpenses = paidExpenses,
        settledPurchases = settled,
        receivedIncomes = receivedIncomes
    )
    if (data.totalRecords == 0) return BackupParseOutcome.Rejected(BackupRejection.EMPTY)
    return BackupParseOutcome.Ok(data, p.discarded, p.orphaned)
}
