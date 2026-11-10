package com.example.gestiongastos.ui.components

import com.example.gestiongastos.data.repository.localDateOf
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

/**
 * Calculates a timestamp in milliseconds for an entry in [targetYearMonth].
 * If [existingDate] is provided and positive, it is preserved.
 * If [targetYearMonth] matches the current system month, current time in millis is used.
 * Otherwise, a date in [targetYearMonth] (day 15) is returned so that
 * when evaluated with ZoneId.systemDefault(), it strictly belongs to [targetYearMonth].
 */
fun calculateSaveDateForMonth(targetYearMonth: YearMonth, existingDate: Long? = null): Long {
    if (existingDate != null && existingDate > 0L) {
        return existingDate
    }
    val now = System.currentTimeMillis()
    val nowLocalDate = localDateOf(now)
    val nowYm = YearMonth.from(nowLocalDate)
    return if (targetYearMonth == nowYm) {
        now
    } else {
        val day = 15.coerceAtMost(targetYearMonth.lengthOfMonth())
        targetYearMonth.atDay(day)
            .atTime(LocalTime.now())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}

/** Fecha larga para los formularios: "9 de noviembre de 2026" / "November 9, 2026". */
fun longDateFormat(): java.text.SimpleDateFormat =
    java.text.SimpleDateFormat(com.example.gestiongastos.i18n.tr("dd 'de' MMMM 'de' yyyy", "MMMM d, yyyy"), com.example.gestiongastos.i18n.appLocale())

/** Fecha corta para las listas: "09 nov 2026" / "Nov 9, 2026". */
fun shortDateFormat(): java.text.SimpleDateFormat =
    java.text.SimpleDateFormat(com.example.gestiongastos.i18n.tr("dd MMM yyyy", "MMM d, yyyy"), com.example.gestiongastos.i18n.appLocale())
