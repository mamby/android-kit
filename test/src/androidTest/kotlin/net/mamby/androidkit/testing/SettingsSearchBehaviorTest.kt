package net.mamby.androidkit.testing

import android.content.ClipboardManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicBoolean
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.compose.form.AndroidKitSettingsLink
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsSearchTerms
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsSearchBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @org.junit.Before fun synchronizePersistentStorage() {
        SettingsPersistenceIdlingResource.reset()
        rule.registerIdlingResource(SettingsPersistenceIdlingResource)
    }

    @Before
    fun keepTestActivityScreenOn() {
        rule.activityRule.scenario.onActivity {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun everyCatalogPageHasTheSearchAction() {
        var page by mutableStateOf("main")
        var searchRequests = 0
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({ searchRequests++ }, emptyList(), {}),
                ) {
                    main("main", "Settings")
                    subpage("sub", "Subpage")
                    about("about", AndroidKitSettingsAbout("App", "1"), {})
                }
                TestCatalogPage(catalog, page)
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
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({}, recents, { recents = it }),
                ) {
                    main("main", "Settings") {
                        section("general", "General") {
                            toggle(
                                key = "sync",
                                label = "Sync",
                                persistence = testSetting("settingssearchbehaviortest-1", enabled),
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
                TestCatalogSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.assertIsFocused()
        search.performTextReplacement("hilfe")
        rule.onNodeWithText("Contact").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, contactClicks) }

        search.performTextReplacement("sauvegarder")
        rule.onNodeWithText("Back up now").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, buttonClicks) }

        search.performTextReplacement("sync")
        rule.onNodeWithText("Sync").assertIsDisplayed().performClick()
        rule.waitUntil { enabled && recents.firstOrNull() == "sync" }
        rule.runOnIdle {
            assertTrue(enabled)
            assertEquals("sync", recents.first())
        }
    }

    @Test
    fun recentQueriesCanBeRemovedIndividuallyOrTogether() {
        var recents by mutableStateOf(listOf("Theme", "Privacy"))
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({}, recents, { recents = it }),
                ) { main("main", "Settings") }
                TestCatalogSearchPage(catalog)
            }
        }
        rule.onNodeWithText("Theme").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Remove recent search")[0].performClick()
        rule.waitUntil { recents == listOf("Privacy") }
        rule.onNodeWithText("Clear all").performClick()
        rule.waitUntil { recents.isEmpty() }
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
    }

    @Test
    fun disabledCatalogHistoryDoesNotRecordOrBlockResultActions() {
        var visible by mutableStateOf(false)
        var recents by mutableStateOf(listOf("Sensitive setting"))
        var clicks = 0
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration(
                        onOpenSearch = {},
                        recentQueries = recents,
                        onRecentQueriesChange = { recents = it },
                        searchHistoryEnabled = visible,
                        onSearchHistoryEnabledChange = { visible = it },
                    ),
                ) {
                    main("main", "Settings") {
                        section("actions") {
                            button("backup", "Back up now", { clicks++ })
                        }
                    }
                }
                TestCatalogSearchPage(catalog)
            }
        }
        rule.onNodeWithText("Sensitive setting").assertDoesNotExist()
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("back up")
        rule.onNodeWithText("Back up now").performClick()
        rule.waitUntil { recents.isEmpty() }
        rule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(emptyList<String>(), recents)
        }
        search.performTextReplacement("")
        rule.onNodeWithText("Sensitive setting").assertDoesNotExist()
        rule.onNodeWithContentDescription("Enable search history").performClick()
        rule.waitUntil { visible }
        rule.onNodeWithText("Sensitive setting").assertDoesNotExist()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        rule.onNodeWithContentDescription("Disable search history").performClick()
        rule.waitUntil { !visible }
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
    }

    @Test
    fun catalogActionsAndSubmissionUpdateHostHistory() {
        var recents by mutableStateOf(listOf("Earlier"))
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({}, recents, { recents = it }),
                ) {
                    main("main", "Settings") {
                        section("actions") { button("alpha", "Alpha", {}) }
                    }
                }
                TestCatalogSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("alpha")
        rule.onNodeWithText("Alpha").performClick()
        rule.waitUntil { recents == listOf("alpha", "Earlier") }
        rule.runOnIdle {
            assertEquals(listOf("alpha", "Earlier"), recents)
        }

        search.performTextReplacement("nothing here")
        search.performImeAction()
        rule.onNodeWithText("No matching settings").assertIsDisplayed()
        rule.waitUntil { recents == listOf("nothing here", "alpha", "Earlier") }
        rule.runOnIdle { assertEquals(listOf("nothing here", "alpha", "Earlier"), recents) }
    }

    @Test
    fun pickerSliderDisabledInfoAndSearchOptOutKeepTheirNativeBehavior() {
        var selectedTheme by mutableStateOf("system")
        var opacity by mutableFloatStateOf(40f)
        var opacityCommits = 0
        var showConditional by mutableStateOf(true)
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({}, emptyList(), {}),
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
                                    persistence = testSetting("settingssearchbehaviortest-2", selectedTheme),
                                    onSelected = { selectedTheme = it },
                                    systemOption = AndroidKitSettingsSystemOption("system", "Light"),
                                ),
                            )
                            transparency(
                                AndroidKitFloatingOpacitySetting(
                                    persistence = testSetting("settingssearchbehaviortest-3", opacity),
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
                TestCatalogSearchPage(catalog)
            }
        }

        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("noir")
        rule.onNodeWithText("Theme").performClick()
        rule.onNodeWithText("Dark").performClick()
        rule.waitUntil { selectedTheme == "dark" }
        rule.runOnIdle { assertEquals("dark", selectedTheme) }

        search.performTextReplacement("opacity")
        rule.onNodeWithContentDescription("Transparency")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(75f) }
        rule.waitUntil { opacity == 75f && opacityCommits == 1 }
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
        var aboutRequests = 0
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration({}, emptyList(), {}),
                ) {
                    main("main", "Settings")
                    about("about", AndroidKitSettingsAbout("App", "1.2.3"), { aboutRequests++ })
                }
                TestCatalogSearchPage(catalog)
            }
        }

        rule.onNodeWithContentDescription("Search").performTextReplacement("release")
        rule.onNodeWithText("Version").performClick()
        rule.runOnIdle {
            val clipboard = rule.activity.getSystemService(ClipboardManager::class.java)
            assertEquals("1.2.3", clipboard.primaryClip?.getItemAt(0)?.text?.toString())
            assertEquals(0, aboutRequests)
        }
    }

    @Test
    fun searchPageRetainsSemanticsWithRtlAndLargeText() {
        val imeVisible = AtomicBoolean()
        rule.setContent {
            val visible = WindowInsets.isImeVisible
            SideEffect { imeVisible.set(visible) }
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(320.dp, 640.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(1.5f)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TestKitTheme {
                            val catalog = testSettingsCatalog(
                                rememberTestSettingsSearchConfiguration({}, listOf("Theme"), {}),
                            ) { main("main", "Settings") }
                            TestCatalogSearchPage(catalog)
                        }
                    }
                }
            }
        }

        rule.waitUntil(5_000) { imeVisible.get() }
        rule.onNodeWithContentDescription("Search").assertIsDisplayed().assertIsFocused()
        // Dismiss the device IME before checking history in the simulated smaller viewport.
        pressBack()
        rule.waitUntil(5_000) { !imeVisible.get() }
        rule.onNodeWithText("Recent searches").assertIsDisplayed()
        rule.onNodeWithText("Theme").performScrollTo().assertIsDisplayed().performClick()
        rule.onNodeWithContentDescription("Search").assertTextEquals("Theme")
    }

    @Test
    fun searchFromEverySettingsDestinationSharesCatalogHistoryAndPrivacy() {
        var page by mutableStateOf("main")
        var searching by mutableStateOf(false)
        var recents by mutableStateOf(listOf("Seed"))
        var visible by mutableStateOf(true)
        var clicks = 0
        rule.setContent {
            TestKitTheme {
                val catalog = testSettingsCatalog(
                    rememberTestSettingsSearchConfiguration(
                        { searching = true }, recents, { recents = it }, visible, { visible = it },
                    ),
                ) {
                    main("main", "Settings") {
                        section("actions") { button("main-action", "Main action", { clicks++ }) }
                    }
                    subpage("sub", "Subpage") {
                        section("actions") { button("sub-action", "Sub action", { clicks++ }) }
                    }
                    about("about", AndroidKitSettingsAbout("App", "1"), {})
                }
                TestCatalogOwner(catalog) {
                    if (searching) {
                        net.mamby.androidkit.compose.form.AndroidKitSettingsSearchPage(onBack = { searching = false })
                    } else {
                        net.mamby.androidkit.compose.form.AndroidKitSettingsPage(pageKey = page)
                    }
                }
            }
        }
        fun openFrom(key: String) {
            rule.runOnIdle { searching = false; page = key }
            rule.onNodeWithContentDescription("Search settings").performClick()
        }
        listOf("main", "sub", "about").forEach { key ->
            openFrom(key)
            rule.onNodeWithText("Seed").assertIsDisplayed()
            val search = rule.onNodeWithContentDescription("Search")
            search.performTextReplacement("main action")
            rule.onNodeWithText("Main action").assertIsDisplayed().performClick()
            rule.waitUntil { recents.firstOrNull() == "main action" }
            search.performTextReplacement("sub action")
            rule.onNodeWithText("Sub action").assertIsDisplayed().performClick()
            rule.waitUntil { recents.firstOrNull() == "sub action" }
            search.performTextReplacement("version")
            rule.onNodeWithText("Version").assertIsDisplayed()
        }
        rule.runOnIdle { assertEquals(6, clicks) }
        openFrom("sub")
        rule.onNodeWithText("main action").assertIsDisplayed()
        rule.onNodeWithContentDescription("Disable search history").performClick()
        rule.waitUntil { !visible }
        openFrom("about")
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        rule.onNodeWithText("Seed").assertDoesNotExist()
        rule.onNodeWithContentDescription("Enable search history").performClick()
        rule.waitUntil { visible && recents.isEmpty() }
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        val search = rule.onNodeWithContentDescription("Search")
        search.performTextReplacement("main action")
        rule.onNodeWithText("Main action").performClick()
        search.performTextReplacement("sub action")
        rule.onNodeWithText("Sub action").performClick()
        search.performTextReplacement("")
        rule.waitUntil { recents.firstOrNull() == "sub action" }
        rule.onAllNodesWithContentDescription("Remove recent search")[0].performClick()
        rule.waitUntil { "sub action" !in recents }
        openFrom("main")
        rule.onNodeWithText("sub action").assertDoesNotExist()
        rule.onNodeWithText("Clear all").performClick()
        rule.waitUntil { recents.isEmpty() }
        openFrom("about")
        rule.onNodeWithText("Seed").assertDoesNotExist()
        rule.onNodeWithText("main action").assertDoesNotExist()
    }

    @Test
    fun oneStoreRejectsSimultaneousSettingsOwnersAndAllowsDisposal() {
        val store = net.mamby.androidkit.compose.form.AndroidKitSettingsStore.open(
            rule.activity, "owner-${java.util.UUID.randomUUID()}",
            net.mamby.androidkit.compose.form.AndroidKitSettingsStorageProtection.Plaintext,
        )
        val first = Any()
        val second = Any()
        store.attachSettingsOwner(first)
        try {
            assertThrows(IllegalStateException::class.java) { store.attachSettingsOwner(second) }
            // A different owner cannot release the active owner's registration.
            store.detachSettingsOwner(second)
            assertThrows(IllegalStateException::class.java) { store.attachSettingsOwner(second) }
        } finally {
            store.detachSettingsOwner(first)
        }
        store.attachSettingsOwner(second)
        store.detachSettingsOwner(second)
    }

    @Test
    fun catalogRejectsDuplicatePageKeys() {
        assertThrows(IllegalArgumentException::class.java) {
            net.mamby.androidkit.compose.form.androidKitSettingsCatalog {
                main("same", "Settings")
                subpage("same", "Other")
            }
        }
    }
}
