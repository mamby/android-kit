package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitSearchGroup
import net.mamby.androidkit.compose.form.AndroidKitSearchItem
import net.mamby.androidkit.compose.form.AndroidKitSearchPage
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.navigation3.listDetailBackAction

@Composable
internal fun SearchPageDemoScreen(
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    onOpenDemo: (ComponentDemo) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val dimensions = AndroidKitThemeTokens.dimensions
    val catalogItems = ComponentDemo.entries
        .filter { it.component != ComponentId.AndroidKitSearchPage }
        .map { demo ->
            AndroidKitSearchItem(
                key = demo.name,
                title = stringResource(demo.titleResource),
                data = demo,
                onClick = { onOpenDemo(demo) },
                supportingText = demo.component.apiName,
                group = AndroidKitSearchGroup(demo.component.name, demo.component.catalogName),
            )
        }
    AndroidKitSearchPage(
        items = catalogItems,
        query = query,
        onQueryChange = { query = it },
        recentQueries = recentQueries,
        onRecentQueriesChange = onRecentQueriesChange,
        onBack = listDetailBackAction(onBack),
        modifier = Modifier.testTag("search_page_demo"),
    ) { matches ->
        matches.groupBy { it.group }.forEach { (group, groupItems) ->
            item(key = "group:${group?.key}") {
                Text(group?.title.orEmpty(), style = MaterialTheme.typography.titleMedium)
            }
            items(groupItems, key = { it.key }) { match ->
                Card(
                    onClick = match.onClick,
                    enabled = match.enabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                    ) {
                        Text(match.title, style = MaterialTheme.typography.titleMedium)
                        Text(match.data.component.apiName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
