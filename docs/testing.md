# Testing Android Kit

All test source lives under the top-level `test` area so published artifacts do
not carry test-only dependencies or fixtures.

## Test layers

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

## Instrumented behavior tests

With a device or emulator connected:

```powershell
.\gradlew.bat :test:connectedDebugAndroidTest
```

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
