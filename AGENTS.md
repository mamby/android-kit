# Android Kit contributor notes

Use Kotlin, Jetpack Compose, AndroidX, Material 3, Navigation 3, coroutines and
official Android APIs. Keep the published modules app-agnostic and keep demo-only
branding and the Prism theme inside `demo`.

All automated test source belongs to the top-level `test` module. Before changing
or executing the test suites, read [docs/testing.md](docs/testing.md). Do not put
tests inside published modules.

Never copy code directly from the inspiration applications. Generalize useful
patterns behind small, typed APIs and prove them in the demo catalog first.
