# Architecture and sharing boundaries

Android Kit takes reusable presentation patterns from Personal Health Vault and
Fralov as inspiration, then expresses them as app-agnostic APIs. Neither app is a
consumer yet. Migration begins only after the catalog, behavior tests and visual
matrix are stable.

## Published surface

| Module | Shared responsibility |
| --- | --- |
| `foundation` | Theme preference model and typed factories for share, view, dial, email and map intents |
| `localization` | Official per-app locale selection and locale-explicit date, time, number, currency and list formatting |
| `compose` | Two themes, design tokens, page/detail/grid layouts, loading-empty-error states, metrics, forms, settings rows and sheets, floating actions, and adaptive navigation with overflow |
| `navigation3` | Generic, saveable, independent top-level back stacks and list-detail back behavior |
| `bom` | One version for every published artifact |

The APIs own presentation policy but not app data. Inputs are typed values,
immutable state and callbacks; consumers retain navigation routes, ViewModels,
repositories and side effects.

## Demo-owned surface

The demo owns its Prism theme, launcher branding, routes, catalog copy and sample
state. Prism intentionally proves that a consumer can define a third
`AndroidKitThemeDefinition`; it is not exposed from a published module.

## Keep app-owned

Do not move domain entities, health data, vault encryption, sync, persistence,
analytics, app-specific permissions, application services or branded copy into
the kit. A capability only becomes shared after at least two unrelated consumers
need the same contract and it can be represented without domain terminology.

## Stabilization order

1. Exercise every component in the demo on compact and expanded windows.
2. Approve light/dark screenshot baselines, large-font behavior and RTL.
3. Run behavior tests on a physical device and in CI.
4. Publish a prerelease from the Central Portal workflow.
5. Integrate one low-risk screen in a consumer and collect API feedback.
6. Migrate additional screens only after the public API has remained stable.

Likely future candidates are search/filter presentation, confirmation and date
picker wrappers, permission rationale surfaces and secure-screen policy. Add
them only when a real second consumer proves the abstraction.
