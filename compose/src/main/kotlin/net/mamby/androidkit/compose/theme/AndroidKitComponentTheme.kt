package net.mamby.androidkit.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

/** Isolates Kit controls from themes applied to host-owned content. */
@Composable
internal fun AndroidKitComponentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AndroidKitThemeTokens.colorScheme,
        typography = AndroidKitDefaults.typography,
        shapes = AndroidKitDefaults.shapes,
        content = content,
    )
}

@Immutable
internal data class AndroidKitHostTheme(
    val colors: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
    val textStyle: TextStyle,
) {
    /** Restore body typography and shapes while retaining the surrounding surface content color. */
    @Composable
    fun Content(content: @Composable () -> Unit) {
        MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes) {
            ProvideTextStyle(textStyle, content)
        }
    }
}

@Composable
internal fun androidKitHostTheme(): AndroidKitHostTheme = AndroidKitHostTheme(
    colors = MaterialTheme.colorScheme,
    typography = MaterialTheme.typography,
    shapes = MaterialTheme.shapes,
    textStyle = LocalTextStyle.current,
)
