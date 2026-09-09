package net.mamby.androidkit.testing

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import net.mamby.androidkit.compose.action.AndroidKitActionFlyout
import net.mamby.androidkit.compose.action.AndroidKitFloatingToolbar
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActionFlyoutBehaviorTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun standaloneActionsDismissBeforeInvokingAndDisabledItemsDoNothing() {
        var expanded by mutableStateOf(false)
        val events = mutableListOf<String>()
        composeRule.setContent {
            AndroidKitTheme {
                val deleteIcon = materialSymbol(R.drawable.ic_symbol_delete)
                Box {
                    Button(onClick = { expanded = true }) { Text("Open") }
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                            events += "dismiss"
                        },
                    ) {
                        item(label = "Share", onClick = { events += "share" })
                        separator()
                        item(
                            label = "Delete",
                            icon = deleteIcon,
                            onClick = { events += "delete" },
                            enabled = false,
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText("Share").assertDoesNotExist()
        composeRule.onNodeWithText("Open").performClick()
        composeRule.onNodeWithText("Share").assertExists()
        composeRule.onNodeWithText("Delete")
            .assertIsNotEnabled()
            .performTouchInput { click() }
        composeRule.runOnIdle { assertEquals(emptyList<String>(), events) }
        composeRule.onNodeWithText("Share").performClick()
        composeRule.runOnIdle { assertEquals(listOf("dismiss", "share"), events) }
        composeRule.onNodeWithText("Share").assertDoesNotExist()
    }

    @Test
    fun backRequestsDismissalWithoutInvokingAnAction() {
        var expanded by mutableStateOf(true)
        var actionClicked = false
        composeRule.setContent {
            AndroidKitTheme {
                Box {
                    Text("Anchor")
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        item(label = "Action", onClick = { actionClicked = true })
                    }
                }
            }
        }

        composeRule.onNodeWithText("Action").assertExists()
        pressBack()
        composeRule.onNodeWithText("Action").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(false, actionClicked) }
    }

    @Test
    fun disablingAnOpenFlyoutDismissesItAndPreventsReopening() {
        var expanded by mutableStateOf(true)
        var enabled by mutableStateOf(true)
        var dismissCount = 0
        composeRule.setContent {
            AndroidKitTheme {
                Box {
                    Text("Anchor")
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        enabled = enabled,
                        onDismissRequest = {
                            expanded = false
                            dismissCount += 1
                        },
                    ) {
                        item(label = "Action", onClick = {})
                    }
                }
            }
        }

        composeRule.onNodeWithText("Action").assertExists()
        composeRule.runOnIdle { enabled = false }
        composeRule.onNodeWithText("Action").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
        composeRule.runOnIdle { expanded = true }
        composeRule.onNodeWithText("Action").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(2, dismissCount) }
    }

    @Test
    fun longFlyoutScrollsToAnActionAndDismissesAfterSelection() {
        var expanded by mutableStateOf(true)
        var selected: Int? = null
        composeRule.setContent {
            AndroidKitTheme {
                Box {
                    Text("Anchor")
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        repeat(50) { index ->
                            item(
                                label = "Action $index",
                                onClick = { selected = index },
                                modifier = Modifier.testTag("action-$index"),
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag("action-49").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(49, selected) }
        composeRule.onNodeWithTag("action-49").assertDoesNotExist()
    }

    @Test
    fun nestedActionDismissesAllMenusBeforeInvokingAndReopensCollapsed() {
        var expanded by mutableStateOf(false)
        val events = mutableListOf<String>()
        composeRule.setContent {
            AndroidKitTheme {
                Box {
                    Button(onClick = { expanded = true }) { Text("Open") }
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                            events += "dismiss"
                        },
                    ) {
                        submenu(label = "Export") {
                            submenu(label = "Format") {
                                item(label = "Text file", onClick = { events += "export" })
                            }
                        }
                    }
                }
            }
        }
        composeRule.onNodeWithText("Open").performClick()
        composeRule.onNodeWithText("Export").performClick()
        composeRule.onNodeWithText("Format").performClick()
        composeRule.onNodeWithText("Text file").performClick()
        composeRule.runOnIdle { assertEquals(listOf("dismiss", "export"), events) }
        composeRule.onNodeWithText("Export").assertDoesNotExist()
        composeRule.onNodeWithText("Text file").assertDoesNotExist()
        composeRule.onNodeWithText("Open").performClick()
        composeRule.onNodeWithText("Export").assertExists()
        composeRule.onNodeWithText("Format").assertDoesNotExist()
    }

    @Test
    fun backClosesOnlySubmenuAndDisablingParentClosesDescendants() {
        var expanded by mutableStateOf(true)
        var enabled by mutableStateOf(true)
        var dismissCount = 0
        composeRule.setContent {
            AndroidKitTheme {
                Box {
                    Text("Anchor")
                    AndroidKitActionFlyout(
                        expanded = expanded,
                        enabled = enabled,
                        onDismissRequest = { expanded = false; dismissCount++ },
                    ) {
                        submenu(label = "Export") {
                            item(label = "Text file", onClick = {})
                        }
                        submenu(label = "Unavailable", enabled = false) {
                            item(label = "Hidden action", onClick = {})
                        }
                    }
                }
            }
        }
        composeRule.onNodeWithText("Unavailable").assertIsNotEnabled()
        composeRule.onNodeWithText("Export").performClick()
        composeRule.onNodeWithText("Text file").assertExists()
        pressBack()
        composeRule.onNodeWithText("Text file").assertDoesNotExist()
        composeRule.onNodeWithText("Export").assertExists()
        composeRule.runOnIdle { assertEquals(0, dismissCount) }
        composeRule.onNodeWithText("Export").performClick()
        composeRule.runOnIdle { enabled = false }
        composeRule.onNodeWithText("Text file").assertDoesNotExist()
        composeRule.onNodeWithText("Export").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test
    fun toolbarFlyoutUsesTheSameActionDismissalBehavior() {
        var actionCount = 0
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitFloatingToolbar {
                    flyout {
                        item(label = "Share", onClick = { actionCount += 1 })
                        separator()
                        item(label = "Cancel", onClick = {})
                    }
                }
            }
        }

        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithText("Share").performClick()
        composeRule.runOnIdle { assertEquals(1, actionCount) }
        composeRule.onNodeWithText("Cancel").assertDoesNotExist()
    }
}
