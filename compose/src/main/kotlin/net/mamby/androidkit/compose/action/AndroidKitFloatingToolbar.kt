package net.mamby.androidkit.compose.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitFloatingToolbarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface

@DslMarker
public annotation class AndroidKitFloatingToolbarDsl

@AndroidKitFloatingToolbarDsl
public interface AndroidKitFloatingToolbarScope {
    public fun icon(
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ): Unit

    public fun iconAndLabel(
        onClick: () -> Unit,
        icon: ImageVector,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ): Unit

    public fun text(
        onClick: () -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
    ): Unit

    public fun separator(modifier: Modifier = Modifier): Unit

    public fun flyout(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        enabled: Boolean = true,
        placement: AndroidKitFloatingDropdownMenuPlacement =
            AndroidKitFloatingDropdownMenuPlacement.Below,
        content: AndroidKitFloatingToolbarFlyoutScope.() -> Unit,
    ): Unit

    public fun item(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ): Unit
}

@AndroidKitFloatingToolbarDsl
public interface AndroidKitFloatingToolbarFlyoutScope {
    public fun separator(modifier: Modifier = Modifier): Unit

    public fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean = true,
    ): Unit
}

public enum class AndroidKitFloatingToolbarFlyoutAnchor {
    Item,
    Toolbar,
}

@Composable
public fun AndroidKitFloatingToolbar(
    modifier: Modifier = Modifier,
    style: AndroidKitFloatingToolbarStyle = AndroidKitThemeTokens.floatingToolbarStyle,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.spaceSmall,
        vertical = AndroidKitThemeTokens.dimensions.spaceExtraSmall,
    ),
    itemSpacing: Dp = AndroidKitThemeTokens.dimensions.spaceSmall,
    flyoutAnchor: AndroidKitFloatingToolbarFlyoutAnchor =
        AndroidKitFloatingToolbarFlyoutAnchor.Item,
    content: AndroidKitFloatingToolbarScope.() -> Unit,
): Unit {
    val scope = FloatingToolbarScopeImpl().apply(content)
    var expandedToolbarFlyoutIndex by remember { mutableStateOf<Int?>(null) }
    val flyoutAvailability = scope.items.map { item ->
        (item as? FloatingToolbarItemDefinition.Flyout)?.enabled == true
    }

    LaunchedEffect(flyoutAnchor, flyoutAvailability) {
        val expandedIndex = expandedToolbarFlyoutIndex
        if (
            flyoutAnchor != AndroidKitFloatingToolbarFlyoutAnchor.Toolbar ||
            expandedIndex == null ||
            flyoutAvailability.getOrNull(expandedIndex) != true
        ) {
            expandedToolbarFlyoutIndex = null
        }
    }

    FloatingSurface(
        modifier = modifier,
        shape = style.shape,
        style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
    ) {
        Box {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                scope.items.forEachIndexed { index, item ->
                    FloatingToolbarItem(
                        item = item,
                        style = style,
                        flyoutAnchor = flyoutAnchor,
                        toolbarFlyoutExpanded = expandedToolbarFlyoutIndex == index,
                        onToolbarFlyoutExpandedChange = { expanded ->
                            expandedToolbarFlyoutIndex = if (expanded) index else null
                        },
                    )
                }
            }
            if (flyoutAnchor == AndroidKitFloatingToolbarFlyoutAnchor.Toolbar) {
                scope.items.forEachIndexed { index, item ->
                    if (item is FloatingToolbarItemDefinition.Flyout) {
                        FloatingToolbarFlyoutPopup(
                            items = item.items,
                            expanded = expandedToolbarFlyoutIndex == index,
                            onDismissRequest = {
                                if (expandedToolbarFlyoutIndex == index) {
                                    expandedToolbarFlyoutIndex = null
                                }
                            },
                            enabled = item.enabled,
                            placement = item.placement,
                            horizontalAlignment =
                                AndroidKitFloatingDropdownMenuHorizontalAlignment.End,
                            toolbarStyle = style,
                        )
                    }
                }
            }
        }
    }
}

private class FloatingToolbarScopeImpl : AndroidKitFloatingToolbarScope {
    val items: MutableList<FloatingToolbarItemDefinition> = mutableListOf()

    override fun icon(
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        items += FloatingToolbarItemDefinition.Icon(
            onClick = onClick,
            icon = icon,
            label = contentDescription,
            modifier = modifier,
            enabled = enabled,
        )
    }

