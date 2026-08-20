# Testing Android Kit

All test source lives in the top-level `test` module so published artifacts do
not carry test-only dependencies or fixtures.

## Test layers

- `test/src/androidTest` contains behavior and integration tests for intent
  factories, explicit-locale formatting, Compose state restoration, component
  semantics, compact navigation overflow and independent Navigation 3 stacks.
- `test/src/screenshotTest` contains host-side Compose screenshot tests. The
  matrix covers compact phones, landscape phones, folded and unfolded devices,
  portrait and landscape tablets, desktop windows, 1.5x font scale and RTL.
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

Review every generated image, then commit the accepted files under
`test/src/screenshotTestDebug/reference`. Validate later changes with:

```powershell
.\gradlew.bat :test:validateDebugScreenshotTest
```

The validation report is generated under
`test/build/reports/screenshotTest/preview/debug`.

Do not update reference images merely to make a failure disappear. First decide
whether the visual change is an intentional API or design change.
