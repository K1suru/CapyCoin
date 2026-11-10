package com.example.gestiongastos

import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.copyDatabaseFiles
import com.example.gestiongastos.data.model.MAX_AMOUNT_CENTS
import com.example.gestiongastos.data.model.centsToAmount
import com.example.gestiongastos.data.model.floorToCents
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.data.model.roundToCents
import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.model.toCentsOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** El dinero se guarda en centavos enteros. */
class MoneyCentsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun amountsAreRoundedToTheNearestCent() {
        assertEquals(150_000_00L, toCents(150_000.0))
        assertEquals(3334L, toCents(33.34))
        assertEquals(3334L, toCents(33.336))
        assertEquals(1L, toCents(0.005))   // la mitad sube
        assertEquals(0L, toCents(0.004))
        assertEquals(10001L, toCents(100.01))
        // 0.1 + 0.2 no es 0.3 en Double, pero en centavos sí
        assertEquals(30L, toCents(0.1 + 0.2))
        // Un monto con más de dos decimales, como los que deja un cálculo
        assertEquals(25_000_004L, toCents(250000.037687581))
        assertEquals(MAX_AMOUNT_CENTS, toCents(9_999_999_999_999.0))
    }

    @Test
    fun centsRoundTripIsExact() {
        listOf(0L, 1L, 99L, 3334L, 10001L, 25_000_004L, MAX_AMOUNT_CENTS).forEach {
            assertEquals(it, toCents(centsToAmount(it)))
        }
        assertEquals(33.34, centsToAmount(3334L), 0.0)
        // La entidad expone el monto con decimales solo para leer
        assertEquals(12.5, ExpenseEntity(concept = "x", amountCents = 1250, date = 0).amount, 0.0)
    }

    @Test
    fun invalidAmountsAreNeverStored() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e20, -1e20).forEach {
            assertNull("toCentsOrNull($it)", toCentsOrNull(it))
            try {
                toCents(it)
                throw AssertionError("toCents($it) debía fallar")
            } catch (e: IllegalArgumentException) {
                // esperado
            }
        }
    }

    @Test
    fun databaseFilesAreCopiedOnceBeforeMigrating() {
        val dbDir = tempFolder.newFolder("databases")
        val db = File(dbDir, "gestion_gastos_db").apply { writeText("datos v23") }
        File(dbDir, "gestion_gastos_db-wal").writeText("wal")
        val backupDir = File(tempFolder.root, "antes_de_centavos")

        assertTrue(copyDatabaseFiles(db, backupDir))
        assertEquals("datos v23", File(backupDir, "gestion_gastos_db").readText())
        assertEquals("wal", File(backupDir, "gestion_gastos_db-wal").readText())

        // Una segunda vez (ya migrada, con otro contenido) no pisa la copia original
        db.writeText("datos v24")
        assertTrue(copyDatabaseFiles(db, backupDir))
        assertEquals("datos v23", File(backupDir, "gestion_gastos_db").readText())

        // Sin base no hay nada que copiar
        assertFalse(copyDatabaseFiles(File(dbDir, "no_existe"), File(tempFolder.root, "otra")))
    }

    @Test
    fun installmentPlanIsInWholeCentsAndTheLastOneAbsorbsTheRemainder() {
        // Sin interés: 100 en 3 cuotas = 33,33 + 33,33 + 33,34 (antes 33,3333… cada una)
        val free = installmentSchedule(100.0, 3, 0.0, isInterestFree = true, isFirstInstallmentInterestFree = false)
        assertEquals(listOf(3333L, 3333L, 3334L), free.map { toCents(it) })
        free.forEach { assertEquals(it, roundToCents(it), 0.0) }

        // Con interés: todas las cuotas en centavos exactos, iguales salvo la última, y el plan deja el saldo en 0
        val rate = 0.0216
        val plan = installmentSchedule(1_000_000.0, 12, 2.16, isInterestFree = false, isFirstInstallmentInterestFree = false)
        plan.forEach { assertEquals("cuota en centavos exactos", it, roundToCents(it), 0.0) }
        // El plan se recalcula cada mes (por los abonos), así que una cuota puede variar un centavo respecto a otra
        val regular = plan.dropLast(1).map { toCents(it) }
        assertTrue("cuotas iguales salvo un centavo: $regular", regular.max() - regular.min() <= 1)
        assertTrue("la última absorbe el residuo", kotlin.math.abs(plan.last() - plan.first()) < 0.20)
        var balance = 1_000_000.0
        plan.forEach { balance = balance * (1 + rate) - it }
        assertEquals(0.0, balance, 0.006)

        // Una sola cuota es el total exacto
        assertEquals(listOf(10001L), installmentSchedule(100.01, 1, 2.0, false, false).map { toCents(it) })
        assertEquals(33.33, floorToCents(33.33), 0.0)   // sin el margen bajaría a 33,32
        assertEquals(33.33, floorToCents(33.339), 0.0)
    }
}
