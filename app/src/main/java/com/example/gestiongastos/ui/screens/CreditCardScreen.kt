package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.ui.components.currencySymbolOf
import com.example.gestiongastos.ui.components.formatUsdWithCop
import com.example.gestiongastos.data.repository.localDateOf
import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.model.arrangeByRegistration
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import com.example.gestiongastos.ui.components.AutoResizedText
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestiongastos.data.local.CardPurchaseEntity
import com.example.gestiongastos.data.local.CreditCardEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.data.model.ExpenseSortOrder
import com.example.gestiongastos.data.model.InstallmentPosition
import com.example.gestiongastos.data.model.PurchaseInstallmentDetail
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.data.model.isDeferredOrRecurring
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.ui.components.privacyBlur
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.capyPattern
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.calculateSaveDateForMonth
import java.time.YearMonth

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

    // También al cambiar el orden (se cambia desde Gastos o Ajustes)
    LaunchedEffect(activeCardId, expenseSortOrder) {
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
                    // Por día y luego por orden de registro (id); la hora no sirve porque lo
                    // registrado viendo otro mes queda el día 15 con la hora del momento
                    val oldestFirst = compareBy<PurchaseInstallmentDetail>(
                        { localDateOf(it.purchaseDateMillis) },
                        { it.purchaseId }
                    )
                    arrangeByRegistration(currentCardSummary.purchases, oldestFirst, expenseSortOrder, installmentsPosition) {
                        it.isDeferredOrRecurring(selectedYearMonth)
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

    cardToEdit?.let { editingCard ->
        AddEditCardDialog(
            cardToEdit = editingCard,
            onDismiss = { cardToEdit = null },
            onSave = { name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpend, isFirstInstallmentInterestFree, creditLimit, colorIndex ->
                onUpdateCard(
                    editingCard.copy(
                        name = name,
                        cutoffDay = cutoffDay,
                        paymentDay = paymentDay,
                        interestRate = interestRate,
                        maintenanceFeeCents = toCents(maintenanceFee),
                        currency = currency,
                        isFeeWaiverEnabled = isFeeWaiverEnabled,
                        minSpendForFeeWaiverCents = toCents(minSpend),
                        isFirstInstallmentInterestFree = isFirstInstallmentInterestFree,
                        creditLimitCents = toCents(creditLimit),
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

    val editingPurchase = purchaseToEdit
    if (editingPurchase != null && currentCardSummary != null) {
        AddEditPurchaseDialog(
            cardName = currentCardSummary.cardName,
            cardInterestRate = currentCardSummary.interestRate,
            cardFirstInstallmentInterestFree = currentCardSummary.isFirstInstallmentInterestFree,
            purchaseToEdit = editingPurchase,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { purchaseToEdit = null },
            onSave = { concept, totalAmount, installments, dateMillis, isInterestFree, isRecurring, currency ->
                onUpdatePurchase(
                    CardPurchaseEntity(
                        id = editingPurchase.purchaseId,
                        cardId = editingPurchase.cardId,
                        concept = concept,
                        totalAmountCents = toCents(totalAmount),
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

    val payingPurchase = purchaseToPay
    if (payingPurchase != null && currentCardSummary != null) {
        AddPaymentDialog(
            purchase = payingPurchase,
            onDismiss = { purchaseToPay = null },
            onSave = { amount ->
                val dateMillis = calculateSaveDateForMonth(selectedYearMonth)
                onAddPurchasePayment(payingPurchase.purchaseId, amount, dateMillis)
                purchaseToPay = null
            }
        )
    }

    purchaseToSettle?.let { purchase ->
        val isSettled = purchase.isSettledThisMonth
        val currencySym = currencySymbolOf(purchase.currency)
        val amountFormatted = if (purchase.currency == "USD") {
            formatUsdWithCop(purchase.remainingBalanceToPay, usdRate)
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

    purchaseToDelete?.let { deleting ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text(tr("Eliminar Compra", "Delete purchase")) },
            text = { Text(tr("¿Deseas eliminar '${deleting.concept}'?", "Delete '${deleting.concept}'?")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePurchase(deleting.purchaseId)
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
                            onOmitPurchaseForMonth(deleting.purchaseId)
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
