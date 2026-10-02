package net.mamby.androidkit.compose.action

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitActionFlyoutStyle
import net.mamby.androidkit.compose.theme.AndroidKitActionFlyoutColors
import net.mamby.androidkit.compose.theme.AndroidKitComponentTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.withColors
import net.mamby.androidkit.compose.theme.FloatingSurface
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Declares menu entries. AndroidKit owns their layout, styling, and dismissal. */
public class AndroidKitActionFlyoutScope internal constructor() {
    private val entries = mutableListOf<Entry>()

    /** Adds an action with an optional vector icon. */
    public fun item(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        destructive: Boolean = false,
    ) {
        entries += Entry.Action(label, onClick, modifier, icon, null, enabled, destructive)
    }

    /** Adds an action with painter data; Kit owns the icon's size and tint. */
    public fun item(
        label: String,
        onClick: () -> Unit,
        icon: Painter,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        destructive: Boolean = false,
    ) {
        entries += Entry.Action(label, onClick, modifier, null, icon, enabled, destructive)
    }

    public fun submenu(
        label: String,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        content: AndroidKitActionFlyoutScope.() -> Unit,
    ) {
        entries += Entry.Submenu(label, modifier, icon, enabled, AndroidKitActionFlyoutScope().apply(content))
    }

    public fun separator(
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
    ) {
        entries += Entry.Separator(modifier, color)
    }

    @Composable
    internal fun render(scope: ActionFlyoutRenderScope) {
        entries.forEach { entry ->
            when (entry) {
                is Entry.Action -> scope.item(
                    label = entry.label,
                    onClick = entry.onClick,
                    modifier = entry.modifier,
                    icon = entry.icon,
                    enabled = entry.enabled,
                    iconPainter = entry.painter,
                    destructive = entry.destructive,
                )
                is Entry.Submenu -> scope.submenu(
                    label = entry.label,
                    modifier = entry.modifier,
                    icon = entry.icon,
                    enabled = entry.enabled,
                ) { entry.children.render(this) }
                is Entry.Separator -> scope.separator(entry.modifier, entry.color)
            }
        }
    }

    private sealed interface Entry {
        data class Action(
            val label: String,
            val onClick: () -> Unit,
            val modifier: Modifier,
            val icon: ImageVector?,
            val painter: Painter?,
            val enabled: Boolean,
            val destructive: Boolean,
        ) : Entry

        data class Submenu(
            val label: String,
            val modifier: Modifier,
            val icon: ImageVector?,
            val enabled: Boolean,
            val children: AndroidKitActionFlyoutScope,
        ) : Entry

        data class Separator(val modifier: Modifier, val color: Color) : Entry
    }
}

/** Internal rendering scope; consumers declare entries through [AndroidKitActionFlyoutScope]. */
internal interface ActionFlyoutRenderScope : ColumnScope {
    /** Dismisses the flyout before invoking [onClick]. Icons are optional. */
    @Suppress("ComposableNaming") // Match the toolbar and action-bar flyout DSL.
    @Composable
    public fun item(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        iconPainter: Painter? = null,
        destructive: Boolean = false,
    ): Unit

