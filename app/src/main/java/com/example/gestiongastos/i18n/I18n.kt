package com.example.gestiongastos.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** Idiomas disponibles dentro de la app (se elige en Ajustes). */
enum class AppLanguage(val code: String, val label: String) {
    ES("es", "Español"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code == code }

        /** Primer uso: inglés si el teléfono está en inglés; en cualquier otro caso, español. */
        fun systemDefault(): AppLanguage =
            if (Locale.getDefault().language == "en") EN else ES
    }
}

object I18n {
    /** Idioma actual. Es estado de Compose: al cambiarlo, la interfaz se vuelve a dibujar. */
    var language by mutableStateOf(AppLanguage.ES)

    /** Pide reabrir Ajustes tras cambiar el idioma (la app se redibuja completa al cambiarlo). */
    var reopenSettings = false
}

/** Texto en el idioma actual: tr("Guardar", "Save"). */
fun tr(es: String, en: String): String = if (I18n.language == AppLanguage.EN) en else es

/** Locale para fechas y nombres de meses en el idioma actual. */
fun appLocale(): Locale = if (I18n.language == AppLanguage.EN) Locale.US else Locale("es", "ES")
