package com.example.deviceasset.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deviceasset.R

/** A quiet, reusable paper-and-ink surface for the app's visual language. */
@Composable
fun InkWashCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)),
        shadowElevation = 3.dp,
        tonalElevation = 0.dp,
    ) {
        Box(modifier = Modifier.clip(shape)) {
            // Keep the decorative canvas out of measurement. A fillMaxSize child
            // participates in the parent's size calculation and can make a card
            // consume the entire screen when it is inside a weighted Column.
            InkWashTexture(modifier = Modifier.matchParentSize())
            content()
        }
    }
}

@Composable
private fun InkWashTexture(modifier: Modifier = Modifier) {
    val vermilion = MaterialTheme.colorScheme.tertiary
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.ink_wash_card_texture),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            alpha = 0.74f,
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = vermilion.copy(alpha = 0.12f),
                radius = 5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.18f),
            )
        }
    }
}

@Composable
fun InkBrushTitle(
    text: String,
    style: TextStyle,
    brushColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val washHeight = size.height * 0.42f
            drawRoundRect(
                color = brushColor.copy(alpha = 0.2f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.44f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.92f, washHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(washHeight / 2f),
            )
            drawRoundRect(
                color = brushColor.copy(alpha = 0.11f),
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.14f, size.height * 0.58f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.76f, washHeight * 0.64f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(washHeight / 2f),
            )
        }
        androidx.compose.material3.Text(
            text = text,
            style = style,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
