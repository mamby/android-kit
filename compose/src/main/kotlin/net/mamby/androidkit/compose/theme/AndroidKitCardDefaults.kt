package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable

public object AndroidKitCardDefaults {
    @Composable
    public fun colors(): CardColors = CardDefaults.cardColors(
        containerColor = AndroidKitThemeTokens.colorScheme.surface,
        contentColor = AndroidKitThemeTokens.colorScheme.onSurface,
    )

    @Composable
    public fun border(): BorderStroke = BorderStroke(
        width = AndroidKitThemeTokens.dimensions.floatingSurfaceBorderWidth,
        color = AndroidKitThemeTokens.colorScheme.outlineVariant,
    )
}
