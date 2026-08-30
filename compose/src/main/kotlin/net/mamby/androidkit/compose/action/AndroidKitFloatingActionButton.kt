package net.mamby.androidkit.compose.action

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionButtonStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
public fun AndroidKitFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: AndroidKitFloatingActionButtonStyle = AndroidKitThemeTokens.floatingActionButtonStyle,
    content: @Composable () -> Unit,
): Unit {
    FloatingSurfaceButton(
        onClick = onClick,
        shape = style.shape,
        visualSize = style.visualSize,
        modifier = modifier,
        enabled = enabled,
        style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
        content = content,
    )
}
