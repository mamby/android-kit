package net.mamby.androidkit.testing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.test.espresso.Espresso.pressBack
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import net.mamby.androidkit.compose.form.AndroidKitFloatingSearchBox
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FloatingSearchBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private var query by mutableStateOf("original")
    private var enabled by mutableStateOf(true)
    private var voiceEnabled by mutableStateOf(true)
    private val submissions = mutableListOf<String>()
    private val registry = SpeechRegistry()

    private fun component() {
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registry) {
                AndroidKitTheme {
                    AndroidKitFloatingSearchBox(query, { query = it }, submissions::add,
                        enabled = enabled, voiceInputEnabled = voiceEnabled)
                }
            }
        }
    }

    @Test fun editingClearingAndSubmissionAreControlled() {
        component()
        rule.onNodeWithContentDescription("Search").assertIsNotFocused()
            .performTextReplacement("query")
        rule.runOnIdle { assertEquals("query", query) }
        rule.onNodeWithContentDescription("Search").performImeAction()
        rule.runOnIdle { assertEquals(listOf("query"), submissions) }
        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.onNodeWithContentDescription("Search").assertIsFocused()
        rule.onNodeWithContentDescription("Clear search").assertDoesNotExist()
        rule.runOnIdle { assertEquals("", query) }
        rule.onNodeWithContentDescription("Search").performTextInput("   ")
        rule.onNodeWithContentDescription("Search").performImeAction()
        rule.runOnIdle { assertEquals(listOf("query"), submissions) }
    }

    @Test fun disabledAndVoiceHiddenControls() {
        enabled = false
        component()
        rule.onNodeWithContentDescription("Search").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Clear search").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Search by voice").assertIsNotEnabled()
        rule.runOnIdle { enabled = true; voiceEnabled = false }
        rule.onNodeWithContentDescription("Search").assertIsEnabled()
        rule.onNodeWithContentDescription("Search by voice").assertDoesNotExist()
    }

    @Test fun speechUsesFreeFormAndReplacesWithoutSubmitting() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.onNodeWithContentDescription("Search by voice").assertIsNotEnabled()
        rule.runOnIdle {
            assertEquals(1, registry.launchCount)
            assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, registry.intent?.action)
            assertEquals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                registry.intent?.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL))
            assertTrue(registry.intent?.hasExtra(RecognizerIntent.EXTRA_LANGUAGE) == false)
            registry.complete(Activity.RESULT_OK, "spoken query", "alternative")
        }
        rule.onNodeWithContentDescription("Search").assertTextContains("spoken query")
        rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
        rule.runOnIdle { assertTrue(submissions.isEmpty()) }
    }

    @Test fun cancellationAndEmptyResultsPreserveQuery() {
        component()
        for ((code, results) in listOf(
            Activity.RESULT_CANCELED to arrayOf("ignored"),
            Activity.RESULT_OK to emptyArray(),
            Activity.RESULT_OK to arrayOf("   ", "not the first result"),
        )) {
            rule.onNodeWithContentDescription("Search by voice").performClick()
            rule.runOnIdle { registry.complete(code, *results) }
            rule.onNodeWithContentDescription("Search").assertTextContains("original")
            rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
        }
    }

    @Test fun disabledWhileRecognizingIgnoresResult() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { enabled = false }
        rule.waitForIdle()
        rule.runOnIdle { registry.complete(Activity.RESULT_OK, "ignored") }
        rule.runOnIdle { assertEquals("original", query) }
    }

    @Test fun missingRecognizerAndSecurityFailureAllowRetryAndTyping() {
        component()
        for (failure in listOf(ActivityNotFoundException(), SecurityException())) {
            rule.runOnIdle { registry.failure = failure }
            rule.onNodeWithContentDescription("Search by voice").performClick()
            rule.onNodeWithText(VoiceError).assertExists()
            rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
            rule.onNodeWithContentDescription("Search").performTextReplacement("typed ${failure.javaClass.simpleName}")
            rule.onNodeWithText(VoiceError).assertDoesNotExist()
        }
        rule.runOnIdle { registry.failure = null }
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { registry.complete(Activity.RESULT_OK, "retry") }
        rule.onNodeWithContentDescription("Search").assertTextContains("retry")
    }

    @Test fun pageKeepsSearchAboveKeyboardAndLastItemClear() = hostGeometry(sheet = false)
    @Test fun sheetKeepsSearchAboveKeyboardAndLastItemClear() = hostGeometry(sheet = true)
    @Test fun pageSupportsNarrowRtlLargeText() = hostGeometry(sheet = false, adaptive = true)
    @Test fun sheetSupportsNarrowRtlLargeText() = hostGeometry(sheet = true, adaptive = true)

    private fun hostGeometry(sheet: Boolean, adaptive: Boolean = false) {
        rule.setContent {
            AndroidKitTheme {
                @androidx.compose.runtime.Composable
                fun Host() {
                    val action = AndroidKitFloatingAction.Search(
                        query, { query = it }, submissions::add, modifier = Modifier.testTag("search"),
                    )
                    @androidx.compose.runtime.Composable
                    fun Body(padding: androidx.compose.foundation.layout.PaddingValues) {
                        LazyColumn(Modifier.fillMaxSize().testTag("list"), contentPadding = padding) {
                            items((0..50).toList()) { Text("Item $it") }
                        }
                    }
                    Box(Modifier.fillMaxSize()) {
                        if (sheet) {
                            AndroidKitBottomSheet(true, "Sheet", {}, floatingAction = action,
                                scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged) { Body(it) }
                        } else {
                            AndroidKitPage(title = "Page", floatingActionButton = action) { Body(it) }
                        }
                    }
                }
                if (adaptive) {
                    DeviceConfigurationOverride(DeviceConfigurationOverride.WindowSize(DpSize(320.dp, 640.dp))) {
                        DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(1.5f)) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { Host() }
                        }
                    }
                } else Host()
            }
        }
        rule.waitForIdle()
        rule.onNodeWithTag("list").performScrollToIndex(50)
        fun assertClearance() {
            val search = rule.onNodeWithTag("search").fetchSemanticsNode().boundsInRoot
            val last = rule.onNodeWithText("Item 50").fetchSemanticsNode().boundsInRoot
            assertTrue("Last item ($last) must clear the floating search ($search)", last.bottom <= search.top)
            assertTrue("Search must have visible width", search.width > 0)
        }
        assertClearance()
        if (!adaptive) {
            val closedBottom = rule.onNodeWithTag("search").fetchSemanticsNode().boundsInRoot.bottom
            rule.onNodeWithContentDescription("Search").performClick()
            rule.waitUntil(5_000) {
                rule.onNodeWithTag("search").fetchSemanticsNode().boundsInRoot.bottom < closedBottom
            }
            rule.onNodeWithTag("list").performScrollToIndex(50)
            assertClearance()
            pressBack()
            rule.waitUntil(5_000) {
                kotlin.math.abs(rule.onNodeWithTag("search").fetchSemanticsNode().boundsInRoot.bottom - closedBottom) < 1f
            }
        }
    }

    private class SpeechRegistry : ActivityResultRegistry(), ActivityResultRegistryOwner {
        override val activityResultRegistry: ActivityResultRegistry get() = this
        var intent: Intent? = null
        var launchCount = 0
        var failure: RuntimeException? = null
        private var requestCode = 0
        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
            input: I, options: ActivityOptionsCompat?) {
            failure?.let { throw it }
            this.requestCode = requestCode
            launchCount++
            intent = contract.createIntent(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext, input)
        }
        fun complete(code: Int, vararg results: String) {
            dispatchResult(requestCode, code, Intent().putStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS, ArrayList(results.toList())))
        }
    }
}

private const val VoiceError = "Voice input is unavailable. You can still type your search."
