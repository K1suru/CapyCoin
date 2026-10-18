package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.privacyBlur
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.R
import com.example.gestiongastos.data.local.SavingsEntity
import com.example.gestiongastos.ui.components.AutoResizedText
import com.example.gestiongastos.ui.components.SavingsManagerDialog
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.navigation.Screen
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import com.example.gestiongastos.ui.viewmodel.SummaryUiState
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import com.example.gestiongastos.ui.components.CapyMood
import com.example.gestiongastos.ui.components.CapyOnsen
import com.example.gestiongastos.ui.components.capyMoodFor
import com.example.gestiongastos.ui.components.CapyTipCard
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.buildCapyTips
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import com.example.gestiongastos.data.model.payableThisMonth
import androidx.compose.ui.graphics.Brush
import java.time.LocalDate
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
                    subtitle = tr("${((summaryState.directExpenses / totalCombined) * 100).toInt()}% del total", "${((summaryState.directExpenses / totalCombined) * 100).toInt()}% of total"),
                    amount = summaryState.directExpenses,
                    accentColor = FinanceColors.expenseText(isDark),
                    containerColor = FinanceColors.expenseContainer(isDark),
                    progress = (summaryState.directExpenses / totalCombined).toFloat(),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { onNavigateToScreen(Screen.Gastos) }
                )

                CapyBreakdownBentoItem(
                    avatarPainter = painterResource(id = R.drawable.capy_logo),
                    title = tr("Cuotas Tarjetas de Crédito", "Credit card installments"),
                    subtitle = tr("${((summaryState.creditCardExpenses / totalCombined) * 100).toInt()}% del total", "${((summaryState.creditCardExpenses / totalCombined) * 100).toInt()}% of total"),
                    amount = summaryState.creditCardExpenses,
                    accentColor = FinanceColors.cardText(isDark),
                    containerColor = FinanceColors.cardContainer(isDark),
                    progress = (summaryState.creditCardExpenses / totalCombined).toFloat(),
                    isPrivacyMode = isPrivacyMode,
                    onClick = { onNavigateToScreen(Screen.Tarjetas) }
                )

                CapyBreakdownBentoItem(
                    avatarPainter = painterResource(id = R.drawable.capy_savings),
                    title = tr("Ahorros del Mes", "Savings this month"),
                    subtitle = tr("${((summaryState.totalSavings / totalCombined) * 100).toInt()}% del total", "${((summaryState.totalSavings / totalCombined) * 100).toInt()}% of total"),
                    amount = summaryState.totalSavings,
                    accentColor = FinanceColors.savingsText(isDark),
                    containerColor = FinanceColors.savingsContainer(isDark),
                    progress = (summaryState.totalSavings / totalCombined).toFloat(),
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

@Composable
fun CircularSpentProgress(
    spentPercentage: Float,
    directExpenses: Double = 0.0,
    creditCardExpenses: Double = 0.0,
    totalExpenses: Double,
    totalSavings: Double,
    totalIncome: Double,
    freeAmount: Double = 0.0,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isDark = isAppDark()
    val animatedPercentage by animateFloatAsState(
        targetValue = spentPercentage,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "percentageAnimation"
    )
    val displayPercentage = (animatedPercentage * 100).toInt()
    val animatedFree = rememberAnimatedAmount(freeAmount)
    val animatedOutflow = rememberAnimatedAmount(totalExpenses + totalSavings)
    val animatedIncome = rememberAnimatedAmount(totalIncome)
    val mood = capyMoodFor(spentPercentage)
    val moodColor = when (mood) {
        CapyMood.RELAXED -> FinanceColors.incomeText(isDark)
        CapyMood.CAUTIOUS -> FinanceColors.goldText(isDark)
        CapyMood.WORRIED -> FinanceColors.savingsText(isDark)
        CapyMood.PANIC -> FinanceColors.expenseText(isDark)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Onsen del capibara: el agua sube con el % de ingresos usado
        CapyOnsen(
            spentFraction = spentPercentage,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$displayPercentage%",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                color = moodColor,
                fontSize = 40.sp
            )
        )
        Text(
            text = tr("de tus ingresos usados", "of your income used"),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.message,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = moodColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Saldo Disponible Pill Badge
        val isPositiveFree = freeAmount >= 0
        Surface(
            shape = CircleShape,
            color = if (isPositiveFree) FinanceColors.incomeContainer(isDark).copy(alpha = 0.7f) else FinanceColors.expenseContainer(isDark).copy(alpha = 0.7f),
            border = BorderStroke(1.dp, if (isPositiveFree) FinanceColors.incomeAccent(isDark).copy(alpha = 0.4f) else FinanceColors.expenseAccent(isDark).copy(alpha = 0.4f)),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isPositiveFree) tr("Saldo Libre: ", "Free balance:") else tr("Déficit: ", "Deficit:"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = privateText(formatCurrency(animatedFree), isPrivacyMode, "$ ••••••"),
                    modifier = Modifier.privacyBlur(isPrivacyMode),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPositiveFree) FinanceColors.incomeText(isDark) else FinanceColors.expenseText(isDark)
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = FinanceColors.expenseContainer(isDark).copy(alpha = 0.6f),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = privateText(tr("Gastos: ${formatCurrency(animatedOutflow)}", "Expenses: ${formatCurrency(animatedOutflow)}"), isPrivacyMode, tr("Gastos: $ ••••••", "Expenses: $ ••••••")),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FinanceColors.expenseText(isDark),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.privacyBlur(isPrivacyMode).padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = FinanceColors.incomeContainer(isDark).copy(alpha = 0.6f),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = privateText(tr("Ingresos: ${formatCurrency(animatedIncome)}", "Income: ${formatCurrency(animatedIncome)}"), isPrivacyMode, tr("Ingresos: $ ••••••", "Income: $ ••••••")),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FinanceColors.incomeText(isDark),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.privacyBlur(isPrivacyMode).padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CapyMetricItem(
    title: String,
    amount: Double,
    painter: Painter,
    accentColor: Color,
    containerColor: Color,
    isPrivacyMode: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Capybara Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            AutoResizedText(
                text = privateText(formatCurrency(rememberAnimatedAmount(amount)), isPrivacyMode, "$ •••"),
                isPrivate = isPrivacyMode,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CapyBreakdownBentoItem(
    avatarPainter: Painter,
    title: String,
    subtitle: String,
    amount: Double,
    accentColor: Color,
    containerColor: Color,
    progress: Float,
    isPrivacyMode: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = containerColor.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            avatarPainter,
                            contentDescription = title,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AutoResizedText(
                        text = privateText(formatCurrency(rememberAnimatedAmount(amount)), isPrivacyMode, "$ ••••••"),
                        isPrivate = isPrivacyMode,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    )
                    if (onClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Minimalist Rounded Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = accentColor,
                trackColor = containerColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Avisos de tarjetas por pagar en el mes actual: "Visa · paga en 3 días · $ 450.000".
 * Solo aparecen las que tienen día de pago, cobro este mes y no están marcadas como pagadas.
 */
@Composable
fun PaymentReminders(
    cardSummaries: List<CardMonthSummary>,
    paidCardIds: Set<Long>,
    ignoredKeys: Set<String>,
    usdRate: Double,
    selectedYearMonth: YearMonth,
    isPrivacyMode: Boolean,
    onOpenCards: () -> Unit
) {
    val today = LocalDate.now()
    if (selectedYearMonth != YearMonth.from(today)) return
    val isDark = isAppDark()

    val reminders = cardSummaries
        .filter { it.paymentDay in 1..31 && it.cardId !in paidCardIds }
        .map { it to it.payableThisMonth(usdRate, ignoredKeys) }
        .filter { (_, amount) -> amount > 0.0 }
        .map { (summary, amount) ->
            val dueDay = summary.paymentDay.coerceAtMost(selectedYearMonth.lengthOfMonth())
            Triple(summary, amount, dueDay - today.dayOfMonth)
        }
        .sortedBy { it.third }

    if (reminders.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reminders.forEach { (summary, amount, daysLeft) ->
            val isLate = daysLeft < 0
            val isUrgent = daysLeft in 0..3
            val (container, accent) = when {
                isLate -> FinanceColors.expenseContainer(isDark) to FinanceColors.expenseText(isDark)
                isUrgent -> FinanceColors.goldContainer(isDark) to FinanceColors.goldText(isDark)
                else -> FinanceColors.cardContainer(isDark) to FinanceColors.cardText(isDark)
            }
            val whenText = when {
                isLate -> tr("venció hace ${-daysLeft} día(s)", "overdue by ${-daysLeft} day(s)")
                daysLeft == 0 -> tr("¡paga hoy!", "pay today!")
                daysLeft == 1 -> tr("paga mañana", "pay tomorrow")
                else -> tr("paga en $daysLeft días", "pay in $daysLeft days")
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = container,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenCards() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (gradientStart, gradientEnd) = cardGradient(summary.cardId, summary.colorIndex)
                    Box(
                        modifier = Modifier
                            .size(width = 34.dp, height = 24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.cardName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = accent,
                            maxLines = 1
                        )
                        Text(
                            text = tr("Día ${summary.paymentDay} · $whenText", "Day ${summary.paymentDay} · $whenText"),
                            style = MaterialTheme.typography.labelMedium,
                            color = accent
                        )
                    }
                    Text(
                        text = privateText(formatCurrency(amount), isPrivacyMode),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = accent,
                        modifier = Modifier.privacyBlur(isPrivacyMode)
                    )
                }
            }
        }
    }
}
