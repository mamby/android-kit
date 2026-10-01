# Section cards

`AndroidKitSectionCard` lives in `net.mamby.androidkit.compose.presentation`.
It accepts `entries`, an outer `modifier`, an optional `title` above the card,
and an optional `description` below it. An empty list renders nothing.

`AndroidKitSectionCardEntry` is sealed:

- `Custom(key) { ... }` supplies a host-owned composable entry body. It inherits
  Kit entry-label typography and card content color. The host owns its body
  layout within the padded body, controls, state and accessibility semantics. The card still
  owns entry padding, title, description, shape, colors, border, dividers and keyed identity.
- `Info(key, label, value?, supportingText?)` is read-only. Values and supporting
  text appear below the label so long content retains the available width.
- `Action(key, label, actionLabel, onClick, supportingText?, icon?, enabled)`
  renders a button row without a navigation chevron. The optional
  `ImageVector` is decorative; the visible text names the row. `actionLabel` is
  a nonblank, localized accessibility action description, such as "Call work".
- `Multiline(key, text, label?)` displays read-only text, preserving line breaks
  and wrapping without a line limit. It has no navigation affordance.

Keys must be nonblank, stable and unique within the card. Invalid keys fail fast.
Reordering entries preserves their keyed composition identity, including focus.
Hosts supply localized text and callbacks for built-in entries, or compose their
own `Custom` body. The public API exposes no card style object, header, footer or
divider slot. The outer modifier is for placement. The host provides scrolling
when the content can exceed the viewport. Every entry receives the same Kit-owned content padding: `spaceMedium` horizontally
and `settingSectionEntryVerticalPadding` vertically (16 dp and 12 dp by default).
Custom bodies must omit equivalent outer padding to avoid doubling it. Hosts still
own spacing between controls inside their bodies. Custom controls act within that
padded body; built-in Action rows and Settings controls keep interactions outside
the padding so their entire row remains interactive. Dividers retain their own inset.

```kotlin
AndroidKitSectionCard(
    title = detailsTitle,
    entries = listOf(
        AndroidKitSectionCardEntry.Info("name", nameLabel, personName),
        AndroidKitSectionCardEntry.Action(
            key = "work-phone",
            label = workNumber,
            supportingText = workLabel,
            actionLabel = callWorkLabel,
            onClick = onCallWork,
        ),
        AndroidKitSectionCardEntry.Multiline("notes", notes, notesLabel),
        AndroidKitSectionCardEntry.Custom("custom-content") {
            Text(text = customMessage)
        },
    ),
)
```

Settings sections call `AndroidKitSectionCard` directly, adapting existing controls
to the same `Custom` entry API available to hosts. An internal overload preserves
Settings compatibility parameters; it does not introduce a separate entry model
or generic layout component. The component owns the frame, entry padding, dividers and keyed
identity. Settings attaches row interaction modifiers to the internal entry wrapper. Settings-only controls and search dispatch remain in Settings.
Both entry families use `settingSectionStyle` / dimension tokens for colors,
border, shape, typography, padding, dividers and minimum touch targets. Settings
retains its public DSL, controls, entry definitions and search dispatch unchanged.
There are no dependencies on contact data or platform intents.

Read-only entries merge their text for accessibility without a click action or
button role. Actions use standard Compose `clickable` behavior for semantics,
disabled state, keyboard focus and activation, following the
[official accessibility guidance](https://developer.android.com/develop/ui/compose/accessibility/api-defaults).
Rows use logical layout direction; Settings retains its auto-mirrored chevron. Text
uses theme typography with natural wrapping and no fixed row height.
Section titles expose heading semantics for screen-reader heading navigation.

## Contact-detail suitability

The Personal Health Vault contact detail screen was reviewed read-only. Each
phone, email, website and address can be an `Action`, including addresses with
embedded line breaks. Existing dial, compose-email, open-website and address-search
callbacks remain host-owned. Nonblank notes map to `Multiline`; the name can stay
in the page body or use `Info`. The host still filters blank values and supplies
stable unique keys, localized section titles and action labels. No contact-specific
Kit API, permissions or intent handling is needed.

Public actions omit the chevron, matching the current contact-value rows.
`Multiline` uses entry-label typography, the card content color and the same
entry padding as Info, Action and Custom. Settings-specific rows retain
their existing rendering, including navigation chevrons. No consumer files were
changed and no consumer build or migration was performed.

The catalog includes a custom toggle body, two independently selectable fictional email addresses,
informational content, a disabled action, a multiline address and notes. Selection
updates the demo status; it does not send mail. Fictional sample data remains fixed
English data; surrounding controls reuse existing localized demo resources.

`SectionCardBehaviorTest` covers action semantics, disabled interaction, read-only
grouping, keyboard traversal, focus after reorder, content updates, empty cards,
custom-body typography inheritance and keyed state, and text layout under a
320 dp RTL viewport at 2x font scale. These automated
checks do not constitute TalkBack listening or visual approval across themes.

## Validation (2026-09-29)

- Passed `:compose:compileDebugKotlin`, `:demo:assembleDebug`,
  `:test:assembleDebugAndroidTest`, `:compose:lintDebug` and `:demo:lintDebug`.
  Demo packaging also passed its AndroidKit localization gate. Lint reported
  zero errors, with 79 Compose warnings and one demo warning; none reference
  the section-card files.
- Direct instrumentation on a Samsung SM-A546B running Android 16 exercised
  39 tests: six card behavior tests, eight parameterized pixel-parity tests,
  and 25 existing Settings page/search/community tests. All passed across runs.
  The combined run had three failures: an overly strict whole-TextStyle equality
  assertion (corrected to check typography fields), and two Settings tests that
  lost their Compose hierarchies. The three focused rechecks passed together.
- Pixel comparisons produced zero differing pixels for both contact-value rows
  and notes across eight light/dark, LTR/RTL and 1x/2x-font configurations
  (16 comparisons). The reference reproduces the current consumer rendering
  with fictional data, Health's 12 dp medium spacing and Material typography,
  and Kit light/dark palettes. Images remain in memory; no golden files or
  device captures were saved. This verifies the component against that rendering
  reference, not a migrated Health application build.
- Custom-body tests verify inherited font size, weight, family, letter spacing
  and line height, callback dispatch and retained state after reordering.
  Public multiline actions fill their available text width using Compose layout
  sizing; existing Settings text sizing and typography remain unchanged.
- `git diff --check` passed. Physical-keyboard behavior and TalkBack speech have
  not been manually verified. Nothing was published and no host apps changed.
