package net.mamby.androidkit.compose.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalAndroidKitFloatingNavigationInsets = staticCompositionLocalOf<WindowInsets> {
    WindowInsets(0, 0, 0, 0)
}

@Composable
public fun androidKitContentWindowInsets(): WindowInsets =
    WindowInsets.safeDrawing
        .exclude(WindowInsets.ime)
        .union(LocalAndroidKitFloatingNavigationInsets.current)
