# Lock page

`AndroidKitLockPage` is a title-free access-gate page in the `compose` artifact.
Use it inside `AndroidKitTheme` and render protected content only when the host
has established an unlocked session.

```kotlin
AndroidKitLockPage(
    message = stringResource(R.string.lock_message),
    unlockLabel = stringResource(R.string.unlock),
    onUnlock = ::requestUnlock,
    isUnlocking = authenticationInProgress,
    errorMessage = authenticationError,
)
```

The component owns centered layout, theme spacing, system-bar clearance and
scrolling when content exceeds the viewport. It uses the Kit lock icon unless
the host supplies an `ImageVector`. All strings belong to the host. While
`isUnlocking` is true, progress replaces the button to prevent duplicate unlock
requests. Errors use Material error color and a polite accessibility live region.

The host owns biometric/device-credential authentication, lifecycle observation,
lock timeouts, protected data, and navigation after unlocking. This component
does not authenticate users or provide a security boundary on its own.

The demo catalog includes ready, authenticating and retry states.
