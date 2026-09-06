# Settings pages

`AndroidKitSettingsPage` is the public settings surface. It composes
`AndroidKitPage` and owns scrolling, section spacing, and edge-to-edge content
clearance. The standalone settings-section component has been removed; its
renderer is internal. Hosts use the public page and entry DSLs.

```kotlin
AndroidKitSettingsPage(title = settingsTitle, onBack = onBack) {
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
    securitySection(label = securityLabel, appLock = appLockSetting) {
        if (appLockEnabled) button(label = lockNowLabel, onClick = onLockNow)
    }
    item(key = "notice") { Text(notice) }
}
```

Sections appear in declaration order. Custom section and page-item keys must be
unique strings. General defaults to key `general`; Security defaults to
`security`. Null predefined configurations hide the corresponding entries.
Empty sections, including custom sections, produce no heading, divider, or gap.
The section DSL is composable, so labels can be resolved with `stringResource`
where entries are declared. Custom `item` content inside a section receives a
`RowScope` and the same entry padding as built-in controls.

## Host-owned choices and state

`AndroidKitSettingsSelection` contains a label, stable keyed options, selected
ID, selection callback, and localized Close description. IDs must be unique and
the selected ID must be present. The host supplies every option: Android Kit
does not define supported languages, language names, or application themes. A
host can include custom themes such as Prism.

Use `systemOption` when a host offers a system choice. The host supplies its
localized label and the localized label of the currently resolved value; Android
Kit renders the shared `System (current value)` form in both the settings row
and picker. The host still owns which languages or themes are available.

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
change the switch. Additional security controls use the section entry DSL.

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
