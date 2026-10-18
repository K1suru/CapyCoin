package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.R
import com.example.gestiongastos.ui.theme.FinanceColors
import com.example.gestiongastos.ui.theme.isAppDark

/** Ánimo del capibara según cuánto de los ingresos ya se usó. */
enum class CapyMood(private val es: String, private val en: String) {
    RELAXED("El capibara está tranquilito bañándose", "The capybara is relaxing in the bath"),
    CAUTIOUS("Al capibara le está subiendo el agua", "The water is rising on the capybara"),
    WORRIED("¡Al capibara ya le llega al cuello!", "The water is up to the capybara's neck!"),
    PANIC("¡Se va a ahogar el capibara!", "The capybara is going under!");

    val message: String get() = tr(es, en)
}

/** Caritas del capibara (usadas en consejos y estados). */
enum class CapyFace { HAPPY, SHINY, WORRIED, PANIC, SLEEPING, AWAKE }

fun CapyMood.face(): CapyFace = when (this) {
    CapyMood.RELAXED -> CapyFace.HAPPY
    CapyMood.CAUTIOUS -> CapyFace.SHINY
    CapyMood.WORRIED -> CapyFace.WORRIED
    CapyMood.PANIC -> CapyFace.PANIC
}

fun CapyMood.drawableRes(): Int = when (this) {
    CapyMood.RELAXED -> R.drawable.capy_onsen_relaxed
    CapyMood.CAUTIOUS -> R.drawable.capy_onsen_cautious
    CapyMood.WORRIED -> R.drawable.capy_onsen_worried
    CapyMood.PANIC -> R.drawable.capy_onsen_panic
}

fun CapyFace.drawableRes(): Int = when (this) {
    CapyFace.HAPPY -> R.drawable.capy_rich
    CapyFace.SHINY -> R.drawable.capy_savings
    CapyFace.WORRIED -> R.drawable.capy_question
    CapyFace.PANIC -> R.drawable.capy_poor
    CapyFace.SLEEPING -> R.drawable.capy_sleeping
    CapyFace.AWAKE -> R.drawable.capy_awake
}

fun capyMoodFor(spentFraction: Float): CapyMood = when {
    spentFraction > 1.0f -> CapyMood.PANIC
    spentFraction >= 0.9f -> CapyMood.WORRIED
    spentFraction >= 0.7f -> CapyMood.CAUTIOUS
    else -> CapyMood.RELAXED
}

/** Color del agua: tibia y tranquila al inicio, cada vez más caliente al acercarse al límite. */
fun onsenWaterColor(mood: CapyMood, isDark: Boolean): Color = when (mood) {
    CapyMood.RELAXED -> if (isDark) Color(0xFF3E7F77) else Color(0xFF9CD5C9)
    CapyMood.CAUTIOUS -> if (isDark) Color(0xFFB98A3E) else Color(0xFFF4C27A)
    CapyMood.WORRIED -> if (isDark) Color(0xFFC27436) else Color(0xFFEE9B4F)
    CapyMood.PANIC -> if (isDark) Color(0xFFB4533A) else Color(0xFFD9663F)
}

/**
 * Aro de progreso con la ilustración del capibara en el onsen según el % de ingresos usado.
 */
@Composable
fun CapyOnsen(spentFraction: Float, modifier: Modifier = Modifier) {
    val isDark = isAppDark()
    val mood = capyMoodFor(spentFraction)
    val animatedProgress by animateFloatAsState(
        targetValue = spentFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "onsenProgress"
    )
    val targetRingColor = when (mood) {
        CapyMood.RELAXED -> FinanceColors.incomeText(isDark)
        CapyMood.CAUTIOUS -> FinanceColors.goldText(isDark)
        CapyMood.WORRIED -> FinanceColors.savingsText(isDark)
        CapyMood.PANIC -> FinanceColors.expenseText(isDark)
    }
    val ringColor by animateColorAsState(
        targetValue = targetRingColor,
        animationSpec = tween(durationMillis = 600),
        label = "onsenRingColor"
    )
    val trackColor = if (isDark) Color(0xFF2C2824) else ringColor.copy(alpha = 0.16f)

    val transition = rememberInfiniteTransition(label = "onsenFloat")
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(204.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = 12.dp.toPx()
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidthPx)
                )
                if (animatedProgress > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidthPx,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            AnimatedContent(
                targetState = mood,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label = "onsenMoodImage"
            ) { currentMood ->
                Image(
                    painter = painterResource(id = currentMood.drawableRes()),
                    contentDescription = currentMood.message,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(168.dp)
                        .clip(CircleShape)
                        .graphicsLayer {
                            translationY = bob * 2.5.dp.toPx()
                        }
                )
            }
        }
    }
}
