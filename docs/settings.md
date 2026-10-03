# Settings catalog and global search

`AndroidKitSettingsCatalog` is the source of truth for the complete Settings
hierarchy. Normal pages and global search render the same keyed declarations, so
search results execute the original callback or control instead of navigating to
the page that contains it.

```kotlin
val catalog = androidKitSettingsCatalog {
    main(key = "main", title = settingsTitle) {
        section(key = "appearance", label = appearanceTitle) {
            theme(themeSetting)
            transparency(opacitySetting)
            toggle(
                key = "autoplay",
                label = autoplayLabel,
                persistence = settingsStore.setting(booleanPreferencesKey("autoplay"), false),
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

AndroidKitSettings(
    catalog = catalog,
    store = settingsStore,
    onOpenSearch = onOpenSearch,
    onStorageFailure = onSettingsStorageFailure,
) {
    // Host navigation destinations render these inside this single owner.
    AndroidKitSettingsPage(pageKey = catalog.mainPageKey)
    // In the Search destination: AndroidKitSettingsSearchPage(onBack = onBack)
}
```

Settings is one logical search scope. Main, About and every subpage share the
complete catalog, recent queries and history enabled state. `AndroidKitSettings`
binds the store's existing `"settings"` history internally. Pages and search have
no catalog or history parameters. Missing, nested and simultaneous owners for
the same store are rejected. Declare the owner above navigation, never inside
an individual destination. Independent content searches retain their own
page-scoped history through `AndroidKitSearchPage`.

The host registers page keys in its navigation stack and routes
`onOpenSearch`/About callbacks. Android Kit does not install a navigation
controller. Every catalog page receives the sealed Search title-bar action.
About remains optional and, when present, is appended to Main.

## Catalog contract

The catalog requires exactly one Main page, at most one About page, unique
nonblank page keys, and nonblank titles. Section keys are unique within a page;
custom entry keys are unique within a section. Stable keys do not depend on
translated labels or list positions.

Hosts own section titles, grouping, custom labels, effect callbacks,
destinations, and custom title-bar actions. Kit owns durable Settings state, predefined labels, icons,
rendering, picker chrome, About order, the Search action, and search-page chrome.
Use `searchable = false` for transient messages or custom rows that should not
appear in search.

Language, Theme, Transparency, and App lock retain their typed declarations.
Selection option IDs must be unique and include the selected ID. App lock remains
host-confirmed and may expose its timeout picker and Lock now callback while
enabled. Custom rows use `button`, `navigation`, `toggle`, `slider`, or `info`;
all now require a stable key.

Language pickers keep System first and sort the remaining options by their stable
IDs, case-insensitively, regardless of the script used in native labels or the
host's insertion order. Use standard BCP 47 language tags as language option IDs
(for example, `id` and `zh-Hans`). New options are sorted on the next render;
filtering preserves this order. Theme and timeout options retain host order.

Selected language and theme options retain their checkmark and use the Material 3
`secondaryContainer` / `onSecondaryContainer` color pair with medium-weight text.
Selection continues to use Compose radio-group semantics.

## Global search

`AndroidKitSettingsSearchPage` searches every catalog page, including pages that
have not been opened. It uses the shared [search page](search-page.md) implementation
for the floating field, recent searches, empty states, scrolling and managed
clearance. Opening the page focuses its input and requests the software keyboard.
The Settings adapter owns catalog indexing and result controls;
results remain in the current app language. The Settings owner requires the persistent store and a storage-failure callback.
It shares history and enabled state across the complete Settings hierarchy.

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

Recent queries are Kit-owned durable state. Android Kit trims submitted queries,
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
host search route. Add stable keys to custom entries and bind editable values to
the Kit store. Host navigation and effect callbacks remain available.

## Persistent settings (0.1.52-SNAPSHOT)

Open `AndroidKitSettingsStore` once in the application's singleton DI provider,
with a stable store name and an explicit `Plaintext` or `Encrypted` protection policy.
The store uses transactional AndroidX DataStore under app-private `noBackupFilesDir`;
encrypted stores use a non-exportable Android Keystore AES-GCM key. Configure
one-time `AndroidKitSettingsStoreMigration` imports on that first call. Existing
values take precedence over imports, and migration cleanup follows a successful write.
History and preference imports are separate transactions with separate completion markers.

Every editable Settings declaration requires an `AndroidKitPersistentSetting`:
String for selections, Boolean for toggles/app lock, and Float for sliders/transparency.
Create bindings with `store.setting(preferencesKey, defaultValue)`. Screen-local values
and arbitrary persistence adapters cannot satisfy this API. The Settings owner
binds one canonical `"settings"` history and its enabled preference for Main,
About and every subpage. `store.searchHistory(stablePageKey)` remains available
for independent content searches; Settings pages cannot select a history key.

Kit loads persisted values before showing controls, writes ordinary changes before
calling host effect callbacks, and keeps writes independent of composition/navigation.
Sliders preview while dragging and persist on completion. App lock remains an
authorization request: after successful authentication, the host commits to the same
Kit binding or `store.preferences`; rejection leaves the saved preference unchanged.
Hosts still apply theme/locale/security effects and must restore those effects from the
saved preferences on startup. Existing host repositories can use `store.preferences`
as their DataStore to retain their keys and all additional preferences.

Storage failures are reported through the required callback. Unreadable or unauthenticated
files are never silently replaced with defaults. The storage contract guarantees use of
durable storage, not successful writes when the device storage or Keystore is unavailable.
Domain records, backups and controls outside the Settings API retain their host-owned storage.

## Shared Settings owner migration

Remove `AndroidKitSettingsSearchConfiguration` and its host-selected history.
Build the complete catalog with `androidKitSettingsCatalog { ... }`, and place
one `AndroidKitSettings(catalog, store, onOpenSearch, onStorageFailure)` above
all Settings navigation destinations. Remove catalog arguments from
`AndroidKitSettingsPage` and `AndroidKitSettingsSearchPage`. Subpage destinations
must not build replacement catalogs or declare their own owner.

Existing enabled `"settings"` queries remain unchanged. Previously hidden histories
are migrated to disabled and cleared before loading. Former separate
About history is left in storage and is not displayed or merged into Settings;
this avoids revealing previously hidden subpage queries. General SearchPage
history preferences use the enabled policy described in [SearchPage](search-page.md).
