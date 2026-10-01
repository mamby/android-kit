# Section cards

`AndroidKitSectionCard` lives in `net.mamby.androidkit.compose.presentation`.
It accepts typed `entries`, an outer placement `modifier`, an optional `title`
and an optional `description`. An empty list renders nothing. Keys must be
nonblank, stable and unique; reordering preserves entry identity and focus.

The sealed `AndroidKitSectionCardEntry` types cover current application and
Settings usages:

- `Action`: a localized primary action with optional decorative leading/trailing
  icons and a supported trailing-icon color. Kit owns the icon size and layout.
- `Navigation`: a primary action with the Kit navigation chevron.
- `Toggle`: a full-row switch action with controlled checked/enabled state.
- `Slider`: controlled value, range, steps, completion callback, optional value
  and endpoint labels, supporting text and a decorative icon.
- `Info`: read-only text with values/supporting text below its label.
- `InlineInfo`: read-only text with a trailing value, preserving Settings layout.
- `CopyableInfo`: a localized copy callback and the Kit copy affordance.
- `Multiline`: read-only text preserving line breaks and wrapping without a limit.

Hosts supply localized content, state and callbacks. There are no custom body,
control, header, footer or divider rendering slots, and no public interaction
override. Kit owns the card, typography, padding, dividers, controls, full-entry
ripple, touch target, focus, accessibility roles and disabled behavior. The
host owns scrolling. New entry behaviors require deliberate typed API additions.

## Context menus

`Action`, `Info` and `Multiline` accept optional `contextMenu` declarations using
`AndroidKitActionFlyoutScope`. Resolve localized menu labels before constructing
the non-composable declaration. Declare `item`, `separator` and `submenu` only;
Kit renders and dismisses the menu.

The entry uses the same context-menu implementation as `AndroidKitContextMenu`.
Long press and secondary mouse click anchor at the invocation point;
accessibility and Menu/Shift+F10 use the entry bounds. Tap and long press cover
entry padding. Actions preserve their localized primary-action label and Button
role; read-only entries do not gain a primary tap action.

While the menu is open, the complete entry receives the shared
`secondaryContainer` selection color and selected semantics. Only the active
entry is highlighted. Back/outside dismissal, menu-item invocation and disabling
an action clear the highlight. Menu state is transient and is not persisted.

```kotlin
AndroidKitSectionCard(
    title = detailsTitle,
    entries = listOf(
        AndroidKitSectionCardEntry.Action(
            key = "work-phone",
            label = workNumber,
            actionLabel = callWorkLabel,
            onClick = onCallWork,
            contextMenu = {
                item(label = copyLabel, onClick = onCopyWork)
                item(label = shareLabel, onClick = onShareWork)
            },
        ),
        AndroidKitSectionCardEntry.Multiline("notes", notes, notesLabel),
    ),
)
```

## Settings and migration

Settings keeps its existing catalog, search metadata, navigation and state APIs.
Its section adapter maps declarations to `Navigation`, `Toggle`, `Slider`,
`InlineInfo` and `CopyableInfo`, and SectionCard renders every row. Existing
Settings modifier and control-color parameters remain supported internally.
The existing opacity-slider presentation is also rendered by SectionCard.

This replaces `Custom` and `AndroidKitSectionCardInteraction` and is a breaking
source/API change for their consumers. Migrate full-row actions to `Action`,
read-only text to `Info`/`Multiline`, switches to `Toggle`, and Settings-like rows
to the corresponding typed entries. Supply `contextMenu` on the entry rather
than wrapping its content in another clickable/menu component. The demo and
active host apps must consume the matching snapshot together.
