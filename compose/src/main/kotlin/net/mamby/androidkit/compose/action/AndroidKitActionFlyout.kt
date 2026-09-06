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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import net.mamby.androidkit.compose.theme.AndroidKitActionFlyoutStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import kotlin.math.max
import kotlin.math.min

/** Content of an action flyout, including standard actions and custom Compose content. */
public interface AndroidKitActionFlyoutScope : ColumnScope {
    /** Dismisses the flyout before invoking [onClick]. Icons are optional. */
    @Suppress("ComposableNaming") // Match the toolbar and action-bar flyout DSL.
    @Composable
    public fun item(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
    ): Unit

    @Suppress("ComposableNaming") // Match the toolbar and action-bar flyout DSL.
    @Composable
    public fun separator(
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
    ): Unit
}

private class ActionFlyoutScopeImpl(
    columnScope: ColumnScope,
    private val onDismissRequest: () -> Unit,
    private val enabled: Boolean,
) : AndroidKitActionFlyoutScope, ColumnScope by columnScope {
    @Composable
    override fun item(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: ImageVector?,
        enabled: Boolean,
    ) {
        val dimensions = AndroidKitThemeTokens.dimensions
        DropdownMenuItem(
            modifier = modifier,
            text = {
                Text(
                    text = label,
                    style = AndroidKitThemeTokens.typography.labelLarge,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            onClick = {
                onDismissRequest()
                onClick()
            },
            enabled = this.enabled && enabled,
            contentPadding = PaddingValues(
                start = dimensions.spaceMedium,
                end = dimensions.spaceLarge,
            ),
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.actionFlyoutIconSize),
                    )
                }
            },
        )
    }

    @Composable
    override fun separator(modifier: Modifier, color: Color) {
        val dimensions = AndroidKitThemeTokens.dimensions
        HorizontalDivider(
            modifier = modifier.padding(
                horizontal = dimensions.spaceMedium,
                vertical = dimensions.spaceExtraSmall,
            ),
            color = color.takeUnless { it == Color.Unspecified }
                ?: AndroidKitThemeTokens.colorScheme.outlineVariant,
        )
    }
}

/**
 * An anchored menu for actions or custom content. Place it alongside its trigger in a Box.
 * The caller owns [expanded] and closes the flyout in [onDismissRequest].
 * Standard [item][AndroidKitActionFlyoutScope.item] entries request dismissal before invoking their
 * action; custom content controls its own dismissal.
 * Content scrolls vertically. [placement] and [horizontalAlignment] prefer an anchor edge and
 * fall back when the menu would extend beyond the window, respecting layout direction.
 */
@Composable
public fun AndroidKitActionFlyout(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placement: AndroidKitActionFlyoutPlacement =
        AndroidKitActionFlyoutPlacement.Below,
    horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment =
        AndroidKitActionFlyoutHorizontalAlignment.Start,
    style: AndroidKitActionFlyoutStyle = AndroidKitThemeTokens.actionFlyoutStyle,
    contentPadding: PaddingValues = PaddingValues(
        vertical = AndroidKitThemeTokens.dimensions.spaceSmall,
    ),
    properties: PopupProperties = PopupProperties(focusable = true),
    scrollState: ScrollState = rememberScrollState(),
    offset: DpOffset = DpOffset.Zero,
    enabled: Boolean = true,
    content: @Composable AndroidKitActionFlyoutScope.() -> Unit,
): Unit = ActionFlyoutContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    placement = placement,
    horizontalAlignment = horizontalAlignment,
    offset = offset,
    style = style,
    contentPadding = contentPadding,
    properties = properties,
    scrollState = scrollState,
    enabled = enabled,
    content = content,
)

