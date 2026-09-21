# Floating search

In the demo catalog, open **Components → FloatingSearchBox**. The page has a
floating search field and an **Open search sheet** button for the bottom-sheet
example. Each host keeps its own query and last submitted search. Both filter
the catalog examples as you type or return from voice input; clearing restores
the full list. Scroll with the keyboard open to inspect floating placement and
content clearance.

`AndroidKitFloatingSearchBox` is controlled input: hosts own the query, persistence,
search execution and results. Kit owns the pill surface, text field, search and
microphone icons, clear control and localized vocabulary. It does not autofocus.

```kotlin
var query by rememberSaveable { mutableStateOf("") }
AndroidKitFloatingSearchBox(
    query = query,
    onQueryChange = { query = it },
    onSearch = { submittedQuery -> search(submittedQuery) },
)
```

The standalone component fills its available width. Its parent owns bottom
alignment, scroll clearance and window insets. Use the typed floating action for
automatic placement in Kit hosts:

```kotlin
val searchAction = AndroidKitFloatingAction.Search(
    query = query,
    onQueryChange = { query = it },
    onSearch = { submittedQuery -> search(submittedQuery) },
)
AndroidKitPage(title = title, floatingActionButton = searchAction) { padding ->
    LazyColumn(contentPadding = padding) { /* results */ }
}
// Or pass the same typed data to a sheet:
AndroidKitBottomSheet(
    visible = visible,
    title = title,
    onDismiss = onDismiss,
    floatingAction = searchAction,
    scrollMode = AndroidKitBottomSheetScrollMode.ContentManaged,
) { padding ->
    LazyColumn(contentPadding = padding) { /* results */ }
}
```

Use one host at a time in actual UI. Floating search occupies the existing single
floating-control position. Apply the host padding to scrollable **content**, not
the viewport. Keep the page default `applyImePadding = true`. The hosts handle
keyboard movement and measure the complete control, including any error text;
do not add `imePadding()` to the search box itself. The existing top-pinned
`AndroidKitSheetSearch` remains available and unchanged.

Typing emits query changes immediately. Clear empties the query and focuses the
field. The IME Search action submits only nonblank queries and hides the keyboard;
the submitted value is not trimmed or otherwise transformed.

Voice input is enabled by default. Set `voiceInputEnabled = false` to omit the
microphone. Tapping it launches the device speech activity using the
[Activity Result API](https://developer.android.com/training/basics/intents/result)
and [`RecognizerIntent.ACTION_RECOGNIZE_SPEECH`](https://developer.android.com/reference/android/speech/RecognizerIntent)
with the free-form language model. The recognizer chooses its default language;
Kit makes no offline, language-detection or translation promises and does not
record audio or request microphone permission itself.

While the speech activity is open, the microphone is disabled. A nonblank result
at index zero replaces the query without submitting. Cancellation, missing
results or a blank first result preserve the query; later alternatives are not
substituted. Results arriving after input or voice input was disabled are ignored.
The host must keep the component composed to receive the result. Query persistence
remains the host responsibility, including across activity recreation.

If the activity is missing or launch is denied, Kit shows an accessible localized
error beneath the pill. Typing, clearing or retrying dismisses that error. Speech
recognition errors inside the external speech UI belong to that provider.

Shape, typography, icons, labels, control arrangement and interaction behavior
are Kit-owned. There is no component style parameter or custom rendering slot.
The component follows shared Kit theme colors, typography and floating-surface
tokens. Hosts supply query/state, callbacks, availability and outer placement;
a different visual direction requires a host-owned component.

## Compatibility

The former `AndroidKitFloatingSearchBoxStyle` and `style` arguments on the
standalone component and `AndroidKitFloatingAction.Search` have been removed.
Remove those arguments from existing call sites; there is no replacement override.

The `Search` variant extends the sealed `AndroidKitFloatingAction` interface.
Consumers with exhaustive `when` expressions over this interface must add a
`Search` branch when recompiling. Page and sheet parameter signatures are unchanged.

The four new private strings are present in every locale listed by the existing
localization contract. Its schema and locale inventory are unchanged: the build
gate discovers string keys from the AAR resources, so no JSON schema or locale
change is needed for new vocabulary.
