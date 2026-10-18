package com.example.gestiongastos.ui.components

import java.time.Instant
import java.time.LocalDate
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
    val nowLocalDate = try {
        Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (e: Exception) {
        LocalDate.now()
    }
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