    override fun iconAndLabel(
        onClick: () -> Unit,
        icon: ImageVector,
        label: String,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        items += FloatingToolbarItemDefinition.IconAndLabel(
            onClick = onClick,
            icon = icon,
            label = label,
            modifier = modifier,
            enabled = enabled,
        )
    }

    override fun text(
        onClick: () -> Unit,
        label: String,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        items += FloatingToolbarItemDefinition.Text(
            onClick = onClick,
            label = label,
            modifier = modifier,
            enabled = enabled,
        )
    }

    override fun separator(modifier: Modifier) {
        items += FloatingToolbarItemDefinition.Separator(modifier)
    }

    override fun flyout(
        modifier: Modifier,
        contentDescription: String?,
        enabled: Boolean,
        placement: AndroidKitFloatingDropdownMenuPlacement,
        content: AndroidKitFloatingToolbarFlyoutScope.() -> Unit,
    ) {
        val flyoutScope = FloatingToolbarFlyoutScopeImpl().apply(content)
        require(flyoutScope.items.any { it is FloatingToolbarFlyoutItem.Action }) {
            "At least one flyout item is required."
        }
        items += FloatingToolbarItemDefinition.Flyout(
            items = flyoutScope.items,
            modifier = modifier,
            contentDescription = contentDescription,
            enabled = enabled,
            placement = placement,
        )
    }

    override fun item(
        modifier: Modifier,
        content: @Composable () -> Unit,
    ) {
        items += FloatingToolbarItemDefinition.Custom(
            modifier = modifier,
            content = content,
        )
    }
}

private class FloatingToolbarFlyoutScopeImpl : AndroidKitFloatingToolbarFlyoutScope {
    val items: MutableList<FloatingToolbarFlyoutItem> = mutableListOf()

    override fun separator(modifier: Modifier) {
        items += FloatingToolbarFlyoutItem.Separator(modifier)
    }

    override fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean,
    ) {
        items += FloatingToolbarFlyoutItem.Action(
            icon = icon,
            label = label,
            onClick = onClick,
            enabled = enabled,
        )
    }
}

private sealed interface FloatingToolbarItemDefinition {
    val modifier: Modifier

    class Icon(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingToolbarItemDefinition

    class IconAndLabel(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingToolbarItemDefinition

    class Text(
        val onClick: () -> Unit,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingToolbarItemDefinition

    class Separator(
        override val modifier: Modifier,
    ) : FloatingToolbarItemDefinition

    class Flyout(
        val items: List<FloatingToolbarFlyoutItem>,
        override val modifier: Modifier,
        val contentDescription: String?,
        val enabled: Boolean,
        val placement: AndroidKitFloatingDropdownMenuPlacement,
    ) : FloatingToolbarItemDefinition

    class Custom(
        override val modifier: Modifier,
        val content: @Composable () -> Unit,
    ) : FloatingToolbarItemDefinition
}

private sealed interface FloatingToolbarFlyoutItem {
    val modifier: Modifier

    class Action(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit,
        val enabled: Boolean,
    ) : FloatingToolbarFlyoutItem {
        override val modifier: Modifier = Modifier
    }

    class Separator(
        override val modifier: Modifier,
    ) : FloatingToolbarFlyoutItem
}

@Composable
private fun FloatingToolbarItem(
    item: FloatingToolbarItemDefinition,
    style: AndroidKitFloatingToolbarStyle,
    flyoutAnchor: AndroidKitFloatingToolbarFlyoutAnchor,
    toolbarFlyoutExpanded: Boolean,
    onToolbarFlyoutExpandedChange: (Boolean) -> Unit,
): Unit {
    when (item) {
        is FloatingToolbarItemDefinition.Icon -> FloatingToolbarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = false,
            enabled = item.enabled,
            style = style,
        )

        is FloatingToolbarItemDefinition.IconAndLabel -> FloatingToolbarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = true,
            enabled = item.enabled,
            style = style,
        )

        is FloatingToolbarItemDefinition.Text -> FloatingToolbarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = null,
            showLabel = true,
            enabled = item.enabled,
            style = style,
        )

        is FloatingToolbarItemDefinition.Separator -> FloatingToolbarSeparator(
            modifier = item.modifier,
            style = style,
        )

        is FloatingToolbarItemDefinition.Flyout -> when (flyoutAnchor) {
            AndroidKitFloatingToolbarFlyoutAnchor.Item -> FloatingToolbarItemAnchoredFlyout(
                items = item.items,
                modifier = item.modifier,
                contentDescription = item.contentDescription
                    ?: AndroidKitThemeTokens.strings.more,
                enabled = item.enabled,
                placement = item.placement,
                toolbarStyle = style,
            )

            AndroidKitFloatingToolbarFlyoutAnchor.Toolbar -> FloatingToolbarFlyoutTrigger(
                onClick = {
                    onToolbarFlyoutExpandedChange(!toolbarFlyoutExpanded)
                },
                contentDescription = item.contentDescription
                    ?: AndroidKitThemeTokens.strings.more,
                modifier = item.modifier,
                enabled = item.enabled,
                toolbarStyle = style,
            )
        }

        is FloatingToolbarItemDefinition.Custom -> Box(
            modifier = item.modifier.minimumInteractiveComponentSize(),
            contentAlignment = Alignment.Center,
        ) {
            item.content()
        }
    }
}

