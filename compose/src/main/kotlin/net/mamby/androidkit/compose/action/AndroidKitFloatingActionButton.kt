package net.mamby.androidkit.compose.action

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

@Composable
public fun AndroidKitFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
): Unit {
    FloatingSurfaceButton(
        onClick = onClick,
        shape = CircleShape,
        visualSize = AndroidKitThemeTokens.dimensions.floatingActionButtonSize,
        modifier = modifier,
        content = content,
    )
}
