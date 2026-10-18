package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.privacyBlur
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.R
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import com.example.gestiongastos.ui.viewmodel.SummaryUiState
import kotlinx.coroutines.delay
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun QuickExpenseCoverScreen(
    summaryState: SummaryUiState,
    allCards: List<CreditCardEntity>,
    selectedYearMonth: YearMonth,
    isPrivacyMode: Boolean,
    recentExpenses: List<ExpenseEntity> = emptyList(),
    onSaveExpense: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean, currency: String) -> Unit,
    onSaveCardPurchase: (cardId: Long, concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit,
    onOpenFullApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = isAppDark()

    // 0 = Vista Capy / Estado, 1 = Registrar Gasto
    var currentTab by remember { mutableIntStateOf(0) }

    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("COP") }
    var isCreditCardMode by remember { mutableStateOf(false) }
    var selectedCardId by remember(allCards) { mutableLongStateOf(allCards.firstOrNull()?.id ?: 0L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successFeedback by remember { mutableStateOf<String?>(null) }

    val monthName = remember(selectedYearMonth) {
        selectedYearMonth.month
            .getDisplayName(TextStyle.SHORT, appLocale())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(appLocale()) else it.toString() }
    }

    // Conceptos frecuentes basados en el historial propio del usuario (sin categorías fijas)
    val userFrequentConcepts = remember(recentExpenses) {
        recentExpenses
            .map { it.concept.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(6)
    }

    LaunchedEffect(successFeedback) {
        if (successFeedback != null) {
            delay(2800)
            successFeedback = null
        }
    }

    // Evaluación del estado del Capibara según la salud del presupuesto
    val capyMood = remember(summaryState.freeAmount, summaryState.spentPercentage) {
        when {
            summaryState.freeAmount < 0 -> CapyMoodState.BROKE
            summaryState.spentPercentage >= 0.90f -> CapyMoodState.WARNING
            summaryState.spentPercentage >= 0.70f -> CapyMoodState.CAUTIOUS
            else -> CapyMoodState.HAPPY
        }
    }

    val capyDrawable = when (capyMood) {
        CapyMoodState.HAPPY -> R.drawable.capy_rich
        CapyMoodState.CAUTIOUS -> R.drawable.capy_savings
        CapyMoodState.WARNING -> R.drawable.capy_question
        CapyMoodState.BROKE -> R.drawable.capy_poor
    }

    val capyMessage = when (capyMood) {
        CapyMoodState.HAPPY -> tr("¡Finanzas saludables! Capy está feliz 🍊", "Healthy finances! Capy is happy 🍊")
        CapyMoodState.CAUTIOUS -> tr("Vas bien, pero modera los antojitos 🪙", "You're doing well, but easy on the treats 🪙")
        CapyMoodState.WARNING -> tr("¡Cuidado! Te queda poco presupuesto 👀", "Careful! You're running low on budget 👀")
        CapyMoodState.BROKE -> tr("¡Emergencia en la alcancía! 🚨", "Piggy bank emergency! 🚨")
    }

    val handleSave = {
        val parsedAmount = parseAmount(amountText)
        when {
            concept.isBlank() -> {
                errorMessage = tr("Escribe un concepto", "Enter a name")
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            parsedAmount == null || parsedAmount <= 0.0 -> {
                errorMessage = tr("Ingresa un monto válido", "Enter a valid amount")
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            isCreditCardMode && (selectedCardId == 0L || allCards.isEmpty()) -> {
                errorMessage = tr("Selecciona una tarjeta", "Select a card")
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> {
                errorMessage = null
                val saveDate = calculateSaveDateForMonth(selectedYearMonth)
                if (isCreditCardMode) {
                    onSaveCardPurchase(
                        selectedCardId,
                        concept.trim(),
                        parsedAmount,
                        1,
                        saveDate,
                        true,
                        false,
                        currency
                    )
                } else {
                    onSaveExpense(
                        concept.trim(),
                        parsedAmount,
                        saveDate,
                        false,
                        currency
                    )
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                keyboardController?.hide()
                successFeedback = tr("¡Gasto de ${if (currency == "USD") "USD $$parsedAmount" else formatCurrency(parsedAmount)} guardado!", "Expense of ${if (currency == "USD") "USD $$parsedAmount" else formatCurrency(parsedAmount)} saved!")
                concept = ""
                amountText = ""
                // Vuelve a la pestaña de estado para ver el balance actualizado
                currentTab = 0
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Compacto (Marca CapyCoin + Mes + Botón Pantalla Completa)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.capy_logo),
                        contentDescription = "CapyCoin Logo",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column {
                        Text(
                            text = "CapyCoin Flip",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$monthName ${selectedYearMonth.year}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenFullApp()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = tr("Ver app completa", "Open full app"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Selector de Pestañas (🐹 Estado vs ⚡ Registrar)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pestaña Estado
                    val isTab0 = currentTab == 0
                    val tab0Bg by animateColorAsState(
                        targetValue = if (isTab0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab0Bg"
                    )
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = tab0Bg,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = 0
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isTab0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = tr("Mi Capy", "My Capy"),
                                fontSize = 11.5.sp,
                                fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Pestaña Registrar
                    val isTab1 = currentTab == 1
                    val tab1Bg by animateColorAsState(
                        targetValue = if (isTab1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab1Bg"
                    )
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = tab1Bg,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentTab = 1
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isTab1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = tr("Gasto Rápido", "Quick expense"),
                                fontSize = 11.5.sp,
                                fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTab1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Banner de Feedback de Éxito
            val successColor = FinanceColors.incomeText(isDark)
            val successContainer = FinanceColors.incomeContainer(isDark)
            AnimatedVisibility(
                visible = successFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = successContainer,
                    border = BorderStroke(1.dp, successColor.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = successColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = successFeedback ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = successColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // CONTENIDO DE LA PESTAÑA SELECCIONADA
            if (currentTab == 0) {
                // ==========================================
                // PESTAÑA 0: ESTADO GLANCEABLE & CAPY MASCOT
                // ==========================================

                // Hero Card del Capibara con Globo de Diálogo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.5f else 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = capyDrawable),
                            contentDescription = "Capy Mascot",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Burbuja de diálogo estilo cómic
                            Surface(
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                                color = FinanceColors.goldContainer(isDark).copy(alpha = if (isDark) 0.7f else 0.85f),
                                border = BorderStroke(1.dp, FinanceColors.goldAccent(isDark).copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = capyMessage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FinanceColors.goldText(isDark),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    lineHeight = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = tr("Disponible este mes:", "Available this month:"),
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = privateText(formatCurrency(summaryState.freeAmount), isPrivacyMode, "••••••"),
                                modifier = Modifier.privacyBlur(isPrivacyMode),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (summaryState.freeAmount >= 0) FinanceColors.incomeText(isDark) else FinanceColors.expenseText(isDark)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bento de Métricas Rápidas (Gastado vs Ingresos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gastado
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = FinanceColors.expenseText(isDark),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Gastado", "Spent"),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = privateText(formatCurrency(summaryState.totalExpenses), isPrivacyMode, "••••"),
                                modifier = Modifier.privacyBlur(isPrivacyMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinanceColors.expenseText(isDark)
                            )
                        }
                    }

                    // Ingresos
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = FinanceColors.incomeText(isDark),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Ingresos", "Income"),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = privateText(formatCurrency(summaryState.totalIncome), isPrivacyMode, "••••"),
                                modifier = Modifier.privacyBlur(isPrivacyMode),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinanceColors.incomeText(isDark)
                            )
                        }
                    }
                }

                // Mini barra de progreso del presupuesto
                if (summaryState.totalIncome > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = tr("Presupuesto utilizado:", "Budget used:"),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(summaryState.spentPercentage * 100).toInt()}%",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summaryState.spentPercentage >= 0.90f) FinanceColors.expenseText(isDark) else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { summaryState.spentPercentage.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (summaryState.spentPercentage >= 0.90f) FinanceColors.expenseText(isDark) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Últimos 2 gastos registrados (si hay datos)
                if (recentExpenses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.3f else 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = tr("Últimos registros:", "Latest entries:"),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            recentExpenses.take(2).forEach { exp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exp.concept,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = privateText(if (exp.currency == "USD") formatUsd(exp.amount) else formatCurrency(exp.amount), isPrivacyMode, "••••"),
                                        modifier = Modifier.privacyBlur(isPrivacyMode),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceColors.expenseText(isDark)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón CTA para registrar gasto
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentTab = 1
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Registrar Nuevo Gasto", "Add new expense"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                TextButton(
                    onClick = onOpenFullApp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr("Abrir vista completa de CapyCoin ↗", "Open full CapyCoin view ↗"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

            } else {
                // ==========================================
                // PESTAÑA 1: REGISTRAR GASTO EXPRESS
                // ==========================================

                // Display del Monto y Selector COP / USD
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.5f else 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("Monto a registrar", "Amount to add"),
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val parsed = parseAmount(amountText)
                            Text(
                                text = if (parsed != null && parsed > 0) {
                                    if (currency == "USD") "USD $$parsed" else formatCurrency(parsed)
                                } else "$ 0",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (parsed != null && parsed > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Selector de Moneda COP / USD
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (currency == "COP") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { currency = "COP" }
                                ) {
                                    Text(
                                        text = "COP",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currency == "COP") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (currency == "USD") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { currency = "USD" }
                                ) {
                                    Text(
                                        text = "USD",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currency == "USD") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botones de Incremento Rápido de Monto (+5k, +10k, +20k, +50k, Limpiar)
                // Permite sumar montos comunes sin necesidad de desplegar el teclado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val increments = if (currency == "COP") {
                        listOf(5000, 10000, 20000, 50000)
                    } else {
                        listOf(5, 10, 20, 50)
                    }

                    increments.forEach { inc ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val cur = parseAmount(amountText) ?: 0.0
                                    val newAmount = (cur + inc).toLong()
                                    amountText = newAmount.toString()
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = if (currency == "COP") "+${inc / 1000}k" else "+$$inc",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Botón para limpiar monto
                    if (amountText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    amountText = ""
                                }
                        ) {
                            Text(
                                text = tr("Borrar", "Delete"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Campo Manual de Monto (por si el valor es exacto)
                AmountTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = "Monto exacto",
                    currency = currency
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Campo de Concepto (Libre, sin categorías fijas)
                OutlinedTextField(
                    value = concept,
                    onValueChange = {
                        concept = it
                        errorMessage = null
                    },
                    label = { Text(tr("Concepto (ej. Taxi, Almuerzo, Cine)", "Name (e.g. Taxi, Lunch, Movies)"), fontSize = 11.5.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (concept.isNotEmpty()) {
                            IconButton(onClick = { concept = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = tr("Limpiar", "Clear"), modifier = Modifier.size(15.dp))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleSave() }
                    )
                )

                // Sugerencias de conceptos frecuentes basadas en el historial propio del usuario
                if (userFrequentConcepts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        userFrequentConcepts.forEach { freqConcept ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        concept = freqConcept
                                        errorMessage = null
                                    }
                            ) {
                                Text(
                                    text = freqConcept,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Selector de Medio de Pago (Efectivo/Débito vs Tarjetas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isCreditCardMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (!isCreditCardMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isCreditCardMode = false
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (!isCreditCardMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("Efectivo/Débito", "Cash/Debit"),
                                fontSize = 11.sp,
                                fontWeight = if (!isCreditCardMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isCreditCardMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    allCards.forEach { card ->
                        val isSelectedCard = isCreditCardMode && selectedCardId == card.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelectedCard) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelectedCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isCreditCardMode = true
                                    selectedCardId = card.id
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isSelectedCard) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = card.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelectedCard) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelectedCard) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        color = FinanceColors.expenseText(isDark),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Guardar Gasto
                Button(
                    onClick = handleSave,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Guardar Gasto", "Save expense"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Botón Volver al Capy / Estado
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentTab = 0
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Volver al resumen de Capy", "Back to Capy's summary"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private enum class CapyMoodState {
    HAPPY,
    CAUTIOUS,
    WARNING,
    BROKE
}
