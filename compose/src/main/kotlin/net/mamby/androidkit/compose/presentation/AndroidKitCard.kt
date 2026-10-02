package net.mamby.androidkit.compose.presentation

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import net.mamby.androidkit.compose.action.AndroidKitActionFlyout
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitCardStyle
import net.mamby.androidkit.compose.theme.AndroidKitCardColors
import net.mamby.androidkit.compose.theme.AndroidKitComponentTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.androidKitHostTheme
import net.mamby.androidkit.compose.theme.withColors

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
    title: String? = null,
    colors: AndroidKitCardColors = AndroidKitCardColors(),
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val hostTheme = androidKitHostTheme()
    AndroidKitComponentTheme {
        AndroidKitCard(
            modifier = modifier,
            menuItems = menuItems,
            title = title,
            style = AndroidKitThemeTokens.cardStyle.withColors(colors),
            supportingText = supportingText,
            content = { hostTheme.Content { content() } },
        )
    }
}

@Composable
internal fun AndroidKitCard(
    modifier: Modifier = Modifier,
    menuItems: List<AndroidKitCardMenuItem> = emptyList(),
    title: String? = null,
    style: AndroidKitCardStyle,
    contentPadding: PaddingValues = PaddingValues(AndroidKitThemeTokens.componentTokens.card.contentPadding),
    contentSpacing: Dp = AndroidKitThemeTokens.componentTokens.card.contentSpacing,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.card
    Card(
        modifier = modifier,
        shape = style.shape,
        colors = AndroidKitCardDefaults.colors(style),
        border = AndroidKitCardDefaults.border(style),
    ) {
        AndroidKitCardContent(
            menuItems = menuItems,
            header = title?.let { text -> { Text(text, style = style.titleTextStyle ?: tokens.titleTextStyle) } },
            headerSupportingContent = supportingText?.let { text -> { Text(text, style = style.supportingTextStyle ?: tokens.supportingTextStyle, color = if (style.supportingTextColor == androidx.compose.ui.graphics.Color.Unspecified) tokens.supportingTextColor else style.supportingTextColor) } },
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
    title: String? = null,
    colors: AndroidKitCardColors = AndroidKitCardColors(),
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val hostTheme = androidKitHostTheme()
    AndroidKitComponentTheme {
        AndroidKitCard(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            menuItems = menuItems,
            title = title,
            style = AndroidKitThemeTokens.cardStyle.withColors(colors),
            supportingText = supportingText,
            content = { hostTheme.Content { content() } },
        )
    }
}

@Composable
internal fun AndroidKitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuItems: List<AndroidKitCardMenuItem> = emptyList(),
    title: String? = null,
    style: AndroidKitCardStyle,
    contentPadding: PaddingValues = PaddingValues(AndroidKitThemeTokens.componentTokens.card.contentPadding),
    contentSpacing: Dp = AndroidKitThemeTokens.componentTokens.card.contentSpacing,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.card
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
            header = title?.let { text -> { Text(text, style = style.titleTextStyle ?: tokens.titleTextStyle) } },
            headerSupportingContent = supportingText?.let { text -> { Text(text, style = style.supportingTextStyle ?: tokens.supportingTextStyle, color = if (style.supportingTextColor == androidx.compose.ui.graphics.Color.Unspecified) tokens.supportingTextColor else style.supportingTextColor) } },
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
    val tokens = AndroidKitThemeTokens.componentTokens.card
    if (menuItems.isEmpty()) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
        ) {
            if (header != null || headerSupportingContent != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(tokens.headerTextSpacing),
                ) {
                    header?.let {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(
                                tokens.headerTextSpacing,
                            ),
                            content = it,
                        )
                    }
                    headerSupportingContent?.let {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(
                                tokens.headerTextSpacing,
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
            verticalArrangement = Arrangement.spacedBy(tokens.headerTextSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.headerActionSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (header == null) {
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = startPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.headerTextSpacing),
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
                    verticalArrangement = Arrangement.spacedBy(tokens.headerTextSpacing),
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
        AndroidKitActionFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            style = AndroidKitThemeTokens.componentTokens.card.flyoutStyle,
        ) {
            items.forEach { item ->
                item(
                    label = item.label,
                    onClick = item.onClick,
                    enabled = item.enabled,
                    icon = item.icon,
                )
            }
        }
    }
}
