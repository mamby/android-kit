package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Immutable
internal data class FloatingSurfaceVisuals(
    val opacity: Float,
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke,
    val shadow: Shadow,
    val buttonShadow: Shadow,
)

@Composable
internal fun floatingSurfaceVisuals(
    style: AndroidKitFloatingSurfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle,
): FloatingSurfaceVisuals {
    val dimensions = AndroidKitThemeTokens.dimensions
    val scheme = AndroidKitThemeTokens.colorScheme
    return remember(style, dimensions, scheme) {
        val baseContainerColor = style.containerColor.takeIf { it != Color.Unspecified }
            ?: scheme.surface
        FloatingSurfaceVisuals(
            opacity = style.opacity,
            containerColor = if (style.opacity == 1f) {
                baseContainerColor
            } else {
                baseContainerColor.copy(alpha = style.opacity)
            },
            contentColor = style.contentColor.takeIf { it != Color.Unspecified }
                ?: scheme.onSurface,
            border = BorderStroke(
                width = style.borderWidth.takeIf { it != Dp.Unspecified }
                    ?: dimensions.floatingSurfaceBorderWidth,
                color = style.borderColor.takeIf { it != Color.Unspecified }
                    ?: scheme.outlineVariant.copy(alpha = FloatingSurfaceBorderAlpha),
            ),
            shadow = Shadow(
                radius = style.shadowRadius.takeIf { it != Dp.Unspecified }
                    ?: dimensions.floatingSurfaceShadowRadius,
                color = style.shadowColor.takeIf { it != Color.Unspecified }
                    ?: scheme.scrim.copy(alpha = FloatingSurfaceShadowAlpha),
                offset = DpOffset(
                    x = 0.dp,
                    y = style.shadowOffsetY.takeIf { it != Dp.Unspecified }
                        ?: dimensions.floatingSurfaceShadowOffsetY,
                ),
            ),
            buttonShadow = Shadow(
                radius = style.buttonShadowRadius.takeIf { it != Dp.Unspecified }
                    ?: dimensions.floatingSurfaceButtonShadowRadius,
                color = style.shadowColor.takeIf { it != Color.Unspecified }
                    ?: scheme.scrim.copy(alpha = FloatingSurfaceShadowAlpha),
                offset = DpOffset(
                    x = 0.dp,
                    y = style.buttonShadowOffsetY.takeIf { it != Dp.Unspecified }
                        ?: dimensions.floatingSurfaceButtonShadowOffsetY,
                ),
            ),
        )
    }
}

@Composable
internal fun FloatingSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    style: AndroidKitFloatingSurfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle,
    content: @Composable () -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals(style)
    Surface(
        modifier = modifier.dropShadow(shape, visuals.shadow),
        shape = shape,
        color = containerColor ?: visuals.containerColor,
        contentColor = visuals.contentColor,
        border = visuals.border,
        content = content,
    )
}

@Composable
internal fun FloatingSurfaceButton(
    onClick: () -> Unit,
    shape: Shape,
    visualSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: AndroidKitFloatingSurfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle,
    content: @Composable () -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals(style)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(visualSize)
            .dropShadow(shape, visuals.buttonShadow),
        shape = shape,
        color = visuals.containerColor,
        contentColor = visuals.contentColor,
        border = visuals.border,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

private const val FloatingSurfaceBorderAlpha = 0.45f
private const val FloatingSurfaceShadowAlpha = 0.03f
