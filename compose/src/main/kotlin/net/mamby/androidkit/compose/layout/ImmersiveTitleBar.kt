package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
internal fun Modifier.toggleTitleBarOnUnconsumedTap(
    enabled: Boolean,
    titleBarVisible: Boolean,
    onTitleBarVisibilityChange: (Boolean) -> Unit,
): Modifier {
    val currentTitleBarVisible by rememberUpdatedState(titleBarVisible)
    val currentOnTitleBarVisibilityChange by rememberUpdatedState(onTitleBarVisibilityChange)
    val strings = AndroidKitThemeTokens.strings
    if (!enabled) return this

    return pointerInput(enabled) {
        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Final,
            )
            if (down.isConsumed) return@awaitEachGesture

            if (waitForUpOrCancellation(pass = PointerEventPass.Final) != null) {
                currentOnTitleBarVisibilityChange(!currentTitleBarVisible)
            }
        }
    }.semantics {
        customActions = listOf(
            CustomAccessibilityAction(
                label = if (titleBarVisible) strings.hideTitleBar else strings.showTitleBar,
                action = {
                    currentOnTitleBarVisibilityChange(!currentTitleBarVisible)
                    true
                },
            ),
        )
    }
}
