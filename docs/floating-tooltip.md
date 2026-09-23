# Floating tooltip

`AndroidKitFloatingTooltip` renders text and optional typed action data inside
Material's `TooltipBox`. The host owns the anchor, placement and visibility;
Kit seals the tooltip's shape, typography, controls and layout. There are no
custom content slots or per-component style overrides.

The component uses the same `FloatingSurface` as other floating controls:
shared theme colors, border, shadow and `floatingSurfaceOpacityLevel` apply.
Only the background is translucent; text and controls stay opaque. Material's
`TooltipBox` owns positioning and dismissal, while a Kit-owned content layout
avoids Material rich-tooltip baseline/action padding. Text-only and actionable
tooltips use `spaceMedium` at the top and sides, with `spaceSmall` between the
message and actions. Text-only tooltips also use `spaceMedium` at the bottom;
actionable tooltips use `spaceExtraSmall` there because Material buttons already
include internal vertical spacing and full touch targets. There is one background
and shadow. No caret is drawn.

```kotlin
val state = rememberTooltipState(isPersistent = true)
TooltipBox(
    state = state,
    focusable = true,
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    tooltip = {
        AndroidKitFloatingTooltip(
            text = message,
            action = AndroidKitFloatingTooltipAction(
                label = actionLabel,
                onClick = { performAction(); state.dismiss() },
            ),
            onDismiss = { state.dismiss() },
        )
    },
) { /* host-owned anchor */ }
```

Import `net.mamby.androidkit.compose.action.AndroidKitFloatingTooltip` and
`AndroidKitFloatingTooltipAction`. The former is a `TooltipScope` extension.
Material tooltip APIs currently require `ExperimentalMaterial3Api` opt-in at
the host's `TooltipBox` call. Omit `action` and `onDismiss` for text-only feedback.
Action callbacks own dismissal; a disabled action is rendered but cannot run.
The Close label is localized internally. Timed feedback should respect the system
accessibility timeout; search errors already do this.

Demo: **Components → FloatingTooltip**, with text-only and actionable examples.
Search errors and floating action button tooltips use this component. Android's
system clipboard confirmation remains unchanged; no duplicate copy notification
has been added.
