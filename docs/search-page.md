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
    recentQueriesVisible = recentQueriesVisible,
    onRecentQueriesVisibleChange = onRecentQueriesVisibleChange,
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

## Page-scoped history visibility

Both `recentQueriesVisible` and `onRecentQueriesVisibleChange` are required in
`AndroidKitSearchPage` and `AndroidKitSettingsSearchConfiguration`. Hosts must
wire the callback to their page-scoped persisted state. Every search page includes the
Kit-owned eye toggle immediately after the Recent searches heading. Its localized
accessibility action is Hide recent searches / Show recent searches. There is no
API option to omit the privacy control. This is a source-breaking change: callers
must supply both arguments explicitly when upgrading.
Hiding immediately removes recent rows, including outgoing animations and their
accessibility semantics. The empty-query body says Recent searches hidden and
keeps the heading and toggle available even when history is empty. Recent queries
render as rounded Kit cards with a leading history icon and trailing X remove
action; tapping the card restores the query. Clear all remains a separate deletion
action when history is shown. Searching and recording continue while hidden.

The recent heading, visibility toggle and Clear all share a floating Kit surface.
The list viewport fills the page edge to edge. Its content padding keeps the first
row below the measured recent bar and the last row above the search field, while
rows scroll behind the recent bar, page title and system bars. The page title bar
and status-bar protection retain the standard `AndroidKitPage` behavior.

Hosts persist visibility alongside history, scoped by a stable logical search
identifier, never a translated title or transient navigation entry. The demo
uses separate DataStore keys `search.settings.recents_visible` and
`search.content.recents_visible` in its existing repository. An absent key
defaults to visible for compatibility. The demo waits for persisted settings
before composing search; hosts must similarly avoid rendering history before
visibility loads, or initially pass `recentQueriesVisible = false`. Visibility
is concealment on this search page, not private browsing or encrypted storage.

## Settings integration

`AndroidKitSettingsSearchPage` uses the same internal page implementation, recent
history logic, and matching/ranking helpers. Its public signature is unchanged.
The Settings adapter supplies catalog results and renders their original
controls through the result slot, retaining the Search settings title, Settings
empty message, multilingual lexicon, section context, and in-place pickers,
switches, sliders, links, and copy actions. See [Settings](settings.md).
