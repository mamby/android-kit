package net.mamby.androidkit.compose.action

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import net.mamby.androidkit.compose.theme.AndroidKitFloatingDropdownMenuStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import kotlin.math.max
import kotlin.math.min

@Composable
public fun AndroidKitFloatingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placement: AndroidKitFloatingDropdownMenuPlacement =
        AndroidKitFloatingDropdownMenuPlacement.Below,
    style: AndroidKitFloatingDropdownMenuStyle = AndroidKitThemeTokens.floatingDropdownMenuStyle,
    contentPadding: PaddingValues = PaddingValues(
        vertical = AndroidKitThemeTokens.dimensions.spaceSmall,
    ),
    properties: PopupProperties = PopupProperties(focusable = true),
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
): Unit = FloatingDropdownMenuContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    placement = placement,
    offset = DpOffset.Zero,
    style = style,
    contentPadding = contentPadding,
    properties = properties,
    scrollState = scrollState,
    content = content,
)

@Composable
public fun AndroidKitFloatingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset,
    modifier: Modifier = Modifier,
    placement: AndroidKitFloatingDropdownMenuPlacement =
        AndroidKitFloatingDropdownMenuPlacement.Below,
    style: AndroidKitFloatingDropdownMenuStyle = AndroidKitThemeTokens.floatingDropdownMenuStyle,
    contentPadding: PaddingValues = PaddingValues(
        vertical = AndroidKitThemeTokens.dimensions.spaceSmall,
    ),
    properties: PopupProperties = PopupProperties(focusable = true),
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
): Unit = FloatingDropdownMenuContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    placement = placement,
    offset = offset,
    style = style,
    contentPadding = contentPadding,
    properties = properties,
    scrollState = scrollState,
    content = content,
)

public enum class AndroidKitFloatingDropdownMenuPlacement {
    Above,
    Below,
}

@Composable
private fun FloatingDropdownMenuContent(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    placement: AndroidKitFloatingDropdownMenuPlacement,
    offset: DpOffset,
    style: AndroidKitFloatingDropdownMenuStyle,
    contentPadding: PaddingValues,
    properties: PopupProperties,
    scrollState: ScrollState,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val density = LocalDensity.current
    val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
    val positionProvider = remember(placement, offset, density) {
        FloatingDropdownMenuPositionProvider(
            placement = placement,
            offset = offset,
            density = density,
            onPositionCalculated = { anchorBounds, menuBounds ->
                transformOriginState.value = calculateTransformOrigin(anchorBounds, menuBounds)
            },
        )
    }
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    if (expandedState.currentState || expandedState.targetState) {
        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = positionProvider,
            properties = properties,
        ) {
            FloatingDropdownMenuAnimation(
                expandedState = expandedState,
                transformOriginState = transformOriginState,
            ) {
                FloatingSurface(
                    shape = style.shape,
                    modifier = modifier,
                    style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(scrollState),
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingDropdownMenuAnimation(
    expandedState: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    @Suppress("DEPRECATION")
    val transition = updateTransition(expandedState, label = "FloatingDropdownMenu")
    val scale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = FastSpatialDampingRatio,
                stiffness = FastSpatialStiffness,
            )
        },
        label = "FloatingDropdownMenuScale",
    ) { isExpanded ->
        if (isExpanded) ExpandedScale else CollapsedScale
    }
    val alpha by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = FastEffectsStiffness,
            )
        },
        label = "FloatingDropdownMenuAlpha",
    ) { isExpanded ->
        if (isExpanded) ExpandedAlpha else CollapsedAlpha
    }
    val isInspecting = LocalInspectionMode.current

    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .graphicsLayer {
                scaleX = if (isInspecting) {
                    if (expandedState.targetState) ExpandedScale else CollapsedScale
                } else {
                    scale
                }
                scaleY = scaleX
                this.alpha = if (isInspecting) {
                    if (expandedState.targetState) ExpandedAlpha else CollapsedAlpha
                } else {
                    alpha
                }
                transformOrigin = transformOriginState.value
            },
        content = content,
    )
}

