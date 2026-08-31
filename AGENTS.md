# Android Kit contributor notes

Use Kotlin, Jetpack Compose, AndroidX, Material 3, Navigation 3, coroutines and
official Android APIs. Keep the published modules app-agnostic and keep demo-only
branding and the Prism theme inside `demo`.

All automated test source belongs to the top-level `test` module. Before changing
or executing the test suites, read [docs/testing.md](docs/testing.md). Do not put
tests inside published modules. Do not commit generated screenshots, screenshot
reference images, or device captures; keep them local because they can contain
personal information.

Never copy code directly from the inspiration applications. Generalize useful
patterns behind small, typed APIs and prove them in the demo catalog first.

## Dependency version policy

When any repository-managed dependency, package, Gradle plugin, build tool, or
toolchain currently using a prerelease version reaches stable, upgrade to that
stable release. Afterward, remain on stable releases and do not move to a later
alpha, beta, RC, preview, or other prerelease version unless explicitly
requested. This policy applies to AndroidX and non-AndroidX components,
including AGP, Kotlin, Kotlin serialization, screenshot tooling, test
frameworks, and the Gradle wrapper.
