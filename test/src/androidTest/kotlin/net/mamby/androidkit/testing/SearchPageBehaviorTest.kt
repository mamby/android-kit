package net.mamby.androidkit.testing

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import java.util.concurrent.atomic.AtomicBoolean
import net.mamby.androidkit.compose.form.AndroidKitSearchGroup
import net.mamby.androidkit.compose.form.AndroidKitSearchItem
import net.mamby.androidkit.compose.form.AndroidKitSearchPage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchPageBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun keepTestActivityScreenOn() {
        rule.activityRule.scenario.onActivity {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun historyAndResultsScrollBehindSearchAndKeepLastRowAboveIt() {
        rule.activityRule.scenario.onActivity { it.enableEdgeToEdge() }
        var query by mutableStateOf("")
        val imeVisible = AtomicBoolean()
        rule.setContent {
            val visible = WindowInsets.isImeVisible
            SideEffect { imeVisible.set(visible) }
            TestKitTheme {
                AndroidKitSearchPage(
                    items = (0 until 30).map { topicItem("topic-$it", "Topic $it", "Topic $it") },
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = (0 until 10).map { "Recent $it" },
                    onRecentQueriesChange = {},
                    searchHistoryEnabled = true,
                    onSearchHistoryEnabledChange = {},
                    listState = rememberLazyListState(),
                    voiceInputEnabled = false,
                ) { matches -> hostResults(matches) }
            }
        }
        rule.waitUntil(5_000) { imeVisible.get() }
        val search = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        val list = rule.onNode(hasScrollToIndexAction())
        fun assertViewportAndLastRow(lastRow: String, lastIndex: Int) {
            list.performScrollToIndex(lastIndex)
            val fieldBounds = search.fetchSemanticsNode().boundsInRoot
            val viewportBounds = list.fetchSemanticsNode().boundsInRoot
            assertTrue("List viewport must extend behind the search box", viewportBounds.bottom > fieldBounds.bottom)
            val rowBounds = rule.onNodeWithText(lastRow).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue("Last row must scroll clear of the search box", rowBounds.bottom <= fieldBounds.top)
        }
        val headingTop = rule.onNodeWithText("Recent searches").fetchSemanticsNode().boundsInRoot.top
        val toggleBounds = rule.onNodeWithContentDescription("Disable search history").fetchSemanticsNode().boundsInRoot
        assertTrue("History viewport must extend behind the recent header", list.fetchSemanticsNode().boundsInRoot.top < headingTop)
        assertTrue("First recent row must initially clear the header", rule.onNodeWithText("Recent 0").fetchSemanticsNode().boundsInRoot.top >= toggleBounds.bottom)
        val title = rule.onNode(hasContentDescription("Search") and !hasSetTextAction())
        val historyTitleBounds = title.fetchSemanticsNode().boundsInRoot
        val pageBounds = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val historyViewportBounds = list.fetchSemanticsNode().boundsInRoot
        assertEquals("History viewport must reach the page's top edge", pageBounds.top, historyViewportBounds.top, 0f)
        assertEquals("History viewport must reach the page's left edge", pageBounds.left, historyViewportBounds.left, 0f)
        assertEquals("History viewport must reach the page's right edge", pageBounds.right, historyViewportBounds.right, 0f)
        list.performScrollToIndex(1)
        // The target row aligns below content padding; the preceding row moves under the bar.
        assertTrue("Recent rows must scroll behind the header", rule.onNodeWithText("Recent 0").assertIsDisplayed().fetchSemanticsNode().boundsInRoot.top < toggleBounds.bottom)
        assertViewportAndLastRow("Recent 9", 9)
        assertEquals(headingTop, rule.onNodeWithText("Recent searches").fetchSemanticsNode().boundsInRoot.top)
        assertEquals(historyTitleBounds, title.fetchSemanticsNode().boundsInRoot)
        search.performTextReplacement("topic")
        rule.waitUntil(5_000) { rule.onAllNodes(hasText("Event: Topic", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        list.performScrollToIndex(0)
        rule.onNodeWithText("Event: Topic 0").assertIsDisplayed()
        val titleBounds = rule.onNode(hasContentDescription("Search") and !hasSetTextAction()).fetchSemanticsNode().boundsInRoot
        assertEquals("Results must use the same edge-to-edge viewport", historyViewportBounds, list.fetchSemanticsNode().boundsInRoot)
        assertTrue("Result viewport must extend behind the title bar", list.fetchSemanticsNode().boundsInRoot.top < titleBounds.bottom)
        assertTrue("First result must clear the title bar", rule.onNodeWithText("Event: Topic 0").fetchSemanticsNode().boundsInRoot.top >= titleBounds.bottom)
        assertViewportAndLastRow("Event: Topic 29", 29)
        pressBack()
        rule.waitUntil(5_000) { !imeVisible.get() }
        assertViewportAndLastRow("Event: Topic 29", 29)
    }

    @Test
    fun openingAndReopeningFocusesInputWithoutReopeningDismissedKeyboardOnUpdates() {
        var recentHistoryVisible by mutableStateOf(true)
        var shown by mutableStateOf(true)
        var query by mutableStateOf("coffee")
        var recents by mutableStateOf(emptyList<String>())
        val imeVisible = AtomicBoolean()
        rule.setContent {
            val visible = WindowInsets.isImeVisible
            SideEffect { imeVisible.set(visible) }
            TestKitTheme {
                if (shown) {
                    AndroidKitSearchPage(
                        items = listOf(topicItem("coffee", "Coffee", "Coffee outing")),
                        query = query,
                        onQueryChange = { query = it },
                        recentQueries = recents,
                        onRecentQueriesChange = { recents = it },
                        searchHistoryEnabled = recentHistoryVisible,
                        onSearchHistoryEnabledChange = { recentHistoryVisible = it },
                        voiceInputEnabled = false,
                    ) { matches -> hostResults(matches) }
                }
            }
        }
        val search = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        rule.waitUntil(5_000) {
            imeVisible.get() && search.fetchSemanticsNode().config[SemanticsProperties.Focused]
        }
        search.assertIsFocused().assertTextEquals("coffee")

        pressBack()
        rule.waitUntil(5_000) { !imeVisible.get() }
        search.assertIsNotFocused()
        rule.runOnIdle {
            query = "tea"
            recents = listOf("Earlier")
        }
        search.assertIsNotFocused().assertTextEquals("tea")
        rule.runOnIdle {
            assertFalse(imeVisible.get())
            shown = false
        }
        search.assertDoesNotExist()
        rule.runOnIdle { shown = true }
        rule.waitUntil(5_000) {
            imeVisible.get() && search.fetchSemanticsNode().config[SemanticsProperties.Focused]
        }
        search.assertIsFocused().assertTextEquals("tea")
        rule.runOnIdle { assertEquals(listOf("Earlier"), recents) }
    }

    @Test
    fun hostResultsReceiveTypedDataInRelevanceOrder() {
        var recentHistoryVisible by mutableStateOf(true)
        val items = listOf(
            topicItem("alias", "Appointment", "Alias", aliases = listOf("clinic")),
            topicItem("prefix", "Clinic visits", "Prefix"),
            topicItem("visible", "Appointment", "Visible", supportingText = "At the clinic"),
            topicItem("exact", "Clinic", "Exact"),
            topicItem("alias-second", "Appointment", "Alias second", aliases = listOf("clinic")),
        )
        rule.setContent {
            TestKitTheme {
                AndroidKitSearchPage(items, "clinic", {}, emptyList(), {}, recentHistoryVisible, { recentHistoryVisible = it }, voiceInputEnabled = false) { matches ->
                    item(key = "order") { Text(matches.joinToString { it.data.value }) }
                    hostResults(matches)
                }
            }
        }
        rule.onNodeWithText("Exact, Prefix, Visible, Alias, Alias second").assertIsDisplayed()
        rule.onNodeWithText("Event: Exact").assertIsDisplayed()
        rule.onNodeWithText("Clinic visits").assertDoesNotExist()
    }

    @Test
    fun matchingUsesEveryTokenContextAndAliasesAndTracksChangingHostData() {
        var recentHistoryVisible by mutableStateOf(true)
        var query by mutableStateOf("")
        var recents by mutableStateOf(emptyList<String>())
        var clicks = 0
        val items = mutableStateListOf(
            topicItem(
                key = "cafe",
                title = "Café walk",
                value = "Café",
                supportingText = "Saturday in Paris",
                group = AndroidKitSearchGroup("events", "Events"),
                aliases = listOf("balade"),
                onClick = { clicks++ },
            ),
        )
        rule.setContent {
            TestKitTheme {
                AndroidKitSearchPage(items, query, { query = it }, recents, { recents = it }, recentHistoryVisible, { recentHistoryVisible = it },
                    voiceInputEnabled = false) { matches -> hostResults(matches) }
            }
        }
        val search = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        search.performTextReplacement("  CAFÉ, PARIS  ")
        rule.onNodeWithText("Event: Café").assertIsDisplayed().performClick()
        rule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(listOf("CAFÉ, PARIS"), recents)
            query = "balade events"
        }
        search.assertTextEquals("balade events")
        rule.onNodeWithText("Event: Café").assertIsDisplayed()
        rule.runOnIdle {
            items[0] = items[0].copy(data = SearchTopic("Event", "Closed"), enabled = false)
        }
        rule.onNodeWithText("Event: Café").assertDoesNotExist()
        rule.onNodeWithText("Event: Closed").assertIsNotEnabled()
        search.performTextReplacement("balade missing")
        rule.onNodeWithText("No matching results").assertIsDisplayed()
        search.performTextReplacement("🔎")
        rule.onNodeWithText("No matching results").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(listOf("CAFÉ, PARIS"), recents)
            query = "balade events"
        }
        rule.onNodeWithText("Event: Closed").assertIsNotEnabled()
        rule.runOnIdle { items.clear() }
        rule.onNodeWithText("Event: Closed").assertDoesNotExist()
        rule.onNodeWithText("No matching results").assertIsDisplayed()
    }

    @Test
    fun resultActionsAndSubmissionShareControlledRecentHistory() {
        var recentHistoryVisible by mutableStateOf(true)
        var query by mutableStateOf("")
        var recents by mutableStateOf((0 until 10).map { "Old $it" })
        rule.setContent {
            TestKitTheme {
                AndroidKitSearchPage(
                    items = listOf(topicItem("alpha", "Alpha", "Alpha")),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = recents,
                    onRecentQueriesChange = { recents = it },
                    searchHistoryEnabled = recentHistoryVisible,
                    onSearchHistoryEnabledChange = { recentHistoryVisible = it },
                    voiceInputEnabled = false,
                ) { matches -> hostResults(matches) }
            }
        }
        val search = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        search.performTextReplacement("alpha")
        rule.onNodeWithText("Event: Alpha").performClick()
        search.performTextReplacement("ÁLPHA")
        rule.onNodeWithText("Event: Alpha").performClick()
        rule.runOnIdle {
            assertEquals(10, recents.size)
            assertEquals("ÁLPHA", recents.first())
            assertEquals(1, recents.count { it.equals("ÁLPHA", ignoreCase = true) || it == "alpha" })
        }
        search.performTextReplacement("nothing here")
        search.performImeAction()
        rule.onNodeWithText("No matching results").assertIsDisplayed()
        rule.runOnIdle { assertEquals("nothing here", recents.first()) }

        search.performTextReplacement("")
        val beforeRestore = recents
        rule.onNodeWithText("nothing here").performClick()
        search.assertTextEquals("nothing here")
        rule.runOnIdle { assertEquals(beforeRestore, recents) }

        search.performTextReplacement("")
        rule.onAllNodesWithContentDescription("Remove recent search")[0].performClick()
        rule.runOnIdle { assertEquals("ÁLPHA", recents.first()) }
        rule.onNodeWithText("Clear all").performClick()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        rule.runOnIdle { assertTrue(recents.isEmpty()) }
    }

    @Test
    fun hostSavedQueryRestoresItsResults() {
        var recentHistoryVisible by mutableStateOf(true)
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            var query by rememberSaveable { mutableStateOf("") }
            TestKitTheme {
                AndroidKitSearchPage(
                    items = listOf(topicItem("coffee", "Coffee", "Coffee outing")),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = emptyList(),
                    onRecentQueriesChange = {},
                    searchHistoryEnabled = recentHistoryVisible,
                    onSearchHistoryEnabledChange = { recentHistoryVisible = it },
                    voiceInputEnabled = false,
                ) { matches -> hostResults(matches) }
            }
        }
        rule.onNode(hasContentDescription("Search") and hasSetTextAction()).performTextReplacement("coffee")
        rule.onNodeWithText("Event: Coffee outing").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNode(hasContentDescription("Search") and hasSetTextAction()).assertTextEquals("coffee")
        rule.onNodeWithText("Event: Coffee outing").assertIsDisplayed()
    }
    @Test
    fun disablingClearsAnimatedHistoryImmediatelyAndStopsRecording() {
        var query by mutableStateOf("")
        var recents by mutableStateOf(listOf("Sensitive query"))
        var visible by mutableStateOf(true)
        rule.setContent {
            TestKitTheme {
                AndroidKitSearchPage(
                    items = listOf(topicItem("alpha", "Alpha", "Alpha")),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = recents,
                    onRecentQueriesChange = { recents = it },
                    searchHistoryEnabled = visible,
                    onSearchHistoryEnabledChange = { visible = it },
                    voiceInputEnabled = false,
                ) { matches -> hostResults(matches) }
            }
        }
        rule.onNodeWithText("Sensitive query").assertIsDisplayed()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithContentDescription("Disable search history").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
        rule.onNodeWithText("Sensitive query").assertDoesNotExist()
        rule.onAllNodesWithContentDescription("Remove recent search").assertCountEquals(0)
        rule.mainClock.autoAdvance = true
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        rule.runOnIdle { assertEquals(emptyList<String>(), recents) }

        val search = rule.onNode(hasContentDescription("Search") and hasSetTextAction())
        search.performTextReplacement("alpha")
        rule.onNodeWithText("Event: Alpha").performClick()
        search.performTextReplacement("Submitted privately")
        search.performImeAction()
        search.performTextReplacement("")
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        rule.onNodeWithText("Submitted privately").assertDoesNotExist()
        rule.runOnIdle { assertEquals(emptyList<String>(), recents) }

        rule.onNodeWithContentDescription("Enable search history").performClick()
        rule.onNodeWithText("Sensitive query").assertDoesNotExist()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        search.performTextReplacement("alpha")
        search.performImeAction()
        search.performTextReplacement("")
        rule.runOnIdle { assertEquals(listOf("alpha"), recents) }
        rule.onNodeWithText("Clear all").performClick()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        rule.onNodeWithContentDescription("Disable search history").performClick()
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        rule.onNodeWithContentDescription("Enable search history").performClick()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
    }

    @Test
    fun historyEnabledStateRestoresIndependentlyForEachSearchPage() {
        val restoration = StateRestorationTester(rule)
        var secondPage by mutableStateOf(false)
        restoration.setContent {
            var firstVisible by rememberSaveable { mutableStateOf(false) }
            var secondVisible by rememberSaveable { mutableStateOf(true) }
            var secondRecents by rememberSaveable { mutableStateOf(listOf("Second history")) }
            TestKitTheme {
                AndroidKitSearchPage(
                    items = emptyList<AndroidKitSearchItem<SearchTopic>>(),
                    query = "",
                    onQueryChange = {},
                    recentQueries = if (secondPage) secondRecents else emptyList(),
                    onRecentQueriesChange = { if (secondPage) secondRecents = it },
                    searchHistoryEnabled = if (secondPage) secondVisible else firstVisible,
                    onSearchHistoryEnabledChange = { if (secondPage) secondVisible = it else firstVisible = it },
                    voiceInputEnabled = false,
                ) { }
            }
        }
        rule.onNodeWithText("First history").assertDoesNotExist()
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
        rule.runOnIdle { secondPage = true }
        rule.onNodeWithText("Second history").assertIsDisplayed()
        rule.onNodeWithContentDescription("Disable search history").performClick()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithText("Second history").assertDoesNotExist()
        rule.onNodeWithContentDescription("Enable search history").performClick()
        rule.onNodeWithText("Second history").assertDoesNotExist()
        rule.onNodeWithText("No recent searches").assertIsDisplayed()
        rule.runOnIdle { secondPage = false }
        rule.onNodeWithText("First history").assertDoesNotExist()
        rule.onNodeWithText("Search history is disabled").assertIsDisplayed()
    }

}

private data class SearchTopic(val topic: String, val value: String)

private fun topicItem(
    key: String,
    title: String,
    value: String,
    supportingText: String? = null,
    group: AndroidKitSearchGroup? = null,
    aliases: List<String> = emptyList(),
    onClick: () -> Unit = {},
): AndroidKitSearchItem<SearchTopic> = AndroidKitSearchItem(
    key = key,
    title = title,
    data = SearchTopic("Event", value),
    onClick = onClick,
    supportingText = supportingText,
    group = group,
    searchTerms = aliases,
)

private fun LazyListScope.hostResults(matches: List<AndroidKitSearchItem<SearchTopic>>) {
    items(matches, key = { it.key }) { match ->
        Button(onClick = match.onClick, enabled = match.enabled) {
            Text("${match.data.topic}: ${match.data.value}")
        }
    }
}
