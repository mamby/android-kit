package net.mamby.androidkit.testing

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BottomSheetBehaviorTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun callerVisibilityControlsCompositionWithoutReportingDismissal() {
        var visible by mutableStateOf(false)
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = visible,
                    title = SheetTitle,
                    onDismiss = { dismissCount += 1 },
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithText(SheetTitle).assertDoesNotExist()
        composeRule.runOnIdle { visible = true }
        composeRule.onNodeWithText(SheetTitle).assertExists()
        composeRule.runOnIdle { visible = false }
        composeRule.waitUntil { composeRule.onAllNodes(hasTextExactly(SheetTitle)).fetchSemanticsNodes().isEmpty() }
        composeRule.runOnIdle { assertEquals(0, dismissCount) }
    }

    @Test
    fun closeReportsDismissalAfterTheSheetIsRemoved() {
        var visible by mutableStateOf(true)
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = visible,
                    title = SheetTitle,
                    onDismiss = {
                        dismissCount += 1
                        visible = false
                    },
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithContentDescription("Close").performClick()

        composeRule.waitUntil { dismissCount == 1 }
        composeRule.onNodeWithText(SheetTitle).assertDoesNotExist()
    }

    @Test
    fun disabledGesturesLockTheCloseAction() {
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = { dismissCount += 1 },
                    gesturesEnabled = false,
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithContentDescription("Close").performClick()

        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(0, dismissCount) }
        composeRule.onNodeWithText(SheetTitle).assertExists()
    }

    @Test
    fun internalAndSystemBackRouteToOnBackWithoutDismissing() {
        var backCount by mutableIntStateOf(0)
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = { dismissCount += 1 },
                    onBack = { backCount += 1 },
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithContentDescription("Back").performClick()
        pressSystemBack()

        composeRule.runOnIdle {
            assertEquals(2, backCount)
            assertEquals(0, dismissCount)
        }
        composeRule.onNodeWithText(SheetTitle).assertExists()
    }

    @Test
    fun systemBackDismissesWhenNoInternalBackDestinationExists() {
        var visible by mutableStateOf(true)
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = visible,
                    title = SheetTitle,
                    onDismiss = {
                        dismissCount += 1
                        visible = false
                    },
                ) {
                    Text(SheetBody)
                }
            }
        }

        pressSystemBack()

        composeRule.waitUntil(timeoutMillis = 5_000) { dismissCount == 1 }
        composeRule.onNodeWithText(SheetTitle).assertDoesNotExist()
    }

    @Test
    fun chromelessModeKeepsContentAndRemovesHeaderControls() {
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = {},
                    fitContent = true,
                    showChrome = false,
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithText(SheetBody).assertExists()
        composeRule.onNodeWithText(SheetTitle).assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }

    @Test
    fun verticalAndContentManagedModesExposeTheExpectedScrollOwner() {
        var contentManaged by mutableStateOf(false)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = {},
                    scrollMode = if (contentManaged) {
                        AndroidKitBottomSheetScrollMode.ContentManaged
                    } else {
                        AndroidKitBottomSheetScrollMode.VerticalScroll
                    },
                ) {
                    if (contentManaged) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(ManagedScrollTag),
                        ) {
                            items((0..40).toList()) { index ->
                                Text("Managed $index", modifier = Modifier.height(48.dp))
                            }
                        }
                    } else {
                        repeat(40) { index ->
                            Text("Automatic $index", modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }

        composeRule.onNode(hasScrollAction()).assertExists()
        composeRule.runOnIdle { contentManaged = true }
        composeRule.onNodeWithTag(ManagedScrollTag)
            .performScrollToNode(hasTextExactly("Managed 40"))
        composeRule.onNodeWithText("Managed 40").assertExists()
    }

    @Test
    fun contentPullMustEndBeforeAFreshPullCanDismissTheSheet() {
        var visible by mutableStateOf(true)
        var dismissCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = visible,
                    title = SheetTitle,
                    onDismiss = {
                        dismissCount += 1
                        visible = false
                    },
                ) {
                    repeat(40) { index ->
                        Text("Scrollable $index", modifier = Modifier.height(48.dp))
                    }
                }
            }
        }

        val scrollableContent = composeRule.onNode(hasScrollAction())
        scrollableContent.performTouchInput {
            swipeUp(
                startY = centerY + height * 0.15f,
                endY = centerY - height * 0.15f,
                durationMillis = 500,
            )
        }
        scrollableContent.performTouchInput {
            swipeDown(durationMillis = 1_000)
        }

        composeRule.waitForIdle()
        composeRule.runOnIdle { assertEquals(0, dismissCount) }
        composeRule.onNodeWithText(SheetTitle).assertExists()

        scrollableContent.performTouchInput {
            swipeDown(durationMillis = 1_000)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { dismissCount == 1 }
        composeRule.onNodeWithText(SheetTitle).assertDoesNotExist()
    }

    @Test
    fun requestedHeightIsClampedToNinetyPercentOfTheWindow() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitBottomSheet(
                        visible = true,
                        title = SheetTitle,
                        onDismiss = {},
                        maxHeightFraction = 2f,
                        scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .testTag(SheetContentTag),
                        )
                    }
                }
            }
        }

        val titleTop = composeRule.onNodeWithText(SheetTitle)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val minimumTop = 80.dp.value * composeRule.activity.resources.displayMetrics.density
        assertTrue(titleTop >= minimumTop)
        composeRule.onNodeWithTag(SheetContentTag).assertExists()
    }
}

private const val SheetTitle = "Bottom sheet"
private const val SheetBody = "Sheet body"
private const val ManagedScrollTag = "managedSheetScroll"
private const val SheetContentTag = "sheetContent"

private fun pressSystemBack() {
    InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
}
