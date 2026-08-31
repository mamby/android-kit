package net.mamby.androidkit.compose.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenuHorizontalAlignment
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenuPlacement
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.LocalAndroidKitFloatingNavigationInsets
import net.mamby.androidkit.compose.theme.AndroidKitAdaptiveNavigationItemStyle
import net.mamby.androidkit.compose.theme.AndroidKitFloatingNavigationStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals
import kotlin.math.roundToInt

public class AndroidKitFloatingNavigationItem<Key : Any>(
    public val key: Key,
    public val label: String,
    public val icon: ImageVector,
    public val selectedIcon: ImageVector = icon,
    public val contentDescription: String = label,
    public val badge: (@Composable () -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun <Key : Any> AndroidKitFloatingNavigation(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    modifier: Modifier = Modifier,
    compactVisibleDestinationCount: Int = 4,
    showCompactLabels: Boolean = false,
    style: AndroidKitFloatingNavigationStyle = AndroidKitThemeTokens.floatingNavigationStyle,
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

    if (compact) {
        CompactNavigationLayout(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .background(style.containerColor),
            navigation = {
                FloatingNavigationBar(
                    items = items,
                    selectedKey = selectedKey,
                    onSelected = onSelected,
                    showLabels = showCompactLabels,
                    visibleDestinationCount = compactVisibleDestinationCount,
                    style = style,
                )
            },
            content = content,
        )
    } else {
        val navigationSuiteItemColors = style.adaptiveItemStyle
            ?.toNavigationSuiteItemColors()
            ?: NavigationSuiteDefaults.itemColors()
        CompositionLocalProvider(
            LocalAndroidKitFloatingNavigationInsets provides WindowInsets(0, 0, 0, 0),
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
                                    contentDescription = item.contentDescription,
                                )
                            },
                            label = { Text(item.label) },
                            badge = item.badge,
                            colors = navigationSuiteItemColors,
                        )
                    }
                },
                modifier = modifier
                    .fillMaxSize()
                    .imePadding(),
                layoutType = layoutType,
                containerColor = style.containerColor,
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = style.navigationBarContainerColor,
                    navigationRailContainerColor = style.navigationRailContainerColor,
                    navigationDrawerContainerColor = style.navigationDrawerContainerColor,
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
                LocalAndroidKitFloatingNavigationInsets provides WindowInsets(
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

private data class CompactNavigationItemSlot(val index: Int)

private data class CompactNavigationItemMeasureSlot(val index: Int)

private enum class CompactNavigationItemsSlot {
    More,
    MoreMeasure,
}

private class CompactNavigationLayoutState {
    var selectionBounds by mutableStateOf<IntRect?>(null)
    var overflowStartIndex by mutableIntStateOf(Int.MAX_VALUE)
}

@Composable
private fun <Key : Any> FloatingNavigationBar(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    showLabels: Boolean,
    visibleDestinationCount: Int,
    style: AndroidKitFloatingNavigationStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val surfaceStyle = style.compactSurfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle
    val visuals = floatingSurfaceVisuals(surfaceStyle)
    val layoutState = remember { CompactNavigationLayoutState() }
    var overflowFlyoutVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                },
        )
        BoxWithConstraints(
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
            val barMaxWidth = maxWidth.coerceAtMost(dimensions.floatingNavigationMaxWidth)

            FloatingSurface(
                modifier = Modifier
                    .widthIn(max = barMaxWidth)
                    .testTag(CompactNavigationBarTestTag),
                shape = style.barShape,
                containerColor = Color.Transparent,
                style = surfaceStyle,
            ) {
                Box {
                    NavigationSelectionBackground(
                        modifier = Modifier.matchParentSize(),
                        containerColor = style.compactContainerColor,
                        selectedContainerColor = style.selectedContainerColor,
                        selectionBounds = layoutState.selectionBounds,
                        selectionShape = style.itemShape,
                        opacity = visuals.opacity,
                    )
                    CompactNavigationItemsLayout(
                        items = items,
                        selectedKey = selectedKey,
                        onSelected = onSelected,
                        onOverflowClick = {
                            overflowFlyoutVisible = !overflowFlyoutVisible
                        },
                        showLabels = showLabels,
                        visibleDestinationCount = visibleDestinationCount,
                        layoutState = layoutState,
                        backgroundContentPadding = dimensions.floatingNavigationContentPadding,
                        style = style,
                        modifier = Modifier
                            .padding(dimensions.floatingNavigationContentPadding)
                            .selectableGroup(),
                    )
                    val overflowItems = items.drop(
                        layoutState.overflowStartIndex.coerceIn(0, items.size),
                    )
                    LaunchedEffect(overflowItems.isEmpty()) {
                        if (overflowItems.isEmpty()) overflowFlyoutVisible = false
                    }
                    if (overflowFlyoutVisible && overflowItems.isNotEmpty()) {
                        NavigationOverflowFlyout(
                            items = overflowItems,
                            selectedKey = selectedKey,
                            onDismissRequest = { overflowFlyoutVisible = false },
                            onSelected = { key ->
                                overflowFlyoutVisible = false
                                onSelected(key)
                            },
                            style = style,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <Key : Any> CompactNavigationItemsLayout(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    onOverflowClick: () -> Unit,
    showLabels: Boolean,
    visibleDestinationCount: Int,
    layoutState: CompactNavigationLayoutState,
    backgroundContentPadding: Dp,
    style: AndroidKitFloatingNavigationStyle,
    modifier: Modifier = Modifier,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    SubcomposeLayout(modifier = modifier) { constraints ->
        val naturalConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
        )
        val requestedItems = items.take(visibleDestinationCount)
        val requestedMeasurements = requestedItems.mapIndexed { index, item ->
            subcompose(CompactNavigationItemMeasureSlot(index)) {
                CompactNavigationItemMeasurement(
                    icon = item.icon,
                    label = item.label,
                    showLabel = showLabels,
                    badge = item.badge,
                    style = style,
                )
            }.single().measure(naturalConstraints)
        }

        var visibleCount = requestedItems.size
        var visibleWidth = requestedMeasurements.sumOf { it.width }
        val needsOverflow = items.size > requestedItems.size || visibleWidth > constraints.maxWidth
        val measuredMore = if (needsOverflow) {
            subcompose(CompactNavigationItemsSlot.MoreMeasure) {
                CompactNavigationItemMeasurement(
                    icon = AndroidKitIcons.More,
                    label = AndroidKitThemeTokens.strings.more,
                    showLabel = showLabels,
                    badge = null,
                    style = style,
                )
            }.single().measure(naturalConstraints)
        } else {
            null
        }

        if (measuredMore != null) {
            while (visibleCount > 0 && visibleWidth + measuredMore.width > constraints.maxWidth) {
                visibleCount -= 1
                visibleWidth -= requestedMeasurements[visibleCount].width
            }
        }

        val visiblePlaceables = requestedItems.take(visibleCount).mapIndexed { index, item ->
            val selected = item.key == selectedKey
            subcompose(CompactNavigationItemSlot(index)) {
                CompactNavigationBarItem(
                    selected = selected,
                    onClick = { onSelected(item.key) },
                    icon = if (selected) item.selectedIcon else item.icon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    showLabel = showLabels,
                    badge = item.badge,
                    style = style,
                    modifier = Modifier.widthIn(min = dimensions.minimumTouchTarget),
                )
            }.single().measure(naturalConstraints)
        }
        val overflowItems = items.drop(visibleCount)
        val overflowSelected = overflowItems.any { it.key == selectedKey }
        val morePlaceable = if (overflowItems.isNotEmpty()) {
            subcompose(CompactNavigationItemsSlot.More) {
                Box {
                    CompactNavigationBarItem(
                        selected = overflowSelected,
                        onClick = onOverflowClick,
                        icon = AndroidKitIcons.More,
                        label = AndroidKitThemeTokens.strings.more,
                        contentDescription = AndroidKitThemeTokens.strings.more,
                        showLabel = showLabels,
                        badge = null,
                        style = style,
                        modifier = Modifier.widthIn(min = dimensions.minimumTouchTarget),
                    )
                }
            }.single().measure(naturalConstraints)
        } else {
            null
        }

        val width = visiblePlaceables.sumOf { it.width } + (morePlaceable?.width ?: 0)
        val height = maxOf(
            visiblePlaceables.maxOfOrNull { it.height } ?: 0,
            morePlaceable?.height ?: 0,
        )
        val layoutWidth = width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val layoutHeight = height.coerceIn(constraints.minHeight, constraints.maxHeight)
        val selectedPlaceableIndex = requestedItems
            .take(visibleCount)
            .indexOfFirst { it.key == selectedKey }
            .takeIf { it >= 0 }
            ?: visiblePlaceables.size.takeIf { overflowSelected }
        val backgroundPaddingPx = backgroundContentPadding.roundToPx()
        layout(width = layoutWidth, height = layoutHeight) {
            layoutState.overflowStartIndex = visibleCount
            layoutState.selectionBounds = null
            var x = 0
            (visiblePlaceables + listOfNotNull(morePlaceable)).forEachIndexed { index, placeable ->
                val y = (layoutHeight - placeable.height) / 2
                if (index == selectedPlaceableIndex) {
                    val relativeX = when (layoutDirection) {
                        LayoutDirection.Ltr -> x
                        LayoutDirection.Rtl -> layoutWidth - x - placeable.width
                    }
                    layoutState.selectionBounds = IntRect(
                        offset = IntOffset(
                            x = relativeX + backgroundPaddingPx,
                            y = y + backgroundPaddingPx,
                        ),
                        size = IntSize(placeable.width, placeable.height),
                    )
                }
                placeable.placeRelative(x = x, y = y)
                x += placeable.width
            }
        }
    }
}

@Composable
private fun CompactNavigationItemMeasurement(
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
    badge: (@Composable () -> Unit)?,
    style: AndroidKitFloatingNavigationStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val horizontalPadding = if (showLabel) {
        dimensions.floatingNavigationItemHorizontalPadding
    } else {
        dimensions.floatingNavigationIconItemHorizontalPadding
    }
    Box(
        modifier = Modifier
            .widthIn(min = dimensions.minimumTouchTarget)
            .heightIn(min = dimensions.minimumTouchTarget)
            .padding(
                horizontal = horizontalPadding,
                vertical = dimensions.floatingNavigationItemVerticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompactNavigationItemContent(
            icon = icon,
            label = label,
            contentDescription = label,
            showLabel = showLabel,
            contentColor = style.unselectedContentColor,
            badge = badge,
            style = style,
            exposeSemantics = false,
        )
    }
}

@Composable
private fun CompactNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String,
    showLabel: Boolean,
    badge: (@Composable () -> Unit)?,
    style: AndroidKitFloatingNavigationStyle,
    modifier: Modifier = Modifier,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val contentColor = if (selected) {
        style.selectedContentColor
    } else {
        style.unselectedContentColor
    }
    val horizontalPadding = if (showLabel) {
        dimensions.floatingNavigationItemHorizontalPadding
    } else {
        dimensions.floatingNavigationIconItemHorizontalPadding
    }
    Box(
        modifier = modifier
            .heightIn(min = dimensions.minimumTouchTarget)
            .minimumInteractiveComponentSize()
            .clip(style.itemShape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(
                horizontal = horizontalPadding,
                vertical = dimensions.floatingNavigationItemVerticalPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompactNavigationItemContent(
            icon = icon,
            label = label,
            contentDescription = contentDescription,
            showLabel = showLabel,
            contentColor = contentColor,
            badge = badge,
            style = style,
        )
    }
}

@Composable
private fun CompactNavigationItemContent(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    showLabel: Boolean,
    contentColor: Color,
    badge: (@Composable () -> Unit)?,
    style: AndroidKitFloatingNavigationStyle,
    exposeSemantics: Boolean = true,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    if (showLabel) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.floatingNavigationLabelSpacing),
        ) {
            NavigationIcon(
                icon = icon,
                contentDescription = null,
                badge = badge,
                size = dimensions.floatingNavigationLabeledIconSize,
                tint = contentColor,
            )
            Text(
                text = label,
                modifier = if (exposeSemantics) {
                    Modifier
                } else {
                    Modifier.clearAndSetSemantics {}
                },
                color = contentColor,
                style = style.labelTextStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Box(
            modifier = Modifier.size(dimensions.floatingNavigationIndicatorSize),
            contentAlignment = Alignment.Center,
        ) {
            NavigationIcon(
                icon = icon,
                contentDescription = contentDescription.takeIf { exposeSemantics },
                badge = badge,
                size = dimensions.floatingNavigationIconSize,
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun NavigationIcon(
    icon: ImageVector,
    contentDescription: String?,
    badge: (@Composable () -> Unit)?,
    size: Dp,
    tint: Color,
): Unit {
    val iconContent: @Composable () -> Unit = {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            tint = tint,
        )
    }
    if (badge == null) {
        iconContent()
    } else {
        BadgedBox(badge = { badge() }, content = { iconContent() })
    }
}

@Composable
private fun <Key : Any> NavigationOverflowFlyout(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onDismissRequest: () -> Unit,
    onSelected: (Key) -> Unit,
    style: AndroidKitFloatingNavigationStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val flyoutStyle = style.overflowFlyoutStyle
        ?: AndroidKitThemeTokens.floatingDropdownMenuStyle
    val flyoutSurfaceStyle = flyoutStyle.surfaceStyle
        ?: AndroidKitThemeTokens.floatingSurfaceStyle
    val flyoutVisuals = floatingSurfaceVisuals(flyoutSurfaceStyle)
    val flyoutContainerColor = flyoutSurfaceStyle.containerColor
        .takeIf { it != Color.Unspecified }
        ?: AndroidKitThemeTokens.colorScheme.surface
    val transparentFlyoutStyle = flyoutStyle.copy(
        surfaceStyle = flyoutSurfaceStyle.copy(
            containerColor = Color.Transparent,
            opacity = 1f,
        ),
    )
    var flyoutBounds by remember { mutableStateOf<IntRect?>(null) }
    var selectedItemBounds by remember(selectedKey, items) { mutableStateOf<IntRect?>(null) }
    val relativeSelectionBounds = selectedItemBounds.relativeTo(flyoutBounds)
    val selectedItemColors = MenuDefaults.itemColors(
        textColor = style.selectedContentColor,
        leadingIconColor = style.selectedContentColor,
        trailingIconColor = style.selectedContentColor,
    )
    val unselectedItemColors = MenuDefaults.itemColors(
        textColor = style.unselectedContentColor,
        leadingIconColor = style.unselectedContentColor,
        trailingIconColor = style.unselectedContentColor,
    )
    AndroidKitFloatingDropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        placement = AndroidKitFloatingDropdownMenuPlacement.Above,
        horizontalAlignment = AndroidKitFloatingDropdownMenuHorizontalAlignment.End,
        style = transparentFlyoutStyle,
        contentPadding = PaddingValues.Zero,
    ) {
        Box(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                flyoutBounds = coordinates.intBoundsInRoot()
            },
        ) {
            NavigationSelectionBackground(
                modifier = Modifier.matchParentSize(),
                containerColor = flyoutContainerColor,
                selectedContainerColor = style.selectedContainerColor,
                selectionBounds = relativeSelectionBounds,
                selectionShape = style.itemShape,
                opacity = flyoutVisuals.opacity,
            )
            Column(
                modifier = Modifier.padding(dimensions.floatingNavigationContentPadding),
            ) {
                items.forEach { item ->
                    val selected = item.key == selectedKey
                    DropdownMenuItem(
                        modifier = Modifier
                            .clip(style.itemShape)
                            .fillMaxWidth()
                            .semantics {
                                role = Role.Tab
                                this.selected = selected
                            }
                            .then(
                                if (selected) {
                                    Modifier.onGloballyPositioned { coordinates ->
                                        selectedItemBounds = coordinates.intBoundsInRoot()
                                    }
                                } else {
                                    Modifier
                                },
                            ),
                        text = {
                            Text(
                                text = item.label,
                                style = style.overflowItemTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = { onSelected(item.key) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(dimensions.floatingNavigationIconSize),
                            )
                        },
                        trailingIcon = item.badge,
                        colors = if (selected) selectedItemColors else unselectedItemColors,
                        contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationSelectionBackground(
    containerColor: Color,
    selectedContainerColor: Color,
    selectionBounds: IntRect?,
    selectionShape: Shape,
    opacity: Float,
    modifier: Modifier = Modifier,
): Unit {
    val backgroundModifier = modifier.let { baseModifier ->
        if (opacity == 1f) {
            baseModifier
        } else {
            baseModifier.graphicsLayer { alpha = opacity }
        }
    }
    Canvas(modifier = backgroundModifier) {
        drawRect(containerColor)
        selectionBounds?.let { bounds ->
            val selectionSize = Size(
                width = bounds.width.toFloat(),
                height = bounds.height.toFloat(),
            )
            val outline = selectionShape.createOutline(
                size = selectionSize,
                layoutDirection = layoutDirection,
                density = this,
            )
            withTransform(
                transformBlock = {
                    translate(
                        left = bounds.left.toFloat(),
                        top = bounds.top.toFloat(),
                    )
                },
            ) {
                drawNavigationSelectionOutline(
                    outline = outline,
                    color = selectedContainerColor,
                )
            }
        }
    }
}

private fun androidx.compose.ui.layout.LayoutCoordinates.intBoundsInRoot(): IntRect =
    boundsInRoot().let { bounds ->
        IntRect(
            left = bounds.left.roundToInt(),
            top = bounds.top.roundToInt(),
            right = bounds.right.roundToInt(),
            bottom = bounds.bottom.roundToInt(),
        )
    }

private fun IntRect?.relativeTo(parentBounds: IntRect?): IntRect? {
    val bounds = this ?: return null
    val parent = parentBounds ?: return null
    return IntRect(
        left = bounds.left - parent.left,
        top = bounds.top - parent.top,
        right = bounds.right - parent.left,
        bottom = bounds.bottom - parent.top,
    )
}

private fun DrawScope.drawNavigationSelectionOutline(
    outline: Outline,
    color: Color,
): Unit = when (outline) {
    is Outline.Rectangle -> drawRect(
        color = color,
        topLeft = outline.rect.topLeft,
        size = outline.rect.size,
    )

    is Outline.Rounded -> drawPath(
        path = Path().apply { addRoundRect(outline.roundRect) },
        color = color,
    )

    is Outline.Generic -> drawPath(
        path = outline.path,
        color = color,
    )
}

private const val CompactNavigationBarTestTag = "androidKitFloatingNavigationBar"

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

@Composable
private fun AndroidKitAdaptiveNavigationItemStyle.toNavigationSuiteItemColors(): NavigationSuiteItemColors {
    val defaultDrawerColors = NavigationDrawerItemDefaults.colors()
    val defaultSelectedDrawerContainer = defaultDrawerColors.containerColor(selected = true).value
    val defaultUnselectedDrawerContainer = defaultDrawerColors.containerColor(selected = false).value
    val defaultSelectedDrawerIcon = defaultDrawerColors.iconColor(selected = true).value
    val defaultUnselectedDrawerIcon = defaultDrawerColors.iconColor(selected = false).value
    val defaultSelectedDrawerText = defaultDrawerColors.textColor(selected = true).value
    val defaultUnselectedDrawerText = defaultDrawerColors.textColor(selected = false).value
    val defaultSelectedDrawerBadge = defaultDrawerColors.badgeColor(selected = true).value
    val defaultUnselectedDrawerBadge = defaultDrawerColors.badgeColor(selected = false).value

    val selectedDrawerText = selectedTextColor.orDefault(defaultSelectedDrawerText)
    val unselectedDrawerText = unselectedTextColor.orDefault(defaultUnselectedDrawerText)

    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            selectedTextColor = selectedTextColor,
            indicatorColor = selectedIndicatorColor,
            unselectedIconColor = unselectedIconColor,
            unselectedTextColor = unselectedTextColor,
            disabledIconColor = disabledIconColor,
            disabledTextColor = disabledTextColor,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = selectedIconColor,
            selectedTextColor = selectedTextColor,
            indicatorColor = selectedIndicatorColor,
            unselectedIconColor = unselectedIconColor,
            unselectedTextColor = unselectedTextColor,
            disabledIconColor = disabledIconColor,
            disabledTextColor = disabledTextColor,
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = drawerSelectedContainerColor.orDefault(
                selectedIndicatorColor.orDefault(defaultSelectedDrawerContainer),
            ),
            unselectedContainerColor = drawerUnselectedContainerColor.orDefault(
                defaultUnselectedDrawerContainer,
            ),
            selectedIconColor = selectedIconColor.orDefault(defaultSelectedDrawerIcon),
            unselectedIconColor = unselectedIconColor.orDefault(defaultUnselectedDrawerIcon),
            selectedTextColor = selectedDrawerText,
            unselectedTextColor = unselectedDrawerText,
            selectedBadgeColor = drawerSelectedBadgeColor.orDefault(
                selectedTextColor.orDefault(defaultSelectedDrawerBadge),
            ),
            unselectedBadgeColor = drawerUnselectedBadgeColor.orDefault(
                unselectedTextColor.orDefault(defaultUnselectedDrawerBadge),
            ),
        ),
    )
}

private fun Color.orDefault(default: Color): Color =
    if (this == Color.Unspecified) default else this
