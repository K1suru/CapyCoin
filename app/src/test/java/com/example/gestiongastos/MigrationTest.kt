package com.example.gestiongastos

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gestiongastos.data.local.AppDatabase
import com.example.gestiongastos.data.local.MIGRATION_23_24
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Migración 23 → 24 (montos de decimales a centavos) con Room y SQLite reales, a partir del esquema
 * exportado de la versión 23. Room valida al final que la base quede idéntica al esquema 24.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun amountsBecomeCentsAndNothingElseChanges() {
        helper.createDatabase("migracion", 23).use { db ->
            db.execSQL("INSERT INTO incomes (id, concept, amount, date, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis) VALUES (1, 'Reembolso', 250000.037687581, 1000, 0, NULL, '1,2', 175000.0, 'Extra', 1, 0.0, NULL)")
            db.execSQL("INSERT INTO expenses (id, concept, amount, date, isRecurring, currency, endDateMillis, installments) VALUES (7, 'Hosting', 33.336, 2000, 1, 'USD', NULL, 0)")
            db.execSQL("INSERT INTO credit_cards (id, name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpendForFeeWaiver, isFirstInstallmentInterestFree, creditLimit, colorIndex) VALUES (8, 'Visa', 20, 5, 2.16, 15900.0, 'COP', 1, 300000.0, 0, 5000000.0, 3)")
            db.execSQL("INSERT INTO card_purchases (id, cardId, concept, totalAmount, installments, purchaseDate, isInterestFree, isRecurring, currency) VALUES (284, 8, 'Oxxo', 15000.0, 1, 3000, 0, 0, 'COP')")
            db.execSQL("INSERT INTO card_purchases (id, cardId, concept, totalAmount, installments, purchaseDate, isInterestFree, isRecurring, currency) VALUES (285, 8, 'Curso', 100.01, 3, 3000, 1, 0, 'USD')")
            db.execSQL("INSERT INTO savings (id, concept, amount, date, isRecurring, endDateMillis) VALUES (2, 'Viaje', 200000.5, 4000, 1, 5000)")
            db.execSQL("INSERT INTO card_purchase_payments (id, purchaseId, amount, date, yearMonthText) VALUES (3, 285, 33.34, 6000, '2026-11')")
            db.execSQL("INSERT INTO expense_payments (id, expenseId, amount, date, yearMonthText) VALUES (4, 7, 0.1, 7000, '2026-11')")
            db.execSQL("INSERT INTO paid_expenses (id, itemKey, yearMonthText) VALUES (1, 'card_inst_284', '2026-11')")
            // Se habían borrado compras: el contador de ids va más adelante que el mayor id
            db.execSQL("UPDATE sqlite_sequence SET seq = 900 WHERE name = 'card_purchases'")
        }

        // validateDroppedTables = true: Room comprueba el esquema completo contra 24.json
        val db = helper.runMigrationsAndValidate("migracion", 24, true, MIGRATION_23_24)

        fun row(sql: String): List<Any?> = db.query(sql).use { c ->
            assertEquals("una fila para: $sql", 1, c.count)
            c.moveToFirst()
            List(c.columnCount) { i ->
                when (c.getType(i)) {
                    android.database.Cursor.FIELD_TYPE_NULL -> null
                    android.database.Cursor.FIELD_TYPE_INTEGER -> c.getLong(i)
                    android.database.Cursor.FIELD_TYPE_FLOAT -> c.getDouble(i)
                    else -> c.getString(i)
                }
            }
        }

        assertEquals(
            listOf<Any?>(1L, "Reembolso", 25_000_004L, 1000L, 0L, null, "1,2", 17_500_000L, "Extra", 1L, 0L, null),
            row("SELECT id, concept, amountCents, date, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmountCents, extraConcept, totalInstallments, totalAmountCents, endDateMillis FROM incomes")
        )
        assertEquals(listOf<Any?>(7L, "Hosting", 3334L, 2000L, 1L, "USD", null, 0L),
            row("SELECT id, concept, amountCents, date, isRecurring, currency, endDateMillis, installments FROM expenses"))
        // La tasa de interés sigue con decimales; los montos de la tarjeta pasan a centavos
        assertEquals(listOf<Any?>(8L, "Visa", 20L, 5L, 2.16, 1_590_000L, "COP", 1L, 30_000_000L, 0L, 500_000_000L, 3L),
            row("SELECT id, name, cutoffDay, paymentDay, interestRate, maintenanceFeeCents, currency, isFeeWaiverEnabled, minSpendForFeeWaiverCents, isFirstInstallmentInterestFree, creditLimitCents, colorIndex FROM credit_cards"))
        assertEquals(listOf<Any?>(284L, 8L, "Oxxo", 1_500_000L, 1L, 3000L, 0L, 0L, "COP"),
            row("SELECT id, cardId, concept, totalAmountCents, installments, purchaseDate, isInterestFree, isRecurring, currency FROM card_purchases WHERE id = 284"))
        assertEquals(listOf<Any?>(10_001L, 3L, 1L, "USD"),
            row("SELECT totalAmountCents, installments, isInterestFree, currency FROM card_purchases WHERE id = 285"))
        assertEquals(listOf<Any?>(2L, "Viaje", 20_000_050L, 4000L, 1L, 5000L),
            row("SELECT id, concept, amountCents, date, isRecurring, endDateMillis FROM savings"))
        assertEquals(listOf<Any?>(3L, 285L, 3334L, 6000L, "2026-11"),
            row("SELECT id, purchaseId, amountCents, date, yearMonthText FROM card_purchase_payments"))
        assertEquals(listOf<Any?>(4L, 7L, 10L, 7000L, "2026-11"),
            row("SELECT id, expenseId, amountCents, date, yearMonthText FROM expense_payments"))
        // Las tablas sin montos no se tocan
        assertEquals(listOf<Any?>(1L, "card_inst_284", "2026-11"), row("SELECT id, itemKey, yearMonthText FROM paid_expenses"))

        // El contador de ids se conserva: una compra nueva no reutiliza el id de una borrada
        assertEquals(listOf<Any?>(900L), row("SELECT seq FROM sqlite_sequence WHERE name = 'card_purchases'"))
        db.execSQL("INSERT INTO card_purchases (cardId, concept, totalAmountCents, installments, purchaseDate, isInterestFree, isRecurring, currency) VALUES (8, 'Nueva', 100, 1, 1, 0, 0, 'COP')")
        assertEquals(listOf<Any?>(901L), row("SELECT id FROM card_purchases WHERE concept = 'Nueva'"))
        // No queda ninguna tabla temporal
        assertEquals(listOf<Any?>(0L), row("SELECT count(*) FROM sqlite_master WHERE name LIKE '%_new'"))
        db.close()
    }
}
