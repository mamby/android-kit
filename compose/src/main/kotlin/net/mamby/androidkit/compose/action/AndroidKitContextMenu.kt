package net.mamby.androidkit.compose.action

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.PopupProperties
import net.mamby.androidkit.compose.R
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens


/**
 * Wraps non-interactive app content with a point-anchored context menu.
 * Touch & hold and secondary mouse presses open at the local pointer position;
 * accessibility and Menu / Shift+F10 invocation anchor to the content bounds.
 * Supply [onClick] for an optional primary action instead of nesting clickable controls.
 * Kit owns menu state, rendering and dismissal; [menu] declares the shared flyout entries.
 */
@Composable
public fun AndroidKitContextMenu(
    menu: AndroidKitActionFlyoutScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
): Unit {
    AndroidKitContextMenuContent(menu, modifier, enabled, onClick, null, null, content)
}

/** Section-card entries retain their localized primary-action semantics on the menu anchor. */
@Composable
internal fun AndroidKitContextMenuContent(
    menu: AndroidKitActionFlyoutScope.() -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    onClickLabel: String?,
    role: Role?,
    content: @Composable () -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.contextMenu
    var expanded by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf<Offset?>(null) }
    var pressPosition by remember { mutableStateOf<Offset?>(null) }
    val haptics = LocalHapticFeedback.current
    val label = stringResource(R.string.androidkit_compose_more)
    val density = LocalDensity.current
    val positionProvider = remember(position, density) {
        ContextMenuPositionProvider(position, density)
    }
    val dismiss = { expanded = false }
    val openFromKeyboard = {
        position = null
        expanded = true
    }
    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }

    val input = if (!enabled) Modifier else Modifier
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.type == PointerEventType.Press) {
                        pressPosition = event.changes.firstOrNull()?.position
                        if (event.buttons.isSecondaryPressed && event.changes.none { it.isConsumed }) {
                            position = pressPosition
                            expanded = true
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
        }
        .onPreviewKeyEvent {
            pressPosition = null
            val contextKey = it.key == Key.Menu || (it.key == Key.F10 && it.isShiftPressed)
            if (contextKey) {
                if (it.type == KeyEventType.KeyUp) openFromKeyboard()
                true
            } else false
        }

    val trigger = when {
        !enabled -> Modifier.semantics(mergeDescendants = true) {
            disabled()
            role?.let { this.role = it }
        }
        onClick != null -> Modifier.semantics {
            onLongClick(label) {
                openFromKeyboard()
                true
            }
        }.combinedClickable(
            onClick = onClick,
            onClickLabel = onClickLabel,
            role = role,
            onLongClickLabel = label,
            onLongClick = {
                position = pressPosition
                expanded = true
            },
        )
        else -> Modifier
            .pointerInput(haptics) {
                detectTapGestures(onLongPress = {
                    position = it
                    expanded = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                })
            }
            .semantics {
                onLongClick(label) {
                    openFromKeyboard()
                    true
                }
            }
            .focusable()
    }

    Box(
        modifier
            .background(if (expanded && enabled) tokens.selectedContainerColor else Color.Transparent)
            .semantics { selected = expanded && enabled }
            .then(input)
            .then(trigger),
    ) {
        content()
        ActionFlyoutPopup(
            expanded = expanded && enabled,
            onDismissRequest = dismiss,
            onActionDismissRequest = dismiss,
            positionProvider = positionProvider,
            style = tokens.flyoutStyle,
            contentPadding = PaddingValues(vertical = tokens.verticalPadding),
            properties = PopupProperties(focusable = true),
        ) {
            AndroidKitActionFlyoutScope().apply(menu).render(this)
        }
    }
}
