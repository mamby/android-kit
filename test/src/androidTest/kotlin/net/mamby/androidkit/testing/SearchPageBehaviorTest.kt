package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import net.mamby.androidkit.compose.form.AndroidKitSearchGroup
import net.mamby.androidkit.compose.form.AndroidKitSearchItem
import net.mamby.androidkit.compose.form.AndroidKitSearchPage
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchPageBehaviorTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun hostResultsReceiveTypedDataInRelevanceOrder() {
        val items = listOf(
            topicItem("alias", "Appointment", "Alias", aliases = listOf("clinic")),
            topicItem("prefix", "Clinic visits", "Prefix"),
            topicItem("visible", "Appointment", "Visible", supportingText = "At the clinic"),
            topicItem("exact", "Clinic", "Exact"),
            topicItem("alias-second", "Appointment", "Alias second", aliases = listOf("clinic")),
        )
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSearchPage(items, "clinic", {}, emptyList(), {}, voiceInputEnabled = false) { matches ->
                    hostResults(matches)
                }
            }
        }
        val positions = listOf("Exact", "Prefix", "Visible", "Alias", "Alias second").map { value ->
            rule.onNodeWithText("Event: $value").fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue(positions.zipWithNext().all { (first, second) -> first < second })
        rule.onNodeWithText("Clinic visits").assertDoesNotExist()
    }

    @Test
    fun matchingUsesEveryTokenContextAndAliasesAndTracksChangingHostData() {
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
            AndroidKitTheme {
                AndroidKitSearchPage(items, query, { query = it }, recents, { recents = it },
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
        var query by mutableStateOf("")
        var recents by mutableStateOf((0 until 10).map { "Old $it" })
        rule.setContent {
            AndroidKitTheme {
                AndroidKitSearchPage(
                    items = listOf(topicItem("alpha", "Alpha", "Alpha")),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = recents,
                    onRecentQueriesChange = { recents = it },
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
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            var query by rememberSaveable { mutableStateOf("") }
            AndroidKitTheme {
                AndroidKitSearchPage(
                    items = listOf(topicItem("coffee", "Coffee", "Coffee outing")),
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = emptyList(),
                    onRecentQueriesChange = {},
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
