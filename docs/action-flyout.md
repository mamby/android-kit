# Action flyout

`AndroidKitActionFlyout` is the shared anchored menu for standalone controls,
toolbar and action-bar flyouts, header actions, cards, and navigation overflow.
Place it in a `Box` with the control it should anchor to, inside `AndroidKitTheme`.

```kotlin
var expanded by remember { mutableStateOf(false) }

Box {
    Button(onClick = { expanded = true }) {
        Text("More")
    }
    AndroidKitActionFlyout(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        item(label = "Edit", onClick = onEdit, icon = editIcon)
        item(label = "Share", onClick = onShare)
        separator()
        item(label = "Delete", onClick = onDelete, enabled = canDelete)
    }
}
```

Standard `item` entries request dismissal before invoking their action. Icons
are optional, and disabled entries cannot invoke their action. Setting the
flyout's `enabled` to `false` closes it and requests dismissal if it was expanded.
The caller owns the expanded state; outside clicks and Back request dismissal
through the popup's default `PopupProperties`.

The content builder accepts only `item`, `separator`, and `submenu` declarations.
It is not composable and does not expose `ColumnScope` or arbitrary UI slots.
Resolve labels with `stringResource` and icons with composable resource loaders
before entering the builder. Actions accept `ImageVector` or `Painter` icon data;
Kit renders the icon, label, spacing, styling, and dismissal behavior.
Content scrolls vertically within the shared flyout.

Use `placement` (`Above` or `Below`) and `horizontalAlignment` (`Start` or `End`)
to choose the preferred anchor edge. The flyout respects RTL and falls back to
another position when the preferred edge cannot fit within the window. It also
accepts `offset`, `contentPadding`, `scrollState`, `properties`, and `style`.

## Material menu trial and submenus

The flyout uses Material 3's `DropdownMenuPopup` and its default motion, while
retaining Kit's floating surface, row styling, scrolling, and root placement.
This trial pins Material 3 to `1.5.0-alpha27`; other Material components also
resolve to this release and should be reviewed before publishing.

The standalone content DSL supports nested menus:

```kotlin
submenu(label = "Share") {
    item(label = "Copy link", onClick = onCopyLink)
    submenu(label = "Export") {
        item(label = "Text", onClick = onExportText)
    }
}
```

Submenus inherit the parent surface style, padding, and popup properties. They
use Material's end-relative positioning, including RTL and window-edge fallback.
Back or an outside click dismisses the current submenu; selecting an action
requests dismissal of every parent before invoking the action. Disabling or
closing a parent closes its nested menus.
The existing toolbar item DSL remains unchanged; `submenu` is available in the
`AndroidKitActionFlyoutScope` content builder.

## Migration from floating dropdown

The separate dropdown API has been removed. Consumers must update these names:

| Previous API | Flyout API |
| --- | --- |
| `AndroidKitFloatingDropdownMenu` | `AndroidKitActionFlyout` |
| `AndroidKitFloatingDropdownMenuPlacement` | `AndroidKitActionFlyoutPlacement` |
| `AndroidKitFloatingDropdownMenuHorizontalAlignment` | `AndroidKitActionFlyoutHorizontalAlignment` |
| `AndroidKitFloatingDropdownMenuStyle` | `AndroidKitActionFlyoutStyle` |
| `floatingDropdownMenuStyle` | `actionFlyoutStyle` |
| Component style `dropdownMenuStyle` | `flyoutStyle` |
| Dimension `floatingDropdownMenuIconSize` | `actionFlyoutIconSize` |
| Dimension `floatingDropdownShadowElevation` | `actionFlyoutShadowElevation` |

Replace custom Compose rows with `item(...)` declarations. Arbitrary children
are no longer supported. Explicitly typed content lambdas now use the
non-composable `AndroidKitActionFlyoutScope.() -> Unit` type.
Use named arguments when migrating the old positional `offset` overload.

Toolbar and action-bar callers continue to use their existing `flyout { ... }`
DSL. Their actions and separators now render through the same standalone flyout.
