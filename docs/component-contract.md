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
  Select an explicit page configuration: `Main` renders any supplied Support,
  Get involved, and About data inline; `Subpage` has no footer. See
  [settings.md](settings.md).
- Cards: replace `header` and `headerSupportingContent` with `title` and
  `supportingText`. Explicit typography and supporting color belong in card style.
- Floating controls: supply `AndroidKitFloatingAction.Button(icon, label, onClick)`
  or `AndroidKitFloatingAction.Bar { ... }` to page/sheet floating-action parameters.
  Standalone buttons use `AndroidKitFloatingActionButton(action)`. Button icons
  accept vectors or painters; Kit owns icon rendering. Optional tooltip text is data.
- Pages: use title, back callback, and action data; the custom `topBar` is removed.
- Sheets: use title/actions and optional `AndroidKitSheetSearch` state for pinned
  search. Replace `dragHandle = null` with `showDragHandle = false`.
- Navigation: supply `AndroidKitNavigationBadge(label, contentDescription)`;
  a null badge hides it, while a badge with null label renders a dot.

Outer modifiers and explicit style/layout parameters remain supported. Authentication,
persistence, localization, navigation decisions, and application state belong to
consumers. Existing edge-to-edge, scroll-padding, IME, and dismissal contracts remain.

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
