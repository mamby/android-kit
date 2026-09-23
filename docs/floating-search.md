# Floating search

In the demo catalog, open **Components → FloatingSearchBox**. The page has a
floating search field and an **Open search sheet** button for the bottom-sheet
example. Each host keeps its own query and last submitted search. Both filter
the catalog examples as you type or dictate; clearing restores
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
keyboard movement and measure the control as its text expands;
do not add `imePadding()` to the search box itself. The existing top-pinned
`AndroidKitSheetSearch` remains available and unchanged.

The field starts at one line, grows to three lines, then scrolls text internally.
Its rounded corners stay consistent as it grows. During dictation, a softly pulsing
halo around the leading microphone, localized status, and the internal Close icon
replace the input in a compact row.
Error popups do not change the field's measured height. The caret is hidden when
the software keyboard is closed. Closing the keyboard also releases editing focus
to dismiss selection handles; tapping the input starts editing again.

Typing emits query changes immediately. Clear empties the query and focuses the
field. The IME Search action submits only nonblank queries and hides the keyboard;
the submitted value is not trimmed or otherwise transformed.

Voice input is enabled by default. Set `voiceInputEnabled = false` to omit the
microphone. Tapping it requests microphone permission when needed, then starts
the official [`SpeechRecognizer`](https://developer.android.com/reference/android/speech/SpeechRecognizer)
service inside the component. No external speech dialog is opened. The microphone
halo remains visible while starting, listening and finishing. Localized status
is announced through accessibility semantics. The Close icon's accessible action
is Stop listening: it ends audio capture and waits for the
provider's final result; it does not submit the search.

Each session preserves the existing query as a prefix. The best nonblank partial
transcript replaces the current dictated suffix; it never appends every partial
revision. A separating space is inserted when the prefix has no trailing whitespace.
The final transcript replaces that suffix once more. Subsequent sessions append
to the updated query. Missing/blank results leave the latest visible text intact.
The host receives these updates through `onQueryChange`, without `onSearch`.
The input returns with the updated query when dictation finishes or is cancelled.
The halo is an activity animation, not a measurement of microphone volume.

Pressing Back, disabling voice/the field, changing
the query externally, disposing the component or moving the owning lifecycle to
the background cancels the session and destroys the recognizer. Late callbacks
are ignored. Partial text already displayed is retained unless the user/host edits
it. Dictation never restarts automatically after returning to the screen or after
activity recreation. Keep hidden underlying hosts disabled when showing another
search surface; the demo does this when opening its sheet.

The library manifest declares `RECORD_AUDIO`, marks microphone hardware optional,
and includes Android's required `RecognitionService` package-visibility query.
Runtime permission is requested only after tapping the microphone, using the
[Activity Result permission contract](https://developer.android.com/training/permissions/requesting).
Denial leaves typing available and shows guidance with an Open app settings action.
Errors use `AndroidKitFloatingTooltip` above the field, sharing the floating
surface shadow and transparency, with a localized Close action and a
six-second timeout extended by the system accessibility recommendation. No error
icon is shown. Tapping the microphone retries permission; if access is still denied,
the tooltip appears again with the settings action. Granting permission in settings clears the error without starting
recording. Other errors disappear after dismissal; editing or retrying clears them.
Recognition does not continue in the background.

Kit requests partial results and, on Android 14+, balanced automatic language
switching (which also enables detection). Both depend on provider support and
available language models; some services only return a final transcript. Older
Android versions use the provider's default language. These
[`RecognizerIntent` options](https://developer.android.com/reference/android/speech/RecognizerIntent)
do not translate the query. The default speech service may process audio remotely;
offline recognition and continuous dictation are not guaranteed.

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

All search and dictation strings are present in every locale listed by the existing
localization contract. Its schema and locale inventory are unchanged: the build
gate discovers string keys from the AAR resources, so no JSON schema or locale
change is needed for new vocabulary.
