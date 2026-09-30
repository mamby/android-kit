package net.mamby.androidkit.testing

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.form.AndroidKitFloatingSearchBox
import net.mamby.androidkit.compose.form.AndroidKitSearchItem
import net.mamby.androidkit.compose.form.AndroidKitSearchMode
import net.mamby.androidkit.compose.form.AndroidKitSearchPage
import net.mamby.androidkit.compose.layout.AndroidKitPage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.Before

class SearchModeBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun keepTestActivityScreenOn() {
        rule.activityRule.scenario.onActivity {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun liveFloatingActionRequestsEditsAndClearWithoutRequestingOnRecomposition() {
        var query by mutableStateOf("")
        val requests = mutableListOf<String>()
        rule.setContent {
            TestKitTheme {
                AndroidKitPage(
                    title = "Topics",
                    floatingActionButton = AndroidKitFloatingAction.Search(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { requests += it },
                        voiceInputEnabled = false,
                        searchMode = AndroidKitSearchMode.Live,
                    ),
                ) { }
            }
        }
        val field = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        field.performTextReplacement("coffee")
        rule.runOnIdle { assertEquals(listOf("coffee"), requests) }
        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.runOnIdle {
            assertEquals(listOf("coffee", ""), requests)
            query = "External update"
        }
        rule.runOnIdle { assertEquals(listOf("coffee", ""), requests) }
    }

    @Test
    fun submittedBoxUpdatesDraftButRequestsOnlyNonblankImeSubmission() {
        var query by mutableStateOf("")
        val requests = mutableListOf<String>()
        rule.setContent {
            TestKitTheme {
                AndroidKitFloatingSearchBox(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { requests += it },
                    voiceInputEnabled = false,
                    searchMode = AndroidKitSearchMode.OnSubmit,
                )
            }
        }
        val field = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        field.performTextReplacement(" coffee ")
        rule.runOnIdle {
            assertEquals(" coffee ", query)
            assertEquals(emptyList<String>(), requests)
        }
        field.performImeAction()
        rule.runOnIdle { assertEquals(listOf(" coffee "), requests) }
        field.performTextReplacement(" ")
        field.performImeAction()
        rule.runOnIdle { assertEquals(listOf(" coffee "), requests) }
    }

    @Test
    fun submittedPageRetainsResultsAndHistoryUntilSubmissionAndRestoresActiveQuery() {
        var query by mutableStateOf("")
        var recents by mutableStateOf(emptyList<String>())
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            TestKitTheme {
                AndroidKitSearchPage(
                    items = listOf(
                        AndroidKitSearchItem("coffee", "Coffee", "Coffee result", {}),
                        AndroidKitSearchItem("tea", "Tea", "Tea result", {}),
                    ),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = recents,
                    onRecentQueriesChange = { recents = it },
                    recentQueriesVisible = true,
                    onRecentQueriesVisibleChange = {},
                    voiceInputEnabled = false,
                    searchMode = AndroidKitSearchMode.OnSubmit,
                ) { matches ->
                    items(matches, key = { it.key }) { match ->
                        Button(onClick = match.onClick) { Text(match.data) }
                    }
                }
            }
        }
        val field = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        field.performTextReplacement("coffee")
        rule.onNodeWithText("Coffee result").assertDoesNotExist()
        rule.runOnIdle { assertEquals(emptyList<String>(), recents) }
        field.performImeAction()
        rule.onNodeWithText("Coffee result").assertIsDisplayed()
        field.performTextReplacement("tea")
        rule.onNodeWithText("Coffee result").assertIsDisplayed()
        rule.onNodeWithText("Tea result").assertDoesNotExist()
        rule.onNodeWithText("Coffee result").performClick()
        rule.runOnIdle { assertEquals(listOf("coffee"), recents) }
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Coffee result").assertIsDisplayed()
        rule.runOnIdle { assertEquals(listOf("coffee"), recents) }
        field.performImeAction()
        rule.onNodeWithText("Tea result").assertIsDisplayed()
        rule.onNodeWithText("Coffee result").assertDoesNotExist()
        rule.runOnIdle { assertEquals(listOf("tea", "coffee"), recents) }
        rule.onNodeWithContentDescription("Clear search").performClick()
        rule.onNodeWithText("Tea result").assertDoesNotExist()
        field.performTextReplacement("coffee")
        rule.onNodeWithText("Coffee result").assertDoesNotExist()
        rule.onNodeWithText("tea").performClick()
        rule.onNodeWithText("Tea result").assertDoesNotExist()
        rule.runOnIdle { assertEquals("tea", query) }
        field.performImeAction()
        rule.onNodeWithText("Tea result").assertIsDisplayed()
    }
}
