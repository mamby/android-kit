package net.mamby.androidkit.compose.form

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CancellationException
import net.mamby.androidkit.compose.layout.AndroidKitPage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Global Settings search rendered from the same catalog as every Settings page. */
@Composable
public fun AndroidKitSettingsSearchPage(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
): Unit {
    val owner = currentSettingsOwner()
    val catalog = owner.catalog
    var query by rememberSaveable { mutableStateOf("") }
    var activePicker by rememberSaveable { mutableStateOf<String?>(null) }
    val strings = AndroidKitThemeTokens.strings
    val history = owner.search.history
    val snapshot by produceState<Result<AndroidKitSearchHistorySnapshot>?>(null, history) {
        value = null
        try {
            history.snapshots.collect { value = Result.success(it) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            value = Result.failure(failure)
        }
    }
    val storageFailure = snapshot?.exceptionOrNull()
    LaunchedEffect(storageFailure) {
        storageFailure?.let(owner.search.onStorageFailure)
    }
    val saved = snapshot?.getOrNull()
    if (saved == null) {
        // Never flash history before its privacy preference loads, or replace unreadable data.
        AndroidKitPage(title = strings.searchSettings, modifier = modifier, onBack = onBack) {
            if (snapshot == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        return
    }
    val lexicon = rememberSettingsSearchLexicon()
    val collected = collectCatalogSearchSections(catalog) { activePicker = it }
    val preferenceFailure = collected.scopes.firstNotNullOfOrNull { it.storageFailure }
    if (preferenceFailure != null || collected.scopes.any { it.waitingForStorage }) {
        LaunchedEffect(preferenceFailure) { preferenceFailure?.let(owner.search.onStorageFailure) }
        AndroidKitPage(title = strings.searchSettings, modifier = modifier, onBack = onBack) {
            if (preferenceFailure == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
        return
    }
    val searchableEntries = collected.sections.flatMap { source ->
        source.section.entries.filter { it.searchable }.map { entry ->
            val visibleText = listOfNotNull(
                entry.label, entry.supportingText,
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
            SearchableSettingsEntry(source, entry, SearchDocument(
                key = "${source.pageKey}:${source.section.key}:${entry.key}",
                label = entry.label, visibleText = visibleText, aliases = aliases,
            ))
        }
    }
    val matchedIndices = rememberSearchMatches(searchableEntries.map { it.document }, query)
    // Matching returns relevance order with source-order ties. Group insertion order therefore
    // retains the original section ranking, while controls always use the current host callbacks.
    val matches = matchedIndices.orEmpty().map { searchableEntries[it.index] }
        .groupBy { it.source.order }.values.map { entries ->
            val source = entries.first().source
            SettingsSearchResult(
                entries = entries.map { it.entry },
                pageKey = source.pageKey,
                section = source.section,
                contextLabel = listOfNotNull(source.pageTitle, source.section.label)
                    .distinct().joinToString(" · "),
            )
        }
    AndroidKitSearchPage(
        query = query,
        onQueryChange = { query = it },
        recentQueries = saved.recentQueries,
        onRecentQueriesChange = { error("Settings history changes must use persistent operations.") },
        searchHistoryEnabled = saved.enabled,
        onSearchHistoryEnabledChange = { enabled ->
            history.submit(owner.search.onStorageFailure) { setEnabled(enabled) }
        },
        onRecordRecent = { value -> history.submit(owner.search.onStorageFailure) { record(value) } },
        onRemoveRecent = { value -> history.submit(owner.search.onStorageFailure) { remove(value) } },
        onClearRecent = { history.submit(owner.search.onStorageFailure) { clear() } },
        title = strings.searchSettings,
        noMatchesMessage = strings.noMatchingSettings,
        hasResults = matches.isNotEmpty(),
        isSearching = matchedIndices == null,
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
)

private data class SearchableSettingsEntry(
    val source: SearchableSettingsSection,
    val entry: SettingsEntryDefinition,
    val document: SearchDocument,
)

@Composable
private fun collectCatalogSearchSections(
    catalog: AndroidKitSettingsCatalog,
    openPicker: (String) -> Unit,
): CollectedSearchSections {
    val owner = currentSettingsOwner()
    val strings = AndroidKitThemeTokens.strings
    val sections = mutableListOf<SearchableSettingsSection>()
    val scopes = mutableListOf<SettingsPageScopeImpl>()
    var order = 0
    catalog.pages.forEach { page ->
        val pageTitle = page.title ?: strings.about
        when (page) {
            is AndroidKitSettingsCatalogPage.Main -> {
                val scope = SettingsPageScopeImpl(page.key, strings, owner.search.onStorageFailure, openPicker)
                AndroidKitSettingsPageScope().apply(page.content).render(scope)
                scopes += scope
                scope.items.forEach { sections += SearchableSettingsSection(page.key, pageTitle,
                    page.searchTerms, it, order++) }
            }
            is AndroidKitSettingsCatalogPage.Subpage -> {
                val scope = SettingsPageScopeImpl(page.key, strings, owner.search.onStorageFailure, openPicker)
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
