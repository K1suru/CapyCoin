package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.ItemKeys
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import com.example.gestiongastos.ui.viewmodel.SummaryUiState
import java.time.LocalDate
import java.time.YearMonth

data class CapyTip(val text: String, val face: CapyFace)

/** Arma consejos tiernos con los datos reales del mes. */
fun buildCapyTips(
    summary: SummaryUiState,
    expenses: List<ExpenseEntity>,
    cardSummaries: List<CardMonthSummary>,
    paidCardIds: Set<Long>,
    ignoredKeys: Set<String>,
    usdRate: Double,
    selectedYearMonth: YearMonth,
    money: (Double) -> String
): List<CapyTip> {
    val tips = mutableListOf<CapyTip>()

    if (summary.totalIncome > 0 || summary.totalExpenses > 0) {
        if (summary.freeAmount < 0) {
            tips += CapyTip(tr("Este mes sale ${money(-summary.freeAmount)} más de lo que entra. ¿Revisamos juntos los gastos? 🧡", "This month ${money(-summary.freeAmount)} more goes out than comes in. Shall we review the expenses together? 🧡"), CapyFace.WORRIED)
        } else {
            tips += CapyTip(tr("¡Te quedan ${money(summary.freeAmount)} libres este mes! Estoy orgulloso de ti 🍊", "You have ${money(summary.freeAmount)} left this month! I'm proud of you 🍊"), CapyFace.HAPPY)
        }
    }

    // Tarjetas que vencen pronto (solo en el mes actual)
    val today = LocalDate.now()
    if (selectedYearMonth == YearMonth.from(today)) {
        cardSummaries
            .filter { it.paymentDay in 1..31 && it.cardId !in paidCardIds && it.payableThisMonth(usdRate, ignoredKeys) > 0 }
            .map { it to it.paymentDay.coerceAtMost(selectedYearMonth.lengthOfMonth()) - today.dayOfMonth }
            .filter { (_, days) -> days in 0..5 }
            .minByOrNull { it.second }
            ?.let { (card, days) ->
                val cuando = when (days) { 0 -> "hoy"; 1 -> tr("mañana", "tomorrow"); else -> tr("en $days días", "in $days days") }
                tips += CapyTip(tr("No olvides pagar ${card.cardName} $cuando 💳 ¡Yo te recuerdo!", "Don't forget to pay ${card.cardName} $cuando 💳 I'll remind you!"), CapyFace.WORRIED)
            }
    }

    val counted = expenses.filter { ItemKeys.direct(it.id) !in ignoredKeys }
    fun inCop(e: ExpenseEntity) = if (e.currency == "USD") e.amount * usdRate else e.amount
    counted.maxByOrNull { inCop(it) }?.let { biggest ->
        tips += CapyTip(tr("Tu gasto fijo más grande del mes es «${biggest.concept}» con ${money(inCop(biggest))}.", "Your biggest fixed expense this month is “${biggest.concept}” at ${money(inCop(biggest))}."), CapyFace.SHINY)
    }
    val dayToDay = counted.filter { !it.isRecurring && it.installments == 0 }
    if (dayToDay.isNotEmpty()) {
        tips += CapyTip(tr("Llevas ${dayToDay.size} gasto(s) del día a día, en total ${money(dayToDay.sumOf { inCop(it) })}. ¡Ojo con los antojitos! 🍡", "You have ${dayToDay.size} everyday expense(s), ${money(dayToDay.sumOf { inCop(it) })} in total. Watch those treats! 🍡"), CapyFace.SHINY)
    }

    tips += if (summary.totalSavings <= 0) {
        CapyTip(tr("Aún no has ahorrado nada este mes. ¡Aunque sea un yuzu cuenta! 🍊", "You haven't saved anything this month yet. Even one yuzu counts! 🍊"), CapyFace.SHINY)
    } else {
        CapyTip(tr("Ya guardaste ${money(summary.totalSavings)} en ahorros este mes. ¡Así se hace! ✨", "You've already saved ${money(summary.totalSavings)} this month. Way to go! ✨"), CapyFace.HAPPY)
    }

    if (tips.isEmpty()) tips += CapyTip(tr("¡Hola! Registra tus ingresos y gastos y te iré dando consejitos 🧡", "Hi! Add your income and expenses and I'll give you little tips 🧡"), CapyFace.HAPPY)
    return tips
}

/** Tarjetita donde el capibara te da un consejo; al tocarla pasa al siguiente. */
@Composable
fun CapyTipCard(tips: List<CapyTip>, modifier: Modifier = Modifier) {
    if (tips.isEmpty()) return
    val isDark = isAppDark()
    var index by remember { mutableIntStateOf(0) }
    val tip = tips[index % tips.size]

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = FinanceColors.goldContainer(isDark),
        border = BorderStroke(1.dp, FinanceColors.goldAccent(isDark).copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { index = (index + 1) % tips.size }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ilustración del capibara según el ánimo del consejo
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(FinanceColors.savingsContainer(isDark)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = tip.face, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tipFace") { face ->
                    Image(
                        painter = painterResource(id = face.drawableRes()),
                        contentDescription = tr("Capy-consejo", "Capy tip"),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tr("Capy-consejo", "Capy tip"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceColors.goldText(isDark)
                )
                AnimatedContent(targetState = tip.text, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tipText") { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (tips.size > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        tips.indices.forEach { i ->
                            val selected = i == index % tips.size
                            if (selected) {
                                PawPrint(FinanceColors.goldAccent(isDark), modifier = Modifier.size(10.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FinanceColors.goldAccent(isDark).copy(alpha = 0.35f))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Toca para otro", "Tap for another"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
