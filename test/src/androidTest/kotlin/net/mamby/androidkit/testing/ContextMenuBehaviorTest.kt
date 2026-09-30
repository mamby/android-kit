package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.MouseButton
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import net.mamby.androidkit.compose.action.AndroidKitContextMenu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContextMenuBehaviorTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun longPressTracksTheInvocationPointAndSelectionPreservesPrimaryClick() {
        var clicks = 0
        var actions = 0
        rule.setContent {
            TestKitTheme {
                AndroidKitContextMenu(
                    modifier = Modifier.testTag("target"),
                    onClick = { clicks += 1 },
                    menu = { item(label = "Edit", onClick = { actions += 1 }) },
                ) { Box(Modifier.size(240.dp, 160.dp)) }
            }
        }
        val target = rule.onNodeWithTag("target")
        target.performTouchInput { longClick(Offset(width * .25f, height * .25f)) }
        val first = rule.onNodeWithText("Edit").fetchSemanticsNode().layoutInfo.coordinates.positionOnScreen()
        rule.runOnIdle { assertEquals(0, clicks) }
        pressBack()
        rule.onNodeWithText("Edit").assertDoesNotExist()
        target.performTouchInput { longClick(Offset(width * .5f, height * .5f)) }
        val second = rule.onNodeWithText("Edit").fetchSemanticsNode().layoutInfo.coordinates.positionOnScreen()
        val bounds = target.fetchSemanticsNode().boundsInWindow
        assertEquals(bounds.width * .25f, second.x - first.x, 1f)
        assertEquals(bounds.height * .25f, second.y - first.y, 1f)
        rule.onNodeWithText("Edit").performClick()
        rule.onNodeWithText("Edit").assertDoesNotExist()
        target.performClick()
        rule.runOnIdle {
            assertEquals(1, actions)
            assertEquals(1, clicks)
        }
    }

    @Test
    fun secondaryClickAccessibilityKeyboardAndDisableUseTheSameMenu() {
        var enabled by mutableStateOf(true)
        var clicks = 0
        lateinit var inputModeManager: InputModeManager
        rule.setContent {
            TestKitTheme {
                inputModeManager = LocalInputModeManager.current
                AndroidKitContextMenu(
                    enabled = enabled,
                    modifier = Modifier.testTag("target"),
                    onClick = { clicks += 1 },
                    menu = { item(label = "Edit", onClick = {}) },
                ) { Text("Record") }
            }
        }
        val target = rule.onNodeWithTag("target")
        target.performMouseInput { click(button = MouseButton.Secondary) }
        rule.onNodeWithText("Edit").assertExists()
        pressBack()
        target.performSemanticsAction(SemanticsActions.OnLongClick) { it() }
        rule.onNodeWithText("Edit").assertExists()
        pressBack()
        rule.waitUntil { rule.activity.hasWindowFocus() }
        rule.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        try {
            target.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            target.assertIsFocused()
            target.performKeyInput { pressKey(Key.Menu) }
            rule.onNodeWithText("Edit").assertExists()
            pressBack()
            rule.waitUntil { rule.activity.hasWindowFocus() }
            target.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            target.performKeyInput {
                keyDown(Key.ShiftLeft)
                pressKey(Key.F10)
                keyUp(Key.ShiftLeft)
            }
            rule.onNodeWithText("Edit").assertExists()
            rule.runOnIdle { enabled = false }
            rule.onNodeWithText("Edit").assertDoesNotExist()
        } finally {
            rule.runOnIdle { inputModeManager.requestInputMode(InputMode.Touch) }
        }
        target.performTouchInput { longClick() }
        rule.onNodeWithText("Edit").assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, clicks) }
    }

    @Test
    fun contentWithoutAPrimaryActionSupportsLongPressAndAccessibility() {
        rule.setContent {
            TestKitTheme {
                AndroidKitContextMenu(
                    modifier = Modifier.testTag("target"),
                    menu = { item(label = "Edit", onClick = {}) },
                ) { Text("Record") }
            }
        }
        val target = rule.onNodeWithTag("target")
        target.performTouchInput { click() }
        rule.onNodeWithText("Edit").assertDoesNotExist()
        target.performTouchInput { longClick() }
        rule.onNodeWithText("Edit").performClick()
        target.performSemanticsAction(SemanticsActions.OnLongClick) { it() }
        rule.onNodeWithText("Edit").assertExists()
    }
}
