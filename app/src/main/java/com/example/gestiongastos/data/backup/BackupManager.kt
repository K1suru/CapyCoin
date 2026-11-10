package com.example.gestiongastos.data.backup

import com.example.gestiongastos.i18n.tr
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.gestiongastos.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    suspend fun generateBackupJson(db: AppDatabase): String = withContext(Dispatchers.IO) {
        val incomes = db.incomeDao().getAllIncomes().first()
        val expenses = db.expenseDao().getAllExpenses().first()
        val expensePayments = db.expensePaymentDao().getAllExpensePayments().first()
        val cards = db.creditCardDao().getAllCreditCards().first()
        val purchases = db.cardPurchaseDao().getAllCardPurchases().first()
        val savings = db.savingsDao().getAllSavings().first()
        val omittedInstallments = db.omittedInstallmentDao().getAllOmittedInstallments().first()
        val payments = db.cardPurchasePaymentDao().getAllCardPurchasePayments().first()
        val ignoredExpenses = db.ignoredExpenseDao().getAllIgnoredExpenses().first()
        val paidCards = db.paidCreditCardDao().getAllPaidCreditCards().first()
        val paidExpenses = db.paidExpenseDao().getAllPaidExpenses().first()
        val settledPurchases = db.settledPurchaseDao().getAllSettledPurchases().first()
        val receivedIncomes = db.receivedIncomeDao().getAllReceivedIncomes().first()

        val root = JSONObject().apply {
            put("app", "CapyCoin")
            put("version", BACKUP_FORMAT_VERSION)
            put("exportTimestamp", System.currentTimeMillis())
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Incomes
            put("incomes", JSONArray().apply {
                incomes.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("concept", it.concept)
                        put("amountCents", it.amountCents)
                        put("date", it.date)
                        put("isRecurring", it.isRecurring)
                        put("linkedExpenseIdsText", it.linkedExpenseIdsText ?: JSONObject.NULL)
                        put("linkedCardPurchaseIdsText", it.linkedCardPurchaseIdsText ?: JSONObject.NULL)
                        put("extraAmountCents", it.extraAmountCents)
                        put("extraConcept", it.extraConcept ?: JSONObject.NULL)
                        put("totalInstallments", it.totalInstallments)
                        put("totalAmountCents", it.totalAmountCents)
                        put("endDateMillis", it.endDateMillis ?: JSONObject.NULL)
                    })
                }
            })

            // Expenses
            put("expenses", JSONArray().apply {
                expenses.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("concept", it.concept)
                        put("amountCents", it.amountCents)
                        put("date", it.date)
                        put("isRecurring", it.isRecurring)
                        put("currency", it.currency)
                        put("endDateMillis", it.endDateMillis ?: JSONObject.NULL)
                        put("installments", it.installments)
                    })
                }
            })

            // Abonos a gastos fijos en cuotas
            put("expense_payments", JSONArray().apply {
                expensePayments.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("expenseId", it.expenseId)
                        put("amountCents", it.amountCents)
                        put("date", it.date)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Credit Cards
            put("credit_cards", JSONArray().apply {
                cards.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("name", it.name)
                        put("cutoffDay", it.cutoffDay)
                        put("paymentDay", it.paymentDay)
                        put("interestRate", it.interestRate)
                        put("maintenanceFeeCents", it.maintenanceFeeCents)
                        put("currency", it.currency)
                        put("isFeeWaiverEnabled", it.isFeeWaiverEnabled)
                        put("minSpendForFeeWaiverCents", it.minSpendForFeeWaiverCents)
                        put("isFirstInstallmentInterestFree", it.isFirstInstallmentInterestFree)
                        put("creditLimitCents", it.creditLimitCents)
                        put("colorIndex", it.colorIndex)
                    })
                }
            })

            // Card Purchases
            put("card_purchases", JSONArray().apply {
                purchases.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("cardId", it.cardId)
                        put("concept", it.concept)
                        put("totalAmountCents", it.totalAmountCents)
                        put("installments", it.installments)
                        put("purchaseDate", it.purchaseDate)
                        put("isInterestFree", it.isInterestFree)
                        put("isRecurring", it.isRecurring)
                        put("currency", it.currency)
                    })
                }
            })

            // Savings
            put("savings", JSONArray().apply {
                savings.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("concept", it.concept)
                        put("amountCents", it.amountCents)
                        put("date", it.date)
                        put("isRecurring", it.isRecurring)
                        put("endDateMillis", it.endDateMillis ?: JSONObject.NULL)
                    })
                }
            })

            // Omitted Installments
            put("omitted_installments", JSONArray().apply {
                omittedInstallments.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("purchaseId", it.purchaseId)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Payments
            put("card_purchase_payments", JSONArray().apply {
                payments.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("purchaseId", it.purchaseId)
                        put("amountCents", it.amountCents)
                        put("date", it.date)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Ignored Expenses
            put("ignored_expenses", JSONArray().apply {
                ignoredExpenses.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("itemKey", it.itemKey)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Paid Cards
            put("paid_credit_cards", JSONArray().apply {
                paidCards.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("cardId", it.cardId)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Paid Expenses
            put("paid_expenses", JSONArray().apply {
                paidExpenses.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("itemKey", it.itemKey)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Settled Purchases (Compras Liquidadas)
            put("settled_purchases", JSONArray().apply {
                settledPurchases.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("purchaseId", it.purchaseId)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })

            // Received Incomes (Ingresos Recibidos / Ingresados)
            put("received_incomes", JSONArray().apply {
                receivedIncomes.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("incomeId", it.incomeId)
                        put("yearMonthText", it.yearMonthText)
                    })
                }
            })
        }

        root.toString(2)
    }

    /**
     * Escribe la copia en la caché privada (carpeta "backups", la única que expone el FileProvider
     * para esto) y abre el menú de compartir para que el usuario elija dónde guardarla.
     */
    suspend fun exportBackup(
        context: Context,
        db: AppDatabase
    ): Result<String> {
        return try {
            val (fileName, timeStamp, cacheFile, counts) = withContext(Dispatchers.IO) {
                val jsonString = generateBackupJson(db)
                val root = JSONObject(jsonString)
                val counts = listOf("incomes", "expenses", "credit_cards", "card_purchases", "savings")
                    .map { root.optJSONArray(it)?.length() ?: 0 }

                val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "CapyCoin_Backup_$timeStamp.json"
                val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
                val file = writeBackupFileSafely(backupDir, fileName, jsonString)
                ExportedBackup(fileName, timeStamp, file, counts)
            }
            val (incomesCount, expensesCount, cardsCount, purchasesCount, savingsCount) = counts

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, tr("Copia de Seguridad CapyCoin - $timeStamp", "CapyCoin backup - $timeStamp"))
                putExtra(Intent.EXTRA_TEXT, tr("Copia de seguridad de gastos y finanzas personales ($fileName)", "Backup of personal expenses and finances ($fileName)"))
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Abrir otra pantalla es trabajo del hilo principal, no del de entrada/salida
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, tr("Guardar / Compartir Copia de Seguridad", "Save / share backup")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }

            val summary = tr("Copia creada ($fileName). Elige dónde guardarla en el menú de compartir:\n• $incomesCount ingresos\n• $expensesCount gastos fijos\n• $cardsCount tarjetas\n• $purchasesCount compras a cuotas\n• $savingsCount ahorros", "Backup created ($fileName). Choose where to save it in the share menu:\n• $incomesCount income items\n• $expensesCount fixed expenses\n• $cardsCount cards\n• $purchasesCount installment purchases\n• $savingsCount savings")
            Result.success(summary)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ExportedBackup(val fileName: String, val timeStamp: String, val file: File, val counts: List<Int>)

    fun newBackupFileName(): String {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
        return "CapyCoin_Backup_$timeStamp.json"
    }

    /** Escribe la copia completa en el archivo que el usuario eligió con el selector del sistema. */
    suspend fun saveBackupTo(
        context: Context,
        db: AppDatabase,
        uri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson(db)
            val root = JSONObject(jsonString)
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception(tr("No se pudo escribir el archivo.", "Couldn't write the file.")))
            val purchasesCount = root.optJSONArray("card_purchases")?.length() ?: 0
            val expensesCount = root.optJSONArray("expenses")?.length() ?: 0
            val incomesCount = root.optJSONArray("incomes")?.length() ?: 0
            Result.success(tr(
                "Copia guardada correctamente:\n• $incomesCount ingresos\n• $expensesCount gastos fijos\n• $purchasesCount compras con tarjeta",
                "Backup saved:\n• $incomesCount income items\n• $expensesCount fixed expenses\n• $purchasesCount card purchases"
            ))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(
        context: Context,
        uri: Uri,
        db: AppDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                readBackupText(stream)
            } ?: return@withContext Result.failure(Exception(tr("No se pudo leer el archivo seleccionado.", "Couldn't read the selected file.")))
            restoreFromJsonString(db, jsonString)
        } catch (e: BackupTooLargeException) {
            Result.failure(Exception(tr(
                "El archivo pesa más de ${MAX_BACKUP_BYTES / (1024 * 1024)} MB y no parece una copia de CapyCoin. No se restauró nada.",
                "The file is larger than ${MAX_BACKUP_BYTES / (1024 * 1024)} MB and doesn't look like a CapyCoin backup. Nothing was restored."
            )))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Texto con el que se nombra cada lista de la copia al avisar de registros no restaurados. */
    private fun sectionLabel(section: String): String = when (section) {
        BackupSections.INCOMES -> tr("ingresos", "income items")
        BackupSections.EXPENSES -> tr("gastos fijos", "fixed expenses")
        BackupSections.EXPENSE_PAYMENTS -> tr("abonos a gastos", "extra payments to expenses")
        BackupSections.CREDIT_CARDS -> tr("tarjetas", "cards")
        BackupSections.CARD_PURCHASES -> tr("compras con tarjeta", "card purchases")
        BackupSections.SAVINGS -> tr("ahorros", "savings")
        BackupSections.CARD_PURCHASE_PAYMENTS -> tr("abonos a compras", "extra payments to purchases")
        else -> tr("marcas (pagado, omitido, recibido…)", "marks (paid, skipped, received…)")
    }

    private fun describeCounts(counts: Map<String, Int>): String =
        counts.entries.groupBy({ sectionLabel(it.key) }, { it.value })
            .map { (label, values) -> "${values.sum()} $label" }
            .joinToString(", ")

    private fun rejectionMessage(reason: BackupRejection): String = when (reason) {
        BackupRejection.NOT_A_BACKUP, BackupRejection.WRONG_APP ->
            tr("El archivo no es una copia de seguridad válida de CapyCoin.", "This file isn't a valid CapyCoin backup.")
        BackupRejection.UNSUPPORTED_VERSION ->
            tr("La copia es de una versión más nueva de CapyCoin o su versión no es válida. Actualiza la app e inténtalo de nuevo.", "This backup comes from a newer CapyCoin version, or its version isn't valid. Update the app and try again.")
        BackupRejection.INVALID_STRUCTURE ->
            tr("La copia de seguridad está dañada (su estructura no es la esperada).", "The backup is damaged (its structure isn't what was expected).")
        BackupRejection.EMPTY ->
            tr("La copia no tiene ningún registro válido. No se restauró nada para no borrar tus datos actuales.", "The backup has no valid records. Nothing was restored, so your current data is untouched.")
    }

    private fun plainValue(value: Any?): Any? = when (value) {
        null, JSONObject.NULL -> null
        is JSONObject -> toPlainMap(value)
        is JSONArray -> List(value.length()) { plainValue(value.opt(it)) }
        else -> value
    }

    /** Pasa el JSON a mapas y listas normales para validarlo con [parseBackup]. */
    private fun toPlainMap(obj: JSONObject): Map<String, Any?> {
        val map = LinkedHashMap<String, Any?>()
        obj.keys().forEach { key -> map[key] = plainValue(obj.opt(key)) }
        return map
    }

    /**
     * Restaura una copia. Primero valida TODO el archivo ([parseBackup]); si se rechaza, no se toca
     * la base de datos. Si es válido, borrar e insertar van en una sola transacción: o queda la copia
     * completa o, si algo falla, los datos de antes. El mensaje avisa de los registros no restaurados.
     */
    suspend fun restoreFromJsonString(
        db: AppDatabase,
        jsonString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanJson = jsonString.trim().removePrefix("\uFEFF")
            val root = try {
                JSONObject(cleanJson)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception(tr("El archivo no es una copia de seguridad válida de CapyCoin (no se pudo leer).", "This file isn't a valid CapyCoin backup (it couldn't be read).")))
            }

            val parsed = when (val outcome = parseBackup(toPlainMap(root))) {
                is BackupParseOutcome.Rejected -> return@withContext Result.failure(Exception(rejectionMessage(outcome.reason)))
                is BackupParseOutcome.Ok -> outcome
            }
            val data = parsed.data

            db.withTransaction {
                db.incomeDao().clearAllIncomes()
                db.expenseDao().clearAllExpenses()
                db.expensePaymentDao().clearAllPayments()
                db.creditCardDao().clearAllCreditCards()
                db.cardPurchaseDao().clearAllCardPurchases()
                db.savingsDao().clearAllSavings()
                db.omittedInstallmentDao().clearAllOmittedInstallments()
                db.cardPurchasePaymentDao().clearAllPayments()
                db.ignoredExpenseDao().clearAllIgnoredExpenses()
                db.paidCreditCardDao().clearAllPaidCreditCards()
                db.paidExpenseDao().clearAllPaidExpenses()
                db.settledPurchaseDao().clearAllSettledPurchases()
                db.receivedIncomeDao().clearAllReceivedIncomes()

                if (data.incomes.isNotEmpty()) db.incomeDao().insertAllIncomes(data.incomes)
                if (data.expenses.isNotEmpty()) db.expenseDao().insertAllExpenses(data.expenses)
                if (data.expensePayments.isNotEmpty()) db.expensePaymentDao().insertAllPayments(data.expensePayments)
                if (data.cards.isNotEmpty()) db.creditCardDao().insertAllCreditCards(data.cards)
                if (data.purchases.isNotEmpty()) db.cardPurchaseDao().insertAllCardPurchases(data.purchases)
                if (data.savings.isNotEmpty()) db.savingsDao().insertAllSavings(data.savings)
                if (data.omittedInstallments.isNotEmpty()) db.omittedInstallmentDao().insertAllOmittedInstallments(data.omittedInstallments)
                if (data.purchasePayments.isNotEmpty()) db.cardPurchasePaymentDao().insertAllPayments(data.purchasePayments)
                if (data.ignoredExpenses.isNotEmpty()) db.ignoredExpenseDao().insertAllIgnoredExpenses(data.ignoredExpenses)
                if (data.paidCards.isNotEmpty()) db.paidCreditCardDao().insertAllPaidCreditCards(data.paidCards)
                if (data.paidExpenses.isNotEmpty()) db.paidExpenseDao().insertAllPaidExpenses(data.paidExpenses)
                if (data.settledPurchases.isNotEmpty()) db.settledPurchaseDao().insertAllSettledPurchases(data.settledPurchases)
                if (data.receivedIncomes.isNotEmpty()) db.receivedIncomeDao().insertAllReceivedIncomes(data.receivedIncomes)
            }

            val resultMsg = buildString {
                append(tr("¡Datos restaurados con éxito!\n• ${data.incomes.size} ingresos\n• ${data.expenses.size} gastos fijos\n• ${data.cards.size} tarjetas de crédito\n• ${data.purchases.size} compras a cuotas\n• ${data.savings.size} ahorros", "Data restored!\n• ${data.incomes.size} income items\n• ${data.expenses.size} fixed expenses\n• ${data.cards.size} credit cards\n• ${data.purchases.size} installment purchases\n• ${data.savings.size} savings"))
                if (parsed.discardedTotal > 0) {
                    val detail = describeCounts(parsed.discarded)
                    append(tr("\n\n⚠ ${parsed.discardedTotal} registros NO se restauraron porque sus datos no eran válidos en la copia: $detail. Revísalos y vuelve a registrarlos.", "\n\n⚠ ${parsed.discardedTotal} records were NOT restored because their data was invalid in the backup: $detail. Check them and add them again."))
                }
                if (parsed.orphanedTotal > 0) {
                    val detail = describeCounts(parsed.orphaned)
                    append(tr("\n\nSe omitieron ${parsed.orphanedTotal} registros que dependían de algo que no está en la copia: $detail.", "\n\n${parsed.orphanedTotal} records were skipped because they depended on something that isn't in the backup: $detail."))
                }
            }
            Result.success(resultMsg)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Escribe la copia sin arriesgar la anterior: primero a un archivo temporal, luego lo renombra
 * y solo al final borra las copias viejas de la carpeta. Si la escritura falla, la copia anterior sigue ahí.
 */
internal fun writeBackupFileSafely(backupDir: File, fileName: String, content: String): File {
    if (!backupDir.isDirectory && !backupDir.mkdirs()) throw java.io.IOException("No se pudo crear la carpeta de copias")
    val target = File(backupDir, fileName)
    val temp = File(backupDir, "$fileName.tmp")
    try {
        temp.writeText(content)
        if (temp.length() == 0L && content.isNotEmpty()) throw java.io.IOException("La copia quedó vacía")
        if (target.exists() && !target.delete()) throw java.io.IOException("No se pudo reemplazar la copia")
        if (!temp.renameTo(target)) throw java.io.IOException("No se pudo guardar la copia")
    } catch (e: Exception) {
        temp.delete()
        throw e
    }
    backupDir.listFiles()?.forEach { if (it != target) it.delete() }
    return target
}

/** Tamaño máximo de una copia que se intenta restaurar. Una copia real pesa unos pocos cientos de KB. */
const val MAX_BACKUP_BYTES = 50L * 1024 * 1024

internal class BackupTooLargeException : java.io.IOException("La copia supera el tamaño máximo")

/**
 * Lee la copia como texto UTF-8 sin cargar más de [maxBytes] en memoria: si el archivo es más grande
 * lanza [BackupTooLargeException] en cuanto lo nota, en vez de leerlo entero y quedarse sin memoria.
 */
internal fun readBackupText(stream: java.io.InputStream, maxBytes: Long = MAX_BACKUP_BYTES): String {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(64 * 1024)
    var total = 0L
    while (true) {
        val read = stream.read(buffer)
        if (read < 0) break
        total += read
        if (total > maxBytes) throw BackupTooLargeException()
        out.write(buffer, 0, read)
    }
    return out.toString(Charsets.UTF_8.name())
}