    /** Opens a nested menu. Back/outside dismisses that level; actions dismiss the entire flyout. */
    @Suppress("ComposableNaming")
    @Composable
    public fun submenu(
        label: String,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true,
        content: @Composable ActionFlyoutRenderScope.() -> Unit,
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
    private val expanded: Boolean,
    private val style: AndroidKitActionFlyoutStyle,
    private val properties: PopupProperties,
    private val containerColor: Color?,
) : ActionFlyoutRenderScope, ColumnScope by columnScope {
    @Composable
    override fun item(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier,
        icon: ImageVector?,
        enabled: Boolean,
        iconPainter: Painter?,
        destructive: Boolean,
    ) {
        val dimensions = AndroidKitThemeTokens.dimensions
        val contentColor = if (destructive) AndroidKitThemeTokens.colorScheme.error else Color.Unspecified
        DropdownMenuItem(
            modifier = modifier.clip(CircleShape),
            text = {
                Text(
                    text = label,
                    style = AndroidKitThemeTokens.componentTokens.actionFlyout.itemTextStyle,
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
            colors = MenuDefaults.itemColors(
                textColor = contentColor,
                leadingIconColor = contentColor,
            ),
            contentPadding = PaddingValues(
                start = AndroidKitThemeTokens.componentTokens.actionFlyout.itemStartPadding,
                end = AndroidKitThemeTokens.componentTokens.actionFlyout.itemEndPadding,
            ),
            leadingIcon = if (iconPainter != null) {
                {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.actionFlyoutIconSize),
                    )
                }
            } else icon?.let {
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
    override fun submenu(
        label: String,
        modifier: Modifier,
        icon: ImageVector?,
        enabled: Boolean,
        content: @Composable ActionFlyoutRenderScope.() -> Unit,
    ) {
        ActionFlyoutSubmenu(
            label, modifier, icon, this.enabled && enabled, expanded,
            style, properties, containerColor, onDismissRequest, content,
        )
    }

    @Composable
    override fun separator(modifier: Modifier, color: Color) {
        val tokens = AndroidKitThemeTokens.componentTokens.actionFlyout
        HorizontalDivider(
            modifier = modifier.padding(
                horizontal = tokens.separatorHorizontalPadding,
                vertical = tokens.separatorVerticalPadding,
            ),
            color = color.takeUnless { it == Color.Unspecified }
                ?: AndroidKitThemeTokens.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ActionFlyoutSubmenu(
    label: String,
    modifier: Modifier,
    icon: ImageVector?,
    enabled: Boolean,
    parentExpanded: Boolean,
    style: AndroidKitActionFlyoutStyle,
    properties: PopupProperties,
    containerColor: Color?,
    onActionDismissRequest: () -> Unit,
    content: @Composable ActionFlyoutRenderScope.() -> Unit,
) {
    val tokens = AndroidKitThemeTokens.componentTokens.actionFlyout
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled, parentExpanded) {
        if (!enabled || !parentExpanded) expanded = false
    }
    val dimensions = AndroidKitThemeTokens.dimensions
    Box {
        DropdownMenuItem(
            text = {
                Text(
                    label,
                    style = tokens.itemTextStyle,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            onClick = { expanded = true },
            modifier = modifier.clip(CircleShape),
            enabled = enabled,
            contentPadding = PaddingValues(
                start = tokens.itemStartPadding, end = tokens.itemEndPadding,
            ),
            leadingIcon = icon?.let {
                {
                    Icon(
                        it, contentDescription = null,
                        modifier = Modifier.size(dimensions.actionFlyoutIconSize),
                    )
                }
            },
            trailingIcon = {
                Icon(
                    AndroidKitIcons.ChevronRight, contentDescription = null,
                    modifier = Modifier.size(dimensions.actionFlyoutIconSize),
                )
            },
        )
        ActionFlyoutPopup(
            expanded = expanded && enabled && parentExpanded,
            onDismissRequest = { expanded = false },
            onActionDismissRequest = {
                expanded = false
                onActionDismissRequest()
            },
            positionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(
                MenuAnchorPosition.End,
            ),
            style = style,
            properties = properties,
            containerColor = containerColor,
            enabled = enabled,
            content = content,
        )
    }
}

/**
 * An anchored menu of Kit-rendered actions, separators, and submenus. Place it alongside its trigger in a Box.
 * The caller owns [expanded] and closes the flyout in [onDismissRequest].
 * Standard [item][AndroidKitActionFlyoutScope.item] entries request dismissal before invoking their
 * action. Resolve composable resources before entering the non-composable content builder.
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
    colors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
    properties: PopupProperties = PopupProperties(focusable = true),
    scrollState: ScrollState = rememberScrollState(),
    offset: DpOffset = DpOffset.Zero,
    enabled: Boolean = true,
    content: AndroidKitActionFlyoutScope.() -> Unit,
): Unit {
    AndroidKitComponentTheme {
        AndroidKitActionFlyout(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            placement = placement,
            horizontalAlignment = horizontalAlignment,
            style = AndroidKitThemeTokens.actionFlyoutStyle.withColors(colors, AndroidKitThemeTokens.floatingSurfaceStyle),
            properties = properties,
            scrollState = scrollState,
            offset = offset,
            enabled = enabled,
            content = content,
        )
    }
}

@Composable
internal fun AndroidKitActionFlyout(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placement: AndroidKitActionFlyoutPlacement =
        AndroidKitActionFlyoutPlacement.Below,
    horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment =
        AndroidKitActionFlyoutHorizontalAlignment.Start,
    style: AndroidKitActionFlyoutStyle,
    properties: PopupProperties = PopupProperties(focusable = true),
    scrollState: ScrollState = rememberScrollState(),
    offset: DpOffset = DpOffset.Zero,
    enabled: Boolean = true,
    content: AndroidKitActionFlyoutScope.() -> Unit,
): Unit = ActionFlyoutContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    placement = placement,
    horizontalAlignment = horizontalAlignment,
    offset = offset,
    style = style,
    properties = properties,
    scrollState = scrollState,
    enabled = enabled,
    content = { AndroidKitActionFlyoutScope().apply(content).render(this) },
)

@Composable
internal fun AndroidKitActionFlyoutWithContainerColor(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    containerColor: Color,
    placement: AndroidKitActionFlyoutPlacement,
    horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment,
    style: AndroidKitActionFlyoutStyle,
    content: @Composable ActionFlyoutRenderScope.() -> Unit,
): Unit = ActionFlyoutContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = Modifier,
    placement = placement,
    horizontalAlignment = horizontalAlignment,
    offset = DpOffset.Zero,
    style = style,
    properties = PopupProperties(focusable = true),
    scrollState = rememberScrollState(),
    containerColor = containerColor,
    // Navigation paints its selection background around the padded rows.
    contentPadding = PaddingValues.Zero,
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
    properties: PopupProperties,
    scrollState: ScrollState,
    containerColor: Color? = null,
    contentPadding: PaddingValues = AndroidKitThemeTokens.componentTokens.actionFlyout.containerPadding,
    enabled: Boolean = true,
    content: @Composable ActionFlyoutRenderScope.() -> Unit,
): Unit {
    val density = LocalDensity.current
    val positionProvider = remember(placement, horizontalAlignment, offset, density) {
        ActionFlyoutPositionProvider(placement, horizontalAlignment, offset, density)
    }

    LaunchedEffect(enabled, expanded) {
        if (!enabled && expanded) onDismissRequest()
    }

    ActionFlyoutPopup(
        expanded = expanded && enabled,
        onDismissRequest = onDismissRequest,
        onActionDismissRequest = onDismissRequest,
        positionProvider = positionProvider,
        modifier = modifier,
        style = style,
        contentPadding = contentPadding,
        properties = properties,
        scrollState = scrollState,
        containerColor = containerColor,
        enabled = enabled,
        content = content,
    )
}

@Composable
internal fun ActionFlyoutPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onActionDismissRequest: () -> Unit,
    positionProvider: DropdownMenuPopupPositionProvider,
    modifier: Modifier = Modifier,
    style: AndroidKitActionFlyoutStyle,
    properties: PopupProperties,
    scrollState: ScrollState = rememberScrollState(),
    containerColor: Color? = null,
    contentPadding: PaddingValues = AndroidKitThemeTokens.componentTokens.actionFlyout.containerPadding,
    enabled: Boolean = true,
    content: @Composable ActionFlyoutRenderScope.() -> Unit,
) {
    AndroidKitComponentTheme {
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            popupPositionProvider = positionProvider,
            properties = properties,
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
                    ActionFlyoutScopeImpl(
                        this, onActionDismissRequest, enabled, expanded,
                        style, properties, containerColor,
                    ).content()
                }
            }
        }
    }
}

/** Uses the same edge fallback and animation origin as anchored flyouts. */
internal class ContextMenuPositionProvider(
    private val position: Offset?,
    density: Density,
) : DropdownMenuPopupPositionProvider {
    private val delegate = ActionFlyoutPositionProvider(
        AndroidKitActionFlyoutPlacement.Below,
        AndroidKitActionFlyoutHorizontalAlignment.Start,
        DpOffset.Zero,
        density,
    )

    override val transformOrigin: TransformOrigin
        get() = delegate.transformOrigin

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val point = position?.let {
            IntOffset(anchorBounds.left + it.x.roundToInt(), anchorBounds.top + it.y.roundToInt())
        }
        return delegate.calculatePosition(
            point?.let { IntRect(it, IntSize.Zero) } ?: anchorBounds,
            windowSize,
            layoutDirection,
            popupContentSize,
        )
    }
}

private data class ActionFlyoutPositionProvider(
    val placement: AndroidKitActionFlyoutPlacement,
    val horizontalAlignment: AndroidKitActionFlyoutHorizontalAlignment,
    val offset: DpOffset,
    val density: Density,
) : DropdownMenuPopupPositionProvider {
    override var transformOrigin: TransformOrigin by mutableStateOf(TransformOrigin.Center)
        private set

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
        transformOrigin = calculateTransformOrigin(
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
