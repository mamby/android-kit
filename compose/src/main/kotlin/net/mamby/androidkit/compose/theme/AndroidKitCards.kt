package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

public object AndroidKitCardDefaults {
    @Composable
    public fun colors(): CardColors = CardDefaults.cardColors(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    public fun border(): BorderStroke = BorderStroke(
        width = AndroidKitThemeTokens.dimensions.floatingSurfaceBorderWidth,
        color = CardOutlineColor,
    )
}

private val CardOutlineColor: Color = Color(0xFFE2E2E2)
