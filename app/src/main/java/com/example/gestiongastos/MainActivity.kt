package com.example.gestiongastos

import androidx.compose.runtime.key
import com.example.gestiongastos.i18n.tr
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import com.example.gestiongastos.ui.screens.QuickExpenseCoverScreen
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.gestiongastos.ui.theme.isAppDark
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.gestiongastos.data.model.InstallmentStatusType
import com.example.gestiongastos.data.model.ItemKeys
import com.example.gestiongastos.data.model.maintenanceFeeInCop
import com.example.gestiongastos.data.model.payableThisMonth
import com.example.gestiongastos.ui.components.TopMonthSelector
import com.example.gestiongastos.ui.components.PawPrint
import com.example.gestiongastos.ui.components.SleepingCapy
import androidx.compose.foundation.layout.offset
import com.example.gestiongastos.ui.components.exportMonthlyCsvReport
import com.example.gestiongastos.ui.navigation.Screen
import com.example.gestiongastos.ui.navigation.defaultBottomNavScreens
import com.example.gestiongastos.ui.screens.CreditCardScreen
import com.example.gestiongastos.ui.screens.ExpenseScreen
import com.example.gestiongastos.ui.screens.IncomeScreen
import com.example.gestiongastos.ui.screens.SavingsScreen
import com.example.gestiongastos.ui.screens.SummaryScreen
import com.example.gestiongastos.ui.theme.AppColorTheme
import com.example.gestiongastos.ui.theme.AppThemeMode
import com.example.gestiongastos.ui.theme.GestionGastosTheme
import com.example.gestiongastos.ui.viewmodel.FinanceViewModel
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : FragmentActivity() {

    // Misma instancia que obtiene GestionGastosApp con viewModel() (ámbito de la actividad)
    private val financeViewModel: FinanceViewModel by viewModels()

    // Momento en que la app pasó a segundo plano, para volver a bloquearla con huella
    private var backgroundedAtMillis: Long? = null

    fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )

        // BIOMETRIC_STRONG | DEVICE_CREDENTIAL no está soportado en API 28-29 (lanza IllegalArgumentException)
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(tr("Desbloquear CapyCoin", "Unlock CapyCoin"))
            .setSubtitle(tr("Confirma tu huella digital o clave para continuar", "Confirm your fingerprint or passcode to continue"))
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        private const val REQUEST_CODE_RESTORE_BACKUP = 1001

        // Tiempo en segundo plano tras el cual se vuelve a pedir la huella
        private const val RELOCK_TIMEOUT_MILLIS = 30_000L
    }

    override fun onStart() {
        super.onStart()
        val backgroundedAt = backgroundedAtMillis
        backgroundedAtMillis = null
        if (backgroundedAt != null &&
            financeViewModel.isBiometricEnabled.value &&
            SystemClock.elapsedRealtime() - backgroundedAt >= RELOCK_TIMEOUT_MILLIS
        ) {
            financeViewModel.setAppUnlocked(false)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            backgroundedAtMillis = SystemClock.elapsedRealtime()
        }
    }

    private var onRestoreFilePicked: ((Uri) -> Unit)? = null

    fun launchRestoreBackupPicker(
        onPicked: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onRestoreFilePicked = onPicked
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")
            )
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_RESTORE_BACKUP)
        } catch (e1: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(
                    Intent.createChooser(fallbackIntent, tr("Seleccionar copia de seguridad", "Select backup file")),
                    REQUEST_CODE_RESTORE_BACKUP
                )
            } catch (e2: Exception) {
                onError(tr("No se pudo abrir el selector de archivos: ${e2.localizedMessage ?: e2.message}", "Couldn't open the file picker: ${e2.localizedMessage ?: e2.message}"))
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_RESTORE_BACKUP && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Safe to ignore if content provider doesn't support persistable permissions
                }
                val callback = onRestoreFilePicked
                onRestoreFilePicked = null
                callback?.invoke(uri)
            }
        }
    }

    var quickExpenseRequested by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("extra_mode") == "quick_expense") {
            quickExpenseRequested = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent?.getStringExtra("extra_mode") == "quick_expense") {
            quickExpenseRequested = true
        }

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val initialModeName = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val initialColorName = prefs.getString("color_theme", AppColorTheme.YELLOW.name) ?: AppColorTheme.YELLOW.name
        val initialMode = try { AppThemeMode.valueOf(initialModeName) } catch (e: Exception) { AppThemeMode.SYSTEM }
        val initialColor = try { AppColorTheme.valueOf(initialColorName) } catch (e: Exception) { AppColorTheme.YELLOW }

        setContent {
            var themeMode by remember { mutableStateOf(initialMode) }
            var colorTheme by remember { mutableStateOf(initialColor) }

            // Al cambiar el idioma se vuelve a crear toda la interfaz con los textos nuevos
            val language by financeViewModel.language.collectAsState()
            key(language) {
            GestionGastosTheme(themeMode = themeMode, colorTheme = colorTheme) {
                GestionGastosApp(
                    viewModel = financeViewModel,
                    quickExpenseRequested = quickExpenseRequested,
                    onQuickExpenseHandled = { quickExpenseRequested = false },
                    currentThemeMode = themeMode,
                    onThemeModeSelected = { mode ->
                        themeMode = mode
                        prefs.edit().putString("theme_mode", mode.name).apply()
                    },
                    currentColorTheme = colorTheme,
                    onColorThemeSelected = { color ->
                        colorTheme = color
                        prefs.edit().putString("color_theme", color.name).apply()
                    }
                )
            }
            }
        }
    }
}

