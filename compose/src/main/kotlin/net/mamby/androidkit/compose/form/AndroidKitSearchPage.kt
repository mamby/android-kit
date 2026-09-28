package net.mamby.androidkit.compose.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

private const val MaximumRecentSearches = 10

/**
 * A local search page with Kit-owned matching, chrome and recents, and a host-owned result body.
 * Hosts own [query], [content], callbacks, navigation and persistence.
 * Item keys must be unique across the page; items sharing a group key must share its title.
 * Recent queries are recorded on IME submission or a result action, never while typing.
 * Render [content] with stable item keys and respect each item's enabled state. Calling a matched
 * item's onClick records the query before invoking the host callback; disabled callbacks do nothing.
 */
@Composable
public fun <T> AndroidKitSearchPage(
    items: List<AndroidKitSearchItem<T>>,
    query: String,
    onQueryChange: (String) -> Unit,
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    voiceInputEnabled: Boolean = true,
    content: LazyListScope.(matches: List<AndroidKitSearchItem<T>>) -> Unit,
): Unit {
    val itemSnapshot = items.toList()
    val validatedItems = remember(itemSnapshot) {
        require(itemSnapshot.map { it.key }.distinct().size == itemSnapshot.size) { "Search item keys must be unique." }
        require(itemSnapshot.mapNotNull { it.group }.groupBy { it.key }.values.all { groups ->
            groups.map { it.title }.distinct().size == 1
        }) { "Search items sharing a group key must share its title." }
        itemSnapshot
    }
    val matches = remember(validatedItems, query) { searchItems(validatedItems, query) }
    val strings = AndroidKitThemeTokens.strings
    AndroidKitSearchPage(
        query = query,
        onQueryChange = onQueryChange,
        recentQueries = recentQueries,
        onRecentQueriesChange = onRecentQueriesChange,
        title = strings.search,
        noMatchesMessage = strings.noMatchingResults,
        hasResults = matches.isNotEmpty(),
        modifier = modifier,
        onBack = onBack,
        listState = listState,
        voiceInputEnabled = voiceInputEnabled,
    ) { recordRecent ->
        content(matches.map { item ->
            item.copy(onClick = {
                if (item.enabled) {
                    recordRecent()
                    item.onClick()
                }
            })
        })
    }
}

private fun <T> searchItems(items: List<AndroidKitSearchItem<T>>, query: String): List<AndroidKitSearchItem<T>> {
    val tokens = searchQueryTokens(query)
    if (tokens.isEmpty()) return emptyList()
    return items.mapNotNull { item ->
        searchMatch(
            label = item.title,
            visibleText = listOfNotNull(item.title, item.supportingText),
            aliases = item.searchTerms + listOfNotNull(item.group?.title),
            tokens = tokens,
        )?.let { match -> item to match }
    }.sortedBy { it.second }.map { it.first }
}

/** Shared chrome also serves Settings' catalog matching and result controls. */
@Composable
internal fun AndroidKitSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    title: String,
    noMatchesMessage: String,
    hasResults: Boolean,
    modifier: Modifier,
    onBack: (() -> Unit)?,
    listState: LazyListState,
    voiceInputEnabled: Boolean = true,
    results: LazyListScope.(recordRecent: () -> Unit) -> Unit,
): Unit {
    val recents = recentQueries.sanitizedRecentSearchQueries()
    fun recordRecent() {
        val value = query.trim()
        if (value.isEmpty()) return
        val normalized = normalizeSearchText(value)
        onRecentQueriesChange(
            (listOf(value) + recents.filterNot { normalizeSearchText(it) == normalized })
                .take(MaximumRecentSearches),
        )
    }
    val strings = AndroidKitThemeTokens.strings
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    AndroidKitPage(
        title = title,
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = AndroidKitFloatingAction.Search(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { recordRecent() },
            voiceInputEnabled = voiceInputEnabled,
        ),
    ) { padding ->
        if (query.isBlank() && recents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .padding(horizontal = dimensions.screenPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spaceLarge),
                ) {
                    Box(
                        modifier = Modifier.size(dimensions.floatingActionButtonSize + dimensions.spaceLarge)
                            .background(AndroidKitThemeTokens.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AndroidKitIcons.Search,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.actionFlyoutIconSize),
                            tint = AndroidKitThemeTokens.colorScheme.onPrimaryContainer,
                        )
                    }
                    Text(
                        text = strings.noRecentSearches,
                        style = AndroidKitThemeTokens.typography.bodyLarge,
                        color = AndroidKitThemeTokens.settingSectionStyle.contentColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(direction) + dimensions.screenPadding,
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(direction) + dimensions.screenPadding,
                    bottom = padding.calculateBottomPadding() + dimensions.spaceMedium,
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.settingsPageSectionSpacing),
            ) {
                if (query.isBlank()) {
                    item(key = "recent-heading") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = strings.recentSearches,
                                style = AndroidKitThemeTokens.settingSectionStyle.sectionLabelTextStyle,
                                color = AndroidKitThemeTokens.settingSectionStyle.secondaryContentColor,
                            )
                            TextButton(
                                onClick = { onRecentQueriesChange(emptyList()) },
                                contentPadding = PaddingValues(
                                    horizontal = (dimensions.minimumTouchTarget - dimensions.floatingActionBarIconSize) / 2,
                                ),
                            ) { Text(strings.clearAll) }
                        }
                    }
                    item(key = "recent-list") {
                        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall)) {
                            recents.forEach { recent ->
                                RecentSearchRow(
                                    query = recent,
                                    onSelect = { onQueryChange(recent) },
                                    onRemove = {
                                        val removed = normalizeSearchText(recent)
                                        onRecentQueriesChange(recents.filterNot { normalizeSearchText(it) == removed })
                                    },
                                )
                            }
                        }
                    }
                } else if (!hasResults) {
                    item(key = "no-matches") { SearchEmptyMessage(noMatchesMessage) }
                } else {
                    results { recordRecent() }
                }
            }
        }
    }
}

private fun List<String>.sanitizedRecentSearchQueries(): List<String> {
    val normalized = mutableSetOf<String>()
    return map(String::trim)
        .filter(String::isNotEmpty)
        .filter { normalized.add(normalizeSearchText(it)) }
        .take(MaximumRecentSearches)
}

@Composable
private fun RecentSearchRow(query: String, onSelect: () -> Unit, onRemove: () -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect)
            .heightIn(min = dimensions.minimumTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
    ) {
        Text(
            text = query,
            modifier = Modifier.weight(1f),
            style = AndroidKitThemeTokens.typography.bodyMedium,
            color = AndroidKitThemeTokens.settingSectionStyle.contentColor,
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(dimensions.minimumTouchTarget)) {
            Icon(
                imageVector = AndroidKitIcons.Trash,
                contentDescription = strings.removeRecentSearch,
                modifier = Modifier.size(dimensions.floatingActionBarIconSize),
            )
        }
    }
}

@Composable
private fun SearchEmptyMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth().padding(AndroidKitThemeTokens.dimensions.spaceMedium),
        style = AndroidKitThemeTokens.settingSectionStyle.supportingTextStyle,
        color = AndroidKitThemeTokens.settingSectionStyle.secondaryContentColor,
    )
}
