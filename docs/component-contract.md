# Component ownership

AndroidKit is opinionated. Components seal shape, typography, icons, labels,
control arrangement, chrome, interaction semantics and control rendering by
default. Consumers provide typed content/data, state, callbacks, availability
options, placement and supported theme colors. Components follow shared Kit
theme tokens internally; they do not expose per-component shape, icon, typography
or rendering overrides that let hosts redesign the control. A host wanting a
different visual direction should implement its own component. Depart from this
default only when explicitly requested.

The public theme accepts a color scheme, light/dark identity, floating-surface
transparency, shared floating-surface colors and component-specific colors.
Immutable color groups contain only colors and other color groups. Instance
`colors` arguments refine existing component color roles; unspecified values
inherit the active theme. Shapes, typography, border widths, shadow geometry,
icon/control sizes and component padding/spacing remain internal.

Shared typography, shapes and dimensions are readable through
`AndroidKitThemeTokens` for host-owned content. Dimensions have no public
constructor or copy operation. No theme or component input accepts replacement
scales. Nested host Material themes style app bodies; Kit controls establish
their own Material theme boundary.

Component-specific defaults belong to that component. Internal semantic token
groups own padding, gaps, icon sizes, text roles, shapes and local colors; a
component must not borrow another component's style or dimension just because
its current value matches. These groups are internal and introduce no new host
geometry APIs. Color groups and internal defaults retain their owning component.

The color palette, typography/shape/spacing scales, accessibility minimum touch
target and floating-surface transparency, border, shadow and disabled-state
policy remain shared foundations. Semantic defaults may derive from those
foundations, but never from a sibling component. Shared action primitives retain
their content inset and icon-label gap; measurement must use the same geometry
as rendering. Composite components continue to use the actual Page, Card,
SectionCard, BottomSheet, Tooltip and Flyout renderers, including their interaction
contracts. Owner-specific overflow chrome is supplied to the shared menu renderer.
Settings intentionally uses the SectionCard entry wrapper; theme/language picker
rows, sliders and the app-lock timeout dialog have their own control tokens.

Separating token ownership does not change appearance by itself. Current section
card padding is 18 dp horizontally and 14 dp vertically, with picker padding of
16 dp horizontally and 12 dp vertically, and the timeout dialog's current
16 dp vertical padding. It does not reset existing dimensions as part of the refactor.

`AndroidKitFloatingTooltip` accepts text, one optional typed action, and a Close
callback. Its chrome is sealed and uses shared floating-surface transparency,
border and shadow. The Material tooltip anchor retains placement and state ownership.
Render scopes and implementations stay internal or private; public declaration
scopes cannot be implemented by consumers.

`AndroidKitContextMenu` owns point-based invocation, transient menu state and
interaction behavior. It shares the action flyout renderer and typed menu DSL;
its app body slot does not expose menu rendering or component style overrides.

Action flyouts, context menus, submenus, and navigation overflow share one internal
container-padding token: 5 dp on every side. Container padding is Kit-owned and
cannot be overridden by hosts; it is separate from each menu item's content padding.

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
  power global search, whose result controls execute in place. Kit owns durable
  Settings values and recent queries through required persistent bindings. Hosts own
  effect callbacks, protected-change authorization, destinations and optional translated
  aliases for host content. Kit owns the Search title action, floating search
  page, matching behavior, built-in multilingual aliases, result chrome and
  predefined labels/icons. About remains fixed and is appended to Main when
  present. See [settings.md](settings.md).
- Cards: replace `header` and `headerSupportingContent` with `title` and
  `supportingText`. Supporting colors belong in `AndroidKitCardColors`;
  title/supporting typography and card padding/spacing remain Kit-owned.
  `AndroidKitSectionCard` provides typed action, navigation, toggle, slider,
  informational, copyable and multiline entries. Settings maps its declarations
  to these same entries; SectionCard renders the controls and surrounding card.
  See [section cards](section-card.md).
- Floating controls: supply `AndroidKitFloatingAction.Button(icon, label, onClick)`
  or `AndroidKitFloatingAction.Bar { ... }` to page/sheet floating-action parameters.
  `AndroidKitFloatingAction.Search` adds controlled floating search with device
  speech input. See [floating search](floating-search.md) for usage and compatibility.
  Search has no component style override: its shape, typography, icons and control
  arrangement are internal and follow Kit's design and shared theme tokens.
  `AndroidKitSearchPage` adds local matching of typed host data and intentionally
  retains a host-owned lazy result body for topic-specific presentation. Its
  Search title, input, recent searches, empty states and layout remain Kit-owned.
  Settings search uses the same implementation with its original catalog controls.
  See [search pages](search-page.md).
  Standalone buttons use `AndroidKitFloatingActionButton(action)`. Button icons
  accept vectors or painters; Kit owns icon rendering. Optional tooltip text is data.
- Pages: use title, back callback, and action data; the custom `topBar` is removed.
  The list-content overload owns scrolling and can prepend an optional typed
  support prompt. See [support prompts](support-prompt.md).
- Sheets: use title/actions and optional `AndroidKitSheetSearch` state for pinned
  search. Replace `dragHandle = null` with `showDragHandle = false`.
- Navigation: supply `AndroidKitNavigationBadge(label, contentDescription)`;
  a null badge hides it, while a badge with null label renders a dot.
  `AndroidKitNavDisplay` owns immediate page changes, including predictive Back,
  while hosts own routes and state. See [page navigation](navigation.md).

Outer modifiers and supported presentation options remain: app-list content
padding/arrangement, window insets, alignment, menu placement/offset/anchor,
sheet width/height/fit options, chrome visibility and typed action layout variants.
These do not replace Kit control renderers or internal geometry.
Legacy public styles and raw component padding/spacing arguments are removed. Authentication,
persistence, application-content localization, navigation decisions, and application
state belong to consumers. Kit-owned vocabulary is translated only in Kit and is
not overridable. Consumers must apply the [localization build gate](localization.md).
Existing edge-to-edge, scroll-padding, IME, and dismissal contracts remain.

`AndroidKitSectionCard` has no public composable entry-body slot or interaction
override. Hosts supply typed content, state, callbacks and context-menu items.
Kit owns padding, touch targets, ripple, focus, roles and control rendering.
Settings retains hierarchy, search metadata and state while mapping its
declarations to the same typed entries. Its existing modifier/color APIs remain
supported through internal data adapters, without a separate row renderer.
Context menus surround the complete entry, including padding. Their anchor
uses the shared secondary-container selection color while the menu is open,
and clears selection on dismissal, item invocation or disabling the entry.

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
