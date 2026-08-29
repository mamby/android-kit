package net.mamby.androidkit.testing

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionButton
import net.mamby.androidkit.compose.layout.AndroidKitFloatingTitleBarAction
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigation
import net.mamby.androidkit.compose.navigation.AndroidKitFloatingNavigationItem
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class ComposeBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactNavigationShowsAndSelectsEveryPrimaryDestination() {
        var selected by mutableStateOf("home")
        val destinations = listOf(
            "home" to "Home",
            "list" to "Lists",
            "edit" to "Editor",
            "settings" to "Settings",
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
                AndroidKitFloatingNavigationItem(
                    "settings",
                    "Settings",
                    materialSymbol(R.drawable.ic_symbol_settings),
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
                        Box(Modifier.fillMaxSize())
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
    }

    @Test
    fun compactNavigationCanShowLabels() {
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
                        selectedKey = "home",
                        onSelected = {},
                        showCompactLabels = true,
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }

        composeRule.onNodeWithText("Home", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Settings", useUnmergedTree = true).assertExists()
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
                        AndroidKitFloatingTitleBarAction(
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
    fun compactNavigationFlyoutUsesRtlAwareAnchoringAndKeepsLabels() {
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                            selectedKey = "home",
                            onSelected = {},
                        ) {
                            Box(Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }

        composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .performClick()

        val settingsCenterX = composeRule.onNodeWithText("Settings").fetchSemanticsNode().boundsInRoot.center.x
        val moreCenterX = composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .center.x
        val homeCenterX = composeRule
            .onNodeWithContentDescription("Home", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
            .center.x

        assertTrue(abs(settingsCenterX - moreCenterX) < abs(settingsCenterX - homeCenterX))
    }

    @Test
    fun compactNavigationFlyoutReversesItemsAndScrollsAboveTheBar() {
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
                                "three",
                                "Overflow 3",
                                materialSymbol(R.drawable.ic_symbol_language),
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
                                showDividerAfterInFlyout = true,
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
        val moreBounds = composeRule
            .onNodeWithContentDescription("More", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val popupBounds = composeRule.onNode(isPopup()).fetchSemanticsNode().boundsInRoot

        assertTrue(settingsBounds.top < firstOverflowBounds.top)
        assertTrue(popupBounds.bottom <= moreBounds.top)
        composeRule.onNode(hasScrollAction()).assertExists()
        composeRule.onNodeWithTag("androidKitFloatingNavigationFlyoutDivider").assertExists()
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

private const val ImmersiveContentTestTag = "immersiveContent"
