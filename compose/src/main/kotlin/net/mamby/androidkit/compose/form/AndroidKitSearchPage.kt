package net.mamby.androidkit.compose.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitComponentTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface

private const val MaximumRecentSearches = 10

/**
 * A local search page with Kit-owned matching, chrome and recents, and a host-owned result body.
 * [searchMode] defaults to live matching. OnSubmit keeps the last submitted results while editing;
 * clearing resets results, and choosing history only fills the draft until IME submission.
 * Hosts own [query], [content], callbacks, navigation and persistence.
 * Item keys must be unique across the page; items sharing a group key must share its title.
 * Recent queries are recorded on IME submission or a result action, never while typing.
 * Hosts persist [searchHistoryEnabled] per logical search page. Disabling clears history and
 * stops recording. Persist enabled state and clearing together in [onSearchHistoryEnabledChange],
 * and reject history writes while disabled. Keep history disabled until the preference has loaded.
 * Opening the page focuses the input and requests the software keyboard once per entry.
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
    searchHistoryEnabled: Boolean,
    onSearchHistoryEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    voiceInputEnabled: Boolean = true,
    searchMode: AndroidKitSearchMode = AndroidKitSearchMode.Live,
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
    var submittedQuery by rememberSaveable(searchMode) { mutableStateOf("") }
    LaunchedEffect(query.isBlank()) {
        if (query.isBlank()) submittedQuery = ""
    }
    val resultsQuery = when (searchMode) {
        AndroidKitSearchMode.Live -> query
        AndroidKitSearchMode.OnSubmit -> submittedQuery.takeUnless { query.isBlank() }.orEmpty()
    }
    val documents = validatedItems.map { item ->
        SearchDocument(
            key = item.key,
            label = item.title,
            visibleText = listOfNotNull(item.title, item.supportingText),
            aliases = item.searchTerms + listOfNotNull(item.group?.title),
        )
    }
    val matchedIndices = rememberSearchMatches(documents, resultsQuery)
    val matches = matchedIndices.orEmpty().map { validatedItems[it.index] }
    val strings = AndroidKitThemeTokens.strings
    AndroidKitSearchPage(
        query = query,
        onQueryChange = {
            if (it.isBlank()) submittedQuery = ""
            onQueryChange(it)
        },
        resultsQuery = resultsQuery,
        searchMode = searchMode,
        onSearch = { submittedQuery = it },
        recentQueries = recentQueries,
        onRecentQueriesChange = onRecentQueriesChange,
        title = strings.search,
        noMatchesMessage = strings.noMatchingResults,
        hasResults = matches.isNotEmpty(),
        isSearching = matchedIndices == null,
        modifier = modifier,
        onBack = onBack,
        listState = listState,
        voiceInputEnabled = voiceInputEnabled,
        searchHistoryEnabled = searchHistoryEnabled,
        onSearchHistoryEnabledChange = onSearchHistoryEnabledChange,
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

/** Shared chrome also serves Settings' catalog matching and result controls. */
@Composable
internal fun AndroidKitSearchPage(
    query: String,
    onQueryChange: (String) -> Unit,
    recentQueries: List<String>,
    onRecentQueriesChange: (List<String>) -> Unit,
    searchHistoryEnabled: Boolean,
    onSearchHistoryEnabledChange: (Boolean) -> Unit,
    title: String,
    noMatchesMessage: String,
    hasResults: Boolean,
    modifier: Modifier,
    onBack: (() -> Unit)?,
    listState: LazyListState,
    voiceInputEnabled: Boolean = true,
    resultsQuery: String = query,
    searchMode: AndroidKitSearchMode = AndroidKitSearchMode.Live,
    onSearch: (String) -> Unit = {},
    isSearching: Boolean = false,
    onRecordRecent: ((String) -> Unit)? = null,
    onRemoveRecent: ((String) -> Unit)? = null,
    onClearRecent: (() -> Unit)? = null,
    results: LazyListScope.(recordRecent: () -> Unit) -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.searchPage
    val recents = recentQueries.sanitizedRecentSearchQueries()
    fun recordRecent(valueToRecord: String = resultsQuery) {
        if (!searchHistoryEnabled) return
        val value = valueToRecord.trim()
        if (value.isEmpty()) return
        if (onRecordRecent != null) {
            onRecordRecent(value)
            return
        }
        val normalized = normalizeSearchText(value)
        onRecentQueriesChange(
            (listOf(value) + recents.filterNot { normalizeSearchText(it) == normalized })
                .take(MaximumRecentSearches),
        )
    }
    val strings = AndroidKitThemeTokens.strings
    AndroidKitPage(
        title = title,
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = AndroidKitFloatingAction.Search(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onSubmit = { recordRecent(query) },
            searchMode = searchMode,
            label = strings.search,
            voiceInputEnabled = voiceInputEnabled,
            requestFocusOnOpen = true,
        ),
    ) { padding ->
        val showingHistory = resultsQuery.isBlank()
        // Read clearance during layout so measured floating controls and IME changes stay current.
        val pageContentPadding = remember(padding, tokens.horizontalPadding) {
            object : PaddingValues by padding {
                override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
                    padding.calculateLeftPadding(layoutDirection) + tokens.horizontalPadding
                override fun calculateRightPadding(layoutDirection: LayoutDirection) =
                    padding.calculateRightPadding(layoutDirection) + tokens.horizontalPadding
            }
        }
        val headingPlacementPadding = remember(pageContentPadding) {
            object : PaddingValues by pageContentPadding {
                override fun calculateBottomPadding() = 0.dp
            }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                if (showingHistory) {
                    Box(Modifier.padding(headingPlacementPadding)) {
                        RecentSearchHeading(
                            enabled = searchHistoryEnabled,
                            onEnabledChange = { enabled ->
                                if (!enabled && onClearRecent == null) onRecentQueriesChange(emptyList())
                                onSearchHistoryEnabledChange(enabled)
                            },
                            canClear = searchHistoryEnabled && recents.isNotEmpty(),
                            onClear = { onClearRecent?.invoke() ?: onRecentQueriesChange(emptyList()) },
                        )
                    }
                }
            },
        ) { headingPadding ->
            val bodyPadding = remember(pageContentPadding, headingPadding, showingHistory) {
                object : PaddingValues by pageContentPadding {
                    override fun calculateTopPadding() = if (showingHistory) {
                        headingPadding.calculateTopPadding()
                    } else {
                        pageContentPadding.calculateTopPadding()
                    }
                }
            }
            val listPadding = remember(bodyPadding, tokens.bottomPadding) {
                object : PaddingValues by bodyPadding {
                    override fun calculateBottomPadding() =
                        bodyPadding.calculateBottomPadding() + tokens.bottomPadding
                }
            }
            Box(Modifier.fillMaxSize()) {
                // Retain removal fades, but dispose outgoing rows immediately on disabling.
                key(searchHistoryEnabled) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        // Rows scroll behind both floating controls; the first and last rows clear them.
                        contentPadding = listPadding,
                        verticalArrangement = Arrangement.spacedBy(
                            if (resultsQuery.isBlank()) tokens.recentRowSpacing else tokens.resultSpacing,
                        ),
                    ) {
                        if (resultsQuery.isBlank() && searchHistoryEnabled) {
                            items(recents, key = { recent -> "recent:${normalizeSearchText(recent)}" }) { recent ->
                                Box(Modifier.animateItem(fadeInSpec = null)) {
                                    RecentSearchRow(
                                        query = recent,
                                        onSelect = {
                                            onQueryChange(recent)
                                            if (searchMode == AndroidKitSearchMode.Live) onSearch(recent)
                                        },
                                        onRemove = {
                                            if (onRemoveRecent != null) {
                                                onRemoveRecent(recent)
                                            } else {
                                                val removed = normalizeSearchText(recent)
                                                onRecentQueriesChange(recents.filterNot { normalizeSearchText(it) == removed })
                                            }
                                        },
                                    )
                                }
                            }
                        } else if (resultsQuery.isNotBlank() && !hasResults && !isSearching) {
                            item(key = "no-matches") { SearchEmptyMessage(noMatchesMessage) }
                        } else if (resultsQuery.isNotBlank() && !isSearching) {
                            results { recordRecent() }
                        }
                    }
                }
                if (resultsQuery.isNotBlank() && isSearching) {
                    Box(Modifier.fillMaxSize().padding(bodyPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                if (resultsQuery.isBlank() && (!searchHistoryEnabled || recents.isEmpty())) {
                    Box(Modifier.fillMaxSize().padding(bodyPadding), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(tokens.emptyContentSpacing),
                        ) {
                            Box(
                                modifier = Modifier.size(tokens.emptyIconContainerSize)
                                    .background(AndroidKitThemeTokens.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (searchHistoryEnabled) AndroidKitIcons.Search else AndroidKitIcons.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(tokens.emptyIconSize),
                                    tint = AndroidKitThemeTokens.colorScheme.onPrimaryContainer,
                                )
                            }
                            Text(
                                text = if (searchHistoryEnabled) strings.noRecentSearches else strings.searchHistoryDisabled,
                                style = tokens.emptyTextStyle,
                                color = tokens.emptyContentColor,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSearchHeading(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    canClear: Boolean,
    onClear: () -> Unit,
) {
    AndroidKitComponentTheme {
        val tokens = AndroidKitThemeTokens.componentTokens.searchPage
        val dimensions = AndroidKitThemeTokens.dimensions
        val strings = AndroidKitThemeTokens.strings
        FloatingSurface(
            shape = tokens.headingShape,
            modifier = Modifier.fillMaxWidth().padding(bottom = tokens.headingBottomSpacing),
            style = AndroidKitThemeTokens.floatingSurfaceStyle,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = tokens.headingHorizontalPadding,
                    vertical = tokens.headingVerticalPadding,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = strings.recentSearches,
                            style = tokens.headingTextStyle,
                            color = tokens.secondaryContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = strings.disablingSearchHistoryClears,
                            style = tokens.headingSupportingTextStyle,
                            color = tokens.secondaryContentColor,
                        )
                    }
                    val historyAction = if (enabled) strings.disableSearchHistory else strings.enableSearchHistory
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                        modifier = Modifier.semantics { contentDescription = historyAction },
                    )
                }
                if (canClear) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(
                            start = (dimensions.minimumTouchTarget - tokens.headingIconSize) / 2,
                            end = tokens.clearButtonEndPadding,
                        ),
                    ) { Text(strings.clearAll, maxLines = 1) }
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
    AndroidKitComponentTheme {
        val tokens = AndroidKitThemeTokens.componentTokens.searchPage
        val dimensions = AndroidKitThemeTokens.dimensions
        val strings = AndroidKitThemeTokens.strings
        AndroidKitCard(
            onClick = onSelect,
            style = tokens.recentCardStyle,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = tokens.recentStartPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = dimensions.minimumTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AndroidKitIcons.History,
                    contentDescription = null,
                    modifier = Modifier.padding(end = tokens.recentIconSpacing)
                        .size(tokens.recentIconSize),
                    tint = tokens.secondaryContentColor,
                )
                Text(
                    text = query,
                    modifier = Modifier.weight(1f).padding(end = tokens.recentTextEndPadding),
                    style = tokens.recentTextStyle,
                    color = tokens.recentContentColor,
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(dimensions.minimumTouchTarget)) {
                    Icon(
                        imageVector = AndroidKitIcons.Close,
                        contentDescription = strings.removeRecentSearch,
                        modifier = Modifier.size(tokens.recentIconSize),
                        tint = tokens.secondaryContentColor,
                    )
                }
            }
        }

    }
}

@Composable
private fun SearchEmptyMessage(message: String) {
    AndroidKitComponentTheme {
        val tokens = AndroidKitThemeTokens.componentTokens.searchPage
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth().padding(tokens.emptyMessagePadding),
            style = tokens.emptyMessageTextStyle,
            color = tokens.secondaryContentColor,
        )

    }
}
