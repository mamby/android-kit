package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface

@Composable
public fun AndroidKitFloatingDropdownMenu(
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
public fun AndroidKitFloatingDropdownMenu(
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
    val dimensions = AndroidKitThemeTokens.dimensions
    val shape = AndroidKitThemeTokens.shapes.extraLarge
    val scrollState = rememberScrollState()
    val positionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(
        dropdownMenuAnchorPosition = MenuAnchorPosition.Below,
        offset = offset,
    )
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        popupPositionProvider = positionProvider,
    ) {
        FloatingSurface(
            shape = shape,
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = dimensions.spaceSmall)
                    .width(IntrinsicSize.Max)
                    .verticalScroll(scrollState),
                content = content,
            )
        }
    }
}
