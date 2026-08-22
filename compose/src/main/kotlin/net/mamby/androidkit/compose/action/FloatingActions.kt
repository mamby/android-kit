package net.mamby.androidkit.compose.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.layout.LocalAndroidKitBottomOverlayProtection
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
public fun FloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = AndroidKitThemeTokens.strings.back,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = CircleShape,
        visualSize = dimensions.floatingBackButtonSize,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

@Composable
public fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = AndroidKitThemeTokens.strings.add,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurfaceButton(
        onClick = onClick,
        shape = CircleShape,
        visualSize = dimensions.floatingAddButtonSize,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(dimensions.floatingActionIconSize),
        )
    }
}

@Composable
public fun FloatingActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    FloatingSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensions.spaceSmall,
                vertical = dimensions.spaceExtraSmall,
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
public fun FloatingActionBarIconItem(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = AndroidKitThemeTokens.dimensions.floatingActionBarIconSize,
): Unit {
    FloatingActionBarItemContent(
        onClick = onClick,
        label = contentDescription,
        modifier = modifier,
        icon = icon,
        iconSize = iconSize,
        showLabel = false,
        textStyle = MaterialTheme.typography.labelSmall,
    )
}

@Composable
public fun FloatingActionBarIconLabelItem(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = AndroidKitThemeTokens.dimensions.floatingActionBarIconSize,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
): Unit {
    FloatingActionBarItemContent(
        onClick = onClick,
        label = label,
        modifier = modifier,
        icon = icon,
        iconSize = iconSize,
        showLabel = true,
        textStyle = textStyle,
    )
}

@Composable
public fun FloatingActionBarTextItem(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
): Unit {
    FloatingActionBarItemContent(
        onClick = onClick,
        label = label,
        modifier = modifier,
        icon = null,
        iconSize = AndroidKitThemeTokens.dimensions.floatingActionBarIconSize,
        showLabel = true,
        textStyle = textStyle,
    )
}

public class FloatingActionBarFlyoutItem(
    public val icon: ImageVector,
    public val label: String,
    public val onClick: () -> Unit,
)

public enum class FloatingActionBarFlyoutStyle {
    Icon,
    IconAndLabel,
    Text,
}

@Composable
public fun FloatingActionBarFlyout(
    items: List<FloatingActionBarFlyoutItem>,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    contentDescription: String = AndroidKitThemeTokens.strings.more,
): Unit = FloatingActionBarFlyoutContent(
    items = items,
    modifier = modifier,
    style = if (showLabel) {
        FloatingActionBarFlyoutStyle.IconAndLabel
    } else {
        FloatingActionBarFlyoutStyle.Icon
    },
    contentDescription = contentDescription,
)

@Composable
public fun FloatingActionBarFlyout(
    items: List<FloatingActionBarFlyoutItem>,
    style: FloatingActionBarFlyoutStyle,
    modifier: Modifier = Modifier,
    contentDescription: String = AndroidKitThemeTokens.strings.more,
): Unit = FloatingActionBarFlyoutContent(
    items = items,
    modifier = modifier,
    style = style,
    contentDescription = contentDescription,
)

@Composable
private fun FloatingActionBarFlyoutContent(
    items: List<FloatingActionBarFlyoutItem>,
    modifier: Modifier,
    style: FloatingActionBarFlyoutStyle,
    contentDescription: String,
): Unit {
    require(items.isNotEmpty()) { "At least one flyout item is required." }
    var expanded by remember { mutableStateOf(false) }
    val dimensions = AndroidKitThemeTokens.dimensions
    val overlayKey = remember { Any() }
    val updateBottomOverlayProtection = LocalAndroidKitBottomOverlayProtection.current

    fun setExpanded(value: Boolean) {
        expanded = value
        updateBottomOverlayProtection(overlayKey, value)
    }

    DisposableEffect(overlayKey, updateBottomOverlayProtection) {
        onDispose { updateBottomOverlayProtection(overlayKey, false) }
    }

    Box(modifier = modifier) {
        when (style) {
            FloatingActionBarFlyoutStyle.Icon -> FloatingActionBarIconItem(
                onClick = { setExpanded(!expanded) },
                icon = Icons.Default.MoreVert,
                contentDescription = contentDescription,
            )

            FloatingActionBarFlyoutStyle.IconAndLabel -> FloatingActionBarIconLabelItem(
                onClick = { setExpanded(!expanded) },
                icon = Icons.Default.MoreVert,
                label = contentDescription,
            )

            FloatingActionBarFlyoutStyle.Text -> FloatingActionBarTextItem(
                onClick = { setExpanded(!expanded) },
                label = contentDescription,
            )
        }
        FloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
            offset = DpOffset(x = 0.dp, y = dimensions.spaceExtraSmall),
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    onClick = {
                        setExpanded(false)
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
                            modifier = Modifier.size(
                                dimensions.floatingActionBarIconSize,
                            ),
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
    iconSize: Dp,
    showLabel: Boolean,
    textStyle: TextStyle,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = dimensions.spaceSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = label.takeUnless { showLabel },
                modifier = Modifier.size(iconSize),
            )
        }
        if (showLabel) {
            Text(
                text = label,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
