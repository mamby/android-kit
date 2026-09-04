package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.isRoot
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
import androidx.test.espresso.Espresso.pressBack
import net.mamby.androidkit.compose.action.AndroidKitAction
import net.mamby.androidkit.compose.action.AndroidKitActionSeparator
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetDefaults
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceDefaults
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitThemes
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
    fun headerActionsInvokeDirectlyAndThroughTheSharedFlyout() {
        var directActionCount by mutableIntStateOf(0)
        var overflowActionCount by mutableIntStateOf(0)
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = {},
                    actions = listOf(
                        AndroidKitAction(
                            icon = materialSymbol(R.drawable.ic_symbol_edit),
                            label = "Edit",
                            onClick = { directActionCount += 1 },
                        ),
                        AndroidKitAction(
                            icon = materialSymbol(R.drawable.ic_symbol_share),
                            label = "Share",
                            onClick = {},
                        ),
                        AndroidKitActionSeparator,
                        AndroidKitAction(
                            icon = materialSymbol(R.drawable.ic_symbol_delete),
                            label = "Delete",
                            onClick = { overflowActionCount += 1 },
                        ),
                    ),
                ) {
                    Text(SheetBody)
                }
            }
        }

        composeRule.onNodeWithContentDescription("Edit").performClick()
        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithText("Delete").performClick()

        composeRule.runOnIdle {
            assertEquals(1, directActionCount)
            assertEquals(1, overflowActionCount)
        }
        composeRule.onNodeWithText("Delete").assertDoesNotExist()
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
    fun contentManagedModeKeepsViewportBehindChromeAndItemsClear() {
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = {},
                    scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
                    header = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .testTag(SheetHeaderTag),
                        )
                    },
                ) { managedContentPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(ManagedScrollTag),
                        contentPadding = managedContentPadding,
                    ) {
                        item {
                            Text(
                                text = SheetBody,
                                modifier = Modifier.testTag(FirstSheetItemTag),
                            )
                        }
                    }
                }
            }
        }

        val headerBounds = composeRule.onNodeWithTag(SheetHeaderTag)
            .fetchSemanticsNode()
            .boundsInRoot
        val viewportBounds = composeRule.onNodeWithTag(ManagedScrollTag)
            .fetchSemanticsNode()
            .boundsInRoot
        val firstItemTop = composeRule.onNodeWithTag(FirstSheetItemTag)
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        assertEquals(headerBounds.top, viewportBounds.top, 1f)
        assertTrue(firstItemTop >= headerBounds.bottom)
    }

    @Test
    fun explicitChromeColorStillUsesSharedFloatingSurfaceOpacity() {
        val underlayColor = Color.Red
        val chromeBaseColor = Color.Blue
        var opacityLevel by mutableStateOf(
            AndroidKitFloatingSurfaceDefaults.MinimumOpacityLevel,
        )
        composeRule.setContent {
            AndroidKitTheme(
                definition = AndroidKitThemes.Light.copy(
                    floatingSurfaceOpacityLevel = opacityLevel,
                ),
            ) {
                AndroidKitBottomSheet(
                    visible = true,
                    title = SheetTitle,
                    onDismiss = {},
                    style = AndroidKitThemeTokens.bottomSheetStyle.copy(
                        chromeContainerColor = chromeBaseColor,
                    ),
                    scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
                    header = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .testTag(OpacitySheetHeaderTag),
                        )
                    },
                ) { managedContentPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(underlayColor),
                        contentPadding = managedContentPadding,
                    ) {
                        item { Box(modifier = Modifier.height(800.dp)) }
                    }
                }
            }
        }

        val translucentPixels = composeRule.onNodeWithTag(OpacitySheetHeaderTag)
            .captureToImage()
            .toPixelMap()
        val sampleX = translucentPixels.width / 2
        val sampleY = translucentPixels.height / 2
        assertColorClose(
            expected = chromeBaseColor
                .copy(alpha = MinimumRenderedFloatingSurfaceAlpha)
                .compositeOver(underlayColor),
            actual = translucentPixels[sampleX, sampleY],
        )

        composeRule.runOnIdle {
            opacityLevel = AndroidKitFloatingSurfaceDefaults.MaximumOpacityLevel
        }
        composeRule.waitForIdle()
        val opaquePixels = composeRule.onNodeWithTag(OpacitySheetHeaderTag)
            .captureToImage()
            .toPixelMap()

        assertEquals(chromeBaseColor.toArgb(), opaquePixels[sampleX, sampleY].toArgb())
    }

    @Test
    fun contentManagedViewportIsEdgeToEdgeAndLastItemClearsBottomInset() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitBottomSheet(
                        visible = true,
                        title = SheetTitle,
                        onDismiss = {},
                        maxHeightFraction = AndroidKitBottomSheetDefaults.MaximumHeightFraction,
                        scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
                        showChrome = false,
                        sheetContentPadding = PaddingValues.Zero,
                        contentBottomPadding = 0.dp,
                        dragHandle = null,
                        contentWindowInsets = WindowInsets(0, 0, 0, TestBottomInsetPx),
                    ) { managedContentPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(EdgeToEdgeViewportTag),
                            contentPadding = managedContentPadding,
                        ) {
                            items(40) { index ->
                                Text(
                                    text = "Inset item $index",
                                    modifier = Modifier
                                        .height(48.dp)
                                        .then(
                                            if (index == LastInsetItemIndex) {
                                                Modifier.testTag(LastInsetItemTag)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag(EdgeToEdgeViewportTag)
            .performScrollToNode(hasTextExactly("Inset item $LastInsetItemIndex"))

        val rootBottom = composeRule.onAllNodes(isRoot(), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .maxOf { it.boundsInRoot.bottom }
        val viewportBottom = composeRule.onNodeWithTag(EdgeToEdgeViewportTag)
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom
        val lastItemBottom = composeRule.onNodeWithTag(LastInsetItemTag)
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom

        assertEquals(rootBottom, viewportBottom, 1f)
        assertTrue(lastItemBottom <= viewportBottom - TestBottomInsetPx)
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
    fun upwardPullOnExpandedSheetSettlesAtItsExistingAnchor() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitBottomSheet(
                        visible = true,
                        title = SheetTitle,
                        onDismiss = {},
                        maxHeightFraction = AndroidKitBottomSheetDefaults.MaximumHeightFraction,
                        scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(SheetContentTag),
                        )
                    }
                }
            }
        }

        val initialTop = composeRule.onNodeWithText(SheetTitle)
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        composeRule.onNodeWithTag(SheetContentTag).performTouchInput {
            swipeUp(durationMillis = 1_000)
        }

        composeRule.waitForIdle()
        val settledTop = composeRule.onNodeWithText(SheetTitle)
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        assertEquals(initialTop, settledTop, 1f)
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
private const val SheetHeaderTag = "sheetHeader"
private const val OpacitySheetHeaderTag = "opacitySheetHeader"
private const val FirstSheetItemTag = "firstSheetItem"
private const val EdgeToEdgeViewportTag = "edgeToEdgeViewport"
private const val LastInsetItemTag = "lastInsetItem"
private const val LastInsetItemIndex = 39
private const val TestBottomInsetPx = 48
private const val MinimumRenderedFloatingSurfaceAlpha = 0.8f
private const val ColorChannelTolerance = 0.015f

private fun pressSystemBack() {
    pressBack()
}

private fun assertColorClose(
    expected: Color,
    actual: Color,
) {
    assertTrue(
        "Expected $expected, but was $actual",
        kotlin.math.abs(expected.red - actual.red) <= ColorChannelTolerance &&
            kotlin.math.abs(expected.green - actual.green) <= ColorChannelTolerance &&
            kotlin.math.abs(expected.blue - actual.blue) <= ColorChannelTolerance &&
            kotlin.math.abs(expected.alpha - actual.alpha) <= ColorChannelTolerance,
    )
}
