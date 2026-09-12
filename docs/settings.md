# Settings pages

`AndroidKitSettingsPage` is the public settings surface. It composes
`AndroidKitPage` and owns scrolling, section spacing, and edge-to-edge content
clearance. The standalone settings-section component has been removed; its
renderer is internal. Hosts use the public page and entry DSLs.

```kotlin
AndroidKitSettingsPage(
    configuration = AndroidKitSettingsPageConfiguration.Main(
        support = support,
        getInvolved = getInvolved,
        about = about,
    ),
    title = settingsTitle,
) {
    generalSection(
        label = generalLabel,
        language = languageSetting,
        theme = themeSetting,
        floatingOpacity = opacitySetting,
    )
    section(key = "media", label = mediaLabel) {
        toggle(label = autoplayLabel, checked = autoplay, onCheckedChange = onAutoplay)
        navigation(label = downloadsLabel, onClick = onOpenDownloads)
    }
    securitySection(label = securityLabel, appLock = appLockSetting)
    section(key = "notice") { info(label = notice) }
}
```

Sections appear in declaration order. Section keys must be
unique strings. General defaults to key `general`; Security defaults to
`security`. Null predefined configurations hide the corresponding entries.
Empty sections, including custom sections, produce no heading, divider, or gap.
Page and section builders are non-composable. Resolve `stringResource`, painters,
and other composable inputs before entering them. Use `info(label, value,
supportingText, icon)` for read-only rows; `button`, `navigation`, `toggle`, and
`slider` declare Kit-rendered controls. Arbitrary page and row `item` slots are
removed. App-owned settings subpages use the same page and entry DSLs.

## Host-owned choices and state

Predefined Language, Theme, and App lock entries own their default icons. Language
and Theme use the translate and sun artwork originally used in Fralov; App lock
uses Lucide LockKeyhole. Omit `icon` (or pass null) to use the shared default, or
supply an `ImageVector` through `AndroidKitSettingsSelection.icon` or
`AndroidKitAppLockSetting.icon` to override it. These icons are decorative; the
row label supplies accessibility text. Custom section entries keep their existing
optional host-owned icons, and floating opacity remains iconless.

`AndroidKitSettingsSelection` contains a label, stable keyed options, selected
ID, selection callback, and localized Close description. IDs must be unique and
the selected ID must be present. The host supplies every option: Android Kit
does not define supported languages, language names, or application themes. A
host can include custom themes such as Prism.

`systemOption` is required for settings selections. The host supplies its
localized label and the localized label of the currently resolved value; Android
Kit renders the shared `System (current value)` form in both the settings row
and picker. The host still owns which languages or themes are available. This
keeps the shared presentation enforced until the host explicitly selects a
different value.

`AndroidKitLanguageSetting` adds localized search and empty-result labels.
Language search ignores case and accents. The search field is part of the
sheet's measured chrome so it stays below the title while the choices scroll.
Selection and dismissal clear the query. Theme selection uses the same choice
presentation in a sheet that fits its content.

`AndroidKitFloatingOpacitySetting` accepts a finite level in `0f..100f` and
localized Min/Max labels. It uses steps of five, with no visible numeric value.
The existing theme mapping remains 0.8 alpha at Min and 1.0 at Max. Hosts update
their theme from `onValueChange` for live preview and save the current value from
`onValueChangeFinished`. This setting does not change theme tokens itself.

`AndroidKitAppLockSetting` is controlled by host-confirmed state. Hosts perform
authentication and persistence; a requested change does not optimistically
change the switch. Optional `timeout` and `onLockNow` configuration add the shared
timeout row and Lock now action only while `checked` is true. `enabled = false`
disables all three controls. Supply a localized `lockNowLabel` with `onLockNow`.

```kotlin
AndroidKitAppLockSetting(
    label = appLockLabel,
    checked = appLockEnabled,
    onCheckedChange = onAppLockChange,
    timeout = AndroidKitAppLockTimeoutSetting(
        label = timeoutLabel,
        options = timeoutOptions,
        selectedId = selectedTimeoutId,
        onSelected = onTimeoutSelected,
    ),
    lockNowLabel = lockNowLabel,
    onLockNow = onLockNow,
)
```

Timeout options use `AndroidKitSettingsOption` with unique IDs and a selected ID
present in the list; no system option is needed. The row shows the selected label
and opens a Material radio-choice dialog. Selection closes it immediately and
invokes the host callback, without a Done button or optimistic state change.
Back or outside dismissal leaves the selection unchanged. Removing the timeout
or its section, unchecking app lock, or disabling either the app-lock setting or
the timeout dismisses an open dialog. Hosts retain authentication, persistence,
duration semantics, and lock enforcement. Other security controls can still use
the section entry DSL. Omitting the new optional fields preserves toggle-only
source usage; consumers must recompile against the updated artifact.

## Subpages and spacing

The `navigation` entry displays the shared directional chevron and invokes a
host callback. Register the destination in the host's existing navigation stack
and render another `AndroidKitSettingsPage` there with its title and Back
callback. There is no internal navigation controller or automatic regrouping.
Retain each destination's `LazyListState` using the host navigation state's
saveable-state support and pass it through `listState`.

`settingsPageSectionSpacing` defaults to `spaceMedium` (16 dp). Horizontal
margins use `screenPadding`; bottom content spacing uses `spaceMedium`. Page
clearance and margins are combined in the list's `contentPadding`, keeping the
viewport edge-to-edge. Do not add a second page-padding modifier in consumers.
