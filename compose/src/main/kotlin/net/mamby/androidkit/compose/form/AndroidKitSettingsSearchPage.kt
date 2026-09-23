package net.mamby.androidkit.compose.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import net.mamby.androidkit.compose.action.AndroidKitFloatingAction
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

private const val MaximumRecentSettingsSearches = 10

/** Global Settings search rendered from the same catalog as every Settings page. */
@Composable
public fun AndroidKitSettingsSearchPage(
    catalog: AndroidKitSettingsCatalog,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
): Unit {
    var query by rememberSaveable { mutableStateOf("") }
    var activePicker by rememberSaveable { mutableStateOf<String?>(null) }
    val strings = AndroidKitThemeTokens.strings
    val lexicon = rememberSettingsSearchLexicon()
    val collected = collectCatalogSearchSections(catalog) { activePicker = it }
    val recentQueries = catalog.search.recentQueries.sanitizedRecentSettingsQueries()
    val matches = remember(collected.sections, query, lexicon) {
        searchSettings(collected.sections, query, lexicon)
    }
    fun recordRecent() {
        val value = query.trim()
        if (value.isEmpty()) return
        val normalized = normalizeSettingsSearchText(value)
        val updated = buildList {
            add(value)
            recentQueries.forEach { existing ->
                if (normalizeSettingsSearchText(existing) != normalized) add(existing)
            }
        }.take(MaximumRecentSettingsSearches)
        catalog.search.onRecentQueriesChange(updated)
    }
    val dimensions = AndroidKitThemeTokens.dimensions
    val direction = LocalLayoutDirection.current
    AndroidKitPage(
        title = strings.searchSettings,
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = AndroidKitFloatingAction.Search(
            query = query,
            onQueryChange = { query = it },
            onSearch = { recordRecent() },
        ),
    ) { padding ->
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
                        Text(strings.recentSearches,
                            style = AndroidKitThemeTokens.settingSectionStyle.sectionLabelTextStyle)
                        if (recentQueries.isNotEmpty()) {
                            TextButton(onClick = { catalog.search.onRecentQueriesChange(emptyList()) }) {
                                Text(strings.clearAll)
                            }
                        }
                    }
                }
                if (recentQueries.isEmpty()) {
                    item(key = "no-recents") { SearchEmptyMessage(strings.noRecentSearches) }
                } else {
                    items(recentQueries, key = { "recent:$it" }) { recent ->
                        RecentSearchRow(
                            query = recent,
                            onSelect = { query = recent },
                            onRemove = {
                                val removed = normalizeSettingsSearchText(recent)
                                catalog.search.onRecentQueriesChange(
                                    recentQueries.filterNot {
                                        normalizeSettingsSearchText(it) == removed
                                    },
                                )
                            },
                        )
                    }
                }
            } else if (matches.isEmpty()) {
                item(key = "no-matches") { SearchEmptyMessage(strings.noMatchingSettings) }
            } else {
                matches.forEach { result ->
                    item(key = "result:${result.pageKey}:${result.section.key}") {
                        SettingsSection(
                            entries = result.entries,
                            label = result.contextLabel,
                            onEntryAction = { recordRecent() },
                        )
                    }
                }
            }
        }
    }
    val availablePickerKeys = collected.scopes.flatMap { scope ->
        scope.pickers.keys + scope.timeoutPickers.keys
    }
    LaunchedEffect(activePicker, availablePickerKeys) {
        if (activePicker != null && activePicker !in availablePickerKeys) activePicker = null
    }
    collected.scopes.forEach { scope ->
        if (activePicker in scope.pickers || activePicker in scope.timeoutPickers) {
            RenderSettingsPicker(scope, activePicker) { activePicker = null }
        }
    }
}

private fun List<String>.sanitizedRecentSettingsQueries(): List<String> {
    val normalized = mutableSetOf<String>()
    return map(String::trim)
        .filter(String::isNotEmpty)
        .filter { normalized.add(normalizeSettingsSearchText(it)) }
        .take(MaximumRecentSettingsSearches)
}

