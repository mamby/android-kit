# Search page

In the demo catalog, open **Components → SearchPage**. Search the component
catalog by typing or dictation, then open a demo from its result card. The demo
owns those cards and persists its recent queries separately from Settings search.

`AndroidKitSearchPage` owns the Search title, floating field, dictation, recent
searches, empty states, scrolling, and measured page/IME clearance. Hosts own the
result body through a `LazyListScope` content slot: different topics can use
different cards, rows, grouping, and controls. Search chrome has no style or
rendering override. Result rows are an intentional host-owned body surface.

Opening the page focuses the search field and requests the software keyboard.
This happens once on entry; query, result, and history updates do not refocus the
field or reopen a dismissed keyboard. Reopening the page requests focus again.

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

## Settings integration

`AndroidKitSettingsSearchPage` uses the same internal page implementation, recent
history logic, and matching/ranking helpers. Its public signature is unchanged.
The Settings adapter supplies catalog results and renders their original
controls through the result slot, retaining the Search settings title, Settings
empty message, multilingual lexicon, section context, and in-place pickers,
switches, sliders, links, and copy actions. See [Settings](settings.md).
