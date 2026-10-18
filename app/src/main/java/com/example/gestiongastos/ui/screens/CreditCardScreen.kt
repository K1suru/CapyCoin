package com.example.gestiongastos.ui.screens

import androidx.compose.ui.text.style.TextOverflow
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.ExpenseSortOrder
import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.data.model.installmentSchedule
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.ui.components.privacyBlur
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Check
import com.example.gestiongastos.ui.theme.CardPalettes
import com.example.gestiongastos.ui.theme.autoCardColorIndex
import com.example.gestiongastos.ui.theme.cardPaletteFor
import androidx.compose.ui.graphics.StrokeCap
import com.example.gestiongastos.ui.components.AmountTextField
import com.example.gestiongastos.ui.components.FormSheet
import com.example.gestiongastos.ui.components.rememberDatePicker
import com.example.gestiongastos.ui.components.amountToInput
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.formatUsd
import com.example.gestiongastos.ui.components.parseAmount
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreditCardScreen(
    cardSummaries: List<CardMonthSummary>,
    allCards: List<CreditCardEntity>,
    selectedYearMonth: YearMonth,
    usdRate: Double = 4150.0,
    paidCardIds: Set<Long> = emptySet(),
    ignoredKeys: Set<String> = emptySet(),
    isPrivacyMode: Boolean = false,
    installmentsPosition: InstallmentPosition = InstallmentPosition.LAST,
    expenseSortOrder: ExpenseSortOrder = ExpenseSortOrder.NEWEST_FIRST,
    onTogglePaidCard: (Long) -> Unit = {},
    onAddCard: (name: String, cutoffDay: Int, paymentDay: Int, interestRate: Double, maintenanceFee: Double, currency: String, isFeeWaiverEnabled: Boolean, minSpendForFeeWaiver: Double, isFirstInstallmentInterestFree: Boolean, creditLimit: Double, colorIndex: Int) -> Unit,
    onUpdateCard: (CreditCardEntity) -> Unit,
    onDeleteCard: (CreditCardEntity) -> Unit,
    onAddPurchase: (cardId: Long, concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit,
    onUpdatePurchase: (CardPurchaseEntity) -> Unit,
    onDeletePurchase: (purchaseId: Long) -> Unit,
    onAddPurchasePayment: (purchaseId: Long, amount: Double, dateMillis: Long) -> Unit,
    onOmitPurchaseForMonth: (purchaseId: Long) -> Unit,
    onToggleSettlePurchase: (Long) -> Unit = {},
    onReorderCards: (List<Long>) -> Unit = {},
    modifier: Modifier = Modifier
) {

    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var localCardSummaries by remember { mutableStateOf(cardSummaries) }
    var draggingCardId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val cardWidthsPx = remember { mutableMapOf<Long, Float>() }
    val density = LocalDensity.current
    val cardSpacingPx = remember(density) { with(density) { 8.dp.toPx() } }

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showCardInfoDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CreditCardEntity?>(null) }
    var cardToDelete by remember { mutableStateOf<CreditCardEntity?>(null) }
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToEdit by remember { mutableStateOf<PurchaseInstallmentDetail?>(null) }
    var purchaseToDelete by remember { mutableStateOf<PurchaseInstallmentDetail?>(null) }
    var purchaseToPay by remember { mutableStateOf<PurchaseInstallmentDetail?>(null) }
    var purchaseToSettle by remember { mutableStateOf<PurchaseInstallmentDetail?>(null) }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Keep local list in sync when cardSummaries changes and user is not dragging
    LaunchedEffect(cardSummaries) {
        if (draggingCardId == null) {
            localCardSummaries = cardSummaries
        }
    }

    // Selected card ID defaults to the first card if not set
    val activeCardId = selectedCardId ?: cardSummaries.firstOrNull()?.cardId
    val currentCardSummary = remember(cardSummaries, activeCardId) {
        cardSummaries.find { it.cardId == activeCardId } ?: cardSummaries.firstOrNull()
    }

    LaunchedEffect(currentCardSummary) {
        if (selectedCardId == null && currentCardSummary != null) {
            selectedCardId = currentCardSummary.cardId
        }
    }

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30
        }
    }

    LaunchedEffect(activeCardId) {
        listState.scrollToItem(0)
    }

    // Mismo criterio que el Resumen: los ítems ignorados este mes no suman
    val totalAllCardsMonth = remember(cardSummaries, usdRate, ignoredKeys) {
        cardSummaries.sumOf { it.payableThisMonth(usdRate, ignoredKeys) }
    }

    val isDark = isAppDark()

    Scaffold(
        modifier = modifier,
        // El Scaffold principal ya aplica las barras del sistema; sin esto quedaba un espacio en blanco arriba
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (currentCardSummary != null) {
                FloatingActionButton(
                    onClick = { showAddPurchaseDialog = true },
                    containerColor = FinanceColors.cardAccent(isDark),
                    contentColor = FinanceColors.onAccent(isDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = tr("Nueva Compra", "New purchase"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Registrar Compra", "Add purchase"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Overall Cards Total Banner with Capy Logo Mascot
            AnimatedVisibility(visible = !isScrolled) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = FinanceColors.cardContainer(isDark).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, FinanceColors.cardContainer(isDark)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .capyPattern(FinanceColors.cardAccent(isDark).copy(alpha = 0.10f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.capy_logo),
                                        contentDescription = tr("Capibara Tarjetas", "Cards capybara"),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tr("Tarjetas de ${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }}", "${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }} cards"),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = FinanceColors.cardText(isDark)
                                        ),
                                        maxLines = 2
                                    )
                                    Text(
                                        text = tr("Cuotas activas + Cuotas de manejo", "Active installments + card fees"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.cardAccent(isDark),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            AutoResizedText(
                                text = privateText(formatCurrency(rememberAnimatedAmount(totalAllCardsMonth)), isPrivacyMode, "$ ••••••"),
                                isPrivate = isPrivacyMode,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.cardText(isDark)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Modern Pill-Shaped Card Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (localCardSummaries.isNotEmpty()) {
                    val fromCardIndex = localCardSummaries.indexOfFirst { it.cardId == draggingCardId }
                    val targetCardIndex = remember(draggingCardId, dragOffsetX, fromCardIndex, localCardSummaries) {
                        if (draggingCardId == null || fromCardIndex == -1) {
                            fromCardIndex
                        } else if (dragOffsetX > 0) {
                            var target = fromCardIndex
                            var cumulated = 0f
                            for (i in (fromCardIndex + 1) until localCardSummaries.size) {
                                val w = cardWidthsPx[localCardSummaries[i].cardId] ?: 200f
                                val threshold = cumulated + (w + cardSpacingPx) * 0.5f
                                if (dragOffsetX > threshold) {
                                    target = i
                                    cumulated += (w + cardSpacingPx)
                                } else {
                                    break
                                }
                            }
                            target
                        } else if (dragOffsetX < 0) {
                            var target = fromCardIndex
                            var cumulated = 0f
                            for (i in (fromCardIndex - 1) downTo 0) {
                                val w = cardWidthsPx[localCardSummaries[i].cardId] ?: 200f
                                val threshold = cumulated + (w + cardSpacingPx) * 0.5f
                                if (-dragOffsetX > threshold) {
                                    target = i
                                    cumulated += (w + cardSpacingPx)
                                } else {
                                    break
                                }
                            }
                            target
                        } else {
                            fromCardIndex
                        }
                    }

                    var lastTargetCardIndex by remember { mutableStateOf<Int?>(null) }
                    LaunchedEffect(targetCardIndex) {
                        if (draggingCardId != null && targetCardIndex != -1 && targetCardIndex != lastTargetCardIndex) {
                            if (lastTargetCardIndex != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            lastTargetCardIndex = targetCardIndex
                        }
                    }

                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(localCardSummaries, key = { it.cardId }) { summary ->
                            val isSelected = activeCardId == summary.cardId
                            val isTabCardPaid = summary.cardId in paidCardIds
                            val isDragging = draggingCardId == summary.cardId

                            val draggedCardWidth = cardWidthsPx[draggingCardId] ?: 200f
                            val draggedCardShift = draggedCardWidth + cardSpacingPx
                            val cardIndex = localCardSummaries.indexOfFirst { it.cardId == summary.cardId }

                            val targetShiftX = when {
                                isDragging -> 0f
                                fromCardIndex != -1 && targetCardIndex != -1 -> {
                                    if (fromCardIndex < targetCardIndex && cardIndex in (fromCardIndex + 1)..targetCardIndex) {
                                        -draggedCardShift
                                    } else if (fromCardIndex > targetCardIndex && cardIndex in targetCardIndex until fromCardIndex) {
                                        draggedCardShift
                                    } else {
                                        0f
                                    }
                                }
                                else -> 0f
                            }

                            val animatedShiftX by animateFloatAsState(
                                targetValue = targetShiftX,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "cardShift_${summary.cardId}"
                            )

                            val currentTranslationX = if (isDragging) dragOffsetX else animatedShiftX

                            Surface(
                                modifier = Modifier
                                    .onSizeChanged { cardWidthsPx[summary.cardId] = it.width.toFloat() }
                                    .zIndex(if (isDragging) 10f else 1f)
                                    .graphicsLayer {
                                        translationX = currentTranslationX
                                        if (isDragging) {
                                            scaleX = 1.08f
                                            scaleY = 1.08f
                                        }
                                    }
                                    .clip(CircleShape)
                                    .pointerInput(summary.cardId) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingCardId = summary.cardId
                                                dragOffsetX = 0f
                                                lastTargetCardIndex = fromCardIndex
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetX += dragAmount.x
                                            },
                                            onDragEnd = {
                                                val currentCardId = draggingCardId
                                                val currentOffset = dragOffsetX
                                                val from = if (currentCardId != null) localCardSummaries.indexOfFirst { it.cardId == currentCardId } else -1
                                                val to = if (from != -1) {
                                                    if (currentOffset > 0) {
                                                        var target = from
                                                        var cumulated = 0f
                                                        for (i in (from + 1) until localCardSummaries.size) {
                                                            val w = cardWidthsPx[localCardSummaries[i].cardId] ?: 200f
                                                            val threshold = cumulated + (w + cardSpacingPx) * 0.5f
                                                            if (currentOffset > threshold) {
                                                                target = i
                                                                cumulated += (w + cardSpacingPx)
                                                            } else {
                                                                break
                                                            }
                                                        }
                                                        target
                                                    } else if (currentOffset < 0) {
                                                        var target = from
                                                        var cumulated = 0f
                                                        for (i in (from - 1) downTo 0) {
                                                            val w = cardWidthsPx[localCardSummaries[i].cardId] ?: 200f
                                                            val threshold = cumulated + (w + cardSpacingPx) * 0.5f
                                                            if (-currentOffset > threshold) {
                                                                target = i
                                                                cumulated += (w + cardSpacingPx)
                                                            } else {
                                                                break
                                                            }
                                                        }
                                                        target
                                                    } else {
                                                        from
                                                    }
                                                } else -1

                                                if (from != -1 && to != -1 && from != to) {
                                                    val mutable = localCardSummaries.toMutableList()
                                                    val item = mutable.removeAt(from)
                                                    mutable.add(to, item)
                                                    localCardSummaries = mutable
                                                    onReorderCards(mutable.map { it.cardId })
                                                }
                                                draggingCardId = null
                                                dragOffsetX = 0f
                                                lastTargetCardIndex = null
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDragCancel = {
                                                draggingCardId = null
                                                dragOffsetX = 0f
                                                lastTargetCardIndex = null
                                            }
                                        )
                                    }
                                    .clickable {
                                        if (draggingCardId == null) {
                                            selectedCardId = summary.cardId
                                        }
                                    },
                                shape = CircleShape,
                                color = if (isDragging) {
                                    FinanceColors.cardAccent(isDark).copy(alpha = 0.85f)
                                } else if (isSelected) {
                                    FinanceColors.cardAccent(isDark)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                                },
                                border = if (!isSelected && !isDragging) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = summary.cardName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) FinanceColors.onAccent(isDark) else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )
                                    )
                                    if (isTabCardPaid) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = tr("Pagada", "Paid"),
                                            tint = if (isSelected) FinanceColors.onAccent(isDark) else FinanceColors.incomeAccent(isDark),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { showAddCardDialog = true },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = tr("Agregar Tarjeta", "Add card"),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentCardSummary == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.capy_question),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("¡No tienes tarjetas registradas!", "You don't have any cards yet!"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr("Agrega tu primera tarjeta para simular cuotas e intereses 💳", "Add your first card to simulate installments and interest 💳"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showAddCardDialog = true }) {
                            Text(tr("Agregar Tarjeta de Crédito", "Add credit card"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Tarjetas tipo Wallet: se deslizan de lado y la seleccionada define la lista de abajo
                val cardPagerState = rememberPagerState(
                    initialPage = cardSummaries.indexOfFirst { it.cardId == currentCardSummary.cardId }.coerceAtLeast(0),
                    pageCount = { cardSummaries.size }
                )
                // Deslizar la tarjeta cambia la selección
                LaunchedEffect(cardPagerState.settledPage) {
                    cardSummaries.getOrNull(cardPagerState.settledPage)?.let { summary ->
                        if (summary.cardId != activeCardId) selectedCardId = summary.cardId
                    }
                }
                // Tocar una píldora mueve la tarjeta
                LaunchedEffect(activeCardId, cardSummaries.size) {
                    val index = cardSummaries.indexOfFirst { it.cardId == activeCardId }
                    if (index >= 0 && index != cardPagerState.currentPage) cardPagerState.animateScrollToPage(index)
                }

                AnimatedVisibility(visible = !isScrolled) {
                    HorizontalPager(
                        state = cardPagerState,
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        pageSpacing = 10.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val summary = cardSummaries[page]
                        CreditCardVisual(
                            summary = summary,
                            payableThisMonth = summary.payableThisMonth(usdRate, ignoredKeys),
                            isPaid = summary.cardId in paidCardIds,
                            isPrivacyMode = isPrivacyMode,
                            onSimulator = {
                                selectedCardId = summary.cardId
                                showSimulatorDialog = true
                            },
                            onInfo = {
                                selectedCardId = summary.cardId
                                showCardInfoDialog = true
                            },
                            onTogglePaid = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePaidCard(summary.cardId)
                            }
                        )
                    }
                }

                // Al bajar en la lista, la tarjeta se compacta para dejar espacio
                AnimatedVisibility(visible = isScrolled) {
                    val (gradientStart, _) = cardGradient(currentCardSummary.cardId, currentCardSummary.colorIndex)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = gradientStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentCardSummary.cardName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = privateText(formatCurrency(currentCardSummary.payableThisMonth(usdRate, ignoredKeys)), isPrivacyMode),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White,
                                modifier = Modifier.privacyBlur(isPrivacyMode)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Purchases List
                val sortedPurchases = remember(currentCardSummary.purchases, selectedYearMonth, installmentsPosition, expenseSortOrder) {
                    val isDeferredOrRecurring: (PurchaseInstallmentDetail) -> Boolean = { detail ->
                        val purchaseYm = YearMonth.from(Instant.ofEpochMilli(detail.purchaseDateMillis).atZone(ZoneId.systemDefault()).toLocalDate())
                        detail.isRecurring || (detail.totalInstallments > 1 && (detail.currentInstallment > 1 || purchaseYm.isBefore(selectedYearMonth)))
                    }

                    val currentMonthItems = currentCardSummary.purchases.filter { !isDeferredOrRecurring(it) }
                    val deferredOrRecurringItems = currentCardSummary.purchases.filter { isDeferredOrRecurring(it) }

                    val sortedCurrentMonth = when (expenseSortOrder) {
                        ExpenseSortOrder.NEWEST_FIRST -> currentMonthItems.sortedWith(
                            compareByDescending<PurchaseInstallmentDetail> { it.purchaseDateMillis }.thenByDescending { it.purchaseId }
                        )
                        ExpenseSortOrder.OLDEST_FIRST -> currentMonthItems.sortedWith(
                            compareBy<PurchaseInstallmentDetail> { it.purchaseDateMillis }.thenBy { it.purchaseId }
                        )
                    }

                    val sortedDeferredOrRecurring = deferredOrRecurringItems.sortedWith(
                        compareByDescending<PurchaseInstallmentDetail> { it.purchaseDateMillis }.thenByDescending { it.purchaseId }
                    )

                    if (installmentsPosition == InstallmentPosition.FIRST) {
                        sortedDeferredOrRecurring + sortedCurrentMonth
                    } else {
                        sortedCurrentMonth + sortedDeferredOrRecurring
                    }
                }

                if (sortedPurchases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.capy_rich),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = tr("¡Sin compras diferidas este mes!", "No installment purchases this month!"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("No tienes cuotas activas para esta tarjeta ✨", "No active installments on this card ✨"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(sortedPurchases, key = { it.purchaseId }) { purchase ->
                            CardPurchaseItem(
                                purchase = purchase,
                                usdRate = usdRate,
                                isPrivacyMode = isPrivacyMode,
                                onSettle = { purchaseToSettle = purchase },
                                onAddPayment = { purchaseToPay = purchase },
                                onEdit = { purchaseToEdit = purchase },
                                onDelete = { purchaseToDelete = purchase }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCardDialog) {
        AddEditCardDialog(
            cardToEdit = null,
            onDismiss = { showAddCardDialog = false },
            onSave = { name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, colorIndex ->
                onAddCard(name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, colorIndex)
                showAddCardDialog = false
            }
        )
    }

    if (cardToEdit != null) {
        AddEditCardDialog(
            cardToEdit = cardToEdit,
            onDismiss = { cardToEdit = null },
            onSave = { name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, colorIndex ->
                onUpdateCard(
                    cardToEdit!!.copy(
                        name = name,
                        cutoffDay = cutoffDay,
                        paymentDay = paymentDay,
                        interestRate = interestRate,
                        maintenanceFee = maintenanceFee,
                        currency = currency,
                        isFeeWaiverEnabled = isFeeWaiverEnabled,
                        minSpendForFeeWaiver = minSpend,
                        isFirstInstallmentInterestFree = isFirstInstallmentInterestFree,
                        creditLimit = creditLimit,
                        colorIndex = colorIndex
                    )
                )
                cardToEdit = null
            },
            onDelete = {
                cardToDelete = cardToEdit
                cardToEdit = null
            }
        )
    }

    cardToDelete?.let { card ->
        val purchasesCount = cardSummaries.find { it.cardId == card.id }?.purchases?.size ?: 0
        ConfirmDeleteDialog(
            itemName = card.name,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { cardToDelete = null },
            onDeleteAll = {
                onDeleteCard(card)
                if (selectedCardId == card.id) selectedCardId = null
            },
            extraMessage = tr("Se eliminarán también todas las compras, cuotas y abonos de esta tarjeta", "All purchases, installments and payments on this card will also be deleted") +
                if (purchasesCount > 0) tr(" ($purchasesCount con cuota activa este mes).", " ($purchasesCount with an active installment this month).") else "."
        )
    }

    if (showAddPurchaseDialog && currentCardSummary != null) {
        AddEditPurchaseDialog(
            cardName = currentCardSummary.cardName,
            cardInterestRate = currentCardSummary.interestRate,
            cardFirstInstallmentInterestFree = currentCardSummary.isFirstInstallmentInterestFree,
            purchaseToEdit = null,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { showAddPurchaseDialog = false },
            onSave = { concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency ->
                onAddPurchase(currentCardSummary.cardId, concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency)
                showAddPurchaseDialog = false
            }
        )
    }

    if (purchaseToEdit != null && currentCardSummary != null) {
        AddEditPurchaseDialog(
            cardName = currentCardSummary.cardName,
            cardInterestRate = currentCardSummary.interestRate,
            cardFirstInstallmentInterestFree = currentCardSummary.isFirstInstallmentInterestFree,
            purchaseToEdit = purchaseToEdit,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { purchaseToEdit = null },
            onSave = { concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency ->
                onUpdatePurchase(
                    CardPurchaseEntity(
                        id = purchaseToEdit!!.purchaseId,
                        cardId = purchaseToEdit!!.cardId,
                        concept = concept,
                        totalAmount = totalAmount,
                        installments = installments,
                        purchaseDate = dateMillis,
                        isInterestFree = isInterestFree,
                        isRecurring = isRecurring,
                        currency = currency
                    )
                )
                purchaseToEdit = null
            }
        )
    }

    if (showCardInfoDialog && currentCardSummary != null) {
        CardInfoDetailDialog(
            cardSummary = currentCardSummary,
            payableThisMonth = currentCardSummary.payableThisMonth(usdRate, ignoredKeys),
            onDismiss = { showCardInfoDialog = false },
            onSettings = {
                val entity = allCards.find { it.id == currentCardSummary.cardId }
                if (entity != null) cardToEdit = entity
                showCardInfoDialog = false
            }
        )
    }

    if (showSimulatorDialog && currentCardSummary != null) {
        PurchaseSimulatorDialog(
            cardSummary = currentCardSummary,
            usdRate = usdRate,
            onDismiss = { showSimulatorDialog = false }
        )
    }

    if (purchaseToPay != null && currentCardSummary != null) {
        AddPaymentDialog(
            purchase = purchaseToPay!!,
            currencySymbol = if (purchaseToPay!!.currency == "USD") "USD $" else "$",
            onDismiss = { purchaseToPay = null },
            onSave = { amount ->
                val dateMillis = calculateSaveDateForMonth(selectedYearMonth)
                onAddPurchasePayment(purchaseToPay!!.purchaseId, amount, dateMillis)
                purchaseToPay = null
            }
        )
    }

    if (purchaseToSettle != null) {
        val purchase = purchaseToSettle!!
        val isSettled = purchase.isSettledThisMonth
        val currencySym = if (purchase.currency == "USD") "USD $" else "$"
        val amountFormatted = if (purchase.currency == "USD") {
            "${formatUsd(purchase.remainingBalanceToPay)} (~${formatCurrency(purchase.remainingBalanceToPay * usdRate)})"
        } else {
            formatCurrency(purchase.remainingBalanceToPay, currencySym)
        }

        AlertDialog(
            onDismissRequest = { purchaseToSettle = null },
            icon = {
                Icon(
                    imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = if (isSettled) FinanceColors.goldAccent(isDark) else FinanceColors.incomeAccent(isDark),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isSettled) tr("Revertir Pago Total", "Undo full payment") else tr("Pagar Completo este Mes", "Pay in full this month"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isSettled) {
                        Text(
                            text = tr("¿Deseas cancelar la liquidación de '${purchase.concept}' y volver al cobro de cuotas mensuales regulares?", "Undo the full payment of '${purchase.concept}' and go back to regular monthly installments?"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = tr("¿Deseas pagar la totalidad restante de '${purchase.concept}' en este mes (${selectedYearMonth.monthValue}/${selectedYearMonth.year})?", "Pay the remaining balance of '${purchase.concept}' this month (${selectedYearMonth.monthValue}/${selectedYearMonth.year})?"),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Surface(
                            color = FinanceColors.incomeContainer(isDark).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FinanceColors.incomeAccent(isDark).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = tr("Total a pagar este mes:", "Total to pay this month:"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = amountFormatted,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinanceColors.incomeText(isDark)
                                    )
                                )
                                if (purchase.totalInstallments > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tr("Cuotas restantes: ${purchase.currentInstallment} a ${purchase.totalInstallments} (${purchase.remainingInstallmentsCount} cuotas)", "Remaining installments: ${purchase.currentInstallment} to ${purchase.totalInstallments} (${purchase.remainingInstallmentsCount} installments)"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = tr("💡 Al liquidar, este mes se cargará el saldo restante completo y en los meses siguientes esta compra quedará finalizada sin más cobros.", "💡 Paying in full charges the whole remaining balance this month, and the purchase is finished with no more charges in the following months."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSettlePurchase(purchase.purchaseId)
                        purchaseToSettle = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isSettled) MaterialTheme.colorScheme.error else FinanceColors.incomeAccent(isDark)
                    )
                ) {
                    Text(
                        text = if (isSettled) tr("Revertir a Cuotas", "Back to installments") else tr("Pagar Saldo Completo", "Pay full balance"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToSettle = null }) {
                    Text(tr("Cancelar", "Cancel"))
                }
            }
        )
    }

    if (purchaseToDelete != null) {
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text(tr("Eliminar Compra", "Delete purchase")) },
            text = { Text(tr("¿Deseas eliminar '${purchaseToDelete!!.concept}'?", "Delete '${purchaseToDelete!!.concept}'?")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePurchase(purchaseToDelete!!.purchaseId)
                        purchaseToDelete = null
                    }
                ) {
                    Text(tr("Eliminar Todo", "Delete all"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onOmitPurchaseForMonth(purchaseToDelete!!.purchaseId)
                            purchaseToDelete = null
                        }
                    ) {
                        Text(tr("Omitir este Mes", "Skip this month"))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { purchaseToDelete = null }) {
                        Text(tr("Cancelar", "Cancel"))
                    }
                }
            }
        )
    }
}

@Composable
fun CardPurchaseItem(
    purchase: PurchaseInstallmentDetail,
    usdRate: Double = 4150.0,
    isPrivacyMode: Boolean = false,
    onSettle: () -> Unit,
    onAddPayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat(tr("dd MMM yyyy", "MMM d, yyyy"), appLocale())
    val formattedDate = dateFormat.format(Date(purchase.purchaseDateMillis))
    val currencySymbol = if (purchase.currency == "USD") "USD $" else "$"
    val isDark = isAppDark()

    val (badgeBg, badgeText) = when {
        purchase.isSettledThisMonth -> Pair(FinanceColors.goldContainer(isDark), FinanceColors.goldText(isDark))
        purchase.statusType == InstallmentStatusType.ACTIVE -> Pair(FinanceColors.cardContainer(isDark), FinanceColors.cardText(isDark))
        purchase.statusType == InstallmentStatusType.FINISHED -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        purchase.statusType == InstallmentStatusType.FUTURE -> Pair(FinanceColors.goldContainer(isDark), FinanceColors.goldText(isDark))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val showRemaining = purchase.totalInstallments > 1 && purchase.statusType == InstallmentStatusType.ACTIVE

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (purchase.isSettledThisMonth) {
                FinanceColors.incomeContainer(isDark).copy(alpha = if (isDark) 0.35f else 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (purchase.isSettledThisMonth) {
                FinanceColors.goldAccent(isDark).copy(alpha = if (isDark) 0.6f else 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (purchase.isSettledThisMonth) FinanceColors.goldContainer(isDark)
                                else FinanceColors.cardContainer(isDark)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (purchase.isSettledThisMonth) Icons.Default.CheckCircle else Icons.Default.Payment,
                            contentDescription = null,
                            tint = if (purchase.isSettledThisMonth) FinanceColors.goldAccent(isDark) else FinanceColors.cardAccent(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = purchase.concept,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                ),
                                modifier = Modifier.weight(1f, fill = false),
                                softWrap = true
                            )
                            if (purchase.isInterestFree && purchase.totalInstallments > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = FinanceColors.incomeContainer(isDark),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tr("0% int.", "0% int."),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FinanceColors.incomeText(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        val totalStr = if (isPrivacyMode) tr("Total: $ ••••••", "Total: $ ••••••") else if (purchase.currency == "USD") tr("Total: ${formatUsd(purchase.totalAmount)} (~${formatCurrency(purchase.totalAmount * usdRate)})", "Total: ${formatUsd(purchase.totalAmount)} (~${formatCurrency(purchase.totalAmount * usdRate)})") else tr("Total: ${formatCurrency(purchase.totalAmount, currencySymbol)}", "Total: ${formatCurrency(purchase.totalAmount, currencySymbol)}")
                        val remainingStr = if (!isPrivacyMode && showRemaining) {
                            if (purchase.currency == "USD") tr(" | Restante: ${formatUsd(purchase.remainingBalanceToPay)} (~${formatCurrency(purchase.remainingBalanceToPay * usdRate)})", " | Remaining: ${formatUsd(purchase.remainingBalanceToPay)} (~${formatCurrency(purchase.remainingBalanceToPay * usdRate)})")
                            else tr(" | Restante: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}", " | Remaining: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}")
                        } else ""

                        Text(
                            text = "$totalStr$remainingStr",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        if (purchase.totalPaidAmount > 0) {
                            val abonoStr = if (isPrivacyMode) tr("Abonado: $ ••••••", "Paid extra: $ ••••••") else if (purchase.currency == "USD") tr("Abonado: ${formatUsd(purchase.totalPaidAmount)} (~${formatCurrency(purchase.totalPaidAmount * usdRate)})", "Paid extra: ${formatUsd(purchase.totalPaidAmount)} (~${formatCurrency(purchase.totalPaidAmount * usdRate)})") else tr("Abonado: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}", "Paid extra: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}")
                            Text(
                                text = abonoStr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FinanceColors.incomeText(isDark),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = tr("Opciones de la compra", "Purchase options"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
                    ) {
                        if (purchase.isSettledThisMonth) {
                            DropdownMenuItem(
                                text = { Text(tr("Revertir liquidación", "Undo full payment"), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FinanceColors.goldAccent(isDark)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onSettle()
                                }
                            )
                        } else if (purchase.statusType == InstallmentStatusType.ACTIVE && (purchase.totalInstallments > 1 || purchase.isRecurring)) {
                            DropdownMenuItem(
                                text = { Text(tr("Pagar saldo completo este mes", "Pay full balance this month"), fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = FinanceColors.incomeAccent(isDark)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onSettle()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(tr("Hacer abono", "Make extra payment"), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = FinanceColors.incomeAccent(isDark)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onAddPayment()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(tr("Editar", "Edit"), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = { Text(tr("Eliminar compra", "Delete purchase"), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (purchase.totalInstallments > 1 && purchase.statusType == InstallmentStatusType.ACTIVE) {
                val progress = (purchase.currentInstallment.toFloat() / purchase.totalInstallments.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(FinanceColors.cardAccent(isDark))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = purchase.statusText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tr("Pago este mes", "Due this month"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPrivacyMode) {
                        Text(
                            text = "$ ••••••",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                    } else if (purchase.currency == "USD") {
                        Text(
                            text = formatUsd(purchase.monthlyPayment),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                        Text(
                            text = "(~${formatCurrency(purchase.monthlyPayment * usdRate)})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    } else {
                        Text(
                            text = formatCurrency(purchase.monthlyPayment, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceColors.cardText(isDark)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    purchase: PurchaseInstallmentDetail,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedAmount = parseAmount(amountText) ?: 0.0
    val newBalance = (purchase.remainingBalanceToPay - parsedAmount).coerceAtLeast(0.0)

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = tr("Abonar a: ${purchase.concept}", "Extra payment to: ${purchase.concept}"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = tr("Monto inicial compra: ${formatCurrency(purchase.totalAmount, currencySymbol)}", "Original amount: ${formatCurrency(purchase.totalAmount, currencySymbol)}"),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (purchase.totalPaidAmount > 0) {
                    Text(
                        text = tr("Abonado acumulado: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}", "Extra paid so far: ${formatCurrency(purchase.totalPaidAmount, currencySymbol)}"),
                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeAccent(isAppDark()))
                    )
                }
                Text(
                    text = tr("Saldo pendiente: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}", "Remaining balance: ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                AmountTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Monto a abonar",
                    currency = purchase.currency,
                    prominent = true
                )

                if (parsedAmount > 0) {
                    Surface(
                        color = FinanceColors.incomeContainer(isAppDark()),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tr("Nuevo saldo estimado:", "Estimated new balance:"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.incomeText(isAppDark()))
                            )
                            Text(
                                text = formatCurrency(newBalance, currencySymbol),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.incomeAccent(isAppDark())
                                )
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedAmount <= 0) {
                        errorMessage = tr("Ingresa un monto de abono mayor a 0", "Enter a payment greater than 0")
                    } else if (parsedAmount > purchase.remainingBalanceToPay) {
                        errorMessage = tr("El abono no puede ser mayor al saldo de ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}", "The payment can't be more than the balance of ${formatCurrency(purchase.remainingBalanceToPay, currencySymbol)}")
                    } else {
                        onSave(parsedAmount)
                    }
                }
            ) {
                Text(tr("Guardar Abono", "Save payment"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}

@Composable
fun AddEditCardDialog(
    cardToEdit: CreditCardEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, cutoffDay: Int, paymentDay: Int, interestRate: Double, maintenanceFee: Double, currency: String, isFeeWaiverEnabled: Boolean, minSpendForFeeWaiver: Double, isFirstInstallmentInterestFree: Boolean, creditLimit: Double, colorIndex: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(cardToEdit?.name ?: "") }
    var cutoffDayText by remember { mutableStateOf(cardToEdit?.cutoffDay?.toString() ?: "15") }
    var paymentDayText by remember { mutableStateOf(cardToEdit?.paymentDay?.let { if (it > 0) it.toString() else "" } ?: "") }
    var interestRateText by remember { mutableStateOf(cardToEdit?.interestRate?.toString() ?: "0.0") }
    var maintenanceFeeText by remember { mutableStateOf(amountToInput(cardToEdit?.maintenanceFee)) }
    var creditLimitText by remember { mutableStateOf(amountToInput(cardToEdit?.creditLimit)) }
    // Color elegido; al editar una tarjeta sin color propio se parte del automático que ya tenía
    var selectedColorIndex by remember {
        mutableIntStateOf(
            when {
                cardToEdit == null -> 0
                cardToEdit.colorIndex in CardPalettes.indices -> cardToEdit.colorIndex
                else -> autoCardColorIndex(cardToEdit.id)
            }
        )
    }
    val currency = "COP"

    var isFeeWaiverEnabled by remember { mutableStateOf(cardToEdit?.isFeeWaiverEnabled ?: false) }
    var minSpendText by remember { mutableStateOf(amountToInput(cardToEdit?.minSpendForFeeWaiver)) }
    var isFirstInstallmentInterestFree by remember { mutableStateOf(cardToEdit?.isFirstInstallmentInterestFree ?: false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (cardToEdit == null) tr("Agregar Tarjeta", "Add card") else tr("Configurar Tarjeta", "Card settings"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(tr("Nombre de la Tarjeta", "Card name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                CardColorPicker(
                    cardName = name.ifBlank { tr("Mi tarjeta", "My card") },
                    selectedIndex = selectedColorIndex,
                    onSelect = { selectedColorIndex = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cutoffDayText,
                        onValueChange = { cutoffDayText = it },
                        label = { Text(tr("Día de corte (1-31)", "Statement day (1-31)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paymentDayText,
                        onValueChange = { paymentDayText = it },
                        label = { Text(tr("Día de pago (1-31)", "Payment day (1-31)")) },
                        placeholder = { Text(tr("Opcional", "Optional")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic cycle hint
                val parsedCutoff = cutoffDayText.toIntOrNull()
                val parsedPayment = paymentDayText.toIntOrNull() ?: 0
                if (parsedCutoff != null && parsedCutoff in 1..31) {
                    val cycleHint = when {
                        parsedPayment in 1..31 -> {
                            if (parsedPayment > parsedCutoff) tr("💳 Corte día $parsedCutoff y pago día $parsedPayment: compras del ciclo se pagan este mismo mes.", "💳 Statement on day $parsedCutoff and payment on day $parsedPayment: purchases in the cycle are paid the same month.")
                            else tr("💳 Corte día $parsedCutoff y pago día $parsedPayment: compras del ciclo se pagan al mes siguiente.", "💳 Statement on day $parsedCutoff and payment on day $parsedPayment: purchases in the cycle are paid the following month.")
                        }
                        parsedCutoff >= 28 -> tr("💳 Corte día $parsedCutoff (fin de mes): compras se pagan al mes siguiente.", "💳 Statement on day $parsedCutoff (end of month): purchases are paid the following month.")
                        else -> tr("💳 Corte día $parsedCutoff: compras se pagan en el mismo mes.", "💳 Statement on day $parsedCutoff: purchases are paid the same month.")
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cycleHint,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                AmountTextField(
                    value = creditLimitText,
                    onValueChange = { creditLimitText = it },
                    label = tr("Cupo total de la tarjeta (opcional)", "Card credit limit (optional)"),
                    supportingText = { Text(tr("Para mostrar cuánto cupo llevas usado", "To show how much of your limit you've used")) }
                )

                AmountTextField(
                    value = maintenanceFeeText,
                    onValueChange = { maintenanceFeeText = it },
                    label = tr("Cuota de manejo", "Card fee"),
                    placeholder = "0"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("Exoneración por consumo mínimo", "Fee waiver for minimum spending"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Si el cupo gastado total alcanza un monto mínimo, la cuota es $0", "If your total spending reaches a minimum amount, the fee is $0"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isFeeWaiverEnabled,
                        onCheckedChange = { isFeeWaiverEnabled = it }
                    )
                }

                if (isFeeWaiverEnabled) {
                    AmountTextField(
                        value = minSpendText,
                        onValueChange = { minSpendText = it },
                        label = tr("Cupo gastado mínimo para exonerar", "Minimum spending to waive the fee")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("1ª Cuota Sin Interés (0%)", "1st installment interest-free (0%)"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("La primera cuota de cualquier compra no cobra intereses", "The first installment of any purchase has no interest"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isFirstInstallmentInterestFree,
                        onCheckedChange = { isFirstInstallmentInterestFree = it }
                    )
                }

                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it },
                    label = { Text(tr("Tasa de interés % mensual (ej. 2.2)", "Monthly interest rate % (e.g. 2.2)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cutoffDay = cutoffDayText.toIntOrNull()
                    val paymentDay = paymentDayText.toIntOrNull() ?: 0
                    val interestRate = parseAmount(interestRateText) ?: 0.0
                    val maintenanceFee = parseAmount(maintenanceFeeText) ?: 0.0
                    val minSpend = if (isFeeWaiverEnabled) parseAmount(minSpendText) ?: 0.0 else 0.0
                    val creditLimit = parseAmount(creditLimitText) ?: 0.0

                    if (name.isBlank()) {
                        errorMessage = tr("Por favor ingresa un nombre para la tarjeta", "Enter a name for the card")
                    } else if (cutoffDay == null || cutoffDay !in 1..31) {
                        errorMessage = tr("El día de corte debe ser entre 1 y 31", "The statement day must be between 1 and 31")
                    } else if (paymentDay !in 0..31) {
                        errorMessage = tr("El día de pago debe ser entre 1 y 31 (o déjalo vacío)", "The payment day must be between 1 and 31 (or leave it empty)")
                    } else if (isFeeWaiverEnabled && minSpend <= 0) {
                        errorMessage = tr("Ingresa un monto mínimo de consumo válido para la exoneración", "Enter a valid minimum spending amount for the fee waiver")
                    } else {
                        onSave(name.trim(), cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, selectedColorIndex)
                    }
                }
            ) {
                Text(tr("Guardar", "Save"))
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(tr("Eliminar", "Delete"), color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text(tr("Cancelar", "Cancel"))
                }
            }
        }
    )
}

@Composable
fun AddEditPurchaseDialog(
    cardName: String,
    cardInterestRate: Double = 0.0,
    cardFirstInstallmentInterestFree: Boolean = false,
    purchaseToEdit: PurchaseInstallmentDetail?,
    selectedYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onSave: (concept: String, totalAmount: Double, installments: Int, purchaseDateMillis: Long, isInterestFree: Boolean, isRecurring: Boolean, currency: String) -> Unit
) {
    var concept by remember { mutableStateOf(purchaseToEdit?.concept ?: "") }
    var totalAmountText by remember { mutableStateOf(amountToInput(purchaseToEdit?.totalAmount, purchaseToEdit?.currency == "USD")) }
    var installmentsText by remember { mutableStateOf(purchaseToEdit?.totalInstallments?.toString() ?: "1") }
    var isInterestFree by remember { mutableStateOf(purchaseToEdit?.isInterestFree ?: false) }
    var isRecurring by remember { mutableStateOf(purchaseToEdit?.isRecurring ?: false) }
    var currency by remember { mutableStateOf(purchaseToEdit?.currency ?: "COP") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val initialDateMillis = remember(purchaseToEdit, selectedYearMonth) {
        calculateSaveDateForMonth(selectedYearMonth, purchaseToEdit?.purchaseDateMillis)
    }
    var purchaseDateMillis by remember(purchaseToEdit, selectedYearMonth) { mutableLongStateOf(initialDateMillis) }

    val context = LocalContext.current
    val datePickerDialog = rememberDatePicker(purchaseDateMillis) { purchaseDateMillis = it }

    val purchaseDateFormat = remember { SimpleDateFormat(tr("dd 'de' MMMM 'de' yyyy", "MMMM d, yyyy"), appLocale()) }

    // Live preview calculation of monthly quota
    val parsedTotalAmount = parseAmount(totalAmountText) ?: 0.0
    val parsedInstallments = installmentsText.toIntOrNull() ?: 1
    // Mismo cálculo que la compra real (cuota regular; la 1ª puede ir sin interés)
    val previewMonthlyPayment = remember(parsedTotalAmount, parsedInstallments, isInterestFree, cardInterestRate, cardFirstInstallmentInterestFree) {
        if (parsedTotalAmount <= 0.0) 0.0
        else installmentSchedule(parsedTotalAmount, parsedInstallments, cardInterestRate, isInterestFree, cardFirstInstallmentInterestFree).last()
    }

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (purchaseToEdit == null) tr("Nueva Compra en $cardName", "New purchase on $cardName") else tr("Editar Compra / Cuotas", "Edit purchase / installments"),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(tr("Concepto de la compra", "Purchase name")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                AmountTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = "Monto total",
                    currency = currency,
                    prominent = true
                )

                Text(
                    text = tr("Moneda de la compra:", "Purchase currency:"),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = currency == "COP",
                        onClick = { currency = "COP" }
                    )
                    Text("COP ($)", modifier = Modifier.clickable { currency = "COP" })

                    Spacer(modifier = Modifier.width(16.dp))

                    RadioButton(
                        selected = currency == "USD",
                        onClick = { currency = "USD" }
                    )
                    Text("USD ($)", modifier = Modifier.clickable { currency = "USD" })
                }

                // Date Picker Field
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tr("Fecha de la compra", "Purchase date"),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = purchaseDateFormat.format(Date(purchaseDateMillis)),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = tr("Cambiar fecha", "Change date"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = installmentsText,
                    onValueChange = { installmentsText = it },
                    label = { Text(tr("Número de cuotas (ej. 1, 3, 6, 12, 24)", "Number of installments (e.g. 1, 3, 6, 12, 24)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsedInstallments > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("Exenta de intereses (0% interés)", "Interest-free (0% interest)"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tr("No aplica tasa de interés en esta compra", "No interest rate applies to this purchase"),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Switch(
                            checked = isInterestFree,
                            onCheckedChange = { isInterestFree = it }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tr("Recurrente mensual (ej. Suscripciones)", "Repeats monthly (e.g. subscriptions)"),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = tr("Se cobra automáticamente mes a mes", "Charged automatically every month"),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }

                // Live Preview Card
                if (parsedTotalAmount > 0.0 && parsedInstallments > 0) {
                    val previewCurrencySymbol = if (currency == "USD") "USD $" else "$"
                    Surface(
                        color = FinanceColors.cardContainer(isAppDark()),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = tr("Cuota mensual estimada:", "Estimated monthly payment:"),
                                style = MaterialTheme.typography.bodySmall.copy(color = FinanceColors.cardText(isAppDark()))
                            )
                            Text(
                                text = tr("${formatCurrency(previewMonthlyPayment, previewCurrencySymbol)} / mes (${parsedInstallments} cuota(s))", "${formatCurrency(previewMonthlyPayment, previewCurrencySymbol)} / month (${parsedInstallments} installment(s))"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.cardAccent(isAppDark())
                                )
                            )
                            if (!isInterestFree && cardInterestRate > 0 && parsedInstallments > 1) {
                                Text(
                                    text = tr("Incluye ${cardInterestRate}% interés mensual de la tarjeta", "Includes the card's ${cardInterestRate}% monthly interest"),
                                    style = MaterialTheme.typography.labelSmall.copy(color = FinanceColors.cardText(isAppDark()), fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = parseAmount(totalAmountText)
                    val installments = installmentsText.toIntOrNull()

                    if (concept.isBlank()) {
                        errorMessage = tr("Ingresa el concepto de la compra", "Enter the purchase name")
                    } else if (amount == null || amount <= 0) {
                        errorMessage = tr("Ingresa un monto total válido mayor a 0", "Enter a valid total greater than 0")
                    } else if (installments == null || installments <= 0) {
                        errorMessage = tr("El número de cuotas debe ser al menos 1", "The number of installments must be at least 1")
                    } else {
                        val finalIsInterestFree = if (installments <= 1) false else isInterestFree
                        onSave(concept.trim(), amount, installments, purchaseDateMillis, finalIsInterestFree, isRecurring, currency)
                    }
                }
            ) {
                Text(tr("Guardar Compra", "Save purchase"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar", "Cancel"))
            }
        }
    )
}

@Composable
fun CardInfoDetailDialog(
    cardSummary: CardMonthSummary,
    payableThisMonth: Double,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    val isDark = isAppDark()

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FinanceColors.cardContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = FinanceColors.cardAccent(isDark),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = cardSummary.cardName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = tr("Detalles y configuración de la tarjeta", "Card details and settings"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Card 1: Condiciones Financieras
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Día de Corte", "Statement day"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tr("Día ${cardSummary.cutoffDay} de cada mes", "Day ${cardSummary.cutoffDay} of each month"), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Día Límite de Pago", "Payment due day"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.paymentDay in 1..31) tr("Día ${cardSummary.paymentDay} de cada mes", "Day ${cardSummary.paymentDay} of each month") else tr("No configurado", "Not set"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Tasa de Interés", "Interest rate"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${cardSummary.interestRate}% M.V.", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("1ª Cuota Sin Interés", "1st installment interest-free"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.isFirstInstallmentInterestFree) tr("0% Activado", "0% on") else tr("No", "No"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cardSummary.isFirstInstallmentInterestFree) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Bento Card 2: Cuota de Manejo y Exoneración
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr("Cuota de Manejo", "Card fee"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cardSummary.isFeeWaivedThisMonth) tr("$0 (Exonerada)", "$0 (Waived)") else formatCurrency(cardSummary.maintenanceFee),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cardSummary.isFeeWaivedThisMonth) FinanceColors.incomeText(isDark) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        if (cardSummary.isFeeWaiverEnabled) {
                            val isWaived = cardSummary.isFeeWaivedThisMonth
                            val minRequired = cardSummary.minSpendForFeeWaiver
                            val currentSpending = cardSummary.currentMonthSpending

                            Surface(
                                color = if (isWaived) FinanceColors.incomeContainer(isDark) else FinanceColors.goldContainer(isDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isWaived) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isWaived) FinanceColors.incomeAccent(isDark) else FinanceColors.goldAccent(isDark),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isWaived) {
                                            tr("¡Exoneración cumplida! Superaste el mínimo de ${formatCurrency(minRequired)}", "Fee waived! You passed the minimum of ${formatCurrency(minRequired)}")
                                        } else {
                                            tr("Meta para exonerar: ${formatCurrency(minRequired)} (Llevas ${formatCurrency(currentSpending)})", "Goal to waive the fee: ${formatCurrency(minRequired)} (So far ${formatCurrency(currentSpending)})")
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isWaived) FinanceColors.incomeText(isDark) else FinanceColors.goldText(isDark)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bento Card 3: Totales de la Tarjeta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FinanceColors.cardContainer(isDark).copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, FinanceColors.cardContainer(isDark))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Pago este Mes", "Due this month"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceColors.cardText(isDark)
                                )
                            )
                            Text(
                                text = formatCurrency(payableThisMonth),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.cardText(isDark)
                                )
                            )
                        }

                        HorizontalDivider(color = FinanceColors.cardContainer(isDark).copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("Deuda Total", "Total debt"),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = tr("Saldo total restante por pagar", "Total remaining balance to pay"),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = formatCurrency(cardSummary.currentMonthSpending),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.expenseText(isDark)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Single Action Button: Configuración de la Tarjeta
                Button(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Configuración de la Tarjeta", "Card settings"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun PurchaseSimulatorDialog(
    cardSummary: CardMonthSummary,
    usdRate: Double = 4150.0,
    onDismiss: () -> Unit
) {
    val isDark = isAppDark()
    var amountInput by remember { mutableStateOf("") }
    var installmentsInput by remember { mutableStateOf("1") }
    var isInterestFree by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(cardSummary.currency) }

    val amount = parseAmount(amountInput) ?: 0.0
    val installments = installmentsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1

    // Usa exactamente el mismo plan de cuotas que las compras reales
    val (monthlyPayment, totalToPay, totalInterest) = remember(amount, installments, isInterestFree, cardSummary.interestRate, cardSummary.isFirstInstallmentInterestFree) {
        if (amount <= 0) {
            Triple(0.0, 0.0, 0.0)
        } else {
            val schedule = installmentSchedule(
                principal = amount,
                installments = installments,
                monthlyRatePercent = cardSummary.interestRate,
                isInterestFree = isInterestFree,
                isFirstInstallmentInterestFree = cardSummary.isFirstInstallmentInterestFree
            )
            val total = schedule.sum()
            Triple(schedule.last(), total, (total - amount).coerceAtLeast(0.0))
        }
    }

    val currencySymbol = if (selectedCurrency == "USD") "USD $" else "$"

    FormSheet(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FinanceColors.cardContainer(isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = FinanceColors.cardAccent(isDark),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tr("Simulador de Cuotas", "Installment simulator"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = tr("${cardSummary.cardName} (${cardSummary.interestRate}% int. mensual)", "${cardSummary.cardName} (${cardSummary.interestRate}% monthly int.)"),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmountTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = tr("Monto de la compra", "Purchase amount"),
                    placeholder = tr("Ej: 250.000", "E.g. 250.000"),
                    currency = selectedCurrency,
                    prominent = true
                )

                // Quick Installment Selector Chips
                Text(tr("Número de cuotas:", "Number of installments:"), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 3, 6, 12, 24).forEach { cuota ->
                        val isSelected = installmentsInput == cuota.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) FinanceColors.cardAccent(isDark) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { installmentsInput = cuota.toString() }
                        ) {
                            Text(
                                text = "$cuota",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) FinanceColors.onAccent(isDark) else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // 0% Interest Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr("¿Promoción 0% Interés?", "0% interest promotion?"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isInterestFree,
                        onCheckedChange = { isInterestFree = it }
                    )
                }

                if (amount > 0) {
                    HorizontalDivider()

                    // Result Bento Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FinanceColors.cardContainer(isDark).copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, FinanceColors.cardAccent(isDark).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Pago Mensual Estimado:", "Estimated monthly payment:"), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (selectedCurrency == "USD") "${formatUsd(monthlyPayment)} (~${formatCurrency(monthlyPayment * usdRate)})" else formatCurrency(monthlyPayment),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinanceColors.cardText(isDark)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Total a Pagar ($installments cuotas):", "Total to pay ($installments installments):"), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (selectedCurrency == "USD") formatUsd(totalToPay) else formatCurrency(totalToPay),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tr("Total en Intereses:", "Total interest:"), style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = if (totalInterest <= 0.0) tr("$0 (Sin Interés)", "$0 (No interest)") else if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalInterest <= 0.0) FinanceColors.incomeText(isDark) else FinanceColors.expenseText(isDark)
                                    )
                                )
                            }

                            if (totalInterest > 0) {
                                Surface(
                                    color = FinanceColors.incomeContainer(isDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = tr("💡 ¡Ahorras ${if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest)} si pagas a 1 cuota sin interés!", "💡 You save ${if (selectedCurrency == "USD") formatUsd(totalInterest) else formatCurrency(totalInterest)} by paying in 1 installment with no interest!"),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = FinanceColors.incomeText(isDark)
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cerrar", "Close"), fontWeight = FontWeight.Bold)
            }
        }
    )
}

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
                            privateText(tr("Cupo usado ${(summary.currentMonthSpending / summary.creditLimit * 100).toInt()}%", "Limit used ${(summary.currentMonthSpending / summary.creditLimit * 100).toInt()}%"), isPrivacyMode, tr("Cupo usado ••", "Limit used ••"))
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
