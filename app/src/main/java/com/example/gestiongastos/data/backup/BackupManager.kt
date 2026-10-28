package com.example.gestiongastos.data.backup

import com.example.gestiongastos.data.local.ExpensePaymentEntity
import com.example.gestiongastos.i18n.tr
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.gestiongastos.data.local.AppDatabase
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CardPurchasePaymentEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IgnoredExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.OmittedInstallmentEntity
import com.example.gestiongastos.data.local.PaidCreditCardEntity
import com.example.gestiongastos.data.local.PaidExpenseEntity
import com.example.gestiongastos.data.local.ReceivedIncomeEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.local.SettledPurchaseEntity
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

    private fun optNullableLong(obj: JSONObject, key: String): Long? =
        if (!obj.has(key) || obj.isNull(key)) null else obj.optLong(key)

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
            put("version", 1)
            put("exportTimestamp", System.currentTimeMillis())
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

            // Incomes
            put("incomes", JSONArray().apply {
                incomes.forEach {
                    put(JSONObject().apply {
                        put("id", it.id)
                        put("concept", it.concept)
                        put("amount", it.amount)
                        put("date", it.date)
                        put("isRecurring", it.isRecurring)
                        put("linkedExpenseIdsText", it.linkedExpenseIdsText ?: JSONObject.NULL)
                        put("linkedCardPurchaseIdsText", it.linkedCardPurchaseIdsText ?: JSONObject.NULL)
                        put("extraAmount", it.extraAmount)
                        put("extraConcept", it.extraConcept ?: JSONObject.NULL)
                        put("totalInstallments", it.totalInstallments)
                        put("totalAmount", it.totalAmount)
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
                        put("amount", it.amount)
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
                        put("amount", it.amount)
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
                        put("maintenanceFee", it.maintenanceFee)
                        put("currency", it.currency)
                        put("isFeeWaiverEnabled", it.isFeeWaiverEnabled)
                        put("minSpendForFeeWaiver", it.minSpendForFeeWaiver)
                        put("isFirstInstallmentInterestFree", it.isFirstInstallmentInterestFree)
                        put("creditLimit", it.creditLimit)
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
                        put("totalAmount", it.totalAmount)
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
                        put("amount", it.amount)
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
                        put("amount", it.amount)
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

    suspend fun exportBackup(
        context: Context,
        db: AppDatabase
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson(db)
            val root = JSONObject(jsonString)
            val incomesCount = root.optJSONArray("incomes")?.length() ?: 0
            val expensesCount = root.optJSONArray("expenses")?.length() ?: 0
            val cardsCount = root.optJSONArray("credit_cards")?.length() ?: 0
            val purchasesCount = root.optJSONArray("card_purchases")?.length() ?: 0
            val savingsCount = root.optJSONArray("savings")?.length() ?: 0

            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "CapyCoin_Backup_$timeStamp.json"

            // La copia queda en la caché privada de la app y el usuario elige dónde guardarla
            // (Archivos, correo, etc.) desde el menú de compartir. Ya no se escribe sin cifrar
            // en la carpeta pública de Descargas, donde otras apps podían leerla.
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            backupDir.listFiles()?.forEach { it.delete() }
            val cacheFile = File(backupDir, fileName)
            cacheFile.writeText(jsonString)

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

            context.startActivity(Intent.createChooser(shareIntent, tr("Guardar / Compartir Copia de Seguridad", "Save / share backup")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            val summary = tr("Copia creada ($fileName). Elige dónde guardarla en el menú de compartir:\n• $incomesCount ingresos\n• $expensesCount gastos fijos\n• $cardsCount tarjetas\n• $purchasesCount compras a cuotas\n• $savingsCount ahorros", "Backup created ($fileName). Choose where to save it in the share menu:\n• $incomesCount income items\n• $expensesCount fixed expenses\n• $cardsCount cards\n• $purchasesCount installment purchases\n• $savingsCount savings")
            Result.success(summary)
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
                stream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception(tr("No se pudo leer el archivo seleccionado.", "Couldn't read the selected file.")))
            restoreFromJsonString(db, jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromJsonString(
        db: AppDatabase,
        jsonString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanJson = jsonString.trim().removePrefix("\uFEFF")
            val root = JSONObject(cleanJson)

            val hasDataArrays = root.has("incomes") || root.has("expenses") || root.has("credit_cards") || root.has("card_purchases")
            val hasAppMarker = root.has("app") || root.has("version")
            if (!hasDataArrays && !hasAppMarker) {
                return@withContext Result.failure(Exception(tr("El archivo no es una copia de seguridad válida de CapyCoin.", "This file isn't a valid CapyCoin backup.")))
            }

            val incomesList = mutableListOf<IncomeEntity>()
            root.optJSONArray("incomes")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    incomesList.add(
                        IncomeEntity(
                            id = obj.optLong("id", 0L),
                            concept = obj.optString("concept", "Ingreso").ifBlank { "Ingreso" },
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            linkedExpenseIdsText = if (obj.isNull("linkedExpenseIdsText") || !obj.has("linkedExpenseIdsText")) null else obj.optString("linkedExpenseIdsText").takeIf { it.isNotBlank() },
                            linkedCardPurchaseIdsText = if (obj.isNull("linkedCardPurchaseIdsText") || !obj.has("linkedCardPurchaseIdsText")) null else obj.optString("linkedCardPurchaseIdsText").takeIf { it.isNotBlank() },
                            extraAmount = obj.optDouble("extraAmount", 0.0),
                            extraConcept = if (obj.isNull("extraConcept") || !obj.has("extraConcept")) null else obj.optString("extraConcept").takeIf { it.isNotBlank() },
                            totalInstallments = obj.optInt("totalInstallments", 1).coerceAtLeast(1),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            endDateMillis = optNullableLong(obj, "endDateMillis")
                        )
                    )
                }
            }

            val expensesList = mutableListOf<ExpenseEntity>()
            root.optJSONArray("expenses")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    expensesList.add(
                        ExpenseEntity(
                            id = obj.optLong("id", 0L),
                            concept = obj.optString("concept", "Gasto").ifBlank { "Gasto" },
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            currency = obj.optString("currency", "COP"),
                            endDateMillis = optNullableLong(obj, "endDateMillis"),
                            installments = obj.optInt("installments", 0).coerceAtLeast(0)
                        )
                    )
                }
            }

            val expensePaymentsList = mutableListOf<ExpensePaymentEntity>()
            root.optJSONArray("expense_payments")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    expensePaymentsList.add(
                        ExpensePaymentEntity(
                            id = obj.optLong("id", 0L),
                            expenseId = obj.optLong("expenseId", 0L),
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            yearMonthText = obj.optString("yearMonthText", "")
                        )
                    )
                }
            }

            val cardsList = mutableListOf<CreditCardEntity>()
            root.optJSONArray("credit_cards")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    cardsList.add(
                        CreditCardEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", "Tarjeta").ifBlank { "Tarjeta" },
                            cutoffDay = obj.optInt("cutoffDay", 15),
                            paymentDay = obj.optInt("paymentDay", 0),
                            interestRate = obj.optDouble("interestRate", 0.0),
                            maintenanceFee = obj.optDouble("maintenanceFee", 0.0),
                            currency = obj.optString("currency", "COP"),
                            isFeeWaiverEnabled = obj.optBoolean("isFeeWaiverEnabled", false),
                            minSpendForFeeWaiver = obj.optDouble("minSpendForFeeWaiver", 0.0),
                            isFirstInstallmentInterestFree = obj.optBoolean("isFirstInstallmentInterestFree", false),
                            creditLimit = obj.optDouble("creditLimit", 0.0).takeIf { !it.isNaN() } ?: 0.0,
                            colorIndex = obj.optInt("colorIndex", -1)
                        )
                    )
                }
            }

            val purchasesList = mutableListOf<CardPurchaseEntity>()
            root.optJSONArray("card_purchases")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    purchasesList.add(
                        CardPurchaseEntity(
                            id = obj.optLong("id", 0L),
                            cardId = obj.optLong("cardId", 0L),
                            concept = obj.optString("concept", "Compra").ifBlank { "Compra" },
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            installments = obj.optInt("installments", 1).coerceAtLeast(1),
                            purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
                            isInterestFree = obj.optBoolean("isInterestFree", false),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            currency = obj.optString("currency", "COP")
                        )
                    )
                }
            }

            val savingsList = mutableListOf<SavingsEntity>()
            root.optJSONArray("savings")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    savingsList.add(
                        SavingsEntity(
                            id = obj.optLong("id", 0L),
                            concept = obj.optString("concept", "Ahorro").ifBlank { "Ahorro" },
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            isRecurring = obj.optBoolean("isRecurring", false),
                            endDateMillis = optNullableLong(obj, "endDateMillis")
                        )
                    )
                }
            }

            val omittedList = mutableListOf<OmittedInstallmentEntity>()
            root.optJSONArray("omitted_installments")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val ymText = obj.optString("yearMonthText", "")
                    if (ymText.isNotBlank()) {
                        omittedList.add(
                            OmittedInstallmentEntity(
                                id = obj.optLong("id", 0L),
                                purchaseId = obj.optLong("purchaseId", 0L),
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            val paymentsList = mutableListOf<CardPurchasePaymentEntity>()
            root.optJSONArray("card_purchase_payments")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val ymText = obj.optString("yearMonthText", "")
                    paymentsList.add(
                        CardPurchasePaymentEntity(
                            id = obj.optLong("id", 0L),
                            purchaseId = obj.optLong("purchaseId", 0L),
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            yearMonthText = ymText
                        )
                    )
                }
            }

            val ignoredList = mutableListOf<IgnoredExpenseEntity>()
            root.optJSONArray("ignored_expenses")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val key = obj.optString("itemKey", "")
                    val ymText = obj.optString("yearMonthText", "")
                    if (key.isNotBlank() && ymText.isNotBlank()) {
                        ignoredList.add(
                            IgnoredExpenseEntity(
                                id = obj.optLong("id", 0L),
                                itemKey = key,
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            val paidCardsList = mutableListOf<PaidCreditCardEntity>()
            root.optJSONArray("paid_credit_cards")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val ymText = obj.optString("yearMonthText", "")
                    if (ymText.isNotBlank()) {
                        paidCardsList.add(
                            PaidCreditCardEntity(
                                id = obj.optLong("id", 0L),
                                cardId = obj.optLong("cardId", 0L),
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            val paidExpensesList = mutableListOf<PaidExpenseEntity>()
            root.optJSONArray("paid_expenses")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val key = obj.optString("itemKey", "")
                    val ymText = obj.optString("yearMonthText", "")
                    if (key.isNotBlank() && ymText.isNotBlank()) {
                        paidExpensesList.add(
                            PaidExpenseEntity(
                                id = obj.optLong("id", 0L),
                                itemKey = key,
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            val settledPurchasesList = mutableListOf<SettledPurchaseEntity>()
            root.optJSONArray("settled_purchases")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val ymText = obj.optString("yearMonthText", "")
                    if (ymText.isNotBlank()) {
                        settledPurchasesList.add(
                            SettledPurchaseEntity(
                                id = obj.optLong("id", 0L),
                                purchaseId = obj.optLong("purchaseId", 0L),
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            val receivedIncomesList = mutableListOf<ReceivedIncomeEntity>()
            root.optJSONArray("received_incomes")?.let { array ->
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val ymText = obj.optString("yearMonthText", "")
                    if (ymText.isNotBlank()) {
                        receivedIncomesList.add(
                            ReceivedIncomeEntity(
                                id = obj.optLong("id", 0L),
                                incomeId = obj.optLong("incomeId", 0L),
                                yearMonthText = ymText
                            )
                        )
                    }
                }
            }

            // Transactionally clear and restore all
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

                if (incomesList.isNotEmpty()) db.incomeDao().insertAllIncomes(incomesList)
                if (expensesList.isNotEmpty()) db.expenseDao().insertAllExpenses(expensesList)
                if (expensePaymentsList.isNotEmpty()) db.expensePaymentDao().insertAllPayments(expensePaymentsList)
                if (cardsList.isNotEmpty()) db.creditCardDao().insertAllCreditCards(cardsList)
                if (purchasesList.isNotEmpty()) db.cardPurchaseDao().insertAllCardPurchases(purchasesList)
                if (savingsList.isNotEmpty()) db.savingsDao().insertAllSavings(savingsList)
                if (omittedList.isNotEmpty()) db.omittedInstallmentDao().insertAllOmittedInstallments(omittedList)
                if (paymentsList.isNotEmpty()) db.cardPurchasePaymentDao().insertAllPayments(paymentsList)
                if (ignoredList.isNotEmpty()) db.ignoredExpenseDao().insertAllIgnoredExpenses(ignoredList)
                if (paidCardsList.isNotEmpty()) db.paidCreditCardDao().insertAllPaidCreditCards(paidCardsList)
                if (paidExpensesList.isNotEmpty()) db.paidExpenseDao().insertAllPaidExpenses(paidExpensesList)
                if (settledPurchasesList.isNotEmpty()) db.settledPurchaseDao().insertAllSettledPurchases(settledPurchasesList)
                if (receivedIncomesList.isNotEmpty()) db.receivedIncomeDao().insertAllReceivedIncomes(receivedIncomesList)
            }

            val resultMsg = tr("¡Datos restaurados con éxito!\n• ${incomesList.size} ingresos\n• ${expensesList.size} gastos fijos\n• ${cardsList.size} tarjetas de crédito\n• ${purchasesList.size} compras a cuotas\n• ${savingsList.size} ahorros", "Data restored!\n• ${incomesList.size} income items\n• ${expensesList.size} fixed expenses\n• ${cardsList.size} credit cards\n• ${purchasesList.size} installment purchases\n• ${savingsList.size} savings")
            Result.success(resultMsg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
