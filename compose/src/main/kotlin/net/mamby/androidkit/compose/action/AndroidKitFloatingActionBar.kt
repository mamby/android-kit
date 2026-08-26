package net.mamby.androidkit.compose.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
    ): Unit

    public fun iconAndLabel(
        onClick: () -> Unit,
        icon: ImageVector,
        label: String,
        modifier: Modifier = Modifier,
    ): Unit

    public fun text(
        onClick: () -> Unit,
        label: String,
        modifier: Modifier = Modifier,
    ): Unit

    public fun flyout(
        modifier: Modifier = Modifier,
        style: AndroidKitFloatingActionBarFlyoutStyle = AndroidKitFloatingActionBarFlyoutStyle.Icon,
        contentDescription: String? = null,
        content: AndroidKitFloatingActionBarFlyoutScope.() -> Unit,
    ): Unit
}

@AndroidKitFloatingActionBarDsl
public interface AndroidKitFloatingActionBarFlyoutScope {
    public fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
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
    content: AndroidKitFloatingActionBarScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val scope = FloatingActionBarScopeImpl().apply(content)
    FloatingSurface(
        modifier = modifier,
        shape = AndroidKitThemeTokens.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensions.spaceSmall,
                vertical = dimensions.spaceExtraSmall,
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            scope.items.forEach { item -> FloatingActionBarItem(item) }
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
    ) {
        items += FloatingActionBarItemDefinition.Icon(
            onClick = onClick,
            icon = icon,
            label = contentDescription,
            modifier = modifier,
        )
    }

    override fun iconAndLabel(
        onClick: () -> Unit,
        icon: ImageVector,
        label: String,
        modifier: Modifier,
    ) {
        items += FloatingActionBarItemDefinition.IconAndLabel(
            onClick = onClick,
            icon = icon,
            label = label,
            modifier = modifier,
        )
    }

    override fun text(
        onClick: () -> Unit,
        label: String,
        modifier: Modifier,
    ) {
        items += FloatingActionBarItemDefinition.Text(
            onClick = onClick,
            label = label,
            modifier = modifier,
        )
    }

    override fun flyout(
        modifier: Modifier,
        style: AndroidKitFloatingActionBarFlyoutStyle,
        contentDescription: String?,
        content: AndroidKitFloatingActionBarFlyoutScope.() -> Unit,
    ) {
        val flyoutScope = FloatingActionBarFlyoutScopeImpl().apply(content)
        require(flyoutScope.items.isNotEmpty()) { "At least one flyout item is required." }
        items += FloatingActionBarItemDefinition.Flyout(
            items = flyoutScope.items,
            style = style,
            modifier = modifier,
            contentDescription = contentDescription,
        )
    }
}

private class FloatingActionBarFlyoutScopeImpl : AndroidKitFloatingActionBarFlyoutScope {
    val items: MutableList<FloatingActionBarFlyoutItem> = mutableListOf()

    override fun item(
        icon: ImageVector,
        label: String,
        onClick: () -> Unit,
    ) {
        items += FloatingActionBarFlyoutItem(
            icon = icon,
            label = label,
            onClick = onClick,
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
    ) : FloatingActionBarItemDefinition

    class IconAndLabel(
        val onClick: () -> Unit,
        val icon: ImageVector,
        val label: String,
        override val modifier: Modifier,
    ) : FloatingActionBarItemDefinition

    class Text(
        val onClick: () -> Unit,
        val label: String,
        override val modifier: Modifier,
    ) : FloatingActionBarItemDefinition

    class Flyout(
        val items: List<FloatingActionBarFlyoutItem>,
        val style: AndroidKitFloatingActionBarFlyoutStyle,
        override val modifier: Modifier,
        val contentDescription: String?,
    ) : FloatingActionBarItemDefinition
}

private class FloatingActionBarFlyoutItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun FloatingActionBarItem(item: FloatingActionBarItemDefinition) {
    when (item) {
        is FloatingActionBarItemDefinition.Icon -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = false,
        )

        is FloatingActionBarItemDefinition.IconAndLabel -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = item.icon,
            showLabel = true,
        )

        is FloatingActionBarItemDefinition.Text -> FloatingActionBarItemContent(
            onClick = item.onClick,
            label = item.label,
            modifier = item.modifier,
            icon = null,
            showLabel = true,
        )

        is FloatingActionBarItemDefinition.Flyout -> FloatingActionBarFlyoutContent(
            items = item.items,
            modifier = item.modifier,
            style = item.style,
            contentDescription = item.contentDescription
                ?: AndroidKitThemeTokens.strings.more,
        )
    }
}

@Composable
private fun FloatingActionBarFlyoutContent(
    items: List<FloatingActionBarFlyoutItem>,
    modifier: Modifier,
    style: AndroidKitFloatingActionBarFlyoutStyle,
    contentDescription: String,
): Unit {
    var expanded by remember { mutableStateOf(false) }
    val dimensions = AndroidKitThemeTokens.dimensions

    Box(modifier = modifier) {
        FloatingActionBarItemContent(
            onClick = { expanded = !expanded },
            label = contentDescription,
            modifier = Modifier,
            icon = Icons.Default.MoreVert.takeUnless {
                style == AndroidKitFloatingActionBarFlyoutStyle.Text
            },
            showLabel = style != AndroidKitFloatingActionBarFlyoutStyle.Icon,
        )
        AndroidKitFloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = dimensions.spaceExtraSmall),
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = AndroidKitThemeTokens.typography.labelSmall,
                        )
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
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
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = modifier
            .heightIn(min = dimensions.minimumTouchTarget)
            .minimumInteractiveComponentSize()
            .clip(AndroidKitThemeTokens.shapes.extraLarge)
            .clickable(role = Role.Button, onClick = onClick)
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
                style = AndroidKitThemeTokens.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
