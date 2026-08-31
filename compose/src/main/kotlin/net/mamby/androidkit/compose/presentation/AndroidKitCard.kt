package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import net.mamby.androidkit.compose.action.AndroidKitFloatingDropdownMenu
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitCardStyle
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
    style: AndroidKitCardStyle = AndroidKitThemeTokens.cardStyle,
    contentPadding: PaddingValues = PaddingValues(AndroidKitThemeTokens.dimensions.spaceMedium),
    contentSpacing: Dp = AndroidKitThemeTokens.dimensions.spaceSmall,
    headerSupportingContent: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    Card(
        modifier = modifier,
        shape = style.shape,
        colors = AndroidKitCardDefaults.colors(style),
        border = AndroidKitCardDefaults.border(style),
    ) {
        AndroidKitCardContent(
            menuItems = menuItems,
            header = header,
            headerSupportingContent = headerSupportingContent,
            contentPadding = contentPadding,
            contentSpacing = contentSpacing,
            content = content,
        )
    }
}

@Composable
public fun AndroidKitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuItems: List<AndroidKitCardMenuItem> = emptyList(),
    header: (@Composable ColumnScope.() -> Unit)? = null,
    style: AndroidKitCardStyle = AndroidKitThemeTokens.cardStyle,
    contentPadding: PaddingValues = PaddingValues(AndroidKitThemeTokens.dimensions.spaceMedium),
    contentSpacing: Dp = AndroidKitThemeTokens.dimensions.spaceSmall,
    headerSupportingContent: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = style.shape,
        colors = AndroidKitCardDefaults.colors(style),
        border = AndroidKitCardDefaults.border(style),
    ) {
        AndroidKitCardContent(
            menuItems = menuItems,
            header = header,
            headerSupportingContent = headerSupportingContent,
            contentPadding = contentPadding,
            contentSpacing = contentSpacing,
            content = content,
        )
    }
}

@Composable
private fun AndroidKitCardContent(
    menuItems: List<AndroidKitCardMenuItem>,
    header: (@Composable ColumnScope.() -> Unit)?,
    headerSupportingContent: (@Composable ColumnScope.() -> Unit)?,
    contentPadding: PaddingValues,
    contentSpacing: Dp,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    if (menuItems.isEmpty()) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            if (header != null || headerSupportingContent != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
                ) {
                    header?.let {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(
                                dimensions.spaceExtraSmall,
                            ),
                            content = it,
                        )
                    }
                    headerSupportingContent?.let {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(
                                dimensions.spaceExtraSmall,
                            ),
                            content = it,
                        )
                    }
                }
            }
            content()
        }
        return
    }

    val layoutDirection = LocalLayoutDirection.current
    val startPadding = when (layoutDirection) {
        LayoutDirection.Ltr -> contentPadding.calculateLeftPadding(layoutDirection)
        LayoutDirection.Rtl -> contentPadding.calculateRightPadding(layoutDirection)
    }
    val endPadding = when (layoutDirection) {
        LayoutDirection.Ltr -> contentPadding.calculateRightPadding(layoutDirection)
        LayoutDirection.Rtl -> contentPadding.calculateLeftPadding(layoutDirection)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = contentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (header == null) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = startPadding),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
                        content = header,
                    )
                }
                AndroidKitCardOverflowMenu(items = menuItems)
            }
            if (headerSupportingContent != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = startPadding, end = endPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spaceExtraSmall),
                    content = headerSupportingContent,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
    }
}

@Composable
private fun AndroidKitCardOverflowMenu(
    items: List<AndroidKitCardMenuItem>,
): Unit {
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
