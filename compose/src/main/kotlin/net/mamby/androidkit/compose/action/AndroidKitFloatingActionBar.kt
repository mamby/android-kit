package net.mamby.androidkit.compose.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface

@DslMarker
public annotation class AndroidKitFloatingActionBarDsl

@AndroidKitFloatingActionBarDsl
public interface AndroidKitFloatingActionBarScope {
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

    public fun flyout(
        modifier: Modifier = Modifier,
        style: AndroidKitFloatingActionBarFlyoutStyle = AndroidKitFloatingActionBarFlyoutStyle.Icon,
        contentDescription: String? = null,
        enabled: Boolean = true,
        content: AndroidKitFloatingActionBarFlyoutScope.() -> Unit,
    ): Unit

    public fun item(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ): Unit
}

@AndroidKitFloatingActionBarDsl
public interface AndroidKitFloatingActionBarFlyoutScope {
    public fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean = true,
    ): Unit
}

public enum class AndroidKitFloatingActionBarFlyoutStyle {
    Icon,
    IconAndLabel,
    Text,
}

@Composable
public fun AndroidKitFloatingActionBar(
    modifier: Modifier = Modifier,
    style: AndroidKitFloatingActionBarStyle = AndroidKitThemeTokens.floatingActionBarStyle,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.dimensions.spaceSmall,
        vertical = AndroidKitThemeTokens.dimensions.spaceExtraSmall,
    ),
    itemSpacing: Dp = AndroidKitThemeTokens.dimensions.spaceSmall,
    content: AndroidKitFloatingActionBarScope.() -> Unit,
): Unit {
    val scope = FloatingActionBarScopeImpl().apply(content)
    FloatingSurface(
        modifier = modifier,
        shape = style.shape,
        style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scope.items.forEach { item -> FloatingActionBarItem(item, style) }
        }
    }
}

private class FloatingActionBarScopeImpl : AndroidKitFloatingActionBarScope {
    val items: MutableList<FloatingActionBarItemDefinition> = mutableListOf()

    override fun icon(
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String,
        modifier: Modifier,
        enabled: Boolean,
    ) {
        items += FloatingActionBarItemDefinition.Icon(
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
        items += FloatingActionBarItemDefinition.IconAndLabel(
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
        items += FloatingActionBarItemDefinition.Text(
            onClick = onClick,
            label = label,
            modifier = modifier,
            enabled = enabled,
        )
    }

    override fun flyout(
        modifier: Modifier,
        style: AndroidKitFloatingActionBarFlyoutStyle,
        contentDescription: String?,
        enabled: Boolean,
        content: AndroidKitFloatingActionBarFlyoutScope.() -> Unit,
    ) {
        val flyoutScope = FloatingActionBarFlyoutScopeImpl().apply(content)
        require(flyoutScope.items.isNotEmpty()) { "At least one flyout item is required." }
        items += FloatingActionBarItemDefinition.Flyout(
            items = flyoutScope.items,
            style = style,
            modifier = modifier,
            contentDescription = contentDescription,
            enabled = enabled,
        )
    }

    override fun item(
        modifier: Modifier,
        content: @Composable () -> Unit,
    ) {
        items += FloatingActionBarItemDefinition.Custom(
            modifier = modifier,
            content = content,
        )
    }
}

private class FloatingActionBarFlyoutScopeImpl : AndroidKitFloatingActionBarFlyoutScope {
    val items: MutableList<FloatingActionBarFlyoutItem> = mutableListOf()

    override fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
        enabled: Boolean,
    ) {
        items += FloatingActionBarFlyoutItem(
            icon = icon,
            label = label,
            onClick = onClick,
            enabled = enabled,
        )
    }
}

private sealed interface FloatingActionBarItemDefinition {
    val modifier: Modifier

    class Icon(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingActionBarItemDefinition

    class IconAndLabel(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingActionBarItemDefinition

    class Text(
        val onClick: () -> Unit,
        val label: String,
        override val modifier: Modifier,
        val enabled: Boolean,
    ) : FloatingActionBarItemDefinition

    class Flyout(
        val items: List<FloatingActionBarFlyoutItem>,
        val style: AndroidKitFloatingActionBarFlyoutStyle,
        override val modifier: Modifier,
        val contentDescription: String?,
        val enabled: Boolean,
    ) : FloatingActionBarItemDefinition

    class Custom(
        override val modifier: Modifier,
        val content: @Composable () -> Unit,
    ) : FloatingActionBarItemDefinition
}

private class FloatingActionBarFlyoutItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean,
)

@Composable
private fun FloatingActionBarItem(
    item: FloatingActionBarItemDefinition,
    style: AndroidKitFloatingActionBarStyle,
) {
    when (item) {
        is FloatingActionBarItemDefinition.Icon -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = false,
            enabled = item.enabled,
            style = style,
        )

        is FloatingActionBarItemDefinition.IconAndLabel -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = true,
            enabled = item.enabled,
            style = style,
        )

        is FloatingActionBarItemDefinition.Text -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = null,
            showLabel = true,
            enabled = item.enabled,
            style = style,
        )

        is FloatingActionBarItemDefinition.Flyout -> FloatingActionBarFlyoutContent(
            items = item.items,
            modifier = item.modifier,
            style = item.style,
            contentDescription = item.contentDescription
                ?: AndroidKitThemeTokens.strings.more,
            enabled = item.enabled,
            actionBarStyle = style,
        )

        is FloatingActionBarItemDefinition.Custom -> Box(
            modifier = item.modifier.minimumInteractiveComponentSize(),
            contentAlignment = Alignment.Center,
        ) {
            item.content()
        }
    }
}

@Composable
private fun FloatingActionBarFlyoutContent(
    items: List<FloatingActionBarFlyoutItem>,
    modifier: Modifier,
    style: AndroidKitFloatingActionBarFlyoutStyle,
    contentDescription: String,
    enabled: Boolean,
    actionBarStyle: AndroidKitFloatingActionBarStyle,
): Unit {
    var expanded by remember { mutableStateOf(false) }
    val dimensions = AndroidKitThemeTokens.dimensions

    Box(modifier = modifier) {
        FloatingActionBarItemContent(
            onClick = { expanded = !expanded },
            label = contentDescription,
            modifier = Modifier,
            icon = AndroidKitIcons.More.takeUnless {
                style == AndroidKitFloatingActionBarFlyoutStyle.Text
            },
            showLabel = style != AndroidKitFloatingActionBarFlyoutStyle.Icon,
            enabled = enabled,
            style = actionBarStyle,
        )
        AndroidKitFloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = dimensions.spaceExtraSmall),
            style = actionBarStyle.dropdownMenuStyle
                ?: AndroidKitThemeTokens.floatingDropdownMenuStyle,
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = actionBarStyle.labelTextStyle,
                        )
                    },
                    onClick = {
                        expanded = false
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
                            modifier = Modifier.size(dimensions.floatingActionBarIconSize),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun FloatingActionBarItemContent(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier,
    icon: ImageVector?,
    showLabel: Boolean,
    enabled: Boolean,
    style: AndroidKitFloatingActionBarStyle,
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
                modifier = Modifier.size(dimensions.floatingActionBarIconSize),
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
