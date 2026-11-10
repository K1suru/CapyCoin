package com.example.gestiongastos.data.local

// Los montos se guardan en centavos enteros (columnas ...Cents). Cada entidad expone además el monto
// con decimales (amount, totalAmount...) solo para leer; para crear o copiar se usan los centavos (toCents).

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.gestiongastos.data.model.centsToAmount

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concept: String,
    val amountCents: Long,
    val date: Long, // timestamp in millis
    val isRecurring: Boolean = false,
    val linkedExpenseIdsText: String? = null,
    val linkedCardPurchaseIdsText: String? = null,
    val extraAmountCents: Long = 0,
    val extraConcept: String? = null,
    val totalInstallments: Int = 1,
    val totalAmountCents: Long = 0,
    val endDateMillis: Long? = null
) {
    @get:Ignore val amount: Double get() = centsToAmount(amountCents)
    @get:Ignore val extraAmount: Double get() = centsToAmount(extraAmountCents)
    @get:Ignore val totalAmount: Double get() = centsToAmount(totalAmountCents)
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concept: String,
    val amountCents: Long,
    val date: Long, // timestamp in millis
    val isRecurring: Boolean = false,
    val currency: String = "COP", // "COP" or "USD"
    val endDateMillis: Long? = null, // Último mes en que aplica un gasto recurrente (null = sin fin)
    val installments: Int = 0 // Gasto en cuotas (p. ej. una deuda con una persona): 0 = no es en cuotas
) {
    @get:Ignore val amount: Double get() = centsToAmount(amountCents)
}

/** Abono a un gasto fijo en cuotas: baja el saldo pendiente sin cambiar el valor de la cuota. */
@Entity(tableName = "expense_payments")
data class ExpensePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseId: Long,
    val amountCents: Long,
    val date: Long, // timestamp in millis
    val yearMonthText: String // e.g. "2026-11"
) {
    @get:Ignore val amount: Double get() = centsToAmount(amountCents)
}

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cutoffDay: Int = 15,
    val paymentDay: Int = 0, // Día límite de pago (1-31 o 0 si no definido)
    val interestRate: Double = 0.0,
    val maintenanceFeeCents: Long = 0,
    val currency: String = "COP", // Legacy field retained for database stability
    val isFeeWaiverEnabled: Boolean = false,
    val minSpendForFeeWaiverCents: Long = 0,
    val isFirstInstallmentInterestFree: Boolean = false,
    val creditLimitCents: Long = 0, // Cupo total (0 = no definido)
    val colorIndex: Int = -1 // Color elegido de CardPalettes (-1 = automático)
) {
    @get:Ignore val maintenanceFee: Double get() = centsToAmount(maintenanceFeeCents)
    @get:Ignore val minSpendForFeeWaiver: Double get() = centsToAmount(minSpendForFeeWaiverCents)
    @get:Ignore val creditLimit: Double get() = centsToAmount(creditLimitCents)
}

@Entity(tableName = "card_purchases")
data class CardPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val concept: String,
    val totalAmountCents: Long,
    val installments: Int, // e.g. 1, 3, 12
    val purchaseDate: Long, // timestamp in millis
    val isInterestFree: Boolean = false,
    val isRecurring: Boolean = false,
    val currency: String = "COP" // "COP" or "USD"
) {
    @get:Ignore val totalAmount: Double get() = centsToAmount(totalAmountCents)
}

@Entity(tableName = "savings")
data class SavingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concept: String,
    val amountCents: Long,
    val date: Long, // timestamp in millis
    val isRecurring: Boolean = false,
    val endDateMillis: Long? = null // Último mes en que aplica un ahorro recurrente (null = sin fin)
) {
    @get:Ignore val amount: Double get() = centsToAmount(amountCents)
}

@Entity(tableName = "omitted_installments")
data class OmittedInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val yearMonthText: String // e.g. "2026-08"
)

@Entity(tableName = "card_purchase_payments")
data class CardPurchasePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val amountCents: Long,
    val date: Long, // timestamp in millis
    val yearMonthText: String // e.g. "2026-08"
) {
    @get:Ignore val amount: Double get() = centsToAmount(amountCents)
}

@Entity(tableName = "ignored_expenses")
data class IgnoredExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemKey: String, // e.g. "direct_1", "card_inst_2", "card_fee_3", "saving_4"
    val yearMonthText: String // e.g. "2026-08"
)

@Entity(tableName = "paid_credit_cards")
data class PaidCreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val yearMonthText: String // e.g. "2026-08"
)

@Entity(tableName = "paid_expenses")
data class PaidExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemKey: String, // e.g. "direct_1", "card_inst_2", "card_fee_3", "saving_4"
    val yearMonthText: String // e.g. "2026-08"
)

@Entity(tableName = "settled_purchases")
data class SettledPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val yearMonthText: String // e.g. "2026-08"
)

@Entity(tableName = "received_incomes")
data class ReceivedIncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val incomeId: Long,
    val yearMonthText: String // e.g. "2026-09"
)