@Composable
private fun FloatingToolbarItemAnchoredFlyout(
    items: List<FloatingToolbarFlyoutItem>,
    modifier: Modifier,
    contentDescription: String,
    enabled: Boolean,
    placement: AndroidKitFloatingDropdownMenuPlacement,
    toolbarStyle: AndroidKitFloatingToolbarStyle,
): Unit {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }

    Box(modifier = modifier) {
        FloatingToolbarFlyoutTrigger(
            onClick = { expanded = !expanded },
            contentDescription = contentDescription,
            modifier = Modifier,
            enabled = enabled,
            toolbarStyle = toolbarStyle,
        )
        FloatingToolbarFlyoutPopup(
            items = items,
            expanded = expanded,
            onDismissRequest = { expanded = false },
            enabled = enabled,
            placement = placement,
            horizontalAlignment = AndroidKitFloatingDropdownMenuHorizontalAlignment.Start,
            toolbarStyle = toolbarStyle,
        )
    }
}

@Composable
private fun FloatingToolbarFlyoutTrigger(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier,
    enabled: Boolean,
    toolbarStyle: AndroidKitFloatingToolbarStyle,
): Unit = FloatingToolbarItemContent(
    onClick = onClick,
    label = contentDescription,
    modifier = modifier,
    icon = AndroidKitIcons.More,
    showLabel = false,
    enabled = enabled,
    style = toolbarStyle,
)

@Composable
private fun FloatingToolbarFlyoutPopup(
    items: List<FloatingToolbarFlyoutItem>,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    enabled: Boolean,
    placement: AndroidKitFloatingDropdownMenuPlacement,
    horizontalAlignment: AndroidKitFloatingDropdownMenuHorizontalAlignment,
    toolbarStyle: AndroidKitFloatingToolbarStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions

    LaunchedEffect(enabled) {
        if (!enabled && expanded) onDismissRequest()
    }

    AndroidKitFloatingDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        placement = placement,
        horizontalAlignment = horizontalAlignment,
        style = toolbarStyle.dropdownMenuStyle
            ?: AndroidKitThemeTokens.floatingDropdownMenuStyle,
    ) {
        items.forEach { item ->
            when (item) {
                is FloatingToolbarFlyoutItem.Action -> DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = toolbarStyle.labelTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        item.onClick()
                    },
                    enabled = item.enabled,
                    contentPadding = PaddingValues(
                        start = dimensions.spaceMedium,
                        end = dimensions.spaceLarge,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(toolbarStyle.iconSize),
                        )
                    },
                )

                is FloatingToolbarFlyoutItem.Separator -> HorizontalDivider(
                    modifier = item.modifier.padding(
                        horizontal = dimensions.spaceMedium,
                        vertical = dimensions.spaceExtraSmall,
                    ),
                    color = toolbarStyle.separatorColor,
                )
            }
        }
    }
}

@Composable
private fun FloatingToolbarSeparator(
    modifier: Modifier,
    style: AndroidKitFloatingToolbarStyle,
): Unit {
    Box(
        modifier = modifier.padding(horizontal = AndroidKitThemeTokens.dimensions.spaceSmall),
        contentAlignment = Alignment.Center,
    ) {
        VerticalDivider(
            modifier = Modifier.height(style.iconSize),
            color = style.separatorColor,
        )
    }
}

@Composable
private fun FloatingToolbarItemContent(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier,
    icon: ImageVector?,
    showLabel: Boolean,
    enabled: Boolean,
    style: AndroidKitFloatingToolbarStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = modifier
            .clip(style.itemShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = dimensions.spaceSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = label.takeUnless { showLabel },
                modifier = Modifier.size(style.iconSize),
            )
        }
        if (showLabel) {
            Text(
                text = label,
                style = style.labelTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
