package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

internal val LocalAndroidKitNavigationInsets = staticCompositionLocalOf<WindowInsets> {
    WindowInsets(0, 0, 0, 0)
}

internal val LocalAndroidKitNavigationOverlayProtection = staticCompositionLocalOf { 0.dp }

internal val LocalAndroidKitBottomOverlayProtection =
    staticCompositionLocalOf<(key: Any, expanded: Boolean) -> Unit> { { _, _ -> } }

@Composable
public fun androidKitContentWindowInsets(): WindowInsets =
    WindowInsets.safeDrawing
        .exclude(WindowInsets.ime)
        .union(LocalAndroidKitNavigationInsets.current)
