package net.mamby.androidkit.testing

import androidx.test.uiautomator.uiAutomator

/** Send system Back to the active window, including Compose dialogs and popups. */
internal fun pressBack() {
    uiAutomator {
        // The return value tracks an accessibility event, not the Compose callback.
        // Callers assert the resulting state through their Compose test rule.
        pressBack()
    }
}
