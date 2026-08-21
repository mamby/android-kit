package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
    val containerColor: Color,
    val contentColor: Color,
    val border: BorderStroke,
    val shadow: Shadow,
)

@Composable
internal fun floatingSurfaceVisuals(): FloatingSurfaceVisuals {
    val style = AndroidKitThemeTokens.floatingSurfaceStyle
    val dimensions = AndroidKitThemeTokens.dimensions
    val scheme = MaterialTheme.colorScheme
    return FloatingSurfaceVisuals(
        containerColor = Color.White.copy(alpha = style.opacity),
        contentColor = Color.Black,
        border = BorderStroke(
            width = dimensions.floatingSurfaceBorderWidth,
            color = scheme.outlineVariant.copy(alpha = FloatingSurfaceBorderAlpha),
        ),
        shadow = Shadow(
            radius = dimensions.floatingSurfaceShadowRadius,
            color = scheme.scrim.copy(alpha = FloatingSurfaceShadowAlpha),
            offset = DpOffset(x = 0.dp, y = dimensions.floatingSurfaceShadowOffsetY),
        ),
    )
}

@Composable
internal fun FloatingSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals()
    Surface(
        modifier = modifier.dropShadow(shape, visuals.shadow),
        shape = shape,
        color = visuals.containerColor,
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
    content: @Composable () -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals()
    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(visualSize)
            .dropShadow(shape, visuals.shadow),
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
private const val FloatingSurfaceShadowAlpha = 0.08f
