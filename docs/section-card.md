# Section cards

`AndroidKitSectionCard` lives in `net.mamby.androidkit.compose.presentation`.
It accepts `entries`, an outer `modifier`, an optional `title` above the card,
and an optional `description` below it. An empty list renders nothing.

`AndroidKitSectionCardEntry` is sealed:

- `Custom(key, interaction?) { ... }` supplies a host-owned composable entry body.
  It inherits Kit entry-label typography and card content color. The card owns
  entry padding and optional full-row interaction. The host owns layout within
  the padded body, controls and state. Without an interaction, the body retains
  its own control semantics. The card also owns title, description, shape,
  colors, border, dividers and keyed identity.
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
when the content can exceed the viewport. Every entry receives the same Kit-owned
content padding: `sectionCardHorizontalPadding` horizontally and
`settingSectionEntryVerticalPadding` vertically (18 dp and 16 dp by default).
Titles, descriptions and dividers share the horizontal inset. This does not change
`AndroidKitCard` or the general `spaceMedium` token.

`AndroidKitSectionCardInteraction.Click(onClick, actionLabel?, enabled)` and
`Toggle(checked, onCheckedChange, enabled)` attach interaction to the Custom entry's
surrounding wrapper. The entire row, including padding, is interactive; the Kit
owns the minimum touch target, ripple, keyboard focus, disabled semantics, and
Button or Switch role. A supplied click action label must be nonblank and localized;
otherwise accessibility uses the body's visible text. Built-in Action entries and
Settings use this same interaction renderer.

Click also accepts optional `onLongClick` and `longClickLabel` parameters. Supply
a localized, nonblank label such as "Open contact actions" for accessibility.
Tap and long press both cover the entire entry, including its padding. The Kit
uses Compose `combinedClickable` when a long-click callback is supplied, retaining
normal `clickable` behavior otherwise. Disabled entries invoke neither callback.
The callback can request a context menu or another host-owned contextual action;
the entry does not automatically create or position a menu.

```kotlin
AndroidKitSectionCardEntry.Custom(
    key = "contact",
    interaction = AndroidKitSectionCardInteraction.Click(
        onClick = onOpenContact,
        actionLabel = openContactLabel,
        onLongClick = onShowContactActions,
        longClickLabel = contactActionsLabel,
    ),
) {
    Text(contactName)
}
```

Custom bodies must omit equivalent outer padding, full-row `clickable`/`toggleable`,
and minimum touch-target sizing when an interaction is supplied. Render a toggle's
Switch with `onCheckedChange = null` so the wrapper owns the action. Hosts still own
spacing between controls inside their bodies. For bodies containing independent
controls, omit the row interaction and keep each control's own behavior.

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
        AndroidKitSectionCardEntry.Custom(
            key = "custom-content",
            interaction = AndroidKitSectionCardInteraction.Click(
                onClick = onOpenCustom,
                actionLabel = openCustomLabel,
            ),
        ) {
            Text(text = customMessage)
        },
    ),
)
```

Settings sections call `AndroidKitSectionCard` directly, adapting existing controls
to the same `Custom` entry API available to hosts. An internal overload preserves
Settings compatibility parameters; it does not introduce a separate entry model
or generic layout component. The component owns the frame, entry padding, dividers and keyed
identity. Settings declares Click and Toggle interactions through the same public
Custom-entry API as external hosts. Its internal layout adapter only preserves
existing modifiers and read-only row sizing. Settings-only controls and search
dispatch remain in Settings.
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

In the original 2026-09-29 review, the Personal Health Vault contact detail screen was reviewed read-only. Each
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

## Custom-entry migration

Contacts-style rows with custom icons can retain their Row, text and icon content.
Move the row's click callback, localized action label and enabled state into
`Custom(interaction = AndroidKitSectionCardInteraction.Click(...))`, and remove
its outer padding, `clickable` and minimum-height modifier. The Kit then owns
spacing and the full-row touch area. Consume a snapshot containing this API before
migrating external hosts; the previously published 0.1.38-SNAPSHOT does not include it.
