package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.action.AndroidKitFloatingTooltip
import net.mamby.androidkit.compose.action.AndroidKitFloatingTooltipAction
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class FloatingTooltipBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private var enabled by mutableStateOf(false)
    private var actions = 0
    private var expectedPadding = 0f

    private fun content(interactive: Boolean) {
        rule.setContent {
            AndroidKitTheme {
                expectedPadding = with(LocalDensity.current) { AndroidKitThemeTokens.dimensions.spaceMedium.roundToPx().toFloat() }
                val state = rememberTooltipState(isPersistent = true)
                val scope = rememberCoroutineScope()
                TooltipBox(
                    state = state,
                    focusable = true,
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        AndroidKitFloatingTooltip(
                            text = "Tooltip message",
                            modifier = Modifier.testTag("tooltip"),
                            action = if (interactive) AndroidKitFloatingTooltipAction(
                                label = "Confirm", enabled = enabled,
                                onClick = { actions++; state.dismiss() },
                            ) else null,
                            onDismiss = if (interactive) ({ state.dismiss() }) else null,
                        )
                    },
                ) {
                    Button(onClick = { scope.launch { state.show() } }) { Text("Show") }
                }
            }
        }
        rule.onNodeWithText("Show").performClick()
    }

    @Test fun textOnlyTooltipUsesMaterialDismissalWithoutExtraControls() {
        content(interactive = false)
        assertMessagePadding()
        val surface = rule.onNodeWithTag("tooltip").fetchSemanticsNode().boundsInRoot
        val message = rule.onNodeWithText("Tooltip message").fetchSemanticsNode().boundsInRoot
        assertEquals(expectedPadding, surface.right - message.right, 1f)
        assertEquals(expectedPadding, surface.bottom - message.bottom, 1f)
        rule.onNodeWithText("Tooltip message").assertExists()
        rule.onNodeWithText("Confirm").assertDoesNotExist()
        rule.onNodeWithText("Close").assertDoesNotExist()
        pressBack()
        rule.onNodeWithText("Tooltip message").assertDoesNotExist()
    }

    @Test fun typedActionRespectsEnabledStateAndCloseOnlyDismisses() {
        content(interactive = true)
        assertMessagePadding()
        rule.onNodeWithText("Confirm").assertIsNotEnabled().performClick()
        rule.runOnIdle { assertEquals(0, actions); enabled = true }
        rule.onNodeWithText("Confirm").performClick()
        rule.onNodeWithText("Tooltip message").assertDoesNotExist()
        rule.runOnIdle { assertEquals(1, actions) }
        rule.onNodeWithText("Show").performClick()
        rule.onNodeWithText("Close").performClick()
        rule.onNodeWithText("Tooltip message").assertDoesNotExist()
        rule.runOnIdle { assertEquals(1, actions) }
    }

    private fun assertMessagePadding() {
        val surface = rule.onNodeWithTag("tooltip").fetchSemanticsNode().boundsInRoot
        val message = rule.onNodeWithText("Tooltip message").fetchSemanticsNode().boundsInRoot
        assertEquals(expectedPadding, message.left - surface.left, 1f)
        assertEquals(expectedPadding, message.top - surface.top, 1f)
    }
}
