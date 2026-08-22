package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.min

internal fun Modifier.statusBarEdgeProtection(
    statusBarInsets: WindowInsets,
    fadeLength: Dp,
    protectionColor: Color,
): Modifier = drawWithCache {
    val topProtectedHeight = statusBarInsets.getTop(this).toFloat()
    val fadeHeight = fadeLength.toPx()
    val topHeight = protectedEdgeHeight(topProtectedHeight, fadeHeight, size.height)
    val topProtectedStop = protectedStop(topProtectedHeight, topHeight)
    val topMidStop = topProtectedStop + ((1f - topProtectedStop) * EdgeFadeMidpoint)
    val topBrush = if (topHeight > 0f) {
        Brush.verticalGradient(
            0f to protectionColor.copy(alpha = EdgeStrongAlpha),
            topProtectedStop to protectionColor.copy(alpha = EdgeStrongAlpha),
            topMidStop to protectionColor.copy(alpha = EdgeMidAlpha),
            1f to Color.Transparent,
            startY = 0f,
            endY = topHeight,
        )
    } else {
        null
    }

    onDrawWithContent {
        drawContent()
        topBrush?.let {
            drawRect(
                brush = it,
                size = Size(size.width, topHeight),
            )
        }
    }
}

private fun protectedEdgeHeight(
    protectedHeight: Float,
    fadeHeight: Float,
    availableHeight: Float,
): Float = if (protectedHeight > 0f) {
    min(protectedHeight + fadeHeight, availableHeight / 2f)
} else {
    0f
}

private fun protectedStop(protectedHeight: Float, totalHeight: Float): Float =
    if (totalHeight > 0f) {
        (protectedHeight / totalHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

private const val EdgeStrongAlpha = 0.80f
private const val EdgeMidAlpha = 0.44f
private const val EdgeFadeMidpoint = 0.50f
