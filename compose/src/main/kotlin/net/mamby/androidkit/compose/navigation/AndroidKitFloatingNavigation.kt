package net.mamby.androidkit.compose.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.LocalAndroidKitFloatingNavigationInsets
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

public class AndroidKitFloatingNavigationItem<Key : Any>(
    public val key: Key,
    public val label: String,
    public val icon: ImageVector,
    public val selectedIcon: ImageVector = icon,
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
    content: @Composable () -> Unit,
): Unit {
    val colorScheme = AndroidKitThemeTokens.colorScheme
    require(items.isNotEmpty()) { "At least one navigation item is required." }
    require(items.map { it.key }.distinct().size == items.size) { "Navigation item keys must be unique." }
    require(items.any { it.key == selectedKey }) { "The selected navigation key is not registered." }
    require(compactVisibleDestinationCount in 1..4) {
        "Compact navigation supports between one and four directly visible destinations."
    }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = remember(adaptiveInfo) { androidKitNavigationSuiteType(adaptiveInfo) }
    val compact = layoutType == NavigationSuiteType.ShortNavigationBarCompact
    var overflowSheetVisible by remember { mutableStateOf(false) }

    if (compact) {
        CompactNavigationLayout(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .background(colorScheme.background),
            navigation = {
                FloatingNavigationBar(
                    items = items,
                    selectedKey = selectedKey,
                    onSelected = onSelected,
                    overflowSheetVisible = overflowSheetVisible,
                    onOverflowSheetVisibleChange = { overflowSheetVisible = it },
                    showLabels = showCompactLabels,
                    visibleDestinationCount = compactVisibleDestinationCount,
                )
            },
            content = content,
        )
    } else {
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
                containerColor = colorScheme.background,
                navigationSuiteColors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = colorScheme.surface,
                    navigationRailContainerColor = colorScheme.surface,
                    navigationDrawerContainerColor = colorScheme.surface,
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
    var overflowStartIndex: Int = Int.MAX_VALUE
}

@Composable
private fun <Key : Any> FloatingNavigationBar(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    overflowSheetVisible: Boolean,
    onOverflowSheetVisibleChange: (Boolean) -> Unit,
    showLabels: Boolean,
    visibleDestinationCount: Int,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val colorScheme = AndroidKitThemeTokens.colorScheme
    val visuals = floatingSurfaceVisuals()
    val layoutState = remember { CompactNavigationLayoutState() }
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
                shape = AndroidKitThemeTokens.shapes.extraLarge,
                containerColor = Color.Transparent,
            ) {
                Box {
                    val backgroundModifier = Modifier.matchParentSize().let { modifier ->
                        if (visuals.opacity == 1f) {
                            modifier
                        } else {
                            modifier.graphicsLayer { alpha = visuals.opacity }
                        }
                    }
                    val selectionShape = AndroidKitThemeTokens.shapes.extraLarge
                    Canvas(modifier = backgroundModifier) {
                        drawRect(colorScheme.surface)
                        layoutState.selectionBounds?.let { bounds ->
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
                                    color = colorScheme.secondaryContainer,
                                )
                            }
                        }
                    }
                    CompactNavigationItemsLayout(
                        items = items,
                        selectedKey = selectedKey,
                        onSelected = onSelected,
                        onOverflowSheetVisibleChange = onOverflowSheetVisibleChange,
                        showLabels = showLabels,
                        visibleDestinationCount = visibleDestinationCount,
                        layoutState = layoutState,
                        backgroundContentPadding = dimensions.floatingNavigationContentPadding,
                        modifier = Modifier
                            .padding(dimensions.floatingNavigationContentPadding)
                            .selectableGroup(),
                    )
                }
            }
        }
        if (overflowSheetVisible) {
            NavigationOverflowSheet(
                visible = true,
                items = items.drop(layoutState.overflowStartIndex.coerceIn(0, items.size)),
                selectedKey = selectedKey,
                onDismissRequest = { onOverflowSheetVisibleChange(false) },
                onSelected = { key ->
                    onOverflowSheetVisibleChange(false)
                    onSelected(key)
                },
            )
        }
    }
}

@Composable
private fun <Key : Any> CompactNavigationItemsLayout(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    onOverflowSheetVisibleChange: (Boolean) -> Unit,
    showLabels: Boolean,
    visibleDestinationCount: Int,
    layoutState: CompactNavigationLayoutState,
    backgroundContentPadding: Dp,
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
                    showLabel = showLabels,
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
                        onClick = {
                            layoutState.overflowStartIndex = visibleCount
                            onOverflowSheetVisibleChange(true)
                        },
                        icon = AndroidKitIcons.More,
                        label = AndroidKitThemeTokens.strings.more,
                        showLabel = showLabels,
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
            showLabel = showLabel,
            contentColor = AndroidKitThemeTokens.colorScheme.onSurface,
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
    showLabel: Boolean,
    modifier: Modifier = Modifier,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val visuals = floatingSurfaceVisuals()
    val contentColor = if (selected) {
        AndroidKitThemeTokens.colorScheme.primary
    } else {
        visuals.contentColor
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
            .clip(AndroidKitThemeTokens.shapes.extraLarge)
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
            showLabel = showLabel,
            contentColor = contentColor,
        )
    }
}

@Composable
private fun CompactNavigationItemContent(
    icon: ImageVector,
    label: String,
    showLabel: Boolean,
    contentColor: Color,
    exposeSemantics: Boolean = true,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    if (showLabel) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.floatingNavigationLabelSpacing),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensions.floatingNavigationLabeledIconSize),
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
                style = AndroidKitThemeTokens.typography.labelSmall,
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
            Icon(
                imageVector = icon,
                contentDescription = label.takeIf { exposeSemantics },
                modifier = Modifier.size(dimensions.floatingNavigationIconSize),
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun <Key : Any> NavigationOverflowSheet(
    visible: Boolean,
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onDismissRequest: () -> Unit,
    onSelected: (Key) -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val colorScheme = AndroidKitThemeTokens.colorScheme
    val visuals = floatingSurfaceVisuals()
    val itemTextStyle = AndroidKitThemeTokens.typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
    )
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colorScheme.secondaryContainer,
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = colorScheme.primary,
        unselectedIconColor = visuals.contentColor,
        selectedTextColor = colorScheme.primary,
        unselectedTextColor = visuals.contentColor,
    )
    AndroidKitBottomSheet(
        visible = visible,
        title = AndroidKitThemeTokens.strings.more,
        onDismiss = onDismissRequest,
        fitContent = true,
        showChrome = false,
    ) {
        items.forEach { item ->
            val selected = item.key == selectedKey
            NavigationDrawerItem(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = item.label,
                        style = itemTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = selected,
                onClick = { onSelected(item.key) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.floatingNavigationIconSize),
                    )
                },
                colors = itemColors,
            )
        }
    }
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
