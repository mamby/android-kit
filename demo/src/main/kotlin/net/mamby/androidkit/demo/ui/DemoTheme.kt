package net.mamby.androidkit.demo.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.theme.AndroidKitDefaults
import net.mamby.androidkit.compose.theme.AndroidKitStrings
import net.mamby.androidkit.compose.theme.AndroidKitThemeDefinition
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import net.mamby.androidkit.demo.R

enum class DemoThemeChoice {
    Light,
    Dark,
    Prism,
}

val PrismThemeDefinition = AndroidKitThemeDefinition(
    colorScheme = darkColorScheme(
        primary = Color(0xFFAFA4FF),
        onPrimary = Color(0xFF251568),
        primaryContainer = Color(0xFF513CC4),
        onPrimaryContainer = Color(0xFFE6DFFF),
        secondary = Color(0xFFFFB3B2),
        onSecondary = Color(0xFF68000A),
        secondaryContainer = Color(0xFF922531),
        onSecondaryContainer = Color(0xFFFFDAD8),
        tertiary = Color(0xFFFFC857),
        onTertiary = Color(0xFF432C00),
        tertiaryContainer = Color(0xFF614100),
        onTertiaryContainer = Color(0xFFFFDEA1),
        background = Color(0xFF17132E),
        onBackground = Color(0xFFF0EBFF),
        surface = Color(0xFF17132E),
        onSurface = Color(0xFFF0EBFF),
        surfaceVariant = Color(0xFF49415F),
        onSurfaceVariant = Color(0xFFCCC3DD),
        outline = Color(0xFF968DA8),
        outlineVariant = Color(0xFF49415F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
    ),
    typography = AndroidKitDefaults.typography,
    shapes = AndroidKitDefaults.shapes,
)

fun DemoThemeChoice.definition(): AndroidKitThemeDefinition = when (this) {
    DemoThemeChoice.Light -> AndroidKitThemes.Light
    DemoThemeChoice.Dark -> AndroidKitThemes.Dark
    DemoThemeChoice.Prism -> PrismThemeDefinition
}

@Composable
fun androidKitStrings(): AndroidKitStrings = AndroidKitStrings(
    back = stringResource(R.string.action_back),
    add = stringResource(R.string.action_add),
    close = stringResource(R.string.action_close),
    more = stringResource(R.string.action_more),
    retry = stringResource(R.string.action_retry),
    cancel = stringResource(R.string.action_cancel),
    confirm = stringResource(R.string.action_confirm),
    save = stringResource(R.string.action_save),
)
