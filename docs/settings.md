# Settings catalog and global search

`AndroidKitSettingsCatalog` is the source of truth for the complete Settings
hierarchy. Normal pages and global search render the same keyed declarations, so
search results execute the original callback or control instead of navigating to
the page that contains it.

```kotlin
val catalog = androidKitSettingsCatalog(
    search = AndroidKitSettingsSearchConfiguration(
        onOpenSearch = onOpenSearch,
        recentQueries = recentQueries,
        onRecentQueriesChange = onRecentQueriesChange,
    ),
) {
    main(key = "main", title = settingsTitle) {
        section(key = "appearance", label = appearanceTitle) {
            theme(themeSetting)
            transparency(opacitySetting)
            toggle(
                key = "autoplay",
                label = autoplayLabel,
                checked = autoplay,
                onCheckedChange = onAutoplay,
            )
        }
    }
    subpage(key = "downloads", title = downloadsTitle) {
        section(key = "storage") {
            button(key = "clear", label = clearLabel, onClick = onClear)
        }
    }
    about(key = "about", content = aboutContent, onOpen = onAbout)
}

AndroidKitSettingsPage(catalog, pageKey = catalog.mainPageKey)
AndroidKitSettingsSearchPage(catalog, onBack = onBack)
```

The host registers page keys in its navigation stack and routes
`onOpenSearch`/About callbacks. Android Kit does not install a navigation
controller. Every catalog page receives the sealed Search title-bar action.
About remains optional and, when present, is appended to Main.

## Catalog contract

The catalog requires exactly one Main page, at most one About page, unique
nonblank page keys, and nonblank titles. Section keys are unique within a page;
custom entry keys are unique within a section. Stable keys do not depend on
translated labels or list positions.

Hosts own section titles, grouping, custom labels, state, persistence, callbacks,
destinations, and custom title-bar actions. Kit owns predefined labels, icons,
rendering, picker chrome, About order, the Search action, and search-page chrome.
Use `searchable = false` for transient messages or custom rows that should not
appear in search.

Language, Theme, Transparency, and App lock retain their typed declarations.
Selection option IDs must be unique and include the selected ID. App lock remains
host-confirmed and may expose its timeout picker and Lock now callback while
enabled. Custom rows use `button`, `navigation`, `toggle`, `slider`, or `info`;
all now require a stable key.

## Global search

`AndroidKitSettingsSearchPage` searches every catalog page, including pages that
have not been opened. It uses `AndroidKitFloatingSearchBox`, reserves managed
scroll clearance, and displays results in the current app language.

Matching is case-, accent-, punctuation-, and whitespace-insensitive. Every query
token must match. Exact/current labels rank before prefixes, contained visible
text, aliases, and page/section context. No fuzzy matching or network translation
is performed.

Results reuse the original controls: links and buttons run, toggles change,
pickers open in the search page, sliders remain adjustable, and Version copies.
Informational and disabled rows preserve their normal behavior. The automatic
Main About destination is excluded because the individual About entries are
indexed directly.

Kit-owned settings use an offline lexicon containing labels and semantic aliases
for every supported locale. A query in any supported language can therefore find
a built-in result while the result remains rendered in the current app language.
For host-owned entries, the current rendered text is always searchable. Supply
optional `AndroidKitSettingsSearchTerms` maps on pages, sections, entries, legal
entries, or options to add translated labels and aliases from other languages.

Recent queries are controlled host state. Android Kit trims submitted queries,
deduplicates them case/accent-insensitively, moves the newest spelling first, and
keeps at most ten. A query is recorded after IME submission or a result action;
typing and selecting an existing recent query do not change history. The host
persists the list if it must survive app restarts.

## About

The fixed About page renders app name/description, optional Website and Source
code, required Version, optional Contact, and optional Legal entries in that
order. Empty groups are omitted. Built-in links use Kit-owned labels and icons;
additional legal entries keep host-localized titles and optional multilingual
search terms.

## Migration from page-local Settings

Remove `AndroidKitSettingsPageConfiguration` and construct one catalog containing
all former Main, Subpage, and About declarations. Move each page title, actions,
and content into `main`, `subpage`, or `about`; render by `pageKey`; and add the
host search route. Add stable keys to custom entries and persist the controlled
recent-query list. Existing host navigation and setting callbacks remain valid.
