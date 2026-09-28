package net.mamby.androidkit.compose.form

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

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
    val matches = remember(collected.sections, query, lexicon) {
        searchSettings(collected.sections, query, lexicon)
    }
    AndroidKitSearchPage(
        query = query,
        onQueryChange = { query = it },
        recentQueries = catalog.search.recentQueries,
        onRecentQueriesChange = catalog.search.onRecentQueriesChange,
        title = strings.searchSettings,
        noMatchesMessage = strings.noMatchingSettings,
        hasResults = matches.isNotEmpty(),
        modifier = modifier,
        onBack = onBack,
        listState = listState,
    ) { recordRecent ->
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
    val score: SearchMatch,
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
    val tokens = searchQueryTokens(query)
    if (tokens.isEmpty()) return emptyList()
    return sections.mapNotNull { source ->
        val matches = source.section.entries.mapIndexedNotNull { index, entry ->
            if (!entry.searchable) return@mapIndexedNotNull null
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
            searchMatch(entry.label, direct, aliases, tokens)?.let { score -> Triple(entry, score, index) }
        }.sortedWith(compareBy<Triple<SettingsEntryDefinition, SearchMatch, Int>> { it.second }.thenBy { it.third })
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
