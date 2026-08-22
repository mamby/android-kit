package net.mamby.androidkit.compose.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

@Composable
public fun FloatingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
): Unit = FloatingDropdownMenuContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    offset = DpOffset.Zero,
    content = content,
)

@Composable
public fun FloatingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
): Unit = FloatingDropdownMenuContent(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    offset = offset,
    content = content,
)

@Composable
private fun FloatingDropdownMenuContent(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals()
    val shape = MaterialTheme.shapes.extraLarge
    // Paint translucency inside the popup's transparent Surface so it is composed against the
    // protected page content instead of being flattened against an opaque popup layer.
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(
            color = visuals.containerColor,
            shape = shape,
        ),
        offset = offset,
        shape = shape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = AndroidKitThemeTokens.dimensions.floatingDropdownShadowElevation,
        border = visuals.border,
        content = content,
    )
}
