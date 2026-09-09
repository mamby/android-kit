package net.mamby.androidkit.compose.action

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurfaceButton

/** Renders a Kit-owned icon button from data, with an optional Kit-owned tooltip. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitFloatingActionButton(action: AndroidKitFloatingAction.Button): Unit {
    val style = action.style ?: AndroidKitThemeTokens.floatingActionButtonStyle
    val button: @Composable (Modifier) -> Unit = { modifier ->
        FloatingSurfaceButton(
            onClick = action.onClick,
            shape = style.shape,
            visualSize = style.visualSize,
            modifier = modifier,
            enabled = action.enabled,
            style = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle,
        ) { FloatingActionIcon(action) }
    }
    val tooltip = action.tooltip
    if (tooltip == null) {
        button(action.modifier)
    } else {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = { PlainTooltip { Text(tooltip) } },
            state = rememberTooltipState(),
            modifier = action.modifier,
        ) { button(Modifier) }
    }
}
