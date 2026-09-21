# Component ownership

AndroidKit owns every component's chrome, layout, typography defaults, interaction
semantics, and control rendering. Consumers provide typed data, state, callbacks,
and explicit theme or component styles. Render scopes and implementations stay
internal or private; public declaration scopes cannot be implemented by consumers.

App-specific body content remains composable inside `AndroidKitPage`,
`AndroidKitCard`, and `AndroidKitBottomSheet`. The `content` parameter of
`AndroidKitFloatingNavigation` is the destination screen displayed alongside the
navigation bar, rail, or drawer. It cannot replace navigation items, their
renderers, badges, or the overflow menu. Apps supply those through typed data and
styles. The public theme also retains its composition slot.

## Migration

This is a breaking API update; removed rendering slots have no compatibility
escape hatch. Migrate consumers and the demo with the library.

- Flyouts: declare `item`, `separator`, and `submenu`. Toolbar/action-bar builders
  also accept only their typed declarations; custom toolbar `item` is removed.
- Settings: resolve composable resources before the page builder. Use `section`
  and typed rows, including `info` for labels, values, and supporting text.
  Hosts own section titles, grouping, and entry order; predefined `language`,
  `theme`, `transparency`, and `appLock` declarations keep their labels, icons,
  and normal descriptions sealed. App-lock failures use explicit `errorMessage`.
  `Main` optionally appends About as its final row. `About` renders App information,
  Contact, and Legal information in fixed order, omitting
  empty groups and section headings. The main About row has no subtitle.
  Website and Source code follow the app description and precede
  Version; Third-party licenses belongs in Legal. Hosts supply app data and
  optional destinations; Kit owns link
  labels and icons. `Subpage` has no footer. See [settings.md](settings.md).
- Cards: replace `header` and `headerSupportingContent` with `title` and
  `supportingText`. Explicit typography and supporting color belong in card style.
- Floating controls: supply `AndroidKitFloatingAction.Button(icon, label, onClick)`
  or `AndroidKitFloatingAction.Bar { ... }` to page/sheet floating-action parameters.
  `AndroidKitFloatingAction.Search` adds controlled floating search with device
  speech input. See [floating search](floating-search.md) for usage and compatibility.
  Standalone buttons use `AndroidKitFloatingActionButton(action)`. Button icons
  accept vectors or painters; Kit owns icon rendering. Optional tooltip text is data.
- Pages: use title, back callback, and action data; the custom `topBar` is removed.
  The list-content overload owns scrolling and can prepend an optional typed
  support prompt. See [support prompts](support-prompt.md).
- Sheets: use title/actions and optional `AndroidKitSheetSearch` state for pinned
  search. Replace `dragHandle = null` with `showDragHandle = false`.
- Navigation: supply `AndroidKitNavigationBadge(label, contentDescription)`;
  a null badge hides it, while a badge with null label renders a dot.

Outer modifiers and explicit style/layout parameters remain supported. Authentication,
persistence, application-content localization, navigation decisions, and application
state belong to consumers. Kit-owned vocabulary is translated only in Kit and is
not overridable. Consumers must apply the [localization build gate](localization.md).
Existing edge-to-edge, scroll-padding, IME, and dismissal contracts remain.

```kotlin
val addAction = AndroidKitFloatingAction.Button(
    icon = addIcon,
    label = addLabel,
    onClick = onAdd,
)
AndroidKitPage(title = title, floatingActionButton = addAction) { padding ->
    LazyColumn(contentPadding = padding) {
        items(records, key = { it.id }) { record ->
            AndroidKitCard(title = record.title) { Text(record.description) }
        }
    }
}
```
