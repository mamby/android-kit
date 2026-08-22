package net.mamby.androidkit.compose.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import kotlin.math.min

internal fun Modifier.progressiveEdgeProtection(
    topProtectedExtent: Dp,
    bottomProtectedExtent: Dp,
    fadeLength: Dp,
    blurRadius: Dp,
    protectionColor: Color,
): Modifier = drawWithCache {
    val blurLayer = obtainGraphicsLayer()
    val blurEffect = BlurEffect(
        radiusX = blurRadius.toPx(),
        radiusY = blurRadius.toPx(),
        edgeTreatment = TileMode.Clamp,
    )
    val topProtectedHeight = topProtectedExtent.toPx()
    val bottomProtectedHeight = bottomProtectedExtent.toPx()
    val fadeHeight = fadeLength.toPx()
    val topHeight = min(topProtectedHeight + fadeHeight, size.height / 2f)
    val bottomHeight = min(bottomProtectedHeight + fadeHeight, size.height / 2f)
    val topProtectedStop = protectedStop(topProtectedHeight, topHeight)
    val bottomFadeStop = 1f - protectedStop(bottomProtectedHeight, bottomHeight)
    // A single blur pass stays efficient while the nonlinear mask makes the perceived blur
    // begin softly and reach full strength only as content enters the protected bar area.
    val topMaskMidStop = topProtectedStop +
        ((1f - topProtectedStop) * BlurMaskMidpoint)
    val topMaskLightStop = topProtectedStop +
        ((1f - topProtectedStop) * BlurMaskLightPoint)
    val bottomMaskLightStop = bottomFadeStop * (1f - BlurMaskLightPoint)
    val bottomMaskMidStop = bottomFadeStop * (1f - BlurMaskMidpoint)
    val topMask = Brush.verticalGradient(
        0f to Color.White,
        topProtectedStop to Color.White,
        topMaskMidStop to Color.White.copy(alpha = BlurMaskMidAlpha),
        topMaskLightStop to Color.White.copy(alpha = BlurMaskLightAlpha),
        1f to Color.Transparent,
        startY = 0f,
        endY = topHeight,
    )
    val bottomMask = Brush.verticalGradient(
        0f to Color.Transparent,
        bottomMaskLightStop to Color.White.copy(alpha = BlurMaskLightAlpha),
        bottomMaskMidStop to Color.White.copy(alpha = BlurMaskMidAlpha),
        bottomFadeStop to Color.White,
        1f to Color.White,
        startY = size.height - bottomHeight,
        endY = size.height,
    )
    val topMidpoint = topProtectedStop +
        ((1f - topProtectedStop) * EdgeScrimMidpoint)
    val bottomMidpoint = bottomFadeStop * (1f - EdgeScrimMidpoint)
    val topScrim = Brush.verticalGradient(
        0f to protectionColor.copy(alpha = EdgeScrimStrongAlpha),
        topProtectedStop to protectionColor.copy(alpha = EdgeScrimStrongAlpha),
        topMidpoint to protectionColor.copy(alpha = EdgeScrimMidAlpha),
        1f to Color.Transparent,
        startY = 0f,
        endY = topHeight,
    )
    val bottomScrim = Brush.verticalGradient(
        0f to Color.Transparent,
        bottomMidpoint to protectionColor.copy(alpha = EdgeScrimMidAlpha),
        bottomFadeStop to protectionColor.copy(alpha = EdgeScrimStrongAlpha),
        1f to protectionColor.copy(alpha = EdgeScrimStrongAlpha),
        startY = size.height - bottomHeight,
        endY = size.height,
    )
    val layerPaint = Paint()

    onDrawWithContent {
        drawContent()

        if (blurEffect.isSupported() && (topHeight > 0f || bottomHeight > 0f)) {
            blurLayer.record {
                this@onDrawWithContent.drawContent()
            }
            blurLayer.renderEffect = blurEffect

            if (topHeight > 0f) {
                drawMaskedLayer(
                    layer = blurLayer,
                    bounds = Rect(0f, 0f, size.width, topHeight),
                    mask = topMask,
                    layerPaint = layerPaint,
                )
            }
            if (bottomHeight > 0f) {
                drawMaskedLayer(
                    layer = blurLayer,
                    bounds = Rect(
                        left = 0f,
                        top = size.height - bottomHeight,
                        right = size.width,
                        bottom = size.height,
                    ),
                    mask = bottomMask,
                    layerPaint = layerPaint,
                )
            }
        }

        if (topHeight > 0f) {
            drawRect(
                brush = topScrim,
                size = Size(size.width, topHeight),
            )
        }
        if (bottomHeight > 0f) {
            drawRect(
                brush = bottomScrim,
                topLeft = Offset(0f, size.height - bottomHeight),
                size = Size(size.width, bottomHeight),
            )
        }
    }
}

private fun protectedStop(protectedHeight: Float, totalHeight: Float): Float =
    if (totalHeight > 0f) {
        (protectedHeight / totalHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

private fun ContentDrawScope.drawMaskedLayer(
    layer: GraphicsLayer,
    bounds: Rect,
    mask: Brush,
    layerPaint: Paint,
) {
    drawContext.canvas.saveLayer(bounds, layerPaint)
    clipRect(
        left = bounds.left,
        top = bounds.top,
        right = bounds.right,
        bottom = bounds.bottom,
    ) {
        drawLayer(layer)
    }
    drawRect(
        brush = mask,
        topLeft = bounds.topLeft,
        size = bounds.size,
        blendMode = BlendMode.DstIn,
    )
    drawContext.canvas.restore()
}

private const val BlurMaskMidpoint = 0.38f
private const val BlurMaskLightPoint = 0.74f
private const val BlurMaskMidAlpha = 0.58f
private const val BlurMaskLightAlpha = 0.14f
private const val EdgeScrimStrongAlpha = 0.24f
private const val EdgeScrimMidAlpha = 0.06f
private const val EdgeScrimMidpoint = 0.40f
