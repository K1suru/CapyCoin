package com.example.gestiongastos.ui.components

import com.example.gestiongastos.i18n.tr
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.gestiongastos.R

/** Huellita de capibara: almohadilla grande y 4 deditos. [size] es el alto total. */
fun DrawScope.drawPaw(center: Offset, size: Float, color: Color, rotation: Float = 0f) {
    rotate(rotation, pivot = center) {
        val pad = size * 0.5f
        drawOval(color, topLeft = center + Offset(-pad * 0.55f, -pad * 0.05f), size = Size(pad * 1.1f, pad * 0.85f))
        val toe = size * 0.17f
        listOf(
            Offset(-pad * 0.62f, -pad * 0.32f),
            Offset(-pad * 0.22f, -pad * 0.62f),
            Offset(pad * 0.22f, -pad * 0.62f),
            Offset(pad * 0.62f, -pad * 0.32f)
        ).forEach { o -> drawCircle(color, radius = toe, center = center + o) }
    }
}

@Composable
fun PawPrint(color: Color, modifier: Modifier = Modifier, rotation: Float = 0f) {
    Canvas(modifier = modifier) {
        drawPaw(Offset(size.width / 2f, size.height * 0.55f), minOf(size.width, size.height), color, rotation)
    }
}

/** Patrón suave de huellitas y yuzus para el fondo de las cabeceras. */
fun Modifier.capyPattern(color: Color): Modifier = drawBehind {
    val step = 46.dp.toPx()
    val paw = 13.dp.toPx()
    var row = 0
    var y = step * 0.4f
    while (y < size.height + step) {
        var x = if (row % 2 == 0) step * 0.3f else step * 0.85f
        var col = 0
        while (x < size.width + step) {
            if ((row + col) % 3 == 0) {
                drawCircle(color, radius = paw * 0.3f, center = Offset(x, y))
                drawCircle(color, radius = paw * 0.12f, center = Offset(x + paw * 0.18f, y - paw * 0.36f))
            } else {
                drawPaw(Offset(x, y), paw, color, rotation = if (col % 2 == 0) -18f else 16f)
            }
            x += step * 1.1f
            col++
        }
        y += step * 0.75f
        row++
    }
}

/**
 * Capibara para el bloqueo con huella: ilustración dormido y despertando.
 */
@Composable
fun SleepingCapy(isAwake: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sleepingCapy")
    val breath by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isAwake,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(350)) },
            label = "sleepingCapyState"
        ) { awake ->
            Image(
                painter = painterResource(id = if (awake) R.drawable.capy_awake else R.drawable.capy_sleeping),
                contentDescription = if (awake) tr("Capibara despierto", "Capybara awake") else tr("Capibara dormido", "Capybara asleep"),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .graphicsLayer {
                        val scale = if (awake) 1.03f else 1f + breath * 0.025f
                        scaleX = scale
                        scaleY = scale
                        translationY = if (awake) -4f else breath * 4f
                    }
            )
        }
    }
}

/**
 * Botón rápido ✓ con huellita: al marcarlo da un saltito y deja una huellita de capibara
 * con mini huellitas que salen volando.
 */
@Composable
fun PawCheckButton(
    checked: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
    checkedColor: Color,
    uncheckedColor: Color,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val burst = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(checked) }
    LaunchedEffect(checked) {
        if (checked != previous) {
            previous = checked
            if (checked) {
                launch { burst.snapTo(0f); burst.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
                scale.snapTo(0.6f)
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
            } else {
                scale.snapTo(0.85f)
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
    }
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .semantics { this.contentDescription = contentDescription; role = Role.Checkbox }
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value }) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            if (checked) {
                drawCircle(checkedColor, radius = radius, center = c)
                drawPaw(c + Offset(0f, radius * 0.08f), radius * 1.15f, Color.White)
            } else {
                drawCircle(uncheckedColor, radius = radius - 1.5.dp.toPx(), center = c, style = Stroke(width = 2.dp.toPx()))
                drawPaw(c + Offset(0f, radius * 0.08f), radius * 1.0f, uncheckedColor.copy(alpha = uncheckedColor.alpha * 0.55f))
            }
            // Mini huellitas que salen volando al marcar
            if (burst.value < 1f) {
                val t = burst.value
                listOf(-60f, 0f, 60f).forEach { angle ->
                    val rad = Math.toRadians((angle - 90).toDouble())
                    val dist = radius * (1.1f + t * 1.4f)
                    val p = c + Offset((kotlin.math.cos(rad) * dist).toFloat(), (kotlin.math.sin(rad) * dist).toFloat())
                    drawPaw(p, radius * 0.6f, checkedColor.copy(alpha = 1f - t), rotation = angle * 0.5f)
                }
            }
        }
    }
}
