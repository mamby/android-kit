package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.LayoutDirection
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry as Entry
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SectionCardBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun contextMenuPreservesTheFullEntryTargetAndHighlightsOnlyItsOwner() {
        var calls = 0
        var background = Color.Unspecified
        var highlight = Color.Unspecified
        rule.setContent {
            TestKitTheme {
                val selectedColor = MaterialTheme.colorScheme.secondaryContainer
                val containerColor = AndroidKitThemeTokens.settingSectionStyle.containerColor
                SideEffect {
                    background = containerColor
                    highlight = selectedColor
                }
                AndroidKitSectionCard(
                    entries = listOf(
                        Entry.Action(
                            key = "phone", label = "Phone", actionLabel = "Call phone",
                            onClick = { calls++ },
                            contextMenu = { item(label = "Copy phone", onClick = {}) },
                        ),
                        Entry.Multiline(
                            key = "notes", text = "Notes",
                            contextMenu = { item(label = "Copy notes", onClick = {}) },
                        ),
                        Entry.Action(
                            key = "disabled", label = "Disabled", actionLabel = "Unavailable",
                            onClick = { calls++ }, enabled = false,
                            contextMenu = { item(label = "Disabled menu", onClick = {}) },
                        ),
                    ),
                )
            }
        }
        val phone = rule.onNodeWithText("Phone")
        val notes = rule.onNodeWithText("Notes")
        phone.assert(SemanticsMatcher("Primary action retains its label and role") {
            it.config[SemanticsActions.OnClick].label == "Call phone" &&
                it.config[SemanticsProperties.Role] == Role.Button
        })
        phone.performTouchInput { click(Offset(2f, center.y)) }
        phone.performTouchInput { longClick(Offset(2f, center.y)) }
        phone.assertIsSelected()
        notes.assertIsNotSelected()
        rule.onNodeWithText("Copy phone").assertIsDisplayed()
        rule.onNodeWithText("Copy phone").performClick()
        phone.assertIsNotSelected()
        notes.assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .performTouchInput { longClick(Offset(2f, center.y)) }
        notes.assertIsSelected()
        phone.assertIsNotSelected()
        // Read-only entries have no primary-action ripple to blend with the selection color.
        // Sample away from the point-anchored popup and its shadow.
        val pixels = notes.captureToImage().toPixelMap()
        assertEquals(highlight, pixels[pixels.width - 10, pixels.height / 2])
        rule.onNodeWithText("Copy notes").performClick()
        notes.assertIsNotSelected()
        val restored = notes.captureToImage().toPixelMap()
        assertEquals(background, restored[restored.width - 10, restored.height / 2])
        rule.onNodeWithText("Disabled").assertIsNotEnabled()
            .performTouchInput { longClick() }
        rule.onNodeWithText("Disabled menu").assertDoesNotExist()
        rule.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun allEntryBodiesSharePaddingAndActionPaddingRemainsClickable() {
        var direction by mutableStateOf(LayoutDirection.Ltr)
        var calls = 0
        var horizontalPadding = 0f
        var verticalPadding = 0f
        rule.setContent {
            TestKitTheme {
                val density = LocalDensity.current
                val dimensions = AndroidKitThemeTokens.dimensions
                SideEffect {
                    horizontalPadding = with(density) { dimensions.sectionCardHorizontalPadding.toPx() }
                    verticalPadding = with(density) { dimensions.settingSectionEntryVerticalPadding.toPx() }
                }
                DeviceConfigurationOverride(DeviceConfigurationOverride.LayoutDirection(direction)) {
                    Column {
                        AndroidKitSectionCard(listOf(Entry.Info("info", "Info\nSecond line")), Modifier.testTag("info-card"))
                        AndroidKitSectionCard(
                            listOf(Entry.Action("action", "Action\nSecond line", "Activate", { calls++ })),
                            Modifier.testTag("action-card"),
                        )
                        AndroidKitSectionCard(
                            listOf(Entry.Multiline("multiline", "Multiline\nSecond line")), Modifier.testTag("multiline-card"),
                        )
                    }
                }
            }
        }
        for (layoutDirection in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
            rule.runOnIdle { direction = layoutDirection }
            for ((tag, text) in listOf("info" to "Info\nSecond line", "action" to "Action\nSecond line",
                "multiline" to "Multiline\nSecond line")) {
                val card = rule.onNodeWithTag("$tag-card").fetchSemanticsNode().boundsInRoot
                val body = rule.onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
                val leadingPadding = if (layoutDirection == LayoutDirection.Ltr) {
                    body.left - card.left
                } else {
                    card.right - body.right
                }
                assertEquals("$tag leading padding in $layoutDirection", horizontalPadding, leadingPadding, 1f)
                assertEquals("$tag top padding in $layoutDirection", verticalPadding, body.top - card.top, 1f)
            }
            rule.onNodeWithText("Action\nSecond line").performTouchInput { click(Offset(2f, center.y)) }
        }
        rule.runOnIdle { assertEquals(2, calls) }
    }

    @Test
    fun actionsExposeLabelsAndDisabledRowsDoNotInvokeCallbacks() {
        var calls = 0
        rule.enableAccessibilityChecks()
        rule.setContent {
            TestKitTheme {
                AndroidKitSectionCard(listOf(
                    Entry.Action("work", "Work", "Call work", { calls++ }, supportingText = "555 0100"),
                    Entry.Action("home", "Home", "Call home", { calls += 10 }, enabled = false),
                    Entry.Info("status", "Status", "Available", "Weekdays"),
                ))
            }
        }
        rule.onRoot().tryPerformAccessibilityChecks()
        rule.onNodeWithText("Work")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher("action label and supporting text are retained") {
                it.config[SemanticsActions.OnClick].label == "Call work" &&
                    it.config[SemanticsProperties.Text].any { text -> text.text == "555 0100" }
            })
            .performClick()
        rule.onNodeWithText("Home").assertIsNotEnabled().performTouchInput { click() }
        rule.onNodeWithText("Status")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            .assert(SemanticsMatcher("read-only value is grouped with its label") {
                it.config[SemanticsProperties.Text].map { text -> text.text } ==
                    listOf("Status", "Available", "Weekdays")
            })
        rule.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun keyboardSkipsDisabledEntriesAndFocusFollowsStableKeyAcrossReorder() {
        var reversed by mutableStateOf(false)
        var selected: String? = null
        lateinit var inputModeManager: InputModeManager
        rule.setContent {
            inputModeManager = LocalInputModeManager.current
            TestKitTheme {
                val first = Entry.Action("first", "First", "Select first", { selected = "first" })
                val second = Entry.Action("second", "Second", "Select second", { selected = "second" })
                val disabled = Entry.Action("disabled", "Disabled", "Unavailable", {}, enabled = false)
                AndroidKitSectionCard(if (reversed) listOf(second, disabled, first) else listOf(first, disabled, second))
            }
        }
        rule.waitUntil { rule.activity.hasWindowFocus() }
        rule.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        try {
            rule.onNodeWithText("First").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            rule.onNodeWithText("First").assertIsFocused()
            rule.onNodeWithText("First").performKeyInput { pressKey(Key.Tab) }
            rule.onNodeWithText("Second").assertIsFocused()
            rule.runOnIdle { reversed = true }
            rule.onNodeWithText("Second").assertIsFocused().performKeyInput { pressKey(Key.Enter) }
            rule.runOnIdle { assertEquals("second", selected) }
        } finally {
            rule.runOnIdle { inputModeManager.requestInputMode(InputMode.Touch) }
        }
    }

    @Test
    fun multilineAndLongValuesWrapAtLargeFontScaleInRtl() {
        val notes = "First line\n\nA longer note with enough words to wrap across several lines without clipping."
        val address = "42 Example Street, Apartment 5, Example City"
        val actionableAddress = "42 Example Street\nApartment 5\nExample City"
        var addressActions = 0
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(320.dp, 640.dp))) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(2f)) {
                    DeviceConfigurationOverride(DeviceConfigurationOverride.LayoutDirection(LayoutDirection.Rtl)) {
                        TestKitTheme {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                AndroidKitSectionCard(
                                    title = "Details", description = "End of section",
                                    entries = listOf(
                                        Entry.Info("address", "Address", address),
                                        Entry.Action("open-address", actionableAddress, "Find address", { addressActions++ }),
                                        Entry.Multiline("notes", notes, "Notes"),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
        rule.onNodeWithText("Details").assert(SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit))
        for (text in listOf(address, actionableAddress, notes)) {
            val layouts = mutableListOf<TextLayoutResult>()
            rule.onNodeWithText(text, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(layouts.single().lineCount > 1)
            val layout = layouts.single()
            assertFalse(
                "Overflow for '$text': size=${layout.size}, constraints=${layout.layoutInput.constraints}, " +
                    "width=${layout.didOverflowWidth}, height=${layout.didOverflowHeight}",
                layout.hasVisualOverflow,
            )
        }
        rule.onNodeWithText(actionableAddress).performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, addressActions) }
        rule.onNodeWithText("Notes").assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        rule.onNodeWithText("End of section").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun multipleValuesDispatchIndependentlyAfterContentUpdates() {
        var secondValue by mutableStateOf("second@example.org")
        val selected = mutableListOf<String>()
        rule.setContent {
            TestKitTheme {
                val currentSecond = secondValue
                AndroidKitSectionCard(listOf(
                    Entry.Action("first", "first@example.org", "Select first", { selected += "first@example.org" }),
                    Entry.Action("second", currentSecond, "Select second", { selected += currentSecond }),
                ))
            }
        }
        rule.onNodeWithText("first@example.org").performClick()
        rule.onNodeWithText("second@example.org").performClick()
        rule.runOnIdle { secondValue = "updated@example.org" }
        rule.onNodeWithText("updated@example.org").performClick()
        rule.runOnIdle {
            assertEquals(listOf("first@example.org", "second@example.org", "updated@example.org"), selected)
        }
    }

    @Test
    fun contentUpdatesAndEmptyCardRemovesItsChrome() {
        var entries by mutableStateOf<List<Entry>>(listOf(Entry.Info("status", "Status", "Before")))
        rule.setContent {
            TestKitTheme { AndroidKitSectionCard(entries, title = "Title", description = "Description") }
        }
        rule.runOnIdle { entries = listOf(Entry.Info("status", "Status", "After")) }
        rule.onNodeWithText("Before").assertDoesNotExist()
        rule.onNodeWithText("After").assertIsDisplayed()
        rule.runOnIdle { entries = emptyList() }
        rule.onNodeWithText("Title").assertDoesNotExist()
        rule.onNodeWithText("Description").assertDoesNotExist()
        rule.onNodeWithText("Status").assertDoesNotExist()
    }
}
