# Context menu

`AndroidKitContextMenu` wraps app content and opens the shared Kit action menu
at the touch-and-hold or secondary mouse press position. Placement respects RTL
and falls back at window edges. Menu rendering, animation, scrolling, submenus,
disabled entries and dismissal are shared with `AndroidKitActionFlyout`.

```kotlin
val editLabel = stringResource(R.string.edit)
AndroidKitContextMenu(
    onClick = onOpenRecord,
    menu = {
        item(label = editLabel, onClick = onEditRecord)
    },
) {
    Text(record.title)
}
```

The component owns transient open state and closes on action selection, Back,
outside clicks or `enabled = false`. Touch-and-hold supplies long-press haptics.
The content is an app body slot; menu content accepts only the flyout's typed
`item`, `separator` and `submenu` declarations. No component style or menu
rendering slots are exposed.

Use non-interactive content inside the wrapper. Supply its optional `onClick`
for the primary action instead of nesting clickable controls or text-selection
surfaces that compete for the same gestures. Omit `onClick` for content with no
primary action. Keyboard users can focus the wrapper and press Menu or Shift+F10;
accessibility services can invoke its localized More long-click action. These
invocations use the content bounds because they have no pointer position.

This is an item-action menu, not a replacement for the platform text-selection
toolbar or an application selection mode.
