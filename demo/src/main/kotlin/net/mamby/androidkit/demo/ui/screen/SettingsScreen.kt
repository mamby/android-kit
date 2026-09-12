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
import net.mamby.androidkit.compose.form.AndroidKitSettingsAbout
import net.mamby.androidkit.demo.BuildConfig
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.demo.ui.DemoAppLockTimeout
import net.mamby.androidkit.localization.AppLocaleManager

@Composable
fun SettingsScreen(
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
    val actionCloseText = stringResource(R.string.action_close)
    val themeLightText = stringResource(R.string.theme_light)
    val themeDarkText = stringResource(R.string.theme_dark)
    val themePrismText = stringResource(R.string.theme_prism)
    val systemThemeText = stringResource(
                        if (isSystemInDarkTheme()) R.string.theme_dark else R.string.theme_light,
                    )
    val floatingSurfaceOpacityDescriptionText = stringResource(R.string.floating_surface_opacity_description)
    val settingsAppLockDescriptionText = stringResource(R.string.settings_app_lock_description)
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
            action = AndroidKitSettingsAction(stringResource(R.string.settings_support_action), { showSupportPreview = true }),
        ),
        getInvolved = AndroidKitSettingsGetInvolved(
            reportIssue = AndroidKitSettingsAction(stringResource(R.string.settings_report_issue), {
                uriHandler.openUri("$CatalogRepository/issues/new")
            }),
            suggestImprovement = AndroidKitSettingsAction(stringResource(R.string.settings_suggest_improvement), {
                uriHandler.openUri("$CatalogRepository/issues/new")
            }),
        ),
        about = catalogAbout(),
    )
    AndroidKitSettingsPage(configuration = configuration, title = stringResource(R.string.settings_title)) {
        generalSection(
            language = AndroidKitLanguageSetting(
                selection = AndroidKitSettingsSelection(
                    options = SupportedLanguageTags.map { tag ->
                        AndroidKitSettingsOption(tag, nativeLanguageName(tag))
                    },
                    selectedId = localeManager.selectedLanguageTag() ?: "system",
                    onSelected = { id -> localeManager.setApplicationLanguage(id.takeUnless { it == "system" }) },
                    systemOption = AndroidKitSettingsSystemOption(
                        id = "system",
                        currentValueLabel = localeManager.systemLocale().getDisplayLanguage(displayLocale),
                    ),
                ),
            ),
            theme = AndroidKitSettingsSelection(
                options = listOf(
                    AndroidKitSettingsOption(DemoThemeChoice.Light.name, themeLightText),
                    AndroidKitSettingsOption(DemoThemeChoice.Dark.name, themeDarkText),
                    AndroidKitSettingsOption(DemoThemeChoice.Prism.name, themePrismText),
                ),
                selectedId = themeChoice.name,
                onSelected = { onThemeChoice(DemoThemeChoice.valueOf(it)) },
                systemOption = AndroidKitSettingsSystemOption(
                    id = DemoThemeChoice.System.name,
                    currentValueLabel = systemThemeText,
                ),
            ),
            floatingOpacity = AndroidKitFloatingOpacitySetting(
                value = floatingSurfaceOpacityLevel,
                onValueChange = onFloatingSurfaceOpacityLevelChange,
                onValueChangeFinished = onFloatingSurfaceOpacityLevelChangeFinished,
                supportingText = floatingSurfaceOpacityDescriptionText,
            ),
        )
        securitySection(
            appLock = AndroidKitAppLockSetting(
                checked = appLockEnabled,
                onCheckedChange = onAppLockChange,
                enabled = !appLockBusy,
                supportingText = appLockError ?: settingsAppLockDescriptionText,
                onLockNow = onLockNow,
                timeout = AndroidKitAppLockTimeoutSetting(
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

private const val CatalogRepository = "https://github.com/mamby/android-kit"

@Composable
private fun catalogAbout(): AndroidKitSettingsAbout {
    val uriHandler = LocalUriHandler.current
    return AndroidKitSettingsAbout(
        appName = stringResource(R.string.app_name),
        description = stringResource(R.string.about_body),
        maintainer = stringResource(R.string.about_maintainer),
        version = BuildConfig.VERSION_NAME,
        sourceCode = AndroidKitSettingsAction(stringResource(R.string.about_source), { uriHandler.openUri(CatalogRepository) }),
        contributors = AndroidKitSettingsAction(stringResource(R.string.about_contributors), {
            uriHandler.openUri("$CatalogRepository/graphs/contributors")
        }),
        license = AndroidKitSettingsAction(stringResource(R.string.about_license), {
            uriHandler.openUri("$CatalogRepository/blob/main/LICENSE")
        }),
    )
}
