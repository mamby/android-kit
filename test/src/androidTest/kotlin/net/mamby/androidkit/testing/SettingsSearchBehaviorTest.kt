package net.mamby.androidkit.testing

import android.content.ClipboardManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsLink
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchTerms
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.compose.form.androidKitSettingsCatalog
import net.mamby.androidkit.compose.form.normalizeSettingsSearchText
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsSearchBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun everyCatalogPageHasTheSearchAction() {
        var page by mutableStateOf("main")
        var searchRequests = 0
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({ searchRequests++ }, emptyList(), {}),
                ) {
                    main("main", "Settings")
                    subpage("sub", "Subpage")
                    about("about", AndroidKitSettingsAbout("App", "1"), {})
                }
                AndroidKitSettingsPage(catalog, page)
            }
        }
        listOf("main", "sub", "about").forEachIndexed { index, key ->
            rule.runOnIdle { page = key }
            rule.onNodeWithContentDescription("Search settings").performClick()
            rule.runOnIdle { assertEquals(index + 1, searchRequests) }
        }
    }

    @Test
    fun globalSearchMatchesOtherLanguagesAndRunsControlsInPlace() {
        var enabled by mutableStateOf(false)
        var buttonClicks = 0
        var contactClicks = 0
        var recents by mutableStateOf(emptyList<String>())
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({}, recents, { recents = it }),
                ) {
                    main("main", "Settings") {
                        section("general", "General") {
                            toggle(
                                key = "sync",
                                label = "Sync",
                                checked = enabled,
                                onCheckedChange = { enabled = it },
                            )
                        }
                    }
                    subpage("backup", "Backup") {
                        section("actions", "Actions") {
                            button(
                                key = "backup-now",
                                label = "Back up now",
                                onClick = { buttonClicks++ },
                                searchTerms = AndroidKitSettingsSearchTerms(
                                    mapOf("fr" to listOf("sauvegarder")),
                                ),
                            )
                        }
                    }
                    about(
                        "about",
                        AndroidKitSettingsAbout(
                            "App",
                            "1",
                            contact = AndroidKitSettingsLink(onClick = { contactClicks++ }),
                        ),
                        {},
                    )
                }
                AndroidKitSettingsSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("hilfe")
        rule.onNodeWithText("Contact").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, contactClicks) }

        search.performTextReplacement("sauvegarder")
        rule.onNodeWithText("Back up now").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, buttonClicks) }

        search.performTextReplacement("sync")
        rule.onNodeWithText("Sync").assertIsDisplayed().performClick()
        rule.runOnIdle {
            assertTrue(enabled)
            assertEquals("sync", recents.first())
        }
    }

    @Test
    fun recentQueriesCanBeRemovedIndividuallyOrTogether() {
        var recents by mutableStateOf(listOf("Theme", "Privacy"))
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({}, recents, { recents = it }),
                ) { main("main", "Settings") }
                AndroidKitSettingsSearchPage(catalog)
            }
        }
        rule.onNodeWithText("Theme").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Remove recent search")[0].performClick()
        rule.runOnIdle { assertEquals(listOf("Privacy"), recents) }
        rule.onNodeWithText("Clear all").performClick()
        rule.runOnIdle { assertTrue(recents.isEmpty()) }
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
    }

    @Test
    fun recentQueriesAreSubmittedRestoredDeduplicatedAndCapped() {
        var recents by mutableStateOf((0 until 10).map { "Old $it" })
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({}, recents, { recents = it }),
                ) {
                    main("main", "Settings") {
                        section("actions") { button("alpha", "Alpha", {}) }
                    }
                }
                AndroidKitSettingsSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("alpha")
        rule.onNodeWithText("Alpha").performClick()
        search.performTextReplacement("ÁLPHA")
        rule.onNodeWithText("Alpha").performClick()
        rule.runOnIdle {
            assertEquals(10, recents.size)
            assertEquals("ÁLPHA", recents.first())
            assertEquals(1, recents.count { normalizeSettingsSearchText(it) == "alpha" })
        }

        search.performTextReplacement("nothing here")
        search.performImeAction()
        rule.onNodeWithText("No matching settings").assertIsDisplayed()
        rule.runOnIdle { assertEquals("nothing here", recents.first()) }

        search.performTextReplacement("")
        val beforeRestore = recents
        rule.onNodeWithText("nothing here").performClick()
        search.assertTextEquals("nothing here")
        rule.runOnIdle { assertEquals(beforeRestore, recents) }
    }

    @Test
    fun pickerSliderDisabledInfoAndSearchOptOutKeepTheirNativeBehavior() {
        var selectedTheme by mutableStateOf("system")
        var opacity by mutableFloatStateOf(40f)
        var opacityCommits = 0
        var showConditional by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({}, emptyList(), {}),
                ) {
                    main("main", "Settings") {
                        section("general", "General") {
                            theme(
                                AndroidKitSettingsSelection(
                                    options = listOf(
                                        AndroidKitSettingsOption(
                                            "dark",
                                            "Dark",
                                            AndroidKitSettingsSearchTerms(mapOf("fr" to listOf("noir"))),
                                        ),
                                    ),
                                    selectedId = selectedTheme,
                                    onSelected = { selectedTheme = it },
                                    systemOption = AndroidKitSettingsSystemOption("system", "Light"),
                                ),
                            )
                            transparency(
                                AndroidKitFloatingOpacitySetting(
                                    value = opacity,
                                    onValueChange = { opacity = it },
                                    onValueChangeFinished = { opacityCommits++ },
                                ),
                            )
                            button("disabled", "Disabled action", {}, enabled = false)
                            info("information", "Information", "Read only")
                            button("hidden", "Hidden action", {}, searchable = false)
                            if (showConditional) button("conditional", "Conditional action", {})
                        }
                    }
                }
                AndroidKitSettingsSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("noir")
        rule.onNodeWithText("Theme").performClick()
        rule.onNodeWithText("Dark").performClick()
        rule.runOnIdle { assertEquals("dark", selectedTheme) }

        search.performTextReplacement("opacity")
        rule.onNodeWithContentDescription("Transparency")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(75f) }
        rule.runOnIdle {
            assertEquals(75f, opacity)
            assertEquals(1, opacityCommits)
        }

        search.performTextReplacement("disabled")
        rule.onNodeWithText("Disabled action").assertIsNotEnabled()
        search.performTextReplacement("information")
        val information = rule.onNodeWithText("Information").fetchSemanticsNode()
        assertTrue(SemanticsActions.OnClick !in information.config)
        search.performTextReplacement("hidden")
        rule.onNodeWithText("Hidden action").assertDoesNotExist()
        rule.onNodeWithText("No matching settings").assertIsDisplayed()

        search.performTextReplacement("conditional")
        rule.onNodeWithText("Conditional action").assertIsDisplayed()
        rule.runOnIdle { showConditional = false }
        rule.onNodeWithText("Conditional action").assertDoesNotExist()
        rule.onNodeWithText("No matching settings").assertIsDisplayed()
    }

    @Test
    fun versionResultCopiesDirectlyWithoutOpeningAbout() {
        rule.setContent {
            AndroidKitTheme {
                val catalog = androidKitSettingsCatalog(
                    AndroidKitSettingsSearchConfiguration({}, emptyList(), {}),
                ) {
                    main("main", "Settings")
                    about("about", AndroidKitSettingsAbout("App", "1.2.3"), {})
                }
                AndroidKitSettingsSearchPage(catalog)
            }
        }

        rule.onNodeWithContentDescription("Search").performTextReplacement("release")
        rule.onNodeWithText("Version").performClick()
        rule.runOnIdle {
            val clipboard = rule.activity.getSystemService(ClipboardManager::class.java)
            assertEquals("1.2.3", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        }
    }

    @Test
    fun searchPageRetainsSemanticsWithRtlAndLargeText() {
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(320.dp, 640.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(1.5f)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AndroidKitTheme {
                            val catalog = androidKitSettingsCatalog(
                                AndroidKitSettingsSearchConfiguration({}, listOf("Theme"), {}),
                            ) { main("main", "Settings") }
                            AndroidKitSettingsSearchPage(catalog)
                        }
                    }
                }
            }
        }

        rule.onNodeWithContentDescription("Search").assertIsDisplayed()
        rule.onNodeWithText("Recent searches").assertIsDisplayed()
        rule.onNodeWithText("Theme").assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Search").assertTextEquals("Theme")
    }

    @Test
    fun catalogRejectsDuplicatePageKeys() {
        assertThrows(IllegalArgumentException::class.java) {
            androidKitSettingsCatalog(AndroidKitSettingsSearchConfiguration({}, emptyList(), {})) {
                main("same", "Settings")
                subpage("same", "Other")
            }
        }
    }
}
