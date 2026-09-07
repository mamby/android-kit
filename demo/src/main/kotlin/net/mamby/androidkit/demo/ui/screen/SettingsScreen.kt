package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.localization.AppLocaleManager

@Composable
fun SettingsScreen(
    themeChoice: DemoThemeChoice,
    onThemeChoice: (DemoThemeChoice) -> Unit,
    floatingSurfaceOpacityLevel: Float,
    onFloatingSurfaceOpacityLevelChange: (Float) -> Unit,
    onFloatingSurfaceOpacityLevelChangeFinished: () -> Unit,
) {
    val context = LocalContext.current
    val displayLocale = LocalLocale.current.platformLocale
    val localeManager = remember(context) {
        AppLocaleManager(context, SupportedLanguageTags.toSet())
    }
    AndroidKitSettingsPage(title = stringResource(R.string.settings_title)) {
        generalSection(
            label = stringResource(R.string.settings_general),
            language = AndroidKitLanguageSetting(
                selection = AndroidKitSettingsSelection(
                    label = stringResource(R.string.language_section),
                    options = SupportedLanguageTags.map { tag ->
                        AndroidKitSettingsOption(tag, nativeLanguageName(tag))
                    },
                    selectedId = localeManager.selectedLanguageTag() ?: "system",
                    onSelected = { id -> localeManager.setApplicationLanguage(id.takeUnless { it == "system" }) },
                    closeContentDescription = stringResource(R.string.action_close),
                    systemOption = AndroidKitSettingsSystemOption(
                        id = "system",
                        label = stringResource(R.string.language_system),
                        currentValueLabel = localeManager.systemLocale().getDisplayLanguage(displayLocale),
                    ),
                ),
                searchLabel = stringResource(R.string.settings_search_languages),
                emptyResultsLabel = stringResource(R.string.settings_no_languages),
            ),
            theme = AndroidKitSettingsSelection(
                label = stringResource(R.string.settings_theme),
                options = listOf(
                    AndroidKitSettingsOption(DemoThemeChoice.Light.name, stringResource(R.string.theme_light)),
                    AndroidKitSettingsOption(DemoThemeChoice.Dark.name, stringResource(R.string.theme_dark)),
                    AndroidKitSettingsOption(DemoThemeChoice.Prism.name, stringResource(R.string.theme_prism)),
                ),
                selectedId = themeChoice.name,
                onSelected = { onThemeChoice(DemoThemeChoice.valueOf(it)) },
                closeContentDescription = stringResource(R.string.action_close),
                systemOption = AndroidKitSettingsSystemOption(
                    id = DemoThemeChoice.System.name,
                    label = stringResource(R.string.language_system),
                    currentValueLabel = stringResource(
                        if (isSystemInDarkTheme()) R.string.theme_dark else R.string.theme_light,
                    ),
                ),
            ),
            floatingOpacity = AndroidKitFloatingOpacitySetting(
                label = stringResource(R.string.floating_surface_opacity),
                value = floatingSurfaceOpacityLevel,
                minimumLabel = stringResource(R.string.settings_min),
                maximumLabel = stringResource(R.string.settings_max),
                onValueChange = onFloatingSurfaceOpacityLevelChange,
                onValueChangeFinished = onFloatingSurfaceOpacityLevelChangeFinished,
                supportingText = stringResource(R.string.floating_surface_opacity_description),
            ),
        )
        section(key = "about", label = stringResource(R.string.about_section)) {
            item { Text(stringResource(R.string.about_body)) }
        }
        item(key = "scroll-content") { DemoScrollContent() }
    }
}
