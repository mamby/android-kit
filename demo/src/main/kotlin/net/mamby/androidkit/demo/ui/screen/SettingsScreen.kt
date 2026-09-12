package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitAppLockSetting
import net.mamby.androidkit.compose.form.AndroidKitAppLockTimeoutSetting
import net.mamby.androidkit.compose.form.AndroidKitFloatingOpacitySetting
import net.mamby.androidkit.compose.form.AndroidKitLanguageSetting
import net.mamby.androidkit.compose.form.AndroidKitSettingsOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPage
import net.mamby.androidkit.compose.form.AndroidKitSettingsSelection
import net.mamby.androidkit.compose.form.AndroidKitSettingsSystemOption
import net.mamby.androidkit.compose.form.AndroidKitSettingsPageConfiguration
import net.mamby.androidkit.compose.form.AndroidKitSettingsSupport
import net.mamby.androidkit.compose.form.AndroidKitSettingsGetInvolved
import net.mamby.androidkit.compose.form.AndroidKitSettingsAction
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.demo.ui.DemoAppLockTimeout
import net.mamby.androidkit.localization.AppLocaleManager

@Composable
fun SettingsScreen(
    onAbout: () -> Unit,
    appLockEnabled: Boolean,
    appLockTimeout: DemoAppLockTimeout,
    onAppLockTimeoutChange: (DemoAppLockTimeout) -> Unit,
    onAppLockChange: (Boolean) -> Unit,
    appLockBusy: Boolean,
    appLockError: String?,
    onLockNow: () -> Unit,
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
    val settingsGeneralText = stringResource(R.string.settings_general)
    val languageSectionText = stringResource(R.string.language_section)
    val actionCloseText = stringResource(R.string.action_close)
    val languageSystemText = stringResource(R.string.language_system)
    val settingsSearchLanguagesText = stringResource(R.string.settings_search_languages)
    val settingsNoLanguagesText = stringResource(R.string.settings_no_languages)
    val settingsThemeText = stringResource(R.string.settings_theme)
    val themeLightText = stringResource(R.string.theme_light)
    val themeDarkText = stringResource(R.string.theme_dark)
    val themePrismText = stringResource(R.string.theme_prism)
    val systemThemeText = stringResource(
                        if (isSystemInDarkTheme()) R.string.theme_dark else R.string.theme_light,
                    )
    val floatingSurfaceOpacityText = stringResource(R.string.floating_surface_opacity)
    val settingsMinText = stringResource(R.string.settings_min)
    val settingsMaxText = stringResource(R.string.settings_max)
    val floatingSurfaceOpacityDescriptionText = stringResource(R.string.floating_surface_opacity_description)
    val settingsSecurityText = stringResource(R.string.settings_security)
    val settingsAppLockText = stringResource(R.string.settings_app_lock)
    val settingsAppLockDescriptionText = stringResource(R.string.settings_app_lock_description)
    val settingsLockNowText = stringResource(R.string.settings_lock_now)
    val settingsAppLockTimeoutText = stringResource(R.string.settings_app_lock_timeout)
    val timeoutOptions = DemoAppLockTimeout.entries.map { timeout ->
        AndroidKitSettingsOption(id = timeout.name,
            label = stringResource(
                                when (timeout) {
                                    DemoAppLockTimeout.Immediately -> R.string.settings_lock_immediately
                                    DemoAppLockTimeout.OneMinute -> R.string.settings_lock_after_one_minute
                                    DemoAppLockTimeout.FiveMinutes -> R.string.settings_lock_after_five_minutes
                                    DemoAppLockTimeout.FifteenMinutes -> R.string.settings_lock_after_fifteen_minutes
                                },
                            )
        )
    }
    val uriHandler = LocalUriHandler.current
    var showSupportPreview by rememberSaveable { mutableStateOf(false) }
    val configuration = AndroidKitSettingsPageConfiguration.Main(
        support = AndroidKitSettingsSupport(
            title = stringResource(R.string.settings_support_title),
            description = stringResource(R.string.settings_support_description),
            action = AndroidKitSettingsAction(stringResource(R.string.settings_support_action), { showSupportPreview = true }),
        ),
        getInvolved = AndroidKitSettingsGetInvolved(
            title = stringResource(R.string.settings_get_involved),
            reportIssue = AndroidKitSettingsAction(stringResource(R.string.settings_report_issue), {
                uriHandler.openUri("$CatalogRepository/issues/new")
            }),
            suggestImprovement = AndroidKitSettingsAction(stringResource(R.string.settings_suggest_improvement), {
                uriHandler.openUri("$CatalogRepository/issues/new")
            }),
        ),
        about = catalogAbout(),
        onAbout = onAbout,
    )
    AndroidKitSettingsPage(configuration = configuration, title = stringResource(R.string.settings_title)) {
        generalSection(
            label = settingsGeneralText,
            language = AndroidKitLanguageSetting(
                selection = AndroidKitSettingsSelection(
                    label = languageSectionText,
                    options = SupportedLanguageTags.map { tag ->
                        AndroidKitSettingsOption(tag, nativeLanguageName(tag))
                    },
                    selectedId = localeManager.selectedLanguageTag() ?: "system",
                    onSelected = { id -> localeManager.setApplicationLanguage(id.takeUnless { it == "system" }) },
                    closeContentDescription = actionCloseText,
                    systemOption = AndroidKitSettingsSystemOption(
                        id = "system",
                        label = languageSystemText,
                        currentValueLabel = localeManager.systemLocale().getDisplayLanguage(displayLocale),
                    ),
                ),
                searchLabel = settingsSearchLanguagesText,
                emptyResultsLabel = settingsNoLanguagesText,
            ),
            theme = AndroidKitSettingsSelection(
                label = settingsThemeText,
                options = listOf(
                    AndroidKitSettingsOption(DemoThemeChoice.Light.name, themeLightText),
                    AndroidKitSettingsOption(DemoThemeChoice.Dark.name, themeDarkText),
                    AndroidKitSettingsOption(DemoThemeChoice.Prism.name, themePrismText),
                ),
                selectedId = themeChoice.name,
                onSelected = { onThemeChoice(DemoThemeChoice.valueOf(it)) },
                closeContentDescription = actionCloseText,
                systemOption = AndroidKitSettingsSystemOption(
                    id = DemoThemeChoice.System.name,
                    label = languageSystemText,
                    currentValueLabel = systemThemeText,
                ),
            ),
            floatingOpacity = AndroidKitFloatingOpacitySetting(
                label = floatingSurfaceOpacityText,
                value = floatingSurfaceOpacityLevel,
                minimumLabel = settingsMinText,
                maximumLabel = settingsMaxText,
                onValueChange = onFloatingSurfaceOpacityLevelChange,
                onValueChangeFinished = onFloatingSurfaceOpacityLevelChangeFinished,
                supportingText = floatingSurfaceOpacityDescriptionText,
            ),
        )
        securitySection(
            label = settingsSecurityText,
            appLock = AndroidKitAppLockSetting(
                label = settingsAppLockText,
                checked = appLockEnabled,
                onCheckedChange = onAppLockChange,
                enabled = !appLockBusy,
                supportingText = appLockError ?: settingsAppLockDescriptionText,
                lockNowLabel = settingsLockNowText,
                onLockNow = onLockNow,
                timeout = AndroidKitAppLockTimeoutSetting(
                    label = settingsAppLockTimeoutText,
                    options = timeoutOptions,
                    selectedId = appLockTimeout.name,
                    onSelected = { onAppLockTimeoutChange(DemoAppLockTimeout.valueOf(it)) },
                ),
            ),
        )
    }
    if (showSupportPreview) {
        AlertDialog(
            onDismissRequest = { showSupportPreview = false },
            title = { Text(stringResource(R.string.settings_support_action)) },
            text = { Text(stringResource(R.string.settings_support_preview)) },
            confirmButton = {
                TextButton(onClick = { showSupportPreview = false }) { Text(actionCloseText) }
            },
        )
    }
}
