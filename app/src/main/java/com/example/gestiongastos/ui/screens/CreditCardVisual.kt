package com.example.gestiongastos.ui.screens

import androidx.compose.ui.text.style.TextOverflow
import com.example.gestiongastos.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.data.model.safePercent
import com.example.gestiongastos.ui.components.privacyBlur
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Check
import com.example.gestiongastos.ui.theme.CardPalettes
import com.example.gestiongastos.ui.theme.cardPaletteFor
import androidx.compose.ui.graphics.StrokeCap
import com.example.gestiongastos.ui.components.formatCurrency

// Dibujo de la tarjeta de crédito y selector de su color.
/** Degradado de la tarjeta: el color elegido o, si no hay, el automático según su id. */
fun cardGradient(cardId: Long, colorIndex: Int = -1): Pair<Color, Color> {
    val palette = cardPaletteFor(cardId, colorIndex)
    return palette.start to palette.end
}

/** Vista previa de la tarjeta y círculos para elegir su color. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardColorPicker(
    cardName: String,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val palette = CardPalettes.getOrElse(selectedIndex) { CardPalettes.first() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = tr("Color de la tarjeta: ${palette.name}", "Card color: ${palette.name}"),
            style = MaterialTheme.typography.labelLarge
        )
        // Mini tarjeta de vista previa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(palette.start, palette.end)))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = cardName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(width = 30.dp, height = 22.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFF6D365), Color(0xFFD4A017))))
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardPalettes.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(option.start, option.end)))
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = option.name, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreditCardVisual(
    summary: CardMonthSummary,
    payableThisMonth: Double,
    isPaid: Boolean,
    isPrivacyMode: Boolean,
    onSimulator: () -> Unit,
    onInfo: () -> Unit,
    onTogglePaid: () -> Unit
) {
    val (startColor, endColor) = cardGradient(summary.cardId, summary.colorIndex)
    val animatedPayable = rememberAnimatedAmount(payableThisMonth)
    val onCard = Color.White
    val onCardSoft = Color.White.copy(alpha = 0.78f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(startColor, endColor)))
    ) {
        // Brillo decorativo
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 90f; translationY = -110f }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.cardName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = onCard,
                        maxLines = 1
                    )
                    if (isPaid) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = onCard, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(tr("Pagada este mes", "Paid this month"), style = MaterialTheme.typography.labelSmall, color = onCard)
                        }
                    }
                }
                IconButton(onClick = onSimulator, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Calculate, contentDescription = tr("Simulador de Cuotas", "Installment simulator"), tint = onCard, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onInfo, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Info, contentDescription = tr("Información de la Tarjeta", "Card information"), tint = onCard, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onTogglePaid, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isPaid) tr("Desmarcar tarjeta pagada", "Mark card as unpaid") else tr("Marcar tarjeta como pagada", "Mark card as paid"),
                        tint = onCard,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Chip de la tarjeta
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFF6D365), Color(0xFFD4A017))))
            )

            Column {
                Text(tr("Pago este mes", "Due this month"), style = MaterialTheme.typography.labelSmall, color = onCardSoft)
                Text(
                    text = privateText(formatCurrency(animatedPayable), isPrivacyMode),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = onCard,
                    maxLines = 1,
                    modifier = Modifier.privacyBlur(isPrivacyMode)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (summary.creditLimit > 0.0) {
                    val usedFraction = (summary.currentMonthSpending / summary.creditLimit).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { usedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = onCard,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ocupa el espacio que sobre y se recorta con "…" para no montarse sobre la deuda
                    Text(
                        text = buildString {
                            append(tr("Corte día ${summary.cutoffDay}", "Closes ${summary.cutoffDay}"))
                            if (summary.paymentDay in 1..31) append(tr(" · Pago día ${summary.paymentDay}", " · Due ${summary.paymentDay}"))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = onCardSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
                    )
                    Text(
                        text = if (summary.creditLimit > 0.0) {
                            privateText(tr("Cupo usado ${safePercent(summary.currentMonthSpending, summary.creditLimit)}%", "Limit used ${safePercent(summary.currentMonthSpending, summary.creditLimit)}%"), isPrivacyMode, tr("Cupo usado ••", "Limit used ••"))
                        } else {
                            privateText(tr("Deuda ${formatCurrency(summary.currentMonthSpending)}", "Debt ${formatCurrency(summary.currentMonthSpending)}"), isPrivacyMode, tr("Deuda ••••", "Debt ••••"))
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = onCard,
                        modifier = Modifier.privacyBlur(isPrivacyMode)
                    )
                }
            }
        }
    }
}
