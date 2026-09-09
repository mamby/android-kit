<p align="center">
  <img src="demo/brand/source/prism-kit.svg" width="112" alt="Android Kit Prism icon" />
</p>

# Android Kit

Opinionated, reusable Android foundations and Jetpack Compose components with an
adaptive catalog app.

Android Kit is deliberately app-agnostic. It provides fixed light and dark
themes, extensible theme definitions, adaptive page and list presentations,
floating navigation and actions, localization helpers, and Navigation 3 state.

## Modules

| Artifact | Purpose |
| --- | --- |
| `net.mamby.androidkit:foundation` | Theme mode and external Android intents |
| `net.mamby.androidkit:localization` | Per-app locales and locale-aware formatting |
| `net.mamby.androidkit:compose` | Themes, adaptive layouts, components, forms and navigation UI |
| `net.mamby.androidkit:navigation3` | Generic Navigation 3 multi-back-stack state |
| `net.mamby.androidkit:bom` | Aligns Android Kit artifact versions |

The `demo` application is the reference consumer. It includes the two library
themes plus a third, demo-owned Prism theme that proves app-defined theming.
The sharing boundaries and stabilization order are recorded in
[docs/architecture.md](docs/architecture.md).

All Android Kit Compose components must be descendants of `AndroidKitTheme`.
They fail fast when the theme is missing, and consumer styling is supplied as a
custom `AndroidKitThemeDefinition` so every component uses one coherent design
system. Public components accept per-instance styles and typed data, state, and callbacks.
Kit owns control rendering and chrome; app body content remains composable inside
pages, cards, sheets, and navigation. Styles default to the nearest Kit theme and
do not change existing component appearance unless supplied explicitly.

Use [`AndroidKitActionFlyout`](docs/action-flyout.md) for anchored action menus
with typed entries, including menus opened from standalone buttons.

Use [`AndroidKitSettingsPage`](docs/settings.md) for shared settings presentation,
optional host-configured controls, custom sections, and host-owned subpages.
Language lists and all setting values belong to the host application.

Use [`AndroidKitLockPage`](docs/lock-page.md) for a title-free lock screen with
progress and retry feedback. Authentication, lock timing and navigation belong
to the host application.

See [the component API contract](docs/component-contract.md) for ownership and migration.

## Build the catalog

```powershell
.\gradlew.bat :demo:assembleDebug
```

Testing commands are documented in [docs/testing.md](docs/testing.md). Tests are
kept in the top-level `test` module.

Maven Central setup and release mechanics are documented in
[docs/publishing.md](docs/publishing.md). The demo icon and Prism branding are
not packaged in any published library artifact.

## License

Android Kit is available under the MIT License.
