package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsPageBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hiddenSectionsLeaveNoGapAndCustomSectionsKeepTheirOrder() {
        var showEmpty by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    section("first") { button("First", {}) }
                    if (showEmpty) {
                        generalSection(label = "Hidden general")
                        securitySection(label = "Hidden security")
                        section("empty", label = "Hidden custom") {}
                    }
                    section("last") { button("Last", {}) }
                }
            }
        }
        rule.onNodeWithText("Hidden general").assertDoesNotExist()
        rule.onNodeWithText("Hidden security").assertDoesNotExist()
        rule.onNodeWithText("Hidden custom").assertDoesNotExist()
        val before = rule.onNodeWithText("Last").fetchSemanticsNode().boundsInRoot.top
        assertTrue(before > rule.onNodeWithText("First").fetchSemanticsNode().boundsInRoot.top)
        rule.runOnIdle { showEmpty = false }
        assertEquals(before, rule.onNodeWithText("Last").fetchSemanticsNode().boundsInRoot.top)
    }

    @Test
    fun languageSearchUsesHostOptionsAndStableIdsAndResetsOnReopen() {
        var selected by mutableStateOf("en")
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    generalSection(language = AndroidKitLanguageSetting(
                        selection = AndroidKitSettingsSelection(
                            label = "Language", selectedId = selected, onSelected = { selected = it },
                            options = listOf(AndroidKitSettingsOption("en", "English"), AndroidKitSettingsOption("fr", "Français")),
                            closeContentDescription = "Close picker",
                        ), searchLabel = "Search languages", emptyResultsLabel = "No results",
                    ))
                }
            }
        }
        rule.onNodeWithText("Language").performClick()
        rule.onNodeWithText("Search languages").performTextReplacement("missing")
        rule.onNodeWithText("No results").assertIsDisplayed()
        rule.onNodeWithText("Search languages").performTextReplacement("FRANCAIS")
        rule.onNodeWithText("Français").performClick()
        rule.runOnIdle { assertEquals("fr", selected) }
        rule.onNodeWithText("Language").performClick()
        rule.onNodeWithText("English").assertIsDisplayed()
        rule.onNodeWithContentDescription("Close picker").performClick()
    }

    @Test
    fun themePickerAcceptsHostDefinedChoices() {
        var selected by mutableStateOf("light")
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    generalSection(theme = AndroidKitSettingsSelection(
                        label = "Theme", selectedId = selected, onSelected = { selected = it },
                        options = listOf(AndroidKitSettingsOption("light", "Light"), AndroidKitSettingsOption("prism", "Prism")),
                        closeContentDescription = "Close picker",
                    ))
                }
            }
        }
        rule.onNodeWithText("Theme").performClick()
        rule.onNodeWithText("Prism").performClick()
        rule.runOnIdle { assertEquals("prism", selected) }
    }

    @Test
    fun opacityHasEndpointLabelsAndAppLockWaitsForHostConfirmation() {
        var level by mutableFloatStateOf(0f)
        var commits = 0
        var lockRequests = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    generalSection(floatingOpacity = AndroidKitFloatingOpacitySetting(
                        label = "Opacity", value = level, minimumLabel = "Min", maximumLabel = "Max",
                        onValueChange = { level = it }, onValueChangeFinished = { commits++ },
                    ))
                    securitySection(appLock = AndroidKitAppLockSetting(
                        label = "App lock", checked = false, onCheckedChange = { lockRequests++ },
                    ))
                }
            }
        }
        rule.onNodeWithText("Min").assertIsDisplayed()
        rule.onNodeWithText("Max").assertIsDisplayed()
        rule.onNodeWithContentDescription("Opacity").performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
        rule.runOnIdle { assertEquals(100f, level); assertEquals(1, commits) }
        rule.onNodeWithText("App lock").performClick()
        rule.onNodeWithText("App lock").performClick()
        rule.runOnIdle { assertEquals(2, lockRequests) }
    }

    @Test
    fun sliderUpdatesDoNotDuplicateTrailingPageItems() {
        var level by mutableFloatStateOf(0f)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    generalSection(floatingOpacity = AndroidKitFloatingOpacitySetting(
                        label = "Opacity", value = level, minimumLabel = "Min", maximumLabel = "Max",
                        onValueChange = { level = it }, onValueChangeFinished = {},
                    ))
                    item(key = "scroll") { Text("Scroll content") }
                }
            }
        }

        rule.onNodeWithContentDescription("Opacity")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
        rule.runOnIdle {
            assertEquals(100f, level)
        }
        val progress = rule.onNodeWithContentDescription("Opacity")
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(100f, progress.current)
        rule.onAllNodesWithText("Scroll content").assertCountEquals(1)
    }

    @Test
    fun conditionalSettingsUpdateWithoutDuplicatingTheirSection() {
        var showLanguage by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    section("visibility") {
                        toggle("Show language", showLanguage, { showLanguage = it })
                    }
                    generalSection(
                        label = "General",
                        language = if (showLanguage) AndroidKitLanguageSetting(
                            selection = AndroidKitSettingsSelection(
                                label = "Language", selectedId = "en", onSelected = {},
                                options = listOf(AndroidKitSettingsOption("en", "English")),
                                closeContentDescription = "Close",
                            ),
                            searchLabel = "Search", emptyResultsLabel = "No results",
                        ) else null,
                        theme = AndroidKitSettingsSelection(
                            label = "Theme", selectedId = "light", onSelected = {},
                            options = listOf(AndroidKitSettingsOption("light", "Light")),
                            closeContentDescription = "Close",
                        ),
                    )
                }
            }
        }

        rule.onNodeWithText("Language").assertIsDisplayed()
        rule.onNodeWithText("Show language").performClick()
        rule.runOnIdle { assertTrue(!showLanguage) }
        rule.onNodeWithText("Language").assertDoesNotExist()
        rule.onAllNodesWithText("General").assertCountEquals(1)
        rule.onNodeWithText("Theme").assertIsDisplayed()
    }

    @Test
    fun removedVisibleSectionsDoNotRemainInThePage() {
        var showSection by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage {
                    if (showSection) {
                        section("conditional", label = "Conditional") {
                            button("Conditional entry", {})
                        }
                    }
                    section("persistent", label = "Persistent") {
                        button("Persistent entry", {})
                    }
                }
            }
        }

        rule.onNodeWithText("Conditional").assertIsDisplayed()
        rule.onNodeWithText("Conditional entry").assertIsDisplayed()
        rule.runOnIdle { showSection = false }
        rule.onNodeWithText("Conditional").assertDoesNotExist()
        rule.onNodeWithText("Conditional entry").assertDoesNotExist()
        rule.onNodeWithText("Persistent").assertIsDisplayed()
    }

    @Test
    fun hostNavigationRestoresParentScrollPosition() {
        var destination by mutableStateOf("root")
        rule.setContent {
            AndroidKitTheme {
                val holder = rememberSaveableStateHolder()
                holder.SaveableStateProvider(destination) {
                    AndroidKitSettingsPage(
                        title = destination,
                        listState = rememberLazyListState(),
                        onBack = if (destination == "child") ({ destination = "root" }) else null,
                    ) {
                        if (destination == "root") {
                            repeat(30) { index -> item("item-$index") { Text("Entry $index") } }
                            section("navigation") { navigation("Open child", { destination = "child" }) }
                        } else {
                            item("child-content") { Text("Child content") }
                        }
                    }
                }
            }
        }
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Open child"))
        val before = rule.onNodeWithText("Open child").fetchSemanticsNode().boundsInRoot.top
        rule.onNodeWithText("Open child").performClick()
        rule.onNodeWithText("Child content").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("Open child").assertIsDisplayed()
        assertEquals(before, rule.onNodeWithText("Open child").fetchSemanticsNode().boundsInRoot.top)
    }
}
