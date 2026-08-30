package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable

public object AndroidKitCardDefaults {
    @Composable
    public fun colors(
        style: AndroidKitCardStyle = AndroidKitThemeTokens.cardStyle,
    ): CardColors = CardDefaults.cardColors(
        containerColor = style.containerColor,
        contentColor = style.contentColor,
    )

    @Composable
    public fun border(
        style: AndroidKitCardStyle = AndroidKitThemeTokens.cardStyle,
    ): BorderStroke = BorderStroke(
        width = style.borderWidth,
        color = style.borderColor,
    )
}
