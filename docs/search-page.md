# Search page

In the demo catalog, open **Components → SearchPage**. Search the component
catalog by typing or dictation, then open a demo from its result card. The demo
owns those cards and persists its recent queries separately from Settings search.
The SearchPage demo uses `OnSubmit`: press the keyboard Search action to show
matches. The floating-search demo and Settings search retain live matching.

`AndroidKitSearchPage` owns the Search title, floating field, dictation, recent
searches, empty states, scrolling, and measured page/IME clearance. Hosts own the
result body through a `LazyListScope` content slot: different topics can use
different cards, rows, grouping, and controls. Search chrome has no style or
rendering override. Result rows are an intentional host-owned body surface.

Opening the page focuses the search field and requests the software keyboard.
This happens once on entry; query, result, and history updates do not refocus the
field or reopen a dismissed keyboard. Reopening the page requests focus again.

## Search timing

`searchMode` accepts the same `AndroidKitSearchMode.Live` / `OnSubmit` enum as
the floating search box and floating action. The page defaults to `Live` to
preserve existing behavior. With `OnSubmit`, input remains a draft until the
nonblank IME Search action. Before the first submission, history remains visible;
after submission, the previous results remain visible while editing the next
query. Result actions record the query that produced those results. Clearing
resets results and returns to history. Selecting history fills the draft and
requires submission in `OnSubmit`. The last submitted query survives saved-state
restoration; switching modes resets that stored query.

This page still matches supplied items locally. The timing option does not add
a remote-result API. Hosts can already execute remote searches through the
floating box/action's `onSearch`; fetching and remote result state stay host-owned.

```kotlin
val searchItems = records.map { record ->
    AndroidKitSearchItem(
        key = record.id,
        title = record.title,
        data = record,
        onClick = { onOpenRecord(record) },
        supportingText = record.description,
        searchTerms = record.aliases,
    )
}
AndroidKitSearchPage(
    items = searchItems,
    query = query,
    onQueryChange = onQueryChange,
    recentQueries = recentQueries,
    onRecentQueriesChange = onRecentQueriesChange,
    searchHistoryEnabled = searchHistoryEnabled,
    onSearchHistoryEnabledChange = onSearchHistoryEnabledChange,
    onBack = onBack,
) { matches ->
    items(matches, key = { it.key }) { match ->
        Card(onClick = match.onClick, enabled = match.enabled) {
            Text(match.data.title)
        }
    }
}
```

Use Compose's `androidx.compose.foundation.lazy.items` extension in the result
slot. The page supplies its lazy list and content padding; do not nest another
vertical scroller or add IME padding. Supply `listState` when the host needs to
control scrolling. `voiceInputEnabled = false` omits the microphone; dictation
uses the existing [floating search contract](floating-search.md).

## Matching and result actions

`AndroidKitSearchItem<T>` carries the host's typed domain model in `data`. Keys
must be unique across the page. An optional `AndroidKitSearchGroup` supplies a
stable group key and a host-localized context title; items sharing its key must
share its title. The content slot receives matched items in relevance order;
the host chooses whether and how to group them.

Matching uses the title, supporting text, group title, and optional aliases in
any language. It is case-, accent-, punctuation-, and whitespace-insensitive;
every query token must match. Exact titles rank before prefixes, contained
titles, supporting text, and aliases/context. Ties keep the input order. A
nonblank query containing only punctuation or emoji has no matches. This is
local in-memory search; there is no database query or network translation.

Searchable text is normalized once per dataset and reused across queries.
Normalization and matching run in cancellable background coroutines. While the
current query or dataset is being processed, Kit shows a progress indicator and
removes outgoing results so they cannot invoke stale actions. Query or dataset
changes cancel superseded work. Results always use the current host data,
availability and callbacks; changing only callbacks or availability does not
rebuild the text index. Ranking and `Live` / `OnSubmit` timing remain the same.

Call the matched item's `onClick` from the host result action. Kit records the
query before invoking the original callback. Disabled items remain searchable,
but their matched callbacks do nothing; the host must also render their disabled
state and accessibility semantics. Item content and aliases are host-localized;
generic Search and history/empty-state vocabulary are translated by Kit.

Hosts own the query and recent-query state. Kit trims recorded queries,
deduplicates them using the matching normalization, moves the newest spelling
first, and keeps ten. History changes after IME submission or result activation;
typing and selecting an existing recent query do not record it. Individual
removal and Clear all use `onRecentQueriesChange`. Persist the list in the host
if it must survive restarts. Save query state in the host when restoration is
required; the demo uses `rememberSaveable` for the query and DataStore for history.

## Page-scoped search history

`searchHistoryEnabled` and `onSearchHistoryEnabledChange` are required in
`AndroidKitSearchPage`. The Kit-owned Material 3 switch enables or disables
history; its localized accessibility actions are Enable search history and
Disable search history. A visible note explains that disabling clears saved
searches. There is no API option to omit this control.

Disabling immediately removes recent rows, including outgoing animations and
accessibility semantics, clears saved queries, and stops recording both IME
submissions and result actions. Searching and result actions continue normally.
The empty-query body says Search history is disabled. Enabling starts empty;
deleted searches do not return. Clear all clears entries while leaving history
enabled. Recent-query cards and individual removal retain their existing behavior.

The public page requests an empty list when disabled and calls
`onSearchHistoryEnabledChange`. Hosts must atomically persist clearing and the
new enabled state in that callback, and reject writes when their persisted state
is disabled. This protects against pending writes and callbacks from other screens.
The Kit persistent store enforces this inside DataStore transactions using
`AndroidKitPersistentSearchHistory.setEnabled`; its snapshot exposes `enabled`.

Persist this preference per stable logical search identifier. Main Settings,
About and Settings subpages share one owner and one history preference. Independent
content searches retain separate preferences. The absent preference defaults to
enabled. Load persisted state before rendering history; initially pass
`searchHistoryEnabled = false` while loading.

This is a source-breaking rename from `recentQueriesVisible`,
`onRecentQueriesVisibleChange`, snapshot `visible`, and `setVisible`. Kit storage
version 2 migrates version 1 before exposing data: old hidden histories become
disabled and empty on disk; visible histories remain enabled with their queries.
Migration markers and unrelated preferences are preserved. The demo similarly
clears its previously hidden independent content history before presenting it.

The recent heading, switch and Clear all share a floating Kit surface. The list
viewport remains edge to edge, with measured content padding keeping its rows
clear of floating controls. Encryption remains an independent storage policy.

## Settings integration

`AndroidKitSettingsSearchPage` uses the same internal page implementation, recent
history logic, and matching/ranking helpers. It resolves the complete catalog
and shared history from its enclosing `AndroidKitSettings` owner; individual
Settings pages cannot supply a separate catalog or history.
The Settings adapter supplies catalog results and renders their original
controls through the result slot, retaining the Search settings title, Settings
empty message, multilingual lexicon, section context, and in-place pickers,
switches, sliders, links, and copy actions. See [Settings](settings.md).
