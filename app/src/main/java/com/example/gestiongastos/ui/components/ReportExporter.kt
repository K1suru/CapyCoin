package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.model.ItemKeys
import com.example.gestiongastos.data.model.maintenanceFeeInCop
import com.example.gestiongastos.data.model.payableThisMonth
import java.math.BigDecimal
import java.math.RoundingMode
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

/**
 * Número para el CSV sin notación científica (12000000.0 -> "12000000") y con coma decimal,
 * que es lo que espera Excel en español al usar ";" como separador.
 */
internal fun csvNumber(value: Double): String =
    BigDecimal(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        .let { if (it.scale() < 0) it.setScale(0) else it }
        .toPlainString()
        .replace('.', ',')

fun exportMonthlyCsvReport(
    context: Context,
    incomes: List<IncomeEntity>,
    expenses: List<ExpenseEntity>,
    cardSummaries: List<CardMonthSummary>,
    savings: List<SavingsEntity>,
    selectedYearMonth: YearMonth,
    usdRate: Double = DEFAULT_USD_RATE,
    ignoredKeys: Set<String> = emptySet()
) {
    val monthName = selectedYearMonth.month
        .getDisplayName(TextStyle.FULL, appLocale())
        .uppercase()
    val year = selectedYearMonth.year

    // Totales con el mismo criterio que el Resumen de la app (sin ítems ignorados)
    val totalIncome = incomes.sumOf { it.amount }
    val directExpenses = expenses
        .filter { ItemKeys.direct(it.id) !in ignoredKeys }
        .sumOf { if (it.currency == "USD") it.amount * usdRate else it.amount }
    val cardExpenses = cardSummaries.sumOf { it.payableThisMonth(usdRate, ignoredKeys) }
    val totalExpenses = directExpenses + cardExpenses
    val totalSavings = savings
        .filter { ItemKeys.saving(it.id) !in ignoredKeys }
        .sumOf { it.amount }
    val ignoredSuffix = tr(";(Ignorado - no suma)", ";(Ignored - not counted)")
    val neededAmount = totalExpenses + totalSavings
    val freeAmount = totalIncome - neededAmount

    val sb = StringBuilder()
    // UTF-8 BOM for Microsoft Excel auto-detection
    sb.append('\uFEFF')

    sb.appendLine(tr("REPORTE MENSUAL CAPYCOIN - $monthName $year", "CAPYCOIN MONTHLY REPORT - $monthName $year"))
    sb.appendLine(tr("Fecha de Generación", "Generated on") + ";${SimpleDateFormat("dd/MM/yyyy HH:mm", appLocale()).format(Date())}")
    sb.appendLine(tr("Tasa USD Referencia;${csvNumber(usdRate)}", "USD reference rate;${csvNumber(usdRate)}"))
    sb.appendLine()

    // 1. RESUMEN GENERAL
    sb.appendLine(tr("1. RESUMEN GENERAL", "1. OVERVIEW"))
    sb.appendLine(tr("Concepto;Monto (COP)", "Item;Amount (COP)"))
    sb.appendLine(tr("Total Ingresos;${csvNumber(totalIncome)}", "Total income;${csvNumber(totalIncome)}"))
    sb.appendLine(tr("Gastos Directos;${csvNumber(directExpenses)}", "Direct expenses;${csvNumber(directExpenses)}"))
    sb.appendLine(tr("Gastos Tarjetas de Crédito;${csvNumber(cardExpenses)}", "Credit card expenses;${csvNumber(cardExpenses)}"))
    sb.appendLine(tr("TOTAL GASTOS;${csvNumber(totalExpenses)}", "TOTAL EXPENSES;${csvNumber(totalExpenses)}"))
    sb.appendLine(tr("TOTAL AHORROS;${csvNumber(totalSavings)}", "TOTAL SAVINGS;${csvNumber(totalSavings)}"))
    sb.appendLine(tr("PRESUPUESTO NECESARIO;${csvNumber(neededAmount)}", "BUDGET NEEDED;${csvNumber(neededAmount)}"))
    sb.appendLine(tr("MONTO LIBRE / RESTANTE;${csvNumber(freeAmount)}", "FREE / REMAINING;${csvNumber(freeAmount)}"))
    sb.appendLine()

    // 2. INGRESOS
    sb.appendLine(tr("2. INGRESOS DEL MES", "2. INCOME THIS MONTH"))
    sb.appendLine(tr("Concepto;Monto (COP);Tipo;Fecha", "Item;Amount (COP);Type;Date"))
    if (incomes.isEmpty()) {
        sb.appendLine(tr("(Sin ingresos registrados);0;;", "(No income added);0;;"))
    } else {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        incomes.forEach { inc ->
            val dateStr = dateFormat.format(Date(inc.date))
            val typeStr = when {
                inc.totalInstallments > 1 -> {
                    val entryDate = try {
                        Instant.ofEpochMilli(inc.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    } catch (e: Exception) {
                        java.time.LocalDate.now()
                    }
                    val entryYearMonth = YearMonth.from(entryDate)
                    val diff = (selectedYearMonth.year - entryYearMonth.year) * 12 + (selectedYearMonth.monthValue - entryYearMonth.monthValue)
                    val currentInst = (diff + 1).coerceIn(1, inc.totalInstallments)
                    tr("Cuota $currentInst de ${inc.totalInstallments}", "Installment $currentInst of ${inc.totalInstallments}")
                }
                inc.isRecurring -> {
                    if (inc.endDateMillis != null) {
                        val endYm = try {
                            YearMonth.from(Instant.ofEpochMilli(inc.endDateMillis).atZone(ZoneId.systemDefault()).toLocalDate())
                        } catch (e: Exception) { null }
                        if (endYm != null) tr("Recurrente (hasta ${endYm.monthValue}/${endYm.year})", "Recurring (until ${endYm.monthValue}/${endYm.year})") else tr("Recurrente", "Recurring")
                    } else {
                        tr("Recurrente", "Recurring")
                    }
                }
                else -> tr("Único", "One-time")
            }
            sb.appendLine("\"${inc.concept.replace("\"", "\"\"")}\";${csvNumber(inc.amount)};$typeStr;$dateStr")
        }
    }
    sb.appendLine()

    // 3. GASTOS DIRECTOS
    sb.appendLine(tr("3. GASTOS DIRECTOS / FIJOS", "3. DIRECT / FIXED EXPENSES"))
    sb.appendLine(tr("Concepto;Monto COP;Moneda Original;Monto Original;Tipo;Fecha", "Item;Amount COP;Original currency;Original amount;Type;Date"))
    if (expenses.isEmpty()) {
        sb.appendLine(tr("(Sin gastos directos);0;COP;0;;", "(No direct expenses);0;COP;0;;"))
    } else {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        expenses.forEach { exp ->
            val dateStr = dateFormat.format(Date(exp.date))
            val cop = if (exp.currency == "USD") exp.amount * usdRate else exp.amount
            val typeStr = if (exp.isRecurring) tr("Fijo Recurrente", "Recurring fixed") else tr("Gasto Único", "One-time expense")
            val ignored = if (ItemKeys.direct(exp.id) in ignoredKeys) ignoredSuffix else ""
            sb.appendLine("\"${exp.concept.replace("\"", "\"\"")}\";${csvNumber(cop)};${exp.currency};${csvNumber(exp.amount)};$typeStr;$dateStr$ignored")
        }
    }
    sb.appendLine()

    // 4. TARJETAS DE CRÉDITO
    sb.appendLine(tr("4. TARJETAS DE CRÉDITO Y CUOTAS ACTIVAS", "4. CREDIT CARDS AND ACTIVE INSTALLMENTS"))
    sb.appendLine(tr("Tarjeta;Concepto;Cuota Mes (COP);Moneda;Monto Cuota Orig.;Estado Cuotas;Condición Interés;Saldo Pendiente COP", "Card;Item;Monthly payment (COP);Currency;Orig. payment amount;Installment status;Interest;Remaining balance COP"))
    if (cardSummaries.isEmpty()) {
        sb.appendLine(tr("(Sin tarjetas registradas);;;;;;;", "(No cards added);;;;;;;"))
    } else {
        cardSummaries.forEach { card ->
            val activePurchases = card.purchases.filter { it.statusType == InstallmentStatusType.ACTIVE }
            activePurchases.forEach { p ->
                val copMonth = p.monthlyPaymentInCop(usdRate)
                val cond = when {
                    p.isFirstInstallmentInterestFree && p.currentInstallment == 1 -> tr("1ª Cuota 0% Interés", "1st installment 0% interest")
                    p.isInterestFree -> tr("0% Interés", "0% interest")
                    card.interestRate > 0 -> tr("Interés ${card.interestRate}%", "Interest ${card.interestRate}%")
                    else -> tr("Sin Interés", "No interest")
                }
                val pendingCop = if (p.totalInstallments > 1) p.remainingBalanceToPayInCop(usdRate) else 0.0
                val ignored = if (ItemKeys.cardInstallment(p.purchaseId) in ignoredKeys) ignoredSuffix else ""
                sb.appendLine("\"${card.cardName.replace("\"", "\"\"")}\";\"${p.concept.replace("\"", "\"\"")}\";${csvNumber(copMonth)};${p.currency};${csvNumber(p.monthlyPayment)};${p.statusText};$cond;${csvNumber(pendingCop)}$ignored")
            }
            val feeCop = card.maintenanceFeeInCop(usdRate)
            if (feeCop > 0) {
                val ignored = if (ItemKeys.cardFee(card.cardId) in ignoredKeys) ignoredSuffix else ""
                sb.appendLine(tr("\"${card.cardName.replace("\"", "\"\"")}\";\"Cuota de Manejo\";${csvNumber(feeCop)};${card.currency};${csvNumber(card.maintenanceFee)};Mensual;Tarifa Fija;0$ignored", "\"${card.cardName.replace("\"", "\"\"")}\";\"Card fee\";${csvNumber(feeCop)};${card.currency};${csvNumber(card.maintenanceFee)};Monthly;Fixed fee;0$ignored"))
            }
        }
    }
    sb.appendLine()

    // 5. AHORROS
    sb.appendLine(tr("5. AHORROS DEL MES", "5. SAVINGS THIS MONTH"))
    sb.appendLine(tr("Concepto;Monto (COP);Tipo;Fecha", "Item;Amount (COP);Type;Date"))
    if (savings.isEmpty()) {
        sb.appendLine(tr("(Sin aportes a ahorros este mes);0;;", "(No savings this month);0;;"))
    } else {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        savings.forEach { sav ->
            val dateStr = dateFormat.format(Date(sav.date))
            val typeStr = if (sav.isRecurring) tr("Recurrente", "Recurring") else tr("Aporte Único", "One-time saving")
            val ignored = if (ItemKeys.saving(sav.id) in ignoredKeys) ignoredSuffix else ""
            sb.appendLine("\"${sav.concept.replace("\"", "\"\"")}\";${csvNumber(sav.amount)};$typeStr;$dateStr$ignored")
        }
    }

    val fileName = tr("CapyCoin_Reporte_${monthName}_${year}.csv", "CapyCoin_Report_${monthName}_${year}.csv")
    val csvContent = sb.toString()

    try {
        var savedToDownloads = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csvContent.toByteArray(Charsets.UTF_8))
                    savedToDownloads = true
                }
            }
        }

        val cacheFile = File(context.cacheDir, fileName)
        cacheFile.writeText(csvContent, Charsets.UTF_8)

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, tr("Reporte Mensual CapyCoin - $monthName $year", "CapyCoin monthly report - $monthName $year"))
            putExtra(Intent.EXTRA_TEXT, tr("Adjunto reporte financiero correspondiente a $monthName $year.", "Attached is the financial report for $monthName $year."))
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, tr("Descargar / Compartir Reporte CSV", "Download / share CSV report")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        if (savedToDownloads) {
            Toast.makeText(context, tr("Reporte guardado en Descargas: $fileName", "Report saved to Downloads: $fileName"), Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, tr("Reporte Mensual CapyCoin - $monthName $year", "CapyCoin monthly report - $monthName $year"))
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        context.startActivity(Intent.createChooser(intent, tr("Compartir Reporte", "Share report")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
