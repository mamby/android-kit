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

## Scope and material implications

Proceed autonomously with routine implementation decisions that are necessary
to complete the explicit request and preserve existing behavior.

Before making a change whose implications are not determined by the request,
stop and ask one concise clarification question. First explain:

- The implicit decision.
- Why it is necessary.
- Its behavioral and technical implications.

Clarification is required for changes affecting public APIs, persisted data,
migrations, user-visible behavior, security or permissions, dependencies,
architecture, compatibility, destructive operations, or substantial unrelated
refactoring.

Do not request confirmation for ordinary in-scope implementation, verification,
formatting, or testing decisions. When possible, choose the option that
preserves existing behavior and minimizes scope.

## Dependency version policy

When any repository-managed dependency, package, Gradle plugin, build tool, or
toolchain currently using a prerelease version reaches stable, upgrade to that
stable release. Afterward, remain on stable releases and do not move to a later
alpha, beta, RC, preview, or other prerelease version unless explicitly
requested. This policy applies to AndroidX and non-AndroidX components,
including AGP, Kotlin, Kotlin serialization, screenshot tooling, test
frameworks, and the Gradle wrapper.
