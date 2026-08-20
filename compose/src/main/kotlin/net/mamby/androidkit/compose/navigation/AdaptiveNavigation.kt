package net.mamby.androidkit.compose.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.form.AndroidKitModalSheet
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

public class AndroidKitNavigationItem<Key : Any>(
    public val key: Key,
    public val label: String,
    public val icon: ImageVector,
    public val selectedIcon: ImageVector = icon,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun <Key : Any> AdaptiveNavigationScaffold(
    items: List<AndroidKitNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    modifier: Modifier = Modifier,
    compactPrimaryCount: Int = 4,
    content: @Composable (PaddingValues) -> Unit,
): Unit {
    require(items.isNotEmpty()) { "At least one navigation item is required." }
    require(items.map { it.key }.distinct().size == items.size) { "Navigation item keys must be unique." }
    require(items.any { it.key == selectedKey }) { "The selected navigation key is not registered." }
    require(compactPrimaryCount in 1..4) { "Compact navigation supports between one and four primary items." }

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = remember(adaptiveInfo) { androidKitNavigationSuiteType(adaptiveInfo) }
    val compact = layoutType == NavigationSuiteType.ShortNavigationBarCompact
    val primaryItems = items.take(compactPrimaryCount)
    val overflowItems = items.drop(compactPrimaryCount)
    val selectedInOverflow = overflowItems.any { it.key == selectedKey }
    var moreVisible by remember { mutableStateOf(false) }

    if (compact) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                FloatingNavigationBar(
                    items = primaryItems,
                    selectedKey = selectedKey,
                    onSelected = onSelected,
                    hasOverflow = overflowItems.isNotEmpty(),
                    overflowSelected = selectedInOverflow,
                    onOverflow = { moreVisible = true },
                )
            },
        ) { contentPadding ->
            content(contentPadding)
        }
    } else {
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
            modifier = modifier,
            layoutType = layoutType,
            containerColor = MaterialTheme.colorScheme.background,
            navigationSuiteColors = NavigationSuiteDefaults.colors(
                navigationBarContainerColor = MaterialTheme.colorScheme.surface,
                navigationRailContainerColor = MaterialTheme.colorScheme.surface,
                navigationDrawerContainerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            content(PaddingValues())
        }
    }

    if (moreVisible) {
        AndroidKitModalSheet(
            title = AndroidKitThemeTokens.strings.more,
            onDismissRequest = { moreVisible = false },
        ) {
            val dimensions = AndroidKitThemeTokens.dimensions
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = dimensions.cardMinWidth * 1.5f),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
            ) {
                items(overflowItems, key = { it.key }) { item ->
                    FilledTonalButton(
                        onClick = {
                            moreVisible = false
                            onSelected(item.key)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = if (item.key == selectedKey) item.selectedIcon else item.icon,
                            contentDescription = null,
                        )
                        Text(
                            text = item.label,
                            modifier = Modifier.padding(start = dimensions.spaceSmall),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <Key : Any> FloatingNavigationBar(
    items: List<AndroidKitNavigationItem<Key>>,
    selectedKey: Key,
    onSelected: (Key) -> Unit,
    hasOverflow: Boolean,
    overflowSelected: Boolean,
    onOverflow: () -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .asPaddingValues(),
            )
            .padding(dimensions.floatingNavigationMargin),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = dimensions.spaceExtraSmall,
            shadowElevation = dimensions.spaceSmall,
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                windowInsets = WindowInsets(0, 0, 0, 0),
                tonalElevation = 0.dp,
            ) {
                items.forEach { item ->
                    val selected = item.key == selectedKey
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelected(item.key) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
                if (hasOverflow) {
                    NavigationBarItem(
                        selected = overflowSelected,
                        onClick = onOverflow,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = AndroidKitThemeTokens.strings.more,
                            )
                        },
                        label = {
                            Text(
                                text = AndroidKitThemeTokens.strings.more,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
        }
    }
}

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
