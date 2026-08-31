package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitFloatingToolbarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

public typealias AndroidKitFloatingActionBarDsl = AndroidKitFloatingToolbarDsl

public typealias AndroidKitFloatingActionBarScope = AndroidKitFloatingToolbarScope

public typealias AndroidKitFloatingActionBarFlyoutScope = AndroidKitFloatingToolbarFlyoutScope

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
    AndroidKitFloatingToolbar(
        modifier = modifier,
        style = style.asFloatingToolbarStyle(),
        contentPadding = contentPadding,
        itemSpacing = itemSpacing,
        flyoutAnchor = AndroidKitFloatingToolbarFlyoutAnchor.Toolbar,
        content = content,
    )
}

@Composable
private fun AndroidKitFloatingActionBarStyle.asFloatingToolbarStyle():
    AndroidKitFloatingToolbarStyle =
    AndroidKitFloatingToolbarStyle(
        surfaceStyle = surfaceStyle,
        dropdownMenuStyle = dropdownMenuStyle,
        separatorColor = AndroidKitThemeTokens.floatingToolbarStyle.separatorColor,
        shape = shape,
        itemShape = itemShape,
        labelTextStyle = labelTextStyle,
        iconSize = AndroidKitThemeTokens.dimensions.floatingActionBarIconSize,
    )
