package net.mamby.androidkit.testing

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.WindowSize
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.TextRange
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
import net.mamby.androidkit.compose.form.LocalSearchSpeechInputFactory
import net.mamby.androidkit.compose.form.SearchSpeechInput
import net.mamby.androidkit.compose.form.searchRecognitionIntent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    private val inputs = mutableListOf<FakeSpeechInput>()
    private var permissionGranted = true
    private var shown by mutableStateOf(true)
    private var startFailure: RuntimeException? = null
    private var testLifecycleOwner: LifecycleOwner? = null

    private fun component() {
        rule.setContent {
            val base = LocalContext.current
            val context = androidx.compose.runtime.remember {
                object : ContextWrapper(base) {
                    override fun checkPermission(permission: String, pid: Int, uid: Int): Int =
                        if (permission == Manifest.permission.RECORD_AUDIO) {
                            if (permissionGranted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
                        } else super.checkPermission(permission, pid, uid)
                }
            }
            val factory = androidx.compose.runtime.remember {
                { _: android.content.Context ->
                    startFailure?.let { throw it }
                    FakeSpeechInput().also { inputs.add(it) }
                }
            }
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registry,
                LocalLifecycleOwner provides (testLifecycleOwner ?: LocalLifecycleOwner.current),
                LocalContext provides context, LocalSearchSpeechInputFactory provides factory) {
                AndroidKitTheme {
                    if (shown) AndroidKitFloatingSearchBox(query, { query = it }, submissions::add,
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

    @Test fun partialSpeechAppendsWithoutDuplicatesAndStopCommitsFinalText() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle {
            assertEquals(1, inputs.size)
            inputs.last().listener.onReadyForSpeech(null)
            inputs.last().listener.onPartialResults(results("spoken"))
        }
        rule.onNodeWithText("Listening…").assertExists()
        rule.onNodeWithContentDescription("Search").assertTextContains("original spoken")
        rule.runOnIdle { inputs.last().listener.onPartialResults(results("spoken query")) }
        rule.onNodeWithContentDescription("Search").assertTextContains("original spoken query")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange,
                TextRange("original spoken query".length)))
        rule.onNodeWithContentDescription("Stop listening").performClick().assertIsNotEnabled()
        rule.onNodeWithText("Finishing…").assertExists()
        rule.runOnIdle {
            assertEquals(1, inputs.last().stopCount)
            inputs.last().listener.onResults(results("final words"))
            assertEquals(1, inputs.last().destroyCount)
        }
        rule.onNodeWithContentDescription("Search").assertTextContains("original final words")
        rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
        rule.runOnIdle { assertTrue(submissions.isEmpty()) }
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { inputs.last().listener.onResults(results("again")) }
        rule.onNodeWithContentDescription("Search").assertTextContains("original final words again")
    }

    @Test fun emptyResultsPreserveQuery() {
        component()
        for (text in listOf("", "   ")) {
            rule.onNodeWithContentDescription("Search by voice").performClick()
            rule.runOnIdle { inputs.last().listener.onResults(results(text)) }
            rule.onNodeWithContentDescription("Search").assertTextContains("original")
            rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
        }
    }

    @Test fun disabledWhileRecognizingIgnoresResult() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { enabled = false }
        rule.waitForIdle()
        rule.runOnIdle {
            inputs.last().listener.onResults(results("ignored"))
            assertEquals("original", query)
            assertEquals(1, inputs.last().cancelCount)
            assertEquals(1, inputs.last().destroyCount)
        }
    }

    @Test fun missingRecognizerAndSecurityFailureAllowRetryAndTyping() {
        component()
        for (failure in listOf(IllegalStateException(), SecurityException())) {
            rule.runOnIdle { startFailure = failure }
            rule.onNodeWithContentDescription("Search by voice").performClick()
            rule.onNodeWithText(if (failure is SecurityException) PermissionError else VoiceError).assertExists()
            rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
            rule.onNodeWithContentDescription("Search").performTextReplacement("typed ${failure.javaClass.simpleName}")
            rule.onNodeWithText(VoiceError).assertDoesNotExist()
        }
        rule.runOnIdle { startFailure = null }
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { inputs.last().listener.onResults(results("retry")) }
        rule.runOnIdle { assertTrue(query.endsWith(" retry")) }
    }

    @Test fun permissionDenialDoesNotStartRecordingAndTypingStillWorks() {
        permissionGranted = false
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick().assertIsNotEnabled()
        rule.runOnIdle {
            assertEquals(Manifest.permission.RECORD_AUDIO, registry.requestedPermission)
            registry.complete(false)
            assertTrue(inputs.isEmpty())
        }
        rule.onNodeWithText(PermissionError).assertExists()
        rule.onNodeWithContentDescription("Search").performTextReplacement("typed")
        rule.onNodeWithText(PermissionError).assertDoesNotExist()
    }

    @Test fun permissionGrantStartsOnlyTheRequestedSession() {
        permissionGranted = false
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { permissionGranted = true; registry.complete(true) }
        rule.onNodeWithContentDescription("Stop listening").assertExists()
        rule.runOnIdle { assertEquals(1, inputs.size) }
    }

    @Test fun recognizerRequestsPartialResultsAndSupportedLanguageSwitching() {
        val intent = searchRecognitionIntent()
        assertEquals(RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL))
        assertTrue(intent.getBooleanExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false))
        assertTrue(!intent.hasExtra(RecognizerIntent.EXTRA_LANGUAGE))
        if (Build.VERSION.SDK_INT >= 34) {
            assertEquals(RecognizerIntent.LANGUAGE_SWITCH_BALANCED,
                intent.getStringExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH))
        }
    }

    @Test fun backgroundingReleasesMicrophoneWithoutRestartingOnReturn() {
        val owner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        rule.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        testLifecycleOwner = owner
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        rule.runOnIdle {
            assertEquals(1, inputs.last().cancelCount)
            assertEquals(1, inputs.last().destroyCount)
            inputs.last().listener.onResults(results("late"))
            assertEquals("original", query)
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        rule.onNodeWithContentDescription("Search by voice").assertIsEnabled()
        rule.runOnIdle { assertEquals(1, inputs.size) }
    }

    @Test fun clearingWhileListeningCancelsAndKeepsFieldFocused() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.onNodeWithContentDescription("Search").assertIsFocused()
        rule.runOnIdle {
            inputs.last().listener.onResults(results("late"))
            assertEquals("", query)
            assertEquals(1, inputs.last().cancelCount)
        }
    }

    @Test fun externalQueryChangeCancelsPendingDictation() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { query = "host replacement" }
        rule.waitForIdle()
        rule.runOnIdle {
            inputs.last().listener.onResults(results("late"))
            assertEquals("host replacement", query)
            assertEquals(1, inputs.last().destroyCount)
        }
    }

    @Test fun editingCancelsAndIgnoresLateResults() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { inputs.last().listener.onPartialResults(results("partial")) }
        rule.onNodeWithContentDescription("Search").performTextReplacement("manual")
        rule.runOnIdle {
            inputs.last().listener.onResults(results("late result"))
            assertEquals("manual", query)
            assertEquals(1, inputs.last().destroyCount)
        }
    }

    @Test fun disposalReleasesMicrophoneAndIgnoresLateResults() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle { shown = false }
        rule.waitForIdle()
        rule.runOnIdle {
            inputs.last().listener.onResults(results("late result"))
            assertEquals("original", query)
            assertEquals(1, inputs.last().cancelCount)
            assertEquals(1, inputs.last().destroyCount)
        }
    }

    @Test fun providerFailurePreservesVisiblePartialAndAllowsRetry() {
        component()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle {
            inputs.last().listener.onPartialResults(results("partial"))
            inputs.last().listener.onError(SpeechRecognizer.ERROR_NETWORK)
        }
        rule.onNodeWithContentDescription("Search").assertTextContains("original partial")
        rule.onNodeWithText(VoiceError).assertExists()
        rule.onNodeWithContentDescription("Search by voice").performClick()
        rule.runOnIdle {
            inputs.first().listener.onResults(results("stale"))
            inputs.last().listener.onResults(results("new"))
        }
        rule.onNodeWithContentDescription("Search").assertTextContains("original partial new")
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
        var requestedPermission: String? = null
        private var requestCode = 0
        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
            input: I, options: ActivityOptionsCompat?) {
            this.requestCode = requestCode
            requestedPermission = input as String
        }
        fun complete(granted: Boolean) { dispatchResult(requestCode, granted) }
    }

    private class FakeSpeechInput : SearchSpeechInput {
        lateinit var listener: RecognitionListener
        var stopCount = 0
        var cancelCount = 0
        var destroyCount = 0
        override fun start(listener: RecognitionListener) { this.listener = listener }
        override fun stop() { stopCount++ }
        override fun cancel() { cancelCount++ }
        override fun destroy() { destroyCount++ }
    }

    private fun results(text: String) = Bundle().apply {
        putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf(text))
    }
}

private const val VoiceError = "Voice input is unavailable. You can still type your search."
private const val PermissionError = "Microphone access is needed for voice input. You can allow it in app settings."
