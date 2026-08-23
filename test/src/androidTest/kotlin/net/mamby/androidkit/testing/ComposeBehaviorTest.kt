package net.mamby.androidkit.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.FloatingButton
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.navigation.AndroidKitNavigationItem
import net.mamby.androidkit.compose.navigation.FloatingNavigation
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
            Triple("home", "Home", Icons.Default.Home),
            Triple("list", "Lists", Icons.AutoMirrored.Filled.List),
            Triple("edit", "Editor", Icons.Default.Edit),
            Triple("settings", "Settings", Icons.Default.Settings),
        )
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    FloatingNavigation(
                        items = destinations.map { (key, label, icon) ->
                            AndroidKitNavigationItem(key, label, icon)
                        },
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
                    FloatingNavigation(
                        items = listOf(
                            AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
                            AndroidKitNavigationItem("settings", "Settings", Icons.Default.Settings),
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
    fun pageScaffoldIncludesMeasuredNavigationAndActionClearance() {
        var showLabels by mutableStateOf(false)
        var bottomPadding = 0.dp
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 360.dp)),
            ) {
                AndroidKitTheme {
                    FloatingNavigation(
                        items = listOf(
                            AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
                            AndroidKitNavigationItem("settings", "Settings", Icons.Default.Settings),
                        ),
                        selectedKey = "home",
                        onSelected = {},
                        showCompactLabels = showLabels,
                    ) {
                        PageScaffold(
                            title = "Clearance",
                            floatingActionButton = { FloatingButton(onClick = {}) {} },
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
    fun compactNavigationSelectsAnOverflowDestination() {
        var selected by mutableStateOf("home")
        composeRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.WindowSize(DpSize(360.dp, 800.dp)),
            ) {
                AndroidKitTheme {
                    FloatingNavigation(
                        items = listOf(
                            AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
                            AndroidKitNavigationItem("list", "Lists", Icons.AutoMirrored.Filled.List),
                            AndroidKitNavigationItem("edit", "Editor", Icons.Default.Edit),
                            AndroidKitNavigationItem("language", "Language", Icons.Default.Language),
                            AndroidKitNavigationItem("settings", "Settings", Icons.Default.Settings),
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
                    FloatingNavigation(
                            items = listOf(
                                AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
                                AndroidKitNavigationItem("list", "Lists", Icons.AutoMirrored.Filled.List),
                                AndroidKitNavigationItem("edit", "Editor", Icons.Default.Edit),
                                AndroidKitNavigationItem("language", "Language", Icons.Default.Language),
                                AndroidKitNavigationItem("settings", "Settings", Icons.Default.Settings),
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
                    FloatingNavigation(
                        items = listOf(
                            AndroidKitNavigationItem("home", "Home", Icons.Default.Home),
                            AndroidKitNavigationItem("list", "Lists", Icons.AutoMirrored.Filled.List),
                            AndroidKitNavigationItem("edit", "Editor", Icons.Default.Edit),
                            AndroidKitNavigationItem("one", "Overflow 1", Icons.Default.Language),
                            AndroidKitNavigationItem("two", "Overflow 2", Icons.Default.Language),
                            AndroidKitNavigationItem("three", "Overflow 3", Icons.Default.Language),
                            AndroidKitNavigationItem("four", "Overflow 4", Icons.Default.Language),
                            AndroidKitNavigationItem(
                                key = "settings",
                                label = "Settings",
                                icon = Icons.Default.Settings,
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
        composeRule.onNodeWithTag("androidKitNavigationFlyoutDivider").assertExists()
    }
}
