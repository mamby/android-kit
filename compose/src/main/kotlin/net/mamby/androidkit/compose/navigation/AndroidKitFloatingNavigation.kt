package net.mamby.androidkit.compose.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
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
    public val showDividerAfterInFlyout: Boolean = false,
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
    val primaryItems = items.take(compactVisibleDestinationCount)
    val overflowItems = items.drop(compactVisibleDestinationCount)
    val selectedInOverflow = overflowItems.any { it.key == selectedKey }
    var flyoutVisible by remember { mutableStateOf(false) }

    if (compact) {
        CompactNavigationLayout(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .background(colorScheme.background),
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

@Composable
private fun <Key : Any> FloatingNavigationBar(
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    overflowItems: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    overflowSelected: Boolean,
    flyoutVisible: Boolean,
    onFlyoutVisibleChange: (Boolean) -> Unit,
    showLabels: Boolean,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val indicatorBoundsTransform = remember {
        BoundsTransform { _, _ ->
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        }
    }
    val selectionIndicator = remember {
        movableContentOf<Modifier> { modifier ->
            val visuals = floatingSurfaceVisuals()
            val indicatorColor by animateColorAsState(
                targetValue = AndroidKitThemeTokens.colorScheme.secondaryContainer.copy(
                    alpha = visuals.containerColor.alpha,
                ),
                label = "compact navigation selection indicator color",
            )
            Box(
                modifier = modifier
                    .clip(AndroidKitThemeTokens.shapes.extraLarge)
                    .background(indicatorColor),
            )
        }
    }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = dimensions.floatingNavigationMaxWidth),
            ) {
                FloatingSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AndroidKitThemeTokens.shapes.extraLarge,
                ) {
                    LookaheadScope {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spaceExtraSmall)
                                .heightIn(min = dimensions.minimumTouchTarget)
                                .selectableGroup(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            items.forEach { item ->
                                val selected = item.key == selectedKey
                                Box(modifier = Modifier.weight(1f)) {
                                    if (selected) {
                                        selectionIndicator(
                                            Modifier
                                                .matchParentSize()
                                                .animateBounds(
                                                    lookaheadScope = this@LookaheadScope,
                                                    boundsTransform = indicatorBoundsTransform,
                                                ),
                                        )
                                    }
                                    CompactNavigationBarItem(
                                        selected = selected,
                                        onClick = { onSelected(item.key) },
                                        icon = if (selected) item.selectedIcon else item.icon,
                                        label = item.label,
                                        showLabel = showLabels,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            if (overflowItems.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (overflowSelected) {
                                        selectionIndicator(
                                            Modifier
                                                .matchParentSize()
                                                .animateBounds(
                                                    lookaheadScope = this@LookaheadScope,
                                                    boundsTransform = indicatorBoundsTransform,
                                                ),
                                        )
                                    }
                                    CompactNavigationBarItem(
                                        selected = overflowSelected,
                                        onClick = { onFlyoutVisibleChange(true) },
                                        icon = AndroidKitIcons.More,
                                        label = AndroidKitThemeTokens.strings.more,
                                        showLabel = showLabels,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    NavigationFlyout(
                                        expanded = flyoutVisible,
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
            }
        }
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
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            AndroidKitThemeTokens.colorScheme.primary
        } else {
            visuals.contentColor
        },
        label = "compact navigation item content",
    )
    val indicatorShape = AndroidKitThemeTokens.shapes.extraLarge
    Box(
        modifier = modifier
            .heightIn(min = dimensions.minimumTouchTarget)
            .minimumInteractiveComponentSize()
            .clip(indicatorShape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(dimensions.spaceExtraSmall),
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
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    if (showLabel) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensions.floatingNavigationIconSize),
                tint = contentColor,
            )
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
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
                contentDescription = label,
                modifier = Modifier.size(dimensions.floatingNavigationIconSize),
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun <Key : Any> NavigationFlyout(
    expanded: Boolean,
    items: List<AndroidKitFloatingNavigationItem<Key>>,
    selectedKey: Key,
    onDismissRequest: () -> Unit,
    onSelected: (Key) -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val visuals = floatingSurfaceVisuals()
    AndroidKitFloatingDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = 0.dp, y = dimensions.spaceExtraSmall),
    ) {
        val displayedItems = items.asReversed()
        displayedItems.forEachIndexed { index, item ->
            val selected = item.key == selectedKey
            val itemColor = if (selected) {
                AndroidKitThemeTokens.colorScheme.primary
            } else {
                visuals.contentColor
            }
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.label,
                        color = itemColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = { onSelected(item.key) },
                contentPadding = PaddingValues(
                    start = dimensions.spaceMedium,
                    end = dimensions.spaceLarge,
                ),
                leadingIcon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.floatingActionIconSize),
                        tint = itemColor,
                    )
                },
            )
            if (item.showDividerAfterInFlyout && index != displayedItems.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensions.spaceSmall)
                        .testTag(FlyoutDividerTestTag),
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = dimensions.spaceMedium),
                        thickness = Dp.Hairline,
                        color = AndroidKitThemeTokens.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

private const val FlyoutDividerTestTag = "androidKitFloatingNavigationFlyoutDivider"

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
