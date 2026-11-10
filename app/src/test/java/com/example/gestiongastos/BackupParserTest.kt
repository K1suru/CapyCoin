package com.example.gestiongastos

import com.example.gestiongastos.data.backup.BACKUP_FORMAT_VERSION
import com.example.gestiongastos.data.backup.BackupParseOutcome
import com.example.gestiongastos.data.backup.BackupRejection
import com.example.gestiongastos.data.backup.BackupSections
import com.example.gestiongastos.data.backup.BackupTooLargeException
import com.example.gestiongastos.data.backup.MAX_BACKUP_BYTES
import com.example.gestiongastos.data.backup.parseBackup
import com.example.gestiongastos.data.backup.readBackupText
import com.example.gestiongastos.data.backup.writeBackupFileSafely
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Validación de la copia de seguridad antes de restaurarla (sin tocar ninguna base de datos). */
class BackupParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val date = 1_780_000_000_000L

    private fun income(id: Long = 1, amount: Any? = 3_500_000.0) =
        mapOf("id" to id, "concept" to "Sueldo", "amount" to amount, "date" to date, "isRecurring" to true)

    private fun expense(id: Long = 1, amount: Any? = 1_200_000) =
        mapOf("id" to id, "concept" to "Arriendo", "amount" to amount, "date" to date, "currency" to "COP")

    private fun card(id: Long = 1, extra: Map<String, Any?> = emptyMap()) =
        mapOf<String, Any?>("id" to id, "name" to "Visa", "cutoffDay" to 15, "interestRate" to 2.2) + extra

    private fun purchase(id: Long = 1, cardId: Long = 1, total: Any? = 600_000.0) =
        mapOf("id" to id, "cardId" to cardId, "concept" to "TV", "totalAmount" to total, "installments" to 6, "purchaseDate" to date)

    private fun backup(vararg sections: Pair<String, Any?>): Map<String, Any?> =
        mapOf<String, Any?>("app" to "CapyCoin", "version" to BACKUP_FORMAT_VERSION) + sections

    private fun ok(root: Map<String, Any?>): BackupParseOutcome.Ok {
        val outcome = parseBackup(root, nowMillis = date)
        assertTrue("Se esperaba una copia válida y fue $outcome", outcome is BackupParseOutcome.Ok)
        return outcome as BackupParseOutcome.Ok
    }

    private fun rejection(root: Map<String, Any?>): BackupRejection {
        val outcome = parseBackup(root, nowMillis = date)
        assertTrue("Se esperaba un rechazo y fue $outcome", outcome is BackupParseOutcome.Rejected)
        return (outcome as BackupParseOutcome.Rejected).reason
    }

    @Test
    fun validBackupIsReadCompletely() {
        val result = ok(backup(
            BackupSections.INCOMES to listOf(income()),
            BackupSections.EXPENSES to listOf(expense()),
            BackupSections.CREDIT_CARDS to listOf(card()),
            BackupSections.CARD_PURCHASES to listOf(purchase()),
            BackupSections.SAVINGS to listOf(mapOf("id" to 1, "concept" to "Viaje", "amount" to 200_000, "date" to date, "endDateMillis" to null)),
            BackupSections.EXPENSE_PAYMENTS to listOf(mapOf("id" to 1, "expenseId" to 1, "amount" to 50_000, "date" to date, "yearMonthText" to "2026-11")),
            BackupSections.CARD_PURCHASE_PAYMENTS to listOf(mapOf("id" to 1, "purchaseId" to 1, "amount" to 100_000.0, "date" to date, "yearMonthText" to "")),
            BackupSections.OMITTED_INSTALLMENTS to listOf(mapOf("id" to 1, "purchaseId" to 1, "yearMonthText" to "2026-08")),
            BackupSections.SETTLED_PURCHASES to listOf(mapOf("id" to 1, "purchaseId" to 1, "yearMonthText" to "2026-09")),
            BackupSections.PAID_CREDIT_CARDS to listOf(mapOf("id" to 1, "cardId" to 1, "yearMonthText" to "2026-08")),
            BackupSections.RECEIVED_INCOMES to listOf(mapOf("id" to 1, "incomeId" to 1, "yearMonthText" to "2026-08")),
            BackupSections.IGNORED_EXPENSES to listOf(mapOf("id" to 1, "itemKey" to "direct_1", "yearMonthText" to "2026-08")),
            BackupSections.PAID_EXPENSES to listOf(mapOf("id" to 1, "itemKey" to "card_inst_1", "yearMonthText" to "2026-08"))
        ))

        assertEquals(13, result.data.totalRecords)
        assertEquals(0, result.discardedTotal)
        assertEquals(0, result.orphanedTotal)
        assertEquals(3_500_000.0, result.data.incomes.single().amount, 0.0)
        assertEquals(1_200_000.0, result.data.expenses.single().amount, 0.0)
        assertEquals(2.2, result.data.cards.single().interestRate, 0.0)
        assertEquals(600_000.0, result.data.purchases.single().totalAmount, 0.0)
        assertNull(result.data.savings.single().endDateMillis)
        // Un abono antiguo sin mes se conserva tal cual (aplica desde el inicio)
        assertEquals("", result.data.purchasePayments.single().yearMonthText)
    }

    @Test
    fun rejectsFilesThatAreNotCapyCoinBackups() {
        assertEquals(BackupRejection.NOT_A_BACKUP, rejection(mapOf("foo" to "bar")))
        assertEquals(BackupRejection.WRONG_APP, rejection(mapOf("app" to "OtraApp", "version" to 1, BackupSections.INCOMES to listOf(income()))))
        assertEquals(BackupRejection.INVALID_STRUCTURE, rejection(backup(BackupSections.INCOMES to "no soy una lista")))
    }

    @Test
    fun headerAloneIsNotEnough() {
        // Antes bastaba con tener "version": se aceptaba, se borraba todo y no se insertaba nada
        assertEquals(BackupRejection.NOT_A_BACKUP, rejection(mapOf("app" to "CapyCoin", "version" to 1)))
    }

    @Test
    fun emptyBackupDoesNotWipeCurrentData() {
        assertEquals(BackupRejection.EMPTY, rejection(backup(BackupSections.INCOMES to emptyList<Any>(), BackupSections.EXPENSES to emptyList<Any>())))
        // Tampoco si todo lo que trae es inválido
        assertEquals(BackupRejection.EMPTY, rejection(backup(BackupSections.INCOMES to listOf(income(amount = "abc"), "basura", null))))
    }

    @Test
    fun rejectsNewerOrInvalidVersions() {
        val data = BackupSections.INCOMES to listOf(income())
        assertEquals(BackupRejection.UNSUPPORTED_VERSION, rejection(mapOf("version" to BACKUP_FORMAT_VERSION + 1, data)))
        assertEquals(BackupRejection.UNSUPPORTED_VERSION, rejection(mapOf("version" to "abc", data)))
        assertEquals(BackupRejection.UNSUPPORTED_VERSION, rejection(mapOf("version" to 0, data)))
        assertEquals(BackupRejection.UNSUPPORTED_VERSION, rejection(mapOf("version" to null, data)))
        // Las copias antiguas sin cabecera siguen sirviendo
        assertEquals(1, ok(mapOf(data)).data.incomes.size)
    }

    @Test
    fun invalidAmountDiscardsTheRecordInsteadOfBecomingZero() {
        // Antes optDouble(..., 0.0) guardaba estos registros con monto 0 (o NaN) sin avisar
        val result = ok(backup(BackupSections.INCOMES to listOf(
            income(id = 1),
            income(id = 2, amount = "abc"),
            income(id = 3, amount = null),
            income(id = 4, amount = Double.NaN),
            income(id = 5, amount = "Infinity"),
            income(id = 6, amount = -100.0),
            mapOf("id" to 7, "concept" to "Sin monto", "date" to date),
            income(id = 8, amount = "2500000.5")
        )))

        assertEquals(listOf(1L, 8L), result.data.incomes.map { it.id })
        assertEquals(2_500_000.5, result.data.incomes.last().amount, 0.0)
        assertTrue(result.data.incomes.none { it.amount == 0.0 || it.amount.isNaN() })
        assertEquals(mapOf(BackupSections.INCOMES to 6), result.discarded)
    }

    @Test
    fun malformedRecordsAreCountedNotSilentlySkipped() {
        val result = ok(backup(
            BackupSections.EXPENSES to listOf(
                expense(id = 1),
                "no soy un objeto",
                null,
                expense(id = 1),                                              // id repetido: reemplazaría al primero
                mapOf("id" to 3, "concept" to "Sin fecha", "amount" to 10),   // sin fecha no se sabe a qué mes va
                mapOf("id" to "x", "concept" to "Id malo", "amount" to 10, "date" to date)
            ),
            BackupSections.PAID_EXPENSES to listOf(
                mapOf("itemKey" to "direct_1", "yearMonthText" to "2026-11"),
                mapOf("itemKey" to "direct_1", "yearMonthText" to "noviembre"),
                mapOf("itemKey" to "", "yearMonthText" to "2026-11")
            )
        ))

        assertEquals(1, result.data.expenses.size)
        assertEquals(1, result.data.paidExpenses.size)
        assertEquals(mapOf(BackupSections.EXPENSES to 5, BackupSections.PAID_EXPENSES to 2), result.discarded)
    }

    @Test
    fun recordsWhoseParentIsMissingAreReportedAsOrphans() {
        // La tarjeta 2 trae un cupo inválido y se descarta: sus compras y abonos no se verían en la app
        val result = ok(backup(
            BackupSections.CREDIT_CARDS to listOf(card(1), card(2, mapOf("creditLimit" to "NaN"))),
            BackupSections.CARD_PURCHASES to listOf(purchase(id = 10, cardId = 1), purchase(id = 20, cardId = 2), purchase(id = 30, cardId = 99)),
            BackupSections.CARD_PURCHASE_PAYMENTS to listOf(
                mapOf("purchaseId" to 10, "amount" to 1000, "yearMonthText" to "2026-11"),
                mapOf("purchaseId" to 20, "amount" to 1000, "yearMonthText" to "2026-11")
            ),
            BackupSections.RECEIVED_INCOMES to listOf(mapOf("incomeId" to 5, "yearMonthText" to "2026-11"))
        ))

        assertEquals(listOf(1L), result.data.cards.map { it.id })
        assertEquals(listOf(10L), result.data.purchases.map { it.id })
        assertEquals(listOf(10L), result.data.purchasePayments.map { it.purchaseId })
        assertEquals(date, result.data.purchasePayments.single().date) // sin fecha: se usa la de la restauración
        assertEquals(mapOf(BackupSections.CREDIT_CARDS to 1), result.discarded)
        assertEquals(
            mapOf(BackupSections.CARD_PURCHASES to 2, BackupSections.CARD_PURCHASE_PAYMENTS to 1, BackupSections.RECEIVED_INCOMES to 1),
            result.orphaned
        )
    }

    @Test
    fun optionalFieldsKeepTheirDefaults() {
        val result = ok(backup(
            BackupSections.INCOMES to listOf(mapOf("amount" to 1000, "date" to date)),
            BackupSections.CREDIT_CARDS to listOf(mapOf("id" to 1))
        ))
        val income = result.data.incomes.single()
        assertEquals(0L, income.id)
        assertEquals("Ingreso", income.concept)
        assertEquals(1, income.totalInstallments)
        assertFalse(income.isRecurring)
        val card = result.data.cards.single()
        assertEquals("Tarjeta", card.name)
        assertEquals(15, card.cutoffDay)
        assertEquals(0.0, card.creditLimit, 0.0)
        assertEquals(-1, card.colorIndex)
    }

    // ---- Archivo de la copia ----

    @Test
    fun newBackupReplacesOldOnesOnlyAfterItIsWritten() {
        val dir = tempFolder.newFolder("backups")
        File(dir, "CapyCoin_Backup_viejo.json").writeText("viejo")

        val written = writeBackupFileSafely(dir, "CapyCoin_Backup_nuevo.json", "{\"version\":1}")

        assertEquals("{\"version\":1}", written.readText())
        assertEquals(listOf("CapyCoin_Backup_nuevo.json"), dir.list()!!.toList())
    }

    @Test
    fun failedWriteKeepsThePreviousBackup() {
        val dir = tempFolder.newFolder("backups")
        val old = File(dir, "CapyCoin_Backup_viejo.json").apply { writeText("viejo") }
        // Sin permiso de escritura en la carpeta, crear la copia nueva falla
        assumeTrue(dir.setWritable(false))
        assumeTrue(!File(dir, "prueba").let { runCatching { it.createNewFile() }.getOrDefault(false) })
        try {
            writeBackupFileSafely(dir, "CapyCoin_Backup_nuevo.json", "nuevo")
            fail("La escritura debía fallar")
        } catch (e: java.io.IOException) {
            // esperado
        } finally {
            dir.setWritable(true)
        }
        // Antes se borraban todas las copias ANTES de escribir: aquí la vieja ya no existiría
        assertEquals("viejo", old.readText())
        assertEquals(listOf("CapyCoin_Backup_viejo.json"), dir.list()!!.toList())
    }

    @Test
    fun version2BackupStoresCentsAndVersion1IsStillRead() {
        // v2: centavos enteros en "<campo>Cents"
        val v2 = ok(mapOf(
            "app" to "CapyCoin", "version" to 2,
            BackupSections.INCOMES to listOf(mapOf("id" to 1, "concept" to "Sueldo", "amountCents" to 350_000_000L, "extraAmountCents" to 1050, "date" to date)),
            BackupSections.EXPENSES to listOf(mapOf("id" to 1, "concept" to "Hosting", "amountCents" to 3334, "date" to date, "currency" to "USD"))
        ))
        assertEquals(350_000_000L, v2.data.incomes.single().amountCents)
        assertEquals(1050L, v2.data.incomes.single().extraAmountCents)
        assertEquals(0L, v2.data.incomes.single().totalAmountCents)
        assertEquals(3334L, v2.data.expenses.single().amountCents)

        // v1: decimales en "<campo>", se redondean al centavo
        val v1 = ok(mapOf(
            "app" to "CapyCoin", "version" to 1,
            BackupSections.EXPENSES to listOf(mapOf("id" to 1, "concept" to "Hosting", "amount" to 33.336, "date" to date, "currency" to "USD")),
            BackupSections.INCOMES to listOf(income(amount = 250000.037687581))
        ))
        assertEquals(3334L, v1.data.expenses.single().amountCents)
        assertEquals(25_000_004L, v1.data.incomes.single().amountCents)
        assertEquals(2, BACKUP_FORMAT_VERSION)
    }

    @Test
    fun invalidCentsDiscardTheRecordInsteadOfGuessing() {
        listOf<Any?>(-5, 12.5, "abc", Double.NaN, Long.MAX_VALUE).forEach { bad ->
            val root = mapOf(
                "app" to "CapyCoin", "version" to 2,
                BackupSections.EXPENSES to listOf(
                    mapOf("id" to 1, "concept" to "Bueno", "amountCents" to 100, "date" to date),
                    mapOf("id" to 2, "concept" to "Malo", "amountCents" to bad, "date" to date)
                )
            )
            val result = ok(root)
            assertEquals("amountCents=$bad", listOf(1L), result.data.expenses.map { it.id })
            assertEquals(1, result.discardedTotal)
        }
        // Un monto v1 que no cabe tampoco se guarda
        assertEquals(BackupRejection.EMPTY, rejection(backup(BackupSections.EXPENSES to listOf(expense(amount = 1e20)))))
    }

    @Test
    fun backupLargerThanTheLimitIsRejectedWithoutReadingItAll() {
        // Un flujo "infinito": antes se leía entero con readText() hasta agotar la memoria
        var served = 0L
        val endless = object : java.io.InputStream() {
            override fun read(): Int { served++; return 'a'.code }
        }
        try {
            readBackupText(endless, maxBytes = 1000)
            fail("Debía rechazarse por tamaño")
        } catch (e: BackupTooLargeException) {
            // esperado
        }
        assertTrue("Solo debe leer un poco más del límite, leyó $served", served <= 1000 + 64 * 1024)
    }

    @Test
    fun backupWithinTheLimitIsReadCompleteAsUtf8() {
        val text = "{\"concepto\":\"Café ñandú €\"}"
        val bytes = text.toByteArray(Charsets.UTF_8)
        assertEquals(text, readBackupText(bytes.inputStream(), maxBytes = bytes.size.toLong())) // justo en el límite
        assertEquals("", readBackupText(ByteArray(0).inputStream()))
        try {
            readBackupText(bytes.inputStream(), maxBytes = bytes.size - 1L)
            fail("Un byte por encima del límite debe rechazarse")
        } catch (e: BackupTooLargeException) {
            // esperado
        }
        assertEquals(50L * 1024 * 1024, MAX_BACKUP_BYTES)
    }
}
