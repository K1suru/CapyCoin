package com.example.gestiongastos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.gestiongastos.R

/**
 * Nunito (Google Fonts, licencia OFL en assets/licenses/Nunito-OFL.txt).
 * Es una fuente variable: un solo archivo con todos los pesos.
 */
@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: FontWeight) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val Nunito = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold),
    nunito(FontWeight.Black)
)

private val Base = Typography()

private fun TextStyle.nunito() = copy(fontFamily = Nunito)

// Toda la tipografía de Material usa Nunito
val Typography = Typography(
    displayLarge = Base.displayLarge.nunito(),
    displayMedium = Base.displayMedium.nunito(),
    displaySmall = Base.displaySmall.nunito(),
    headlineLarge = Base.headlineLarge.nunito(),
    headlineMedium = Base.headlineMedium.nunito(),
    headlineSmall = Base.headlineSmall.nunito(),
    titleLarge = Base.titleLarge.nunito().copy(fontWeight = FontWeight.Bold),
    titleMedium = Base.titleMedium.nunito().copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Base.titleSmall.nunito().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.nunito(),
    bodyMedium = Base.bodyMedium.nunito(),
    bodySmall = Base.bodySmall.nunito(),
    labelLarge = Base.labelLarge.nunito().copy(fontWeight = FontWeight.SemiBold),
    labelMedium = Base.labelMedium.nunito().copy(fontWeight = FontWeight.SemiBold),
    labelSmall = Base.labelSmall.nunito().copy(fontWeight = FontWeight.SemiBold)
)
