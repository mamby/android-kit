package net.mamby.androidkit.testing

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import net.mamby.androidkit.compose.form.AndroidKitSearchItem
import net.mamby.androidkit.compose.form.AndroidKitSearchPage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchAsyncBehaviorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun datasetAndQueryChangesUseCurrentResultsAvailabilityAndCallbacks() {
        var query by mutableStateOf("Old topic")
        var updated by mutableStateOf(false)
        var enabled by mutableStateOf(false)
        val clicks = mutableListOf<String>()
        rule.setContent {
            TestKitTheme {
                val target = if (updated) "New topic" else "Old topic"
                val callbackVersion = if (enabled) "current" else "outgoing"
                AndroidKitSearchPage(
                    items = listOf(AndroidKitSearchItem(
                        key = "target", title = target, data = target,
                        enabled = enabled,
                        onClick = { clicks += callbackVersion },
                    )) + (0 until 2_000).map {
                        AndroidKitSearchItem("other-$it", "Other $it", "Other $it", {})
                    },
                    query = query,
                    onQueryChange = { query = it },
                    recentQueries = emptyList(),
                    onRecentQueriesChange = {},
                    recentQueriesVisible = false,
                    onRecentQueriesVisibleChange = {},
                    voiceInputEnabled = false,
                ) { matches ->
                    items(matches, key = { it.key }) { match ->
                        Button(onClick = match.onClick, enabled = match.enabled) {
                            Text("Result: ${match.data}")
                        }
                    }
                }
            }
        }
        rule.runOnIdle {
            updated = true
            query = "New topic"
        }
        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("Result: New topic")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Result: Old topic").assertDoesNotExist()
        rule.onNodeWithText("Result: New topic").assertIsNotEnabled()
        rule.runOnIdle { enabled = true }
        rule.onNodeWithText("Result: New topic").performClick()
        rule.runOnIdle { assertEquals(listOf("current"), clicks) }
    }
}
