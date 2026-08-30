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
system. Public components also accept per-instance styles and focused layout or
content overrides; those overrides default to the nearest Android Kit theme and
do not change existing component appearance unless supplied explicitly.

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
