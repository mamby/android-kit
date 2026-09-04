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
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorder: BorderStroke,
    val shadow: Shadow,
    val buttonShadow: Shadow,
)

@Composable
internal fun floatingSurfaceVisuals(
    style: AndroidKitFloatingSurfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle,
): FloatingSurfaceVisuals {
    val dimensions = AndroidKitThemeTokens.dimensions
    val scheme = AndroidKitThemeTokens.colorScheme
    val opacity = floatingSurfaceAlphaForLevel(
        AndroidKitThemeTokens.floatingSurfaceOpacityLevel,
    )
    return remember(style, dimensions, scheme, opacity) {
        val baseContainerColor = style.containerColor.takeIf { it != Color.Unspecified }
            ?: scheme.surface
        FloatingSurfaceVisuals(
            opacity = opacity,
            containerColor = baseContainerColor.copy(alpha = opacity),
            contentColor = style.contentColor.takeIf { it != Color.Unspecified }
                ?: scheme.onSurface,
            border = BorderStroke(
                width = style.borderWidth.takeIf { it != Dp.Unspecified }
                    ?: dimensions.floatingSurfaceBorderWidth,
                color = style.borderColor.takeIf { it != Color.Unspecified }
                    ?: scheme.outlineVariant.copy(alpha = FloatingSurfaceBorderAlpha),
            ),
            disabledContainerColor = style.disabledContainerColor.takeIf {
                it != Color.Unspecified
            } ?: scheme.onSurface.copy(alpha = DisabledContainerAlpha),
            disabledContentColor = style.disabledContentColor.takeIf {
                it != Color.Unspecified
            } ?: scheme.onSurface.copy(alpha = DisabledContentAlpha),
            disabledBorder = BorderStroke(
                width = style.borderWidth.takeIf { it != Dp.Unspecified }
                    ?: dimensions.floatingSurfaceBorderWidth,
                color = style.disabledBorderColor.takeIf { it != Color.Unspecified }
                    ?: scheme.onSurface.copy(alpha = DisabledBorderAlpha),
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
    val shadowModifier = if (enabled) {
        Modifier.dropShadow(shape, visuals.buttonShadow)
    } else {
        Modifier
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(visualSize)
            .then(shadowModifier),
        shape = shape,
        color = if (enabled) visuals.containerColor else visuals.disabledContainerColor,
        contentColor = if (enabled) visuals.contentColor else visuals.disabledContentColor,
        border = if (enabled) visuals.border else visuals.disabledBorder,
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
private const val DisabledContainerAlpha = 0.12f
private const val DisabledContentAlpha = 0.38f
private const val DisabledBorderAlpha = 0.12f
