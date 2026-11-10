package com.example.gestiongastos.ui.screens

import com.example.gestiongastos.data.model.toCents
import com.example.gestiongastos.data.model.markedFirst
import com.example.gestiongastos.i18n.tr
import com.example.gestiongastos.i18n.appLocale
import com.example.gestiongastos.ui.components.rememberAnimatedAmount
import com.example.gestiongastos.ui.components.privateText
import com.example.gestiongastos.ui.components.capyPattern
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.gestiongastos.ui.components.AutoResizedText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.data.local.ExpenseEntity
import com.example.gestiongastos.data.local.IncomeEntity
import com.example.gestiongastos.data.model.CardMonthSummary
import com.example.gestiongastos.ui.components.formatCurrency
import com.example.gestiongastos.ui.components.ConfirmDeleteDialog
import com.example.gestiongastos.data.model.DEFAULT_USD_RATE
import java.time.YearMonth
import java.time.format.TextStyle

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark

@Composable
fun IncomeScreen(
    incomes: List<IncomeEntity>,
    expenses: List<ExpenseEntity> = emptyList(),
    cardSummaries: List<CardMonthSummary> = emptyList(),
    selectedYearMonth: YearMonth,
    usdRate: Double = DEFAULT_USD_RATE,
    isPrivacyMode: Boolean = false,
    receivedIncomeIds: Set<Long> = emptySet(),
    movePaidToTop: Boolean = true,
    onToggleReceivedIncome: (Long) -> Unit = {},
    onAddIncome: (concept: String, amount: Double, dateMillis: Long, isRecurring: Boolean, linkedExpenseIdsText: String?, linkedCardPurchaseIdsText: String?, extraAmount: Double, extraConcept: String?, totalInstallments: Int, totalAmount: Double, endDateMillis: Long?) -> Unit,
    onUpdateIncome: (IncomeEntity) -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
    onEndIncomeFromMonth: (IncomeEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var incomeToEdit by remember { mutableStateOf<IncomeEntity?>(null) }
    var incomeToDelete by remember { mutableStateOf<IncomeEntity?>(null) }
    var viewingReimbursement by remember { mutableStateOf<IncomeEntity?>(null) }
    val isDark = isAppDark()

    val totalIncomeMonth = remember(incomes) { incomes.sumOf { it.amount } }
    val sortedIncomes = remember(incomes, receivedIncomeIds, movePaidToTop) {
        markedFirst(incomes, movePaidToTop) { it.id in receivedIncomeIds }
    }

    Scaffold(
        modifier = modifier,
        // El Scaffold principal ya aplica las barras del sistema; sin esto quedaba un espacio en blanco arriba
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FinanceColors.incomeAccent(isDark),
                contentColor = FinanceColors.onAccent(isDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = tr("Agregar Ingreso", "Add income"))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Registrar Ingreso", "Add income"), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Hero Total Incomes Bento Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .capyPattern(FinanceColors.incomeAccent(isDark).copy(alpha = 0.10f))
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
                                painter = painterResource(id = R.drawable.capy_rich),
                                contentDescription = tr("Capibara Millonario", "Rich capybara"),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tr("Ingresos de ${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }}", "${selectedYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, appLocale()).replaceFirstChar { it.uppercase() }} income"),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceColors.incomeText(isDark)
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = tr("${incomes.size} registro(s) activos", "${incomes.size} active item(s)"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FinanceColors.incomeAccent(isDark),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AutoResizedText(
                        text = privateText(formatCurrency(rememberAnimatedAmount(totalIncomeMonth)), isPrivacyMode, "$ ••••••"),
                        isPrivate = isPrivacyMode,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FinanceColors.incomeText(isDark)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (incomes.isEmpty()) {
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
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("¡Sin ingresos registrados aún!", "No income added yet!"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr("Toca \"Registrar Ingreso\" para registrar tus ganancias 🪙", "Tap \"Add income\" to log your earnings 🪙"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(sortedIncomes, key = { it.id }) { income ->
                        IncomeItemCard(
                            income = income,
                            targetYearMonth = selectedYearMonth,
                            isReceived = income.id in receivedIncomeIds,
                            isPrivacyMode = isPrivacyMode,
                            onToggleReceived = { onToggleReceivedIncome(income.id) },
                            onEdit = { incomeToEdit = income },
                            onDelete = { incomeToDelete = income },
                            onShowReimbursementDetail = { viewingReimbursement = income }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditIncomeDialog(
            incomeToEdit = null,
            expenses = expenses,
            cardSummaries = cardSummaries,
            usdRate = usdRate,
            defaultYearMonth = selectedYearMonth,
            onDismiss = { showAddDialog = false },
            onSave = { concept, amount, dateMillis, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis ->
                onAddIncome(concept, amount, dateMillis, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis)
                showAddDialog = false
            }
        )
    }

    incomeToEdit?.let { editing ->
        AddEditIncomeDialog(
            incomeToEdit = editing,
            expenses = expenses,
            cardSummaries = cardSummaries,
            usdRate = usdRate,
            defaultYearMonth = selectedYearMonth,
            onDismiss = { incomeToEdit = null },
            onSave = { concept, amount, dateMillis, isRecurring, linkedExpenseIdsText, linkedCardPurchaseIdsText, extraAmount, extraConcept, totalInstallments, totalAmount, endDateMillis ->
                onUpdateIncome(
                    editing.copy(
                        concept = concept,
                        amountCents = toCents(amount),
                        date = dateMillis,
                        isRecurring = isRecurring,
                        linkedExpenseIdsText = linkedExpenseIdsText,
                        linkedCardPurchaseIdsText = linkedCardPurchaseIdsText,
                        extraAmountCents = toCents(extraAmount),
                        extraConcept = extraConcept,
                        totalInstallments = totalInstallments,
                        totalAmountCents = toCents(totalAmount),
                        endDateMillis = endDateMillis
                    )
                )
                incomeToEdit = null
            }
        )
    }

    incomeToDelete?.let { income ->
        ConfirmDeleteDialog(
            itemName = income.concept,
            selectedYearMonth = selectedYearMonth,
            onDismiss = { incomeToDelete = null },
            onDeleteAll = { onDeleteIncome(income) },
            onEndFromMonth = if (income.isRecurring && income.totalInstallments <= 1) ({ onEndIncomeFromMonth(income) }) else null
        )
    }

    viewingReimbursement?.let { reimbursement ->
        ReimbursementDetailDialog(
            income = reimbursement,
            expenses = expenses,
            cardSummaries = cardSummaries,
            usdRate = usdRate,
            isPrivacyMode = isPrivacyMode,
            onDismiss = { viewingReimbursement = null }
        )
    }
}
