# Settings pages

`AndroidKitSettingsPage` is the public settings surface. It composes
`AndroidKitPage` and owns scrolling, section spacing, and edge-to-edge content
clearance. The standalone settings-section component has been removed; its
renderer is internal. Hosts use the public page and entry DSLs.

```kotlin
AndroidKitSettingsPage(
    configuration = AndroidKitSettingsPageConfiguration.Main(
        about = AndroidKitSettingsLink(onClick = onAbout),
    ),
    title = settingsTitle,
) {
    section(key = "language", label = languageSectionTitle) {
        language(languageSetting)
    }
    section(key = "appearance", label = appearanceTitle) {
        theme(themeSetting)
        transparency(opacitySetting)
    }
    section(key = "media", label = mediaLabel) {
        toggle(label = autoplayLabel, checked = autoplay, onCheckedChange = onAutoplay)
        navigation(label = downloadsLabel, onClick = onOpenDownloads)
    }
    section(key = "security", label = securityTitle) { appLock(appLockSetting) }
    section(key = "notice") { info(label = notice) }
}
```

Hosts own section titles, grouping, and entry order. Language can appear alone or
alongside other entries. Section keys must be unique; each predefined entry kind
can occur once per section. Omit a declaration to hide it.
Empty sections, including custom sections, produce no heading, divider, or gap.
Page and section builders are non-composable. Resolve `stringResource`, painters,
and other composable inputs before entering them. Use `info(label, value,
supportingText, icon)` for read-only rows; `button`, `navigation`, `toggle`, and
`slider` declare Kit-rendered controls. Arbitrary page and row `item` slots are
removed. App-owned settings subpages use the same page and entry DSLs.

## Host-owned choices and state

Predefined Language, Theme, and App lock entries have fixed Kit-owned icons.
Transparency remains iconless. Hosts cannot supply generic descriptions or icons
for predefined entries. App lock accepts an optional `errorMessage` for a host
authentication failure; it is absent during normal operation. Custom entries
retain host-owned labels, descriptions, and optional icons.

`AndroidKitSettingsSelection` contains stable keyed options, selected
ID, and a selection callback. Kit owns the row and Close labels. IDs must be unique and
the selected ID must be present. The host supplies every option: Android Kit
does not define the host language list, language names, or application themes. All
host languages must be supported by Kit translations. A
host can include custom themes such as Prism.

`systemOption` is required for settings selections. The host supplies its
stable ID and the localized label of the currently resolved value; Android
Kit renders the shared `System (current value)` form in both the settings row
and picker. The host still owns which languages or themes are available. This
keeps the shared presentation enforced until the host explicitly selects a
different value.

`AndroidKitLanguageSetting` uses Kit-owned search and empty-result labels.
Language search ignores case and accents. The search field is part of the
sheet's measured chrome so it stays below the title while the choices scroll.
Selection and dismissal clear the query. Theme selection uses the same choice
presentation in a sheet that fits its content.

`AndroidKitFloatingOpacitySetting` accepts a finite level in `0f..100f` and uses
Kit-owned Min/Max labels. It uses steps of five, with no visible numeric value.
The existing theme mapping remains 0.8 alpha at Min and 1.0 at Max. Hosts update
their theme from `onValueChange` for live preview and save the current value from
`onValueChangeFinished`. This setting does not change theme tokens itself.

`AndroidKitAppLockSetting` is controlled by host-confirmed state. Hosts perform
authentication and persistence; a requested change does not optimistically
change the switch. Optional `timeout` and `onLockNow` configuration add the shared
timeout row and Lock now action only while `checked` is true. `enabled = false`
disables all three controls. Kit owns the Lock now and timeout row labels.

```kotlin
AndroidKitAppLockSetting(
    checked = appLockEnabled,
    onCheckedChange = onAppLockChange,
    timeout = AndroidKitAppLockTimeoutSetting(
        options = timeoutOptions,
        selectedId = selectedTimeoutId,
        onSelected = onTimeoutSelected,
    ),
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

## About

`Main(about = link)` appends one About navigation row after all host sections.
`Main()` omits it. The row has a Kit-owned icon, label, and directional chevron,
without a subtitle. Hosts cannot reposition it.

Render `AndroidKitSettingsPageConfiguration.About(AndroidKitSettingsAbout(...))`
for the fixed About subpage. Kit owns its title, grouping, and order:

1. App information: required `appName` and optional `description`, then optional
   `website` and `sourceCode` rows, then required `version`. Website opens the app's
   public website; Source code opens its repository. Neither link has a subtitle.
   The version row copies its current value when tapped.
2. Contact: optional `contact` in a standalone section, with the shared
   Feedback or questions subtitle.
3. Legal information: optional `privacyPolicy`, `termsOfUse`, and `libraries`
   (Third-party licenses), then `additionalLegalEntries` in host-supplied order.
   Third-party licenses can cover both open-source and proprietary dependencies.

The About page retains its title and row labels, but has no section headings.
Separate cards and spacing distinguish app details, Contact, and legal entries.
There are no separate Project, Links, or Open source sections.

Empty groups and blank app descriptions are omitted without reserved space.
`AndroidKitSettingsLink` accepts only a click callback and enabled state; the host
owns navigation and Kit owns standard labels, icons, and rendering. Additional
legal entries retain host-localized titles and optional supporting text, with
unique nonblank IDs. No destinations are inferred.

## Migration

Replace `generalSection` and `securitySection` with host-titled `section` calls
and declare `language`, `theme`, `transparency`, and `appLock` inside them.
Remove predefined `icon` and `supportingText` arguments. Pass authentication
failures as `errorMessage` on App lock. Supply `appName` in About. Main's About
link and About's destinations are now optional; app name and version are required.
Ordinary `Subpage` configurations have no automatic About row.
