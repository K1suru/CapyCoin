package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Hoja inferior para formularios (reemplaza a los AlertDialog largos).
// Tiene los mismos parámetros que AlertDialog para que el cambio sea directo.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            if (icon != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { icon() }
                Spacer(modifier = Modifier.height(8.dp))
            }
            ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)) {
                title()
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.weight(1f, fill = false)) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    text()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Campo de monto con formato en vivo: al escribir 1500000 se ve "$ 1.500.000".
// El valor interno son solo dígitos (y ",dd" si se permiten decimales),
// compatible con parseAmount().
// ---------------------------------------------------------------------------

private const val MAX_AMOUNT_DIGITS = 13

/** Limpia lo que escribe o pega el usuario: dígitos y, si se permite, una coma con 2 decimales. */
fun sanitizeAmountInput(input: String, allowDecimals: Boolean): String {
    val separators = input.count { it == '.' || it == ',' }
    val lastSep = input.indexOfLast { it == '.' || it == ',' }
    val decimalsAfterLast = if (lastSep >= 0) input.substring(lastSep + 1).count { it.isDigit() } else 0
    // Un solo separador seguido de 0-2 dígitos = decimal; si no, eran separadores de miles
    val hasDecimal = allowDecimals && separators == 1 && decimalsAfterLast <= 2
    val intPart = (if (hasDecimal) input.substring(0, lastSep) else input).filter { it.isDigit() }
        .trimStart('0').take(MAX_AMOUNT_DIGITS)
    if (!hasDecimal) return intPart
    val decimals = input.substring(lastSep + 1).filter { it.isDigit() }.take(2)
    return (intPart.ifEmpty { "0" }) + "," + decimals
}

/** Convierte un monto guardado al formato interno del campo (para editar). */
fun amountToInput(amount: Double?, allowDecimals: Boolean = false): String {
    if (amount == null || amount <= 0.0) return ""
    val rounded = Math.round(amount * 100) / 100.0
    return if (allowDecimals && rounded % 1.0 != 0.0) {
        String.format(java.util.Locale.US, "%.2f", rounded).trimEnd('0').replace('.', ',')
    } else {
        Math.round(amount).toString()
    }
}

private class AmountVisualTransformation(private val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        val commaIndex = raw.indexOf(',')
        val intLength = if (commaIndex >= 0) commaIndex else raw.length
        val out = StringBuilder(prefix)
        val originalToTransformed = IntArray(raw.length + 1)
        for (i in raw.indices) {
            if (i in 1 until intLength && (intLength - i) % 3 == 0) out.append('.')
            originalToTransformed[i] = out.length
            out.append(raw[i])
        }
        originalToTransformed[raw.length] = out.length

        val transformedToOriginal = IntArray(out.length + 1)
        var j = 0
        for (p in 0..out.length) {
            while (j < raw.length && originalToTransformed[j + 1] <= p) j++
            transformedToOriginal[p] = j
        }

        return TransformedText(AnnotatedString(out.toString()), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) =
                originalToTransformed[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int) =
                transformedToOriginal[offset.coerceIn(0, out.length)]
        })
    }
}

@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    currency: String = "COP",
    prominent: Boolean = false,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null
) {
    val allowDecimals = currency == "USD"
    val prefix = if (currency == "USD") "US$ " else "$ "
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(sanitizeAmountInput(it, allowDecimals)) },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText,
        singleLine = true,
        visualTransformation = AmountVisualTransformation(prefix),
        keyboardOptions = KeyboardOptions(keyboardType = if (allowDecimals) KeyboardType.Decimal else KeyboardType.Number),
        textStyle = if (prominent) {
            MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        } else {
            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        },
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// ---------------------------------------------------------------------------
// Montos animados: al cambiar de mes o registrar algo, el número "cuenta" hasta el nuevo valor.
// Se interpola en Double para no perder precisión con montos grandes.
// ---------------------------------------------------------------------------

@Composable
fun rememberAnimatedAmount(target: Double, durationMillis: Int = 650): Double {
    var from by remember { mutableDoubleStateOf(target) }
    var to by remember { mutableDoubleStateOf(target) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target != to) {
            from = from + (to - from) * progress.value
            to = target
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis, easing = FastOutSlowInEasing))
        }
    }
    return if (progress.value >= 1f) to else from + (to - from) * progress.value
}

// ---------------------------------------------------------------------------
// Modo privado: desenfoca los montos (Android 12+). En versiones anteriores el
// desenfoque no existe, así que el texto se reemplaza por puntos.
// ---------------------------------------------------------------------------

private val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun Modifier.privacyBlur(enabled: Boolean): Modifier =
    if (enabled && blurSupported) this.blur(9.dp, BlurredEdgeTreatment.Unbounded) else this

fun privateText(text: String, enabled: Boolean, mask: String = "$ ••••••"): String =
    if (enabled && !blurSupported) mask else text

// ---------------------------------------------------------------------------
// Calendario Material 3 (sigue los colores y la fuente de la app).
// rememberDatePicker(fecha) { nuevaFecha -> ... }.show()
// ---------------------------------------------------------------------------

class DatePickerLauncher {
    var isVisible by mutableStateOf(false)
    fun show() {
        isVisible = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDatePicker(selectedMillis: Long, onDatePicked: (Long) -> Unit): DatePickerLauncher {
    val launcher = remember { DatePickerLauncher() }
    if (launcher.isVisible) {
        // El DatePicker trabaja en UTC: se convierte la fecha local a medianoche UTC y de vuelta
        val localDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { launcher.isVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utcMillis ->
                        val picked = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDatePicked(picked.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    }
                    launcher.isVisible = false
                }) { Text(tr("Aceptar", "OK"), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { launcher.isVisible = false }) { Text(tr("Cancelar", "Cancel")) }
            }
        ) {
            DatePicker(state = state)
        }
    }
    return launcher
}
