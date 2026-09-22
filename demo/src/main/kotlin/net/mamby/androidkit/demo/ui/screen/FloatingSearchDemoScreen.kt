package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.form.AndroidKitBottomSheetScrollMode
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.navigation3.listDetailBackAction

@Composable
internal fun FloatingSearchDemoScreen(onBack: () -> Unit) {
    var pageQuery by rememberSaveable { mutableStateOf("") }
    var sheetQuery by rememberSaveable { mutableStateOf("") }
    var pageSubmitted by rememberSaveable { mutableStateOf<String?>(null) }
    var sheetSubmitted by rememberSaveable { mutableStateOf<String?>(null) }
    var sheetVisible by rememberSaveable { mutableStateOf(false) }
    val dimensions = AndroidKitThemeTokens.dimensions
    val examples = ComponentDemo.entries.map { demo ->
        demo to "${demo.component.catalogName} · ${stringResource(demo.titleResource)}"
    }

    AndroidKitPage(
        title = ComponentId.AndroidKitFloatingSearchBox.apiName,
        onBack = listDetailBackAction(onBack),
        floatingActionButton = AndroidKitFloatingAction.Search(
            query = pageQuery,
            enabled = !sheetVisible,
            onQueryChange = { pageQuery = it },
            onSearch = { pageSubmitted = it },
            modifier = Modifier.testTag("page_floating_search"),
        ),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = dimensions.screenPadding),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            item {
                Text(stringResource(R.string.floating_search_page), style = MaterialTheme.typography.titleMedium)
            }
            item { Text(stringResource(R.string.floating_search_hint)) }
            item {
                Button(
                    onClick = { sheetVisible = true },
                    modifier = Modifier.testTag("open_floating_search_sheet"),
                ) { Text(stringResource(R.string.open_floating_search_sheet)) }
            }
            searchResults(examples, pageQuery, pageSubmitted)
        }
    }

    AndroidKitBottomSheet(
        visible = sheetVisible,
        title = stringResource(R.string.floating_search_sheet),
        onDismiss = { sheetVisible = false },
        scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
        floatingAction = AndroidKitFloatingAction.Search(
            query = sheetQuery,
            onQueryChange = { sheetQuery = it },
            onSearch = { sheetSubmitted = it },
            modifier = Modifier.testTag("sheet_floating_search"),
        ),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            item { Text(stringResource(R.string.floating_search_hint)) }
            searchResults(examples, sheetQuery, sheetSubmitted)
        }
    }
}

private fun LazyListScope.searchResults(
    examples: List<Pair<ComponentDemo, String>>,
    query: String,
    submitted: String?,
) {
    if (submitted != null) {
        item { Text(stringResource(R.string.floating_search_submitted, submitted)) }
    }
    val matches = examples.filter { (_, label) -> label.contains(query.trim(), ignoreCase = true) }
    if (matches.isEmpty()) {
        item { Text(stringResource(R.string.floating_search_empty)) }
    }
    items(matches, key = { it.first.name }) { (_, label) ->
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
