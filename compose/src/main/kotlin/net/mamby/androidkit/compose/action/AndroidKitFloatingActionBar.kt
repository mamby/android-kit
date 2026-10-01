package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionBarStyle
import net.mamby.androidkit.compose.theme.AndroidKitFloatingToolbarStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

public typealias AndroidKitFloatingActionBarDsl = AndroidKitFloatingToolbarDsl

public typealias AndroidKitFloatingActionBarScope = AndroidKitFloatingToolbarScope

public typealias AndroidKitFloatingActionBarFlyoutScope = AndroidKitFloatingToolbarFlyoutScope

public typealias AndroidKitFloatingActionBarIconAndLabelLayout =
    AndroidKitFloatingToolbarIconAndLabelLayout

@Composable
public fun AndroidKitFloatingActionBar(
    modifier: Modifier = Modifier,
    style: AndroidKitFloatingActionBarStyle = AndroidKitThemeTokens.floatingActionBarStyle,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = AndroidKitThemeTokens.componentTokens.actionBar.horizontalPadding,
        vertical = AndroidKitThemeTokens.componentTokens.actionBar.verticalPadding,
    ),
    content: AndroidKitFloatingActionBarScope.() -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.actionBar
    AndroidKitFloatingToolbar(
        modifier = modifier,
        style = style.asFloatingToolbarStyle(),
        contentPadding = contentPadding,
        itemSpacing = tokens.itemSpacing,
        flyoutAnchor = AndroidKitFloatingToolbarFlyoutAnchor.Toolbar,
        content = content,
    )
}

@Composable
private fun AndroidKitFloatingActionBarStyle.asFloatingToolbarStyle():
    AndroidKitFloatingToolbarStyle =
    AndroidKitFloatingToolbarStyle(
        surfaceStyle = surfaceStyle,
        flyoutStyle = flyoutStyle ?: AndroidKitThemeTokens.componentTokens.actionBar.flyoutStyle,
        separatorColor = AndroidKitThemeTokens.componentTokens.actionBar.separatorColor,
        shape = shape,
        itemShape = itemShape,
        labelTextStyle = labelTextStyle,
        iconSize = AndroidKitThemeTokens.dimensions.floatingActionBarIconSize,
    )
