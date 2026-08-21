package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

@Composable
public fun FloatingDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val visuals = floatingSurfaceVisuals()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = visuals.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = AndroidKitThemeTokens.dimensions.floatingDropdownShadowElevation,
        border = visuals.border,
        content = content,
    )
}
