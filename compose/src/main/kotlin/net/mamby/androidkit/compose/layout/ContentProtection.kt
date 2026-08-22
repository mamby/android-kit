package net.mamby.androidkit.compose.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.min

internal fun Modifier.edgeProtection(
    topProtectedExtent: Dp,
    bottomProtectedExtent: Dp,
    fadeLength: Dp,
    protectionColor: Color,
): Modifier = drawWithCache {
    val topProtectedHeight = topProtectedExtent.toPx()
    val bottomProtectedHeight = bottomProtectedExtent.toPx()
    val fadeHeight = fadeLength.toPx()
    val topHeight = protectedEdgeHeight(topProtectedHeight, fadeHeight, size.height)
    val bottomHeight = protectedEdgeHeight(bottomProtectedHeight, fadeHeight, size.height)
    val topProtectedStop = protectedStop(topProtectedHeight, topHeight)
    val bottomFadeStop = 1f - protectedStop(bottomProtectedHeight, bottomHeight)
    val topMidStop = topProtectedStop + ((1f - topProtectedStop) * EdgeFadeMidpoint)
    val bottomMidStop = bottomFadeStop * (1f - EdgeFadeMidpoint)
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
    val bottomBrush = if (bottomHeight > 0f) {
        Brush.verticalGradient(
            0f to Color.Transparent,
            bottomMidStop to protectionColor.copy(alpha = EdgeMidAlpha),
            bottomFadeStop to protectionColor.copy(alpha = EdgeStrongAlpha),
            1f to protectionColor.copy(alpha = EdgeStrongAlpha),
            startY = size.height - bottomHeight,
            endY = size.height,
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
        bottomBrush?.let {
            drawRect(
                brush = it,
                topLeft = Offset(0f, size.height - bottomHeight),
                size = Size(size.width, bottomHeight),
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
