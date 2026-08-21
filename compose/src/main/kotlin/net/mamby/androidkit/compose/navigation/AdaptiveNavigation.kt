package net.mamby.androidkit.compose.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import net.mamby.androidkit.compose.layout.LocalAndroidKitNavigationInsets
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

public class AndroidKitNavigationItem<Key : Any>(
    public val key: Key,
    public val label: String,
    public val icon: ImageVector,
    public val selectedIcon: ImageVector = icon,
    public val showDividerAfterInFlyout: Boolean = false,
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun <Key : Any> AdaptiveNavigationScaffold(
    items: List<AndroidKitNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    modifier: Modifier = Modifier,
    compactVisibleDestinationCount: Int = 4,
    showCompactLabels: Boolean = false,
    content: @Composable () -> Unit,
): Unit {
    require(items.isNotEmpty()) { "At least one navigation item is required." }
    require(items.map { it.key }.distinct().size == items.size) { "Navigation item keys must be unique." }
    require(items.any { it.key == selectedKey }) { "The selected navigation key is not registered." }
    require(compactVisibleDestinationCount in 1..4) {
        "Compact navigation supports between one and four directly visible destinations."
    }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = remember(adaptiveInfo) { androidKitNavigationSuiteType(adaptiveInfo) }
    val compact = layoutType == NavigationSuiteType.ShortNavigationBarCompact
    val primaryItems = items.take(compactVisibleDestinationCount)
    val overflowItems = items.drop(compactVisibleDestinationCount)
    val selectedInOverflow = overflowItems.any { it.key == selectedKey }
    var flyoutVisible by remember { mutableStateOf(false) }

    if (compact) {
        CompactNavigationLayout(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            navigation = {
                FloatingNavigationBar(
                    items = primaryItems,
                    overflowItems = overflowItems,
                    selectedKey = selectedKey,
                    onSelected = onSelected,
                    overflowSelected = selectedInOverflow,
                    flyoutVisible = flyoutVisible,
                    onFlyoutVisibleChange = { flyoutVisible = it },
                    showLabels = showCompactLabels,
                )
            },
            content = content,
        )
    } else {
        CompositionLocalProvider(
            LocalAndroidKitNavigationInsets provides WindowInsets(0, 0, 0, 0),
        ) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    items.forEach { item ->
                        item(
                            selected = item.key == selectedKey,
                            onClick = { onSelected(item.key) },
                            icon = {
                                Icon(
                                    imageVector = if (item.key == selectedKey) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                },
                modifier = modifier
                    .fillMaxSize()
                    .imePadding(),
                layoutType = layoutType,
                containerColor = MaterialTheme.colorScheme.background,
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = MaterialTheme.colorScheme.surface,
                    navigationRailContainerColor = MaterialTheme.colorScheme.surface,
                    navigationDrawerContainerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CompactNavigationLayout(
    navigation: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
): Unit {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val navigationPlaceables = subcompose(CompactNavigationSlot.Navigation, navigation)
            .map { measurable ->
                measurable.measure(constraints.copy(minHeight = 0))
            }
        val navigationHeight = navigationPlaceables.maxOfOrNull { it.height } ?: 0
        val contentPlaceables = subcompose(CompactNavigationSlot.Content) {
            CompositionLocalProvider(
                LocalAndroidKitNavigationInsets provides WindowInsets(
                    left = 0,
                    top = 0,
                    right = 0,
                    bottom = navigationHeight,
                ),
            ) {
                content()
            }
        }.map { measurable -> measurable.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }
            navigationPlaceables.forEach { placeable ->
                placeable.placeRelative(
                    x = (constraints.maxWidth - placeable.width) / 2,
                    y = constraints.maxHeight - placeable.height,
                )
            }
        }
    }
}

private enum class CompactNavigationSlot {
    Content,
    Navigation,
}

@Composable
private fun <Key : Any> FloatingNavigationBar(
    items: List<AndroidKitNavigationItem<Key>>,
    overflowItems: List<AndroidKitNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    overflowSelected: Boolean,
    flyoutVisible: Boolean,
    onFlyoutVisibleChange: (Boolean) -> Unit,
    showLabels: Boolean,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    var navigationBounds by remember { mutableStateOf(Rect.Zero) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .exclude(WindowInsets.ime)
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
            .padding(dimensions.floatingNavigationMargin),
        contentAlignment = Alignment.Center,
    ) {
        FloatingSurface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = dimensions.floatingNavigationMaxWidth)
                .onGloballyPositioned { navigationBounds = it.boundsInWindow() },
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensions.spaceExtraSmall),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimensions.minimumTouchTarget)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        val selected = item.key == selectedKey
                        CompactNavigationBarItem(
                            selected = selected,
                            onClick = { onSelected(item.key) },
                            icon = if (selected) item.selectedIcon else item.icon,
                            label = item.label,
                            showLabel = showLabels,
                        )
                    }
                    if (overflowItems.isNotEmpty()) {
                        CompactNavigationBarItem(
                            selected = overflowSelected,
                            onClick = { onFlyoutVisibleChange(true) },
                            icon = Icons.Default.MoreVert,
                            label = AndroidKitThemeTokens.strings.more,
                            showLabel = showLabels,
                        )
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    NavigationFlyout(
                        expanded = flyoutVisible,
                        navigationTopInWindow = navigationBounds.top,
                        items = overflowItems,
                        selectedKey = selectedKey,
                        onDismissRequest = { onFlyoutVisibleChange(false) },
                        onSelected = { key ->
                            onFlyoutVisibleChange(false)
                            onSelected(key)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CompactNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
): Unit {
    val visuals = floatingSurfaceVisuals()
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else visuals.contentColor,
        label = "compact navigation item content",
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = AndroidKitThemeTokens.dimensions.minimumTouchTarget)
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompactNavigationItemContent(
            icon = icon,
            label = label,
            selected = selected,
            showLabel = showLabel,
            contentColor = contentColor,
        )
    }
}

@Composable
private fun CompactNavigationItemContent(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    showLabel: Boolean,
    contentColor: Color,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "compact navigation selection indicator",
    )
    val indicatorModifier = if (showLabel) {
        Modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = dimensions.spaceSmall)
    } else {
        Modifier
            .size(dimensions.floatingNavigationIndicatorSize)
            .background(containerColor, CircleShape)
    }
    Column(
        modifier = indicatorModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label.takeUnless { showLabel },
            modifier = Modifier.size(dimensions.floatingNavigationIconSize),
            tint = contentColor,
        )
        if (showLabel) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun <Key : Any> NavigationFlyout(
    expanded: Boolean,
    navigationTopInWindow: Float,
    items: List<AndroidKitNavigationItem<Key>>,
    selectedKey: Key,
    onDismissRequest: () -> Unit,
    onSelected: (Key) -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val density = LocalDensity.current
    val safeDrawingTopPx = WindowInsets.safeDrawing.getTop(density)
    val maximumHeight = with(density) {
        (navigationTopInWindow.toInt() - safeDrawingTopPx)
            .coerceAtLeast(0)
            .toDp()
    }
    val visuals = floatingSurfaceVisuals()
    val shape = MaterialTheme.shapes.extraLarge
    val gapPx = with(density) { dimensions.floatingNavigationMargin.roundToPx() }
    val positionProvider = remember(gapPx) {
        NavigationFlyoutPositionProvider(gapPx = gapPx)
    }
    if (expanded) {
        Popup(
            popupPositionProvider = positionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            FloatingSurface(
                modifier = Modifier
                    .fillMaxWidth(FlyoutWindowWidthFraction)
                    .heightIn(
                        max = (maximumHeight - dimensions.floatingNavigationMargin)
                            .coerceAtLeast(0.dp),
                    ),
                shape = shape,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    val displayedItems = items.asReversed()
                    displayedItems.forEachIndexed { index, item ->
                        val selected = item.key == selectedKey
                        val itemColor = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            visuals.contentColor
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = dimensions.minimumTouchTarget)
                                .clickable(
                                    role = Role.Button,
                                    onClick = { onSelected(item.key) },
                                )
                                .padding(
                                    horizontal = dimensions.spaceMedium,
                                    vertical = dimensions.spaceSmall,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(dimensions.floatingActionIconSize),
                                tint = itemColor,
                            )
                            Text(
                                text = item.label,
                                modifier = Modifier.weight(1f),
                                color = itemColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (item.showDividerAfterInFlyout && index != displayedItems.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = dimensions.spaceMedium)
                                    .testTag(FlyoutDividerTestTag),
                                thickness = dimensions.floatingSurfaceBorderWidth,
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private class NavigationFlyoutPositionProvider(
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.right - popupContentSize.width
            LayoutDirection.Rtl -> anchorBounds.left
        }
        val maximumX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        return IntOffset(
            x = preferredX.coerceIn(0, maximumX),
            y = (anchorBounds.top - gapPx - popupContentSize.height).coerceAtLeast(0),
        )
    }
}

private const val FlyoutDividerTestTag = "androidKitNavigationFlyoutDivider"
private const val FlyoutWindowWidthFraction = 0.75f

private fun androidKitNavigationSuiteType(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType =
    when (val recommended = NavigationSuiteScaffoldDefaults.navigationSuiteType(adaptiveInfo)) {
        NavigationSuiteType.NavigationBar,
        NavigationSuiteType.ShortNavigationBarMedium,
        -> NavigationSuiteType.ShortNavigationBarCompact

        NavigationSuiteType.WideNavigationRailCollapsed,
        NavigationSuiteType.WideNavigationRailExpanded,
        -> NavigationSuiteType.NavigationRail

        else -> recommended
    }
