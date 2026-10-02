# Color-only themes

AndroidKit owns typography, shapes, component padding/spacing, icon/control sizes,
border widths and shadow geometry. Hosts configure the color scheme, light/dark
identity, floating-surface transparency and explicit component color roles.

```kotlin
val definition = AndroidKitThemeDefinition(
    colorScheme = appColors,
    isDark = darkTheme,
    floatingSurfaceOpacityLevel = savedTransparency,
    floatingSurfaceColors = AndroidKitFloatingSurfaceColors(
        borderColor = appColors.outlineVariant,
    ),
    componentColors = AndroidKitComponentColors(
        card = AndroidKitCardColors(borderColor = appColors.outline),
        floatingActionButton = AndroidKitFloatingActionButtonColors(
            surfaceColors = AndroidKitFloatingSurfaceColors(
                containerColor = appColors.tertiary,
                contentColor = appColors.onTertiary,
            ),
        ),
    ),
)
AndroidKitTheme(definition) {
    AndroidKitCard(
        title = title,
        colors = AndroidKitCardColors(containerColor = appColors.surfaceContainerLow),
    ) {
        Text(body)
    }
}
```

Color groups contain only `Color` values and nested color groups.
`Color.Unspecified` inherits the relevant component theme color, shared
floating-surface color (for floating roles), or current palette default.
Explicit instance colors take precedence. Replacing a definition's color scheme
recomputes unspecified defaults instead of retaining colors from the old scheme.
Explicit overrides remain explicit. Destructive actions retain the Kit error
color policy, and existing disabled-state color behavior remains intact.

| Component | Supported appearance inputs |
| --- | --- |
| Cards | Container, content, border and supporting-text colors |
| Section cards and Settings | Container, content, border, divider and secondary-content colors; existing switch/slider colors |
| Pages and lock pages | Background and content-protection colors; title/button/flyout surface colors |
| Bottom sheets | Container, content, drag-handle, scrim, chrome and flyout colors |
| Floating buttons, action bars and toolbars | Surface and flyout colors; toolbar separator color |
| Navigation | Containers, selected/unselected content and indicators, disabled colors, drawer badges and overflow surfaces |
| Search fields, tooltips and other floating controls | Shared palette and floating-surface colors |

Floating-surface roles include container, content, border, shadow and disabled
colors. Component colors inherit from the palette unless explicitly overridden.

Container alpha for floating surfaces follows `floatingSurfaceOpacityLevel`,
including surfaces with an explicit container color. Keep the existing persisted
0–100 setting; this migration does not change its range or opacity mapping.

## Reading shared scales

`AndroidKitThemeTokens.typography`, `shapes` and `dimensions` remain readable for
app-owned content. Dimensions have an internal constructor and no copy operation.
Neither a theme definition nor a component can accept replacement scales.

Hosts can use their own Material theme for application bodies. Kit chrome and
controls establish a separate Material theme boundary with Kit typography and
shapes; pages, cards, sheets, navigation and search results preserve host-owned
body content. Surface content colors continue to flow into their bodies.

## Migrating from 0.1.50-SNAPSHOT

This is a source and binary breaking change in `0.1.51-SNAPSHOT`.

| Former input | Replacement |
| --- | --- |
| Theme `typography`, `shapes`, `dimensions` | Remove; use host Material themes and read-only tokens for app bodies |
| Theme component `*Style` | Corresponding group in `componentColors` |
| Theme `floatingSurfaceStyle` | Definition `floatingSurfaceColors` |
| Component `style` | Color-only `colors` |
| Page `titleBarStyle` | `titleBarColors` |
| List page `supportCardStyle` | `supportCardColors` |
| Floating action `Button` / `Bar` `style` | Color-only `colors` |
| Theme `settingSectionStyle` colors | `componentColors.sectionCard` |
| Card padding/spacing, toolbar padding/item spacing | Remove; Kit supplies geometry |
| Page/sheet floating-action margin | Remove; retain supported alignment |
| Sheet body/chrome padding and chrome-content spacing | Remove; apply the returned managed padding once |
| Border width, elevation, shape, typography and shadow dimensions | Remove; Kit supplies defaults |

Outer modifiers, app-list padding/arrangement, window insets, menu
placement/offset/anchor, sheet sizing/fit, chrome visibility and typed action
layout choices remain supported. Existing switch/slider color options and explicit
tints remain color-only inputs. There are no compatibility style overloads.
