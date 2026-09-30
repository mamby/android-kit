# Testing Android Kit

All test source lives under the top-level `test` area so published artifacts do
not carry test-only dependencies or fixtures.

## Test layers

- `test/src/test` contains device-independent matching and Unicode normalization
  contract tests, executed by `:test:testDebugUnitTest` and CI.
- `test/src/androidTest` contains behavior and integration tests for intent
  factories, explicit-locale formatting, Compose state restoration, component
  semantics, compact navigation overflow and independent Navigation 3 stacks.
- `test/src/screenshotTest` contains host-side Compose screenshot tests. The
  matrix covers compact phones, landscape phones, folded and unfolded devices,
  portrait and landscape tablets, desktop windows, 1.5x font scale and RTL.
- `test/performance` contains release-mode Macrobenchmark journeys and the
  Baseline and Startup Profile generator for the demo catalog.
- The demo application remains the end-to-end manual test surface for the two
  shared themes and its own Prism theme.

## Localization contract build tests

Run `./test/gradle/verify-localization.ps1` from PowerShell 7. It verifies that asset
merging accepts host-owned content and rejects Kit resource overrides, aliases,
generated overrides, dependency overrides, unsupported locale resources, locale
configuration entries, unsupported declared languages, and incompatible language
aliases in resource directories, dependency AARs and locale filters. Fixture sources live
under `test/gradle`; no application source files are edited by the test runner.

## Instrumented behavior tests

With a device or emulator connected:

```powershell
.\gradlew.bat :test:connectedDebugAndroidTest
```

CI executes the behavior suite on the Gradle-managed `pixel2api35` device and
runs the localization packaging fixtures under PowerShell 7. Run that same
managed device locally with:

```powershell
.\gradlew.bat :test:pixel2api35DebugAndroidTest --dependency-verification strict
```

Behavior fixtures use English Kit resources without changing the device's
language. Dedicated RTL and font-scale overrides remain active. Section-card
and lock-page interaction checks also run the official Compose accessibility
checks; these complement manual TalkBack and keyboard verification.

Keep detailed shared search-history behavior in `SearchPageBehaviorTest`;
Settings tests verify catalog integration and host callbacks. Navigation tests
cover independent histories, saved-state restoration, reset, and replacement
guards. No coverage percentage is claimed; unused JaCoCo configuration has been
removed.

## Screenshot baselines

Generate or intentionally update approved reference images:

```powershell
.\gradlew.bat :test:updateDebugScreenshotTest
```

Review every generated image locally. Reference images under
`test/src/screenshotTestDebug/reference` are local artifacts and must not be
committed because screenshots and device captures can contain personal
information. While a local reference set is available, validate later changes with:

```powershell
.\gradlew.bat :test:validateDebugScreenshotTest
```

The validation report is generated under
`test/build/reports/screenshotTest/preview/debug`.

Screenshot comparisons remain local because approved references are not
committed. CI must not regenerate references to treat current rendering as an
approved baseline.

Do not update reference images merely to make a failure disappear. First decide
whether the visual change is an intentional API or design change.

## Baseline profiles and performance benchmarks

Connect a physical device running Android 13 (API 33) or newer, then regenerate
the demo's Baseline and Startup Profiles after changing a critical user journey:

```powershell
.\gradlew.bat :demo:generateBaselineProfile
```

The generated profiles are written under
`demo/src/release/generated/baselineProfiles`. Review and commit them with the
change that affected the journey.

Run the release-mode startup, frame-timing and memory benchmarks with:

```powershell
.\gradlew.bat :test:performance:connectedBenchmarkReleaseAndroidTest
```

Benchmark results are written under
`test/performance/build/outputs/connected_android_test_additional_output`.
