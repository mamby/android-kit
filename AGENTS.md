# Android Kit contributor notes

Use Kotlin, Jetpack Compose, AndroidX, Material 3, Navigation 3, coroutines and
official Android APIs. Keep the published modules app-agnostic and keep demo-only
branding and the Prism theme inside `demo`.

AndroidKit is opinionated. For new components, keep shape, typography, icons,
labels, control arrangement, chrome, and interaction behavior Kit-owned and
sealed by default. Public APIs accept typed content/data, state, callbacks,
availability options, placement, and supported theme colors. Do not expose
component style objects or parameters that let hosts redesign Kit controls, or
arbitrary rendering slots for headers, controls, menus, settings rows, or badges.
Follow shared Kit theme tokens internally; preserve app body slots in pages,
cards, sheets, and navigation. Hosts wanting a different visual direction should
implement their own component. Depart from this default only when explicitly
requested. Existing style APIs are not authorization to copy their extensibility
into new components, nor a request to migrate them outside the current scope.
See [docs/component-contract.md](docs/component-contract.md).

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

## Translation quality

English resources are the semantic reference for every locale. Before changing
translations, read [the localization workflow](docs/localization.md#translation-quality-workflow)
and [the terminology glossary](docs/translation-glossary.md). Inspect the actual
component or demo usage; translate its UX meaning, never isolated words.

Prefer official platform terminology and corroborated usage in established apps
in the target language for equivalent actions. Popularity alone is not evidence
of correctness. Preserve the project's accepted vocabulary and regional register.
For ambiguous terms, consult primary sources and record the sources actually
checked; never invent a reference or claim native-speaker approval for AI review.

Check the English reference for mistakes before propagating it. Correct clear
source errors when the intended meaning is established by the request and usage,
and document the correction. Keep translated meaning, timing, optionality,
placeholders and action versus heading roles aligned with English. Re-review all
affected locales, including demo duplicates, and record coverage and unresolved
linguistic uncertainty. Resource/build validation does not prove language quality.