private fun loadBottomNavOrder(prefs: SharedPreferences): List<Screen> {
    val raw = prefs.getString("bottom_nav_order", null) ?: return defaultBottomNavScreens
    val routes = raw.split(",").map { it.trim() }
    val ordered = routes.mapNotNull { route -> defaultBottomNavScreens.find { it.route == route } }
    val remaining = defaultBottomNavScreens.filter { it !in ordered }
    return if (ordered.isNotEmpty()) ordered + remaining else defaultBottomNavScreens
}

@Composable
fun GestionGastosApp(
    quickExpenseRequested: Boolean = false,
    onQuickExpenseHandled: () -> Unit = {},
    currentThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    currentColorTheme: AppColorTheme = AppColorTheme.YELLOW,
    onColorThemeSelected: (AppColorTheme) -> Unit = {},
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var bottomScreens by remember { mutableStateOf(loadBottomNavOrder(prefs)) }
    var pagerScreens by remember { mutableStateOf(bottomScreens) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pagerScreens.size })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var draggedBottomIndex by remember { mutableStateOf<Int?>(null) }
    var bottomDragOffsetX by remember { mutableFloatStateOf(0f) }
    var bottomBarWidthPx by remember { mutableFloatStateOf(0f) }

    fun saveBottomNavOrder(newScreens: List<Screen>) {
        bottomScreens = newScreens
        prefs.edit().putString("bottom_nav_order", newScreens.joinToString(",") { it.route }).apply()
    }

    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var showBackupResultDialog by remember { mutableStateOf(false) }

    val handleRestoreResult: (Result<String>) -> Unit = { result ->
        result.onSuccess { msg ->
            backupStatusMessage = msg
            showBackupResultDialog = true
        }.onFailure { err ->
            backupStatusMessage = tr("Error al restaurar copia: ${err.localizedMessage ?: err.message}", "Couldn't restore the backup: ${err.localizedMessage ?: err.message}")
            showBackupResultDialog = true
        }
    }



    val selectedYearMonth by viewModel.selectedYearMonth.collectAsState()
    val summaryUiState by viewModel.summaryUiState.collectAsState()
    val monthlyIncomes by viewModel.monthlyIncomes.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val monthlySavings by viewModel.monthlySavings.collectAsState()
    val cardSummaries by viewModel.cardSummaries.collectAsState()
    val allCards by viewModel.allCreditCards.collectAsState()
    val usdRate by viewModel.usdRate.collectAsState()
    val ignoredKeys by viewModel.ignoredExpenseKeys.collectAsState()
    val paidCardIds by viewModel.paidCreditCardIds.collectAsState()
    val paidExpenseKeys by viewModel.paidExpenseKeys.collectAsState()
    val receivedIncomeIds by viewModel.receivedIncomeIds.collectAsState()
    val installmentsPosition by viewModel.installmentsPosition.collectAsState()
    val expenseSortOrder by viewModel.expenseSortOrder.collectAsState()
    val movePaidToTop by viewModel.movePaidToTop.collectAsState()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()

    // Al desbloquear, el capibara dormido se despierta (bostezo) antes de mostrar la app
    var isCapyWakingUp by remember { mutableStateOf(false) }
    LaunchedEffect(isCapyWakingUp) {
        if (isCapyWakingUp) {
            kotlinx.coroutines.delay(950)
            viewModel.setAppUnlocked(true)
            isCapyWakingUp = false
        }
    }

    // Pide la huella al abrir la app y cada vez que se vuelve a bloquear
    LaunchedEffect(isBiometricEnabled, isAppUnlocked) {
        if (isBiometricEnabled && !isAppUnlocked) {
            activity?.showBiometricPrompt(
                onSuccess = { isCapyWakingUp = true }
            )
        }
    }

    // ¿Está pagado todo lo que cuenta este mes? (los ítems ignorados y lo que no cobra nada no bloquean)
    val isAllPaid = remember(monthlyExpenses, monthlySavings, cardSummaries, paidExpenseKeys, paidCardIds, ignoredKeys, usdRate) {
        val pendingDirect = monthlyExpenses.filter { ItemKeys.direct(it.id) !in ignoredKeys }
        val pendingSavings = monthlySavings.filter { ItemKeys.saving(it.id) !in ignoredKeys }
        val cardsWithCharges = cardSummaries.filter { it.payableThisMonth(usdRate, ignoredKeys) > 0.0 }

        if (pendingDirect.isEmpty() && pendingSavings.isEmpty() && cardsWithCharges.isEmpty()) {
            false
        } else {
            val directPaid = pendingDirect.all { ItemKeys.direct(it.id) in paidExpenseKeys }
            val savingsPaid = pendingSavings.all { ItemKeys.saving(it.id) in paidExpenseKeys }
            val cardsPaid = cardsWithCharges.all { summary ->
                if (summary.cardId in paidCardIds) {
                    true
                } else {
                    val feeKey = ItemKeys.cardFee(summary.cardId)
                    val feePaid = summary.maintenanceFeeInCop(usdRate) <= 0.0 || feeKey in ignoredKeys || feeKey in paidExpenseKeys
                    val installmentsPaid = summary.purchases
                        .filter { it.statusType == InstallmentStatusType.ACTIVE && it.monthlyPayment > 0.0 }
                        .map { ItemKeys.cardInstallment(it.purchaseId) }
                        .filter { it !in ignoredKeys }
                        .all { it in paidExpenseKeys }
                    feePaid && installmentsPaid
                }
            }
            directPaid && savingsPaid && cardsPaid
        }
    }

    val navigateToScreen: (Screen) -> Unit = { targetScreen ->
        val index = pagerScreens.indexOfFirst { it.route == targetScreen.route }
        if (index >= 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    }

    val configuration = LocalConfiguration.current
    // View.getDisplay() existe desde API 17 (Context.getDisplay() requiere API 30)
    val currentDisplayId = LocalView.current.display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
    val largestSideDp = maxOf(configuration.screenWidthDp, configuration.screenHeightDp)
    val smallestSideDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(1)
    val sidesRatio = largestSideDp.toFloat() / smallestSideDp
    // Pantalla exterior de plegables tipo Flip: pequeña en AMBOS lados y casi cuadrada.
    // Un teléfono en horizontal, una tablet o la pantalla interior de un Fold no cumplen esto.
    val isCoverScreenDevice = largestSideDp < 600 &&
        (sidesRatio < 1.35f || currentDisplayId != android.view.Display.DEFAULT_DISPLAY)

    var userForcedFullApp by remember { mutableStateOf(false) }

    var lastIsCover by remember { mutableStateOf(isCoverScreenDevice) }
    LaunchedEffect(isCoverScreenDevice) {
        if (isCoverScreenDevice && !lastIsCover) {
            // Se cerró el teléfono volviendo a la pantalla exterior
            userForcedFullApp = false
        }
        lastIsCover = isCoverScreenDevice
    }

    val showQuickCoverScreen = isCoverScreenDevice && !userForcedFullApp

    // Atajo "Registrar Gasto" del ícono: abre el formulario de gasto (o el Modo Express en pantalla exterior)
    var pendingAddExpenseDialog by remember { mutableStateOf(false) }
    LaunchedEffect(quickExpenseRequested, showQuickCoverScreen) {
        if (quickExpenseRequested) {
            if (!showQuickCoverScreen) {
                val gastosIndex = pagerScreens.indexOfFirst { it.route == Screen.Gastos.route }
                if (gastosIndex >= 0) pagerState.scrollToPage(gastosIndex)
                pendingAddExpenseDialog = true
            }
            onQuickExpenseHandled()
        }
    }

    if (showQuickCoverScreen) {
        QuickExpenseCoverScreen(
            summaryState = summaryUiState,
            allCards = allCards,
            selectedYearMonth = selectedYearMonth,
            isPrivacyMode = isPrivacyModeEnabled,
            recentExpenses = monthlyExpenses,
            onSaveExpense = { concept, amount, dateMillis, isRecurring, currency ->
                viewModel.addExpense(concept, amount, dateMillis, isRecurring, currency)
            },
            onSaveCardPurchase = { cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency ->
                viewModel.addCardPurchase(cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency)
            },
            onOpenFullApp = {
                userForcedFullApp = true
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (isCoverScreenDevice && userForcedFullApp) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                userForcedFullApp = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("Modo Express", "Express mode"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            },
            topBar = {
            TopMonthSelector(
                selectedYearMonth = selectedYearMonth,
                onYearMonthSelected = { viewModel.setSelectedYearMonth(it) },
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onRefresh = { viewModel.refreshData() },
                currentThemeMode = currentThemeMode,
                onThemeModeSelected = onThemeModeSelected,
                currentColorTheme = currentColorTheme,
                onColorThemeSelected = onColorThemeSelected,
                currentInstallmentsPosition = installmentsPosition,
                onInstallmentsPositionSelected = { viewModel.setInstallmentsPosition(it) },
                currentExpenseSortOrder = expenseSortOrder,
                onExpenseSortOrderSelected = { viewModel.setExpenseSortOrder(it) },
                isMovePaidToTop = movePaidToTop,
                onToggleMovePaidToTop = { viewModel.setMovePaidToTop(it) },
                isPrivacyMode = isPrivacyModeEnabled,
                onTogglePrivacyMode = { viewModel.togglePrivacyMode() },
                isBiometricEnabled = isBiometricEnabled,
                onToggleBiometric = { enabled ->
                    if (enabled) {
                        activity?.showBiometricPrompt(
                            onSuccess = {
                                viewModel.setBiometricEnabled(true)
                                viewModel.setAppUnlocked(true)
                            }
                        )
                    } else {
                        viewModel.setBiometricEnabled(false)
                    }
                },
                onExportBackup = {
                    viewModel.exportBackup(context) { result ->
                        result.onSuccess { msg ->
                            backupStatusMessage = msg
                            showBackupResultDialog = true
                        }.onFailure { err ->
                            backupStatusMessage = tr("Error al exportar copia: ${err.message}", "Couldn't export the backup: ${err.message}")
                            showBackupResultDialog = true
                        }
                    }
                },
                onRestoreBackup = {
                    if (activity != null) {
                        activity.launchRestoreBackupPicker(
                            onPicked = { uri ->
                                viewModel.restoreBackup(context, uri, handleRestoreResult)
                            },
                            onError = { errMsg ->
                                backupStatusMessage = errMsg
                                showBackupResultDialog = true
                            }
                        )
                    } else {
                        backupStatusMessage = tr("No se pudo iniciar el selector de archivos.", "Couldn't open the file picker.")
                        showBackupResultDialog = true
                    }
                },
                onExportCsv = {
                    exportMonthlyCsvReport(
                        context = context,
                        incomes = monthlyIncomes,
                        expenses = monthlyExpenses,
                        cardSummaries = cardSummaries,
                        savings = monthlySavings,
                        selectedYearMonth = selectedYearMonth,
                        usdRate = usdRate,
                        ignoredKeys = ignoredKeys
                    )
                },
                onShareAppApk = {
                    shareAppApk(context)
                },
                currentLanguage = viewModel.language.collectAsState().value,
                onLanguageSelected = { viewModel.setLanguage(it) }
            )
        },
        bottomBar = {
            val isDark = isAppDark()
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .onSizeChanged { bottomBarWidthPx = it.width.toFloat() },
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabWidthPx = if (bottomBarWidthPx > 0 && bottomScreens.isNotEmpty()) {
                        bottomBarWidthPx / bottomScreens.size
                    } else 150f

                    val currentViewingRoute = pagerScreens.getOrNull(pagerState.currentPage)?.route

                    val targetIndex = remember(draggedBottomIndex, bottomDragOffsetX, tabWidthPx, bottomScreens.size) {
                        if (draggedBottomIndex != null && tabWidthPx > 0) {
                            (draggedBottomIndex!! + (bottomDragOffsetX / tabWidthPx).roundToInt())
                                .coerceIn(0, bottomScreens.size - 1)
                        } else {
                            draggedBottomIndex
                        }
                    }

                    var lastTargetIndex by remember { mutableStateOf<Int?>(null) }
                    LaunchedEffect(targetIndex) {
                        if (draggedBottomIndex != null && targetIndex != null && targetIndex != lastTargetIndex) {
                            if (lastTargetIndex != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            lastTargetIndex = targetIndex
                        }
                    }

                    bottomScreens.forEachIndexed { index, screen ->
                        val isSelected = currentViewingRoute == screen.route
                        val isDragging = draggedBottomIndex == index

                        val targetShiftX = when {
                            isDragging -> 0f
                            draggedBottomIndex != null && targetIndex != null -> {
                                val from = draggedBottomIndex!!
                                val to = targetIndex
                                if (from < to && index in (from + 1)..to) {
                                    -tabWidthPx
                                } else if (from > to && index in to until from) {
                                    tabWidthPx
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
                            label = "tabShift_$index"
                        )

                        val currentTranslationX = if (isDragging) bottomDragOffsetX else animatedShiftX

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .zIndex(if (isDragging) 10f else 1f)
                                .graphicsLayer {
                                    translationX = currentTranslationX
                                    if (isDragging) {
                                        scaleX = 1.14f
                                        scaleY = 1.14f
                                    }
                                }
                                .clip(CircleShape)
                                .background(
                                    if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                                    else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .pointerInput(screen.route) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedBottomIndex = index
                                            bottomDragOffsetX = 0f
                                            lastTargetIndex = index
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            bottomDragOffsetX += dragAmount.x
                                        },
                                        onDragEnd = {
                                            val from = draggedBottomIndex
                                            val curTabWidth = if (bottomBarWidthPx > 0 && bottomScreens.isNotEmpty()) {
                                                bottomBarWidthPx / bottomScreens.size
                                            } else 150f
                                            val to = if (from != null && curTabWidth > 0) {
                                                (from + (bottomDragOffsetX / curTabWidth).roundToInt())
                                                    .coerceIn(0, bottomScreens.size - 1)
                                            } else null

                                            if (from != null && to != null && from != to) {
                                                val activeRoute = pagerScreens.getOrNull(pagerState.currentPage)?.route
                                                val mutable = bottomScreens.toMutableList()
                                                val item = mutable.removeAt(from)
                                                mutable.add(to, item)
                                                bottomScreens = mutable
                                                pagerScreens = mutable
                                                saveBottomNavOrder(mutable)
                                                if (activeRoute != null) {
                                                    val newPageIndex = mutable.indexOfFirst { it.route == activeRoute }
                                                    if (newPageIndex >= 0 && newPageIndex != pagerState.currentPage) {
                                                        coroutineScope.launch { pagerState.scrollToPage(newPageIndex) }
                                                    }
                                                }
                                            }
                                            draggedBottomIndex = null
                                            bottomDragOffsetX = 0f
                                            lastTargetIndex = null
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragCancel = {
                                            draggedBottomIndex = null
                                            bottomDragOffsetX = 0f
                                            lastTargetIndex = null
                                        }
                                    )
                                }
                                .clickable {
                                    if (draggedBottomIndex == null) {
                                        val targetScreenIndex = pagerScreens.indexOfFirst { it.route == screen.route }
                                        if (targetScreenIndex >= 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(targetScreenIndex)
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Huellita de capibara que marca la pestaña seleccionada
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isSelected,
                                        enter = androidx.compose.animation.scaleIn(
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                                        ) + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 9.dp, y = (-5).dp)
                                    ) {
                                        PawPrint(
                                            color = MaterialTheme.colorScheme.primary,
                                            rotation = 20f,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (pagerScreens.getOrNull(page)) {
                Screen.Resumen -> {
                    SummaryScreen(
                        summaryState = summaryUiState,
                        monthlySavings = monthlySavings,
                        selectedYearMonth = selectedYearMonth,
                        ignoredKeys = ignoredKeys,
                        cardSummaries = cardSummaries,
                        monthlyExpenses = monthlyExpenses,
                        paidCardIds = paidCardIds,
                        usdRate = usdRate,
                        isPrivacyMode = isPrivacyModeEnabled,
                        isAllPaid = isAllPaid,
                        onNavigateToScreen = navigateToScreen,
                        onAddSavings = { concept, amount, dateMillis, isRecurring ->
                            viewModel.addSavings(concept, amount, dateMillis, isRecurring)
                        },
                        onUpdateSavings = { viewModel.updateSavings(it) },
                        onDeleteSavings = { viewModel.deleteSavings(it) },
                        onEndSavingFromMonth = { viewModel.endSavingFromSelectedMonth(it) }
                    )
                }

                Screen.Ingresos -> {
                    IncomeScreen(
                        incomes = monthlyIncomes,
                        expenses = monthlyExpenses,
                        cardSummaries = cardSummaries,
                        selectedYearMonth = selectedYearMonth,
                        usdRate = usdRate,
                        isPrivacyMode = isPrivacyModeEnabled,
                        receivedIncomeIds = receivedIncomeIds,
                        movePaidToTop = movePaidToTop,
                        onToggleReceivedIncome = { viewModel.toggleReceivedIncome(it) },
                        onAddIncome = { concept, amount, dateMillis, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis ->
                            viewModel.addIncome(concept, amount, dateMillis, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis)
                        },
                        onUpdateIncome = { viewModel.updateIncome(it) },
                        onDeleteIncome = { viewModel.deleteIncome(it) },
                        onEndIncomeFromMonth = { viewModel.endIncomeFromSelectedMonth(it) }
                    )
                }

                Screen.Gastos -> {
                    ExpenseScreen(
                        expenses = monthlyExpenses,
                        cardSummaries = cardSummaries,
                        savings = monthlySavings,
                        allCards = allCards,
                        selectedYearMonth = selectedYearMonth,
                        usdRate = usdRate,
                        ignoredKeys = ignoredKeys,
                        paidExpenseKeys = paidExpenseKeys,
                        isPrivacyMode = isPrivacyModeEnabled,
                        movePaidToTop = movePaidToTop,
                        installmentsPosition = installmentsPosition,
                        onInstallmentsPositionSelected = { viewModel.setInstallmentsPosition(it) },
                        expenseSortOrder = expenseSortOrder,
                        onExpenseSortOrderSelected = { viewModel.setExpenseSortOrder(it) },
                        openAddDialogRequest = pendingAddExpenseDialog,
                        onAddDialogRequestHandled = { pendingAddExpenseDialog = false },
                        onToggleIgnoreExpense = { viewModel.toggleIgnoredExpense(it) },
                        onTogglePaidExpense = { viewModel.togglePaidExpense(it) },
                        onAddExpense = { concept, amount, dateMillis, isRecurring, currency ->
                            viewModel.addExpense(concept, amount, dateMillis, isRecurring, currency)
                        },
                        onUpdateExpense = { viewModel.updateExpense(it) },
                        onDeleteExpense = { viewModel.deleteExpense(it) },
                        onEndExpenseFromMonth = { viewModel.endExpenseFromSelectedMonth(it) },
                        onEndSavingFromMonth = { viewModel.endSavingFromSelectedMonth(it) },
                        onAddCardPurchase = { cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency ->
                            viewModel.addCardPurchase(cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency)
                        },
                        onUpdateCardPurchase = { viewModel.updateCardPurchase(it) },
                        onDeleteCardPurchase = { viewModel.deleteCardPurchase(it) },
                        onAddSavings = { concept, amount, dateMillis, isRecurring ->
                            viewModel.addSavings(concept, amount, dateMillis, isRecurring)
                        },
                        onUpdateSavings = { viewModel.updateSavings(it) },
                        onDeleteSavings = { viewModel.deleteSavings(it) }
                    )
                }

                Screen.Ahorros -> {
                    SavingsScreen(
                        savings = monthlySavings,
                        ignoredKeys = ignoredKeys,
                        onEndSavingFromMonth = { viewModel.endSavingFromSelectedMonth(it) },
                        selectedYearMonth = selectedYearMonth,
                        isPrivacyMode = isPrivacyModeEnabled,
                        onAddSavings = { concept, amount, dateMillis, isRecurring ->
                            viewModel.addSavings(concept, amount, dateMillis, isRecurring)
                        },
                        onUpdateSavings = { viewModel.updateSavings(it) },
                        onDeleteSavings = { viewModel.deleteSavings(it) }
                    )
                }

                Screen.Tarjetas -> {
                    CreditCardScreen(
                        cardSummaries = cardSummaries,
                        allCards = allCards,
                        selectedYearMonth = selectedYearMonth,
                        usdRate = usdRate,
                        paidCardIds = paidCardIds,
                        ignoredKeys = ignoredKeys,
                        isPrivacyMode = isPrivacyModeEnabled,
                        installmentsPosition = installmentsPosition,
                        expenseSortOrder = expenseSortOrder,
                        onTogglePaidCard = { viewModel.togglePaidCreditCard(it) },
                        onAddCard = { name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpendForFeeWaiver, isFirstInstallmentInterestFree, creditLimit, colorIndex ->
                            viewModel.addCreditCard(name, cutoffDay, paymentDay, interestRate, maintenanceFee, currency, isFeeWaiverEnabled, minSpendForFeeWaiver, isFirstInstallmentInterestFree, creditLimit, colorIndex)
                        },
                        onUpdateCard = { viewModel.updateCreditCard(it) },
                        onDeleteCard = { viewModel.deleteCreditCard(it) },
                        onAddPurchase = { cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency ->
                            viewModel.addCardPurchase(cardId, concept, totalAmount, installments, purchaseDateMillis, isInterestFree, isRecurring, currency)
                        },
                        onUpdatePurchase = { viewModel.updateCardPurchase(it) },
                        onDeletePurchase = { viewModel.deleteCardPurchase(it) },
                        onAddPurchasePayment = { purchaseId, amount, dateMillis ->
                            viewModel.addCardPurchasePayment(purchaseId, amount, dateMillis, selectedYearMonth)
                        },
                        onOmitPurchaseForMonth = { viewModel.omitCardPurchaseInstallmentForMonth(it, selectedYearMonth) },
                        onToggleSettlePurchase = { viewModel.toggleSettleCardPurchase(it) },
                        onReorderCards = { viewModel.setCardOrder(it) }
                    )
                }

                null -> {}
            }
        }
    }
    }

    // Biometric Fullscreen Lock Overlay
    if (isBiometricEnabled && !isAppUnlocked) {
        val isCompact = configuration.screenHeightDp < 500
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                // Consume los toques para que no lleguen a la app que está debajo del bloqueo
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(if (isCompact) 16.dp else 32.dp)
            ) {
                SleepingCapy(
                    isAwake = isCapyWakingUp,
                    modifier = Modifier.size(if (isCompact) 120.dp else 220.dp)
                )
                Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 12.dp))
                Text(
                    text = if (isCapyWakingUp) tr("¡Buenos días! ☀️", "Good morning! ☀️") else tr("El capy está dormidito", "The capy is fast asleep"),
                    style = (if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall).copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!isCompact) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isCapyWakingUp) tr("Abriendo tus finanzas…", "Opening your finances…") else tr("Despiértalo con tu huella o tu clave para ver tus finanzas", "Wake it up with your fingerprint or passcode to see your finances"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(if (isCompact) 14.dp else 28.dp))
                Button(
                    onClick = {
                        activity?.showBiometricPrompt(
                            onSuccess = { isCapyWakingUp = true }
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth(if (isCompact) 0.95f else 0.8f)
                        .height(if (isCompact) 44.dp else 50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Despertar al capy", "Wake the capy"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showBackupResultDialog && backupStatusMessage != null) {
        AlertDialog(
            onDismissRequest = { showBackupResultDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Copia de Seguridad", "Backup"), style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Text(
                    text = backupStatusMessage!!,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showBackupResultDialog = false }) {
                    Text(tr("Aceptar", "OK"))
                }
            }
        )
    }
}

fun shareAppApk(context: Context) {
    try {
        val appInfo = context.applicationInfo
        val sourceApk = File(appInfo.sourceDir)
        if (!sourceApk.exists()) {
            Toast.makeText(context, tr("No se encontró el archivo de la aplicación", "Couldn't find the app file"), Toast.LENGTH_SHORT).show()
            return
        }

        val shareDir = File(context.cacheDir, "shared_apk")
        if (!shareDir.exists()) shareDir.mkdirs()
        val targetApk = File(shareDir, "CapyCoin.apk")
        sourceApk.copyTo(targetApk, overwrite = true)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetApk
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, tr("Compartir APK", "Share APK")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, tr("Error al compartir la aplicación: ${e.localizedMessage}", "Couldn't share the app: ${e.localizedMessage}"), Toast.LENGTH_LONG).show()
    }
}