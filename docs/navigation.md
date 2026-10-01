# Page navigation

Use `AndroidKitNavDisplay` from `net.mamby.androidkit:navigation3` for Kit pages.
It owns a sealed, immediate page transition policy: no fade, slide, scale, flip,
or predictive-back page preview. The current page stays in place during a Back
gesture; completing it changes the page, and cancelling it keeps the current page.

Hosts own routes, back stacks, Back callbacks, entry state decorators and adaptive
scene selection. Entry or scene transition metadata cannot override the Kit page
policy. Dialogs, sheets and animations inside page bodies retain their component
behavior. The navigation-button selection highlight is independent of page changes.

The `entries` overload accepts entries already decorated by the host. For multiple
roots, keep one `rememberDecoratedNavEntries` call and saved-state decorator per
root, even while another root is visible. `MultiBackStackNavigationState.backStackFor`
provides each retained stack. The `backStack` overload provides the standard cached
entry decoration and supports host-provided decorators.

The demo and Personal Health Vault use this renderer. Local Events currently uses
Navigation 2 and sets its NavHost enter, exit and pop transitions to `None`.
Its predictive Back uses no entering motion and keeps the current page until the
gesture finishes, matching the Kit page policy.
