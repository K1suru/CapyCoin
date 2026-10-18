package com.example.gestiongastos.ui.theme

import com.example.gestiongastos.i18n.tr
import androidx.compose.ui.graphics.Color

/** Colores disponibles para las tarjetas de crédito (degradado de inicio a fin). */
data class CardPalette(val nameEs: String, val nameEn: String, val start: Color, val end: Color) {
    val name: String get() = tr(nameEs, nameEn)
}

val CardPalettes = listOf(
    CardPalette("Morado", "Purple", Color(0xFF6A1B9A), Color(0xFFAB47BC)),
    CardPalette("Azul", "Blue", Color(0xFF0D47A1), Color(0xFF42A5F5)),
    CardPalette("Turquesa", "Teal", Color(0xFF00695C), Color(0xFF26A69A)),
    CardPalette("Coral", "Coral", Color(0xFFBF360C), Color(0xFFFF7043)),
    CardPalette("Grafito", "Graphite", Color(0xFF263238), Color(0xFF607D8B)),
    CardPalette("Rosa", "Pink", Color(0xFFAD1457), Color(0xFFEC407A)),
    CardPalette("Dorado", "Gold", Color(0xFF8D6E00), Color(0xFFD4A017)),
    CardPalette("Negra", "Black", Color(0xFF0B0B0F), Color(0xFF3A3A44)),
    CardPalette("Rojo", "Red", Color(0xFF8E0000), Color(0xFFE53935)),
    CardPalette("Índigo", "Indigo", Color(0xFF1A237E), Color(0xFF7986CB)),
    CardPalette("Esmeralda", "Emerald", Color(0xFF1B5E20), Color(0xFF66BB6A)),
    CardPalette("Atardecer", "Sunset", Color(0xFFE65100), Color(0xFFFFB300))
)

/** Índice de color automático (el que tenían las tarjetas antes de poder elegirlo). */
fun autoCardColorIndex(cardId: Long): Int = Math.floorMod(cardId, 7L).toInt()

/** Color de la tarjeta: el elegido por el usuario o, si no eligió (-1), el automático. */
fun cardPaletteFor(cardId: Long, colorIndex: Int): CardPalette =
    CardPalettes.getOrNull(colorIndex) ?: CardPalettes[autoCardColorIndex(cardId)]
