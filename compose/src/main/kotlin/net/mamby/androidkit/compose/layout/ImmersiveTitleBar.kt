package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangedIgnoreConsumed
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
internal fun Modifier.toggleTitleBarOnUnconsumedTap(
    enabled: Boolean,
    titleBarVisible: Boolean,
    onToggleTitleBar: () -> Unit,
): Modifier {
    val currentOnToggleTitleBar by rememberUpdatedState(onToggleTitleBar)
    val strings = AndroidKitThemeTokens.strings
    if (!enabled) return this

    return pointerInput(enabled) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Final,
            )
            if (awaitUnclaimedTapRelease(down.id)) {
                currentOnToggleTitleBar()
            }
        }
    }.semantics {
        customActions = listOf(
            CustomAccessibilityAction(
                label = if (titleBarVisible) strings.hideTitleBar else strings.showTitleBar,
                action = {
                    currentOnToggleTitleBar()
                    true
                },
            ),
        )
    }
}

private suspend fun AwaitPointerEventScope.awaitUnclaimedTapRelease(pointerId: PointerId): Boolean {
    var canceled = false
    while (true) {
        val event = awaitPointerEvent(pass = PointerEventPass.Final)
        val change = event.changes.firstOrNull { it.id == pointerId } ?: return false

        if (event.changes.any { it.id != pointerId && !it.previousPressed && it.pressed }) {
            canceled = true
        }
        if (change.positionChangedIgnoreConsumed() && change.isConsumed) {
            canceled = true
        }
        if (change.previousPressed && !change.pressed) {
            return !canceled && !change.isConsumed
        }
        if (!change.pressed || change.isOutOfBounds(size, extendedTouchPadding)) {
            return false
        }
    }
}
