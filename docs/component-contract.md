# Component ownership

AndroidKit is opinionated. New components seal shape, typography, icons, labels,
control arrangement, chrome, interaction semantics and control rendering by
default. Consumers provide typed content/data, state, callbacks, availability
options, placement and supported theme colors. Components follow shared Kit
theme tokens internally; they do not expose per-component shape, icon, typography
or rendering overrides that let hosts redesign the control. A host wanting a
different visual direction should implement its own component. Depart from this
default only when explicitly requested.

Existing component style APIs described below remain until explicitly migrated;
they are not a precedent for adding equivalent extensibility to new components.
`AndroidKitFloatingTooltip` accepts text, one optional typed action, and a Close
callback. Its chrome is sealed and uses shared floating-surface transparency,
border and shadow. The Material tooltip anchor retains placement and state ownership.
Render scopes and implementations stay internal or private; public declaration
scopes cannot be implemented by consumers.

Toolbar actions, floating action bars, and page action controls share an internal
content inset derived from the small spacing token (8 dp by default). The renderer
reserves this inset around the content box, including icon-only and More buttons;
it does not depend on whitespace in icon artwork. Page action width measurement
uses the same inset and the icon box width. A horizontal icon-and-label action uses
the leading inset of an icon centered in an icon-only action's minimum width,
including Material's minimum interactive size and the page's visual control size.
This keeps its leading icon aligned with icon-only controls such as More in LTR
and RTL. Minimum interactive sizing can reserve additional space.
External horizontal padding is omitted at icon-facing edges,
and an inter-item gap is added only when both facing edges require it.

App-specific body content remains composable inside `AndroidKitPage`,
`AndroidKitCard`, and `AndroidKitBottomSheet`. The `content` parameter of
`AndroidKitFloatingNavigation` is the destination screen displayed alongside the
navigation bar, rail, or drawer. It cannot replace navigation items, their
renderers, badges, or the overflow menu. Apps supply those through typed data and
supported styling. The public theme also retains its composition slot.

## Migration

This is a breaking API update; removed rendering slots have no compatibility
escape hatch. Migrate consumers and the demo with the library.

- Flyouts: declare `item`, `separator`, and `submenu`. Toolbar/action-bar builders
  also accept only their typed declarations; custom toolbar `item` is removed.
- Settings: declare one keyed `AndroidKitSettingsCatalog` containing Main,
  subpages and optional About, then render a page by key. The same typed entries
  power global search, whose result controls execute in place. Hosts own state,
  callbacks, destinations, recent-query persistence and optional translated
  aliases for host content. Kit owns the Search title action, floating search
  page, matching behavior, built-in multilingual aliases, result chrome and
  predefined labels/icons. About remains fixed and is appended to Main when
  present. See [settings.md](settings.md).
- Cards: replace `header` and `headerSupportingContent` with `title` and
  `supportingText`. Explicit typography and supporting color belong in card style.
- Floating controls: supply `AndroidKitFloatingAction.Button(icon, label, onClick)`
  or `AndroidKitFloatingAction.Bar { ... }` to page/sheet floating-action parameters.
  `AndroidKitFloatingAction.Search` adds controlled floating search with device
  speech input. See [floating search](floating-search.md) for usage and compatibility.
  Search has no component style override: its shape, typography, icons and control
  arrangement are internal and follow Kit's design and shared theme tokens.
  Standalone buttons use `AndroidKitFloatingActionButton(action)`. Button icons
  accept vectors or painters; Kit owns icon rendering. Optional tooltip text is data.
- Pages: use title, back callback, and action data; the custom `topBar` is removed.
  The list-content overload owns scrolling and can prepend an optional typed
  support prompt. See [support prompts](support-prompt.md).
- Sheets: use title/actions and optional `AndroidKitSheetSearch` state for pinned
  search. Replace `dragHandle = null` with `showDragHandle = false`.
- Navigation: supply `AndroidKitNavigationBadge(label, contentDescription)`;
  a null badge hides it, while a badge with null label renders a dot.

Outer modifiers and existing style/layout parameters remain supported; new
components follow the sealed default above. Authentication,
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
