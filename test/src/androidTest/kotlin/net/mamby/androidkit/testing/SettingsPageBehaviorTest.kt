package net.mamby.androidkit.testing

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDialog
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
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import androidx.test.espresso.Espresso.pressBack
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsPageBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun informationalRowsUpdateAndDisappearWithTheirSection() {
        var value by mutableStateOf("Before")
        var visible by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    if (visible) {
                        section(key = "status") { info(label = "Status", value = value) }
                    }
                    section(key = "remaining") { info(label = "Remaining") }
                }
            }
        }
        rule.onNodeWithText("Before").assertIsDisplayed()
        rule.onNodeWithText("Status").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        rule.runOnIdle { value = "After" }
        rule.onNodeWithText("After").assertIsDisplayed()
        rule.onNodeWithText("Before").assertDoesNotExist()
        rule.runOnIdle { visible = false }
        rule.onNodeWithText("After").assertDoesNotExist()
        rule.onNodeWithText("Remaining").assertIsDisplayed()
    }

    @Test
    fun appLockExtrasWaitForHostAndTimeoutSelectionClosesDialog() {
        var checked by mutableStateOf(false)
        var selected by mutableStateOf("0")
        var requested: String? = null
        var locks = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    securitySection(appLock = AndroidKitAppLockSetting(
                        checked = checked, onCheckedChange = {},
                        timeout = AndroidKitAppLockTimeoutSetting(
                            options = timeoutOptions,
                            selectedId = selected, onSelected = { requested = it },
                        ),
                        onLockNow = { locks++ },
                    ))
                }
            }
        }
        rule.onNodeWithText("App lock").performClick()
        rule.onNodeWithText("Lock after leaving the app").assertDoesNotExist()
        rule.onNodeWithText("Lock now").assertDoesNotExist()
        rule.runOnIdle { checked = true }
        rule.onNodeWithText("Lock after leaving the app").performClick()
        rule.onNode(isDialog()).assertExists()
        rule.onNode(hasText("Immediately") and radioRole).assertIsSelected()
        rule.onNode(hasText("After 5 minutes") and radioRole).performClick()
        rule.onNode(isDialog()).assertDoesNotExist()
        rule.runOnIdle { assertEquals("5", requested) }
        rule.onNodeWithText("Immediately").assertIsDisplayed()
        rule.runOnIdle { selected = requireNotNull(requested) }
        rule.onNodeWithText("After 5 minutes").assertIsDisplayed()
        rule.onNodeWithText("Lock now").performClick()
        rule.runOnIdle { assertEquals(1, locks) }
    }

    @Test
    fun timeoutDialogBackDismissesWithoutChangingSelection() {
        var requests = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    securitySection(appLock = AndroidKitAppLockSetting(
                        checked = true, onCheckedChange = {},
                        timeout = AndroidKitAppLockTimeoutSetting(
                            options = timeoutOptions,
                            selectedId = "5", onSelected = { requests++ },
                        ),
                    ))
                }
            }
        }
        rule.onNodeWithText("Lock now").assertDoesNotExist()
        rule.onNodeWithText("Lock after leaving the app").performClick()
        pressBack()
        rule.onNode(isDialog()).assertDoesNotExist()
        rule.runOnIdle { assertEquals(0, requests) }
        rule.onNodeWithText("Lock after leaving the app").performClick()
        rule.onNode(hasText("After 5 minutes") and radioRole).assertIsSelected()
    }

    @Test
    fun timeoutDialogCannotOutliveItsEnabledConfiguration() {
        var checked by mutableStateOf(true)
        var enabled by mutableStateOf(true)
        var timeoutEnabled by mutableStateOf(true)
        var hasTimeout by mutableStateOf(true)
        var hasSection by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    if (hasSection) {
                        securitySection(appLock = AndroidKitAppLockSetting(
                            checked = checked, onCheckedChange = {}, enabled = enabled,
                            timeout = if (hasTimeout) AndroidKitAppLockTimeoutSetting(
                                options = timeoutOptions, enabled = timeoutEnabled,
                                selectedId = "0", onSelected = {},
                            ) else null,
                            onLockNow = {},
                        ))
                    }
                }
            }
        }
        val removals: List<() -> Unit> = listOf(
            { checked = false }, { enabled = false }, { timeoutEnabled = false },
            { hasTimeout = false }, { hasSection = false },
        )
        removals.forEach { remove ->
            rule.onNodeWithText("Lock after leaving the app").performClick()
            rule.onNode(isDialog()).assertExists()
            rule.runOnIdle { remove() }
            try {
                rule.onNode(isDialog()).assertDoesNotExist()
            } catch (failure: AssertionError) {
                throw AssertionError(
                    "Dialog remained: checked=$checked enabled=$enabled timeoutEnabled=$timeoutEnabled " +
                        "hasTimeout=$hasTimeout hasSection=$hasSection", failure,
                )
            }
            if (!enabled) rule.onNodeWithText("Lock now").assertIsNotEnabled()
            rule.runOnIdle {
                checked = true; enabled = true; timeoutEnabled = true; hasTimeout = true; hasSection = true
            }
            rule.onNode(isDialog()).assertDoesNotExist()
            rule.onAllNodesWithText("Lock after leaving the app").assertCountEquals(1)
        }
    }

    @Test
    fun predefinedIconsAllowHostOverridesAndRestoreDefaults() {
        var icon by mutableStateOf<ImageVector?>(null)
        val overrideIcon = ImageVector.Builder(
            name = "Host override", defaultWidth = 48.dp, defaultHeight = 24.dp,
            viewportWidth = 48f, viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(0f, 0f)
                lineTo(48f, 12f)
                lineTo(0f, 24f)
                close()
            }
        }.build()
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(
                        language = AndroidKitLanguageSetting(
                            selection = AndroidKitSettingsSelection(
                                options = listOf(AndroidKitSettingsOption("en", "English")),
                                selectedId = "en", onSelected = {},
                                systemOption = AndroidKitSettingsSystemOption("system", "English"),
                                icon = icon,
                            ),
                        ),
                        theme = AndroidKitSettingsSelection(
                            options = listOf(AndroidKitSettingsOption("light", "Light")),
                            selectedId = "light", onSelected = {},
                            systemOption = AndroidKitSettingsSystemOption("system", "Light"),
                            icon = icon,
                        ),
                    )
                    securitySection(appLock = AndroidKitAppLockSetting(
                        checked = false, onCheckedChange = {}, icon = icon,
                    ))
                }
            }
        }
        val labels = listOf("Language", "Theme", "App lock")
        fun labelStarts() = labels.map {
            rule.onNodeWithText(it, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left
        }
        val defaultStarts = labelStarts()
        rule.runOnIdle { icon = overrideIcon }
        labelStarts().zip(defaultStarts).forEach { (custom, default) ->
            assertTrue("The host icon's intrinsic width must be used", custom > default)
        }
        rule.runOnIdle { icon = null }
        assertEquals(defaultStarts, labelStarts())
    }

    @Test
    fun hiddenSectionsLeaveNoGapAndCustomSectionsKeepTheirOrder() {
        var showEmpty by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    section("first") { button("First", {}) }
                    if (showEmpty) {
                        generalSection()
                        securitySection()
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
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(language = AndroidKitLanguageSetting(
                        selection = AndroidKitSettingsSelection(
                            selectedId = selected, onSelected = { selected = it },
                            options = listOf(AndroidKitSettingsOption("en", "English"), AndroidKitSettingsOption("fr", "Français")),
                            systemOption = AndroidKitSettingsSystemOption("system", "English"),
                        ),
                    ))
                }
            }
        }
        rule.onNodeWithText("Language").performClick()
        rule.onNodeWithText("Search languages").performTextReplacement("missing")
        rule.onNodeWithText("No matching languages").assertIsDisplayed()
        rule.onNodeWithText("Search languages").performTextReplacement("FRANCAIS")
        rule.onNodeWithText("Français").performClick()
        rule.runOnIdle { assertEquals("fr", selected) }
        rule.onNodeWithText("Language").performClick()
        rule.onNodeWithText("English").assertIsDisplayed()
        rule.onNodeWithContentDescription("Close").performClick()
    }

    @Test
    fun themePickerAcceptsHostDefinedChoices() {
        var selected by mutableStateOf("light")
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(theme = AndroidKitSettingsSelection(
                        selectedId = selected, onSelected = { selected = it },
                        options = listOf(AndroidKitSettingsOption("light", "Light"), AndroidKitSettingsOption("prism", "Prism")),
                        systemOption = AndroidKitSettingsSystemOption("system", "Light"),
                    ))
                }
            }
        }
        rule.onNodeWithText("Theme").performClick()
        rule.onNodeWithText("Prism").performClick()
        rule.runOnIdle { assertEquals("prism", selected) }
    }

    @Test
    fun systemOptionShowsCurrentHostValueInSettingsRowAndPicker() {
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(theme = AndroidKitSettingsSelection(
                        selectedId = "system", onSelected = {},
                        options = listOf(AndroidKitSettingsOption("light", "Light")),
                        systemOption = AndroidKitSettingsSystemOption(
                            id = "system", currentValueLabel = "Light",
                        ),
                    ))
                }
            }
        }

        rule.onNodeWithText("System (Light)").assertIsDisplayed().performClick()
        rule.onNode(
            hasText("System (Light)") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
        ).assertIsDisplayed()
    }

    @Test
    fun opacityHasEndpointLabelsAndAppLockWaitsForHostConfirmation() {
        var level by mutableFloatStateOf(0f)
        var commits = 0
        var lockRequests = 0
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(floatingOpacity = AndroidKitFloatingOpacitySetting(
                        value = level,
                        onValueChange = { level = it }, onValueChangeFinished = { commits++ },
                    ))
                    securitySection(appLock = AndroidKitAppLockSetting(
                        checked = false, onCheckedChange = { lockRequests++ },
                    ))
                }
            }
        }
        rule.onNodeWithText("Min").assertIsDisplayed()
        rule.onNodeWithText("Max").assertIsDisplayed()
        rule.onNodeWithContentDescription("Transparency").performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
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
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    generalSection(floatingOpacity = AndroidKitFloatingOpacitySetting(
                        value = level,
                        onValueChange = { level = it }, onValueChangeFinished = {},
                    ))
                    section(key = "scroll") { info(label = "Scroll content") }
                }
            }
        }

        rule.onNodeWithContentDescription("Transparency")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
        rule.runOnIdle {
            assertEquals(100f, level)
        }
        val progress = rule.onNodeWithContentDescription("Transparency")
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(100f, progress.current)
        rule.onAllNodesWithText("Scroll content").assertCountEquals(1)
    }

    @Test
    fun conditionalSettingsUpdateWithoutDuplicatingTheirSection() {
        var showLanguage by mutableStateOf(true)
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
                    section("visibility") {
                        toggle("Show language", showLanguage, { showLanguage = it })
                    }
                    generalSection(
                        language = if (showLanguage) AndroidKitLanguageSetting(
                            selection = AndroidKitSettingsSelection(
                                selectedId = "en", onSelected = {},
                                options = listOf(AndroidKitSettingsOption("en", "English")),
                                systemOption = AndroidKitSettingsSystemOption("system", "English"),
                            ),
                        ) else null,
                        theme = AndroidKitSettingsSelection(
                            selectedId = "light", onSelected = {},
                            options = listOf(AndroidKitSettingsOption("light", "Light")),
                            systemOption = AndroidKitSettingsSystemOption("system", "Light"),
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
                AndroidKitSettingsPage(configuration = AndroidKitSettingsPageConfiguration.Subpage) {
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
                        configuration = AndroidKitSettingsPageConfiguration.Subpage,
                        title = destination,
                        listState = rememberLazyListState(),
                        onBack = if (destination == "child") ({ destination = "root" }) else null,
                    ) {
                        if (destination == "root") {
                            repeat(30) { index -> section(key = "item-$index") { info(label = "Entry $index") } }
                            section("navigation") { navigation("Open child", { destination = "child" }) }
                        } else {
                            section(key = "child-content") { info(label = "Child content") }
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

private val timeoutOptions = listOf(
    AndroidKitSettingsOption("0", "Immediately"),
    AndroidKitSettingsOption("1", "After 1 minute"),
    AndroidKitSettingsOption("5", "After 5 minutes"),
    AndroidKitSettingsOption("15", "After 15 minutes"),
)
private val radioRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