private data class FloatingDropdownMenuPositionProvider(
    val placement: AndroidKitFloatingDropdownMenuPlacement,
    val offset: DpOffset,
    val density: Density,
    val onPositionCalculated: (anchorBounds: IntRect, menuBounds: IntRect) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val horizontalMargin = with(density) { MenuHorizontalMargin.roundToPx() }
        val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }
        val offsetX = with(density) { offset.x.roundToPx() }
        val offsetY = with(density) { offset.y.roundToPx() }
        val startAlignedX = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.left + offsetX
            LayoutDirection.Rtl -> anchorBounds.right - popupContentSize.width - offsetX
        }
        val endAlignedX = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.right - popupContentSize.width + offsetX
            LayoutDirection.Rtl -> anchorBounds.left - offsetX
        }
        val edgeAlignedX = if (popupContentSize.width >= windowSize.width - 2 * horizontalMargin) {
            (windowSize.width - popupContentSize.width) / 2
        } else if (anchorBounds.center.x < windowSize.width / 2) {
            horizontalMargin
        } else {
            windowSize.width - horizontalMargin - popupContentSize.width
        }
        val x = firstFittingCandidate(
            candidates = intArrayOf(startAlignedX, endAlignedX, edgeAlignedX),
            size = popupContentSize.width,
            availableSize = windowSize.width,
            margin = horizontalMargin,
        )

        val aboveAnchor = anchorBounds.top - popupContentSize.height + offsetY
        val belowAnchor = anchorBounds.bottom + offsetY
        val centeredOnAnchorTop = anchorBounds.top - popupContentSize.height / 2 + offsetY
        val edgeAlignedY = if (popupContentSize.height >= windowSize.height - 2 * verticalMargin) {
            (windowSize.height - popupContentSize.height) / 2
        } else if (anchorBounds.center.y < windowSize.height / 2) {
            verticalMargin
        } else {
            windowSize.height - verticalMargin - popupContentSize.height
        }
        val verticalCandidates = when (placement) {
            AndroidKitFloatingDropdownMenuPlacement.Above -> intArrayOf(
                aboveAnchor,
                belowAnchor,
                centeredOnAnchorTop,
                edgeAlignedY,
            )
            AndroidKitFloatingDropdownMenuPlacement.Below -> intArrayOf(
                belowAnchor,
                aboveAnchor,
                centeredOnAnchorTop,
                edgeAlignedY,
            )
        }
        val y = firstFittingCandidate(
            candidates = verticalCandidates,
            size = popupContentSize.height,
            availableSize = windowSize.height,
            margin = verticalMargin,
        )
        val menuOffset = IntOffset(x, y)
        onPositionCalculated(
            anchorBounds,
            IntRect(offset = menuOffset, size = popupContentSize),
        )
        return menuOffset
    }
}

private fun firstFittingCandidate(
    candidates: IntArray,
    size: Int,
    availableSize: Int,
    margin: Int,
): Int = candidates.firstOrNull { candidate ->
    candidate >= margin && candidate + size <= availableSize - margin
} ?: candidates.last()

private fun calculateTransformOrigin(
    anchorBounds: IntRect,
    menuBounds: IntRect,
): TransformOrigin {
    val pivotX = when {
        menuBounds.left >= anchorBounds.right -> 0f
        menuBounds.right <= anchorBounds.left -> 1f
        menuBounds.width == 0 -> 0f
        else -> {
            val intersectionCenter = (
                max(anchorBounds.left, menuBounds.left) +
                    min(anchorBounds.right, menuBounds.right)
                ) / 2
            (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
        }
    }
    val pivotY = when {
        menuBounds.top >= anchorBounds.bottom -> 0f
        menuBounds.bottom <= anchorBounds.top -> 1f
        menuBounds.height == 0 -> 0f
        else -> {
            val intersectionCenter = (
                max(anchorBounds.top, menuBounds.top) +
                    min(anchorBounds.bottom, menuBounds.bottom)
                ) / 2
            (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

private val MenuHorizontalMargin = 8.dp
private val MenuVerticalMargin = 48.dp
private const val ExpandedScale = 1f
private const val CollapsedScale = 0.8f
private const val ExpandedAlpha = 1f
private const val CollapsedAlpha = 0f
private const val FastSpatialDampingRatio = 0.9f
private const val FastSpatialStiffness = 1_400f
private const val FastEffectsStiffness = 3_800f
