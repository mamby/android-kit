package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Immutable
public class AndroidKitCardMenuItem(
    public val label: String,
    public val onClick: () -> Unit,
    public val icon: ImageVector? = null,
    public val enabled: Boolean = true,
)

@Composable
public fun AndroidKitCard(
    modifier: Modifier = Modifier,
    menuItems: List<AndroidKitCardMenuItem> = emptyList(),
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    Card(
        modifier = modifier,
        shape = AndroidKitThemeTokens.shapes.extraLarge,
        colors = AndroidKitCardDefaults.colors(),
        border = AndroidKitCardDefaults.border(),
    ) {
        Column(
            modifier = Modifier.padding(dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            if (header != null || menuItems.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = dimensions.spaceSmall,
                        alignment = Alignment.End,
                    ),
                    verticalAlignment = Alignment.Top,
                ) {
                    if (header == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
                            content = header,
                        )
                    }
                    if (menuItems.isNotEmpty()) {
                        AndroidKitCardOverflowMenu(items = menuItems)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun AndroidKitCardOverflowMenu(items: List<AndroidKitCardMenuItem>): Unit {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
        ) {
            Icon(
                imageVector = AndroidKitIcons.More,
                contentDescription = AndroidKitThemeTokens.strings.more,
            )
        }
        AndroidKitFloatingDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                    enabled = item.enabled,
                    leadingIcon = item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        }
    }
}
