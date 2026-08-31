package net.mamby.androidkit.testing

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.layout.AndroidKitPageAction
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSurfaceStyle
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class ComposeBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactNavigationCentersThreeDestinationsWithoutMore() {
        var selected by mutableStateOf("home")
        val destinations = listOf(
            "home" to "Home",
            "list" to "Lists",
            "edit" to "Editor",
        )
        composeRule.setContent {
            val navigationItems = listOf(
                AndroidKitFloatingNavigationItem(
                    "home",
                    "Home",
                    materialSymbol(R.drawable.ic_symbol_home),
                ),
                AndroidKitFloatingNavigationItem(
                    "list",
                    "Lists",
                    materialSymbol(R.drawable.ic_symbol_list),
                ),
                AndroidKitFloatingNavigationItem(
                    "edit",
                    "Editor",
                    materialSymbol(R.drawable.ic_symbol_edit),
                ),
            )
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = navigationItems,
                        selectedKey = selected,
                        onSelected = { selected = it },
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .testTag(NavigationContentTestTag),
                        )
                    }
                }
            }
        }

        destinations.forEach { (key, label) ->
            composeRule
                .onNodeWithContentDescription(label, useUnmergedTree = true)
                .assertExists()
                .performClick()
            composeRule.runOnIdle { assertEquals(key, selected) }
        }
        composeRule.onNodeWithContentDescription("More").assertDoesNotExist()
        composeRule.onNodeWithText("Home", useUnmergedTree = true).assertDoesNotExist()

        val navigationBounds = composeRule
            .onNodeWithTag(FloatingNavigationBarTestTag)
            .fetchSemanticsNode()
            .boundsInRoot
        val contentBounds = composeRule
            .onNodeWithTag(NavigationContentTestTag)
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(navigationBounds.width < contentBounds.width)
        assertTrue(abs(navigationBounds.center.x - contentBounds.center.x) <= 1f)
    }

    @Test
    fun compactNavigationCompositesBackgroundOpacityOnceAndOpaqueSurfaceIsExact() {
        val underlayColor = Color.Red
        val selectedColor = AndroidKitThemes.Light.colorScheme.secondaryContainer
        var opacity by mutableStateOf(ProductionTransparentSurfaceOpacity)
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme(
                    definition = AndroidKitThemes.Light,
                    floatingSurfaceStyle = AndroidKitFloatingSurfaceStyle(opacity),
                ) {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "list",
                                "Lists",
                                materialSymbol(R.drawable.ic_symbol_list),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "edit",
                                "Editor",
                                materialSymbol(R.drawable.ic_symbol_edit),
                            ),
                        ),
                        selectedKey = "home",
                        onSelected = {},
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(underlayColor),
                        )
                    }
                }
            }
        }

        val transparentPixels = composeRule
            .onNodeWithTag(FloatingNavigationBarTestTag)
            .captureToImage()
            .toPixelMap()
        val sampleY = transparentPixels.height / 2
        val selectedSampleX = transparentPixels.width / 12
        val unselectedSampleX = transparentPixels.width * 5 / 12
        assertColorClose(
            expected = selectedColor
                .copy(alpha = ProductionTransparentSurfaceOpacity)
                .compositeOver(underlayColor),
            actual = transparentPixels[selectedSampleX, sampleY],
        )
        assertColorClose(
            expected = Color.White
                .copy(alpha = ProductionTransparentSurfaceOpacity)
                .compositeOver(underlayColor),
            actual = transparentPixels[unselectedSampleX, sampleY],
        )

        composeRule.runOnIdle { opacity = 1f }
        composeRule.waitForIdle()
        val opaquePixels = composeRule
            .onNodeWithTag(FloatingNavigationBarTestTag)
            .captureToImage()
            .toPixelMap()
        assertEquals(
            selectedColor.toArgb(),
            opaquePixels[selectedSampleX, sampleY].toArgb(),
        )
        assertEquals(
            Color.White.toArgb(),
            opaquePixels[unselectedSampleX, sampleY].toArgb(),
        )
    }

    @Test
    fun compactNavigationLabelsRemainVisibleAfterSelection() {
        var showLabels by mutableStateOf(false)
        var selected by mutableStateOf("home")
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "settings",
                                "Settings",
                                materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = selected,
                        onSelected = { selected = it },
                        showCompactLabels = showLabels,
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        val iconOnlyHeight = composeRule
            .onNodeWithTag(FloatingNavigationBarTestTag)
            .fetchSemanticsNode()
            .boundsInRoot
            .height
        composeRule.runOnIdle { showLabels = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Home", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Settings", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        composeRule.runOnIdle { assertEquals("settings", selected) }
        composeRule.onNodeWithText("Home", useUnmergedTree = true).assertExists()
        val labeledHeight = composeRule
            .onNodeWithTag(FloatingNavigationBarTestTag)
            .fetchSemanticsNode()
            .boundsInRoot
            .height
        assertTrue(abs(labeledHeight - iconOnlyHeight) <= 1f)
    }

    @Test
    fun androidKitPageIncludesMeasuredNavigationAndActionClearance() {
        var showLabels by mutableStateOf(false)
        var bottomPadding = 0.dp
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 360.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "settings",
                                "Settings",
                                materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = "home",
                        onSelected = {},
                        showCompactLabels = showLabels,
                    ) {
                        AndroidKitPage(
                            title = "Clearance",
                            floatingActionButton = {
                                AndroidKitFloatingActionButton(onClick = {}) {}
                            },
                        ) { contentPadding ->
                            bottomPadding = contentPadding.calculateBottomPadding()
                            Box(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        composeRule.waitUntil { bottomPadding > 100.dp }
        val iconOnlyBottomPadding = bottomPadding
        composeRule.runOnIdle { showLabels = true }
        composeRule.waitForIdle()

        assertTrue(iconOnlyBottomPadding > 100.dp)
        assertTrue(bottomPadding > 100.dp)
    }

    @Test
    fun immersiveTitleBarTogglesWhenContentConsumesOnlyTheInitialDown() {
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitPage(
                    title = "Immersive title",
                    titleBarImmersiveMode = true,
                ) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .testTag(ImmersiveContentTestTag)
                            .consumeInitialDownForTest(),
                    )
                }
            }
        }

        composeRule.onNodeWithTag(ImmersiveContentTestTag).performTouchInput { click() }
        composeRule.onNodeWithContentDescription("Immersive title").assertDoesNotExist()

        composeRule.onNodeWithTag(ImmersiveContentTestTag).performTouchInput { click() }
        composeRule.onNodeWithContentDescription("Immersive title").assertExists()
    }

    @Test
    fun immersiveTitleBarRemainsVisibleWhenAnActionIsTapped() {
        var actionCount = 0
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitPage(
                    title = "Immersive title",
                    actions = listOf(
                        AndroidKitPageAction(
                            icon = materialSymbol(R.drawable.ic_symbol_edit),
                            label = "Edit",
                            onClick = { actionCount += 1 },
                        ),
                    ),
                    titleBarImmersiveMode = true,
                ) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                    )
                }
            }
        }

        composeRule
            .onNodeWithContentDescription("Edit", useUnmergedTree = true)
            .performTouchInput { click() }

        composeRule.runOnIdle { assertEquals(1, actionCount) }
        composeRule.onNodeWithContentDescription("Immersive title").assertExists()
    }

    @Test
    fun immersiveTitleBarRemainsVisibleWhileContentScrolls() {
        composeRule.setContent {
            AndroidKitTheme {
                AndroidKitPage(
                    title = "Immersive title",
                    titleBarImmersiveMode = true,
                ) { contentPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(ImmersiveContentTestTag),
                        contentPadding = contentPadding,
                    ) {
                        items(50) { index ->
                            Text(
                                text = "Item $index",
                                modifier = Modifier.padding(24.dp),
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag(ImmersiveContentTestTag).performTouchInput { swipeUp() }

        composeRule.onNodeWithContentDescription("Immersive title").assertExists()
    }

    @Test
    fun compactNavigationSelectsAnOverflowDestination() {
        var selected by mutableStateOf("home")
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "list",
                                "Lists",
                                materialSymbol(R.drawable.ic_symbol_list),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "edit",
                                "Editor",
                                materialSymbol(R.drawable.ic_symbol_edit),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "language",
                                "Language",
                                materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "settings",
                                "Settings",
                                materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = selected,
                        onSelected = { selected = it },
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithText("Settings").performClick()

        composeRule.runOnIdle { assertEquals("settings", selected) }
        composeRule.onNodeWithText("Language").assertDoesNotExist()
    }

    @Test
    fun compactNavigationMoreOpensAChromelessBottomSheet() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "list",
                                "Lists",
                                materialSymbol(R.drawable.ic_symbol_list),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "edit",
                                "Editor",
                                materialSymbol(R.drawable.ic_symbol_edit),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "language",
                                "Language",
                                materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "settings",
                                "Settings",
                                materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = "settings",
                        onSelected = {},
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .performClick()

        composeRule.onNodeWithText("Language").assertExists()
        composeRule.onNodeWithText("Settings").assertIsSelected()
        composeRule.onNodeWithContentDescription("More").assertIsSelected()
        composeRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    }

    @Test
    fun compactNavigationMovesDestinationsThatDoNotFitIntoBottomSheet() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(220.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home destination",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "list",
                                "Lists destination",
                                materialSymbol(R.drawable.ic_symbol_list),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "edit",
                                "Editor destination",
                                materialSymbol(R.drawable.ic_symbol_edit),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "settings",
                                "Settings destination",
                                materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = "home",
                        onSelected = {},
                        compactVisibleDestinationCount = 4,
                        showCompactLabels = true,
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule.onNodeWithText("More", useUnmergedTree = true).performClick()
        composeRule.onNodeWithText("Settings destination").assertExists()
    }

    @Test
    fun compactNavigationBottomSheetKeepsNaturalOrderAndScrolls() {
        var selected by mutableStateOf("home")
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 360.dp)),
            ) {
                AndroidKitTheme {
                    AndroidKitFloatingNavigation(
                        items = listOf(
                            AndroidKitFloatingNavigationItem(
                                "home",
                                "Home",
                                materialSymbol(R.drawable.ic_symbol_home),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "list",
                                "Lists",
                                materialSymbol(R.drawable.ic_symbol_list),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "edit",
                                "Editor",
                                materialSymbol(R.drawable.ic_symbol_edit),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "one",
                                "Overflow 1",
                                materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "two",
                                "Overflow 2",
                                materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                key = "three",
                                label = "Overflow 3",
                                icon = materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                "four",
                                "Overflow 4",
                                materialSymbol(R.drawable.ic_symbol_language),
                            ),
                            AndroidKitFloatingNavigationItem(
                                key = "settings",
                                label = "Settings",
                                icon = materialSymbol(R.drawable.ic_symbol_settings),
                            ),
                        ),
                        selectedKey = selected,
                        onSelected = { selected = it },
                        compactVisibleDestinationCount = 3,
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .performClick()

        val settingsBounds = composeRule.onNodeWithText("Settings").fetchSemanticsNode().boundsInRoot
        val firstOverflowBounds = composeRule.onNodeWithText("Overflow 1").fetchSemanticsNode().boundsInRoot
        assertTrue(firstOverflowBounds.top < settingsBounds.top)
        composeRule.onNode(hasScrollAction()).assertExists()
    }
}

private fun Modifier.consumeInitialDownForTest(): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false).consume()
        while (awaitPointerEvent().changes.any { it.pressed }) {
            // Leave the release unconsumed, matching a scrollable that only stops an active fling.
        }
    }
}

private fun assertColorClose(
    expected: Color,
    actual: Color,
) {
    assertTrue(
        "Expected $expected, but was $actual",
        abs(expected.red - actual.red) <= ColorChannelTolerance &&
            abs(expected.green - actual.green) <= ColorChannelTolerance &&
            abs(expected.blue - actual.blue) <= ColorChannelTolerance &&
            abs(expected.alpha - actual.alpha) <= ColorChannelTolerance,
    )
}

private const val ImmersiveContentTestTag = "immersiveContent"
private const val NavigationContentTestTag = "navigationContent"
private const val FloatingNavigationBarTestTag = "androidKitFloatingNavigationBar"
private const val ProductionTransparentSurfaceOpacity = 0.92f
private const val ColorChannelTolerance = 0.015f
