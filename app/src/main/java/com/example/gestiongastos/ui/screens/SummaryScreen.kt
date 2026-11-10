package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.data.model.monthUsesUsd
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.R
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.ui.components.SavingsManagerDialog
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.navigation.Screen
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import com.example.gestiongastos.ui.viewmodel.SummaryUiState
import com.example.gestiongastos.ui.components.CapyTipCard
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.buildCapyTips
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.model.safePercent
import com.example.gestiongastos.data.model.safeRatio
import java.time.YearMonth

@Composable
fun SummaryScreen(
    summaryState: SummaryUiState,
    monthlySavings: List<SavingsEntity>,
    selectedYearMonth: YearMonth,
    ignoredKeys: Set<String> = emptySet(),
    cardSummaries: List<CardMonthSummary> = emptyList(),
    monthlyExpenses: List<com.example.gestiongastos.data.local.ExpenseEntity> = emptyList(),
    paidCardIds: Set<Long> = emptySet(),
    usdRate: Double = DEFAULT_USD_RATE,
    isUsdRateOutdated: Boolean = false,
    isAllPaid: Boolean = false,
    isPrivacyMode: Boolean = false,
    onNavigateToScreen: (Screen) -> Unit,
    onAddSavings: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean) -> Unit,
    onUpdateSavings: (SavingsEntity) -> Unit,
    onDeleteSavings: (SavingsEntity) -> Unit,
    onEndSavingFromMonth: (SavingsEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showSavingsDialog by remember { mutableStateOf(false) }
    val isDark = isAppDark()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimalist Brand Pill Header
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.5f else 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.capy_logo),
                    contentDescription = "CapyCoin",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CapyCoin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // Celebration Card when all debts/expenses are 100% paid
        if (isAllPaid && (summaryState.directExpenses > 0 || summaryState.creditCardExpenses > 0 || summaryState.totalExpenses > 0)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FinanceColors.incomeContainer(isDark).copy(alpha = if (isDark) 0.6f else 0.45f)
                ),
                border = BorderStroke(1.5.dp, FinanceColors.goldAccent(isDark)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.capy_rich),
                        contentDescription = tr("Mes 100% Pagado", "Month 100% paid"),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr("¡Mes 100% Liquidado! 👑✨", "Month 100% paid off! 👑✨"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.incomeText(isDark)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tr("Has pagado todos tus gastos y tarjetas de este mes. ¡Nivel maestro del Capibara!", "You've paid all your expenses and cards this month. Capybara master level!"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Recordatorios de pago de tarjetas (solo en el mes actual)
        PaymentReminders(
            cardSummaries = cardSummaries,
            paidCardIds = paidCardIds,
            ignoredKeys = ignoredKeys,
            usdRate = usdRate,
            selectedYearMonth = selectedYearMonth,
            isPrivacyMode = isPrivacyMode,
            onOpenCards = { onNavigateToScreen(Screen.Tarjetas) }
        )

        // Hero Circular Donut Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            CircularSpentProgress(
                spentPercentage = summaryState.spentPercentage,
                directExpenses = summaryState.directExpenses,
                creditCardExpenses = summaryState.creditCardExpenses,
                totalExpenses = summaryState.totalExpenses,
                totalSavings = summaryState.totalSavings,
                totalIncome = summaryState.totalIncome,
                freeAmount = summaryState.freeAmount,
                isPrivacyMode = isPrivacyMode,
                onClick = { onNavigateToScreen(Screen.Gastos) },
                modifier = Modifier
                    .capyPattern(FinanceColors.goldAccent(isDark).copy(alpha = 0.09f))
                    .padding(vertical = 18.dp, horizontal = 12.dp)
            )
        }

        // Aviso discreto: la TRM no se pudo actualizar y este mes hay montos en dólares
        if (isUsdRateOutdated && monthUsesUsd(monthlyExpenses, cardSummaries)) {
            Text(
                text = tr(
                    "No se pudo actualizar la TRM: los montos en dólares usan la última conocida (${formatCurrency(usdRate)} por USD).",
                    "Couldn't update the exchange rate: dollar amounts use the last known one (${formatCurrency(usdRate)} per USD)."
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        // Capy-consejo con los datos reales del mes
        val capyTips = remember(summaryState, monthlyExpenses, cardSummaries, paidCardIds, ignoredKeys, usdRate, selectedYearMonth, isPrivacyMode) {
            buildCapyTips(
                summary = summaryState,
                expenses = monthlyExpenses,
                cardSummaries = cardSummaries,
                paidCardIds = paidCardIds,
                ignoredKeys = ignoredKeys,
                usdRate = usdRate,
                selectedYearMonth = selectedYearMonth,
                money = { amount -> if (isPrivacyMode) "$ •••" else formatCurrency(amount) }
            )
        }
        CapyTipCard(tips = capyTips)

        // 4 Quick-Action Capybara Bento Capsules
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapyMetricItem(
                title = tr("Ingresos", "Income"),
                amount = summaryState.totalIncome,
                painter = painterResource(id = R.drawable.capy_rich),
                accentColor = FinanceColors.incomeText(isDark),
                containerColor = FinanceColors.incomeContainer(isDark),
                isPrivacyMode = isPrivacyMode,
                onClick = { onNavigateToScreen(Screen.Ingresos) },
                modifier = Modifier.weight(1f)
            )

            CapyMetricItem(
                title = tr("Gastos", "Expenses"),
                amount = summaryState.totalExpenses,
                painter = painterResource(id = R.drawable.capy_poor),
                accentColor = FinanceColors.expenseText(isDark),
                containerColor = FinanceColors.expenseContainer(isDark),
                isPrivacyMode = isPrivacyMode,
                onClick = { onNavigateToScreen(Screen.Gastos) },
                modifier = Modifier.weight(1f)
            )

            CapyMetricItem(
                title = tr("Ahorros", "Savings"),
                amount = summaryState.totalSavings,
                painter = painterResource(id = R.drawable.capy_savings),
                accentColor = FinanceColors.savingsText(isDark),
                containerColor = FinanceColors.savingsContainer(isDark),
                isPrivacyMode = isPrivacyMode,
                onClick = { showSavingsDialog = true },
                modifier = Modifier.weight(1f)
            )

            val isPositiveFree = summaryState.freeAmount >= 0
            CapyMetricItem(
                title = tr("Libre", "Free"),
                amount = summaryState.freeAmount,
                painter = painterResource(id = R.drawable.capy_question),
                accentColor = if (isPositiveFree) FinanceColors.goldText(isDark) else FinanceColors.expenseText(isDark),
                containerColor = if (isPositiveFree) FinanceColors.goldContainer(isDark) else FinanceColors.expenseContainer(isDark),
                isPrivacyMode = isPrivacyMode,
                onClick = { onNavigateToScreen(Screen.Ingresos) },
                modifier = Modifier.weight(1f)
            )
        }

        // Minimalist Bento "Desglose del mes" with Capybara Mascots
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Desglose del mes", "Monthly breakdown"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = tr("Por categorías", "By category"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                val totalCombined = (summaryState.directExpenses + summaryState.creditCardExpenses + summaryState.totalSavings).coerceAtLeast(0.01)

                CapyBreakdownBentoItem(
                    avatarPainter = painterResource(id = R.drawable.capy_poor),
                    title = tr("Gastos Fijos / Directos", "Fixed / direct expenses"),
                    subtitle = tr("${safePercent(summaryState.directExpenses, totalCombined)}% del total", "${safePercent(summaryState.directExpenses, totalCombined)}% of total"),
                    amount = summaryState.directExpenses,
                    accentColor = FinanceColors.expenseText(isDark),
                    containerColor = FinanceColors.expenseContainer(isDark),
                    progress = safeRatio(summaryState.directExpenses, totalCombined).toFloat(),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { onNavigateToScreen(Screen.Gastos) }
                )

                CapyBreakdownBentoItem(
                    avatarPainter = painterResource(id = R.drawable.capy_logo),
                    title = tr("Cuotas Tarjetas de Crédito", "Credit card installments"),
                    subtitle = tr("${safePercent(summaryState.creditCardExpenses, totalCombined)}% del total", "${safePercent(summaryState.creditCardExpenses, totalCombined)}% of total"),
                    amount = summaryState.creditCardExpenses,
                    accentColor = FinanceColors.cardText(isDark),
                    containerColor = FinanceColors.cardContainer(isDark),
                    progress = safeRatio(summaryState.creditCardExpenses, totalCombined).toFloat(),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { onNavigateToScreen(Screen.Tarjetas) }
                )

                CapyBreakdownBentoItem(
                    avatarPainter = painterResource(id = R.drawable.capy_savings),
                    title = tr("Ahorros del Mes", "Savings this month"),
                    subtitle = tr("${safePercent(summaryState.totalSavings, totalCombined)}% del total", "${safePercent(summaryState.totalSavings, totalCombined)}% of total"),
                    amount = summaryState.totalSavings,
                    accentColor = FinanceColors.savingsText(isDark),
                    containerColor = FinanceColors.savingsContainer(isDark),
                    progress = safeRatio(summaryState.totalSavings, totalCombined).toFloat(),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { showSavingsDialog = true }
                )
            }
        }
    }

    if (showSavingsDialog) {
        SavingsManagerDialog(
            savingsList = monthlySavings,
            selectedYearMonth = selectedYearMonth,
            ignoredKeys = ignoredKeys,
            isPrivacyMode = isPrivacyMode,
            onEndSavingFromMonth = onEndSavingFromMonth,
            onDismiss = { showSavingsDialog = false },
            onAddSavings = onAddSavings,
            onUpdateSavings = onUpdateSavings,
            onDeleteSavings = onDeleteSavings
        )
    }
}