@Composable
internal fun AndroidKitActionFlyoutWithContainerColor(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    containerColor: Color,
    placement: AndroidKitActionFlyoutPlacement,
    horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment,
    style: AndroidKitActionFlyoutStyle,
    contentPadding: PaddingValues,
    content: @Composable AndroidKitActionFlyoutScope.() -> Unit,
): Unit = ActionFlyoutContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = Modifier,
    placement = placement,
    horizontalAlignment = horizontalAlignment,
    offset = DpOffset.Zero,
    style = style,
    contentPadding = contentPadding,
    properties = PopupProperties(focusable = true),
    scrollState = rememberScrollState(),
    containerColor = containerColor,
    content = content,
)

public enum class AndroidKitActionFlyoutPlacement {
    Above,
    Below,
}

public enum class AndroidKitActionFlyoutHorizontalAlignment {
    Start,
    End,
}

@Composable
private fun ActionFlyoutContent(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    placement: AndroidKitActionFlyoutPlacement,
    horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment,
    offset: DpOffset,
    style: AndroidKitActionFlyoutStyle,
    contentPadding: PaddingValues,
    properties: PopupProperties,
    scrollState: ScrollState,
    containerColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable AndroidKitActionFlyoutScope.() -> Unit,
): Unit {
    val density = LocalDensity.current
    val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
    val positionProvider = remember(placement, horizontalAlignment, offset, density) {
        ActionFlyoutPositionProvider(
            placement = placement,
            horizontalAlignment = horizontalAlignment,
            offset = offset,
            density = density,
            onPositionCalculated = { anchorBounds, menuBounds ->
                transformOriginState.value = calculateTransformOrigin(anchorBounds, menuBounds)
            },
        )
    }
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded && enabled

    LaunchedEffect(enabled, expanded) {
        if (!enabled && expanded) onDismissRequest()
    }

    if (expandedState.currentState || expandedState.targetState) {
        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = positionProvider,
            properties = properties,
        ) {
            ActionFlyoutAnimation(
                expandedState = expandedState,
                transformOriginState = transformOriginState,
            ) {
                FloatingSurface(
                    shape = style.shape,
                    modifier = modifier,
                    containerColor = containerColor,
                    style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .width(IntrinsicSize.Max)
                            .verticalScroll(scrollState),
                    ) {
                        ActionFlyoutScopeImpl(this, onDismissRequest, enabled).content()
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionFlyoutAnimation(
    expandedState: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    @Suppress("DEPRECATION")
    val transition = updateTransition(expandedState, label = "ActionFlyout")
    val scale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = FastSpatialDampingRatio,
                stiffness = FastSpatialStiffness,
            )
        },
        label = "ActionFlyoutScale",
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
        label = "ActionFlyoutAlpha",
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

private data class ActionFlyoutPositionProvider(
    val placement: AndroidKitActionFlyoutPlacement,
    val horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment,
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
        val horizontalCandidates = when (horizontalAlignment) {
            AndroidKitActionFlyoutHorizontalAlignment.Start -> intArrayOf(
                startAlignedX,
                endAlignedX,
                edgeAlignedX,
            )
            AndroidKitActionFlyoutHorizontalAlignment.End -> intArrayOf(
                endAlignedX,
                startAlignedX,
                edgeAlignedX,
            )
        }
        val x = firstFittingCandidate(
            candidates = horizontalCandidates,
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
        val aboveFits = aboveAnchor >= verticalMargin &&
            aboveAnchor + popupContentSize.height <= windowSize.height
        val belowFits = belowAnchor >= 0 &&
            belowAnchor + popupContentSize.height <= windowSize.height - verticalMargin
        val centeredFits = centeredOnAnchorTop >= verticalMargin &&
            centeredOnAnchorTop + popupContentSize.height <= windowSize.height - verticalMargin
        val y = when (placement) {
            AndroidKitActionFlyoutPlacement.Above -> when {
                aboveFits -> aboveAnchor
                belowFits -> belowAnchor
                centeredFits -> centeredOnAnchorTop
                else -> edgeAlignedY
            }
            AndroidKitActionFlyoutPlacement.Below -> when {
                belowFits -> belowAnchor
                aboveFits -> aboveAnchor
                centeredFits -> centeredOnAnchorTop
                else -> edgeAlignedY
            }
        }
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
