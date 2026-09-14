package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.presentation.AndroidKitSupportPrompt
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@Composable
private fun EnglishKitTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val resources = remember(context) {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList(Locale.ENGLISH))
        }
        context.createConfigurationContext(configuration).resources
    }
    CompositionLocalProvider(LocalResources provides resources) {
        AndroidKitTheme(content = content)
    }
}

class SupportPromptBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun dismissalIsRetainedAndNewIdCanDonate() {
        var id by mutableStateOf("first")
        var dismissed = 0
        var donated = 0
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            EnglishKitTheme {
                AndroidKitPage(
                    supportPrompt = AndroidKitSupportPrompt(id, { donated++ }, { dismissed++ }),
                    listContent = { item(key = "body") { Text("Body") } },
                )
            }
        }
        rule.onNodeWithText("Learn more").performClick()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Donate").assertIsDisplayed().performClick()
        rule.runOnIdle { assertEquals(1, donated); assertEquals(0, dismissed) }
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Learn more").assertDoesNotExist()
        rule.runOnIdle { id = "second" }
        rule.onNodeWithText("Not now").performClick()
        rule.onNodeWithText("Learn more").assertDoesNotExist()
        rule.runOnIdle { assertEquals(1, dismissed); assertEquals(1, donated) }
    }

    @Test fun optionalPromptScrollsAwayAndRemovalRetainsBodyPosition() {
        var visible by mutableStateOf(false)
        lateinit var state: androidx.compose.foundation.lazy.LazyListState
        rule.setContent {
            EnglishKitTheme {
                state = rememberLazyListState()
                AndroidKitPage(
                    listState = state,
                    supportPrompt = if (visible) AndroidKitSupportPrompt("one", {}, {}) else null,
                    listContent = { items(100, key = { "body:$it" }) { Text("Body $it") } },
                )
            }
        }
        rule.onNodeWithText("Learn more").assertDoesNotExist()
        rule.runOnIdle { visible = true }
        rule.onNode(hasScrollAction()).performScrollToIndex(0)
        rule.onNodeWithText("Learn more").assertIsDisplayed()
        rule.onNode(hasScrollAction()).performScrollToIndex(40)
        rule.onNodeWithText("Learn more").assertDoesNotExist()
        var key: Any? = null
        var offset = 0
        rule.runOnIdle {
            key = state.layoutInfo.visibleItemsInfo.first().key
            offset = state.firstVisibleItemScrollOffset
            visible = false
        }
        rule.runOnIdle {
            assertEquals(key, state.layoutInfo.visibleItemsInfo.first().key)
            assertEquals(offset, state.firstVisibleItemScrollOffset)
        }
    }

    @Test fun sheetCloseEndsPresentationOnce() {
        var dismissed = 0
        rule.setContent {
            EnglishKitTheme {
                AndroidKitPage(
                    supportPrompt = AndroidKitSupportPrompt("one", {}, { dismissed++ }),
                    listContent = { item { Text("Body") } },
                )
            }
        }
        rule.onNodeWithText("Learn more").performClick()
        rule.onNodeWithContentDescription("Close").performClick()
        rule.onNodeWithText("Learn more").assertDoesNotExist()
        rule.onNodeWithText("Donate").assertDoesNotExist()
        rule.runOnIdle { assertEquals(1, dismissed) }
    }

    @Test fun removingConfigurationClosesSheetAndDisabledPromptCannotOpenIt() {
        var prompt by mutableStateOf<AndroidKitSupportPrompt?>(AndroidKitSupportPrompt("one", {}, {}))
        rule.setContent {
            EnglishKitTheme {
                AndroidKitPage(supportPrompt = prompt, listContent = { item { Text("Body") } })
            }
        }
        rule.onNodeWithText("Learn more").performClick()
        rule.onNodeWithText("Donate").assertIsDisplayed()
        rule.runOnIdle { prompt = null }
        rule.onNodeWithText("Donate").assertDoesNotExist()
        rule.runOnIdle { prompt = AndroidKitSupportPrompt("two", {}, {}, enabled = false) }
        rule.onNodeWithText("Learn more").assertIsNotEnabled()
        rule.onNodeWithText("Not now").assertIsEnabled()
    }
}