@Composable
private fun RecentSearchRow(query: String, onSelect: () -> Unit, onRemove: () -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onSelect)
            .heightIn(min = dimensions.minimumTouchTarget)
            .padding(start = dimensions.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
    ) {
        Icon(AndroidKitIcons.History, contentDescription = null)
        Text(query, modifier = Modifier.weight(1f),
            style = AndroidKitThemeTokens.settingSectionStyle.entryLabelTextStyle)
        IconButton(onClick = onRemove) {
            Icon(AndroidKitIcons.Trash, contentDescription = strings.removeRecentSearch)
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

private data class CollectedSearchSections(
    val sections: List<SearchableSettingsSection>,
    val scopes: List<SettingsPageScopeImpl>,
)

private data class SearchableSettingsSection(
    val pageKey: String,
    val pageTitle: String,
    val pageTerms: AndroidKitSettingsSearchTerms?,
    val section: SettingsRenderedSection,
    val order: Int,
)

private data class SettingsSearchResult(
    val pageKey: String,
    val section: SettingsRenderedSection,
    val entries: List<SettingsEntryDefinition>,
    val contextLabel: String,
    val score: Int,
    val order: Int,
)

@Composable
private fun collectCatalogSearchSections(
    catalog: AndroidKitSettingsCatalog,
    openPicker: (String) -> Unit,
): CollectedSearchSections {
    val strings = AndroidKitThemeTokens.strings
    val sections = mutableListOf<SearchableSettingsSection>()
    val scopes = mutableListOf<SettingsPageScopeImpl>()
    var order = 0
    catalog.pages.forEach { page ->
        val pageTitle = page.title ?: strings.about
        when (page) {
            is AndroidKitSettingsCatalogPage.Main -> {
                val scope = SettingsPageScopeImpl(page.key, strings, openPicker)
                AndroidKitSettingsPageScope().apply(page.content).render(scope)
                scopes += scope
                scope.items.forEach { sections += SearchableSettingsSection(page.key, pageTitle,
                    page.searchTerms, it, order++) }
            }
            is AndroidKitSettingsCatalogPage.Subpage -> {
                val scope = SettingsPageScopeImpl(page.key, strings, openPicker)
                AndroidKitSettingsPageScope().apply(page.content).render(scope)
                scopes += scope
                scope.items.forEach { sections += SearchableSettingsSection(page.key, pageTitle,
                    page.searchTerms, it, order++) }
            }
            is AndroidKitSettingsCatalogPage.About -> settingsAboutSections(page.content).forEach {
                sections += SearchableSettingsSection(page.key, pageTitle, null, it, order++)
            }
        }
    }
    return CollectedSearchSections(sections, scopes)
}

private fun searchSettings(
    sections: List<SearchableSettingsSection>,
    query: String,
    lexicon: SettingsSearchLexicon,
): List<SettingsSearchResult> {
    val tokens = normalizeSettingsSearchText(query).split(' ').filter(String::isNotBlank)
    if (tokens.isEmpty()) return emptyList()
    return sections.mapNotNull { source ->
        val matches = source.section.entries.mapIndexedNotNull { index, entry ->
            if (!entry.searchable) return@mapIndexedNotNull null
            val label = normalizeSettingsSearchText(entry.label)
            val direct = listOfNotNull(
                entry.label,
                entry.supportingText,
                (entry as? SettingsEntryDefinition.Info)?.value,
                (entry as? SettingsEntryDefinition.Slider)?.valueLabel,
            )
            val aliases = buildList {
                add(source.pageTitle)
                source.section.label?.let(::add)
                source.section.description?.let(::add)
                source.pageTerms?.expandedTerms(lexicon)?.let(::addAll)
                source.section.searchTerms?.expandedTerms(lexicon)?.let(::addAll)
                entry.searchTerms?.expandedTerms(lexicon)?.let(::addAll)
            }
            val directText = normalizeSettingsSearchText(direct.joinToString(" "))
            val allText = normalizeSettingsSearchText((direct + aliases).joinToString(" "))
            if (!tokens.all(allText::contains)) return@mapIndexedNotNull null
            val normalizedQuery = tokens.joinToString(" ")
            val score = when {
                label == normalizedQuery -> 0
                label.startsWith(normalizedQuery) -> 1
                label.contains(normalizedQuery) -> 2
                tokens.all(directText::contains) -> 3
                else -> 4
            }
            Triple(entry, score, index)
        }.sortedWith(compareBy<Triple<SettingsEntryDefinition, Int, Int>> { it.second }.thenBy { it.third })
        if (matches.isEmpty()) null else SettingsSearchResult(
            pageKey = source.pageKey,
            section = source.section,
            entries = matches.map { it.first },
            contextLabel = listOfNotNull(source.pageTitle, source.section.label)
                .distinct().joinToString(" · "),
            score = matches.minOf { it.second },
            order = source.order,
        )
    }.sortedWith(compareBy<SettingsSearchResult> { it.score }.thenBy { it.order })
}
