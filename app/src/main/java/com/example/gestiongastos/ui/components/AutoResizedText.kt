package com.example.gestiongastos.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    minFontSize: TextUnit = 9.sp,
    isPrivate: Boolean = false
) {
    var resizedTextStyle by remember(text, style) { mutableStateOf(style) }
    var shouldDraw by remember(text, style) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontWeight = fontWeight ?: style.fontWeight,
        modifier = modifier.privacyBlur(isPrivate).drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        softWrap = false,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = resizedTextStyle,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                if (resizedTextStyle.fontSize > minFontSize) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = (resizedTextStyle.fontSize.value * 0.9f).sp
                    )
                } else {
                    shouldDraw = true
                }
            } else {
                shouldDraw = true
            }
        }
    )
}
