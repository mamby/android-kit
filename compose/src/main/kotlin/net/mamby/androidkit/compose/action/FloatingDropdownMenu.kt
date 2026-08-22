package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = shape,
        containerColor = visuals.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = visuals.border,
        content = content,
    )
}
