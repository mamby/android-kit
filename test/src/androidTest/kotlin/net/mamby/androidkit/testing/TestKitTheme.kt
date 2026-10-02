package net.mamby.androidkit.testing

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import java.util.Locale
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeDefinition
import net.mamby.androidkit.compose.theme.AndroidKitThemes

/** English resources make label assertions independent of the connected device's locale. */
@Composable
internal fun TestKitTheme(
    definition: AndroidKitThemeDefinition = if (isSystemInDarkTheme()) AndroidKitThemes.Dark else AndroidKitThemes.Light,
    content: @Composable TestSettingsPersistence.() -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val persistence = remember(context) { TestSettingsPersistence(context) }
    val resources = remember(context, configuration) {
        context.createConfigurationContext(Configuration(configuration).apply {
            setLocales(LocaleList(Locale.ENGLISH))
        }).resources
    }
    CompositionLocalProvider(LocalResources provides resources, LocalTestSettingsPersistence provides persistence) {
        AndroidKitTheme(definition = definition) { persistence.content() }
    }
}
